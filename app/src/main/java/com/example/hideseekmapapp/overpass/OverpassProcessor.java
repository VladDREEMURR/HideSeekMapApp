package com.example.hideseekmapapp.overpass;

import java.io.PrintWriter;
import java.io.StringWriter;

import de.westnordost.osmapi.*;
import de.westnordost.osmapi.common.*;
import de.westnordost.osmapi.overpass.*;
import de.westnordost.osmapi.map.data.*;
import de.westnordost.osmapi.map.handler.*;

import com.menecats.polybool.helpers.PolyBoolHelper;
import com.menecats.polybool.models.geojson.Geometry;

// TODO: протестировать обработку объектов (на примере точек музеев) (с отображением)
// TODO: протестировать обработку объектов (на примере точек парков) (с отображением)
// TODO: протестировать обработку объектов (получение областей районов и административных округов) (с отображением)

public class OverpassProcessor {
    private StringBuilder displayed_text = new StringBuilder();



    private final MapDataHandler mapdata_handler = new MapDataHandler() {
        @Override
        public void handle(BoundingBox bounds) {
        }

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


    public String testOverpass() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                OsmConnection connection = new OsmConnection("https://maps.mail.ru/osm/tools/overpass/api/", "my user agent");
                OverpassMapDataApi overpass = new OverpassMapDataApi(connection);
                try {
                    /*
                    ElementCount count = overpass.queryCount(
                            "[bbox:55.489,37.216,55.989,38.206];\n" +
                                    "nwr[shop];\n" +
                                    "out count;"
                    );
                    */
                    overpass.queryElements("[bbox:55.489,37.216,55.989,38.206];\n" +
                            "nwr[aeroway=aerodrome];\n" +
                            "out geom;", mapdata_handler);
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
        /*
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
*/
        com.menecats.polybool.models.Polygon p1 = PolyBoolHelper.polygon(
                PolyBoolHelper.region(
                        PolyBoolHelper.point(0,0),
                        PolyBoolHelper.point(0,5),
                        PolyBoolHelper.point(5,5),
                        PolyBoolHelper.point(5,0),
                        PolyBoolHelper.point(0,0)
                )
        );
        com.menecats.polybool.models.Polygon p2 = PolyBoolHelper.polygon(
                PolyBoolHelper.region(
                        PolyBoolHelper.point(2,2),
                        PolyBoolHelper.point(2,4),
                        PolyBoolHelper.point(4,4),
                        PolyBoolHelper.point(4,2),
                        PolyBoolHelper.point(2,2)
                )
        );
        com.menecats.polybool.models.Polygon p3 = PolyBoolHelper.polygon(
                PolyBoolHelper.region(
                        PolyBoolHelper.point(6,1),
                        PolyBoolHelper.point(6,3),
                        PolyBoolHelper.point(8,3),
                        PolyBoolHelper.point(8,1),
                        PolyBoolHelper.point(6,1)
                )
        );
        com.menecats.polybool.models.Polygon res = com.menecats.polybool.PolyBool.difference(
                PolyBoolHelper.epsilon(),
                p1,
                p2
        );
        res = com.menecats.polybool.PolyBool.union(
                PolyBoolHelper.epsilon(),
                res,
                p3
        );
        return displayed_text.toString();
    }
}
