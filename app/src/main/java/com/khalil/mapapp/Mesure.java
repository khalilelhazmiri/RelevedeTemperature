package com.khalil.mapapp;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Mesure {

    @SerializedName("idMesure")
    @Expose
    private String idMesure;
    @SerializedName("User")
    @Expose
    private String user;
    @SerializedName("Mesure")
    @Expose
    private String mesure;
    @SerializedName("LatLng")
    @Expose
    private String latLng;
    @SerializedName("City")
    @Expose
    private String city;
    @SerializedName("Country")
    @Expose
    private String country;
    @SerializedName("Timestamp")
    @Expose
    private String timestamp;

    /**
     * No args constructor for use in serialization
     *
     */
    public Mesure() {
    }

    /**
     *
     * @param country
     * @param city
     * @param mesure
     * @param idMesure
     * @param user
     * @param latLng
     * @param timestamp
     */
    public Mesure(String idMesure, String user, String mesure, String latLng, String city, String country, String timestamp) {
        super();
        this.idMesure = idMesure;
        this.user = user;
        this.mesure = mesure;
        this.latLng = latLng;
        this.city = city;
        this.country = country;
        this.timestamp = timestamp;
    }

    public String getIdMesure() {
        return idMesure;
    }

    public void setIdMesure(String idMesure) {
        this.idMesure = idMesure;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMesure() {
        return mesure;
    }

    public void setMesure(String mesure) {
        this.mesure = mesure;
    }

    public String getLatLng() {
        return latLng;
    }

    public void setLatLng(String latLng) {
        this.latLng = latLng;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }


}