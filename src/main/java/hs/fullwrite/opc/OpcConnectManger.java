package hs.fullwrite.opc;

import hs.fullwrite.bean.OpcServeInfo;
import hs.fullwrite.bean.Point;
import hs.fullwrite.dao.service.OpcPointOperateService;
import hs.fullwrite.dao.service.OpcServeOperateService;
import hs.fullwrite.longTimeServe.InfluxdbWrite;
import hs.fullwrite.opc.event.Event;
import hs.fullwrite.opc.event.RegisterEvent;
import hs.fullwrite.opc.event.WriteEvent;
import hs.fullwrite.opcproxy.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/4 13:38
 */
@Component
public class OpcConnectManger /*implements Runnable*/ {
    private Logger logger = LoggerFactory.getLogger(OpcConnectManger.class);

    private Map<Integer, OpcServeInfo> opcservepool = new ConcurrentHashMap();
    private Map<Integer, OpcExecute> opcconnectpool = new ConcurrentHashMap();

    private SessionManager sessionManager;

    private InfluxdbWrite influxdbWrite;

    private String opcservedir;
    private String nettypoty;

    private OpcServeOperateService opcServeOperateService;


    private OpcPointOperateService opcPointOperateService;


    private ExecutorService executorService;

    @Autowired
    public OpcConnectManger(@Value("${opcservedir}") String opcservedir,
                            @Value("${nettyport}") String nettypoty,
                            InfluxdbWrite influxdbWrite,
                            OpcServeOperateService opcServeOperateService,
                            OpcPointOperateService opcPointOperateService,
                            ExecutorService executorService, SessionManager sessionManager) {
        this.influxdbWrite = influxdbWrite;
        this.opcPointOperateService = opcPointOperateService;
        this.opcServeOperateService = opcServeOperateService;
        this.executorService = executorService;
        this.sessionManager = sessionManager;
        this.nettypoty = nettypoty;
        this.opcservedir=opcservedir;
//        executorService.execute(this);
    }

    private OpcExecute connectinit(OpcServeInfo serveInfo) {

        OpcExecute opcExecute= new OpcExecute(serveInfo,opcservedir, "127.0.0.1", nettypoty,serveInfo.getServename(),serveInfo.getServeip(),serveInfo.getServeid()+"",sessionManager,influxdbWrite);

        List<Point> pointsByServeid = opcPointOperateService.findAllPointsByServeid(serveInfo.getServeid());


        for (Point point : pointsByServeid) {
            RegisterEvent registerEvent = new RegisterEvent();
            registerEvent.setPoint(point);
            opcExecute.addOPCEvent(registerEvent);
//            if (point.getWriteable() == 1) {
//                writeopcConnect.addOPCEvent(registerEvent);
//            }
        }

        return opcExecute;
    }

    @PostConstruct
    void init() {
        List<OpcServeInfo> opcServeInfos = opcServeOperateService.findAllOpcServe();
        for (OpcServeInfo serveInfo : opcServeInfos) {

            OpcExecute execute = connectinit(serveInfo);

            executorService.execute(execute);

            opcconnectpool.put(serveInfo.getServeid(),execute);
            opcservepool.put(serveInfo.getServeid(), serveInfo);


        }

    }


    @PreDestroy
    void close() {
        logger.info("opc connect try to shutdown");
        for (OpcExecute opcExecute : opcconnectpool.values()) {
            opcExecute.sendStopItemsCmd();
            opcExecute.getExecutePythonBridge().stop();
        }
    }


    public Map<Integer, OpcServeInfo> getOpcservepool() {
        return opcservepool;
    }

    public Map<Integer, OpcExecute> getOpcconnectpool() {
        return opcconnectpool;
    }

//    @Override
//    public void run() {
//        while (!Thread.currentThread().isInterrupted()) {
//            try {
//                Integer opcserid = eventLinkedBlockingQueue.take();
//                OpcServeInfo opcServebyid = opcServeOperateService.findOpcServebyid(opcserid);
//                OpcConnIntegrated opcConnIntegrated = connectinit(opcServebyid);
//
//                //运行opc连接
//                executorService.execute(opcConnIntegrated.getReadconn());
//                executorService.execute(opcConnIntegrated.getWriteconn());
//
//
//                //停止旧的
//                OpcConnIntegrated reomveconnect = opcconnectpool.remove(opcServebyid.getServeid());
//                reomveconnect.getWriteconn().setShouldRun(true);
//                reomveconnect.getWriteconn().disconnect();
//
//                reomveconnect.getReadconn().setShouldRun(true);
//                reomveconnect.getReadconn().disconnect();
//
//                //更新集成连接
//                opcconnectpool.put(opcServebyid.getServeid(), opcConnIntegrated);
//                opcservepool.put(opcServebyid.getServeid(), opcServebyid);
//            } catch (InterruptedException e) {
//                logger.error(e.getMessage(), e);
//            }
//        }
//    }
}
