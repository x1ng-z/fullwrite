package hs.fullwrite.contrl;

import com.alibaba.fastjson.JSONObject;
import hs.fullwrite.opc.OpcConnect;
import hs.fullwrite.opc.WriteEvent;
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
    public void setOpcConnect(OpcConnect opcConnect) {
        this.opcConnect = opcConnect;
    }

    private OpcConnect opcConnect;


    @RequestMapping("/read")
    public String readopctags(@RequestParam("tags") String tags) {
        JSONObject msg= new JSONObject();
        String[] splittags=tags.split(",");
        try {
            for(String tag:splittags){
                msg.put(tag,opcConnect.getOpcServe().getItemManger().getItemUnit(tag).getValue());
            }
            msg.put("msg","success");
            return msg.toJSONString();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        msg.put("msg","failed");
        return msg.toJSONString();
    }




    @RequestMapping("/write")
    public String writeopctags(@RequestParam("tagvalue") String tags) {
        JSONObject msg= JSONObject.parseObject(tags);
        JSONObject result=new JSONObject();
        boolean writeresult=true;
        try {
            for(String tag:msg.keySet()){
                System.out.println(tag+"-------" +msg.getFloat(tag));
                WriteEvent writeEvent=new WriteEvent();
                writeEvent.setTag(tag);
                writeEvent.setValue(msg.getFloat(tag));
                writeresult=opcConnect.addWriteEvent(writeEvent);
//                writeresult =opcConnect.writeItem(tag,msg.getFloat(tag))&&writeresult;
            }
            if(writeresult){
                result.put("msg","success");
            }else {
                result.put("msg","failed");
            }
            return result.toJSONString();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        result.put("msg","failed");
        return result.toJSONString();
    }




}