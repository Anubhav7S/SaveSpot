package com.example.savespot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.savespot.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var binding:ActivityMainBinding?=null

    override fun onCreate(savedInstanceState: Bundle?) {
       // val fabAddHappyPlace:FloatingActionButton=findViewById(R.id.fabAddHappyPlace)
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        binding?.fabAddHappyPlace?.setOnClickListener {
            val intent=Intent(this, AddHappyPlaceActivity::class.java)
            startActivity(intent)
        }
        getSavedSpotListFromLocalDB()
    }

    private fun getSavedSpotListFromLocalDB(){
        val dao = (application as SaveSpotApp).db.saveSpotDao()
        printPlace(dao)
    }

    private fun printPlace(saveSpotDao: SaveSpotDao){
        lifecycleScope.launch{
            saveSpotDao.fetchAllPlace().collect { allSpots ->
                if (allSpots.isNotEmpty()) {
                    binding?.rvSpotsList?.visibility= View.VISIBLE
                    binding?.tvNoRecordsFound?.visibility=View.GONE
                    val items=ArrayList<SaveSpotEntity>()
                    for (i in allSpots){
                        Toast.makeText(this@MainActivity, "$i", Toast.LENGTH_LONG).show()
                        items.add(i)

                    }
                    binding?.rvSpotsList?.layoutManager=LinearLayoutManager(this@MainActivity)
                    val adapter=Adapter(this@MainActivity,items,{deleteID -> delete(deleteID,saveSpotDao,items)})
                    binding?.rvSpotsList?.adapter=adapter
                    adapter.setOnClickListener(object : Adapter.OnClickListener{
                        override fun onClick(position: Int, entity: SaveSpotEntity) {
                            val intent=Intent(this@MainActivity,SpotDetailActivity::class.java)
                            intent.putExtra(EXTRA_PLACE_DETAILS,entity)
                            startActivity(intent)
                        }
                    })
                    val editSwipeHandler=object:SwipeToEditCallback(this@MainActivity){
                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            val newAdapter=binding?.rvSpotsList?.adapter as Adapter
                            newAdapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition,
                                ADD_PLACE_ACTIVITY_REQUEST_CODE)
                        }
                    }

                    val editItemTouchHelper=ItemTouchHelper(editSwipeHandler)
                    editItemTouchHelper.attachToRecyclerView(binding?.rvSpotsList)

                    val deleteSwipeHandler=object:SwipeToDeleteCallback(this@MainActivity){
                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            val newAdapterDelete=binding?.rvSpotsList?.adapter as Adapter
                            newAdapterDelete.removeAt(viewHolder.adapterPosition)
                        }
                    }

                    val deleteItemTouchHelper=ItemTouchHelper(deleteSwipeHandler)
                    deleteItemTouchHelper.attachToRecyclerView(binding?.rvSpotsList)
                }
                else {
//                    Toast.makeText(this@MainActivity, "FAILED", Toast.LENGTH_LONG).show()
                    binding?.rvSpotsList?.visibility= View.GONE
                    binding?.tvNoRecordsFound?.visibility=View.VISIBLE
                }
            }
        }
        //finish()
    }

    private fun delete(id:Int,saveSpotDao: SaveSpotDao,items:ArrayList<SaveSpotEntity>){

        lifecycleScope.launch {
            for (i in items){
               // items.removeAt(id)
                if (i.id==id){
                    saveSpotDao.delete(saveSpotEntity = i)
                }
            }
            //saveSpotDao.delete(SaveSpotEntity(id))

            Toast.makeText(applicationContext, "Record deleted successfully.", Toast.LENGTH_LONG).show()
        }
    }

//    private fun delete2(id:Int,placesDao: SaveSpotDao){
//        lifecycleScope.launch {
//           // placesDao.delete(SaveSpotEntity(id))
//            Toast.makeText(applicationContext, "Record deleted successfully.", Toast.LENGTH_LONG).show()
//        }
//    }

//    private fun setUpRecyclerView(saveSpotDao: SaveSpotDao){
//        lifecycleScope.launch{
//            saveSpotDao.fetchAllPlace().collect { allSpots ->
//
//                if (allSpots.isNotEmpty()) {
//                    val items=ArrayList<SaveSpotEntity>()
//                    for (item in allSpots){
//                        items.add(item)
//                        //Toast.makeText(this@MainActivity, "$i", Toast.LENGTH_LONG).show()
//                    }
//                    val adapter=Adapter(items)
//                    binding?.rvSpotsList?.adapter=adapter
//
//                }
//                else {
//                    Toast.makeText(this@MainActivity, "FAILED", Toast.LENGTH_LONG).show()
//                }
//            }
//        }
//    }

    companion object{
        var ADD_PLACE_ACTIVITY_REQUEST_CODE=1
        var EXTRA_PLACE_DETAILS="extra_place_details"
    }

    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }
}