package hs.fullwrite.opc;

import opc.item.ItemUnit;
import opc.serve.OPCServe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/11/25 16:10
 */
@Component
public class OpcConnect implements Runnable {
    private Logger logger = LoggerFactory.getLogger(OpcConnect.class);

    private String opcservename;
    private String opcip;
    private Map<String, List<MeasurePoint>> registeredMeasurePoint = new ConcurrentHashMap();
    private Thread thread;
    private List<MeasurePoint> waittoregistertag = new CopyOnWriteArrayList<>();
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private LinkedBlockingQueue<WriteEvent> eventLinkedBlockingQueue = new LinkedBlockingQueue();


    public OPCServe getOpcServe() {
        return opcServe;
    }


    private OPCServe opcServe;


    public synchronized void addtag_offline(MeasurePoint m) {
        waittoregistertag.add(m);
    }


    public boolean addWriteEvent(WriteEvent writeEvent){
            return eventLinkedBlockingQueue.offer(writeEvent);
    }


    public synchronized void reconnect() {
        opcServe.disconnect();
        opcServe.connect();
        if (opcServe.isConectstatus()) {

            for (String tag : registeredMeasurePoint.keySet()) {
                registerItem2(tag);
            }
            synchronized (waittoregistertag) {
                registerItem(waittoregistertag);
                waittoregistertag.clear();
            }
        }
        logger.info("ip=" + opcip + " servename=" + opcservename);

    }


    public OpcConnect(@Value("${opc.servename}") String opcservename, @Value("${opc.ip}") String opcip) {

        this.opcservename = opcservename;
        this.opcip = opcip;
        this.opcServe = new OPCServe(opcip, opcservename);
        opcServe.connect();
        if (opcServe.isConectstatus()) {
            logger.info("opc serve connect success");
        } else {
            logger.info("opc serve connect failed");
        }


        List<MeasurePoint> waitregister = new ArrayList<>();
//        MeasurePoint tag1=new MeasurePoint();
//        tag1.setTag("M4111Y01");//磨辊位移
//        waitregister.add(tag1);
//
//        MeasurePoint tag2=new MeasurePoint();
//        tag2.setTag("M41111AI");//磨机电流
//        waitregister.add(tag2);
//
//        MeasurePoint tag3=new MeasurePoint();
//        tag3.setTag("M41191AI");//提升机电流
//        waitregister.add(tag3);
//
//
//        MeasurePoint tag4=new MeasurePoint();
//        tag4.setTag("M4111V01");//磨机振动
//        waitregister.add(tag4);
//
//
//        MeasurePoint tag5=new MeasurePoint();
//        tag5.setTag("A_SLM2_PV5");//反写位号模型判断点号
//        waitregister.add(tag5);
//
//        MeasurePoint tag6=new MeasurePoint();
//        tag6.setTag("A_SLM1_PV5");//反写位号规则判断点号
//        waitregister.add(tag6);
//
//
//
        MeasurePoint tag6=new MeasurePoint();
        tag6.setTag("KXPS3AT");//bool test
        waitregister.add(tag6);

//        MeasurePoint a1 = new MeasurePoint();
//        a1.setTag("Channel_3._System._EnableDiagnostics");
//        waitregister.add(a1);


        if (waitregister != null) {
            registerItem(waitregister);
        }


//        List<MeasurePoint> list=new ArrayList<>();
//        MeasurePoint a1=new MeasurePoint();
//        a1.setTag("Channel_3._System._EnableDiagnostics");
//
//
//        list.add(a1);
//        registerItem(list);
//
//        redisUtil.set("test1",1.1);
//        logger.info("redis value="+redisUtil.get("test1"));;
////
//        MeasurePoint a2=new MeasurePoint();
//        a2.setFix_tagname("dcs.User.ff2");
//        list.add(a2);
//
//        MeasurePoint a3=new MeasurePoint();
//        a3.setFix_tagname("dcs.User.ff3");
//        list.add(a3);
//        registerItem(list);
//        if(opcclient!=null){
//            if(OTT.INSTANTCE.ADDITEM(opcclient,"dcs.User.ff1")==0){
//                System.out.println("failed");
//            }
//            if(OTT.INSTANTCE.ADDITEM(opcclient,"dcs.User.ff2")==0){
//                System.out.println("failed");
//            }
//
//            if(OTT.INSTANTCE.ADDITEM(opcclient,"dcs.User.ff3")==0){
//                System.out.println("failed");
//            }
//        }

//        for(int i=0;i<10;i++){
//            final int j=i;
//            new Thread(new Runnable() {
//
//                @Override
//                public void run() {
//                    if(!writeItem("Channel_3._System._EnableDiagnostics",1.1f+j)){
//                        System.out.println("******* error j="+j);
//                    }
//                }
//            }).start();
//        }

        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }


