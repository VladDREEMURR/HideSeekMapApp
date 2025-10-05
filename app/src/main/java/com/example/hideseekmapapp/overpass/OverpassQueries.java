package com.example.hideseekmapapp.overpass;

public class OverpassQueries {
    public static String COMMERCIAL_AIRPORT =
            "[bbox:55.489,37.216,55.989,38.206];\n" +
                    "nwr[\"aeroway\"=\"aerodrome\"][\"aerodrome:type\"=\"public\"];\n" +
                    "out body;";
    public static String TRAIN_TERMINAL =
            "[out:json][timeout:25][bbox:55.489,37.216,55.989,38.206];\n" +
                    "(\n" +
                    "  node[\"public_transport\"=\"station\"][\"railway\"=\"station\"][\"train\"=\"yes\"];\n" +
                    "  node[\"public_transport\"=\"station\"][\"railway\"=\"halt\"][\"train\"=\"yes\"];\n" +
                    ");\n" +
                    "out body;";
    public static String PARK =
            "[out:json][timeout:25][bbox:55.489,37.216,55.989,38.000];\n" +
                    "nwr[\"leisure\"=\"park\"][\"name\"];\n" +
                    "out body;";
    public static String THEME_PARK =
            "[out:json][timeout:25][bbox:55.550,37.216,55.989,38.000];\n" +
                    "nwr[\"tourism\"=\"theme_park\"][\"name\"];\n" +
                    "out body;";
    public static String ZOO =
            "[out:json][timeout:25][bbox:55.550,37.216,55.989,38.000];\n" +
                    "nwr[\"tourism\"=\"zoo\"];\n" +
                    "out body;";
    public static String GOLF_FIELD =
            "[out:json][timeout:25][bbox:55.550,37.216,55.989,38.000];\n" +
                    "nwr[\"leisure\"=\"golf_course\"];\n" +
                    "out body;";
    public static String MUSEUM =
            "[out:json][timeout:25][bbox:55.489,37.216,55.989,38.206];\n" +
                    "nwr[\"tourism\"=\"museum\"] -> .a;\n" +
                    "> ->.b;\n" +
                    "(\n" +
                    "  .a;\n" +
                    "  nwr.b[\"tourism\"=\"museum\"];\n" +
                    ") -> ._;\n" +
                    "out body;";
    public static String CINEMA =
            "[out:json][timeout:25][bbox:55.550,37.216,55.989,38.000];\n" +
                    "nwr[\"amenity\"=\"cinema\"];\n" +
                    "out body;";
    public static String HOSPITAL =
            "[out:json][timeout:25][bbox:55.550,37.216,55.989,38.000];\n" +
                    "nwr[\"amenity\"=\"hospital\"];\n" +
                    "out body;";
    public static String LIBRARY =
            "[out:json][timeout:25][bbox:55.550,37.216,55.989,38.000];\n" +
                    "nwr[\"amenity\"=\"library\"];\n" +
                    "out body;";
    public static String FOREIGN_CONSULATE =
            "[out:json][timeout:25][bbox:55.550,37.216,55.989,38.000];\n" +
                    "nwr[\"diplomatic\"=\"embassy\"];\n" +
                    "out body;";
    public static String DISTRICT =
            "[out:json][timeout:25][bbox:55.550,37.216,55.989,38.000];\n" +
                    "relation[\"type\"=\"boundary\"][\"boundary\"=\"administrative\"][\"admin_level\"=\"8\"];\n" +
                    "out body;";
    public static String ADMINISTRATIVE_DISTRICT =
            "[out:json][timeout:25][bbox:55.550,37.216,55.989,38.000];\n" +
                    "relation[\"type\"=\"boundary\"][\"boundary\"=\"administrative\"][\"admin_level\"=\"5\"];\n" +
                    "out body;";
    public static String MCD_STATION =
            "[out:json][timeout:25][bbox:55.500,37.216,55.989,38.000];\n" +
                    "(\n" +
                    "  relation[\"ref\"=\"D1\"][\"type\"=\"route\"]; >>;\n" +
                    "  relation[\"ref\"=\"D2\"][\"type\"=\"route\"]; >>;\n" +
                    "  relation[\"ref\"=\"D3\"][\"type\"=\"route\"]; >>;\n" +
                    "  relation[\"ref\"=\"D4\"][\"type\"=\"route\"]; >>;\n" +
                    ") -> .routes;\n" +
                    "node.routes[\"railway\"=\"stop\"];\n" +
                    "out body;";
    public static String SUBWAY_STATION =
            "";
}
