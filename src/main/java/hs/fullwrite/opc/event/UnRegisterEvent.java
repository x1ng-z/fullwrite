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
 * @date 2021/1/4 16:16
 */
public class UnRegisterEvent extends BaseEvent {
    public UnRegisterEvent(boolean repeat) {
        super(repeat);
    }

    public UnRegisterEvent() {
        super(false);
    }

    @Override
    public void execute(OpcExecute opcExecute) {
        Point point=getPoint();
        if (opcExecute.getRegisteredMeasurePointpool().containsKey(point.getTag())) {
            sendRemoveItemCmd(opcExecute, point.getTag());
        }
    }
    private void sendRemoveItemCmd(OpcExecute opcExecute, String tag) {
        JSONObject msg = new JSONObject();
        msg.put("tag", tag);
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(msg);
        try {
            Session session;
            if ((session = opcExecute.getMySession()) != null) {
                session.getCtx().writeAndFlush(CommandImp.REMOVEITEM.build(jsonArray.toJSONString().getBytes("utf-8"), opcExecute.getServeInfo().getServeid()));
            }
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
