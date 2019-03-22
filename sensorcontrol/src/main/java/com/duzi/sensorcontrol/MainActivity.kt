package com.duzi.sensorcontrol

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.duzi.base.plusAssign
import com.duzi.sensorcontrol.R.id.tvSensorType
import com.duzi.sensorcontrol.R.id.tvSensorValue
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), SensorEventListener {

    var manager: SensorManager? = null
    var sensor: Sensor?= null

    val compositeDisposable = CompositeDisposable()
    val proxy: BehaviorSubject<SensorEvent> = BehaviorSubject.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initSensor()
        observe()
        observeInterval()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.also { proxy.onNext(it) }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Nothing
    }

    override fun onResume() {
        super.onResume()
        manager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        manager?.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun initSensor() {
        manager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private fun observe() {
        compositeDisposable += proxy.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                tvSensorType.text = it.sensor.name
                tvSensorValue.text = "${it.values[0]} ${it.values[1]} ${it.values[2]}"
            }, { e -> e.printStackTrace() })
    }

    private fun observeInterval() {
        compositeDisposable += Observable.interval(5000, TimeUnit.MILLISECONDS)
            .map{ proxy.value }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe {
                Toast.makeText(this@MainActivity, it?.sensor?.name, Toast.LENGTH_SHORT).show()
            }
    }
}
