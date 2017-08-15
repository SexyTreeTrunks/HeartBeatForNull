package com.sookmyung.heartbeatfornull.footprint;

public class FootprintInfoListViewItem {
    private String user_id;
    private String fp_contents;
    private String fp_lat;
    private String fp_lon;

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setFp_contents(String fp_contents) {
        this.fp_contents = fp_contents;
    }

    public void setFp_lat(String fp_lat) {
        this.fp_lat = fp_lat;
    }

    public void setFp_lon(String fp_lon) {
        this.fp_lon = fp_lon;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getFp_contents() {
        return fp_contents;
    }

    public String getFp_lat() {
        return fp_lat;
    }

    public String getFp_lon() {
        return fp_lon;
    }


}
