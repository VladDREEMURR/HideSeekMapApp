package com.example.hideseekmapapp.overpass;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;

// TODO: полная реализация Matching вопросов

public class Matching implements Question {
    // какой это тип вопроса
    public QuestionType type = QuestionType.MATCHING;

    // главные данные вопроса
    public MatchingType matching_type; // какой именно вопрос совпадения мы задаём
    public Long object_id; // объект, который мы ищем

    // внутренние данные вопроса (private)
    ArrayList <Point> overpass_points; // точки, получаемые из overpass
    ArrayList <Polygon> voronoi_polygons; // области близости к overpass точкам (область состоит из одного полигона)
    ArrayList <ArrayList <Polygon>> overpass_areas; // области, получаемые из overpass (например, район) (область может состоять из нескольких полигонов)


    Matching (MatchingType type) {
        matching_type = type;
    }


    @Override
    public void prepare() {

    }


    @Override
    public void exec_overpass() {

    }


    @Override
    public void create_areas() {

    }


    @Override
    public void generate_answer(double x, double y) {

    }
}
