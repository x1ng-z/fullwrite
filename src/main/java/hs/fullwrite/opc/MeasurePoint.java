package hs.fullwrite.opc;


import java.time.Instant;


public class MeasurePoint {
    /**数据库属性*/
    private int measurepointid;
    private String tag;
    private String notion;
    private int opcinfoid;


    /**实时属性*/
    private float value;
    private Instant instant;

    public int getMeasurepointid() {
        return measurepointid;
    }

    public void setMeasurepointid(int measurepointid) {
        this.measurepointid = measurepointid;
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

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public Instant getInstant() {
        return instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    public int getOpcinfoid() {
        return opcinfoid;
    }

    public void setOpcinfoid(int opcinfoid) {
        this.opcinfoid = opcinfoid;
    }
}
