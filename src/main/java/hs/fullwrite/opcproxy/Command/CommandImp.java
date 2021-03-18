package hs.fullwrite.opcproxy.Command;


import com.alibaba.fastjson.JSONObject;
import hs.fullwrite.opc.OpcConnectManger;
import hs.fullwrite.opc.OpcExecute;
import hs.fullwrite.opcproxy.session.Session;
import hs.fullwrite.opcproxy.session.SessionManager;
import hs.fullwrite.util.ByteUtil;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/2/16 15:25
 */

public enum CommandImp implements Command {

    /**
     * read opc data
     */
    READ(0x01) {
        @Override
        public void operate(byte[] context, ChannelHandlerContext ctx, SessionManager sessionManager, OpcConnectManger opcConnectManger) {
        }
    },
    /**
     * write opc data
     */
    WRITE(0x02) {
        @Override
        public void operate(byte[] context, ChannelHandlerContext ctx, SessionManager sessionManager, OpcConnectManger opcConnectManger) {
        }
    },
    /**
     * opc connect status
     */
    STATUS(0x03) {
        @Override
        public void operate(byte[] context, ChannelHandlerContext ctx, SessionManager sessionManager, OpcConnectManger opcConnectManger) {
            if (valid(context)) {
                long opcserveid = ByteUtil.byteToLong(context, 3, false, ByteUtil.CType.Int);
                logger.info("opcserveid=" + opcserveid + ":" + analye(context).toJSONString());
            }
        }
    },
    /**
     * send heart msg
     */
    HEART(0x04) {
        @Override
        public void operate(byte[] context, ChannelHandlerContext ctx, SessionManager sessionManager, OpcConnectManger opcConnectManger) {
            if (valid(context)) {
                long opcserveid = ByteUtil.byteToLong(context, 3, false, ByteUtil.CType.Int);
                JSONObject heartmsg = analye(context);
                sessionManager.addSessionModule(opcserveid, heartmsg.getString("function"), ctx);
                /***写进程重连判定在心跳包中进行处理，主要是在连接完成后，再断开重连一下，已应对某些opc服务器单次重连读取数据异常的问题*/
                if (heartmsg.getString("function").equals(OpcExecute.FUNCTION_WRITE) && (opcConnectManger.getOpcconnectpool().get(opcserveid).getWriteopcexecute().getReconnectcount() > 0)) {
                    synchronized (opcConnectManger.getOpcconnectpool().get(opcserveid).getWriteopcexecute()) {
                        try {
                            opcConnectManger.getOpcconnectpool().get(opcserveid).getWriteopcexecute().sendStopItemsCmd();
                            TimeUnit.MICROSECONDS.sleep(100);
                            opcConnectManger.getOpcconnectpool().get(opcserveid).getWriteopcexecute().minsReconnectcount();
                            Session session = sessionManager.removeSessionModule(ctx);
                            if (session != null) {
                                session.getCtx().close();
                            }
                            opcConnectManger.getOpcconnectpool().get(opcserveid).getWriteopcexecute().reconnect();
                        } catch (InterruptedException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }

        }
    },
    /**
     * ack any revice msg
     */
    ACK(0x05) {
        @Override
        public void operate(byte[] context, ChannelHandlerContext ctx, SessionManager sessionManager, OpcConnectManger opcConnectManger) {
            if (valid(context)) {
                logger.info(CommandImp.ACK.analye(context).toJSONString());
            }
        }
    },
    /**
     * stop opc coneect
     */
    STOP(0x06) {
        @Override
        public void operate(byte[] context, ChannelHandlerContext ctx, SessionManager sessionManager, OpcConnectManger opcConnectManger) {

        }


    },
    /**
     * opc data result
     */
    OPCREADRESULT(0x07) {
        @Override
        public void operate(byte[] context, ChannelHandlerContext ctx, SessionManager sessionManager, OpcConnectManger opcConnectManger) {
            if (valid(context)) {

                JSONObject readjson = analye(context);
                long opcserveid = ByteUtil.byteToLong(context, 3, false, ByteUtil.CType.Int);
                /***判断读取数据之前那判断下重连次数值是否为大于0，如果大于0，那么需要进行重连操作，同时把write processor重启*/
                if (readjson.getString("function").equals(OpcExecute.FUNCTION_READ) && (opcConnectManger.getOpcconnectpool().get(opcserveid).getReadopcexecute().getReconnectcount() > 0)) {

                    synchronized (opcConnectManger.getOpcconnectpool().get(opcserveid).getReadopcexecute()) {
                        try {
                            opcConnectManger.getOpcconnectpool().get(opcserveid).getReadopcexecute().sendStopItemsCmd();
                            TimeUnit.MICROSECONDS.sleep(10);
                            opcConnectManger.getOpcconnectpool().get(opcserveid).getReadopcexecute().minsReconnectcount();
                            Session session = sessionManager.removeSessionModule(ctx);
                            if (session != null) {
                                session.getCtx().close();
                            }
                            opcConnectManger.getOpcconnectpool().get(opcserveid).getReadopcexecute().reconnect();
                        } catch (InterruptedException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }

                    synchronized (opcConnectManger.getOpcconnectpool().get(opcserveid).getWriteopcexecute()) {
                        //知道已经断线了，那么也重连下write processor
                        try {
                            Session writesession = sessionManager.getSpecialSession(opcserveid, OpcExecute.FUNCTION_WRITE);
                            opcConnectManger.getOpcconnectpool().get(opcserveid).getWriteopcexecute().sendStopItemsCmd();
                            TimeUnit.MICROSECONDS.sleep(10);
                            if (writesession != null) {
                                sessionManager.removeSessionModule(writesession.getCtx());
                                writesession.getCtx().close();
                            }
                            opcConnectManger.getOpcconnectpool().get(opcserveid).getWriteopcexecute().reconnect();
                        } catch (InterruptedException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }

                    return;//取消本次读取到的内容解析
                }
                opcConnectManger.getOpcconnectpool().get(opcserveid).getReadopcexecute().dealReadAllItemsResult(readjson);
            }
        }
    },
    /**
     * opc write result
     */
    OPCWRITERESULT(0x08) {
        @Override
        public void operate(byte[] context, ChannelHandlerContext ctx, SessionManager sessionManager, OpcConnectManger opcConnectManger) {
            if (valid(context)) {
                logger.debug(analye(context).toJSONString());
            }
        }
    },
    /**
     * add opc item
     */
    ADDITEM(0x09) {
        @Override
        public void operate(byte[] context, ChannelHandlerContext ctx, SessionManager sessionManager, OpcConnectManger opcConnectManger) {

        }
    },
    /**
     * remove opc item
     */
    REMOVEITEM(0x0a) {
        @Override
        public void operate(byte[] context, ChannelHandlerContext ctx, SessionManager sessionManager, OpcConnectManger opcConnectManger) {

        }
    },
    /**
     * add item result
     */
    ADDITEMRESULT(0x0b) {
        @Override
        public void operate(byte[] context, ChannelHandlerContext ctx, SessionManager sessionManager, OpcConnectManger opcConnectManger) {
            if (valid(context)) {
                long opcserveid = ByteUtil.byteToLong(context, 3, false, ByteUtil.CType.Int);
                JSONObject addjson = analye(context);
                if (addjson.getString("function").equals(OpcExecute.FUNCTION_READ)) {
                    opcConnectManger.getOpcconnectpool().get(opcserveid).getReadopcexecute().dealAddItemResult(addjson);
                } else if (addjson.getString("function").equals(OpcExecute.FUNCTION_WRITE)) {
                    opcConnectManger.getOpcconnectpool().get(opcserveid).getWriteopcexecute().dealAddItemResult(addjson);
                }
            }
        }
    },
    /**
     * remove item result
     */
    REMOVEITEMRESULT(0x0c) {
        @Override
        public void operate(byte[] context, ChannelHandlerContext ctx, SessionManager sessionManager, OpcConnectManger opcConnectManger) {
            if (valid(context)) {
                long opcserveid = ByteUtil.byteToLong(context, 3, false, ByteUtil.CType.Int);
                JSONObject removejson = analye(context);
                if (removejson.getString("function").equals(OpcExecute.FUNCTION_READ)) {
                    opcConnectManger.getOpcconnectpool().get(opcserveid).getReadopcexecute().dealRemoveResult(removejson);
                } else if (removejson.getString("function").equals(OpcExecute.FUNCTION_WRITE)) {
                    opcConnectManger.getOpcconnectpool().get(opcserveid).getWriteopcexecute().dealRemoveResult(removejson);
                }
            }

        }
    },
    /**
     * patch add item, suggest only for reconnect ,then patch add already right items
     */
    BATCHADDITEM(0x0d) {
        @Override
        public void operate(byte[] context, ChannelHandlerContext ctx, SessionManager sessionManager, OpcConnectManger opcConnectManger) {

        }
    },

    /**
     * deal patch items result
     */
    BATCHADDITEMRESULT(0x0e) {
        @Override
        public void operate(byte[] context, ChannelHandlerContext ctx, SessionManager sessionManager, OpcConnectManger opcConnectManger) {
            if (valid(context)) {
                long opcserveid = ByteUtil.byteToLong(context, 3, false, ByteUtil.CType.Int);
                JSONObject addjson = analye(context);
                if (addjson.getString("function").equals(OpcExecute.FUNCTION_READ)) {
                    opcConnectManger.getOpcconnectpool().get(opcserveid).getReadopcexecute().dealAddItemResult(addjson);
                } else if (addjson.getString("function").equals(OpcExecute.FUNCTION_WRITE)) {
                    opcConnectManger.getOpcconnectpool().get(opcserveid).getWriteopcexecute().dealAddItemResult(addjson);
                }
            }

        }
    };


    public Logger logger = LoggerFactory.getLogger(CommandImp.class);
    private byte command;;

    private CommandImp(int maincommand) {
        this.command = (byte) (maincommand & 0xff);
    }


    @Override
    public String toString() {
        return super.toString() + command;
    }

    public byte getCommand() {
        return command;
    }

    @Override
    public JSONObject analye(byte[] context) {

        byte[] paramercontext = Arrays.copyOfRange(context, 10, context.length);
        try {
            String str = new String(paramercontext, "UTF-8");
            str = str.replace(":nan,", ":0,").replace(":inf,", ":" + Double.MAX_VALUE + ",").replace(":-inf,", ":" + Double.MIN_VALUE + ",");
            try {
               JSONObject tmp= (JSONObject) JSONObject.parseObject(str);
               //logger.info("analye:"+tmp.toJSONString());
                return tmp;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public byte[] build(byte[] context, long nodeid) {
        int length = context.length + 10;
        byte[] result = new byte[length];
        //header
        result[0] = (byte) 0x88;//new Integer(0x88).byteValue();
        result[1] = (byte) 0x18;//new Integer(0x18).byteValue();
        //command
        result[2] = command;
        //nodeid
        result[3] = (byte) ((nodeid >> 24) & 0xff);
        result[4] = (byte) ((nodeid >> 16) & 0xff);
        result[5] = (byte) ((nodeid >> 8) & 0xff);
        result[6] = (byte) ((nodeid >> 0) & 0xff);
        //context length
        result[7] = (byte) ((context.length >> 16) & 0xff);
        result[8] = (byte) ((context.length >> 8) & 0xff);
        result[9] = (byte) ((context.length >> 0) & 0xff);

        for (int index = 10; index < length; index++) {
            result[index] = context[index - 10];
        }
        return result;
    }

    @Override
    public boolean valid(byte[] context) {
        //头校验
        if (context.length <= 10) {
            return false;
        }
        //header check
        if ((0x88 != context[0]) && (0x18 != context[1])) {
            return false;
        }
        if (command != context[2]) {
            return false;
        }
        byte[] paramercontext = Arrays.copyOfRange(context, 10, context.length);
        try {
            String str = new String(paramercontext, "UTF-8");
//                logger.info(str);
            try {
                JSONObject.parseObject(str);
                return true;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return false;
            }
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public abstract void operate(byte[] context, ChannelHandlerContext ctx, SessionManager sessionManager, OpcConnectManger opcConnectManger);

    public static Map<Byte, CommandImp> getCmdMapping() {
        Map<Byte, CommandImp> mapping = new HashMap<>();
        for (CommandImp cmdipm : values()) {
            mapping.put(cmdipm.getCommand(), cmdipm);
        }
        return mapping;
    }
}
