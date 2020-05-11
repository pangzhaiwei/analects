package com.zhaowei.analects.beans;

import java.io.Serializable;

public class MusicInfo implements Serializable {

    private int id;

    private String name;

    private String path;

    private int part;

    public MusicInfo() {
    }

    public MusicInfo(int id, String name, String path, int part) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.part = part;
    }

    public int getPart() {
        return part;
    }

    public void setPart(int part) {
        this.part = part;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
