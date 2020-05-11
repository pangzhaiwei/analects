package com.zhaowei.analects.beans;

import java.io.Serializable;

public class ReplyBean implements Serializable {

    private int replyid;

    private String replycontent;

    private int postid;

    private int fromuserid;

    private int touserid;

    private long replytime;

    private String fromusername;

    private String tousername;

    public ReplyBean(int replyid, String replycontent, int postid, int fromuserid, int touserid, long replytime, String fromusername, String tousername) {
        this.replyid = replyid;
        this.replycontent = replycontent;
        this.postid = postid;
        this.fromuserid = fromuserid;
        this.touserid = touserid;
        this.replytime = replytime;
        this.fromusername = fromusername;
        this.tousername = tousername;
    }

    public ReplyBean() {
    }

    public int getReplyid() {
        return replyid;
    }

    public void setReplyid(int replyid) {
        this.replyid = replyid;
    }

    public String getReplycontent() {
        return replycontent;
    }

    public void setReplycontent(String replycontent) {
        this.replycontent = replycontent;
    }

    public int getPostid() {
        return postid;
    }

    public void setPostid(int postid) {
        this.postid = postid;
    }

    public long getReplytime() {
        return replytime;
    }

    public void setReplytime(long replytime) {
        this.replytime = replytime;
    }

    public int getFromuserid() {
        return fromuserid;
    }

    public void setFromuserid(int fromuserid) {
        this.fromuserid = fromuserid;
    }

    public int getTouserid() {
        return touserid;
    }

    public void setTouserid(int touserid) {
        this.touserid = touserid;
    }

    public String getFromusername() {
        return fromusername;
    }

    public void setFromusername(String fromusername) {
        this.fromusername = fromusername;
    }

    public String getTousername() {
        return tousername;
    }

    public void setTousername(String tousername) {
        this.tousername = tousername;
    }

}
