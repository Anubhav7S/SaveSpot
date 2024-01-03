package com.example.savespot

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationRequest
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.savespot.databinding.ActivityAddHappyPlaceBinding
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddHappyPlaceActivity : AppCompatActivity() , View.OnClickListener {
    private var binding:ActivityAddHappyPlaceBinding?=null
    private var cal=Calendar.getInstance()
    private var saveImageToInternalStorage: Uri? = null
    private var editSpotDetails:SaveSpotEntity?=null
    private var mLatitude:Double=0.0
    private var mLongitude:Double=0.0

    private lateinit var mFusedLocationClient:FusedLocationProviderClient

    private lateinit var dateSetListener:DatePickerDialog.OnDateSetListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setSupportActionBar(binding?.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.toolbarAddPlace?.setNavigationOnClickListener {
            onBackPressed()
        }

        mFusedLocationClient=LocationServices.getFusedLocationProviderClient(this) //To get latitude and longitude

        if(!Places.isInitialized()){
            Places.initialize(this@AddHappyPlaceActivity,resources.getString(R.string.google_maps_api_key))
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            editSpotDetails=intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as SaveSpotEntity
        }

        dateSetListener=DatePickerDialog.OnDateSetListener{view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDateInView()
        }
        updateDateInView()

        if (editSpotDetails!=null){
            supportActionBar?.title="Edit Spot"
            binding?.etTitle?.setText(editSpotDetails!!.title)
            binding?.etDescription?.setText(editSpotDetails!!.description)
            binding?.etDate?.setText(editSpotDetails!!.date)
            binding?.etLocation?.setText(editSpotDetails!!.location)
            mLatitude=editSpotDetails!!.latitude
            mLongitude=editSpotDetails!!.longitude
            saveImageToInternalStorage= Uri.parse(editSpotDetails!!.image) //saveImageToInternalStorage is image's address
            binding?.ivPlaceImage?.setImageURI(saveImageToInternalStorage)
            binding?.btnSave?.text=("UPDATE")
        }
        binding?.etDate?.setOnClickListener(this)
        binding?.tvAddImage?.setOnClickListener(this)
        binding?.btnSave?.setOnClickListener(this)
        binding?.etLocation?.setOnClickListener(this)
        binding?.tvSelectCurrentLocation?.setOnClickListener(this)
    }

    private fun isLocationEnabled():Boolean{
        val locationManager:LocationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    @SuppressLint("missingPermission")
    private fun requestNewLocationData(){
        var mLocationRequest = com.google.android.gms.location.LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).apply {
            setMinUpdateDistanceMeters(0F)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
            setMaxUpdates(1)
        }.build()
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback,Looper.myLooper())

    }

    private val mLocationCallback=object :LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            mLatitude= mLastLocation!!.latitude
            mLongitude = mLastLocation.longitude
            val addressTask=GetAddressFromLatLng(this@AddHappyPlaceActivity,mLatitude,mLongitude)
            addressTask.setAddressListener(object: GetAddressFromLatLng.AddressListener {
                override fun onAddressFound(address: String?) {
                    binding?.etLocation?.setText(address)
                }

                override fun onError() {
                    Log.e("Get address","Something went wrong")
                }
            })
            addressTask.getAddress()
        }
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.et_date ->{
                DatePickerDialog(this@AddHappyPlaceActivity,
                    dateSetListener,cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()
            }
            R.id.tv_add_image ->{
                val pictureDialog=AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems= arrayOf("Select Photo From Gallery","Capture Photo From Camera")
                pictureDialog.setItems(pictureDialogItems){
                    dialog, which->
                    when(which){
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }
            R.id.btn_save ->{
                when{
                    binding?.etTitle?.text.isNullOrEmpty() ->{
                        Toast.makeText(this,"Please enter the title",Toast.LENGTH_SHORT).show()
                    }
                    binding?.etDescription?.text.isNullOrEmpty() ->{
                        Toast.makeText(this,"Please enter the description",Toast.LENGTH_SHORT).show()
                    }
                    binding?.etLocation?.text.isNullOrEmpty() ->{
                        Toast.makeText(this,"Please enter the location",Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalStorage==null ->{
                        Toast.makeText(this,"Please select an image",Toast.LENGTH_SHORT).show()
                    }
                    else->{
                        val dao = (application as SaveSpotApp).db.saveSpotDao()
                        if (editSpotDetails==null){
                        addPlace(dao)
                        finish()
                        }
                        else{
                            //Toast.makeText(this@AddHappyPlaceActivity,"SIIU",Toast.LENGTH_LONG).show()
                            updatePlace(editSpotDetails!!.id,dao)
                            finish()
                        }
//                        addPlace(dao)
//                        finish()
                        //Toast.makeText(this@AddHappyPlaceActivity,"Bonjour",Toast.LENGTH_LONG).show()
                        //printPlace(dao)
                    }
                }

            }
            R.id.et_location->{
                try {
                    val fields = listOf(Place.Field.ID,Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.ADDRESS)
                    val intent=Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,fields).build(this@AddHappyPlaceActivity)
                   // startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
                    googleMapsLauncher.launch(intent)

                }catch (e:Exception){
                    e.printStackTrace()
                    Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.tv_select_current_location->{
                if (!isLocationEnabled()){
                    Toast.makeText(this@AddHappyPlaceActivity,"Location provider is turned off, please turn it on!",Toast.LENGTH_SHORT).show()
                    val intent=Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
                else{
                    Dexter.withContext(this).withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION).withListener(object : MultiplePermissionsListener{
                        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                            if (report!!.areAllPermissionsGranted()){
                                requestNewLocationData()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            showRationaleDialogForPermissions()
                        }

                    }).onSameThread().check()
                }
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode==Activity.RESULT_OK){
            if (requestCode== GALLERY){
                if (data!=null){
                    val contentURI=data.data
                    try {
                        val selectedImageBitmap=MediaStore.Images.Media.getBitmap(this.contentResolver,contentURI)
                        saveImageToInternalStorage=saveImageToInternalStorage(selectedImageBitmap)
                        Toast.makeText(this@AddHappyPlaceActivity,"Path: $saveImageToInternalStorage",Toast.LENGTH_LONG).show()
                        binding?.ivPlaceImage?.setImageBitmap(selectedImageBitmap)
                    }catch (e:IOException){
                        e.printStackTrace()
                        Toast.makeText(this@AddHappyPlaceActivity,"Failed to load the image from the gallery!",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else if (requestCode== CAMERA){
                val thumbNail:Bitmap=data!!.extras!!.get("data") as Bitmap
                saveImageToInternalStorage=saveImageToInternalStorage(thumbNail)
                Toast.makeText(this@AddHappyPlaceActivity,"Path: $saveImageToInternalStorage",Toast.LENGTH_LONG).show()
                binding?.ivPlaceImage?.setImageBitmap(thumbNail)
            }
        }
    }

    private fun takePhotoFromCamera(){
        Dexter.withContext(this).withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.CAMERA).withListener(
            object: MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()){
                        val cameraIntent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(cameraIntent, CAMERA)

                    }
                }
                override fun onPermissionRationaleShouldBeShown(permissions:MutableList<PermissionRequest>?, token:PermissionToken?) {
                    showRationaleDialogForPermissions()
                    token?.continuePermissionRequest()
                }
            }).onSameThread().check()
    }

    private fun choosePhotoFromGallery(){
        Dexter.withContext(this).withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.CAMERA).withListener(
            object: MultiplePermissionsListener{
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()){
                    val galleryIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)

                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions:MutableList<PermissionRequest>?, token:PermissionToken?) {
                showRationaleDialogForPermissions()
                token?.continuePermissionRequest()
            }
                }).onSameThread().check()
    }

    private fun showRationaleDialogForPermissions(){
        AlertDialog.Builder(this).setMessage("Permissions to use the feature have been denied. " +
                "They can be granted in Application Settings.").setPositiveButton("GO TO SETTINGS"){
                    _,_ ->
                        try {
                            val intent=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri=Uri.fromParts("package",packageName,null)
                            intent.data=uri
                            startActivity(intent)
                        }catch (e:ActivityNotFoundException){
                            e.printStackTrace()
                        }
        }.setNegativeButton("Cancel"){dialog,which->
            dialog.dismiss() }.show()
    }

    private fun updateDateInView(){
        val myFormat="dd.MM.yyyy"
        val sdf=SimpleDateFormat(myFormat, Locale.getDefault())
        binding?.etDate?.setText(sdf.format(cal.time).toString())
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap):Uri{
        val wrapper=ContextWrapper(applicationContext) //used to get directory where we are going to store our files.
        var file=wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file=File(file,"${UUID.randomUUID()}.jpg") //uuid=unique user id
        try {
            val stream:OutputStream=FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e:IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    companion object{
        private const val GALLERY=1
        private const val CAMERA=2
        private const val IMAGE_DIRECTORY="FavouriteSpotImages"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE=3
    }

    private fun updatePlace(id:Int,saveSpotDao: SaveSpotDao){
        val title = binding?.etTitle?.text.toString()
        val description = binding?.etDescription?.text.toString()
        val date = binding?.etDate?.text.toString()
        val location = binding?.etLocation?.text.toString()
        val image = saveImageToInternalStorage.toString()
        lifecycleScope.launch {
//            val title = binding?.etTitle?.text.toString()
//            val description = binding?.etDescription?.text.toString()
//            val date = binding?.etDate?.text.toString()
//            val location = binding?.etLocation?.text.toString()
//            val image = saveImageToInternalStorage.toString()
            saveSpotDao.update(SaveSpotEntity(id,title,image,description,date,location, latitude = 0.0, longitude = 0.0))
        }
    }

    private fun addPlace(saveSpotDao: SaveSpotDao) {
        // run on coroutine
        lifecycleScope.launch {
            saveSpotDao.insert(
                SaveSpotEntity(
                    id = 0,
                    title = binding?.etTitle?.text.toString(),
                    image = saveImageToInternalStorage.toString(),
                    description = binding?.etDescription?.text.toString(),
                    location = binding?.etLocation?.text.toString(),
                    date = binding?.etDate?.text.toString(),
                    latitude = mLatitude,
                    longitude = mLongitude
                )
            )


        }
    }

    private fun printPlace(saveSpotDao: SaveSpotDao){
        lifecycleScope.launch{
            saveSpotDao.fetchAllPlace().collect { allCompleted ->
                if (allCompleted.isNotEmpty()) {
                    Toast.makeText(
                        this@AddHappyPlaceActivity,
                        "MISSION SUCCESSFUL",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(this@AddHappyPlaceActivity, "FAILED", Toast.LENGTH_LONG).show()
                }
            }
        }
        finish()
    }

    private val googleMapsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this,"All Well", Toast.LENGTH_LONG).show()

            val data = result.data
            val place : Place = Autocomplete.getPlaceFromIntent(data!!)

            binding?.etLocation?.setText(place.address)
            mLatitude = place.latLng!!.latitude
            mLongitude = place.latLng!!.longitude
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }


}


