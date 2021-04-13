package hs.fullwrite.opc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hs.fullwrite.bean.OpcServeInfo;
import hs.fullwrite.bean.Point;
import hs.fullwrite.dao.service.InfluxdbOperateService;
import hs.fullwrite.longTimeServe.InfluxdbWrite;
import hs.fullwrite.longTimeServe.event.InfluxWriteEvent;
import hs.fullwrite.opc.bridge.ExecutePythonBridge;
import hs.fullwrite.opc.event.Event;
import hs.fullwrite.opcproxy.Command.CommandImp;
import hs.fullwrite.opcproxy.session.Session;
import hs.fullwrite.opcproxy.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/5 15:45
 */
public class OpcExecute implements Runnable {
    private Logger logger = LoggerFactory.getLogger(OpcExecute.class);

    public static final String FUNCTION_READ = "read";
    public static final String FUNCTION_WRITE = "write";
    private ExecutePythonBridge executePythonBridge;
    private String exename;
    private String ip;
    private String port;
    private String opcsevename;
    private String opcseveip;
    private String opcsevid;
    private OpcServeInfo serveInfo;
    private SessionManager sessionManager;
    private InfluxdbWrite influxdbWrite;
    private int opcsaveinterval;
    private long writetimestamp = System.currentTimeMillis();
    private String function;


    /**
     * 已经注册的点号
     */
    private Map<String, MeasurePoint> registeredMeasurePointpool = new ConcurrentHashMap();
    /**
     * 待注册的点号
     */
    private Map<String, MeasurePoint> waittoregistertagpool = new ConcurrentHashMap<>();
    /**
     * 事件表
     */
    private LinkedBlockingQueue<Event> eventLinkedBlockingQueue = new LinkedBlockingQueue(Integer.MAX_VALUE);

    /**
     * 重连次数，当为大于0的时候，则在读取到数据(read)或者接受心跳包时(write)重新进行连接
     */

    private int reconnectcount = 0;


    public void addwaitaddIteambuf(MeasurePoint m) {
        waittoregistertagpool.put(m.getPoint().getTag(), m);
    }

    public boolean addOPCEvent(Event event) {
        return eventLinkedBlockingQueue.offer(event);
    }


    public OpcExecute(int opcsaveinterval, String function, OpcServeInfo serveInfo, String exename, String ip, String port, String opcsevename, String opcseveip, String opcsevid, SessionManager sessionManager, InfluxdbWrite influxdbWrite) {
        this.function = function;
        this.serveInfo = serveInfo;
        this.exename = System.getProperty("user.dir") + "\\" + exename;
        this.ip = ip;
        this.port = port;
        this.opcsevename = opcsevename;
        this.opcseveip = opcseveip;
        this.opcsevid = opcsevid;
        this.sessionManager = sessionManager;
        this.influxdbWrite = influxdbWrite;
        this.opcsaveinterval = opcsaveinterval;
        executePythonBridge = new ExecutePythonBridge(exename, ip, port, opcsevename, opcseveip, opcsevid, function);
    }

    private boolean isneedWriteinflux() {

        if (System.currentTimeMillis() - writetimestamp > opcsaveinterval * 1000) {
            writetimestamp = System.currentTimeMillis();
//            logger.info("check is need write");
            return true;
        } else {
//            logger.info("check isn't need write");
            return false;
        }
    }

    public boolean isOpcServeOnline() {

        Session session = sessionManager.getSpecialSession(serveInfo.getServeid(), function);
        if (session != null) {
            if (session.getCtx() != null) {
                return true;
            } else {
                return false;
            }

        } else {
            return false;
        }
    }


