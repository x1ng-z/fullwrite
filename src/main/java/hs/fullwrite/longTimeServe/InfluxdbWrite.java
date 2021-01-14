package hs.fullwrite.longTimeServe;

import hs.fullwrite.dao.service.InfluxdbOperateService;
import hs.fullwrite.longTimeServe.event.Event;
import hs.fullwrite.longTimeServe.event.InfluxWriteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/5 1:08
 */
@Component
public class InfluxdbWrite implements Runnable {
    private Logger logger = LoggerFactory.getLogger(InfluxdbWrite.class);


    private InfluxdbOperateService influxdbOperateService;

    private ExecutorService executorService;

    private LinkedBlockingQueue<Event> eventLinkedBlockingQueue = new LinkedBlockingQueue();

    @Autowired
    public InfluxdbWrite( ExecutorService executorService, InfluxdbOperateService influxdbOperateService) {
        this.influxdbOperateService=influxdbOperateService;
        this.executorService=executorService;
        executorService.execute(this);
    }


    public void addEvent(Event event){
        try {
            eventLinkedBlockingQueue.put(event);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(),e);
        }
    }


    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted()){

            try {
                Event event=eventLinkedBlockingQueue.take();
                if(event instanceof InfluxWriteEvent){
                    InfluxWriteEvent influxWriteEvent=(InfluxWriteEvent) event;
                    influxdbOperateService.writeData(influxWriteEvent.getData(),influxWriteEvent.getMeasurement(),influxWriteEvent.getTimestamp());
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage(),e);
            }

        }
    }
}
