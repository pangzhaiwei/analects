package com.zhaowei.analects.beans;

import java.io.Serializable;

public class Paragraph implements Serializable {

    private String classical;
    private String modern;

    public Paragraph(String classical, String modern) {
        this.classical = classical;
        this.modern = modern;
    }

    public Paragraph() {
    }

    public String getClassical() {
        return classical;
    }

    public void setClassical(String classical) {
        this.classical = classical;
    }

    public String getModern() {
        return modern;
    }

    public void setModern(String modern) {
        this.modern = modern;
    }
}
