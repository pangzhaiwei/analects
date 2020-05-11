package com.zhaowei.analects.beans;

import java.io.Serializable;

public class ReplyPostBean implements Serializable {

    //构建postbean
    private int postid;

    private String posttitle;

    private String postcontent;

    private int userid;

    private String username;

    private Long posttime;

    //构建reply
    private String replycontent;

    private long replytime;

    private String fromusername;

    private String tousername;

    public int getPostid() {
        return postid;
    }

    public void setPostid(int postid) {
        this.postid = postid;
    }

    public String getPosttitle() {
        return posttitle;
    }

    public void setPosttitle(String posttitle) {
        this.posttitle = posttitle;
    }

    public String getPostcontent() {
        return postcontent;
    }

    public void setPostcontent(String postcontent) {
        this.postcontent = postcontent;
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

    public Long getPosttime() {
        return posttime;
    }

    public void setPosttime(Long posttime) {
        this.posttime = posttime;
    }

    public String getReplycontent() {
        return replycontent;
    }

    public void setReplycontent(String replycontent) {
        this.replycontent = replycontent;
    }

    public long getReplytime() {
        return replytime;
    }

    public void setReplytime(long replytime) {
        this.replytime = replytime;
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

    public ReplyPostBean(int postid, String posttitle, String postcontent, int userid, String username, Long posttime, String replycontent, long replytime, String fromusername, String tousername) {
        this.postid = postid;
        this.posttitle = posttitle;
        this.postcontent = postcontent;
        this.userid = userid;
        this.username = username;
        this.posttime = posttime;
        this.replycontent = replycontent;
        this.replytime = replytime;
        this.fromusername = fromusername;
        this.tousername = tousername;
    }

    public ReplyPostBean() {
    }

}
