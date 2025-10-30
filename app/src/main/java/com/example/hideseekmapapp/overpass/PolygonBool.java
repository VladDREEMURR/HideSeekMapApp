package com.example.hideseekmapapp.overpass;

import com.menecats.polybool.models.Polygon;
import com.menecats.polybool.PolyBool;
import com.menecats.polybool.Epsilon;

import static com.menecats.polybool.helpers.PolyBoolHelper.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PolygonBool {
    // участники операции
    private Polygon result;

    // на выход
    public org.locationtech.jts.geom.Polygon[] polygons;

    // для процесса операций
    private Epsilon eps = epsilon(1e-12);



    public PolygonBool (org.locationtech.jts.geom.MultiPolygon target, org.locationtech.jts.geom.Polygon[] polygons, PolygonBoolOperationType operation_type) {

        result = jts_to_polybool(multijts_to_arrjts(target));

        if (polygons.length <= 0) {
            this.polygons = polybool_to_jts(result);
            return;
        }

        PolyBool.Segments seg_res = PolyBool.segments(eps, result);

        int threads_num = 4;
        int chunk_size = polygons.length / threads_num;
        ExecutorService executor = Executors.newFixedThreadPool(4);
        PolyBool.Segments[] seg_opers = new PolyBool.Segments[threads_num];

        for (int t = 0; t < threads_num; t++) {
            final int start = t * chunk_size;
            final int end = (t == threads_num - 1) ? polygons.length : start + chunk_size;
            final int seg_id = t;

            executor.submit(new Runnable() {
                @Override
                public void run() {
                    seg_opers[seg_id] = PolyBool.segments(eps, jts_to_polybool(new org.locationtech.jts.geom.Polygon[]{polygons[start]}));
                    for (int i = start + 1; i < end; i++) {
                        PolyBool.Segments newseg = PolyBool.segments(eps, jts_to_polybool(new org.locationtech.jts.geom.Polygon[]{polygons[i]}));
                        PolyBool.Combined combination = PolyBool.combine(eps, seg_opers[seg_id], newseg);
                        switch (operation_type) {
                            case UNION: case DIFFERENCE:
                                seg_opers[seg_id] = PolyBool.selectUnion(combination);
                                break;
                            case INTERSECTION:
                                seg_opers[seg_id] = PolyBool.selectIntersect(combination);
                                break;
                        }
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String s = sw.toString();
        }

        for (int i = 0; i < threads_num; i++) {
            PolyBool.Combined combination = PolyBool.combine(eps, seg_res, seg_opers[i]);
            switch (operation_type) {
                case UNION:
                    seg_res = PolyBool.selectUnion(combination);
                    break;
                case DIFFERENCE:
                    seg_res = PolyBool.selectDifference(combination);
                    break;
                case INTERSECTION:
                    seg_res = PolyBool.selectIntersect(combination);
                    break;
            }
        }

        // положить результат в jts формат
        result = PolyBool.polygon(eps, seg_res);
        this.polygons = polybool_to_jts(result);
    }



    private org.locationtech.jts.geom.Polygon[] multijts_to_arrjts (org.locationtech.jts.geom.MultiPolygon multipolygon) {
        org.locationtech.jts.operation.polygonize.Polygonizer polygonizer = new org.locationtech.jts.operation.polygonize.Polygonizer(true);
        polygonizer.add(multipolygon);
        Collection<org.locationtech.jts.geom.Polygon> coll = polygonizer.getPolygons();
        org.locationtech.jts.geom.Polygon[] pols = new org.locationtech.jts.geom.Polygon[coll.size()];
        coll.toArray(pols);
        return pols;
    }



    private Polygon jts_to_polybool (org.locationtech.jts.geom.Polygon[] polygons) {
        ArrayList<List<double[]>> regions = new ArrayList<>();
        ArrayList<double[]> coords;

        for (org.locationtech.jts.geom.Polygon polygon : polygons) {
            // outer
            coords = new ArrayList<>();
            for (org.locationtech.jts.geom.Coordinate c : polygon.getExteriorRing().getCoordinates()) {
                coords.add(new double[]{c.x, c.y});
            }
            regions.add(coords);

            // inner
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                coords = new ArrayList<>();
                for (org.locationtech.jts.geom.Coordinate c : polygon.getInteriorRingN(i).getCoordinates()) {
                    coords.add(new double[]{c.x, c.y});
                }
                regions.add(coords);
            }
        }
        
        return new Polygon(regions);
    }



    private org.locationtech.jts.geom.Polygon[] polybool_to_jts (Polygon polygon) {
        org.locationtech.jts.geom.GeometryFactory GF = new org.locationtech.jts.geom.GeometryFactory();
        ArrayList<org.locationtech.jts.geom.LinearRing> rings = new ArrayList<>();
        org.locationtech.jts.geom.LinearRing[] ring_arr;
        ArrayList<org.locationtech.jts.geom.Coordinate> coords;
        org.locationtech.jts.geom.Coordinate[] coord_arr;

        // берём и подготавливаем координаты
        ArrayList<ArrayList<double[]>> regions = new ArrayList<>();
        ArrayList<double[]> temp;
        for (List<double[]> list : polygon.getRegions()) {
            temp = new ArrayList<>(list);
            temp.add(temp.get(0));
            regions.add(temp);
        }

        // создаём кольца
        for (ArrayList<double[]> region : regions) {
            coords = new ArrayList<>();
            for (double[] c : region) {
                coords.add(new org.locationtech.jts.geom.Coordinate(c[0], c[1]));
            }
            coord_arr = new org.locationtech.jts.geom.Coordinate[coords.size()];
            coords.toArray(coord_arr);
            rings.add(GF.createLinearRing(coord_arr));
        }
        ring_arr = new org.locationtech.jts.geom.LinearRing[rings.size()];
        rings.toArray(ring_arr);
        org.locationtech.jts.geom.MultiLineString mls = GF.createMultiLineString(ring_arr);

        // полигонизируем
        org.locationtech.jts.operation.polygonize.Polygonizer polygonizer = new org.locationtech.jts.operation.polygonize.Polygonizer(true);
        polygonizer.add(mls);
        Collection coll = polygonizer.getPolygons();
        org.locationtech.jts.geom.Polygon[] polygon_arr = new org.locationtech.jts.geom.Polygon[coll.size()];
        coll.toArray(polygon_arr);
        return polygon_arr;
    }
}
