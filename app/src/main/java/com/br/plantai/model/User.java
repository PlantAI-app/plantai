package com.br.plantai.model;

public class User {
    private String email;
    private String classificationObject;
    private String classificationRate;
    private Double lat;
    private Double lng;
    private String data;

    public User() {
    }

    public User(String email, Double lat, Double lng, String classificationRate,
                String classificationObject, String data) {
        this.email = email;
        this.classificationRate = classificationRate;
        this.classificationObject = classificationObject;
        this.lat = lat;
        this.lng = lng;
        this.data = data;
    }


    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String name) {
        this.email = name;
    }

    public String getClassificationObject() {
        return classificationObject;
    }

    public void setClassificationObject(String classificationObject) {
        this.classificationObject = classificationObject;
    }

    public String getClassificationRate() {
        return classificationRate;
    }

    public void setClassificationRate(String classificationRate) {
        this.classificationRate = classificationRate;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
