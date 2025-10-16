package com.example.hideseekmapapp.overpass;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

public class Radar implements Question {
    public QuestionType type = QuestionType.RADAR;

    // статусы вопроса
    public boolean answered = false;

    // входные пааремтры
    public double radius = 0.0; // радиус в километрах
    public double lon = 0.0; // центральная точка (x)
    public double lat = 0.0; // центральная точка (y)

    // результат
    public Polygon area = null;
    public boolean is_inside = true;

    // private
    private GeometryFactory GF = new GeometryFactory();



    public Radar (double x, double y, double radius) {
        this.lon = x;
        this.lat = y;
        this.radius = radius;

        create_areas();
    }



    // делаем выводы после получения ответа
    public void set_answer (boolean inside) {
        is_inside = inside;
        answered = true;
    }


    @Override
    public void exec_overpass() {
    }


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
        Coordinate[] coord_array = new Coordinate[coord_list.size()];
        coord_list.toArray(coord_array);
        area = GF.createPolygon(coord_array);
    }



    @Override
    public void generate_answer(double x, double y) {
        Point p = GF.createPoint(new Coordinate(x, y));
        is_inside = area.covers(p);
    }
}
