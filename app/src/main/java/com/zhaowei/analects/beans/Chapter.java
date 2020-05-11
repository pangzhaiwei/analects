package com.zhaowei.analects.beans;

import java.util.List;

public class Chapter {

    private String title;
    private List<String> content;

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }

    public List<String> getContent() {
        return content;
    }

    @Override
    public String toString() {
        String text = "";
        for(int i = 0; i < content.size(); i++){
            //句子缩进
            String temp = "\u3000\u3000";
            //句子换行同时空一行
            String next = "\n\n";
            if((i+1) == content.size()){
                next = "\n";
            }
            text += (temp + content.get(i) + next);
        }
        return text;
    }
}
