package com.example.hideseekmapapp.overpass;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

import java.util.Collection;

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
    }


    private Polygon[] create_polygons () {
        GeometryFactory GF = new GeometryFactory();
        // сделать диаграмму Вороного
        VoronoiDiagramBuilder VDB = new VoronoiDiagramBuilder();
        VDB.setClipEnvelope(bounding_box);
        Geometry pts_geom = GF.createMultiPoint(points);
        VDB.setSites(pts_geom);
        Geometry g = VDB.getDiagram(GF);
        // получить диаграмму в виде массива полигонов
        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(g);
        Collection coll = polygonizer.getPolygons();
        return GF.toPolygonArray(coll);
    }
}
