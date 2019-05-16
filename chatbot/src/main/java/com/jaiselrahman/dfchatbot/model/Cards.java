package com.jaiselrahman.dfchatbot.model;

/**
 * Created by jaisel on 12/1/18.
 */

public class Cards {
    private String title, subtitle, imgUrl;

    public String getImgUrl() {
        return imgUrl;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getTitle() {
        return title;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
