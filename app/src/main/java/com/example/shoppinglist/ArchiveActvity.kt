package com.example.shoppinglist

import Adapter.MyAdapter
import Interface.MyInterface
import Model.ListData
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.custom_view.*

class ArchiveActvity : AppCompatActivity() {

    private lateinit var totalAmountTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: MyAdapter
    private var myList: ArrayList<ListData> = ArrayList()
    private lateinit var simpleCallback: ItemTouchHelper.SimpleCallback
    private lateinit var myInterface: MyInterface
    private lateinit var myToolBar: Toolbar
    private lateinit var backImage: ImageView
    private lateinit var lottieAnimationBox: LottieAnimationView
    // Firebase
    private lateinit var myReference: DatabaseReference
    private lateinit var myAuth: FirebaseAuth
    private lateinit var myUser: FirebaseUser
    private var myUserID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_archive_actvity)

        bindViews()
//        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
//        supportActionBar?.setCustomView(R.layout.custom_action_bar)
        setSupportActionBar(myToolBar)
        setUpClickListener()
        setUpFirebase()
        retrieveFromFirebase()
        setUpListCheck()
        setUpItemTouchHelper()
        setUpInterface()
    }

    private fun setUpClickListener() {
        backImage.setOnClickListener(View.OnClickListener {
            finish()
        })
    }

    private fun setUpListCheck(){
        lottieAnimationBox.visibility = View.GONE
        recyclerView.visibility = View.GONE

        if (myList.isNotEmpty()){
            recyclerView.visibility = View.VISIBLE
            lottieAnimationBox.visibility = View.GONE
        }
        else{
            recyclerView.visibility = View.GONE
            lottieAnimationBox.visibility = View.VISIBLE
        }
    }

    private fun retrieveFromFirebase() {

        var myAlertDialog = AlertDialog.Builder(this).setCancelable(false)
            .setView(R.layout.progress_bar)
            .create()
        myAlertDialog.show()

        myReference.child("Archived").addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot : DataSnapshot) {

                myList.clear()
                var totalAmount: Int = 0
                for( myData in dataSnapshot.children){

                    var data= myData.getValue(ListData::class.java)
                    myList.add(data!!)
                    totalAmount += data.amount
                }
                myAlertDialog.dismiss()
                totalAmountTextView.text = "₹ " + totalAmount.toString()
                myAdapter = MyAdapter(myList, myInterface)
                setUpListCheck()
                setUpRecyclerView()
            }

            override fun onCancelled(dataSnapshot: DatabaseError) {
                Toast.makeText(this@ArchiveActvity, "Internet problem", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setUpFirebase() {
        myAuth = Firebase.auth
        myUser = myAuth.currentUser!!
        myUserID = myUser.uid
        myReference = Firebase.database.reference.child("ShoppingList").child(myUserID!!)
    }

    private fun setUpItemTouchHelper() {
        simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT + ItemTouchHelper.LEFT){

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                if (direction == ItemTouchHelper.LEFT){
                    var position = viewHolder.adapterPosition
                    var dataToBeDeleted = myList[position]

                    myReference.child("Archived").child(dataToBeDeleted.id).removeValue()
                    myList.removeAt(position)
                    myAdapter.notifyItemRemoved(position)

                    Snackbar.make(recyclerView, "Deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo", View.OnClickListener {
                            myList.add(position, dataToBeDeleted)
                            myReference.child("Archived").child(dataToBeDeleted.id).setValue(dataToBeDeleted)
                            myAdapter.notifyItemInserted(position)
                        }).show()
                }else{
                    // UnArchived
                    var position = viewHolder.adapterPosition
                    var dataToBeUnArchive = myList[position]
                    myReference.child("Archived").child(dataToBeUnArchive.id).removeValue()
                    myReference.child("MainList").child(dataToBeUnArchive.id).setValue(dataToBeUnArchive)
                    myList.removeAt(position)
                    myAdapter.notifyItemRemoved(position)

                    Snackbar.make(recyclerView, "UnArchived", Snackbar.LENGTH_LONG)
                        .setAction("Undo", View.OnClickListener {

                            myList.add(position, dataToBeUnArchive)
                            myReference.child("Archived").child(dataToBeUnArchive.id).setValue(dataToBeUnArchive)
                            myReference.child("MainList").child(dataToBeUnArchive.id).removeValue()
                            myAdapter.notifyItemInserted(position)
                        }).show() 
                }
                setUpListCheck()
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

                RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(this@ArchiveActvity, R.color.deleteColor))
                    .addSwipeLeftActionIcon(R.drawable.ic_delete_cross)
                    .addSwipeRightBackgroundColor(ContextCompat.getColor(this@ArchiveActvity, R.color.archiveColor))
                    .addSwipeRightActionIcon(R.drawable.ic_unarchive)
                    .create()
                    .decorate()

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
    }

    private fun setUpInterface() {
        myInterface = object : MyInterface{
            override fun onItemClick(myData: ListData, position: Int) {
                // Nothing we do here
            }
        }
    }

    private fun setUpRecyclerView() {
        var linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        linearLayoutManager.stackFromEnd = true
        linearLayoutManager.reverseLayout = true
        recyclerView.layoutManager = linearLayoutManager
        // setUp ItemTouchHelper
        var itemTouchHelper = ItemTouchHelper(simpleCallback)  // .attachToRecyclerView(recyclerView)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        recyclerView.adapter = myAdapter
    }

    private fun bindViews() {
        recyclerView = findViewById(R.id.recyclerArchiveID)
        totalAmountTextView = findViewById(R.id.totalAmountArchiveID)
        backImage = findViewById(R.id.backImageArchiveID)
        lottieAnimationBox = findViewById(R.id.lottieAnimationBoxID)
        myToolBar = findViewById(R.id.archiveToolBarID)
    }
}