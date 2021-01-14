package hs.fullwrite.bean;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/4 14:40
 */
public class Point {
    private int pointid;
    private String tag;
    private String notion;
    private String type;
    private int writeable;
    private int opcserveid;
    private String resouce;
    private String standard;

    public static final String MESRESOURCE="mes";
    public static final String OPCRESOURCE="opc";
    public static final String FLOATTYPE="float";
    public static final String BOOLTYPE="bool";
    public static final int WRITEANDREAD=1;
    public static final int READONLY=0;






    public int getPointid() {
        return pointid;
    }

    public void setPointid(int pointid) {
        this.pointid = pointid;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getNotion() {
        return notion;
    }

    public void setNotion(String notion) {
        this.notion = notion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getWriteable() {
        return writeable;
    }

    public void setWriteable(int writeable) {
        this.writeable = writeable;
    }

    public int getOpcserveid() {
        return opcserveid;
    }

    public void setOpcserveid(int opcserveid) {
        this.opcserveid = opcserveid;
    }

    public String getResouce() {
        return resouce;
    }

    public void setResouce(String resouce) {
        this.resouce = resouce;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }
}
