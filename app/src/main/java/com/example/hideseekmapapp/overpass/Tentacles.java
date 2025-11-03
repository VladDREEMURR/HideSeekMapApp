package com.example.hideseekmapapp.overpass;

import androidx.annotation.NonNull;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
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

public class Tentacles implements Question {
    public final QuestionType type = QuestionType.TENTACLES;

    // статусы вопроса
    public boolean answered = false;

    // входные данные
    public String overpass_query = ""; // какой именно вопрос тентаклей мы задаём
    public double radius = 0.0; // радиус в километрах
    public double lon = 0.0; // центральная точка (x)
    public double lat = 0.0; // центральная точка (y)

    // результаты
    public HashMap<Long, String> names = new HashMap<>(); // названия вариантов ответа
    public HashMap<Long, MultiPolygon> variants = new HashMap<>(); // варианты ответа
    public Long id_of_interest = 0L; // ключ к Map, подходящий объект
    public MultiPolygon area = null; // результирующая область (мульти может быть из-за проверки на принадлежность району)
    public String area_name = ""; // название варианта
    public Polygon circle = null; // область тентаклей
    public boolean is_inside = false; // находится ли вообще внутри круга

    // private
    private GeometryFactory GF = new GeometryFactory();
    public HashMap<Long, Point> point_storage = new HashMap<>();





    public Tentacles (String overpass_query, double x, double y, double radius) {
        this.overpass_query = overpass_query;
        this.lon = x;
        this.lat = y;
        this.radius = radius;
    }





    public void set_answer (Long answer_id, boolean is_inside) {
        this.is_inside = is_inside;
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
                    overpass.queryElementsWithGeometry(overpass_query, poi_handler);
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
        circle = create_circle();
        clear_unnecessary_points();
    }





    private void clear_unnecessary_points() {
        HashMap<Long, Point> new_point_storage = new HashMap<>();
        com.mapbox.geojson.Point center = com.mapbox.geojson.Point.fromLngLat(lon, lat);
        for (Long ID : point_storage.keySet()) {
            com.mapbox.geojson.Point pnt = com.mapbox.geojson.Point.fromLngLat(point_storage.get(ID).getX(), point_storage.get(ID).getY());
            if (com.mapbox.turf.TurfMeasurement.distance(center, pnt, com.mapbox.turf.TurfConstants.UNIT_KILOMETERS) <= radius) {
                new_point_storage.putIfAbsent(ID, point_storage.get(ID));
            }
        }
        point_storage = new_point_storage;
    }





    private Polygon create_circle () {
        // сделали свой круг
        com.mapbox.geojson.Point cp = com.mapbox.geojson.Point.fromLngLat(lon, lat);
        com.mapbox.geojson.Polygon circle = com.mapbox.turf.TurfTransformation.circle(cp, radius, 180, com.mapbox.turf.TurfConstants.UNIT_KILOMETERS);
        // теперь переделываем его в свой формат
        List<com.mapbox.geojson.Point> points = circle.coordinates().get(0);
        ArrayList<Coordinate> coord_list = new ArrayList<Coordinate>();
        for (com.mapbox.geojson.Point p : points) {
            coord_list.add(new Coordinate(p.longitude(), p.latitude()));
        }
        Coordinate[] coord_array = new Coordinate[coord_list.size()];
        coord_list.toArray(coord_array);
        return GF.createPolygon(coord_array);
    }





    @Override
    public void create_areas() {
        // создаём полигоны
        Point[] pts = point_storage.values().toArray(new Point[point_storage.values().size()]);
        Envelope bbox = circle.getEnvelopeInternal();
        bbox.expandBy(0.02);
        MapVoronoiCreator mvc = new MapVoronoiCreator(bbox, pts);
        Polygon[] polygons = mvc.polygons;

        // в нормальном случае одной точке соответствует одна область
        // заполняем словарь мультиполигонов, проверяя точки на вхождение
        for (int p = 0; p < polygons.length; p++) {
            for (Long ID : point_storage.keySet()) {
                Point dot = point_storage.get(ID);
                // обрезать полигон по окружности (если надо обрезать)
                Polygon pol = polygons[p];
                if (!circle.covers(pol)) {
                    PolygonBool PB = new PolygonBool(GF.createMultiPolygon(new Polygon[]{pol}), new Polygon[]{circle}, PolygonBoolOperationType.INTERSECTION);
                    pol = PB.polygons[0];
                }
                // положить полигон в словарь
                if (polygons[p].covers(dot)) {
                    variants.putIfAbsent(ID, new MultiPolygon(new Polygon[]{pol}, GF));
                }
            }
        }
    }





    @Override
    public void generate_answer(double x, double y) {
        Point p = GF.createPoint(new Coordinate(x, y));
        for (Long ID : variants.keySet()) {
            if (variants.get(ID).covers(p)) {
                set_answer(ID, true);
                break;
            }
        }
    }





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
