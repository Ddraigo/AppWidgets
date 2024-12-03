package com.example.regreen.myapplication.ModelData;



public class User {

    private String email;
    private String passWord;
    private String name;
    private String numberPhone;
    private String address;
    private String avatar;
    private int pointReward;

    public User() {}

    public User(String email, String passWord) {
        this.email = email;
        this.passWord = passWord;
    }

    public User(String email, String passWord, String name, String numberPhone, String address, String avatar) {
        this.email = email;
        this.passWord = passWord;
        this.name = name;
        this.numberPhone = numberPhone;
        this.address = address;
        this.avatar = avatar;
        this.pointReward = 0;
    }

    public User(String email, String passWord, String name, String numberPhone, String address, String avatar, int pointReward) {
        this.email = email;
        this.passWord = passWord;
        this.name = name;
        this.numberPhone = numberPhone;
        this.address = address;
        this.avatar = avatar;
        this.pointReward = pointReward;
    }

    public User(String email, String name, String numberPhone, String address) {
        this.email = email;
        this.name = name;
        this.numberPhone = numberPhone;
        this.address = address;
    }


    public boolean canEditProfile(String sessionEmail) {
        return this.email.equals(sessionEmail);
    }

    // Phương thức cập nhật thông tin
    public void updateProfile(String name, String numberPhone, String address, String avatar) {
        if (name != null) this.name = name;
        if (numberPhone != null) this.numberPhone = numberPhone;
        if (address != null) this.address = address;
        if (avatar != null) this.avatar = avatar;
    }


    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public String getPassWord() {return passWord;}

    public void setPassWord(String passWord) {this.passWord = passWord;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getNumberPhone() {return numberPhone;}

    public void setNumberPhone(String numberPhone) {this.numberPhone = numberPhone;}

    public String getAddress() {return address;}

    public void setAddress(String address) {this.address = address;}

    public String getAvatar() {return avatar;}

    public void setAvatar(String avatar) {this.avatar = avatar;}

    public int getPointReward() {return pointReward;}

    public void setPointReward(int pointReward) {this.pointReward = pointReward;}
}
