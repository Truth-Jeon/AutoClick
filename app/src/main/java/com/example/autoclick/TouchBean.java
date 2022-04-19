package com.example.autoclick;

public class TouchBean {

    int x;
    int y;
    long time;

    public TouchBean(){
//        String[] name_split = data.split(",");
//        x = Integer.parseInt(name_split[0]);
//        y = Integer.parseInt(name_split[1]);
//        time = Long.parseLong(name_split[2]);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public long getTime() {
        return time;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
