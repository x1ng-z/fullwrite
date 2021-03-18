package hs.fullwrite.opc.event;

import hs.fullwrite.bean.Point;
import hs.fullwrite.opc.OpcExecute;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/13 17:44
 */
public abstract class BaseEvent implements Event {
    private Point point;
    private boolean repeat=false;


    public BaseEvent(boolean repeat) {
        this.repeat = repeat;
    }


    @Override
    public  abstract void execute(OpcExecute opcExecute);

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }
}
