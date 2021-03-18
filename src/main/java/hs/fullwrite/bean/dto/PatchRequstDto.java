package hs.fullwrite.bean.dto;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/15 10:47
 */
public class PatchRequstDto {
    private int  ordinal;
    private List<TagDto> data=new ArrayList<>();

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public List<TagDto> getData() {
        return data;
    }

    public void setData(List<TagDto> data) {
        this.data = data;
    }
}
