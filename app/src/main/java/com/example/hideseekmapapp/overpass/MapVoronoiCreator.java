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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MapVoronoiCreator {
    // входные параметры
    public Envelope bounding_box;
    public Point[] points;

    // выходные параметры
    public Polygon[] polygons;

    // private
    private GeometryFactory GF = new GeometryFactory();



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
            // построение диаграммы
            VoronoiDiagramBuilder VDB = new VoronoiDiagramBuilder();
            VDB.setClipEnvelope(bounding_box);
            VDB.setSites(GF.createMultiPoint(points));
            Geometry diag = VDB.getDiagram(GF);

            // делаем полигоны
            Polygonizer polygonizer = new Polygonizer(true);
            polygonizer.add(diag);
            Collection coll = polygonizer.getPolygons();
            Polygon[] polygon_arr = new Polygon[coll.size()];
            coll.toArray(polygon_arr);

            // если полигонов меньше, чем точек, надо доделать оставшиеся полигоны
            if (polygon_arr.length < points.length) {
                Point[] uncovered_points = get_uncovered_points(polygon_arr);
                Polygon[] additional_polygons = get_uncovered_areas(polygon_arr, uncovered_points);
                ArrayList<Polygon> polygonArrayList = new ArrayList<>(Arrays.asList(polygon_arr));
                for (Polygon p : additional_polygons) {
                    polygonArrayList.add(p);
                }
                polygon_arr = new Polygon[polygonArrayList.size()];
                polygonArrayList.toArray(polygon_arr);
            }

            return polygon_arr;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String s = sw.toString();
        }
        return new Polygon[0];
    }



    private Point[] get_uncovered_points (Polygon[] formed_polygons) {
        ArrayList<Point> uncovered_points = new ArrayList<>();

        for (Point point : points) {
            for (Polygon polygon : formed_polygons) {
                if (polygon.covers(point)) {
                    uncovered_points.add(point);
                    break;
                }
            }
        }

        Point[] pts = new Point[uncovered_points.size()];
        uncovered_points.toArray(pts);
        return pts;
    }



    private Polygon[] get_uncovered_areas (Polygon[] formed_polygons, Point[] uncovered_points) {
        // для непокрытых точек формируем области вороного
        VoronoiDiagramBuilder VDB = new VoronoiDiagramBuilder();
        VDB.setClipEnvelope(bounding_box);
        VDB.setSites(GF.createMultiPoint(uncovered_points));
        Geometry diag = VDB.getDiagram(GF);

        // делаем полигоны
        Polygonizer polygonizer = new Polygonizer(true);
        polygonizer.add(diag);
        Collection coll = polygonizer.getPolygons();
        Polygon[] missing_polygons = new Polygon[coll.size()];
        coll.toArray(missing_polygons);

        // вычитаем из них сформированные
        Geometry[] cut_polygs = new Geometry[missing_polygons.length];
        for (int i = 0; i < missing_polygons.length; i++) {
            Geometry g = missing_polygons[i].copy();
            for (Polygon minus : formed_polygons) {
                g = g.difference(minus);
            }
            cut_polygs[i] = g;
        }

        // вычтенные преобразовываем в полигоны
        polygonizer = new Polygonizer(true);
        polygonizer.add(Arrays.asList(cut_polygs));
        coll = polygonizer.getPolygons();
        missing_polygons = new Polygon[coll.size()];
        coll.toArray(missing_polygons);
        return missing_polygons;
    }
}