    @Override
    public void run() {

        Integer writevloop = 0;
        while (!Thread.currentThread().isInterrupted()) {

            try {
                if (!opcServe.isConectstatus()) {
                    logger.info("reconnect opc server");
                    reconnect();
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }
                    continue;
                }

//
//            if(!writeItem("Channel_3._System._EnableDiagnostics",1.1f)){
//                System.out.println("******* error");
//            }





                //数据读取统计数据读取
                long startgetdate = System.currentTimeMillis();
                if (registeredMeasurePoint.size() > 0) {
                    readAndProcessDataByOnce();
                }

                while (eventLinkedBlockingQueue.size() != 0) {
                    WriteEvent event = eventLinkedBlockingQueue.poll();
                    if ((event != null)&&(event.getTag()!=null)) {
                        if(opcServe.writeSpecialItem(event.getTag(), event.getValue())){
                            logger.info("write success");
                        }else {
                            logger.info("write failed");
                        }
                    }
                }

                long endgetdate = System.currentTimeMillis();
                long spendtime = endgetdate - startgetdate;
                logger.info("所有数据处理耗时=" + spendtime + "ms");

                try {
                    TimeUnit.MILLISECONDS.sleep((1000 - spendtime) > 0 ? (1000 - spendtime) : 0);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }

        }
        }


        public synchronized void readAndProcessDataByOnce () {

            if (opcServe.readAllItem()) {
                for (ItemUnit itemUnit : opcServe.getItemManger().getTagOrderList()) {
                    List<MeasurePoint> list = registeredMeasurePoint.get(itemUnit.getItem());
                    if (list != null) {
                        for (MeasurePoint measurePoint : list) {
                            measurePoint.setValue(itemUnit.getValue());
//                        redisUtil.set(measurePoint.getTag(), Float.valueOf(itemUnit.getValue()));
//                        logger.info("redis value=" + redisUtil.sGet(measurePoint.getTag()));
                            ;
                            measurePoint.setInstant(Instant.now());
                            logger.info(measurePoint.getTag() + " value =" + itemUnit.getValue());
                        }
                    }
                }
            }

        }


        public synchronized void removeItem (List < MeasurePoint > tagname) {
            for (MeasurePoint measurePoint : tagname) {
                if (registeredMeasurePoint.get(measurePoint.getTag()) != null) {
                    List<MeasurePoint> chainmeasure = registeredMeasurePoint.get(measurePoint.getTag());
                    chainmeasure.remove(measurePoint);
                    if (opcServe.removeItem(measurePoint.getTag())) {
                        logger.info(measurePoint.getTag() + "remove success");
                    } else {
                        logger.info(measurePoint.getTag() + "remove failed");
                    }

                }

            }

        }


        public synchronized void registerItem2 (String tagname){

            if (opcServe.registerItem(tagname)) {
                logger.info(tagname + "register success");
            } else {
                logger.info(tagname + "register failed");
            }
        }


        public synchronized void registerItem (List < MeasurePoint > tagname) {

            for (MeasurePoint measurePoint : tagname) {

                if (!opcServe.isConectstatus()) {
                    //离线模式，暂时先存储起来，连接成功再进行插入
                    addtag_offline(measurePoint);
                    continue;
                }
                if (opcServe.registerItem(measurePoint.getTag())) {
                    if (registeredMeasurePoint.get(measurePoint.getTag()) == null) {
                        registeredMeasurePoint.put(measurePoint.getTag(), new CopyOnWriteArrayList<>());
                    }
                    registeredMeasurePoint.get(measurePoint.getTag()).add(measurePoint);
                } else {
                    logger.error(measurePoint.getTag() + "register failed");
                }
            }

        }

        public synchronized boolean writeItem (String tagname,float value){

            if (opcServe.writeSpecialItem(tagname, value)) {
                return true;
            } else {
                return false;
            }

        }


        @PreDestroy
        public synchronized void disconnect () {
            logger.info("Disconect opc serve");
            if (opcServe != null) {
                opcServe.disconnect();
            }
        }

    }
