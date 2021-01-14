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
import hs.fullwrite.opc.event.RegisterEvent;
import hs.fullwrite.opc.event.UnRegisterEvent;
import hs.fullwrite.opc.event.WriteEvent;
import hs.fullwrite.opcproxy.CommandImp;
import hs.fullwrite.opcproxy.session.Session;
import hs.fullwrite.opcproxy.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
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
    private Logger logger = LoggerFactory.getLogger(OpcConnect.class);

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
    private long writetimestamp=System.currentTimeMillis();
    private String function;

    private Map<String, MeasurePoint> registeredMeasurePoint = new ConcurrentHashMap();
    private Map<String, MeasurePoint> waittoregistertag = new ConcurrentHashMap<>();
    private LinkedBlockingQueue<Event> eventLinkedBlockingQueue = new LinkedBlockingQueue();

    private int reconnectcount = 0;

    public synchronized void addwaitaddIteambuf(MeasurePoint m) {
        waittoregistertag.put(m.getPoint().getTag(), m);
    }

    public boolean addOPCEvent(Event event) {
        return eventLinkedBlockingQueue.offer(event);
    }


    public OpcExecute(int opcsaveinterval ,String function, OpcServeInfo serveInfo, String exename, String ip, String port, String opcsevename, String opcseveip, String opcsevid, SessionManager sessionManager, InfluxdbWrite influxdbWrite) {
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
        this.opcsaveinterval=opcsaveinterval;
        executePythonBridge = new ExecutePythonBridge(exename, ip, port, opcsevename, opcseveip, opcsevid,function);
    }

    private boolean isneedWrite(){
        if(System.currentTimeMillis()-writetimestamp>opcsaveinterval*1000){
            writetimestamp=System.currentTimeMillis();
            return true;
        }else {
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


    public synchronized void connect() {

//        null == sessionManager.getModulepoolbynodeid().get(serveInfo.getServeid())
        if (!isOpcServeOnline()) {
            logger.info("*********need connect "+function);
            setReconnectcount(1);
            executePythonBridge.stop();
            executePythonBridge.execute();
            int trycheck = 3;
            while (trycheck-- > 0) {
                if (isOpcServeOnline()) {
                    logger.info("********" + opcsevename + opcseveip + " connect success");
                    for (MeasurePoint measurePoint : registeredMeasurePoint.values()) {
                        sendAddItemCmd(measurePoint.getPoint().getTag());
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
//            registeredMeasurePoint.clear();

        } else {
            logger.info(" connect status is hold");
        }
    }


    public synchronized void reconnect() {
        logger.info("**** reconnect "+function);
        if (!isOpcServeOnline()) {
//            setReconnectcount(1);
            executePythonBridge.stop();
            executePythonBridge.execute();
            int trycheck = 3;
            while (trycheck-- > 0) {
                if (isOpcServeOnline()) {
                    logger.info(opcsevename + opcseveip + " reconnect success");
                    for (MeasurePoint measurePoint : registeredMeasurePoint.values()) {
                        sendAddItemCmd(measurePoint.getPoint().getTag());
                    }
                    break;
                } else {
                    logger.info("try reconnect failed");
                }
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    return;
                }
            }
//            registeredMeasurePoint.clear();

        } else {
            logger.info(" connect status is hold");
        }
    }


    public void sendReadAllItemsCmd() {
        JSONObject msg = new JSONObject();
        msg.put("msg", "read");
        try {
            Session session=sessionManager.getSpecialSession(serveInfo.getServeid(),function);
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
                    if(session.getCtx()!=null){
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
            MeasurePoint measurePoint = registeredMeasurePoint.get(key);
            if (measurePoint != null) {
                if (measurePoint.getPoint().getType().equals(Point.FLOATTYPE)) {
                    float value = datajson.getFloatValue(key);
                    //很小的浮点数
                    if (Math.abs(value) < 1E-10) {
                        value = 0f;
                    }

                    measurePoint.setValue(value);
                    String influxdbkey=((measurePoint.getPoint().getStandard()==null)||(measurePoint.getPoint().getStandard().equals("")))?measurePoint.getPoint().getTag():measurePoint.getPoint().getStandard();
                    influxwritedata.put(influxdbkey, value);
                } else if (measurePoint.getPoint().getType().equals(Point.BOOLTYPE)) {
                    //bool 非0则为1 为0才为0
                    measurePoint.setValue(datajson.getFloatValue(key) != 0 ? 1 : 0);
                    String influxdbkey=((measurePoint.getPoint().getStandard()==null)||(measurePoint.getPoint().getStandard().equals("")))?measurePoint.getPoint().getTag():measurePoint.getPoint().getStandard();
                    influxwritedata.put(influxdbkey, datajson.getFloatValue(key) != 0 ? 1 : 0);
                }
                measurePoint.setInstant(Instant.now());
            }
        }

        if (influxdbWrite != null&&isneedWrite()) {
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
            sessionManager.getSpecialSession(serveInfo.getServeid(),function).getCtx().writeAndFlush(
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
            sessionManager.getSpecialSession(serveInfo.getServeid(),function).getCtx().writeAndFlush(
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
                registeredMeasurePoint.remove(key);
            }
        }
    }

    public void sendAddItemCmd(String tag) {
        JSONObject msg = new JSONObject();
        msg.put("tag", tag);
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(msg);
        try {
            sessionManager.getSpecialSession(serveInfo.getServeid(),function).getCtx().writeAndFlush(
                    CommandImp.ADDITEM.build(jsonArray.toJSONString().getBytes("utf-8"), serveInfo.getServeid())
            );
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
                MeasurePoint point = waittoregistertag.get(key);
                registeredMeasurePoint.put(key, point);
            }
        }
    }


    public void sendStopItemsCmd() {
        JSONObject msg = new JSONObject();
        msg.put("msg", "stop");
        Session session=sessionManager.getSpecialSession(serveInfo.getServeid(),function);
        if(session==null){
            return;
        }
        try {
            session.getCtx().writeAndFlush(
                    CommandImp.STOP.build(msg.toJSONString().getBytes("utf-8"), serveInfo.getServeid())
            );
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
            logger.info("*****OPC RUN");
            try {
                connect();

                if (isOpcServeOnline()) {

                    logger.info("*****Connect success try deal event");
                    while (eventLinkedBlockingQueue.size() != 0) {
                        Event event = eventLinkedBlockingQueue.poll();
                        if ((event != null) && (event.getPoint() != null)) {
                            if (event instanceof WriteEvent) {
                                if (registeredMeasurePoint.containsKey(event.getPoint().getTag())) {
                                    WriteEvent writeevent = (WriteEvent) event;
                                    sendWriteItemCmd(writeevent.getPoint().getTag(), writeevent.getValue());

                                }

//                            logger.info("execute write event");
                            } else if (event instanceof RegisterEvent) {
                                RegisterEvent registerevent = (RegisterEvent) event;
                                if (!registeredMeasurePoint.containsKey(registerevent.getPoint().getTag())) {
                                    MeasurePoint measurePoint = new MeasurePoint();
                                    measurePoint.setPoint(registerevent.getPoint());

                                    waittoregistertag.put(registerevent.getPoint().getTag(), measurePoint);
                                    sendAddItemCmd(registerevent.getPoint().getTag());
                                    logger.info("***** even"+registerevent.getPoint().getTag());
                                }
                            } else if (event instanceof UnRegisterEvent) {
                                if (registeredMeasurePoint.containsKey(event.getPoint().getTag())) {
                                    UnRegisterEvent unregisterevent = (UnRegisterEvent) event;
                                    sendRemoveItemCmd(unregisterevent.getPoint().getTag());
                                }

                            }
                        }
                    }
                }

                if (registeredMeasurePoint.size() > 0&&function.equals(OpcExecute.FUNCTION_READ)) {
                    sendReadAllItemsCmd();
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

    public Map<String, MeasurePoint> getRegisteredMeasurePoint() {
        return registeredMeasurePoint;
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
}
