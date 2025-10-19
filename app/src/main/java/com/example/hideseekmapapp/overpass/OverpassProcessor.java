package com.example.hideseekmapapp.overpass;

import androidx.annotation.NonNull;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.*;
import de.westnordost.osmapi.overpass.*;
import de.westnordost.osmapi.map.data.*;
import de.westnordost.osmapi.map.handler.*;

// TODO: протестировать обработку объектов (на примере точек музеев) (с отображением)
// TODO: протестировать обработку объектов (на примере точек парков) (с отображением)
// TODO: протестировать обработку объектов (получение областей районов и административных округов) (с отображением)

public class OverpassProcessor {
    // соединение с overpass
    public static OsmConnection connection;
    public static OverpassMapDataApi overpass;

    // временное хранилище точек
    private ArrayList<Point> point_storage;

    // для манипуляций геометрией
    private GeometryFactory GF;

    private StringBuilder displayed_text = new StringBuilder();





    public OverpassProcessor() {
        connection = new OsmConnection("https://maps.mail.ru/osm/tools/overpass/api/", "my user agent");
        overpass = new OverpassMapDataApi(connection);
        point_storage = new ArrayList<>();
        GF = new GeometryFactory();
    }




    public Point[] testOverpass(String query) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    overpass.queryElementsWithGeometry(query, geom_handler);
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String s = sw.toString();
                    displayed_text.append(s);
                    displayed_text.append('\n');
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
            displayed_text.append(s);
            displayed_text.append('\n');
        }

        Point[] pts = new Point[point_storage.size()];
        point_storage.toArray(pts);
        return pts;
    }




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
