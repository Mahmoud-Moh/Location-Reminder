package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.*
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import kotlin.properties.Delegates


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    var locationRequest: LocationRequest? = null
    var latitude_local by Delegates.notNull<Double>()
    var longtitude_local by Delegates.notNull<Double>()
    var loc_name_local = "myLocation"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        locationRequest = LocationRequest.create()
        locationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest!!.setInterval(5000)
        locationRequest!!.setFastestInterval(2000)

        binding.saveReminder.setOnClickListener{
            onLocationSelected()
        }

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        val mapFragment = childFragmentManager
            .findFragmentByTag(getString(R.string.map_fragment)) as? SupportMapFragment

        mapFragment!!.getMapAsync(this)
        getCurrentLocation()

        return binding.root
    }

    private fun onLocationSelected() {
        _viewModel.reminderSelectedLocationStr.value = loc_name_local
        _viewModel.latitude.value = latitude_local
        _viewModel.longitude.value = longtitude_local
        _viewModel.navigationCommand.postValue(NavigationCommand.Back)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            true
        }
        R.id.hybrid_map -> {
            true
        }
        R.id.satellite_map -> {
            true
        }
        R.id.terrain_map -> {
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap


        val markerOptions = MarkerOptions()
            .position(map.cameraPosition.target)
            .title(getString(R.string.dropped_pin))
            .draggable(true)

        val circleOptions = CircleOptions()
            .center(map.cameraPosition.target)
            .fillColor(ResourcesCompat.getColor(resources, R.color.colorAccent, null))
            .strokeColor(ResourcesCompat.getColor(resources, R.color.colorPrimary, null))
            .strokeWidth(4f)
            .radius(20.0)

        map.addMarker(markerOptions)
        map.addCircle(circleOptions)
        setMaplongClick(this.map)
        setPOIClick(this.map)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
         grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    getCurrentLocation()
                } else {
                    turnOnGPS()
                }
            }
        }
    }

     override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
         data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                getCurrentLocation()
            }
        }
    }

    private fun getCurrentLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (isGPSEnabled()) {
                    LocationServices.getFusedLocationProviderClient(requireContext())
                        .requestLocationUpdates(locationRequest, object : LocationCallback() {
                            override fun onLocationResult(@NonNull locationResult: LocationResult) {
                                super.onLocationResult(locationResult)
                                LocationServices.getFusedLocationProviderClient(requireContext())
                                    .removeLocationUpdates(this)
                                if (locationResult != null && locationResult.locations.size > 0) {
                                    val index = locationResult.locations.size - 1
                                    val latitude = locationResult.locations[index].latitude
                                    val longitude = locationResult.locations[index].longitude
/*
                                    _viewModel.longitude.value = longitude
                                    _viewModel.latitude.value = latitude
                                    _viewModel.reminderSelectedLocationStr.value = "myLocation"
*/
                                    latitude_local = latitude
                                    longtitude_local = longitude
                                    val homeLatLng = LatLng(latitude, longitude)
                                    map.addMarker(MarkerOptions().position(homeLatLng).title("Marker in your location"))
                                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, 15f))
                                }
                            }
                        }, Looper.getMainLooper())
                } else {
                    turnOnGPS()
                }
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }
    }

    private fun turnOnGPS() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!)
        builder.setAlwaysShow(true)
        val result: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(requireContext())
                .checkLocationSettings(builder.build())
        result.addOnCompleteListener(OnCompleteListener<LocationSettingsResponse?> { task ->
            try {
                val response: LocationSettingsResponse? = task.getResult(ApiException::class.java)
                Toast.makeText(requireContext(), "GPS is already tured on", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: ApiException) {
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvableApiException = e as ResolvableApiException
                        resolvableApiException.startResolutionForResult(requireActivity(), 2)
                    } catch (ex: SendIntentException) {
                        ex.printStackTrace()
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                }
            }
        })
    }

    private fun isGPSEnabled(): Boolean {
        var locationManager: LocationManager? = null
        var isEnabled = false
        if (locationManager == null) {
            locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        }
        isEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return isEnabled
    }

    fun setMaplongClick(map: GoogleMap){
        map.setOnMapLongClickListener {
                latLng ->
            val circleOptions = CircleOptions()
                .center(latLng)
                .fillColor(ResourcesCompat.getColor(resources, R.color.colorAccent, null))
                .strokeColor(ResourcesCompat.getColor(resources, R.color.colorPrimary, null))
                .strokeWidth(4f)
                .radius(20.0)
/*
            _viewModel.longitude.value = latLng.longitude
            _viewModel.latitude.value = latLng.latitude
*/
            latitude_local = latLng.latitude
            longtitude_local = latLng.longitude
            map.addCircle(circleOptions)
         //   map.addMarker(MarkerOptions().position(latLng).title("Marker in your location"))
        }
    }
    fun setPOIClick(map: GoogleMap){
        map.setOnPoiClickListener {
                Poi ->
            val circleOptions = CircleOptions()
                .center(Poi.latLng)
                .fillColor(ResourcesCompat.getColor(resources, R.color.colorAccent, null))
                .strokeColor(ResourcesCompat.getColor(resources, R.color.colorPrimary, null))
                .strokeWidth(4f)
                .radius(20.0)
           /* _viewModel.longitude.value = Poi.latLng.longitude
            _viewModel.latitude.value = Poi.latLng.latitude*/
            latitude_local = Poi.latLng.latitude
            longtitude_local = Poi.latLng.longitude
            loc_name_local = Poi.name
            map.addCircle(circleOptions)
            _viewModel.reminderSelectedLocationStr.value = Poi.name.toString()

         //   map.addMarker(MarkerOptions().position(latLng).title("Marker in your location"))
        }
    }


}
