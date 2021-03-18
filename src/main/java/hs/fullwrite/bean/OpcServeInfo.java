package hs.fullwrite.bean;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/4 14:37
 */
public class OpcServeInfo {
    private long serveid;
    private String servename;
    private String serveip;

    public long getServeid() {
        return serveid;
    }

    public void setServeid(long serveid) {
        this.serveid = serveid;
    }

    public String getServename() {
        return servename;
    }

    public void setServename(String servename) {
        this.servename = servename;
    }

    public String getServeip() {
        return serveip;
    }

    public void setServeip(String serveip) {
        this.serveip = serveip;
    }
}
