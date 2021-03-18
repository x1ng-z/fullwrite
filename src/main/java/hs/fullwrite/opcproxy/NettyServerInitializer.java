package hs.fullwrite.opcproxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    @Autowired
    public MsgDecoderInbound msgdecoder;

    @Autowired
    public MsgencoderOutbound msgencoder;

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline=socketChannel.pipeline();
        pipeline.addLast("msgencode", msgencoder);
        pipeline.addLast("inlengthcontrl",new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,7,3,0,0));
        pipeline.addLast("msgdecode", msgdecoder);
    }
}
