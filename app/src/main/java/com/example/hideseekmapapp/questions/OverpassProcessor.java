package com.example.hideseekmapapp.questions;

import hu.supercluster.overpasser.adapter.OverpassQueryResult;
import hu.supercluster.overpasser.adapter.OverpassServiceProvider;

import android.util.Log;


public class OverpassProcessor {
    /*
    Что включить:
        1) Вытягивание запроса из вопроса
        2) Отправка запроса и получение ответа
        3) (здесь ли?) Форматирование в нормальном формате
     */

    private static final String TAG = OverpassProcessor.class.getSimpleName();

    public void testOverpass() {
        String test_query = "[out:json][timeout:25];\n" +
                "{{geocodeArea:Moscow}}->.searchArea;\n" +
                "nwr[\"waterway\"=\"river\"][\"wikidata\"=\"Q175117\"](area.searchArea);\n" +
                "out geom;";
        OverpassQueryResult res = null;
        try {
            res = OverpassServiceProvider.get().interpreter(test_query).execute().body();
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }
        if (res != null) {
            Log.d(TAG, res.toString());
        }
    }
}


class Results {

}
