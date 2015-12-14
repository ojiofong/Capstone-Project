package com.ojiofong.arounda.data;


import org.json.JSONException;
import org.json.JSONObject;


public class Place {
    private String id;
    private String name;
    private String rating;
    private String address;
    private String reference;
    private Double latitude;
    private Double longitude;
 
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }

	public String getRating() {
		return rating;
	}
	public void setRating(String rating) {
		this.rating = rating;
	}
	public String getReference(){
		return reference;
	}
	
	public void setReference(String reference){
		this.reference = reference;
	}
	
    
 
    public static Place buildPlace(JSONObject jsonObject) {
        try {
            Place result = new Place();
            JSONObject geometry = (JSONObject) jsonObject.get("geometry");
            JSONObject location = (JSONObject) geometry.get("location");
            result.setLatitude((Double) location.get("lat"));
            result.setLongitude((Double) location.get("lng"));
            result.setName(jsonObject.getString("name"));
            result.setReference(jsonObject.getString("reference"));
            
            
            try{
            	 result.setRating(jsonObject.getString("rating"));
            }catch(JSONException e){
            	result.setRating("0");
            }
           

			try {
				result.setAddress(jsonObject.getString("formatted_address"));
			} catch (JSONException e) {
                // If no address then set the vicinity
				result.setAddress(jsonObject.getString("vicinity"));
				
			}
			
            return result;
        } catch (JSONException ex) {
        }
        return null;
    }
 
}