package com.example.savespot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.savespot.databinding.ActivityMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity() , OnMapReadyCallback {
    private var binding:ActivityMapBinding?=null
    private var mSpotDetail:SaveSpotEntity?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mSpotDetail=intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as SaveSpotEntity
        }
        if (mSpotDetail!=null){
            setSupportActionBar(binding?.toolbarMap)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title=mSpotDetail!!.title
            binding?.toolbarMap?.setNavigationOnClickListener {
                onBackPressed()
            }
            val supportMapFragment:SupportMapFragment= supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            supportMapFragment.getMapAsync(this)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val position=LatLng(mSpotDetail!!.latitude,mSpotDetail!!.longitude)
        googleMap.addMarker(MarkerOptions().position(position).title(mSpotDetail!!.location))
        val latlngZoom=CameraUpdateFactory.newLatLngZoom(position,15f)
        googleMap.animateCamera(latlngZoom)
    }
}