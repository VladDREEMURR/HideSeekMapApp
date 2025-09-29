package com.example.hideseekmapapp.overpass;

// TODO: полная реализация Thermometer вопросов

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.polygonize.Polygonizer;

import java.util.Collection;

public class Thermometer implements Question {
    public QuestionType type = QuestionType.THERMOMETER;

    // входные параметры
    public Envelope bounding_box;
    public Geometry thermo_box;
    public Point start_point;
    public Point end_point;

    // результат
    public Polygon colder_area = null;
    public Polygon hotter_area = null;
    public boolean hotter = true; // hotter = true, если ближе к end_point

    // test params
    public Collection<Polygon> polygons;


    public Thermometer (Geometry input_area, double start_x, double start_y, double end_x, double end_y) {
        GeometryFactory GF = new GeometryFactory();

        this.bounding_box = input_area.getEnvelopeInternal();
        this.thermo_box = input_area.getEnvelope();
        this.start_point = GF.createPoint(new Coordinate(start_x, start_y));
        this.end_point = GF.createPoint(new Coordinate(end_x, end_y));
    }


    @Override
    public void prepare() {}
    @Override
    public void exec_overpass() {}
    @Override
    public void generate_answer(double x, double y) {}


    @Override
    public void create_areas() {
        // посчитать среднюю точку
        double mid_x, mid_y;
        mid_x = (end_point.getX() + start_point.getX()) / 2.0;
        mid_y = (end_point.getY() + start_point.getY()) / 2.0;

        // вывести срединный перпендикуляр (прямую вида y = kx + b)
        double k, b;
        if (start_point.getX() == end_point.getX()) {
            k = 0.0;
            b = mid_y;
        } else {
            k = (end_point.getY() - start_point.getY()) / (end_point.getX() - start_point.getX()); // наклон исходной прямой
            k = -1.0 / k; // наклон перпендикуляра
            b = mid_y - k * mid_x;
        }

        // посчитать точки пересечения с полигоном
        double x1, y1;
        double x2, y2;
        x1 = bounding_box.getMinX();
        x2 = bounding_box.getMaxX();
        y1 = k * x1 + b;
        y2 = k * x2 + b;
        if (y1 < bounding_box.getMinY() || y1 > bounding_box.getMaxY()) {
            if (y1 < bounding_box.getMinY()) {
                y1 = bounding_box.getMinY();
            } else {
                y1 = bounding_box.getMaxY();
            }
            x1 = (y1 - b) / k;
        }
        if (y2 < bounding_box.getMinY() || y2 > bounding_box.getMaxY()) {
            if (y2 < bounding_box.getMinY()) {
                y2 = bounding_box.getMinY();
            } else {
                y2 = bounding_box.getMaxY();
            }
            x2 = (y2 - b) / k;
        }

        // сформировать линию из этих двух точек
        Coordinate[] coord_array = {
                new Coordinate(x1, y1),
                new Coordinate(x2, y2)
        };
        GeometryFactory GF = new GeometryFactory();
        LineString line = GF.createLineString(coord_array);
        Geometry uni = thermo_box.union(line);
        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(uni);
        polygons = polygonizer.getPolygons();
    }
}
