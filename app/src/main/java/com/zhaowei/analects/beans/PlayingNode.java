package com.zhaowei.analects.beans;

public class PlayingNode {

    private int position;
    private int listKind;

    public PlayingNode(int position, int listKind) {
        this.position = position;
        this.listKind = listKind;
    }

    public PlayingNode() {
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getListKind() {
        return listKind;
    }

    public void setListKind(int listKind) {
        this.listKind = listKind;
    }
}
