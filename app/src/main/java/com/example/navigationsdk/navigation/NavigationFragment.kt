package com.example.navigationsdk.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.navigationsdk.R
import com.example.navigationsdk.navigation.place.Place
import com.example.navigationsdk.navigation.place.PlaceRenderer
import com.example.navigationsdk.navigation.place.PlacesReader
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager

class NavigationFragment : Fragment(R.layout.fragment_navigation), OnMapReadyCallback {

    private var mapFragment: SupportMapFragment? = SupportMapFragment()
    private val places: List<Place> by lazy {
        PlacesReader(requireContext()).read()
    }
    private var circle: Circle? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mapFragment = parentFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configMap()
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

    private fun configMap() {
        mapFragment?.getMapAsync { googleMap ->
            googleMap.setOnMapLoadedCallback {
                val bounds = LatLngBounds.builder()
                places.forEach { bounds.include(it.latLng) }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))
            }

            addClusteredMarkers(googleMap)
        }
    }

    private fun addClusteredMarkers(googleMap: GoogleMap) {

        val clusterManager = ClusterManager<Place>(requireContext(), googleMap)
        clusterManager.renderer =
            PlaceRenderer(
                requireContext(),
                googleMap,
                clusterManager
            )

        clusterManager.markerCollection.setInfoWindowAdapter(MarkerInfoWindowAdapter(requireContext()))
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
                .fillColor(ContextCompat.getColor(requireContext(), R.color.teal_200))
                .strokeColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
        )
    }
}