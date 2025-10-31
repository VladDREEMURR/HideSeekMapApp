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
import java.util.Arrays;
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

public class Measuring implements Question {
    public final QuestionType type = QuestionType.MEASURING;

    // входные параметры
    public String overpass_query = "";
    public double lon = 0.0; // точка отсчёта дистанции (x)
    public double lat = 0.0; // точка отсчёта дистанции (y)
    public Envelope bbox = null; // квадрат поиска (необязателен, нужен только при огромных количествах объектов)

    // состояния вопроса
    public boolean answered = false;

    // результаты
    public double rad = 0.0;
    public boolean is_closer = false;
    public MultiPolygon area = null;
    public Point[] target_points = new Point[0];
    public Point comparator_point = null;

    // private
    private GeometryFactory GF = new GeometryFactory();
    private ArrayList<Point> point_storage = new ArrayList<>();





    public Measuring (@NonNull String overpass_query, double lon, double lat, Envelope bbox) {
        this.overpass_query = overpass_query;
        this.lon = lon;
        this.lat = lat;
        this.bbox = bbox;
        this.comparator_point = GF.createPoint(new Coordinate(lon, lat));
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

        // положить точки в массив
        target_points = new Point[point_storage.size()];
        point_storage.toArray(target_points);

        // найти минимальную дистанцию между целями и местоположением искателей
        Point comparator_point = GF.createPoint(new Coordinate(lon, lat));
        rad = count_min_distance(comparator_point);

        // сократить количества точек (если их слишком много и есть Envelope)
        if (bbox != null && target_points.length > 300) {
            point_storage = new ArrayList<>();
            for (int i = 0; i < target_points.length; i++) {
                if (bbox.covers(target_points[i].getCoordinate())) {
                    point_storage.add(target_points[i]);
                }
            }
        }
        target_points = point_storage.toArray(new Point[point_storage.size()]);
    }





    private double count_min_distance (Point comparator_point) {
        com.mapbox.geojson.Point CP = com.mapbox.geojson.Point.fromLngLat(comparator_point.getX(), comparator_point.getY());
        com.mapbox.geojson.Point[] target_pts = new com.mapbox.geojson.Point[target_points.length];
        for (int p = 0; p < target_points.length; p++) {
            target_pts[p] = com.mapbox.geojson.Point.fromLngLat(target_points[p].getX(), target_points[p].getY());
        }

        // считаем минимальное расстояние
        double min_distance = com.mapbox.turf.TurfMeasurement.distance(CP, target_pts[0], com.mapbox.turf.TurfConstants.UNIT_KILOMETERS);
        double temp;
        for (int p = 1; p < target_pts.length; p++) {
            temp = com.mapbox.turf.TurfMeasurement.distance(CP, target_pts[p], com.mapbox.turf.TurfConstants.UNIT_KILOMETERS);
            if (temp < min_distance) {
                min_distance = temp;
            }
        }
        return min_distance;
    }





    private Polygon create_circle (Point center) {
        int steps = (int)(-17.0 / (1.0 + Math.max(rad, 0.05)) * Math.tanh(0.05 * (point_storage.size() - 350.0)) + 30.0);
        // сделали свой круг
        com.mapbox.geojson.Point cp = com.mapbox.geojson.Point.fromLngLat(center.getX(), center.getY());
        com.mapbox.geojson.Polygon circle = com.mapbox.turf.TurfTransformation.circle(cp, rad, steps, com.mapbox.turf.TurfConstants.UNIT_KILOMETERS);
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
        // создать круги вокруг каждой точки
        Polygon[] polygons = new Polygon[target_points.length];
        for (int p = 0; p < target_points.length; p++) {
            polygons[p] = create_circle(target_points[p]);
        }

        // объединяем круги
        PolygonBool PB = new PolygonBool(GF.createMultiPolygon(new Polygon[]{polygons[0]}), Arrays.copyOfRange(polygons, 1, polygons.length), PolygonBoolOperationType.UNION);
        area = GF.createMultiPolygon(PB.polygons);
    }





    public void set_answer (boolean is_closer) {
        this.is_closer = is_closer;
        answered = true;
    }





    @Override
    public void generate_answer(double x, double y) {
        Point p = GF.createPoint(new Coordinate(x, y));
        set_answer(area.covers(p));
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
            Point p = g.getCentroid();
            point_storage.add(p);
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
        }
    };
}
