package com.example.navigationsdk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.navigationsdk.navigation.MarkerInfoWindowAdapter
import com.example.navigationsdk.navigation.place.Place
import com.example.navigationsdk.navigation.place.PlaceRenderer
import com.example.navigationsdk.navigation.place.PlacesReader
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val sydney = LatLng(0.0, 0.0)
        googleMap.addMarker(
            MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney")
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}
