package com.example.hideseekmapapp.overpass;

public class ObjectTypeTranslator {
    public static final String[] object_type_names = {
            "COMMERCIAL_AIRPORT",
            "TRAIN_TERMINAL",
            "PARK",
            "THEME_PARK",
            "ZOO",
            "GOLF_FIELD",
            "MUSEUM",
            "CINEMA",
            "HOSPITAL",
            "LIBRARY",
            "FOREIGN_CONSULATE",
            "DISTRICT",
            "ADMINISTRATIVE_DISTRICT"
    };


    public static String str_to_query (String object_type) {
        switch (object_type) {
            case "COMMERCIAL_AIRPORT": return OverpassQueries.COMMERCIAL_AIRPORT;
            case "TRAIN_TERMINAL": return OverpassQueries.TRAIN_TERMINAL;
            case "PARK": return OverpassQueries.PARK;
            case "THEME_PARK": return OverpassQueries.THEME_PARK;
            case "ZOO": return OverpassQueries.ZOO;
            case "GOLF_FIELD": return OverpassQueries.GOLF_FIELD;
            case "MUSEUM": return OverpassQueries.MUSEUM;
            case "CINEMA": return OverpassQueries.CINEMA;
            case "HOSPITAL": return OverpassQueries.HOSPITAL;
            case "LIBRARY": return OverpassQueries.LIBRARY;
            case "FOREIGN_CONSULATE": return OverpassQueries.FOREIGN_CONSULATE;
            case "DISTRICT": return OverpassQueries.DISTRICT;
            case "ADMINISTRATIVE_DISTRICT": return OverpassQueries.ADMINISTRATIVE_DISTRICT;
            default: return "";
        }
    }


    public static String str_to_russian (String object_type) {
        switch (object_type) {
            case "COMMERCIAL_AIRPORT": return "Коммерческий аэропорт";
            case "TRAIN_TERMINAL": return "ЖД остановка";
            case "PARK": return "Парк";
            case "THEME_PARK": return "Тематический парк";
            case "ZOO": return "Зоопарк";
            case "GOLF_FIELD": return "Поле для гольфа";
            case "MUSEUM": return "Музей";
            case "CINEMA": return "Кинотеатр";
            case "HOSPITAL": return "Медицинское учреждение";
            case "LIBRARY": return "Библиотека";
            case "FOREIGN_CONSULATE": return "Иностранное посольство";
            case "DISTRICT": return "Район";
            case "ADMINISTRATIVE_DISTRICT": return "Административный округ";
            default: return "---";
        }
    }


    public static String query_to_str (String query) {
        for (String s : object_type_names) {
            if (query == str_to_query(s)) {
                return s;
            }
        }
        return "COMMERCIAL_AIRPORT";
    }


    public static String russian_to_str (String russ) {
        for (String s : object_type_names) {
            if (russ == str_to_russian(s)) {
                return s;
            }
        }
        return "COMMERCIAL_AIRPORT";
    }
}
