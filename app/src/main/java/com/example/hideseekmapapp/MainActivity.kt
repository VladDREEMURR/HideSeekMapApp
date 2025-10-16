package com.example.hideseekmapapp

import com.example.hideseekmapapp.overpass.OverpassProcessor

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.example.hideseekmapapp.overpass.Matching
import com.example.hideseekmapapp.overpass.OverpassQueries

import com.example.hideseekmapapp.overpass.Radar
import com.example.hideseekmapapp.overpass.Thermometer

import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.geometry.Polygon
import com.yandex.mapkit.geometry.LinearRing
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.PolygonMapObject

// TODO: сделать рабочей область вопросов и настроек (возможно, отребуются отдельные классы для управления этими категориями)
// TODO: протестировать зарисовку областей на карте через OverpassProcessor
// TODO: сделать overpass запрос в отдельных файлах для каждого вопроса

class MainActivity : ComponentActivity() {
    // режим приложения
    private var mode : String = "seeker" // "seeker" или "hider"

    // содержатели элементов меню
    private lateinit var map_view : MapView
    private lateinit var button_orientation_north : Button
    private lateinit var button_zoom_area : Button
    private lateinit var button_questions : Button
    private lateinit var button_settings : Button
    private lateinit var layout_questions : LinearLayout
    private lateinit var layout_settings : GridLayout

    // показываем ли элементы меню
    private var questons_shown : Boolean = false
    private var settings_shown : Boolean = false

    // зона поиска
    private lateinit var remaining_area : org.locationtech.jts.geom.MultiPolygon
    private lateinit var bounding_box : BoundingBox

    private var overpass_processor : OverpassProcessor = OverpassProcessor()

    // для тестирования
    private lateinit var test_output_block : TextView
    private lateinit var point_array : Array<org.locationtech.jts.geom.Point>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // яндекс карты подготовка
        MapKitFactory.setApiKey("60b6e681-e142-4dd6-8f98-73996515ab97")
        MapKitFactory.initialize(this@MainActivity)

        // подготовка интерфейса
        prepare_interface()

        // блок тестового вывода
        test_output_block = findViewById(R.id.test_output)

        // some code
        point_array = overpass_processor.testOverpass()
        for (p in point_array) {
            add_point_to_map(p)
        }

        // пример использования радара
        var rad : Radar = Radar(37.620, 55.754, 10.0)
        rad.create_areas()
//        add_polygon_to_map(rad.area)

