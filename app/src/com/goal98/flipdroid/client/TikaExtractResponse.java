package com.goal98.flipdroid.client;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jleo
 * Date: 3/20/11
 * Time: 6:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class TikaExtractResponse {
    public String getContent() {
        return content;
    }
    String author;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    String title;

    public List<String> getImages() {
        return images;
    }

    String content;
    List<String> images;

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;

    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
