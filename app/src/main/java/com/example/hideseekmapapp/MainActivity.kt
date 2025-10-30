package com.example.hideseekmapapp

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.widget.PopupMenu
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible

import com.example.hideseekmapapp.overpass.OverpassQueries
import com.example.hideseekmapapp.overpass.Question
import com.example.hideseekmapapp.overpass.QuestionType
import com.example.hideseekmapapp.overpass.Radar
import com.example.hideseekmapapp.overpass.Thermometer
import com.example.hideseekmapapp.overpass.OverpassProcessor
import com.example.hideseekmapapp.overpass.PolygonBool
import com.example.hideseekmapapp.overpass.PolygonBoolOperationType

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
import com.yandex.runtime.image.ImageProvider

// TODO: сделать рабочей область вопросов и настроек (возможно, отребуются отдельные классы для управления этими категориями)
// TODO: добавить возможность кастомизировать цвета
// TODO: интерфейс + Matching
// TODO: интерфейс + Measuring
// TODO: интерфейс + Thermometer
// TODO: интерфейс + Radar
// TODO: интерфейс + Tentacles
// TODO: добавить возможность копировать координаты точки при долгом нажатии на яндекс карте

class MainActivity : ComponentActivity() {
    // режим приложения
    private var mode : String = "seeker" // "seeker" или "hider"

    // содержатели элементов меню
    private lateinit var map_view : MapView
    private lateinit var button_orientation_north : Button
    private lateinit var button_zoom_area : Button
    private lateinit var button_questions : Button
    private lateinit var button_settings : Button
    private lateinit var button_new_question : Button
    private lateinit var layout_question_list : LinearLayout
    private lateinit var layout_question_block : LinearLayout
    private lateinit var layout_settings : GridLayout

    // список вопросов
    private var question_object_list : ArrayList<Question> = arrayListOf()
    private var question_tablet_list : ArrayList<View> = arrayListOf()
    private var question_label_list : ArrayList<LinearLayout> = arrayListOf()
    private var question_id_list : ArrayList<String> = arrayListOf()

    // показываем ли элементы меню
    private var questons_shown : Boolean = false
    private var settings_shown : Boolean = false

    // зона поиска
    private lateinit var remaining_area : org.locationtech.jts.geom.MultiPolygon
    private lateinit var bounding_box : BoundingBox

    // вспомогательное
    private val GF = org.locationtech.jts.geom.GeometryFactory()

    // для тестирования
    private var overpass_processor : OverpassProcessor = OverpassProcessor()
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

        val query = OverpassQueries.COMMERCIAL_AIRPORT

        point_array = overpass_processor.testOverpass(query)
        for (p in point_array) {
            add_point_to_map(p, ContextCompat.getColor(this@MainActivity, R.color.dot_of_object))
        }

        // пример использования радара
        var rad = Radar(37.620, 55.754, 10.0)
        rad.create_areas()

        var start_x = 37.490
        var start_y = 55.746
        var GF = org.locationtech.jts.geom.GeometryFactory()
        val curr_point = GF.createPoint(org.locationtech.jts.geom.Coordinate(start_x, start_y))

        add_point_to_map(curr_point, ContextCompat.getColor(this@MainActivity, R.color.dot_location))

