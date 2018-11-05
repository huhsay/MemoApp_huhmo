package com.studiobethejustice.huhmo.model;

import java.util.Date;

public class Memo {
    private String key;
    private String text, title;
    private long createDate, updateDate;


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getText() {
        return text;
    }

    public long getCreateDate() {
        return createDate;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public String getTitle() {
        if (text != null) {
            if (text.indexOf("\n") > -1) {
                return text.substring(0, text.indexOf("\n"));
            } else {
                return text;
            }
        }
        return null;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }
}
