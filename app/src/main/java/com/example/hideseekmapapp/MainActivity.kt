package com.example.hideseekmapapp



import android.content.res.TypedArray
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView



class MainActivity : ComponentActivity() {
    // режим приложения
    private var mode : String = "seeker" // "seeker" или "hider"

    private lateinit var mapView : MapView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("60b6e681-e142-4dd6-8f98-73996515ab97")
        MapKitFactory.initialize(this@MainActivity)
        setContentView(R.layout.activity_main_hider)
        mapView = findViewById(R.id.map_view)
    }


    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }


    override fun onStop() {
        super.onStop()
        MapKitFactory.getInstance().onStop()
        mapView.onStop()
    }
}
