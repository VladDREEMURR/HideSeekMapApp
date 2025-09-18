package com.example.hideseekmapapp.overpass;

import android.util.Log;

import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.overpass.ElementCount;
import de.westnordost.osmapi.overpass.OverpassMapDataApi;


public class OverpassProcessor {
    /*
    Что включить:
        1) Вытягивание запроса из вопроса
        2) Отправка запроса и получение ответа
        3) (здесь ли?) Форматирование в нормальном формате
     */

    private static final String TAG = OverpassProcessor.class.getSimpleName();


    public void testOverpass() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                OsmConnection connection = new OsmConnection("https://maps.mail.ru/osm/tools/overpass/api/", "my user agent");
                OverpassMapDataApi overpass = new OverpassMapDataApi(connection);
                ElementCount count = overpass.queryCount(
                        "{{geocodeArea:Vienna}}->.searchArea; nwr[shop](area.searchArea); out count;"
                );
                Log.d(TAG, Long.toString(count.total));
            }
        });
        thread.start();
    }
}
