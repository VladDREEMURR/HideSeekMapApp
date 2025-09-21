package com.example.hideseekmapapp.overpass;

import de.westnordost.osmapi.*;
import de.westnordost.osmapi.common.*;
import de.westnordost.osmapi.overpass.*;
import de.westnordost.osmapi.map.data.*;
import de.westnordost.osmapi.map.handler.*;


public class OverpassProcessor {
    /*
    Что включить:
        1) Вытягивание запроса из вопроса
        2) Отправка запроса и получение ответа
        3) (здесь ли?) Форматирование в нормальном формате
     */

    private static final String TAG = OverpassProcessor.class.getSimpleName();


    private static final MapDataHandler mapdata_handler = new MapDataHandler() {
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


    private static final MapDataHandler polygon_handler = new MapDataHandler() {
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


    Handler<String[]> table_handler = new Handler<String[]>() {
        @Override
        public void handle(String[] tea) {

        }
    };


    public void testOverpass() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                OsmConnection connection = new OsmConnection("https://maps.mail.ru/osm/tools/overpass/api/", "my user agent");
                OverpassMapDataApi overpass = new OverpassMapDataApi(connection);
                ElementCount count = overpass.queryCount(
                        "{{geocodeArea:Vienna}}->.searchArea; nwr[shop](area.searchArea); out count;"
                );
                overpass.queryElements("[out:json][timeout:25];\n" +
                        "{{geocodeArea:Moscow}}->.searchArea;\n" +
                        "(\n" +
                        "  node[\"tourism\"=\"museum\"](area.searchArea);\n" +
                        "  way[\"tourism\"=\"museum\"](area.searchArea);\n" +
                        "  relation[\"tourism\"=\"museum\"](area.searchArea);\n" +
                        ");\n" +
                        "out body;\n" +
                        ">;\n" +
                        "out skel qt;", mapdata_handler);
//                overpass.queryTable("ww", table_handler);
            }
        });
        thread.start();
    }
}