        // пример использования термометра
        try {
            var start_x = 37.60
            var start_y = 55.74
            var end_x = 37.66
            var end_y = 55.76
//        var thermo : Thermometer = Thermometer(rad.area, start_x, start_y, end_x, end_y)
            var matching : Matching = Matching(OverpassQueries.TRAIN_TERMINAL)

            var i = 0;
            while (i < matching.polygons.size) {
                if (matching.polygons[i] == null) {
                    throw Exception("Found null at " + i.toString())
                }
                i += 1
            }

            remaining_area = org.locationtech.jts.geom.MultiPolygon(matching.polygons, org.locationtech.jts.geom.GeometryFactory())
//        draw_remaining_area()
            /*
            for (multipolygon in matching.variants.values) {
                add_multipolygon_to_map(multipolygon)
            }
            */
            add_polygon_array_to_map(matching.polygons)
        } catch (e : Exception) {
            val s : String = e.stackTraceToString()
        }

    }


    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        map_view.onStart()
    }


    override fun onStop() {
        super.onStop()
        MapKitFactory.getInstance().onStop()
        map_view.onStop()
    }


    private fun unite_polygon_with_remaining(polygon : org.locationtech.jts.geom.Polygon) {
        // TODO: доделать объединение с областью поиска
    }


    private fun draw_remaining_area() {
        add_multipolygon_to_map(remaining_area)
    }


    private fun add_point_to_map(point : org.locationtech.jts.geom.Point) {
        val map_p = Point(point.y, point.x)
        val placemark : PlacemarkMapObject = map_view.map.mapObjects.addPlacemark(map_p)
    }


    private fun add_multipolygon_to_map(multipolygon : org.locationtech.jts.geom.MultiPolygon) {
        val count = multipolygon.numGeometries
        val polygon_array = Array(count){i -> multipolygon.getGeometryN(i) as org.locationtech.jts.geom.Polygon}
        add_polygon_array_to_map(polygon_array)
    }


    private fun add_polygon_array_to_map(polygon_array : Array<org.locationtech.jts.geom.Polygon>) {
        for (p in polygon_array) {
            add_polygon_to_map(p)
        }
    }


    private fun add_polygon_to_map(polygon : org.locationtech.jts.geom.Polygon) {
        // TODO: учитывать внутренние границы геометрии
        val mapLinRing : LinearRing = LinearRing(polygon.exteriorRing.coordinates.map { Point(it.y, it.x) })
        val mapPolygon : Polygon = Polygon(mapLinRing, emptyList<LinearRing>())
        var map_obj : PolygonMapObject = map_view.map.mapObjects.addPolygon(mapPolygon)
        map_obj.apply {
            strokeWidth = 1.0f
            strokeColor = ContextCompat.getColor(this@MainActivity, R.color.red_dark)
            fillColor = Color.argb(
                20,
                3, 218, 197
            )
        }
    }


    private fun clear_map() {
        map_view.map.mapObjects.clear()
    }


    private fun prepare_interface() {
        // получение элементов интерфейса
        setContentView(R.layout.activity_main)
        map_view = findViewById(R.id.map_view)
        button_orientation_north = findViewById(R.id.north_orientation_button)
        button_zoom_area = findViewById(R.id.zoom_area_button)
        button_questions = findViewById(R.id.toggle_questions_button)
        button_settings = findViewById(R.id.toggle_settings_button)
        layout_questions = findViewById(R.id.questions_layout)
        layout_settings = findViewById(R.id.settings_layout)

        // события клика на кнопки
        button_orientation_north.setOnClickListener { // ориентация на север
            val current_map_position = map_view.map.cameraPosition.target
            val current_zoom = map_view.map.cameraPosition.zoom
            map_view.map.move(
                CameraPosition(current_map_position, current_zoom, 0.0f, 0.0f),
                Animation(Animation.Type.SMOOTH, 0.5f),
                null
            )
        }
        button_zoom_area.setOnClickListener { // зум по области
            val bounding_box : org.locationtech.jts.geom.Envelope = remaining_area.envelopeInternal
            val map_bounding = BoundingBox(
                Point(bounding_box.minY, bounding_box.minX),
                Point(bounding_box.maxY, bounding_box.maxX)
            )
            var camera_pos = map_view.map.cameraPosition(Geometry.fromBoundingBox(map_bounding))
            camera_pos = CameraPosition(camera_pos.target, camera_pos.zoom - 0.8f, camera_pos.azimuth, camera_pos.tilt)
            map_view.map.move(
                camera_pos,
                Animation(Animation.Type.SMOOTH, 1.0f),
                null
            )
        }
        button_questions.setOnClickListener {
            if (questons_shown) {
                questons_shown = false
            } else {
                settings_shown = false
                questons_shown = true
            }
            refresh_layout_weights()
        }
        button_settings.setOnClickListener {
            if (settings_shown) {
                settings_shown = false
            } else {
                questons_shown = false
                settings_shown = true
            }
            refresh_layout_weights()
        }

        // обновить весы высот интерфейса
        refresh_layout_weights()
    }


    private fun refresh_layout_weights() {
        val layout_params_questions = layout_questions.layoutParams as LinearLayout.LayoutParams
        val layout_params_settings : ViewGroup.LayoutParams = layout_settings.layoutParams

        if (questons_shown) {
            layout_params_questions.weight = 10.0f
        } else {
            layout_params_questions.weight = 0.0f
        }

        if (settings_shown) {
            layout_params_settings.height = ((layout_settings.parent as View).height * 0.2).toInt();
        } else {
            layout_params_settings.height = 0
        }

        layout_questions.layoutParams = layout_params_questions
        layout_settings.layoutParams = layout_params_settings
    }
}
