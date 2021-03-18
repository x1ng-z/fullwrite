package hs.fullwrite.contrl;

import com.alibaba.fastjson.JSONObject;
import hs.fullwrite.opc.OpcConnectManger;
import hs.fullwrite.opc.OpcGroup;
import hs.fullwrite.opc.event.WriteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/11/30 1:19
 */
@RestController
@RequestMapping("/opc")
public class OPCValueController {
    private Logger logger = LoggerFactory.getLogger(OPCValueController.class);

    @Autowired
    private OpcConnectManger opcConnectManger;




    @RequestMapping("/read")
    public String readopctags(@RequestParam("tags") String tags) {
        JSONObject msg = new JSONObject();
        String[] splittags = tags.split(",");
        try {
            for (String tag : splittags) {
                for (OpcGroup opcGroup : opcConnectManger.getOpcconnectpool().values()) {
                    if (opcGroup.getReadopcexecute().getRegisteredMeasurePointpool().containsKey(tag)) {
                        if(opcGroup.getReadopcexecute().getRegisteredMeasurePointpool().get(tag).getInstant()!=null){
                            msg.put(tag, opcGroup.getReadopcexecute().getRegisteredMeasurePointpool().get(tag).getValue());
                        }
                        break;
                    }
                }
            }
            msg.put("msg", "success");
            return msg.toJSONString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        msg.put("msg", "failed");
        return msg.toJSONString();
    }




    /**
     * {tag:value,
     * tag:value
     * }
     * */
    @RequestMapping("/write")
    public String writeopctags(@RequestParam("tagvalue") String tags) {
        JSONObject msg = JSONObject.parseObject(tags);
        JSONObject result = new JSONObject();
        boolean writeresult = true;
        try {
            for (String tag : msg.keySet()) {
//                System.out.println(tag + "-------" + msg.getFloat(tag));

                for (OpcGroup opcGroup : opcConnectManger.getOpcconnectpool().values()) {

                    if (opcGroup.getWriteopcexecute().getRegisteredMeasurePointpool().containsKey(tag)) {
                        if(opcGroup.getWriteopcexecute().isOpcServeOnline()){
                            WriteEvent writeEvent = new WriteEvent(msg.getFloat(tag));
                            writeEvent.setPoint(opcGroup.getWriteopcexecute().getRegisteredMeasurePointpool().get(tag).getPoint());
                            writeEvent.setValue(msg.getFloat(tag));
                            writeresult = opcGroup.getWriteopcexecute().addOPCEvent(writeEvent);
                            logger.info("add write event");
                            break;
                        }else {
                            writeresult=false;
                        }

                    }

                }

//                writeresult =opcConnect.writeItem(tag,msg.getFloat(tag))&&writeresult;
            }
            if (writeresult) {
                result.put("msg", "success");
            } else {
                result.put("msg", "failed");
            }
            return result.toJSONString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        result.put("msg", "failed");
        return result.toJSONString();
    }


}
