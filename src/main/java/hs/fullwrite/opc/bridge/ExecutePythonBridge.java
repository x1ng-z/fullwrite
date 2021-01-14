package hs.fullwrite.opc.bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutePythonBridge {
    private Logger logger = LoggerFactory.getLogger(ExecutePythonBridge.class);
    public Process p = null;
    Thread result = null;
    Thread error = null;
    private String ip;
    private String port;
    private String opcsevename;
    private String opcseveip;
    private String opcsevid;
    private String exename;

    public ExecutePythonBridge(String exename, String ip, String port, String opcsevename, String opcseveip, String opcsevid) {
        this.exename = exename;
        this.ip = ip;
        this.port = port;
        this.opcsevename = opcsevename;
        this.opcseveip = opcseveip;
        this.opcsevid = opcsevid;
    }

    public boolean stop() {
        if (p != null) {
            p.destroy();
            result.interrupt();
            error.interrupt();
            p = null;
            return true;
        }

        return true;
    }

    public boolean execute() {
//        logger.info("*****exename 1" + exename);
        if (p != null) {
            return true;
        }
        //LinkedBlockingQueue<String> linkedBlockingQueue=new LinkedBlockingQueue();
//        BufferedReader bReader = null;
//        InputStreamReader sReader = null;
//        logger.info("****exename 2" + exename);
        try {
            p = Runtime.getRuntime().exec(new String[]{exename, ip,
                    port,
                    opcsevename,
                    opcseveip,
                    opcsevid});
            result = new Thread(new InputStreamRunnable(p.getInputStream(), "Result", null));
            result.setDaemon(true);
            result.start();

            /* 为"错误输出流"单独开一个线程读取之,否则会造成标准输出流的阻塞 */
            error = new Thread(new InputStreamRunnable(p.getErrorStream(), "ErrorStream", null));
            error.setDaemon(true);
            error.start();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logger.error("exename " + exename);
            return false;
        }
        return true;
    }

}


