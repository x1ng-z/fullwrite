package hs.fullwrite.opcproxy.session;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/12/29 23:05
 */
public class Session {
   // private Module object;//apc module;
    private ChannelHandlerContext ctx;
    private Integer tcpPort;
    private int opcserveid;


    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }



    public Integer getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(Integer tcpPort) {
        this.tcpPort = tcpPort;
    }

    public int getOpcserveid() {
        return opcserveid;
    }

    public void setOpcserveid(int opcserveid) {
        this.opcserveid = opcserveid;
    }
}
