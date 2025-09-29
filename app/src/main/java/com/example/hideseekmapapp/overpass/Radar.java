package com.example.hideseekmapapp.overpass;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

public class Radar implements Question {
    public QuestionType type = QuestionType.RADAR;

    // входные пааремтры
    public double radius; // радиус в километрах
    public double lon; // центральная точка (x)
    public double lat; // центральная точка (y)

    // результат
    public Polygon area = null;
    public boolean is_inside = true;


    public Radar (double x, double y, double radius) {
        this.lon = x;
        this.lat = y;
        this.radius = radius;
    }


    @Override
    public void prepare() {}
    @Override
    public void exec_overpass() {}


    @Override
    public void create_areas() {
        // сделали свой круг
        com.mapbox.geojson.Point cp = com.mapbox.geojson.Point.fromLngLat(lon, lat);
        com.mapbox.geojson.Polygon circle = com.mapbox.turf.TurfTransformation.circle(cp, radius, 180, com.mapbox.turf.TurfConstants.UNIT_KILOMETERS);
        // теперь переделываем его в свой формат
        List<com.mapbox.geojson.Point> points = circle.coordinates().get(0);
        ArrayList<Coordinate> coord_list = new ArrayList<Coordinate>();
        for (com.mapbox.geojson.Point p : points) {
            coord_list.add(new Coordinate(p.longitude(), p.latitude()));
        }
        GeometryFactory GF = new GeometryFactory();
        Coordinate[] coord_array = new Coordinate[coord_list.size()];
        coord_list.toArray(coord_array);
        area = GF.createPolygon(coord_array);
    }


    @Override
    public void generate_answer(double x, double y) {
        GeometryFactory GF = new GeometryFactory();
        Point p = GF.createPoint(new Coordinate(x, y));
        is_inside = area.covers(p);
    }
}
