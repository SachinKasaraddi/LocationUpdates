package com.location.update

import android.Manifest
import android.app.Application
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationProvider
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.fragment_map.*


class MapFragment : Fragment(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    companion object {
        val TAG = MapFragment::class.java.name
    }
    private var googleApiClient: GoogleApiClient? = null
    private var locationRequest: LocationRequest? = null
    private var mapFragmentViewModel: MapFragmentViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectToGoogleApiClient()
    }
    private fun connectToGoogleApiClient() {
        googleApiClient = activity?.let {
            GoogleApiClient.Builder(it)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build()
        }
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(1000)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)
        mapFragmentViewModel = ViewModelProviders.of(this).get(MapFragmentViewModel::class.java)
        mapFragmentViewModel?.currentLocation?.observe(this, Observer {
            txt_lat_long.text = "Lat :" + it?.latitude + "Longitude:" + it?.longitude
        })
        return rootView
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.d(TAG, "onConnectionFailed")
    }

    override fun onConnected(bundle: Bundle?) {
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) && Build.VERSION.SDK_INT >= 23) {
            val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(activity!!, permissions, 1000)
        } else {
            val location = LocationServices.getFusedLocationProviderClient(activity!!)
            location.requestLocationUpdates(locationRequest,object : LocationCallback() {
                override fun onLocationAvailability(p0: LocationAvailability?) {

                }
                override fun onLocationResult(p0: LocationResult?) {
                    p0?.lastLocation?.let {
                        Log.e("changed the location", "values" + it.longitude + " " + it.latitude)
                        it.let { it -> mapFragmentViewModel?.updateLocation(it) }
                    }
                }

            },null)
            location.lastLocation.addOnSuccessListener {

            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1000 -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    connectToGoogleApiClient()
                } else {
                    Toast.makeText(activity, "Cannot get Location Updates", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onResume() {
        super.onResume()
        googleApiClient?.connect()
    }


    override fun onPause() {
        super.onPause()
        googleApiClient?.isConnected?.let { if (it) googleApiClient?.disconnect() }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity!!, permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onLocationChanged(p0: Location?) {
        p0?.let { mapFragmentViewModel?.updateLocation(p0) }
    }


}