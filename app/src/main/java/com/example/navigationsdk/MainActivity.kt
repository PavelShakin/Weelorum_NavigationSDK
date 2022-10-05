package com.example.navigationsdk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.navigationsdk.place.Place
import com.example.navigationsdk.place.PlaceRenderer
import com.example.navigationsdk.place.PlacesReader
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val places: List<Place> by lazy {
        PlacesReader(this).read()
    }
    private var circle: Circle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync{ googleMap ->
            googleMap.setOnMapLoadedCallback {
                val bounds = LatLngBounds.builder()
                places.forEach { bounds.include(it.latLng) }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))
            }

            addClusteredMarkers(googleMap)
        }

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

    private fun addClusteredMarkers(googleMap: GoogleMap) {

        val clusterManager = ClusterManager<Place>(this, googleMap)
        clusterManager.renderer =
            PlaceRenderer(
                this,
                googleMap,
                clusterManager
            )

        clusterManager.markerCollection.setInfoWindowAdapter(MarkerInfoWindowAdapter(this))
        clusterManager.addItems(places)
        clusterManager.cluster()

        clusterManager.setOnClusterItemClickListener { item ->
            addCircle(googleMap, item)
            return@setOnClusterItemClickListener false
        }

        googleMap.setOnCameraMoveStartedListener {
            clusterManager.markerCollection.markers.forEach { it.alpha = 0.3f }
            clusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 0.3f }
        }

        googleMap.setOnCameraIdleListener {
            clusterManager.markerCollection.markers.forEach { it.alpha = 1.0f }
            clusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 1.0f }
            clusterManager.onCameraIdle()
        }
    }

    private fun addCircle(googleMap: GoogleMap, item: Place) {
        circle?.remove()
        circle = googleMap.addCircle(
            CircleOptions()
                .center(item.latLng)
                .radius(1000.0)
                .fillColor(ContextCompat.getColor(this, R.color.teal_200))
                .strokeColor(ContextCompat.getColor(this, R.color.purple_200))
        )
    }
}
