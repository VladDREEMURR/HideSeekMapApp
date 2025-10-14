package com.example.hideseekmapapp.overpass;

import androidx.annotation.NonNull;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.osmapi.map.handler.MapDataHandler;
import de.westnordost.osmapi.overpass.MapDataWithGeometryHandler;

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
    Map<Long, Geometry> variants; // варианты ответа

    /*
    У меня 2 типа вопроса:
    1) voronoi принадлежность к одной области
    2) принадлежность полученной из overpass области
     */
    /*
    Думаю, будет Map
    <Long, Geometry>
    <(ID), (область принадлежности)>
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
    private final MapDataWithGeometryHandler geom_handler = new MapDataWithGeometryHandler() {
        @Override
        public void handle(@NonNull BoundingBox bounds) {

        }


        @Override
        public void handle(@NonNull Node node) {
            Point p = GF.createPoint(
                    new Coordinate(node.getPosition().getLongitude(), node.getPosition().getLatitude())
            );
            point_storage.add(p);
        }


        @Override
        public void handle(@NonNull Way way, @NonNull BoundingBox bounds, @NonNull List<LatLon> geometry) {
            ArrayList<Point> p_arr = new ArrayList<>();

            for (LatLon ll : geometry) {
                p_arr.add(GF.createPoint(
                        new Coordinate(ll.getLongitude(), ll.getLatitude())
                ));
            }

            Point[] pts = new Point[p_arr.size()];
            p_arr.toArray(pts);
            Geometry g = GF.createMultiPoint(pts);
            point_storage.add(g.getCentroid());
        }


        @Override
        public void handle(@NonNull Relation relation, @NonNull BoundingBox bounds, @NonNull Map<Long, LatLon> nodeGeometries, @NonNull Map<Long, List<LatLon>> wayGeometries) {
            ArrayList<Point> p_arr = new ArrayList<>();

            // nodes
            for (LatLon ll : nodeGeometries.values()) {
                p_arr.add(GF.createPoint(
                        new Coordinate(ll.getLongitude(), ll.getLatitude())
                ));
            }
            // ways
            for (List<LatLon> ll_list : wayGeometries.values()) {
                for (LatLon ll : ll_list) {
                    p_arr.add(GF.createPoint(
                            new Coordinate(ll.getLongitude(), ll.getLatitude())
                    ));
                }
            }

            Point[] pts = new Point[p_arr.size()];
            p_arr.toArray(pts);
            Geometry g = GF.createMultiPoint(pts);
            point_storage.add(g.getCentroid());
        }
    };
}
