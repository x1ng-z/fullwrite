package hs.fullwrite.opc;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/4 17:15
 */
public class OpcConnIntegrated {
    private OpcConnect readconn;//读数据opc连接(内涵read/write点号)
    private OpcConnect writeconn;//写数据opc连接(write点号)
    private OpcExecute execute;

    public OpcConnect getReadconn() {
        return readconn;
    }

    public void setReadconn(OpcConnect readconn) {
        this.readconn = readconn;
    }

    public OpcConnect getWriteconn() {
        return writeconn;
    }

    public void setWriteconn(OpcConnect writeconn) {
        this.writeconn = writeconn;
    }

    public OpcExecute getExecute() {
        return execute;
    }

    public void setExecute(OpcExecute execute) {
        this.execute = execute;
    }
}
