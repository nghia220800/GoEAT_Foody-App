package com.example.goeat;

public class PriceRange {
        int max_price;
        int min_price;
        public PriceRange(){

        }
        public String getString(){
            return min_price+" "+max_price;
        }
        public long getMax_price(){
            return max_price;
        }
        public long getMin_price(){
            return min_price;
        }

    public void setMax_price(int max_price) {
        this.max_price = max_price;
    }

    public void setMin_price(int min_price) {
        this.min_price = min_price;
    }
}
