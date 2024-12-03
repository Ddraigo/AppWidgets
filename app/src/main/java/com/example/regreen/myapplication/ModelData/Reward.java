package com.example.regreen.myapplication.ModelData;

import java.io.Serializable;

public class Reward implements Serializable {

    private String idReward;
    private int point;
    private String category;
    private String title;
    private String condition;
    private String imageResource;
    private String dateUp;

    public Reward() {
    }


    public Reward(String idReward, int point, String category, String title, String condition, String imageResource, String dateUp) {
        this.idReward = idReward;
        this.point = point;
        this.category = category;
        this.title = title;
        this.condition = condition;
        this.imageResource = imageResource;
        this.dateUp = dateUp;
    }


    public String getIdReward() {return idReward;}
    public void setIdReward(String idReward) {this.idReward = idReward;}

    public int getPoint() {return point;}
    public void setPoint(int point) {this.point = point;}

    public String getCategory() {return category;}
    public void setCategory(String category) {this.category = category;}

    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}

    public String getCondition() {return condition;}
    public void setCondition(String condition) {this.condition = condition;}

    public String getImageResource() {return imageResource;}
    public void setImageResource(String imageResource) {this.imageResource = imageResource;}

    public String getDateUp() {return dateUp;}
    public void setDateUp(String dateUp) {this.dateUp = dateUp;}
}
