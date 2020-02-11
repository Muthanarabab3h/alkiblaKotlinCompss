package com.example.kiblakotlin
 /************************************************
 *                                               *
 *                                               *
 *   This kibla compass is developed By :        *
 *                                               *
 *     MUTHANA A. Rababh                         *
 *    If you have any problem                    *
 *     please contact me on                      *
 *     FaceBooK :                                *
 *       https://web.facebook.com/raba3e123      *
 *                                               *
 *                                               *
 *                                               *
 ************************************************/
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.StringBuilder

class MainActivity : AppCompatActivity(), LocationListener, SensorEventListener {

    private var thereSnsrRotVector = false
    private var rotVector: Sensor? = null
    private var thereSnsrMagmeter = false
    private var thereSnsrAccmeter = false
    private var magmeter: Sensor? = null
    private var accmeter: Sensor? = null
    private var SnsorMngr: SensorManager? = null

    val request_LOQ = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setLocation()

        SnsorMngr = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        startCompass()

    }




    private fun setLocation() {
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this,android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION)
                ,request_LOQ)
        } else {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            val provider = locationManager.getBestProvider(criteria,false)
            val location = locationManager.getLastKnownLocation(provider)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0f,this)

            if (location!= null)
            {
                text_view_loacation.text=convertLocationToString(location.latitude,location.longitude)
            }
            else{
                Toast.makeText(this,"Sorry The Location isn't available :(", Toast.LENGTH_SHORT).show() } } }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode==request_LOQ) setLocation()

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    private fun convertLocationToString(latitude: Double, longitude: Double): String {
        val builder= StringBuilder()
        if (latitude < 0 ) builder.append("S ") else builder.append("N ")

        val latitudeDegrees = Location.convert(Math.abs(latitude), Location.FORMAT_SECONDS)
        val latitudeSplit = latitudeDegrees.split((":").toRegex()).dropLastWhile({it.isEmpty()}).toTypedArray()
        builder.append(latitudeSplit[0])
        builder.append("°")
        builder.append(latitudeSplit[1])
        builder.append("'")
        builder.append(latitudeSplit[2])
        builder.append("\"")
        builder.append("\n")

        if(longitude < 0 ) builder.append("W ") else builder.append("E ")
        val longitudeDegrees = Location.convert(Math.abs(longitude), Location.FORMAT_SECONDS)
        val longitudSplit = longitudeDegrees.split((":").toRegex()).dropLastWhile ({it.isEmpty()}).toTypedArray()
        builder.append(longitudSplit[0])
        builder.append("°")
        builder.append(longitudSplit[1])
        builder.append("'")
        builder.append(longitudSplit[2])
        builder.append("\"")

        return builder.toString() }

    override fun onLocationChanged(location: Location?) {
        setLocation()    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }

    override fun onProviderEnabled(provider: String?) {

    }

    override fun onProviderDisabled(provider: String?) {

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
    private var rotationMatrix = FloatArray(9)
    private var orientation = FloatArray(3)
    private var lastAcceslometer = FloatArray(3)
    private var lastMagnetometer = FloatArray(3)
    private var azimuth: Int = 0
    private var lastAcceslometerSet = false
    private var lastMagnetometerSet = false
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR)
        {
            SensorManager.getRotationMatrixFromVector(rotationMatrix,event.values)
            azimuth = (Math.toDegrees(SensorManager.getOrientation(rotationMatrix,orientation)[0].toDouble())+360).toInt()%360
        }
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER){
            System.arraycopy(event.values,0,lastAcceslometer,0,event.values.size)
            lastAcceslometerSet = true
        }
        else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD){
            System.arraycopy(event.values,0,lastMagnetometer,0,event.values.size)
            lastMagnetometerSet = true
        }
        if(lastAcceslometerSet && lastMagnetometerSet){
            SensorManager.getRotationMatrix(rotationMatrix,null,lastAcceslometer,lastMagnetometer)
            SensorManager.getOrientation(rotationMatrix,orientation)
            azimuth=(Math.toDegrees(SensorManager.getOrientation(rotationMatrix,orientation)[0].toDouble())+360).toInt()%360
        }
        azimuth = Math.round(azimuth.toFloat())
        comps_image.rotation = (-azimuth).toFloat()
        val where = when(azimuth){
            in 281..349 -> "NW"
            in 261..280 -> "w"
            in 191..260 -> "SW"
            in 171..190 -> "S"
            in 101..170 -> "SE"
            in 81..100 -> "E"
            in 11..80 -> "NE"
            else -> "N"
        }
        text_view_geree.text="$azimuth° $where"
    }

    private  fun startCompass(){

        if(SnsorMngr!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)==null){
            if(SnsorMngr!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)==null
                || SnsorMngr!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null){
                noSensorAlert()

            }else{
                accmeter = SnsorMngr!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                magmeter = SnsorMngr!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                thereSnsrAccmeter = SnsorMngr!!.registerListener(this,accmeter,
                    SensorManager.SENSOR_DELAY_UI)
                thereSnsrMagmeter = SnsorMngr!!.registerListener(this,magmeter,
                    SensorManager.SENSOR_DELAY_UI)
            }
        }else{
            rotVector = SnsorMngr!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            thereSnsrRotVector = SnsorMngr!!.registerListener(this,rotVector,
                SensorManager.SENSOR_DELAY_UI)

        }


    }
    private fun stopCompass(){

        if(thereSnsrRotVector)SnsorMngr!!.unregisterListener(this,rotVector)
        if(thereSnsrAccmeter)SnsorMngr!!.unregisterListener(this,accmeter)
        if(thereSnsrMagmeter)SnsorMngr!!.unregisterListener(this,magmeter)
    }
    private fun noSensorAlert() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setMessage("Sorry Your Device doesn't support our compass :( ")
            .setCancelable(false)
            .setNegativeButton("Exit"){_,_ -> finish()}
        alertDialog.show()

    }

    override fun onResume() {
        super.onResume()
        startCompass()
    }

    override fun onPause() {
        super.onPause()
        stopCompass()
    }
}
