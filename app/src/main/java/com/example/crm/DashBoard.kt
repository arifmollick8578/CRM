package com.example.crm

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.crm.databinding.ActivityDashBoardBinding
import com.example.crm.model.UserLocation
import com.example.crm.pending.PendingViewModel
import com.example.crm.permission.CheckPermission
import com.example.crm.permission.CheckPermission.Companion.LOCATION_PERMISSION_CODE
import com.example.crm.permission.CheckPermission.Companion.requestLocationPermissions
import com.example.crm.permission.CheckPermission.Companion.showDialogToAccessGps
import com.example.crm.preferences.IPreferenceHelper
import com.example.crm.preferences.PreferenceManager
import com.example.crm.utility.CurrentDateTime
import com.example.crm.worker.LocationWorker
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import java.time.Duration

class DashBoard : AppCompatActivity(), LocationListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDashBoardBinding
    private lateinit var pendingViewModel: PendingViewModel
    private val preferenceHelper: IPreferenceHelper by lazy { PreferenceManager(applicationContext) }
    private var locationWorker = WorkManager.getInstance(this)
    private lateinit var locationManager: LocationManager
    private lateinit var connectivityManager: ConnectivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarDashBoard.toolbar)
        pendingViewModel = ViewModelProvider(this)[PendingViewModel::class.java].apply {
            allLocationData.observe(this@DashBoard) {
                // Will implement worker
//                startWorker(it)
            }
            currentLocation.observeForever {
                Snackbar
                    .make(binding.root, "Location updated: ${it.latitude}", Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_dash_board)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_pending, R.id.nav_nearby, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_pending -> {
                    Toast.makeText(this@DashBoard, "You tapped pending", Toast.LENGTH_LONG)
                        .show()
                }
                else -> Toast.makeText(this@DashBoard, "You tapped else", Toast.LENGTH_LONG)
                    .show()
            }
        }

        navView.getHeaderView(0).findViewById<TextView>(R.id.user_name).text =
            preferenceHelper.getUserId()
        navView.getHeaderView(0).findViewById<TextView>(R.id.imei_number).text =
            preferenceHelper.getImeiNo()

        saveLocationData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.dash_board, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            else -> {
                // NO OP
            }
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_dash_board)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun getUserLocation(latitude: Double, longitude: Double): UserLocation {
        return UserLocation(
            CurrentDateTime.currentTime,
            appId = preferenceHelper.getImeiNo(),
            userId = preferenceHelper.getUserId(),
            latitude = latitude,
            longitude = longitude
        )
    }

    private fun saveLocationData() {
        Log.d("TAG", "Location save starting.")
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        if (!CheckPermission.checkLocationPermissions(this)) requestLocationPermissions()
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            this.showDialogToAccessGps()
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            10000,
            10f,
            this@DashBoard
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                this.showDialogToAccessGps()
            }
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                this@DashBoard
            )
        }
    }

    private fun startWorker(userLocation: List<UserLocation>) {
        if (userLocation.isEmpty()) {
            Log.d("BugInfo", "No pending location to post.")
            return
        }
        Log.d("BugInfo", "Start worker called.")
        val workRequest =
            PeriodicWorkRequest.Builder(LocationWorker::class.java, Duration.ofMinutes(15))
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                ).build()
        locationWorker.enqueue(
            workRequest
        )
        locationWorker.getWorkInfoByIdLiveData(workRequest.id).observe(this) {
            Log.d("BugInfo", "Observer called worker. $it")
            pendingViewModel.startPostingLocationData()
        }
    }

    override fun onProviderDisabled(provider: String) {
        Snackbar.make(binding.root, "Please turn on location.", Snackbar.LENGTH_LONG).show()
        this.showDialogToAccessGps()
    }

    override fun onProviderEnabled(provider: String) {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this@DashBoard)
    }

    override fun onLocationChanged(location: Location) {
        Log.d("TAG", "DraftActivity: locationListener: ${location.latitude}")
        pendingViewModel.updateCurrentLocation(location)
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        pendingViewModel.insertUserLocation(getUserLocation(location.latitude, location.longitude))

        if (connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnected) {
            pendingViewModel.startPostingLocationData()
            Snackbar.make(binding.root, "Location data posted to API", Snackbar.LENGTH_LONG).show()
        }
    }
}