package ca.gbc.comp3074.sensorlab

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager

    // nullable bc getDefaultSensor() gives null if the device doesn't have that sensor
    private var temperatureSensor: Sensor? = null
    private var gyroscopeSensor: Sensor? = null

    // mutableStateOf so Compose redraws every time a new reading comes in
    private var temperature by mutableStateOf<Float?>(null)
    private var gyroX by mutableStateOf(0f)
    private var gyroY by mutableStateOf(0f)
    private var gyroZ by mutableStateOf(0f)

    private var temperatureAvailable by mutableStateOf(false)
    private var gyroscopeAvailable by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        temperatureAvailable = temperatureSensor != null
        gyroscopeAvailable = gyroscopeSensor != null

        setContent {
            MaterialTheme {
                SensorScreen(
                    temperature = temperature,
                    temperatureSensor = temperatureSensor,
                    temperatureAvailable = temperatureAvailable,
                    gyroX = gyroX,
                    gyroY = gyroY,
                    gyroZ = gyroZ,
                    gyroscopeSensor = gyroscopeSensor,
                    gyroscopeAvailable = gyroscopeAvailable
                )
            }
        }
    }

    // only listen while the screen is in front, no point burning battery in the background
    override fun onResume() {
        super.onResume()
        temperatureSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscopeSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        // one call drops both sensors since they share this same listener
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        // same callback for both sensors, so check which one fired
        when (event.sensor.type) {
            Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                temperature = event.values[0]
            }
            Sensor.TYPE_GYROSCOPE -> {
                gyroX = event.values[0]
                gyroY = event.values[1]
                gyroZ = event.values[2]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // not needed for this lab
    }
}

@Composable
fun SensorScreen(
    temperature: Float?,
    temperatureSensor: Sensor?,
    temperatureAvailable: Boolean,
    gyroX: Float,
    gyroY: Float,
    gyroZ: Float,
    gyroscopeSensor: Sensor?,
    gyroscopeAvailable: Boolean
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Android Sensor Monitor",
                style = MaterialTheme.typography.headlineMedium
            )
            TemperatureCard(
                temperature = temperature,
                sensor = temperatureSensor,
                available = temperatureAvailable
            )
            GyroscopeCard(
                x = gyroX,
                y = gyroY,
                z = gyroZ,
                sensor = gyroscopeSensor,
                available = gyroscopeAvailable
            )
        }
    }
}

@Composable
fun TemperatureCard(
    temperature: Float?,
    sensor: Sensor?,
    available: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Ambient Temperature",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))

            if (!available || sensor == null) {
                Text("Temperature sensor is not available.")
            } else {
                if (temperature == null) {
                    Text("Waiting for sensor reading...")
                } else {
                    Text(
                        text = "%.1f °C".format(temperature),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                SensorInfo(sensor = sensor, unit = "°C")
            }
        }
    }
}

@Composable
fun GyroscopeCard(
    x: Float,
    y: Float,
    z: Float,
    sensor: Sensor?,
    available: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Gyroscope",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))

            if (!available || sensor == null) {
                Text("Gyroscope is not available.")
            } else {
                // rad/s = how fast it's spinning, not the angle it's at
                Text("X: %.3f rad/s".format(x))
                Text("Y: %.3f rad/s".format(y))
                Text("Z: %.3f rad/s".format(z))
                SensorInfo(sensor = sensor, unit = "rad/s")
            }
        }
    }
}

// extension: hardware details pulled straight off the Sensor object
@Composable
fun SensorInfo(sensor: Sensor, unit: String) {
    Spacer(Modifier.height(8.dp))
    Text("Name: ${sensor.name}", style = MaterialTheme.typography.bodySmall)
    Text("Vendor: ${sensor.vendor}", style = MaterialTheme.typography.bodySmall)
    Text("Max range: ${sensor.maximumRange} $unit", style = MaterialTheme.typography.bodySmall)
    Text("Resolution: ${sensor.resolution} $unit", style = MaterialTheme.typography.bodySmall)
}
