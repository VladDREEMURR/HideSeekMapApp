package com.example.hideseekmapapp.overpass;

import androidx.annotation.NonNull;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.polygonize.Polygonizer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
    public boolean answered = false;

    // входные данные
    public String overpass_query = ""; // какой именно вопрос совпадения мы задаём
    public Long object_id = 0L; // объект, который мы ищем

    // результаты
    public HashMap<Long, String> names = new HashMap<>(); // названия вариантов ответа
    public HashMap<Long, MultiPolygon> variants = new HashMap<>(); // варианты ответа
    public Long id_of_interest = 0L; // ключ к Map, подходящий объект
    public MultiPolygon area = null; // результирующая область (мульти может быть из-за проверки на принадлежность району)
    public String area_name = ""; // название варианта

    // private
    private GeometryFactory GF = new GeometryFactory();
    private HashMap<Long, Point> point_storage = new HashMap<>();

    // test
    public Polygon[] polygons;


    public Matching (String overpass_query) {
        this.overpass_query = overpass_query;

        exec_overpass();
        create_areas();
    }



    // делаем выводы после получения ответа
    public void set_answer (Long answer_id) {
        id_of_interest = answer_id;
        area = variants.get(id_of_interest);
        area_name = names.get(id_of_interest);
        answered = true;
    }



    @Override
    public void exec_overpass() {
        OsmConnection connection = new OsmConnection("https://maps.mail.ru/osm/tools/overpass/api/", "my user agent");
        OverpassMapDataApi overpass = new OverpassMapDataApi(connection);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (get_answer_set_type() == AnswerSetType.INSIDE_AREA) {
                        overpass.queryElementsWithGeometry(overpass_query, area_handler);
                    } else {
                        overpass.queryElementsWithGeometry(overpass_query, poi_handler);
                    }
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String s = sw.toString();
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String s = sw.toString();
        }
    }



    @Override
    public void create_areas() {
        if (get_answer_set_type() == AnswerSetType.VORONOI) {
//            Polygon[] polygons;
            Point[] pts = point_storage.values().toArray(new Point[point_storage.values().size()]);
            Envelope bbox = GF.createMultiPoint(pts).getEnvelopeInternal();
            MapVoronoiCreator mvc = new MapVoronoiCreator(bbox, pts);
            polygons = mvc.polygons;


            try {
                /*
                int i = 0;
                for (Long p_id : point_storage.keySet()) {
                    variants.putIfAbsent(p_id, new MultiPolygon(new Polygon[]{polygons[i]}, GF));
                    i = i + 1;
                }
                */
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String s = sw.toString();
            }
        }
    }



    @Override
    public void generate_answer(double x, double y) {

    }



    private AnswerSetType get_answer_set_type() {
        if (overpass_query.equals(OverpassQueries.ADMINISTRATIVE_DISTRICT) || overpass_query.equals(OverpassQueries.DISTRICT)) {
            return AnswerSetType.INSIDE_AREA;
        } else {
            return AnswerSetType.VORONOI;
        }
    }



    // обрабатывает запрос на области (area) (для районов)
    private final MapDataWithGeometryHandler area_handler = new MapDataWithGeometryHandler() {
        @Override
        public void handle(@NonNull BoundingBox bounds) {}
        @Override
        public void handle(@NonNull Node node) {}
        @Override
        public void handle(@NonNull Way way, @NonNull BoundingBox bounds, @NonNull List<LatLon> geometry) {}

        @Override
        public void handle(@NonNull Relation relation, @NonNull BoundingBox bounds, @NonNull Map<Long, LatLon> nodeGeometries, @NonNull Map<Long, List<LatLon>> wayGeometries) {
            ArrayList<Point> pts = new ArrayList<>();

            for (List<LatLon> way : wayGeometries.values()) {
                for (LatLon ll : way) {
                    pts.add(GF.createPoint(new Coordinate(
                            ll.getLongitude(), ll.getLatitude()
                    )));
                }
            }

            Point[] point_arr = new Point[pts.size()];
            Geometry g = GF.createMultiPoint(point_arr);
            Polygonizer polygonizer = new Polygonizer();
            polygonizer.add(g);
            Collection coll = polygonizer.getPolygons();
            MultiPolygon mp = new MultiPolygon(GF.toPolygonArray(coll), GF);

            variants.putIfAbsent(relation.getId(), mp);
            names.putIfAbsent(relation.getId(), relation.getTags().getOrDefault("name", "---"));
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
            point_storage.putIfAbsent(node.getId(), p);
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
            point_storage.putIfAbsent(way.getId(), p);
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
            point_storage.putIfAbsent(relation.getId(), p);
            names.putIfAbsent(relation.getId(), relation.getTags().getOrDefault("name", "---"));
        }
    };
}
