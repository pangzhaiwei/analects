package com.zhaowei.analects.beans;

import java.io.Serializable;

public class PostBean implements Serializable {

    private int postid;

    private String posttitle;

    private String postcontent;

    private int userid;

    private String username;

    private Long posttime;

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

    public PostBean(int postid, String posttitle, String postcontent, int userid, String username, Long posttime) {
        this.postid = postid;
        this.posttitle = posttitle;
        this.postcontent = postcontent;
        this.userid = userid;
        this.username = username;
        this.posttime = posttime;
    }

    public PostBean() {
    }

}
