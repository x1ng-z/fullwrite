package hs.fullwrite.opc;


import hs.fullwrite.bean.Point;

import java.time.Instant;


public class MeasurePoint {
    private Point point;


    /**实时属性*/
    private float value;
    private Instant instant;

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
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
}
