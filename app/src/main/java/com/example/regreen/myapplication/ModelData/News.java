package com.example.regreen.myapplication.ModelData;

import java.io.Serializable;

public class News implements Serializable{

    private String idNew;
    private String category;
    private String title;
    private String content;
    private String imageResource;
    private String dateUp;

    public News() {}

    public News(String idNew, String title, String imageResource, String dateUp) {
        this.idNew = idNew;
        this.title = title;
        this.imageResource = imageResource != null ? imageResource : "";
        this.dateUp = dateUp;
    }

    public News(String idNew, String category, String title, String content, String imageResource, String dateUp) {
        this.idNew = idNew;
        this.category = category;
        this.title = title;
        this.content = content;
        this.imageResource = imageResource != null ? imageResource : "";
        this.dateUp = dateUp;
    }

    public String getIdNew() { return idNew; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getImageResource() { return imageResource; }
    public String getDateUp() { return dateUp; }
    public String getContent() { return content; }

    public void setTitle(String title) { this.title = title; }
    public void setImageResource(String imageResource) { this.imageResource = imageResource; }
    public void setCategory(String category) { this.category = category; }
    public void setDateUp(String dateUp) { this.dateUp = dateUp; }
    public void setContent(String content) { this.content = content; }

}
