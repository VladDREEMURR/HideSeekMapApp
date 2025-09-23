package com.example.hideseekmapapp.overpass;

import java.io.PrintWriter;
import java.io.StringWriter;

import de.westnordost.osmapi.*;
import de.westnordost.osmapi.common.*;
import de.westnordost.osmapi.overpass.*;
import de.westnordost.osmapi.map.data.*;
import de.westnordost.osmapi.map.handler.*;

// TODO: протестировать обработку объектов (на примере точек музеев) (с отображением)
// TODO: протестировать обработку объектов (на примере точек парков) (с отображением)
// TODO: протестировать обработку объектов (получение областей районов и административных округов) (с отображением)
// TODO: протестировать обработку объектов (булевы операции с областями) (с отображением)

public class OverpassProcessor {
    /*
    Что включить:
        1) Вытягивание запроса из вопроса
        2) Отправка запроса и получение ответа
        3) (здесь ли?) Форматирование в нормальном формате
     */

    private static final String TAG = OverpassProcessor.class.getSimpleName();

    private int boundings = 0;
    private int nodes = 0;
    private int ways = 0;
    private int relations = 0;


    private final MapDataHandler mapdata_handler = new MapDataHandler() {
        @Override
        public void handle(BoundingBox bounds) {
            boundings++;
        }

        @Override
        public void handle(Node node) {
            nodes++;
        }

        @Override
        public void handle(Way way) {
            ways++;
        }

        @Override
        public void handle(Relation relation) {
            relations++;
        }
    };


    private final MapDataHandler polygon_handler = new MapDataHandler() {
        @Override
        public void handle(BoundingBox bounds) {}

        @Override
        public void handle(Node node) {

        }

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
//        OsmConnection connection = new OsmConnection("https://maps.mail.ru/osm/tools/overpass/api/", "my user agent");
//        OverpassMapDataApi overpass = new OverpassMapDataApi(connection);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                OsmConnection connection = new OsmConnection("https://maps.mail.ru/osm/tools/overpass/api/", "my user agent");
                OverpassMapDataApi overpass = new OverpassMapDataApi(connection);
                try {
                    ElementCount count = overpass.queryCount(
                            "{{geocodeArea:Vienna}}->.searchArea;\n" +
                                    "nwr[shop](area.searchArea);\n" +
                                    "out count;"
                    );

                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String s = sw.toString();
                }
            }
        });
        thread.start();
        /*try {
            thread.join();
        } catch (InterruptedException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String s = sw.toString();
        }*/
        StringBuilder sb = new StringBuilder();
        sb.append(boundings);
        sb.append('\n');
        sb.append(nodes);
        sb.append('\n');
        sb.append(ways);
        sb.append('\n');
        sb.append(relations);
        return sb.toString();
    }
}
