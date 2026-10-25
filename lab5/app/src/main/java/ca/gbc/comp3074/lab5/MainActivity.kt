package ca.gbc.comp3074.lab5

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.location.LocationListenerCompat
import ca.gbc.comp3074.lab5.ui.theme.Lab5Theme

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab5Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LocationScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LocationScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(hasFineLocation(context)) }
    var location by remember { mutableStateOf<Location?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        // GPS needs FINE. if they only allow "approximate" I still can't use it
        hasPermission = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    // ask right away the first time the screen shows
    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(LOCATION_PERMISSIONS)
    }

    if (hasPermission) {
        // start GPS once allowed, stop it when the screen goes away so it doesn't drain battery
        DisposableEffect(Unit) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            // Compat version: a plain LocationListener lambda crashes on Android 10 and lower
            val listener = LocationListenerCompat { location = it }
            location = startGpsUpdates(locationManager, listener)
            onDispose { locationManager.removeUpdates(listener) }
        }
    }

    val current = location
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("My GPS Location", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        when {
            !hasPermission -> {
                Text("Precise location permission is needed to use GPS.")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { permissionLauncher.launch(LOCATION_PERMISSIONS) }) {
                    Text("Grant permission")
                }
            }
            current == null -> Text("Waiting for GPS fix...")
            else -> {
                Text("Latitude: ${current.latitude}", style = MaterialTheme.typography.titleLarge)
                Text("Longitude: ${current.longitude}", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

private fun hasFineLocation(context: Context) =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

// lint can't tell this only runs after the permission check in LocationScreen, so I mute it here
@SuppressLint("MissingPermission")
private fun startGpsUpdates(locationManager: LocationManager, listener: LocationListener): Location? {
    // every 1s, any distance moved. called from the main thread so updates come back there too
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, listener)
    // last known fix so the screen isn't blank while the first update comes in
    return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
}