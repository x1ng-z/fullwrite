package hs.fullwrite.longTimeServe.event;

import com.alibaba.fastjson.JSONObject;
import hs.fullwrite.longTimeServe.InfluxdbWrite;
import org.influxdb.dto.Point;

import java.util.concurrent.TimeUnit;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/5 1:14
 */
public class InfluxWriteEvent implements Event {
    private JSONObject data;
    private long timestamp;
    private String measurement;

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }
}
