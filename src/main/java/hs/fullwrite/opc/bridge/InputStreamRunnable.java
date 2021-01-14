package hs.fullwrite.opc.bridge;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.LinkedBlockingQueue;

public class InputStreamRunnable implements Runnable {
    private Logger logger = LoggerFactory.getLogger(InputStreamRunnable.class);

    private BufferedReader bReader = null;
    private String _type;
    private LinkedBlockingQueue linkedBlockingQueue=null;

    public InputStreamRunnable(InputStream is, String _type, LinkedBlockingQueue linkedBlockingQueue) {
        this.linkedBlockingQueue=linkedBlockingQueue;
        try {
            this._type=_type;
            bReader = new BufferedReader(new InputStreamReader((is), "UTF-8"));
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex);
        }
    }
    @Override
    public void run() {
        String line;
        int num = 0;
        try {
            while ((line = bReader.readLine()) != null) {
                if(linkedBlockingQueue!=null){
                    logger.info(line);
                    linkedBlockingQueue.put(line);
                }else {
                    logger.info(line);

                }
            }
            logger.info("end "+_type);
            bReader.close();
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex);

        }
    }
}
