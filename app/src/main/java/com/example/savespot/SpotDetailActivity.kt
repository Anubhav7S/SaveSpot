package com.example.savespot

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.savespot.databinding.ActivitySpotDetailBinding

class SpotDetailActivity : AppCompatActivity() {
    private var binding:ActivitySpotDetailBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySpotDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        var spotEntityDetails:SaveSpotEntity?=null
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            spotEntityDetails=intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as SaveSpotEntity
        }
        if (spotEntityDetails!=null){
            setSupportActionBar(binding?.toolbarSpotDetail)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title=spotEntityDetails.title
            binding?.toolbarSpotDetail?.setNavigationOnClickListener {
                onBackPressed()
            }
            binding?.ivPlaceImage?.setImageURI(Uri.parse(spotEntityDetails.image))
            binding?.tvDescription?.text=spotEntityDetails.description
            binding?.tvLocation?.text=spotEntityDetails.location
            binding?.btnViewOnMap?.setOnClickListener {
                val intent= Intent(this@SpotDetailActivity,MapActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS,spotEntityDetails)
                startActivity(intent)
            }
        }
    }
}