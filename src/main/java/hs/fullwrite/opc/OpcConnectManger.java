package hs.fullwrite.opc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hs.fullwrite.bean.OpcServeInfo;
import hs.fullwrite.bean.Point;
import hs.fullwrite.dao.service.OpcPointOperateService;
import hs.fullwrite.dao.service.OpcServeOperateService;
import hs.fullwrite.longTimeServe.InfluxdbWrite;
import hs.fullwrite.opc.event.RegisterEvent;
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

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/4 13:38
 */
@Component
public class OpcConnectManger /*implements Runnable*/ {
    private Logger logger = LoggerFactory.getLogger(OpcConnectManger.class);

    private Map<Long, OpcServeInfo> opcservepool = new ConcurrentHashMap();
    private Map<Long, OpcGroup> opcconnectpool = new ConcurrentHashMap();

    private SessionManager sessionManager;

    private InfluxdbWrite influxdbWrite;

    private String opcservedir;
    private String nettypoty;

    private int opcsaveinterval;
    private OpcServeOperateService opcServeOperateService;


    private OpcPointOperateService opcPointOperateService;


    private ExecutorService executorService;

    @Autowired
    public OpcConnectManger(@Value("${opcservedir}") String opcservedir,
                            @Value("${nettyport}") String nettypoty,
                            @Value("${opcsaveinterval}") int opcsaveinterval,
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
        this.opcservedir = opcservedir;
        this.opcsaveinterval = opcsaveinterval;
//        executorService.execute(this);
    }

    private OpcGroup connectinit(OpcServeInfo serveInfo) {

        OpcGroup opcGroup = new OpcGroup();
        OpcExecute readopcexecute = new OpcExecute(opcsaveinterval,OpcExecute.FUNCTION_READ, serveInfo, opcservedir, "127.0.0.1", nettypoty, serveInfo.getServename(), serveInfo.getServeip(), serveInfo.getServeid() + "", sessionManager, influxdbWrite);
        OpcExecute writeopcexecute = new OpcExecute(opcsaveinterval,OpcExecute.FUNCTION_WRITE, serveInfo, opcservedir, "127.0.0.1", nettypoty, serveInfo.getServename(), serveInfo.getServeip(), serveInfo.getServeid() + "", sessionManager, influxdbWrite);

        opcGroup.setReadopcexecute(readopcexecute);
        opcGroup.setWriteopcexecute(writeopcexecute);

        List<Point> pointsByServeid = opcPointOperateService.findAllOpcPointsByServeid(serveInfo.getServeid());


//        JSONArray jsonArray = new JSONArray();
        for (Point point : pointsByServeid) {
            RegisterEvent registerEvent = new RegisterEvent();
            registerEvent.setPoint(point);
            readopcexecute.addOPCEvent(registerEvent);
            if (point.getWriteable() == 1) {
                writeopcexecute.addOPCEvent(registerEvent);
            }
//            JSONObject msg=new JSONObject();
//            msg.put("tag",point.getTag());
//            jsonArray.add(msg);
        }

        return opcGroup;
    }

    @PostConstruct
    void init() {
        List<OpcServeInfo> opcServeInfos = opcServeOperateService.findAllOpcServe();
        for (OpcServeInfo serveInfo : opcServeInfos) {

            OpcGroup opcGroup = connectinit(serveInfo);

            executorService.execute(opcGroup.getReadopcexecute());
            executorService.execute(opcGroup.getWriteopcexecute());

            opcconnectpool.put(serveInfo.getServeid(), opcGroup);
            opcservepool.put(serveInfo.getServeid(), serveInfo);


        }

    }


    @PreDestroy
    void close() {
        logger.info("opc connect try to shutdown");
        for (OpcGroup opcGroup : opcconnectpool.values()) {
            opcGroup.getReadopcexecute().sendStopItemsCmd();
            opcGroup.getReadopcexecute().getExecutePythonBridge().stop();

            opcGroup.getWriteopcexecute().sendStopItemsCmd();
            opcGroup.getWriteopcexecute().getExecutePythonBridge().stop();
        }
    }


    public Map<Long, OpcServeInfo> getOpcservepool() {
        return opcservepool;
    }

    public Map<Long, OpcGroup> getOpcconnectpool() {
        return opcconnectpool;
    }

}
