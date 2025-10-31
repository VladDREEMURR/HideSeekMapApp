package com.example.hideseekmapapp.overpass;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

public class Thermometer implements Question {
    public QuestionType type = QuestionType.THERMOMETER;

    // статусы вопроса
    public boolean answered = false;

    // входные параметры
    public Envelope bounding_box = null;
    public Point start_point = null;
    public Point end_point = null;

    // результат
    public Polygon colder_area = null;
    public Polygon hotter_area = null;
    public boolean hotter = true; // hotter = true, если ближе к end_point
    public Polygon[] polygons;

    // private
    private GeometryFactory GF = new GeometryFactory();



    public Thermometer (Geometry input_area, double start_x, double start_y, double end_x, double end_y) {
        bounding_box = input_area.getEnvelopeInternal();
        start_point = GF.createPoint(new Coordinate(start_x, start_y));
        end_point = GF.createPoint(new Coordinate(end_x, end_y));
    }



    // делаем выводы после получения ответа
    public void set_answer (boolean hotter) {
        this.hotter = hotter;
        answered = true;
    }



    @Override
    public void exec_overpass() {}
    @Override
    public void create_areas() {
        // создание диаграммы Вороного
        Point[] points = {start_point, end_point};
        MapVoronoiCreator MVC = new MapVoronoiCreator(bounding_box, points);
        polygons = MVC.polygons;

        // записать "горячую" и "холодную" области
        for (int i = 0; i < polygons.length; i++) {
            if (polygons[i].covers(start_point)) {
                colder_area = polygons[i];
            }
            if (polygons[i].covers(end_point)) {
                hotter_area = polygons[i];
            }
        }
    }



    @Override
    public void generate_answer(double x, double y) {
        Point p = GF.createPoint(new Coordinate(x, y));
        set_answer(hotter_area.covers(p));
    }
}
