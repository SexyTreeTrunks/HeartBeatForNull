package com.sookmyung.heartbeatfornull.footprint;

import java.util.ArrayList;
import java.util.List;

public class FootprintResponse{

    private List<FootprintInfo> result = new ArrayList<FootprintInfo>();

    public List<FootprintInfo> getResult() {
        return result;
    }
    public void setResult(List<FootprintInfo> result) {
        this.result = result;
    }
}
