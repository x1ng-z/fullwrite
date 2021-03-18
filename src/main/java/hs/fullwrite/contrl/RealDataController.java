package hs.fullwrite.contrl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hs.fullwrite.bean.Point;
import hs.fullwrite.dao.service.InfluxdbOperateService;
import hs.fullwrite.dao.service.OpcPointOperateService;
import hs.fullwrite.opc.OpcConnectManger;
import hs.fullwrite.opc.OpcGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/11/30 1:19
 *
 * 获取最新的数据
 */
@RestController
@RequestMapping("/realdata")
public class RealDataController {
    private Logger logger = LoggerFactory.getLogger(RealDataController.class);

    @Autowired
    private OpcConnectManger opcConnectManger;

    @Autowired
    private OpcPointOperateService opcPointOperateService;

    @Autowired
    private InfluxdbOperateService influxdbOperateService;


    @RequestMapping("/read")
    public String readopctags(@RequestParam("tags") String tags) {
        JSONObject msg = new JSONObject();
        String[] splittags = tags.split(",");

        List<Point> allpoints = opcPointOperateService.findAllPoints();

        JSONObject jsondata = new JSONObject();
        msg.put("data", jsondata);
        try {
            for (String tag : splittags) {

                for (Point point : allpoints) {
                    String dbtag = point.getTag();
                    String dbresource = point.getResouce();

                    if (isNoneString(dbtag) && isNoneString(dbresource)) {

                        if (tag.equals(dbtag)) {
                            if (dbresource.equals(Point.OPCRESOURCE)) {
                                //opc数据
                                for (OpcGroup opcGroup : opcConnectManger.getOpcconnectpool().values()) {
                                    if (opcGroup.getReadopcexecute().getRegisteredMeasurePointpool().containsKey(tag)) {
                                        if(opcGroup.getReadopcexecute().getRegisteredMeasurePointpool().get(tag).getInstant()!=null){
                                            jsondata.put(tag, opcGroup.getReadopcexecute().getRegisteredMeasurePointpool().get(tag).getValue());
                                        }
//                                        jsondata.put(tag, opcGroup.getReadopcexecute().getRegisteredMeasurePoint().get(tag).getValue());
                                        break;
                                    }
                                }


                            } else if (dbresource.equals(Point.MESRESOURCE)) {
                                //mes数据
                                Set<String> tagset = new HashSet<>();
                                tagset.add(tag);
                                JSONArray influxdbdata = influxdbOperateService.readNewestData(tagset, InfluxdbOperateService.MESMEASUERMENT);
                                {
                                    if (influxdbdata.size() > 0) {
                                        JSONObject newestdata = influxdbdata.getJSONObject(0);
                                        jsondata.put(tag, newestdata.getFloatValue(tag));
                                    }
                                }
                            }
                        }

                    }
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
