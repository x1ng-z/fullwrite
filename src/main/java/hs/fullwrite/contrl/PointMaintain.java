package hs.fullwrite.contrl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hs.fullwrite.bean.Point;
import hs.fullwrite.dao.service.OpcPointOperateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/14 23:57
 */

@Controller
@RequestMapping("/pointoperate")
public class PointMaintain {
    private Logger logger = LoggerFactory.getLogger(PointMaintain.class);
    @Autowired
    private OpcPointOperateService opcPointOperateService;


    @RequestMapping("/getalpoints")
    @ResponseBody
    public String getAllPoints() {
        JSONObject result = new JSONObject();
        JSONArray data = new JSONArray();
        try {
            result.put("data", data);
            List<Point> pointList = opcPointOperateService.findAllPoints();
            for (Point point : pointList) {
                JSONObject subdata = new JSONObject();
                subdata.put("tag", point.getTag());
                subdata.put("tagname", point.getNotion());
                data.add(subdata);
            }
            result.put("msg", "success");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result.put("msg", "error");
        }
        return result.toJSONString();
    }



}
