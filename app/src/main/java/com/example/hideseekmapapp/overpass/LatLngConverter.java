package com.example.hideseekmapapp.overpass;

public class LatLngConverter {
    private static double R_earth = 6378137.0;


    public static double longitude_to_x (double longitude) {
        return R_earth * longitude * Math.PI / 180.0;
    }


    public static double latitude_to_y (double latitude) {
        double tn = Math.tan((Math.PI / 4.0) + (latitude * Math.PI / 360.0));
        return R_earth * Math.log(tn);
    }


    public static double x_to_longitude (double x) {
        return (x * 180.0) / (R_earth * Math.PI);
    }


    public static double y_to_latitude (double y) {
        double tn = Math.exp(y / R_earth);
        double inside_tan = Math.atan(tn);
        return (inside_tan - Math.PI / 4.0) * 360.0 / Math.PI;
    }
}
