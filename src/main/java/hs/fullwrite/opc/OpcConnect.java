package hs.fullwrite.opc;

import com.alibaba.fastjson.JSONObject;
import hs.fullwrite.bean.Point;
import hs.fullwrite.dao.service.InfluxdbOperateService;
import hs.fullwrite.longTimeServe.InfluxdbWrite;
import hs.fullwrite.longTimeServe.event.InfluxWriteEvent;
import hs.fullwrite.opc.event.Event;
import hs.fullwrite.opc.event.RegisterEvent;
import hs.fullwrite.opc.event.UnRegisterEvent;
import hs.fullwrite.opc.event.WriteEvent;
import opc.item.ItemUnit;
import opc.serve.OPCServe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/11/25 16:10
 * <p>
 * <p>
 * String[] tags = new String[]{
 * "M4111Y01", //1磨辊位移
 * "M4112Y02", //2磨辊位移
 * "M41111AI",//磨机电流
 * "M41191AI",//提升机电流
 * "M4111V01",//磨机振动
 * "A_SLM2_PV5",////反写位号模型判断点号
 * "A_SLM1_PV5",//反写位号规则判断点号
 * "SLM2_BM2",//反写生料磨饱磨系数
 * "SLM1_BM3",//反写生料磨饱磨系数
 * "SLM2_BM3",//反写生料磨饱磨系数
 * "SLM1_BH1",//反写生料磨变换率
 * "SLM1_BH2",//反写生料磨变换率
 * "SLM1_BH3",//反写生料磨变换率
 * "SLM1_BH4",//反写生料磨变换率
 * "SLM2_BH1",//反写生料磨变换率
 * "SLM2_BH2",//反写生料磨变换率
 * "SLM2_BH3",//反写生料磨变换率
 * "SLM2_BH4"//反写生料磨变换率
 * };
 */
//@Component
public class OpcConnect implements Runnable {
    private Logger logger = LoggerFactory.getLogger(OpcConnect.class);

    private String opcservename;
    private String opcip;
    private Map<String, MeasurePoint> registeredMeasurePoint = new ConcurrentHashMap();
    private List<MeasurePoint> waittoregistertag = new CopyOnWriteArrayList<>();
    private LinkedBlockingQueue<Event> eventLinkedBlockingQueue = new LinkedBlockingQueue();
    private InfluxdbWrite influxdbWrite;
    private boolean isShouldRun = false;
    private String localconnectname;

    public OPCServe getOpcServe() {
        return opcServe;
    }

    private OPCServe opcServe;
    private LinkedBlockingQueue<Integer> reconnqueue;//reconnect msg queue
    private Integer opcserid;


    public synchronized void addtag_offline(MeasurePoint m) {
        waittoregistertag.add(m);
    }


    public boolean addOPCEvent(Event event) {
        return eventLinkedBlockingQueue.offer(event);
    }


    public synchronized void reconnect() {
        opcServe.disconnect();
        opcServe.connect();
        if (opcServe.isConectstatus()) {
            for (MeasurePoint measurePoint : registeredMeasurePoint.values()) {
                if (opcServe.registerItem(measurePoint.getPoint().getTag())) {
                    logger.info(measurePoint.getPoint().getTag() + "register success");
                } else {
                    logger.info(measurePoint.getPoint().getTag() + "register failed");
                }
            }
            synchronized (waittoregistertag) {
                for (MeasurePoint measurePoint : waittoregistertag) {
                    if (opcServe.registerItem(measurePoint.getPoint().getTag())) {
                        logger.info(measurePoint.getPoint().getTag() + "register success");
                    } else {
                        logger.info(measurePoint.getPoint().getTag() + "register failed");
                    }
                }
                waittoregistertag.clear();
            }
        }
        logger.info(localconnectname + ":" + opcservename + "reconnect ip=" + opcip + " servename=" + opcservename);
    }


    //    public OpcConnect( @Value("${opc.servename}") String opcservename, @Value("${opc.ip}") String opcip) {
    public OpcConnect(String opcservename, String opcip, InfluxdbWrite influxdbWrite, String localconnectname, LinkedBlockingQueue<Integer> queue, Integer opcserid) {

        this.opcservename = opcservename;
        this.opcip = opcip;
        this.influxdbWrite = influxdbWrite;
        this.localconnectname = localconnectname;
        this.opcServe = new OPCServe(opcip, opcservename);
        isShouldRun = true;
        this.reconnqueue = queue;
        this.opcserid = opcserid;
//        Thread t = new Thread(this);
//        t.setDaemon(true);
//        t.start();
    }


