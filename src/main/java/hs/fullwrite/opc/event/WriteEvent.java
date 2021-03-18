package hs.fullwrite.opc.event;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hs.fullwrite.bean.Point;
import hs.fullwrite.opc.OpcExecute;
import hs.fullwrite.opcproxy.Command.CommandImp;
import hs.fullwrite.opcproxy.session.Session;

import java.io.UnsupportedEncodingException;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/12/1 8:28
 */
public class WriteEvent extends BaseEvent {

    private float value;

    public WriteEvent(boolean repeat, float value) {
        super(repeat);
        this.value = value;
    }

    public WriteEvent( float value) {
        super(false);
        this.value = value;
    }

    @Override
    public void execute(OpcExecute opcExecute) {
        Point point = getPoint();
        if (opcExecute.getRegisteredMeasurePointpool().containsKey(point.getTag())) {
            JSONArray jsonArray = new JSONArray();
            JSONObject msg = new JSONObject();
            msg.put("tag", point.getTag());
            msg.put("value", value);
            jsonArray.add(msg);
            try {
                Session session;
                if ((session = opcExecute.getMySession()) != null) {
                    session.getCtx().writeAndFlush(CommandImp.WRITE.build(jsonArray.toJSONString().getBytes("utf-8"), opcExecute.getServeInfo().getServeid()));
                }
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
