package hs.fullwrite.opc;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/12/1 8:28
 */
public class WriteEvent {
    private String tag;
    private float value;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