    /**
     * 这里有个主意点
     * 在opc连接成功后，有些opc服务器需要断开，再重新连接读到的数据才是正确的，因此
     * 在连接的时候，设置重连次数为1，这样在opc读数据成功那一次，要进行数据更新的时候，
     * 把进行重连操作，使得读取数据不异常！
     */
    public synchronized void connect() {
        if (!isOpcServeOnline()) {
            logger.info("*********need connect " + function);
            //set reconnect count 1
            setReconnectcount(1);
            executePythonBridge.stop();
            executePythonBridge.execute();
            int trycheck = 3;
            while (trycheck-- > 0) {
                if (isOpcServeOnline()) {
                    logger.info("********" + opcsevename + opcseveip + " connect success");
                    registeredMeasurePointpool.clear();
                    if (waittoregistertagpool.size() > 0) {
                        sendPatchAddItemCmd(waittoregistertagpool.values());
                    }
                    break;
                } else {
                    logger.info("*******try connect failed");
                }
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    return;
                }
            }

        }
    }


    public synchronized void reconnect() {
        logger.info("**** reconnect " + function);
        if (!isOpcServeOnline()) {
            executePythonBridge.stop();
            executePythonBridge.execute();
            int trycheck = 3;
            while (trycheck-- > 0) {
                if (isOpcServeOnline()) {
                    logger.info(opcsevename + opcseveip + function + " reconnect success");
                    logger.info(opcsevename + opcseveip + function + "registeredMeasurePoint size=" + registeredMeasurePointpool.size());
                    registeredMeasurePointpool.clear();
                    if (waittoregistertagpool.size() > 0) {
                        sendPatchAddItemCmd(waittoregistertagpool.values());
                    }
                    break;
                } else {
                    logger.info("try reconnect failed");
                }
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    return;
                }
            }
        } else {
            logger.info(" connect status is hold");
        }
    }


    public void sendReadAllItemsCmd() {
        JSONObject msg = new JSONObject();
        msg.put("msg", "read");
        try {
            Session session = sessionManager.getSpecialSession(serveInfo.getServeid(), function);
            session.getCtx().writeAndFlush(
                    CommandImp.READ.build(msg.toJSONString().getBytes("utf-8"), serveInfo.getServeid())
            );
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void dealReadAllItemsResult(JSONObject datajson) {
        if (!datajson.getString("msg").equals("success")) {
            logger.info("read error close it!");
            if (isOpcServeOnline()) {
                Session session = sessionManager.getSpecialSession(serveInfo.getServeid(), function);
                if (session != null) {
                    sessionManager.removeSessionModule(session.getCtx());
                    if (session.getCtx() != null) {
                        session.getCtx().close();
                    }
                }
                return;
            }
        }

        JSONObject influxwritedata = new JSONObject();
        for (String key : datajson.keySet()) {
            if (key.equals("function")) {
                continue;
            }
            MeasurePoint measurePoint = registeredMeasurePointpool.get(key);
            if (measurePoint != null) {
                if (measurePoint.getPoint().getType().equals(Point.FLOATTYPE)) {
                    float value = datajson.getFloatValue(key);
                    //很小的浮点数
                    if (Math.abs(value) < 1E-10) {
                        value = 0f;
                    }

                    measurePoint.setValue(value);
                    String influxdbkey = ((measurePoint.getPoint().getStandard() == null) || (measurePoint.getPoint().getStandard().equals(""))) ? measurePoint.getPoint().getTag() : measurePoint.getPoint().getStandard();
                    influxwritedata.put(influxdbkey, value);
                } else if (measurePoint.getPoint().getType().equals(Point.BOOLTYPE)) {
                    //bool 非0则为1 为0才为0
                    measurePoint.setValue(datajson.getFloatValue(key) != 0 ? 1 : 0);
                    String influxdbkey = ((measurePoint.getPoint().getStandard() == null) || (measurePoint.getPoint().getStandard().equals(""))) ? measurePoint.getPoint().getTag() : measurePoint.getPoint().getStandard();
                    influxwritedata.put(influxdbkey, datajson.getFloatValue(key) != 0 ? 1 : 0);
                }
                measurePoint.setInstant(Instant.now());
            }
        }

        if (influxdbWrite != null && isneedWriteinflux()) {
            InfluxWriteEvent event = new InfluxWriteEvent();
            event.setData(influxwritedata);
            event.setTimestamp(System.currentTimeMillis());
            event.setMeasurement(InfluxdbOperateService.DCSMEASUERMENT);
            influxdbWrite.addEvent(event);
        }

    }


    public void sendWriteItemCmd(String tag, float value) {
        JSONArray jsonArray = new JSONArray();
        JSONObject msg = new JSONObject();
        msg.put("tag", tag);
        msg.put("value", value);
        jsonArray.add(msg);
        try {
            sessionManager.getSpecialSession(serveInfo.getServeid(), function).getCtx().writeAndFlush(
                    CommandImp.WRITE.build(jsonArray.toJSONString().getBytes("utf-8"), serveInfo.getServeid())
            );
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }

    }


    public void sendRemoveItemCmd(String tag) {
        JSONObject msg = new JSONObject();
        msg.put("tag", tag);
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(msg);
        try {
            sessionManager.getSpecialSession(serveInfo.getServeid(), function).getCtx().writeAndFlush(
                    CommandImp.REMOVEITEM.build(jsonArray.toJSONString().getBytes("utf-8"), serveInfo.getServeid())
            );
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void dealRemoveResult(JSONObject datajson) {
        for (String key : datajson.keySet()) {
            if (key.equals("function")) {
                continue;
            }
            if (1 == datajson.getInteger(key)) {
//                MeasurePoint point=waittoregistertag.get(key);
                registeredMeasurePointpool.remove(key);
            }
        }
    }

    public boolean sendAddItemCmd(String tag) {
        JSONObject msg = new JSONObject();
        msg.put("tag", tag);
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(msg);
        try {
            if (sessionManager.getSpecialSession(serveInfo.getServeid(), function).getCtx() != null) {
                sessionManager.getSpecialSession(serveInfo.getServeid(), function).getCtx().writeAndFlush(CommandImp.ADDITEM.build(jsonArray.toJSONString().getBytes("utf-8"), serveInfo.getServeid()));
            } else {
                return false;
            }

        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public void sendPatchAddItemCmd(Collection<MeasurePoint> measurePointCollection) {
        JSONArray jsonArray = new JSONArray();
        for (MeasurePoint mp : measurePointCollection) {
            JSONObject msg = new JSONObject();
            msg.put("tag", mp.getPoint().getTag());
            jsonArray.add(msg);
        }
        try {
            logger.info("BATCHADDITEM " + measurePointCollection.size());
            int indexwaitsendnum = 0;
            JSONArray waitsenddata = new JSONArray();
            //c++最长字符串长度16380
            for (int indexsplit = 0; indexsplit < jsonArray.size(); indexsplit++) {
                if (waitsenddata.toJSONString().getBytes("utf-8").length < 16380) {
                    waitsenddata.add(jsonArray.getJSONObject(indexsplit));
                    indexwaitsendnum++;
                } else {
                    waitsenddata.remove(indexwaitsendnum - 1);
                    if (waitsenddata.size() > 0) {
                        sessionManager.getSpecialSession(serveInfo.getServeid(), function).getCtx().writeAndFlush(
                                CommandImp.BATCHADDITEM.build(waitsenddata.toJSONString().getBytes("utf-8"), serveInfo.getServeid()));
                    }
                    waitsenddata = new JSONArray();
                    indexwaitsendnum = 0;
                    indexsplit--;
                }
            }

            //最后一批点号注册
            if (waitsenddata.size() > 0) {
                logger.info(waitsenddata.toJSONString());
                sessionManager.getSpecialSession(serveInfo.getServeid(), function).getCtx().writeAndFlush(
                        CommandImp.BATCHADDITEM.build(waitsenddata.toJSONString().getBytes("utf-8"), serveInfo.getServeid()));
            }
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
    }


    public void dealAddItemResult(JSONObject datajson) {
        for (String key : datajson.keySet()) {
            if (key.equals("function")) {
                continue;
            }
            if (1 == datajson.getInteger(key)) {
                MeasurePoint point = waittoregistertagpool.get(key);
                registeredMeasurePointpool.put(key, point);
            }
        }
    }


    public void sendStopItemsCmd() {
        JSONObject msg = new JSONObject();
        msg.put("msg", "stop");
        Session session = sessionManager.getSpecialSession(serveInfo.getServeid(), function);
        if (session == null) {
            return;
        }
        try {
            session.getCtx().writeAndFlush(CommandImp.STOP.build(msg.toJSONString().getBytes("utf-8"), new Long(serveInfo.getServeid()).intValue()));
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }

        //移除session
        sessionManager.removeSessionModule(session.getCtx());
        session.getCtx().close();
    }


    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                connect();
                //时间处理
                synchronized (this) {
                    if (isOpcServeOnline()) {
                        while (eventLinkedBlockingQueue.size() != 0) {
                            Event event = eventLinkedBlockingQueue.poll();
                            if ((event != null)) {
                                event.execute(this);
                            }
                        }
                    }
                }

                //数据读取

                if (registeredMeasurePointpool.size() > 0 && function.equals(OpcExecute.FUNCTION_READ)) {
                    synchronized (this) {
                        sendReadAllItemsCmd();
                    }
                }

                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }

    }

    public ExecutePythonBridge getExecutePythonBridge() {
        return executePythonBridge;
    }

    public Map<String, MeasurePoint> getRegisteredMeasurePointpool() {
        return registeredMeasurePointpool;
    }

    public synchronized int getReconnectcount() {
        return reconnectcount;
    }

    public synchronized void setReconnectcount(int reconnectcount) {
        this.reconnectcount = reconnectcount;
    }

    public synchronized void minsReconnectcount() {
        reconnectcount--;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public String getExename() {
        return exename;
    }

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    public String getOpcsevename() {
        return opcsevename;
    }

    public String getOpcseveip() {
        return opcseveip;
    }

    public String getOpcsevid() {
        return opcsevid;
    }

    public OpcServeInfo getServeInfo() {
        return serveInfo;
    }

    public InfluxdbWrite getInfluxdbWrite() {
        return influxdbWrite;
    }

    public int getOpcsaveinterval() {
        return opcsaveinterval;
    }

    public long getWritetimestamp() {
        return writetimestamp;
    }

    public String getFunction() {
        return function;
    }

    public Map<String, MeasurePoint> getWaittoregistertagpool() {
        return waittoregistertagpool;
    }

    public LinkedBlockingQueue<Event> getEventLinkedBlockingQueue() {
        return eventLinkedBlockingQueue;
    }

    public Session getMySession() {
        return sessionManager.getSpecialSession(serveInfo.getServeid(), function);
    }

    public void addRetryEvent(Event event) {
        eventLinkedBlockingQueue.offer(event);
    }
}
