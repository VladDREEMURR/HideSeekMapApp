package com.example.hideseekmapapp.overpass;

// TODO: полная реализация Thermometer вопросов

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

public class Thermometer implements Question {
    public QuestionType type = QuestionType.THERMOMETER;

    // входные параметры
    public Envelope bounding_box;
    public Point start_point;
    public Point end_point;

    // результат
    public Polygon colder_area = null;
    public Polygon hotter_area = null;
    public boolean hotter = true; // hotter = true, если ближе к end_point
    public Polygon[] polygons;


    public Thermometer (Geometry input_area, double start_x, double start_y, double end_x, double end_y) {
        GeometryFactory GF = new GeometryFactory();

        // обработать входные данные
        bounding_box = input_area.getEnvelopeInternal();
        start_point = GF.createPoint(new Coordinate(start_x, start_y));
        end_point = GF.createPoint(new Coordinate(end_x, end_y));

        // создание диаграммы
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
    public void prepare() {}
    @Override
    public void exec_overpass() {}
    @Override
    public void create_areas() {}


    @Override
    public void generate_answer(double x, double y) {
        GeometryFactory GF = new GeometryFactory();
        Point p = GF.createPoint(new Coordinate(x, y));
        if (hotter_area.covers(p)) {
            hotter = true;
        } else {
            hotter = false;
        }
    }
}
