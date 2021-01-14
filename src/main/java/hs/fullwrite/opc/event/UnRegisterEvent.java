package hs.fullwrite.opc.event;

import hs.fullwrite.bean.Point;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/4 16:16
 */
public class UnRegisterEvent implements Event {
    private Point point;

    @Override
    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }
}
