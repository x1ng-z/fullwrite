package hs.fullwrite.contrl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hs.fullwrite.dao.service.InfluxdbOperateService;
import hs.fullwrite.dao.service.OpcPointOperateService;
import hs.fullwrite.opc.OpcConnectManger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/11/30 1:19
 * <p>
 * 获取最新的数据
 */
@RestController
@RequestMapping("/influxdb")
public class InfluxdbController {
    private Logger logger = LoggerFactory.getLogger(InfluxdbController.class);

    @Autowired
    private OpcConnectManger opcConnectManger;

    @Autowired
    private OpcPointOperateService opcPointOperateService;

    @Autowired
    private InfluxdbOperateService influxdbOperateService;


    @RequestMapping("/newestdata")
    public String newestdata(@RequestParam("measurment") String measurment, @RequestParam("tags") String tags) {
        JSONObject msg = new JSONObject();
        String[] splittags = tags.split(",");
        JSONObject jsondata = new JSONObject();
        msg.put("data", jsondata);
        try {
            for (String tag : splittags) {
                Set<String> tagset = new HashSet<>();
                tagset.add(tag);
                switch (measurment) {
                    case InfluxdbOperateService.MESMEASUERMENT: {
                        JSONArray influxdbdata = influxdbOperateService.readNewestData(tagset, InfluxdbOperateService.MESMEASUERMENT);
                        if (influxdbdata.size() > 0) {
                            JSONObject newestdata = influxdbdata.getJSONObject(0);
                            jsondata.put(tag, newestdata.getFloatValue(tag));
                        }
                        break;
                    }
                    case InfluxdbOperateService.DCSMEASUERMENT: {

                        JSONArray influxdbdata = influxdbOperateService.readNewestData(tagset, InfluxdbOperateService.DCSMEASUERMENT);
                        if (influxdbdata.size() > 0) {
                            JSONObject newestdata = influxdbdata.getJSONObject(0);
                            jsondata.put(tag, newestdata.getFloatValue(tag));
                        }
                        break;
                    }
                    default:
                        break;
                }

            }
            msg.put("msg", "success");
            return msg.toJSONString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        msg.put("msg", "error");
        return msg.toJSONString();
    }


    /**
     * @param starttime 开始时间毫秒
     * @param endtime   结束时间毫秒
     */
    @RequestMapping("/historydata")
    public String historydata(@RequestParam("measurment") String measurment, @RequestParam("tags") String tags, @RequestParam("starttime") long starttime, @RequestParam("endtime") long endtime) {
        JSONObject msg = new JSONObject();
        String[] splittags = tags.split(",");
        JSONObject jsondata = new JSONObject();
        msg.put("data", jsondata);
        try {
            for (String tag : splittags) {
                Set<String> tagset = new HashSet<>();
                tagset.add(tag);
                switch (measurment) {
                    case InfluxdbOperateService.MESMEASUERMENT: {
                        JSONArray influxdbdata = influxdbOperateService.readSomeTimeData(tagset, InfluxdbOperateService.MESMEASUERMENT, Instant.ofEpochMilli(starttime), Instant.ofEpochMilli(endtime));
                        if (influxdbdata.size() > 0) {
                            JSONArray historydata= new JSONArray();
                            for(int index=0;index<influxdbdata.size();index++){
                                historydata.add(influxdbdata.getJSONObject(index).getFloat(tag));
                            }

                            jsondata.put(tag, historydata);
                        }
                        break;
                    }
                    case InfluxdbOperateService.DCSMEASUERMENT: {
                        JSONArray influxdbdata = influxdbOperateService.readSomeTimeData(tagset, InfluxdbOperateService.DCSMEASUERMENT, Instant.ofEpochMilli(starttime), Instant.ofEpochMilli(endtime));
                        if (influxdbdata.size() > 0) {
                            JSONArray historydata= new JSONArray();
                            for(int index=0;index<influxdbdata.size();index++){
                                historydata.add(influxdbdata.getJSONObject(index).getFloat(tag));
                            }

                            jsondata.put(tag, historydata);
                        }
                        break;
                    }
                    default:
                        break;
                }

            }
            msg.put("msg", "success");
            return msg.toJSONString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        msg.put("msg", "error");
        return msg.toJSONString();
    }


    private boolean isNoneString(String value) {
        if ((value != null) && (value != "")) {
            return true;
        }
        return false;
    }

}
