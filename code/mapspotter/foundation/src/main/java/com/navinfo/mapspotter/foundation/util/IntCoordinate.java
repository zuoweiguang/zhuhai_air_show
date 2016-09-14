package com.navinfo.mapspotter.foundation.util;

import java.io.Serializable;

/**
 * Created by gaojian on 2016/1/29.
 */
public class IntCoordinate implements Cloneable, Serializable {
    public int x;
    public int y;

    public IntCoordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntCoordinate) {
            IntCoordinate other = (IntCoordinate) obj;
            return this.x != other.x?false:this.y == other.y;
        }
        return false;
    }

    @Override
    protected Object clone() {
        try {
            IntCoordinate o = (IntCoordinate)super.clone();
            return o;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "(" + this.x + "," + this.y + ")";
    }
}
