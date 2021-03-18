package hs.fullwrite.opc.event;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hs.fullwrite.bean.Point;
import hs.fullwrite.opc.MeasurePoint;
import hs.fullwrite.opc.OpcExecute;
import hs.fullwrite.opcproxy.Command.CommandImp;
import hs.fullwrite.opcproxy.session.Session;

import java.io.UnsupportedEncodingException;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/4 16:15
 * 将点号添加到待注册列表中，然后进行点号注册信息
 */
public class RegisterEvent extends BaseEvent {

    public RegisterEvent(boolean repeat) {
        super(repeat);
    }

    public RegisterEvent() {
        super(false);
    }

    @Override
    public void execute(OpcExecute opcExecute) {
        Point point = getPoint();
        if (!opcExecute.getRegisteredMeasurePointpool().containsKey(point.getTag())) {
            MeasurePoint measurePoint = new MeasurePoint();
            measurePoint.setPoint(point);
            opcExecute.addwaitaddIteambuf(measurePoint);
            sendAddItemCmd(opcExecute, point.getTag());
        }
    }

    private boolean sendAddItemCmd(OpcExecute opcExecute, String tag) {
        JSONObject msg = new JSONObject();
        msg.put("tag", tag);
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(msg);
        try {
            Session session;
            if ((session = opcExecute.getMySession()) != null) {
                session.getCtx().writeAndFlush(CommandImp.ADDITEM.build(jsonArray.toJSONString().getBytes("utf-8"), opcExecute.getServeInfo().getServeid()));
            } else {
                return false;
            }

        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }
}
