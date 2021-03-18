package hs.fullwrite.dao.influxdb;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.time.Instant;
import java.util.Set;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/5 1:31
 */
public interface InfluxdbOperate {

   void writeData(JSONObject data, String measurement, long millisTime);
   JSONArray readSomeTimeData(Set<String> key, String measurement, Instant starttime, Instant endtime);
   JSONArray readNewestData(Set<String> key, String measuremen);
}
