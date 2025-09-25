package com.example.hideseekmapapp.overpass;

import java.util.ArrayList;


public class MultiPolygon {
    public ArrayList <Polygon> polygons = new ArrayList<Polygon>();
}


class Polygon {
    public ArrayList <Point> outer = new ArrayList<Point>();
    public ArrayList <ArrayList <Point>> inners = new ArrayList<ArrayList<Point>>();
}


class Point {
    public double lat = 0.0;
    public double lon = 0.0;

    Point (double latitude, double longitude) {
        lat = latitude;
        lon = longitude;
    }
}
