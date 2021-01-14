package hs.fullwrite.opcproxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@ChannelHandler.Sharable
@Component
public class Msgencoder_Outbound extends ChannelOutboundHandlerAdapter {
    private Logger logger = LoggerFactory.getLogger(Msgencoder_Outbound.class);
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ByteBuf buf =ctx.alloc().buffer(((byte[])msg).length);
        buf.writeBytes((byte[])msg);

        ctx.writeAndFlush(buf).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.debug("send success");
                } else {
                    logger.debug("send failed");

                }
//                ReferenceCountUtil.release(msg);
            }
        });
    }
}
