package com.example.gochickengo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class LocationFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null

    private val defaultLocation = LatLng(31.7683, 35.2137) // Jerusalem default

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mapView = MapView(requireContext())
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        return mapView
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        showLocation(
            latitude = defaultLocation.latitude,
            longitude = defaultLocation.longitude,
            title = "Default Location"
        )
    }

    fun showLocation(latitude: Double, longitude: Double, title: String) {
        if (latitude == 0.0 && longitude == 0.0) {
            return
        }

        val location = LatLng(latitude, longitude)

        googleMap?.clear()

        googleMap?.addMarker(
            MarkerOptions()
                .position(location)
                .title(title)
        )

        googleMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(location, 15f)
        )
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}