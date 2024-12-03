package com.example.regreen.myapplication.ModelData;

import java.util.List;
import java.util.stream.Collectors;

public class RecycleItem {
    private String idRecycle;
    private String name;
    private String detail;
    private String imageResource;
    private int category;
    private int isRecycle;


    public RecycleItem() {
    }

    public RecycleItem(String idRecycle, String name, String detail, String imageResource, int category, int isRecycle) {
        this.idRecycle = idRecycle;
        this.name = name;
        this.detail = detail;
        this.imageResource = imageResource;
        this.category = category;
        this.isRecycle = isRecycle;
    }

    public String getName() { return name; }
    public int getCategory() { return category; }
    public String getImageResource() { return imageResource; }
    public int getIsRecycle() { return isRecycle; }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageResource(String imageResource) {
        this.imageResource = imageResource;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public void setIsRecycle(int isRecycle) {
        this.isRecycle = isRecycle;
    }

    // Static method to filter items by category and isRecycle
    public static List<RecycleItem> getCategoryItems(List<RecycleItem> items, int category, int isRecycle) {
        return items.stream()
                .filter(item -> item.getCategory() == category && item.getIsRecycle() == isRecycle)
                .collect(Collectors.toList());
    }

    public String getIdRecycle() {return idRecycle;}

    public void setIdRecycle(String idRecycle) {this.idRecycle = idRecycle;}

    public String getDetail() {return detail;}

    public void setDetail(String detail) {this.detail = detail;}
}
