package com.example.hideseekmapapp.overpass;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.osmapi.map.handler.MapDataHandler;

// TODO: полная реализация Matching вопросов

public class Matching implements Question {
    public QuestionType type = QuestionType.MATCHING;

    // входные данные
    public MatchingType matching_type; // какой именно вопрос совпадения мы задаём
    public Long object_id; // объект, который мы ищем

    // внутренние данные вопроса (private)
    ArrayList <Point> overpass_points; // точки, получаемые из overpass
    ArrayList <Polygon> voronoi_polygons; // области близости к overpass точкам (область состоит из одного полигона)
    ArrayList <ArrayList <Polygon>> overpass_areas; // области, получаемые из overpass (например, район) (область может состоять из нескольких полигонов)

    // результат

    /*
    У меня 2 типа вопроса:
    1) voronoi принадлежность к одной области
    2) принадлежность полученной из overpass области
     */


    public Matching (MatchingType type) {
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


    // обрабатывает запрос на точки
    private final MapDataHandler point_request_handler = new MapDataHandler() {
        @Override
        public void handle(BoundingBox bounds) {} // не нужно

        @Override
        public void handle(Node node) {

        }

        @Override
        public void handle(Way way) {

        }

        @Override
        public void handle(Relation relation) {

        }
    };
}
