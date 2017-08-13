package com.sookmyung.heartbeatfornull.virtualtravel;

public class SuggestStreetView {
    public final String name;
    public final double lat;
    public final double lon;
    public final String url;

    public SuggestStreetView(String name, double lat, double lon, String url) {
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.url = url;
    }
}
