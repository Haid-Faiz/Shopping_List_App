package Adapter

import Interface.MyInterface
import Model.ListData
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shoppinglist.R

class MyAdapter(var myList: ArrayList<ListData>,  var myInterface: MyInterface) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.custom_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        var myData = myList[position]
        holder.item.text = myData.item
        holder.note.text = myData.note
        holder.amount.text = "₹  " + myData.amount.toString()
        holder.date.text = myData.time

        holder.itemView.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                myInterface.onItemClick(myData, position)
            }
        })
    }

    override fun getItemCount(): Int {
        return myList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var item = itemView.findViewById<TextView>(R.id.itemID)
        var note = itemView.findViewById<TextView>(R.id.noteID)
        var amount = itemView.findViewById<TextView>(R.id.priceID)
        var date = itemView.findViewById<TextView>(R.id.dateID)
    }
}