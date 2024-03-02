package com.example.goeat;

import java.util.List;

public class HistoryVal implements Comparable<HistoryVal> {
    String placeID;
    String district;
    long timestamp;

    public HistoryVal() {
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getString(){
        String str=(placeID==null)?"":placeID;
        str+=(district==null)?"":district;
        str+=timestamp;
        return str;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPlaceID() {
        return placeID;
    }

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }
    public int compareTo(HistoryVal compareHVal) {
        int res = Long.compare(compareHVal.timestamp, timestamp);
        return res;
    }
}
