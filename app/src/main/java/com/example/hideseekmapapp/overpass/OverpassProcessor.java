package com.example.hideseekmapapp.overpass;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import de.westnordost.osmapi.*;
import de.westnordost.osmapi.common.*;
import de.westnordost.osmapi.overpass.*;
import de.westnordost.osmapi.map.data.*;
import de.westnordost.osmapi.map.handler.*;

// TODO: протестировать обработку объектов (на примере точек музеев) (с отображением)
// TODO: протестировать обработку объектов (на примере точек парков) (с отображением)
// TODO: протестировать обработку объектов (получение областей районов и административных округов) (с отображением)

public class OverpassProcessor {
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
        }
    };


    private final MapDataHandler polygon_handler = new MapDataHandler() {
        @Override
        public void handle(BoundingBox bounds) {}

        @Override
        public void handle(Node node) {}

        @Override
        public void handle(Way way) {}

        @Override
        public void handle(Relation relation) {}
    };


    private final Handler<String[]> table_handler = new Handler<String[]>() {
        @Override
        public void handle(String[] tea) {

        }
    };


    public Point[] testOverpass() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                OsmConnection connection = new OsmConnection("https://maps.mail.ru/osm/tools/overpass/api/", "my user agent");
                OverpassMapDataApi overpass = new OverpassMapDataApi(connection);
                try {
                    ElementCount count = overpass.queryCount("[timeout:25][bbox:55.489,37.216,55.989,38.206];\n" +
                            "(\n" +
                            "  node[\"public_transport\"=\"station\"][\"railway\"=\"station\"][\"train\"=\"yes\"];\n" +
                            "  node[\"public_transport\"=\"station\"][\"railway\"=\"halt\"][\"train\"=\"yes\"];\n" +
                            ");\n" +
                            "out count;");
                    overpass.queryElements(OverpassQueries.TRAIN_TERMINAL, mapdata_handler);
                    displayed_text.append(count.total);
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
