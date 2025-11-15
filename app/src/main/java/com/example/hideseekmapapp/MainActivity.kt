package com.example.hideseekmapapp

import android.content.Context
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
import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.Switch
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.hideseekmapapp.overpass.AnswerSetType
import com.example.hideseekmapapp.overpass.GlobalAreaFounder
import com.example.hideseekmapapp.overpass.Matching
import com.example.hideseekmapapp.overpass.Measuring
import com.example.hideseekmapapp.overpass.ObjectTypeQuestionSets
import com.example.hideseekmapapp.overpass.ObjectTypeTranslator

import com.example.hideseekmapapp.overpass.Question
import com.example.hideseekmapapp.overpass.QuestionType
import com.example.hideseekmapapp.overpass.Radar
import com.example.hideseekmapapp.overpass.OverpassProcessor
import com.example.hideseekmapapp.overpass.OverpassQueries
import com.example.hideseekmapapp.overpass.PolygonBool
import com.example.hideseekmapapp.overpass.PolygonBoolOperationType
import com.example.hideseekmapapp.overpass.Tentacles
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
    private lateinit var button_clear_map : Button
    private lateinit var button_new_question : Button
    private lateinit var layout_question_list : LinearLayout
    private lateinit var layout_question_block : LinearLayout
    private lateinit var layout_settings : GridLayout

    // списки для вопросов
    private var question_object_list : MutableMap<String, Question> = mutableMapOf()
    private var question_tablet_list : MutableMap<String, View> = mutableMapOf()
    private var question_label_list : MutableMap<String, LinearLayout> = mutableMapOf()
    private var question_type_list : MutableMap<Int, QuestionType> = mutableMapOf()
    private var question_global_id : Int = 0

    // показываем ли элементы меню
    private var questons_shown : Boolean = false
    private var settings_shown : Boolean = false

    // зона поиска
    private lateinit var remaining_area : org.locationtech.jts.geom.MultiPolygon
    private lateinit var global_area : org.locationtech.jts.geom.MultiPolygon
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
        val GAF = GlobalAreaFounder(OverpassQueries.GLOBAL_AREA)
        global_area = GAF.area
        remaining_area = global_area


        // подготовка интерфейса
        prepare_interface()

        // блок тестового вывода
        test_output_block = findViewById(R.id.test_output)
        val query = OverpassQueries.MUSEUM
        /*
        val OP = OverpassProcessor()
        val p_arr = OP.testOverpass(query)
        for (point in p_arr) {
            add_point_to_map(point, ContextCompat.getColor(this@MainActivity, R.color.dot_of_object))
        }

        */

        // some code
        val start_x = 37.618
        val start_y = 55.756
        val end_x = 37.604
        val end_y = 55.745
        val start_point = GF.createPoint(org.locationtech.jts.geom.Coordinate(start_x, start_y))
        val end_point = GF.createPoint(org.locationtech.jts.geom.Coordinate(end_x, end_y))
        add_point_to_map(start_point, ContextCompat.getColor(this@MainActivity, R.color.dot_location))
        draw_remaining_area()

        try {
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
            this.color = color
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
        draw_remaining_area()
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
        button_clear_map = findViewById(R.id.clear_map_button)
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
        button_clear_map.setOnClickListener {
            clear_map()
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
        create_new_question_id_in_a_list(question_type, question_global_id)
        create_new_question_tablet(question_type, question_global_id)

        question_global_id += 1
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

        question_type_list[list_id] = question_type
        question_label_list[int_and_type_to_string_id(question_type, list_id)] = label
    }




    // создание нового tablet и его показ
    private fun create_new_question_tablet(question_type : QuestionType, list_id: Int) {
        // получаем view
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

        val create_objects_button : Button? = layout_question_tablet.findViewById(R.id.create_show_objects_button)
        val create_areas_button : Button = layout_question_tablet.findViewById(R.id.create_show_areas_button)
        val generate_answer_button : Button = layout_question_tablet.findViewById(R.id.generate_answer_button)
        val apply_answer_button : Button = layout_question_tablet.findViewById(R.id.apply_answer_button)
        val delete_question_button : Button = layout_question_tablet.findViewById(R.id.delete_question_button)
        val object_type_spinner : Spinner? = layout_question_tablet.findViewById(R.id.object_type)
        create_areas_button.isEnabled = false
        generate_answer_button.isEnabled = false
        apply_answer_button.isEnabled = false

        // даём кнопкам функционал
        if (create_objects_button != null) { // создание объектов
            create_objects_button.isEnabled = false
            create_objects_button.setOnClickListener { view ->
                val list_id = (view.parent as View).tag.toString().toInt()
                create_show_objects(question_type_list[list_id], list_id)
            }
        }
        create_areas_button.setOnClickListener { view -> // создание областей
            val list_id = (view.parent as View).tag.toString().toInt()
            create_show_areas(question_type_list[list_id], list_id)
        }
        generate_answer_button.setOnClickListener { view -> // генерирование ответа
            val list_id = (view.parent as View).tag.toString().toInt()
            generate_answer(question_type_list[list_id], list_id)
        }
        apply_answer_button.setOnClickListener { view -> // применение ответа
            val list_id = (view.parent as View).tag.toString().toInt()
            apply_answer(question_type_list[list_id], list_id)
        }
        delete_question_button.setOnClickListener { view -> // удаление ответа
            val list_id = (view.parent as View).tag.toString().toInt()
            delete_question(list_id)
        }

        // даём спиннеру функционал
        val translated_types = arrayListOf<String>()
        var type_strings : Array<String> = arrayOf()
        val adapter : ArrayAdapter<String>
        when (question_type) {
            QuestionType.MATCHING -> {
                type_strings = ObjectTypeQuestionSets.matching
                true
            }
            QuestionType.MEASURING -> {
                type_strings = ObjectTypeQuestionSets.measuring
                true
            }
            QuestionType.TENTACLES -> {
                type_strings = ObjectTypeQuestionSets.tentacles
                true
            }
            else -> false
        }
        if (!type_strings.isEmpty() && object_type_spinner != null) {
            for (str in type_strings) {
                translated_types.add(ObjectTypeTranslator.str_to_russian(str))
            }
            adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,
                translated_types)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            object_type_spinner.adapter = adapter
            object_type_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // пока нечего делать
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // хз, как ничего-невыбираение может произойти
                    // TODO: на случай, если будет возможно ничего не выбрать, предусмотреть
                }
            }
        }

        // добавление блокировки/разблокировки кнопок в зависимости от ответов
        val seeker_point_field : EditText? = layout_question_tablet.findViewById(R.id.seeker_point)
        val object_id_field : EditText? = layout_question_tablet.findViewById(R.id.object_id)
        val distance_field : EditText? = layout_question_tablet.findViewById(R.id.distance)
        val point_colder_field : EditText? = layout_question_tablet.findViewById(R.id.point_colder)
        val point_hotter_field : EditText? = layout_question_tablet.findViewById(R.id.point_hotter)
            // обновляемые поля для прячущегося
        val hider_point_field : EditText? = layout_question_tablet.findViewById(R.id.hider_point)
            // добавляем функцию обновления
        add_field_renew_ivent(seeker_point_field)
        add_field_renew_ivent(object_id_field)
        add_field_renew_ivent(distance_field)
        add_field_renew_ivent(point_colder_field)
        add_field_renew_ivent(point_hotter_field)
        add_field_renew_ivent(hider_point_field)


        // добавляем в интерфейс и список
        question_tablet_list[int_and_type_to_string_id(question_type, list_id)] = layout_question_tablet
        layout_question_list.addView(layout_question_tablet)
    }




    // добавить текстовому полю обновление статуса кнопок
    private fun add_field_renew_ivent(edit_text : EditText?) {
        if (edit_text != null) {
            edit_text.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    renew_buttons_enability(edit_text.parent as View)
                }
            })
        }
    }




    // обновить кнопки
    private fun renew_buttons_enability(tablet : View) {
        val list_id = tablet.tag.toString().toInt()
        val str_id = int_to_string_id(list_id)
        val question_type = question_type_list[list_id]
        val create_objects_button: Button? = tablet.findViewById(R.id.create_show_objects_button)
        val create_areas_button: Button = tablet.findViewById(R.id.create_show_areas_button)
        val generate_answer_button: Button = tablet.findViewById(R.id.generate_answer_button)
        val apply_answer_button: Button = tablet.findViewById(R.id.apply_answer_button)

        // обновление для всех кнопок каждого типа вопроса
        when (question_type) {
            QuestionType.MATCHING -> {
                if (tablet.findViewById<EditText>(R.id.seeker_point).text.length > 0) {
                    create_objects_button?.isEnabled = true
                }
                if (question_object_list.containsKey(str_id)) {
                    create_areas_button.isEnabled = true
                    apply_answer_button.isEnabled = true
                    if (tablet.findViewById<EditText>(R.id.object_id).text.length > 0) {
                        generate_answer_button.isEnabled = true
                    }
                }
                true
            }
            QuestionType.MEASURING -> {
                if (tablet.findViewById<EditText>(R.id.seeker_point).text.length > 0) {
                    create_objects_button?.isEnabled = true
                }
                if (question_object_list.containsKey(str_id)) {
                    create_areas_button.isEnabled = true
                    apply_answer_button.isEnabled = true
                    if (tablet.findViewById<EditText>(R.id.distance).text.length > 0) {
                        generate_answer_button.isEnabled = true
                    }
                }
                true
            }
            QuestionType.RADAR -> {
                if (
                    tablet.findViewById<EditText>(R.id.seeker_point).text.length > 0 &&
                    tablet.findViewById<EditText>(R.id.distance).text.length > 0
                ) {
                    create_areas_button.isEnabled = true
                }
                if (question_object_list.containsKey(str_id)) {
                    apply_answer_button.isEnabled = true
                    if (tablet.findViewById<EditText>(R.id.hider_point).text.length > 0) {
                        generate_answer_button.isEnabled = true
                    }
                }
                true
            }
            QuestionType.TENTACLES -> {
                if (
                    tablet.findViewById<EditText>(R.id.seeker_point).text.length > 0 &&
                    tablet.findViewById<EditText>(R.id.distance).text.length > 0
                ) {
                    create_objects_button?.isEnabled = true
                }
                if (question_object_list.containsKey(str_id)) {
                    create_areas_button.isEnabled = true
                    apply_answer_button.isEnabled = true
                    if (tablet.findViewById<EditText>(R.id.hider_point).text.length > 0) {
                        generate_answer_button.isEnabled = true
                    }
                }
                true
            }
            QuestionType.THERMOMETER -> {
                if (
                    tablet.findViewById<EditText>(R.id.point_colder).text.length > 0 &&
                    tablet.findViewById<EditText>(R.id.point_hotter).text.length > 0
                ) {
                    create_areas_button.isEnabled = true
                }
                if (question_object_list.containsKey(str_id)) {
                    apply_answer_button.isEnabled = true
                    if (tablet.findViewById<EditText>(R.id.hider_point).text.length > 0) {
                        generate_answer_button.isEnabled = true
                    }
                }
                true
            }
            else -> false
        }
    }




    // строковый id в численный формат
    private fun string_id_to_int(question_id : String) : Int {
        val strings = question_id.split("_")
        return strings.get(1).toInt()
    }

    // строковый id в тип вопроса
    private fun string_id_to_type(question_id: String) : QuestionType {
        val str = question_id.split("_").get(0)
        when (str) {
            "matching" -> { return QuestionType.MATCHING }
            "measuring" -> { return QuestionType.MEASURING }
            "radar" -> { return QuestionType.RADAR }
            "tentacles" -> { return QuestionType.TENTACLES }
            "thermometer" -> { return QuestionType.THERMOMETER }
            else -> throw Exception("string_id_to_type() : No question type")
        }
    }

    // число и тип в строковый id
    private fun int_and_type_to_string_id(question_type: QuestionType?, list_id: Int) : String {
        when (question_type) {
            QuestionType.MATCHING -> { return "matching_$list_id" }
            QuestionType.MEASURING -> { return "measuring_$list_id" }
            QuestionType.RADAR -> { return "radar_$list_id" }
            QuestionType.TENTACLES -> { return "tentacles_$list_id" }
            QuestionType.THERMOMETER -> { return "thermometer_$list_id" }
            else -> throw Exception("int_and_type_to_string_id() : No question type")
        }
    }

    // число в строковый id
    private fun int_to_string_id(list_id: Int) : String {
        return int_and_type_to_string_id(question_type_list[list_id], list_id)
    }




    // переключить видимость tablet
    private fun toggle_tablet_visibility(list_id : Int) {
        val q_id = int_to_string_id(list_id)
        val params = question_tablet_list[q_id]?.layoutParams
        if (params != null) {
            if (params.height != 0) {
                params.height = 0
            } else {
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            question_tablet_list[q_id]?.layoutParams = params
        }
    }




    // удаление вопроса (и его элементов)
    private fun delete_question(list_id: Int) {
        // удалить элементы интерфейса
        val q_id = int_to_string_id(list_id)
        layout_question_list.removeView(question_label_list[q_id])
        layout_question_list.removeView(question_tablet_list[q_id])

        // удалить из списков
        question_type_list.remove(list_id)
        question_label_list.remove(q_id)
        question_tablet_list.remove(q_id)
        question_object_list.remove(q_id)
    }




    // строка в точку
    private fun str_to_point(str : String) : org.locationtech.jts.geom.Point {
        val strings = str.split(", ")
        if (strings.size == 2) {
            val y = strings[0].toDoubleOrNull()
            val x = strings[1].toDoubleOrNull()
            if (x == null || y == null) {
                return remaining_area.centroid
            } else {
                return GF.createPoint(org.locationtech.jts.geom.Coordinate(x, y))
            }
        } else {
            return remaining_area.centroid
        }
    }




    // строка в число
    private fun str_to_double(str: String) : Double {
        val d = str.toDoubleOrNull()
        if (d == null || d < 0.0) {
            return 1.0
        } else {
            return d
        }
    }




    // создание и отображение объектов
    private fun create_show_objects(question_type: QuestionType?, list_id: Int) {
        if (question_type == null) {throw Exception("create_show_objects() : No question type")}
        var question : Question? = null
        val tablet = question_tablet_list[int_to_string_id(list_id)]
        when (question_type) {
            QuestionType.MATCHING -> {
                val eng_type_str = ObjectTypeTranslator.russian_to_str((tablet?.findViewById<Spinner>(R.id.object_type)?.selectedItem) as String)
                val point = str_to_point(tablet.findViewById<EditText>(R.id.seeker_point).text.toString())
                question = Matching(
                    ObjectTypeTranslator.str_to_query(eng_type_str)
                )
                question.exec_overpass()
                if (question.get_answer_set_type() == AnswerSetType.VORONOI) {
                    val point_arr = question.point_storage.values
                    for (p in point_arr) {
                        add_point_to_map(p, ContextCompat.getColor(this@MainActivity, R.color.dot_of_object))
                    }
                }
                add_point_to_map(point, ContextCompat.getColor(this@MainActivity, R.color.dot_location))
                true
            }
            QuestionType.MEASURING -> {
                val eng_type_str = ObjectTypeTranslator.russian_to_str((tablet?.findViewById<Spinner>(R.id.object_type)?.selectedItem) as String)
                val point = str_to_point(tablet.findViewById<EditText>(R.id.seeker_point).text.toString())
                question = Measuring(
                    ObjectTypeTranslator.str_to_query(eng_type_str),
                    point.x,
                    point.y,
                    global_area
                )
                question.exec_overpass()
                val point_arr = question.target_points
                for (p in point_arr) {
                    add_point_to_map(p, ContextCompat.getColor(this@MainActivity, R.color.dot_of_object))
                }
                add_point_to_map(point, ContextCompat.getColor(this@MainActivity, R.color.dot_location))
                true
            }
            QuestionType.TENTACLES -> {
                val eng_type_str = ObjectTypeTranslator.russian_to_str((tablet?.findViewById<Spinner>(R.id.object_type)?.selectedItem) as String)
                val point = str_to_point(tablet.findViewById<EditText>(R.id.seeker_point).text.toString())
                val distance = str_to_double(tablet.findViewById<EditText>(R.id.distance).text.toString())
                question = Tentacles(
                    ObjectTypeTranslator.str_to_query(eng_type_str),
                    point.x,
                    point.y,
                    distance
                )
                question.exec_overpass()
                val point_arr = question.point_storage.values
                for (p in point_arr) {
                    add_point_to_map(p, ContextCompat.getColor(this@MainActivity, R.color.dot_of_object))
                }
                add_point_to_map(point, ContextCompat.getColor(this@MainActivity, R.color.dot_location))
                true
            }
            else -> false
        }
        if (question != null && tablet != null) {
            question_object_list[int_to_string_id(list_id)] = question
            renew_buttons_enability(tablet)
        }
    }




    // создание и отображение областей
    private fun create_show_areas(question_type: QuestionType?, list_id: Int) {
        if (question_type == null) {throw Exception("create_show_areas() : No question type")}
        val str_id = int_to_string_id(list_id)
        val tablet = question_tablet_list[str_id]
        val color_border = ContextCompat.getColor(this@MainActivity, R.color.question_areas_border)
        val color_background = ContextCompat.getColor(this@MainActivity, R.color.question_areas_background)
        // TODO: сделать создание и отображение объектов
        if (tablet != null) {
            when (question_type) {
                QuestionType.MATCHING -> {
                    // создать области и сгенерировать ответ
                    val question = question_object_list[str_id] as Matching
                    val point = str_to_point(tablet.findViewById<EditText>(R.id.seeker_point)?.text.toString())
                    question.create_areas()
                    question.generate_answer(point.x, point.y)
                    // вставить id и имя объекта
                    val ID = question.id_of_interest
                    val name_field = tablet.findViewById<TextView>(R.id.object_name)
                    name_field?.text = question.names[ID]
                    val id_field = tablet.findViewById<EditText>(R.id.object_id)
                    id_field?.text?.clear()
                    id_field?.text?.append(question.id_of_interest.toString())
                    // отобразить области
                    for (area in question.variants.values) {
                        add_multipolygon_to_map(area, color_border, color_background)
                    }
                    true
                }

                QuestionType.MEASURING -> {
                    // создать области и сгенерировать ответ
                    val question = question_object_list[str_id] as Measuring
                    val point = str_to_point(tablet.findViewById<EditText>(R.id.seeker_point)?.text.toString())
                    question.create_areas()
                    question.generate_answer(point.x, point.y)
                    // вставить дистанцию
                    val dist_field = tablet.findViewById<EditText>(R.id.distance)
                    dist_field?.text?.clear()
                    dist_field?.text?.append(question.rad.toString())
                    // отобразить области
                    add_multipolygon_to_map(question.area, color_border, color_background)
                    true
                }

                QuestionType.RADAR -> {
                    // создать области
                    val point = str_to_point(tablet.findViewById<EditText>(R.id.seeker_point)?.text.toString())
                    val distance = str_to_double(tablet.findViewById<EditText>(R.id.distance)?.text.toString())
                    val question = Radar(
                        point.x,
                        point.y,
                        distance
                    )
                    add_point_to_map(
                        point,
                        ContextCompat.getColor(this@MainActivity, R.color.dot_location)
                    )
                    question.create_areas()
                    // отобразить область
                    add_polygon_to_map(question.area, color_border, color_background)
                    // добавить в список вопросов
                    question_object_list[str_id] = question
                    true
                }

                QuestionType.TENTACLES -> {
                    // создать области
                    val question = question_object_list[str_id] as Tentacles
                    question.create_areas()
                    // отобразить области
                    for (area in question.variants.values) {
                        add_multipolygon_to_map(area, color_border, color_background)
                    }
                    true
                }

                QuestionType.THERMOMETER -> {
                    // создать области
                    val point_colder = str_to_point(tablet.findViewById<EditText>(R.id.point_colder)?.text.toString())
                    val point_hotter = str_to_point(tablet.findViewById<EditText>(R.id.point_hotter)?.text.toString())
                    val question = Thermometer(
                        remaining_area,
                        point_colder.x,
                        point_colder.y,
                        point_hotter.x,
                        point_hotter.y
                    )
                    question.create_areas()
                    // отобразить области и точки
                    add_point_to_map(point_colder, ContextCompat.getColor(this@MainActivity, R.color.question_areas_border_colder))
                    add_point_to_map(point_hotter, ContextCompat.getColor(this@MainActivity, R.color.question_areas_border_hotter))
                    add_polygon_to_map(
                        question.colder_area,
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.question_areas_border_colder
                        ),
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.question_areas_background_colder
                        )
                    )
                    add_polygon_to_map(
                        question.hotter_area,
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.question_areas_border_hotter
                        ),
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.question_areas_background_hotter
                        )
                    )
                    // добавить в список вопросов
                    question_object_list[str_id] = question
                    true
                }
            }
            renew_buttons_enability(tablet)
        }
    }




    // генерация ответа
    private fun generate_answer(question_type: QuestionType?, list_id: Int) {
        if (question_type == null) {throw Exception("generate_answer() : No question type")}
        // TODO: сделать генерацию ответа
        when (question_type) {
            QuestionType.MATCHING -> {
                true
            }
            QuestionType.MEASURING -> {
                true
            }
            QuestionType.RADAR -> {
                true
            }
            QuestionType.TENTACLES -> {
                true
            }
            QuestionType.THERMOMETER -> {
                true
            }
        }
    }




    // применение ответа
    private fun apply_answer(question_type: QuestionType?, list_id: Int) {
        if (question_type == null) {throw Exception("apply_answer() : No question type")}
        // TODO: сделать применение ответа
        when (question_type) {
            QuestionType.MATCHING -> {
                true
            }
            QuestionType.MEASURING -> {
                true
            }
            QuestionType.RADAR -> {
                true
            }
            QuestionType.TENTACLES -> {
                true
            }
            QuestionType.THERMOMETER -> {
                true
            }
        }
    }
}
