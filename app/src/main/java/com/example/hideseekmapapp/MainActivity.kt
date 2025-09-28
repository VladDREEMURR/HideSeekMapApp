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

import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.geometry.Polygon

// TODO: сделать рабочей область вопросов и настроек (возможно, отребуются отдельные классы для управления этими категориями)
// TODO: протестировать зарисовку областей на карте через OverpassProcessor
// TODO: сделать overpass запрос в отдельных файлах для каждого вопроса

class MainActivity : ComponentActivity() {
    // режим приложения
    private var mode : String = "seeker" // "seeker" или "hider"

    private lateinit var map_view : MapView
    private lateinit var button_orientation_north : Button
    private lateinit var button_zoom_area : Button
    private lateinit var button_questions : Button
    private lateinit var button_settings : Button
    private lateinit var layout_questions : LinearLayout
    private lateinit var layout_settings : GridLayout

    private var questons_shown : Boolean = false
    private var settings_shown : Boolean = false

    private var overpass_processor : OverpassProcessor = OverpassProcessor()

    // для тестирования
    private lateinit var test_output_block : TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // карты подготовка
        MapKitFactory.setApiKey("60b6e681-e142-4dd6-8f98-73996515ab97")
        MapKitFactory.initialize(this@MainActivity)

        // подготовка интерфейса
        prepare_interface()

        // блок тестового вывода
        test_output_block = findViewById(R.id.test_output)

        // some code
        test_output_block.text = overpass_processor.testOverpass()
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
        button_orientation_north.setOnClickListener {
            val current_map_position = map_view.map.cameraPosition.target
            val current_zoom = map_view.map.cameraPosition.zoom
            map_view.map.move(
                CameraPosition(current_map_position, current_zoom, 0.0f, 0.0f)
            )
        }
        button_zoom_area.setOnClickListener {
            // TODO: сделать приближение/отдаление по оставшейся области поиска
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
