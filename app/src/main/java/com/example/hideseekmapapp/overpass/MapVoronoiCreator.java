package com.example.hideseekmapapp.overpass;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MapVoronoiCreator {
    // входные параметры
    public Envelope bounding_box;
    public Point[] points;

    // выходные параметры
    public Polygon[] polygons;


    public MapVoronoiCreator (Envelope bounding_box, Point[] sites) {
        // перевести bbox в плоские координаты
        double min_x = LatLngConverter.longitude_to_x(bounding_box.getMinX());
        double min_y = LatLngConverter.latitude_to_y(bounding_box.getMinY());
        double max_x = LatLngConverter.longitude_to_x(bounding_box.getMaxX());
        double max_y = LatLngConverter.latitude_to_y(bounding_box.getMaxY());
        this.bounding_box = new Envelope(min_x, max_x, min_y, max_y);

        // перевести точки в плоские координаты
        double xf, yf;
        GeometryFactory GF = new GeometryFactory();
        points = new Point[sites.length];
        for (int i = 0; i < sites.length; i++) {
            xf = LatLngConverter.longitude_to_x(sites[i].getX());
            yf = LatLngConverter.latitude_to_y(sites[i].getY());
            points[i] = GF.createPoint(new Coordinate(xf, yf));
        }

        // создать диаграмму Вороного
        convert_polygons_to_map_coords(create_polygons());
    }


    private void convert_polygons_to_map_coords (Polygon[] polygon_array) {
        GeometryFactory GF = new GeometryFactory();
        Coordinate[] coords;
        polygons = new Polygon[polygon_array.length];
        Geometry[] garr = new Geometry[polygon_array.length];

        for (int i = 0; i < polygon_array.length; i++) {
            coords = polygon_array[i].getCoordinates();
            for (int c = 0; c < coords.length; c++) {
                coords[c] = new Coordinate(
                        LatLngConverter.x_to_longitude(coords[c].x),
                        LatLngConverter.y_to_latitude(coords[c].y)
                );
            }
            polygons[i] = GF.createPolygon(coords);
        }

        Polygonizer polygonizer = new Polygonizer(true);
        polygonizer.add(Arrays.asList(polygons));
        Collection coll = polygonizer.getPolygons();
        polygons = new Polygon[coll.size()];
        coll.toArray(polygons);
    }


    private Polygon[] create_polygons () {
        try {
            VoronoiFromKotlin VFK = new VoronoiFromKotlin(bounding_box, points);
            Geometry[] geometry_array = VFK.unpolygonized_list;

            Polygonizer polygonizer = new Polygonizer(true);
            polygonizer.add(Arrays.asList(geometry_array));

            Polygon[] p_arr = new Polygon[polygonizer.getPolygons().size()];
            polygonizer.getPolygons().toArray(p_arr);

            if (p_arr[66] == null) {
                throw new Exception("Found it");
            }

            return p_arr;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String s = sw.toString();
        }
        return new Polygon[0];
    }
}