        try {
            var end_x = 37.66
            var end_y = 55.76


            var thermo = Thermometer(rad.area, start_x, start_y, end_x, end_y)
            remaining_area = org.locationtech.jts.geom.MultiPolygon(arrayOf(thermo.hotter_area), GF)
            draw_remaining_area()
        } catch (e : Exception) {
            val s : String = e.stackTraceToString()
            test_output_block.text = s;
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




    /*
    ==================================================
    РАБОТА С ЯНДЕКС.КАРТОЙ
    ==================================================
    */




    private fun intersect_multipolygon_with_remaining_area(multipolygon : org.locationtech.jts.geom.MultiPolygon) {
        val toarr = PolygonBool(multipolygon, arrayOf(), PolygonBoolOperationType.UNION)
        val PB = PolygonBool(remaining_area, toarr.polygons, PolygonBoolOperationType.INTERSECTION)
        remaining_area = GF.createMultiPolygon(PB.polygons)
    }




    private fun draw_remaining_area() {
        add_multipolygon_to_map(remaining_area,
            ContextCompat.getColor(this@MainActivity, R.color.remaining_area_border),
            ContextCompat.getColor(this@MainActivity, R.color.remaining_area_background)
        )
    }




    private fun add_point_to_map(point : org.locationtech.jts.geom.Point, color: Int) {
        val map_p = Point(point.y, point.x)
        val placemark : PlacemarkMapObject = map_view.map.mapObjects.addPlacemark(map_p)

        val size = 20
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            this.color = 0xFFFF0000.toInt()
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        placemark.setIcon(ImageProvider.fromBitmap(bitmap))
    }




    private fun add_multipolygon_to_map(multipolygon : org.locationtech.jts.geom.MultiPolygon, color_border : Int, color_background : Int) {
        val count = multipolygon.numGeometries
        val polygon_array = Array(count){i -> multipolygon.getGeometryN(i) as org.locationtech.jts.geom.Polygon}
        add_polygon_array_to_map(polygon_array, color_border, color_background)
    }




    private fun add_polygon_array_to_map(polygon_array : Array<org.locationtech.jts.geom.Polygon>, color_border : Int, color_background : Int) {
        for (p in polygon_array) {
            add_polygon_to_map(p, color_border, color_background)
        }
    }




    private fun add_polygon_to_map(polygon : org.locationtech.jts.geom.Polygon, color_border : Int, color_background : Int) {
        fun ring_to_point_arr (ring : org.locationtech.jts.geom.LinearRing) : List<Point> {
            return ring.coordinates.map { Point(it.y, it.x) }
        }

        // outer
        val map_outer_ring = LinearRing(ring_to_point_arr(polygon.exteriorRing))

        // inner
        val map_inner_ring_arr = mutableListOf<LinearRing>()
        for (i in 0 until polygon.numInteriorRing) {
            val inner_ring = polygon.getInteriorRingN(i) as org.locationtech.jts.geom.LinearRing
            val map_inner_ring = LinearRing(ring_to_point_arr(inner_ring))
            map_inner_ring_arr.add(map_inner_ring)
        }

        // form polygon
        val map_polygon = Polygon(map_outer_ring, map_inner_ring_arr)
        var map_obj : PolygonMapObject = map_view.map.mapObjects.addPolygon(map_polygon)
        map_obj.apply {
            strokeWidth = 1.0f
            strokeColor = color_border
            fillColor = color_background
        }
    }




    private fun clear_map() {
        map_view.map.mapObjects.clear()
    }




    /*
    ==================================================
    ИНТЕРФЕЙС
    ==================================================
    */




    private fun prepare_interface() {
        // получение элементов интерфейса
        setContentView(R.layout.activity_main)
        map_view = findViewById(R.id.map_view)
        button_orientation_north = findViewById(R.id.north_orientation_button)
        button_zoom_area = findViewById(R.id.zoom_area_button)
        button_questions = findViewById(R.id.toggle_questions_button)
        button_settings = findViewById(R.id.toggle_settings_button)
        button_new_question = findViewById(R.id.new_question_button)
        layout_question_list = findViewById(R.id.question_list_layout)
        layout_question_block = findViewById(R.id.questions_layout)
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
            camera_pos = CameraPosition(camera_pos.target, camera_pos.zoom + 0.5f, camera_pos.azimuth, camera_pos.tilt)
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
        button_new_question.setOnClickListener { view ->
            val popup_menu = PopupMenu(this, view)
            popup_menu.inflate(R.menu.new_question_popup)

            popup_menu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.popup_new_matching -> {
                        create_new_question(QuestionType.MATCHING)
                        true
                    }
                    R.id.popup_new_measuring -> {
                        create_new_question(QuestionType.MEASURING)
                        true
                    }
                    R.id.popup_new_thermometer -> {
                        create_new_question(QuestionType.THERMOMETER)
                        true
                    }
                    R.id.popup_new_radar -> {
                        create_new_question(QuestionType.RADAR)
                        true
                    }
                    R.id.popup_new_tentacles -> {
                        create_new_question(QuestionType.TENTACLES)
                        true
                    }
                    else -> false
                }
            }
            popup_menu.show()
        }

