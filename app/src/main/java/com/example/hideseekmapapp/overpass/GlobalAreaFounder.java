package com.example.hideseekmapapp.overpass;

import androidx.annotation.NonNull;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.operation.polygonize.Polygonizer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.osmapi.overpass.MapDataWithGeometryHandler;
import de.westnordost.osmapi.overpass.OverpassMapDataApi;

public class GlobalAreaFounder {
    // результат
    public MultiPolygon area;

    // private
    private GeometryFactory GF = new GeometryFactory();



    public GlobalAreaFounder(String overpass_query) {
        OsmConnection connection = new OsmConnection("https://maps.mail.ru/osm/tools/overpass/api/", "my user agent");
        OverpassMapDataApi overpass = new OverpassMapDataApi(connection);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    overpass.queryElementsWithGeometry(overpass_query, area_handler);
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String s = sw.toString();
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
        }
    }





    // обрабатывает запрос на области (area) (для районов)
    private final MapDataWithGeometryHandler area_handler = new MapDataWithGeometryHandler() {
        @Override
        public void handle(@NonNull BoundingBox bounds) {}
        @Override
        public void handle(@NonNull Node node) {}
        @Override
        public void handle(@NonNull Way way, @NonNull BoundingBox bounds, @NonNull List<LatLon> geometry) {}

        @Override
        public void handle(@NonNull Relation relation, @NonNull BoundingBox bounds, @NonNull Map<Long, LatLon> nodeGeometries, @NonNull Map<Long, List<LatLon>> wayGeometries) {
            LineString[] lines = new LineString[wayGeometries.size()];

            int i = 0;
            for (List<LatLon> way : wayGeometries.values()) {
                int j = 0;
                Coordinate[] coords = new Coordinate[way.size()];
                for (LatLon ll : way) {
                    coords[j] = new Coordinate(ll.getLongitude(), ll.getLatitude());
                    j += 1;
                }
                lines[i] = GF.createLineString(coords);
                i += 1;
            }

            Polygonizer polygonizer = new Polygonizer();
            polygonizer.add(Arrays.asList(lines));
            Collection coll = polygonizer.getPolygons();
            area = new MultiPolygon(GF.toPolygonArray(coll), GF);
        }
    };
}
