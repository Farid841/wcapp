package com.example.wcapp;

public class WcAPI {
    private String id;
    private Double longi;
    private Double lati;
    private String descr;
    private String note;
private String avis;


    public WcAPI( Double longi, Double lati, String descr, String note, String avis) {
        this.longi = longi;
        this.lati = lati;
        this.descr = descr;
        this.note = note;
        this.avis = avis;
    }
    public String getId() {
        return id;
    }
    public Double getLongi() {
        return longi;
    }
    public Double getLati() {
        return lati;
    }
    public String getDescr() {
        return descr;
    }
    public String getNote() {
        return note;
    }
    public String getAvis() {
        return avis;
    }
}
