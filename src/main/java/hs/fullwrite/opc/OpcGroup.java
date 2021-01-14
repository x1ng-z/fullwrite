package hs.fullwrite.opc;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/14 22:20
 */
public class OpcGroup {
    private OpcExecute readopcexecute;
    private OpcExecute writeopcexecute;

    public OpcExecute getReadopcexecute() {
        return readopcexecute;
    }

    public void setReadopcexecute(OpcExecute readopcexecute) {
        this.readopcexecute = readopcexecute;
    }

    public OpcExecute getWriteopcexecute() {
        return writeopcexecute;
    }

    public void setWriteopcexecute(OpcExecute writeopcexecute) {
        this.writeopcexecute = writeopcexecute;
    }
}
