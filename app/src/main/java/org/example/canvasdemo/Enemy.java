package org.example.canvasdemo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Random;

/**
 * Created by cecil on 21-03-2017.
 */

                    //Gør sådan, at fjener kan gemmes, når app dør og skal genskabes.
public class Enemy implements Parcelable {

    private Level level; //Definer, hvor hurtig fjenden er
    private int XPos; //x postion for fjenden
    private int YPos;//y postion for fjenden
    private Direction direction; //Retningen fjenden skal gå
    private int directionChangeCounter; //Hvor længe, fjenden skal gå i retningen


    public Enemy(Level level, int XPos, int YPos) {
        this.level = level;
        this.XPos = XPos;
        this.YPos = YPos;
        moveCalc();
    }

    /** Kontruktør, når en fjende skal genskabes. */
    public Enemy(Parcel in) {
        level = Level.values()[in.readInt()];
        XPos = in.readInt();
        YPos = in.readInt();
        direction = Direction.values()[in.readInt()];
        directionChangeCounter = in.readInt();
    }

    public Enemy() {
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public int getXPos() {
        return XPos;
    }

    public void setXPos(int XPos) {
        this.XPos = XPos;
    }

    public int getYPos() {
        return YPos;
    }

    public void setYPos(int YPos) {
        this.YPos = YPos;
    }


    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }


    public int getDirectionChangeCounter() {
        return directionChangeCounter;
    }
    public void decreaseLength() {
        this.directionChangeCounter -= 1;
    }

    /**
     * Beregner ny bevægelse til fjenden.
     * Hvilket vil sige retningen, og hvor længe der skal gås i den retning
     * */
    public void moveCalc(){
        Random random = new Random();
        direction = Direction.values()[random.nextInt(Direction.values().length)];
        directionChangeCounter =(2 + random.nextInt(6))*6;
    }


    @Override
    public int describeContents() {
        return 0;
    }


    /** Metoderne herunder er med til, at en fjende kan genskabes */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(level.ordinal());
        dest.writeInt(XPos);
        dest.writeInt(YPos);
        dest.writeInt(direction.ordinal());
        dest.writeInt(directionChangeCounter);
    }

    public static final Parcelable.Creator CREATOR
            = new Parcelable.Creator() {
        public Enemy createFromParcel(Parcel in) {
            return new Enemy(in);
        }

        public Enemy[] newArray(int size) {
            return new Enemy[size];
        }
    };
}
