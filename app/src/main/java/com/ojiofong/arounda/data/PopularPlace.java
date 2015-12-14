package com.ojiofong.arounda.data;


import java.io.Serializable;

public class PopularPlace implements Serializable {

    private String placeKeywordName;
    private int iconID;
    private String placeID;


    public PopularPlace(String placeKeywordName, int iconID, String placeID) {
        super();
        this.placeKeywordName = placeKeywordName;
        this.iconID = iconID;
        this.placeID = placeID;
    }


    public String getPlaceName() {
        return placeKeywordName;
    }


    public int getIconID() {
        return iconID;
    }

    public String getPlaceID() {
        return placeID;
    }


}