    @Override
    public void run() {
        registeredMeasurePoint.clear();
        opcServe.connect();
        if (opcServe.isConectstatus()) {
            logger.info("opc serve connect success");
        } else {
            logger.info("opc serve connect failed");
        }
        while ((!Thread.currentThread().isInterrupted()) && (isShouldRun)) {
            try {
                if (!opcServe.isConectstatus()) {
                    logger.info(localconnectname + ":" + opcservename + "reconnect opc server");
                    logger.warn(localconnectname + ":" + opcservename + "reconnect opc server");
//                    disconnect();
                    if (reconnqueue != null) {
                        reconnqueue.put(opcserid);
                    }

                    break;
//                    int i = 3;
//                    while (i-- >= 0) {
//                        disconnect();
//                        try {
//                            TimeUnit.SECONDS.sleep(3);
//                        } catch (InterruptedException e) {
//                            logger.error(e.getMessage(), e);
//                        }
//                        reconnect();
//                    }
//                    continue;
                }


                while (eventLinkedBlockingQueue.size() != 0) {
                    Event event = eventLinkedBlockingQueue.poll();
                    if ((event != null) && (event.getPoint() != null)) {
                        if (event instanceof WriteEvent) {
                            WriteEvent writeevent = (WriteEvent) event;
                            writeItem(writeevent.getPoint().getTag(), writeevent.getValue());
                            logger.info("execute write event");
                        } else if (event instanceof RegisterEvent) {
                            RegisterEvent registerevent = (RegisterEvent) event;
                            if (!registeredMeasurePoint.containsKey(registerevent.getPoint().getTag())) {
                                registerItem(registerevent.getPoint());
                            }
                        } else if (event instanceof UnRegisterEvent) {
                            UnRegisterEvent unregisterevent = (UnRegisterEvent) event;
                            removeItem(unregisterevent.getPoint());
                        }
                    }
                }


                //数据读取统计数据读取
                long startgetdate = System.currentTimeMillis();
                if (registeredMeasurePoint.size() > 0) {
                    readAndProcessDataByOnce();
                }


                long endgetdate = System.currentTimeMillis();
                long spendtime = endgetdate - startgetdate;
                logger.info("所有数据处理耗时=" + spendtime + "ms");

                try {
                    TimeUnit.MILLISECONDS.sleep((1000 - spendtime) > 0 ? (1000 - spendtime) : 0);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    disconnect();
//                    isrun=false;
//                    return;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }
    }


    public synchronized void readAndProcessDataByOnce() {

        if (opcServe.readAllItem()) {
            JSONObject influxwritedata = new JSONObject();
            for (ItemUnit itemUnit : opcServe.getItemManger().getTagOrderList()) {
                MeasurePoint measurePoint = registeredMeasurePoint.get(itemUnit.getItem());

                if (measurePoint != null) {
                    measurePoint.setValue(itemUnit.getValue());
                    measurePoint.setInstant(Instant.now());
                    logger.info(localconnectname + ": " + measurePoint.getPoint().getTag() + " value =" + itemUnit.getValue());

                    float value = 0f;
                    if (measurePoint.getPoint().getType().equals(Point.FLOATTYPE)) {
                        value = itemUnit.getValue();
                    } else if (measurePoint.getPoint().getType().equals(Point.BOOLTYPE)) {
                        value = itemUnit.getValue() == 0 ? 0 : 1;
                    }
                    measurePoint.setValue(value);
                    measurePoint.setInstant(Instant.now());
//                    logger.info(measurePoint.getPoint().getTag() + " value =" + itemUnit.getValue());
                    influxwritedata.put(measurePoint.getPoint().getTag(), value);
                }


            }

            if (influxdbWrite != null) {
                InfluxWriteEvent event = new InfluxWriteEvent();
                event.setData(influxwritedata);
                event.setTimestamp(System.currentTimeMillis());
                event.setMeasurement(InfluxdbOperateService.DCSMEASUERMENT);
                influxdbWrite.addEvent(event);
            }
        }

    }


    public synchronized void removeItem(Point point) {
        if (registeredMeasurePoint.containsKey(point.getTag())) {
            registeredMeasurePoint.remove(point.getTag());
            if (opcServe.removeItem(point.getTag())) {
                logger.info(point.getTag() + "remove success");
            } else {
                logger.info(point.getTag() + "remove failed");
            }
        }
    }


    public synchronized void registerItem(Point point) {
        if (!opcServe.isConectstatus()) {
            //离线模式，暂时先存储起来，连接成功再进行插入
            if (!registeredMeasurePoint.containsKey(point.getTag())) {
                MeasurePoint measurePoint = new MeasurePoint();
                measurePoint.setPoint(point);
                addtag_offline(measurePoint);
            }
            return;
        }
        if (!registeredMeasurePoint.containsKey(point.getTag())) {
            if (opcServe.registerItem(point.getTag())) {
                logger.info(point.getTag() + "register success");
                MeasurePoint measurePoint = new MeasurePoint();
                measurePoint.setPoint(point);
                registeredMeasurePoint.put(point.getTag(), measurePoint);
            } else {
                logger.info(point.getTag() + "register failed");
            }
        } else {
            logger.info(point.getTag() + "already register");
        }
    }


    public synchronized boolean writeItem(String tagname, float value) {
        if (opcServe.writeSpecialItem(tagname, value)) {
            logger.info(tagname + " write success");
            return true;
        } else {
            logger.info(tagname + " write failed");
            return false;
        }
    }


    //    @PreDestroy
    public synchronized void disconnect() {
        logger.info("Disconect opc serve");
        if (opcServe != null) {
            opcServe.disconnect();
        }
    }


    public boolean isShouldRun() {
        return isShouldRun;
    }

    public void setShouldRun(boolean shouldRun) {
        isShouldRun = shouldRun;
    }

    public Map<String, MeasurePoint> getRegisteredMeasurePoint() {
        return registeredMeasurePoint;
    }
}
