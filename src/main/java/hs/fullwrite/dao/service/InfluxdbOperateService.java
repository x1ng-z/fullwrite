package hs.fullwrite.dao.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hs.fullwrite.dao.influxdb.InfluxdbOperate;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BoundParameterQuery;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/5 1:33
 */
@Service
public class InfluxdbOperateService implements InfluxdbOperate {
    private Logger logger = LoggerFactory.getLogger(InfluxdbOperateService.class);

    public static final String DCSMEASUERMENT="dcs";
    public static final String MESMEASUERMENT="mes";


    @Autowired
    private InfluxDB influxDB;

    /**
     * @param millisTime now:System.currentTimeMillis()
     * */
    @Override
    public void writeData(JSONObject data, String measurement, long millisTime) {
        org.influxdb.dto.Point.Builder point= Point.measurement(measurement)
                .time(millisTime, TimeUnit.MILLISECONDS);
        for(String key:data.keySet()){
            point.addField(key,data.getFloat(key));
        }
        influxDB.write(point.build());
    }

    /**
     * @param starttime
     * @param endtime now Instant.now()
     * */
    @Override
    public JSONArray readSomeTimeData(Set<String> key, String measurement, Instant starttime, Instant endtime) {
        JSONArray _result=new JSONArray();

        StringBuilder tagname = new StringBuilder();
        for (String tag : key) {
            tagname.append("\""+tag+"\"" + ",");
        }
        Query query = null;
        query = BoundParameterQuery.QueryBuilder.newQuery("SELECT " + tagname.substring(0, tagname.length() - 1) + " FROM " + measurement + " WHERE time > $start AND time < $end ORDER BY time DESC")// LIMIT 1
                .forDatabase("mydb")
                .bind("start", starttime)
                .bind("end", endtime)
                .create();

        QueryResult queryResult = null;
        try {
            queryResult = influxDB.query(query);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
//            logger.error(measureName+sampletimelength);
//            logger.error(tagname.substring(0, tagname.length() - 1));
            return _result;
        }

        List<QueryResult.Result> queryResultResults = queryResult.getResults();
        for (QueryResult.Result result : queryResultResults) {
            if (result.getSeries() == null) {
                logger.error("have no data");
                return _result;
            }
            for (QueryResult.Series serie : result.getSeries()) {
                for (List<Object> list : serie.getValues()) {
                    JSONObject row = new JSONObject();
                    _result.add(row);
                    for (int i = 0; i < serie.getColumns().size(); ++i) {
                        row.put(serie.getColumns().get(i), list.get(i));
                    }
                }
            }
        }
        return _result;
    }

    @Override
    public JSONArray readNewestData(Set<String> key, String measurement) {
        JSONArray _result=new JSONArray();

        StringBuilder tagname = new StringBuilder();
        for (String tag : key) {
            tagname.append("\""+tag+"\"" + ",");
        }
        Query query = null;
        query = BoundParameterQuery.QueryBuilder.newQuery("SELECT " + tagname.substring(0, tagname.length() - 1) + " FROM " + measurement + "  ORDER BY time DESC  LIMIT 1")// LIMIT 1
                .forDatabase("mydb")
                .create();

        QueryResult queryResult = null;
        try {
            queryResult = influxDB.query(query);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
//            logger.error(measureName+sampletimelength);
//            logger.error(tagname.substring(0, tagname.length() - 1));
            return _result;
        }

        List<QueryResult.Result> queryResultResults = queryResult.getResults();
        for (QueryResult.Result result : queryResultResults) {
            if (result.getSeries() == null) {
                logger.error("have no data");
                return _result;
            }
            for (QueryResult.Series serie : result.getSeries()) {
                for (List<Object> list : serie.getValues()) {
                    JSONObject row = new JSONObject();
                    _result.add(row);
                    for (int i = 0; i < serie.getColumns().size(); ++i) {
                        row.put(serie.getColumns().get(i), list.get(i));
                    }
                }
            }
        }
        return _result;
    }
}
