package com.zhaowei.analects.beans;

import java.io.Serializable;

public class User implements Serializable {

    private int id;

    private int icon;

    private String name;

    private String md5pwd;

    public User() {
    }

    public User(int id, int icon, String name, String md5pwd) {
        this.id = id;
        this.icon = icon;
        this.name = name;
        this.md5pwd = md5pwd;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMd5pwd() {
        return md5pwd;
    }

    public void setMd5pwd(String md5pwd) {
        this.md5pwd = md5pwd;
    }
}
