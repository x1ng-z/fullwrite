package hs.fullwrite.opcproxy.session;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/12/29 23:04
 */
@Component
public class SessionManager {
    private Logger logger = LoggerFactory.getLogger(SessionManager.class);

    private Map<ChannelHandlerContext, Session> modulepool = new ConcurrentHashMap<>();

    public synchronized void addSessionModule(int nodeid, String function, ChannelHandlerContext ctx) {
        if (!modulepool.containsKey(ctx)) {
            Session session = new Session();
            session.setCtx(ctx);
            session.setFunction(function);
            session.setOpcserveid(nodeid);
            modulepool.put(ctx, session);
        }

    }


    public synchronized Session removeSessionModule(ChannelHandlerContext ctx) {
        if (ctx != null) {
            return modulepool.remove(ctx);
        }
        return null;
    }

    public synchronized Session  getSpecialSession(int opcserveid,String function){

        for(Session session:modulepool.values()){
            if(session.getOpcserveid()==opcserveid&&session.getFunction().equals(function)){
                return session;
            }
        }
        return null;
    }

    public Map<ChannelHandlerContext, Session> getModulepool() {
        return modulepool;
    }
}
