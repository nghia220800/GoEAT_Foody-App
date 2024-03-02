package com.example.goeat;

public class Position {
       boolean is_verified;
        double latitude;
        double longitude;
        public Position(){

        }
        public String getString(){
            return is_verified+" "+latitude+" "+longitude;
        }
        public boolean getIs_verified(){
            return is_verified;
        }
        public double getLatitude(){
            return latitude;
        }
        public double getLongitude(){
            return longitude;
        }

    public void setIs_verified(boolean is_verified) {
        this.is_verified = is_verified;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
