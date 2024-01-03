package com.example.savespot

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class GetAddressFromLatLng(context: Context,private val latitude:Double, private val longitude:Double) {
    private var coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
    private val geocoder:Geocoder= Geocoder(context, Locale.getDefault())
    private lateinit var mAddressListener: AddressListener
    fun main() = coroutineScope.launch {
        val result = doInBackground()
        onPostExecute(result)
    }


    private suspend fun doInBackground():String= withContext(Dispatchers.IO){
        try {
            val addressList: List<Address>? = geocoder.getFromLocation(latitude,longitude,1)
            if (addressList!=null && addressList.isNotEmpty()){
                val address : Address = addressList[0]
                val sb = StringBuilder()
                for(i in 0..address.maxAddressLineIndex){
                    sb.append(address.getAddressLine(i)).append(" ")
                }
                sb.deleteCharAt(sb.length -1)
                return@withContext sb.toString()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        return@withContext ""
    }

    private fun onPostExecute(resultString: String?){
        if (resultString == null) {
            mAddressListener.onError()
        }
        else {
            mAddressListener.onAddressFound(resultString)
        }
    }

    fun setAddressListener(addressListener:AddressListener){
        mAddressListener=addressListener
    }

    fun getAddress(){
        main()
    }


    interface AddressListener{
        fun onAddressFound(address:String?)
        fun onError()
    }
}