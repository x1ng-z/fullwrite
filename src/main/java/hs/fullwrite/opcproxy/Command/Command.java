package hs.fullwrite.opcproxy.Command;

import com.alibaba.fastjson.JSONObject;
import hs.fullwrite.opc.OpcConnectManger;
import hs.fullwrite.opcproxy.session.SessionManager;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/9/28 13:28
 */
public interface Command {

    JSONObject analye(byte[] context);

    byte[] build(byte[] context, long nodeid);

    boolean valid(byte[] context);

    void operate(byte[] context, ChannelHandlerContext ctx, SessionManager sessionManager, OpcConnectManger opcConnectManger);

}
