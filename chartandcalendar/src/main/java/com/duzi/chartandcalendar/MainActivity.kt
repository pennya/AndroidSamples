package com.duzi.chartandcalendar

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.duzi.chartandcalendar.utils.ColorUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        with(scoreChart) {
            setColor(ColorUtils.getAndroidTestColor(1))
            setIsTransparencyEnabled(true)
            populateWithRandomData()

            setScrollController { newDataOffset ->
                println(newDataOffset.toString())
            }
        }
    }
}
