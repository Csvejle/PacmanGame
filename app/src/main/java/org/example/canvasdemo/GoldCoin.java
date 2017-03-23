package org.example.canvasdemo;

import android.os.Parcel;
import android.os.Parcelable;


public class GoldCoin implements Parcelable {
    private int x;
    private int y;
    private boolean taken;

    public GoldCoin(Parcel in) {
        x = in.readInt();
        y = in.readInt();
        int temp  = in.readInt();
        if (temp==0)
            taken = false;
        else
            taken = true;
    }

    public GoldCoin(int x, int y, boolean taken) {
        this.x = x;
        this.y = y;
        this.taken = taken;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(x);
        dest.writeInt(y);

        int tempTaken = 0;
        if(taken) { tempTaken = 1; }

        dest.writeInt(tempTaken);
    }

    public static final Parcelable.Creator CREATOR
            = new Parcelable.Creator() {
        public GoldCoin createFromParcel(Parcel in) {
            return new GoldCoin(in);
        }

        public GoldCoin[] newArray(int size) {
            return new GoldCoin[size];
        }
    };
}
