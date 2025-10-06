package com.example.hideseekmapapp.overpass;

public class OverpassQueries {
    public static String COMMERCIAL_AIRPORT =
            "[out:xml][timeout:25][bbox:55.489,37.216,55.989,38.206];\n" +
                    "nwr[\"aeroway\"=\"aerodrome\"][\"aerodrome:type\"=\"public\"];\n" +
                    "out meta;";
    public static String TRAIN_TERMINAL =
            "[out:xml][timeout:25][bbox:55.489,37.216,55.989,38.206];\n" +
                    "(\n" +
                    "  node[\"public_transport\"=\"station\"][\"railway\"=\"station\"][\"train\"=\"yes\"];\n" +
                    "  node[\"public_transport\"=\"station\"][\"railway\"=\"halt\"][\"train\"=\"yes\"];\n" +
                    ");\n" +
                    "out meta;";
    public static String PARK =
            "[out:xml][timeout:25][bbox:55.489,37.216,55.989,38.000];\n" +
                    "nwr[\"leisure\"=\"park\"][\"name\"];\n" +
                    "out meta;";
    public static String THEME_PARK =
            "[out:xml][timeout:25][bbox:55.550,37.216,55.989,38.000];\n" +
                    "nwr[\"tourism\"=\"theme_park\"][\"name\"];\n" +
                    "out meta;";
    public static String ZOO =
            "[out:xml][timeout:25][bbox:55.550,37.216,55.989,38.000];\n" +
                    "nwr[\"tourism\"=\"zoo\"];\n" +
                    "out meta;";
    public static String GOLF_FIELD =
            "[out:xml][timeout:25][bbox:55.550,37.216,55.989,38.000];\n" +
                    "nwr[\"leisure\"=\"golf_course\"];\n" +
                    "out meta;";
    public static String MUSEUM =
            "[out:xml][timeout:25][bbox:55.489,37.216,55.989,38.206];\n" +
                    "nwr[\"tourism\"=\"museum\"] -> .a;\n" +
                    "> ->.b;\n" +
                    "(\n" +
                    "  .a;\n" +
                    "  nwr.b[\"tourism\"=\"museum\"];\n" +
                    ") -> ._;\n" +
                    "out meta;";
    public static String CINEMA =
            "[out:xml][timeout:25][bbox:55.550,37.216,55.989,38.000];\n" +
                    "nwr[\"amenity\"=\"cinema\"];\n" +
                    "out meta;";
    public static String HOSPITAL =
            "[out:xml][timeout:25][bbox:55.550,37.216,55.989,38.000];\n" +
                    "nwr[\"amenity\"=\"hospital\"];\n" +
                    "out meta;";
    public static String LIBRARY =
            "[out:xml][timeout:25][bbox:55.550,37.216,55.989,38.000];\n" +
                    "nwr[\"amenity\"=\"library\"];\n" +
                    "out meta;";
    public static String FOREIGN_CONSULATE =
            "[out:xml][timeout:25][bbox:55.550,37.216,55.989,38.000];\n" +
                    "nwr[\"diplomatic\"=\"embassy\"];\n" +
                    "out meta;";
    public static String DISTRICT =
            "[out:xml][timeout:25][bbox:55.550,37.216,55.989,38.000];\n" +
                    "relation[\"type\"=\"boundary\"][\"boundary\"=\"administrative\"][\"admin_level\"=\"8\"];\n" +
                    "out meta;";
    public static String ADMINISTRATIVE_DISTRICT =
            "[out:xml][timeout:25][bbox:55.550,37.216,55.989,38.000];\n" +
                    "relation[\"type\"=\"boundary\"][\"boundary\"=\"administrative\"][\"admin_level\"=\"5\"];\n" +
                    "out meta;";
}
