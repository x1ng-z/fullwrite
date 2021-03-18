package hs.fullwrite.contrl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hs.fullwrite.bean.Point;
import hs.fullwrite.dao.service.InfluxdbOperateService;
import hs.fullwrite.dao.service.OpcPointOperateService;
import hs.fullwrite.longTimeServe.InfluxdbWrite;
import hs.fullwrite.longTimeServe.event.InfluxWriteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/8 12:12
 */
@Controller
@RequestMapping("/data")
public class MesDataController {
    private Logger logger = LoggerFactory.getLogger(MesDataController.class);

    @Autowired
    private InfluxdbWrite influxdbWrite;

    @Autowired
    private OpcPointOperateService opcPointOperateService;


    @RequestMapping("/savedata")
    @ResponseBody
    public String savemesdata(@RequestBody String mesdata) {
        JSONObject result = new JSONObject();
        result.put("msg", "success");
        logger.info("the context is" + mesdata);
        /**
         "points": [
         {
         "ts": 1607427246000,
         "tagName": "OPC.TS.FN2RD",
         "val": 100
         }
         ]
         */
        try {
            JSONObject mesration = JSONObject.parseObject(mesdata);

            JSONArray jsonArray = mesration.getJSONArray("points");

            Map<String, Point> mesAlreadyExistTags = opcPointOperateService.findAllMesPoints();

            for (int index = 0; index < jsonArray.size(); index++) {

                JSONObject subjson = jsonArray.getJSONObject(index);
                String influxdbkey=subjson.getString("tagName");
                //不包含的点号存储起来
                if (!mesAlreadyExistTags.containsKey(subjson.getString("tagName"))) {
                    Point newpoint = new Point();
                    newpoint.setTag(subjson.getString("tagName"));
                    newpoint.setType(Point.FLOATTYPE);
                    newpoint.setResouce(Point.MESRESOURCE);
                    opcPointOperateService.insertMesPoints(newpoint);
                }else {
                    Point exitpoint=mesAlreadyExistTags.get(subjson.getString("tagName"));
                    influxdbkey=((exitpoint.getStandard()==null)||(exitpoint.getStandard().equals("")))?exitpoint.getTag():exitpoint.getStandard();
                }
                InfluxWriteEvent writeEvent = new InfluxWriteEvent();

                JSONObject writecontext = new JSONObject();

                writecontext.put(influxdbkey, subjson.getFloatValue("val"));

                writeEvent.setMeasurement(InfluxdbOperateService.MESMEASUERMENT);
                writeEvent.setTimestamp(subjson.getLongValue("ts"));
                writeEvent.setData(writecontext);
                influxdbWrite.addEvent(writeEvent);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result.put("msg", "faild");
        }
        return result.toJSONString();
    }







}
