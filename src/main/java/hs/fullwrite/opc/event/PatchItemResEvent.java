package hs.fullwrite.opc.event;

import com.alibaba.fastjson.JSON;
import hs.fullwrite.bean.dto.TagDto;
import hs.fullwrite.opc.MeasurePoint;
import hs.fullwrite.opc.OpcExecute;
import hs.fullwrite.opcproxy.Command.CommandImp;
import hs.fullwrite.opcproxy.session.Session;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/15 8:49
 */
public class PatchItemResEvent extends BaseEvent {
    public static final long CONTEXTLENGHT = 16380;

    public PatchItemResEvent() {
        super(false);
    }

    @Override
    public void execute(OpcExecute opcExecute) {

        Session session = opcExecute.getMySession();

        sendPatchAddItemCmd(opcExecute, opcExecute.getServeInfo().getServeid(), session, opcExecute.getWaittoregistertagpool().values());
        //将发送完的

    }


    public void sendPatchAddItemCmd(OpcExecute opcExecute,long serveid, Session session, Collection<MeasurePoint> measurePointCollection) {

        try {
            List<MeasurePoint> measurePointList = new ArrayList(measurePointCollection);
            logger.info("BATCHADDITEM count=" + measurePointCollection.size());
            int indexwaitsendnum = 0;
//            int patchordinal = 0;
//            PatchRequstDto patchRequstDto = new PatchRequstDto();
//            patchRequstDto.setOrdinal(patchordinal);
            List<TagDto> patchwairegiteritems=new ArrayList<>();
            for (int indexsplit = 0; indexsplit < measurePointList.size(); indexsplit++) {

                if (JSON.toJSONString(patchwairegiteritems).getBytes("utf-8").length < CONTEXTLENGHT) {
                    TagDto tagDto = new TagDto();
                    tagDto.setTag(measurePointList.get(indexsplit).getPoint().getTag());
                    patchwairegiteritems.add(tagDto);
                    indexwaitsendnum++;
                } else {
                    patchwairegiteritems.remove(indexwaitsendnum - 1);
                    if (patchwairegiteritems.size() > 0) {
                        String tmpdeug = JSON.toJSONString(patchwairegiteritems);
                        session.getCtx().writeAndFlush(CommandImp.BATCHADDITEM.build(JSON.toJSONString(patchwairegiteritems).getBytes("utf-8"), serveid));
                    }
//                    patchordinal++;
                    patchwairegiteritems = new ArrayList<>();
//                    patchRequstDto.setOrdinal(patchordinal);
                    indexwaitsendnum = 0;
                    indexsplit--;
                }

            }

            //最后一批点号注册
            if (indexwaitsendnum > 0) {
                session.getCtx().writeAndFlush(CommandImp.BATCHADDITEM.build(JSON.toJSONString(patchwairegiteritems).getBytes("utf-8"), serveid));
            }

        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
