package com.example.hideseekmapapp.overpass;

import java.util.ArrayList;

public class Matching implements Question {
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
    public void apply_answer() {

    }


    @Override
    public QuestionResult return_result() {
        return null;
    }
}
