package com.example.regreen.myapplication.ModelData;

public class AccessPoint {

    private String id;
    private String receivingPoint;
    private String category;


    public AccessPoint() {
    }

    public AccessPoint(String id, String receivingPoint, String category) {
        this.id = id;
        this.receivingPoint = receivingPoint;
        this.category = category;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getReceivingPoint() {
        return receivingPoint;
    }

    public void setReceivingPoint(String receivingPoint) {
        this.receivingPoint = receivingPoint;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
