package hs.fullwrite.opcproxy.session;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/12/29 23:05
 */
public class Session {
    private ChannelHandlerContext ctx;
    private long opcserveid;
    private String function;


    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }


    public long getOpcserveid() {
        return opcserveid;
    }

    public void setOpcserveid(long opcserveid) {
        this.opcserveid = opcserveid;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }
}
