package com.example.hideseekmapapp.overpass;

import androidx.annotation.NonNull;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.*;
import de.westnordost.osmapi.common.*;
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



    private StringBuilder displayed_text = new StringBuilder();
    private ArrayList<Point> points = new ArrayList<>();
    private GeometryFactory GF = new GeometryFactory();



    private final MapDataHandler mapdata_handler = new MapDataHandler() {
        @Override
        public void handle(BoundingBox bounds) {
        }

        @Override
        public void handle(Node node) {
            Point p = GF.createPoint(
                    new Coordinate(node.getPosition().getLongitude(), node.getPosition().getLatitude())
            );
            points.add(p);
        }

        @Override
        public void handle(Way way) {
        }

        @Override
        public void handle(Relation relation) {
            point_storage = new ArrayList<>();
            List<RelationMember> rel_members = relation.getMembers();
            for (RelationMember rm : rel_members) {
//                overpass.queryElements();
                overpass.queryElementsWithGeometry("", geom_handler);
            }
        }
    };


    // TODO: новый способ обработать геометрию, РАЗБЕРИСЬ
    private final MapDataWithGeometryHandler geom_handler = new MapDataWithGeometryHandler() {
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


    private final MapDataHandler way_rel_handler = new MapDataHandler() {
        @Override
        public void handle(BoundingBox bounds) {}

        @Override
        public void handle(Node node) {}

        @Override
        public void handle(Way way) {}

        @Override
        public void handle(Relation relation) {}
    };


    public Point[] testOverpass() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                connection = new OsmConnection("https://maps.mail.ru/osm/tools/overpass/api/", "my user agent");
                overpass = new OverpassMapDataApi(connection);
                try {
                    overpass.queryElements(OverpassQueries.COMMERCIAL_AIRPORT, mapdata_handler);
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

        Point[] pts = new Point[points.size()];
        points.toArray(pts);
        return pts;
    }
}
