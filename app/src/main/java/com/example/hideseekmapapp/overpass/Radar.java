package com.example.hideseekmapapp.overpass;

// TODO: полная реализация Radar вопросов

import java.util.List;

public class Radar implements Question {
    // какой это тип вопроса
    public QuestionType type = QuestionType.RADAR;

    // входные пааремтры
    public double radius = 5.0; // радиус в километрах
    public Point center = new Point(0.0, 0.0); // центральная точка

    // данные вариантов ответа
    public Polygon circle = new Polygon();
    public boolean is_inside = true;



    Radar (Point center, double radius) {
        this.center = center;
        this.radius = radius;
    }


    @Override
    public void prepare() {}
    @Override
    public void exec_overpass() {}


    @Override
    public void create_areas() {
        // сделали свой круг
        com.mapbox.geojson.Point cp = com.mapbox.geojson.Point.fromLngLat(center.lon, center.lat);
        com.mapbox.geojson.Polygon circle = com.mapbox.turf.TurfTransformation.circle(cp, radius, 360, com.mapbox.turf.TurfConstants.UNIT_KILOMETERS);
        // теперь переделываем его в свой формат
        List<com.mapbox.geojson.Point> points = circle.coordinates().get(0);

    }


    @Override
    public void apply_answer() {}
}
