package com.example.hideseekmapapp.overpass

import com.slaviboy.voronoi.Delaunay
import com.slaviboy.voronoi.Voronoi
import org.locationtech.jts.geom.Coordinate

import org.locationtech.jts.geom.Envelope
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.operation.polygonize.Polygonizer

class VoronoiFromKotlin {
    private lateinit var points : Array<Point>
    private lateinit var bbox : Envelope

    public lateinit var unpolygonized_list : Array<Geometry>



    constructor(_bbox : Envelope, _points : Array<Point>) {
        points = _points
        bbox = _bbox

        make_voronoi(make_delaunay())
    }



    private fun make_delaunay() : Delaunay {
        var coords = ArrayList<Float>()
        for (p in points) {
            coords.add(p.x.toFloat())
            coords.add(p.y.toFloat())
        }
        val delaunay : Delaunay = Delaunay(coords)
        return delaunay
    }



    private fun make_voronoi(delaunay: Delaunay) {
        try {
            val voronoi : Voronoi = delaunay.voronoi(bbox.minX, bbox.minY, bbox.maxX, bbox.maxY)
            val cells : List<Voronoi.CellValues> = voronoi.getCellsCoordinatesSequence().toList()
            var geometry_list = ArrayList<Geometry>()
            var coord_list = ArrayList<Coordinate>()
            var GF = GeometryFactory()
            var i = 0
            for (cell in cells) {
                while (i < cell.coordinates.size) {
                    coord_list.add(Coordinate(cell.coordinates.get(i), cell.coordinates.get(i+1)))
                    i += 2
                }
                coord_list.add(Coordinate(cell.coordinates.get(0), cell.coordinates.get(1)))
                geometry_list.add(GF.createLinearRing(coord_list.toTypedArray()))
                coord_list = ArrayList<Coordinate>()
                i = 0
            }
            unpolygonized_list = geometry_list.toTypedArray()
        } catch (e : Exception) {
            val s : String = e.stackTraceToString()
        }
    }
}