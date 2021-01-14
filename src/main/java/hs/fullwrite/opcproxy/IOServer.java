package hs.fullwrite.opcproxy;

import hs.fullwrite.opcproxy.session.SessionManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/2/10 8:53
 */
@Component
public class IOServer implements Runnable {
    private Logger logger = LoggerFactory.getLogger(IOServer.class);
    private EventLoopGroup bossGroup =null;
    private EventLoopGroup workerGroup =null;
    //private Channel channel=null;

    @Autowired
    private SessionManager sessionManager;

    private int port;
    private NettyServerInitializer nettyServerInitializer;
    private ExecutorService executorService;
    public IOServer(@Value("${nettyport}")
                    @NonNull int port,
                    @Autowired NettyServerInitializer nettyServerInitializer,
                    SessionManager sessionManager,
                    ExecutorService executorService) {
        this.port = port;
        this.nettyServerInitializer=nettyServerInitializer;
        this.sessionManager=sessionManager;
        this.executorService=executorService;
        executorService.execute(this);
    }

    @Override
    public void run() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup(10);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(nettyServerInitializer)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            /*Bind and start to accept incoming connections.*/
            ChannelFuture f = b.bind(port).sync(); // (7)
            /*Wait until the server socket is closed.
            In this example, this does not happen, but you can do that to gracefully
            shut down your server.*/
            f.channel().closeFuture().sync();
            //channel = f.channel();
        } catch (Exception e){
            logger.error(e.getMessage(),e);
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    @PreDestroy
    public void destory() {
        logger.info("destroy netty server resources");
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

}
