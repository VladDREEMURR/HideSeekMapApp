package com.example.hideseekmapapp.overpass;

import java.util.ArrayList;

public class Polygon {
    public ArrayList <Point> points;
}


class Point {
    public double lat;
    public double lon;

    Point (double latitude, double longitude) {
        lat = latitude;
        lon = longitude;
    }
}
