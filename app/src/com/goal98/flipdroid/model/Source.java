package com.goal98.flipdroid.model;

import java.util.Date;

public class Source {

    public static final String TABLE_NAME = "source";
    public static final String KEY_SOURCE_NAME = "source_name";
    public static final String KEY_SOURCE_DESC = "source_desc";
    public static final String KEY_SOURCE_ID = "source_id";
    public static final String KEY_IMAGE_URL = "image_url";
    public static final String KEY_CONTENT_URL = "content_url";
    public static final String KEY_SOURCE_TYPE = "source_type";
    public static final String KEY_CAT = "cat";
    public static final String KEY_UPDATE_TIME = "update_on";

    private String name;
    private String id;
    private String desc;
    private String accountType;
    private String imageUrl;
    private String contentUrl;
    private Date updateTime;

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
