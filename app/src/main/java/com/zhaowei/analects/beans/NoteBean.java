package com.zhaowei.analects.beans;

import java.io.Serializable;

public class NoteBean implements Serializable {

    private int noteid;
    private String notetitle;
    private String notecontent;
    private long notetime;
    private int userid;
    private String username;

    public NoteBean() {
    }

    public NoteBean(int noteid, String notetitle, String notecontent, long notetime, int userid, String username) {
        this.noteid = noteid;
        this.notetitle = notetitle;
        this.notecontent = notecontent;
        this.notetime = notetime;
        this.userid = userid;
        this.username = username;
    }

    public int getNoteid() {
        return noteid;
    }

    public void setNoteid(int noteid) {
        this.noteid = noteid;
    }

    public String getNotetitle() {
        return notetitle;
    }

    public void setNotetitle(String notetitle) {
        this.notetitle = notetitle;
    }

    public String getNotecontent() {
        return notecontent;
    }

    public void setNotecontent(String notecontent) {
        this.notecontent = notecontent;
    }

    public long getNotetime() {
        return notetime;
    }

    public void setNotetime(long notetime) {
        this.notetime = notetime;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
