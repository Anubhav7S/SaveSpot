package com.example.savespot

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.savespot.databinding.ItemSavedSpotBinding

class Adapter(private val context:Context,private val spots:ArrayList<SaveSpotEntity>, private val deleteListener:(id:Int)->Unit):RecyclerView.Adapter<Adapter.ViewHolder>() {

    private var onClickListener:OnClickListener?=null

    class ViewHolder(binding:ItemSavedSpotBinding):RecyclerView.ViewHolder(binding.root){
        val tvTitle=binding.tvTitle
        val tvDesc=binding.tvDescription
        val ivPlaceImage=binding.ivPlaceImage
    }

    interface OnClickListener{
        fun onClick(position: Int, entity: SaveSpotEntity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSavedSpotBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener=onClickListener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=spots[position]
        holder.tvTitle.text=item.title
        holder.tvDesc.text=item.description
        holder.ivPlaceImage.setImageURI(Uri.parse(item.image))
        holder.itemView.setOnClickListener {
            if (onClickListener!=null){
                onClickListener!!.onClick(position,item)
            }
        }
    }

    fun notifyEditItem(activity:Activity,position: Int,requestCode:Int){
        val intent= Intent(context,AddHappyPlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS,spots[position])
        activity.startActivity(intent)
        notifyItemChanged(position) //To notify adapter about changes, otherwise we won't see changes till we restart app
    }

    fun removeAt(position: Int){
        val item=spots[position]
        //spots.removeAt(position)
        deleteListener.invoke(item.id)
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
        return spots.size
    }
}