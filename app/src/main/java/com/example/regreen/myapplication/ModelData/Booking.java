package com.example.regreen.myapplication.ModelData;


import java.util.List;

public class Booking {
    private String idBooking;
    private String email;
    private List<String> listCategory;
    private String dateBook;
    private String dateAppointment;
    private String timeAppointment;
    private String address;
    private List<String> listImage;
    private String status;

    public Booking() {
    }


    public Booking(String idBooking, String email, List<String> listCategory, String dateBook, String dateAppointment, String timeAppointment, String address, List<String> listImage) {
        this.idBooking = idBooking;
        this.email = email;
        this.listCategory = listCategory;
        this.dateBook = dateBook;
        this.dateAppointment = dateAppointment;
        this.timeAppointment = timeAppointment;
        this.address = address;
        this.listImage = listImage;
    }

    public Booking(String idBooking, String email, List<String> listCategory, String dateBook, String dateAppointment, String timeAppointment, String address, List<String> listImage, String status) {
        this.idBooking = idBooking;
        this.email = email;
        this.listCategory = listCategory;
        this.dateBook = dateBook;
        this.dateAppointment = dateAppointment;
        this.timeAppointment = timeAppointment;
        this.address = address;
        this.listImage = listImage;
        this.status = status;
    }


    public String getEmail() {return email;}

    public String getDateBook() {return dateBook;}

    public void setDateBook(String dateBook) {this.dateBook = dateBook;}

    public String getDateAppointment() {return dateAppointment;}

    public void setDateAppointment(String dateAppointment) {this.dateAppointment = dateAppointment;}

    public String getAddress() {return address;}

    public void setAddress(String address) {this.address = address;}

    public List<String> getListImage() {return listImage;}

    public void setListImage(List<String> listImage) {this.listImage = listImage;}

    public String getTimeAppointment() {return timeAppointment;}

    public void setTimeAppointment(String timeAppointment) {this.timeAppointment = timeAppointment;}

    public String getIdBooking() {return idBooking;}

    public void setIdBooking(String idBooking) {this.idBooking = idBooking;}

    public List<String> getListCategory() {return listCategory;}

    public void setListCategory(List<String> listCategory) {this.listCategory = listCategory;}

    public String getStatus() {return status;}

    public void setStatus(String status) {this.status = status;}
}
