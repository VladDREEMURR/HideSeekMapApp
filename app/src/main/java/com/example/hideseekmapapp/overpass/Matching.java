package com.example.hideseekmapapp.overpass;

import androidx.annotation.NonNull;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.osmapi.overpass.MapDataWithGeometryHandler;
import de.westnordost.osmapi.overpass.OverpassMapDataApi;

// TODO: полная реализация Matching вопросов

public class Matching implements Question {
    public final QuestionType type = QuestionType.MATCHING;

    // статусы вопроса
    public boolean answered;

    // входные данные
    public MatchingType matching_type; // какой именно вопрос совпадения мы задаём
    public Long object_id; // объект, который мы ищем

    // результаты
    public Map<Long, String> names; // названия вариантов ответа
    public Map<Long, Geometry> variants; // варианты ответа
    public Long id_of_interest; // ключ к Map, подходящий объект
    public MultiPolygon area; // результирующая область (мульти может быть из-за проверки на принадлежность району)

    // private
    private GeometryFactory GF;
    private ArrayList<Point> point_storage; // здесь храним список точек для обработки overpass вопросов близости
    private ArrayList<Geometry> geometry_storage; // здесь храним список геометрий для обработки overpass вопросов принадлежности области (району)


    public Matching (MatchingType type) {
        matching_type = type;

        answered = false;

        GF = new GeometryFactory();
        point_storage = new ArrayList<>();
        geometry_storage = new ArrayList<>();

        create_areas();
    }



    // делаем выводы после получения ответа
    public void set_answer (Long answer_id) {
        id_of_interest = answer_id;
        // TODO: функционал обработки ответа
        answered = true;
    }



    @Override
    public void exec_overpass() {
        OsmConnection connection = new OsmConnection("https://maps.mail.ru/osm/tools/overpass/api/", "my user agent");
        OverpassMapDataApi overpass = new OverpassMapDataApi(connection);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (matching_type == MatchingType.DISTRICT || matching_type == MatchingType.ADMINISTRATIVE_DISTRICT) {
                    overpass.queryElementsWithGeometry(matching_to_overpass_query(), area_handler);
                } else {
                    overpass.queryElementsWithGeometry(matching_to_overpass_query(), poi_handler);
                }
            }
        });
    }



    @Override
    public void create_areas() {

    }



    @Override
    public void generate_answer(double x, double y) {

    }



    private String matching_to_overpass_query() {
        switch (matching_type) {
            case COMMERCIAL_AIRPORT : return OverpassQueries.COMMERCIAL_AIRPORT;
            case TRAIN_TERMINAL : return OverpassQueries.TRAIN_TERMINAL;
            case PARK : return OverpassQueries.PARK;
            case THEME_PARK : return OverpassQueries.THEME_PARK;
            case ZOO : return OverpassQueries.ZOO;
            case GOLF_FIELD : return OverpassQueries.GOLF_FIELD;
            case MUSEUM : return OverpassQueries.MUSEUM;
            case CINEMA : return OverpassQueries.CINEMA;
            case HOSPITAL : return OverpassQueries.HOSPITAL;
            case LIBRARY : return OverpassQueries.LIBRARY;
            case FOREIGN_CONSULATE : return OverpassQueries.FOREIGN_CONSULATE;
            case DISTRICT : return OverpassQueries.DISTRICT;
            case ADMINISTRATIVE_DISTRICT : return OverpassQueries.ADMINISTRATIVE_DISTRICT;
            default : return "";
        }
    }



    // обрабатывает запрос на области (area) (для районов)
    private final MapDataWithGeometryHandler area_handler = new MapDataWithGeometryHandler() {
        @Override
        public void handle(@NonNull BoundingBox bounds) {

        }

        @Override
        public void handle(@NonNull Node node) {

        }

        @Override
        public void handle(@NonNull Way way, @NonNull BoundingBox bounds, @NonNull List<LatLon> geometry) {

        }

        @Override
        public void handle(@NonNull Relation relation, @NonNull BoundingBox bounds, @NonNull Map<Long, LatLon> nodeGeometries, @NonNull Map<Long, List<LatLon>> wayGeometries) {

        }
    };



    // обрабатывает запрос на точки (point of interest)
    private final MapDataWithGeometryHandler poi_handler = new MapDataWithGeometryHandler() {
        @Override
        public void handle(@NonNull BoundingBox bounds) {}


        @Override
        public void handle(@NonNull Node node) {
            Point p = GF.createPoint(
                    new Coordinate(node.getPosition().getLongitude(), node.getPosition().getLatitude())
            );
            point_storage.add(p);
            variants.putIfAbsent(node.getId(), p);
            names.putIfAbsent(node.getId(), node.getTags().getOrDefault("name", "---"));
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
            Point p = g.getCentroid();
            point_storage.add(p);
            variants.putIfAbsent(way.getId(), p);
            names.putIfAbsent(way.getId(), way.getTags().getOrDefault("name", "---"));
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
            Point p = g.getCentroid();
            point_storage.add(p);
            variants.putIfAbsent(relation.getId(), p);
            names.putIfAbsent(relation.getId(), relation.getTags().getOrDefault("name", "---"));
        }
    };
}
