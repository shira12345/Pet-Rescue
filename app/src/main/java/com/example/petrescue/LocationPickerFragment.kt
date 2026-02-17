package com.example.petrescue

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import org.maplibre.android.maps.MapView

class LocationPickerFragment : Fragment() {

  private lateinit var mapView: MapView

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    mapView = view.findViewById(R.id.mapView)

    mapView.getMapAsync { map ->

      map.setStyle("https://demotiles.maplibre.org/style.json") {

        map.addOnMapClickListener { point ->

          val lat = point.latitude
          val lon = point.longitude

          // Reverse geocode here
          // Return result to previous fragment

          true
        }
      }
    }
  }
}