        // последние штрихи обработки
        refresh_layout_weights()
        layout_question_list.removeAllViews()
    }




    private fun refresh_layout_weights() {
        val layout_params_questions = layout_question_block.layoutParams as LinearLayout.LayoutParams
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

        layout_question_block.layoutParams = layout_params_questions
        layout_settings.layoutParams = layout_params_settings
    }




    /*
    ==================================================
    РАБОТА С ВОПРОСАМИ
    ==================================================
    */




    // создание нового вопроса
    private fun create_new_question(question_type: QuestionType) {
        val list_id = question_label_list.size

        create_new_question_id_in_a_list(question_type, list_id)
        create_new_question_tablet(question_type, list_id)
    }




    // создание нового tablet и его показ
    private fun create_new_question_tablet(question_type : QuestionType, list_id: Int) {
        val inflater = this.layoutInflater
        val layout_question_tablet : View

        when (question_type) {
            QuestionType.MATCHING -> {
                layout_question_tablet = inflater.inflate(R.layout.matching_tablet, null, false)
                true
            }
            QuestionType.MEASURING -> {
                layout_question_tablet = inflater.inflate(R.layout.measuring_tablet, null, false)
                true
            }
            QuestionType.THERMOMETER -> {
                layout_question_tablet = inflater.inflate(R.layout.thermometer_tablet, null, false)
                true
            }
            QuestionType.RADAR -> {
                layout_question_tablet = inflater.inflate(R.layout.radar_tablet, null, false)
                true
            }
            QuestionType.TENTACLES -> {
                layout_question_tablet = inflater.inflate(R.layout.tentacles_tablet, null, false)
                true
            }
        }

        layout_question_tablet.tag = list_id
        question_tablet_list.add(layout_question_tablet)
        layout_question_list.addView(layout_question_tablet)

        // TODO: дать кнопкам функционал
    }




    // добавим новый id вопроса в список
    private fun create_new_question_id_in_a_list(question_type: QuestionType, list_id : Int) {
        // делаем строку для списка
        val label = LinearLayout(this) // контейнер строки
        val text_label = TextView(this) // label вопроса
        val toggle_vis_button = Button(this) // кнопка переключения видимости

        // функционал кнопки
        toggle_vis_button.tag = list_id
        toggle_vis_button.setOnClickListener { view ->
            toggle_tablet_visibility(view.tag.toString().toInt())
        }

        // добавляем в интерфейс
        layout_question_list.addView(label)
        label.addView(text_label)
        label.addView(toggle_vis_button)

        // параметры контейнера
        val params = label.layoutParams
        label.orientation = LinearLayout.HORIZONTAL
        label.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.ui_background_teal))
        params.width = LinearLayout.LayoutParams.MATCH_PARENT
        params.height = LinearLayout.LayoutParams.WRAP_CONTENT
        label.layoutParams = params

        question_label_list.add(label)
        when (question_type) {
            QuestionType.MATCHING -> {
                question_id_list.add("matching_$list_id")
                true
            }
            QuestionType.MEASURING -> {
                question_id_list.add("measuring_$list_id")
                true
            }
            QuestionType.THERMOMETER -> {
                question_id_list.add("thermometer_$list_id")
                true
            }
            QuestionType.RADAR -> {
                question_id_list.add("radar_$list_id")
                true
            }
            QuestionType.TENTACLES -> {
                question_id_list.add("tentacles_$list_id")
                true
            }
        }
    }




    // строковый id в численный формат
    private fun string_id_to_int(question_id : String) : Int {
        val strings = question_id.split("_")
        return strings.get(1).toInt()
    }




    // переключить видимость tablet
    private fun toggle_tablet_visibility(list_id : Int) {
        var params = question_tablet_list.get(list_id).layoutParams
        if (params.height != 0) {
            params.height = 0
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        question_tablet_list.get(list_id).layoutParams = params
    }




    // удаление вопроса (и его элементов)
    private fun delete_question(list_id: Int) {
        // удалить элементы интерфейса
        layout_question_list.removeView(question_label_list.get(list_id))
        layout_question_list.removeView(question_tablet_list.get(list_id))

        // удалить из списков
        question_id_list.removeAt(list_id)
        question_label_list.removeAt(list_id)
        question_tablet_list.removeAt(list_id)
        question_object_list.removeAt(list_id)
    }




    // создание и отображение объектов
    private fun create_show_areas(list_id: Int) {
        // TODO: сделать создание и отображение объектов
    }




    // генерация ответа
    private fun generate_answer(list_id: Int) {
        // TODO: сделать генерацию ответа
    }




    // применение ответа
    private fun apply_answer(list_id: Int) {
        // TODO: сделать применение ответа
    }
}
