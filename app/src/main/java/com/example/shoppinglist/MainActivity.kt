package com.example.shoppinglist

import Adapter.MyAdapter
import Interface.MyInterface
import Model.ListData
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

//SHA-1 certificate fingerprint:
//E7:C7:0A:5A:8E:76:50:35:9B:14:E3:C6:90:FD:E5:A7:91:77:77:40

class MainActivity : AppCompatActivity() {

    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var totalAmountTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var lottieAnimationBox: LottieAnimationView
    private lateinit var myAdapter: MyAdapter
    private  var myList: ArrayList<ListData> = ArrayList()
    private lateinit var simpleCallback: ItemTouchHelper.SimpleCallback
    private lateinit var myInterface: MyInterface
    private lateinit var myToolbar: Toolbar
    private lateinit var logoutImage: ImageView
    private lateinit var archiveImage: ImageView
    // Firebase
    private lateinit var myDatabase: FirebaseDatabase
    private lateinit var myReference: DatabaseReference
    private lateinit var myAuth: FirebaseAuth
    private lateinit var myAuthStateListener : FirebaseAuth.AuthStateListener
    private lateinit var myUser: FirebaseUser
    private var myUserID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
//        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
//        supportActionBar?.setCustomView(R.layout.custom_action_bar)
        setSupportActionBar(myToolbar)
        clickListeners()
        setUpFirebase()
        retrieveFromFirebase()
        setUpCheckList()
        setUpInterface()
        setUpItemTouchHelper()
    }

    private fun setUpCheckList() {

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

    private fun setUpItemTouchHelper() {
         simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT + ItemTouchHelper.LEFT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                if (direction == ItemTouchHelper.LEFT){
                var position = viewHolder.adapterPosition
                var dataToBeDeleted = myList[position]

                    myReference.child("MainList").child(myList[position].id).removeValue()  // .setValue(null) we can also write it as this
                myList.removeAt(position)
                myAdapter.notifyItemRemoved(position)

                    var snackbar = Snackbar.make(recyclerView, "Deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo", View.OnClickListener {

                        myList.add(position, dataToBeDeleted)
                        myAdapter.notifyItemInserted(position)
                        myReference.child("MainList").child(dataToBeDeleted.id).setValue(dataToBeDeleted)
                    }).show()
            }else{
                    var position = viewHolder.adapterPosition
                    var dataToBeArchive = myList[position]
                    myReference.child("MainList").child(myList[position].id).removeValue()
                    myReference.child("Archived").child(myList[position].id).setValue(myList[position])
                    myList.removeAt(position)
                    myAdapter.notifyItemRemoved(position)

                    Snackbar.make(recyclerView, "Archived", Snackbar.LENGTH_LONG)
                        .setAction("Undo", View.OnClickListener {

                            myList.add(position, dataToBeArchive)
                            myAdapter.notifyItemInserted(position)
                            myReference.child("MainList").child(dataToBeArchive.id).setValue(dataToBeArchive)
                            myReference.child("Archived").child(dataToBeArchive.id).removeValue()
                        }).show()
                }
                setUpCheckList()
            }

             override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

                 RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                     .addSwipeRightBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.archiveColor))
                     .addSwipeRightActionIcon(R.drawable.ic_archive)
                     .addSwipeLeftBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.deleteColor))
                     .addSwipeLeftActionIcon(R.drawable.ic_delete_cross)
                     .create()
                     .decorate()

                 super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
             }
        }
    }

    private fun setUpInterface() {
        myInterface = object : MyInterface{         // This is called anonymous object
            override fun onItemClick(myData: ListData, position: Int) {
                var view = LayoutInflater.from(this@MainActivity).inflate(R.layout.update_data, null)
                var updateItem = view.findViewById<TextInputLayout>(R.id.updateItemID)
                var updateAmount = view.findViewById<TextInputLayout>(R.id.updateAmountID)
                var updateNote = view.findViewById<TextInputLayout>(R.id.updateNoteID)
                var updateButton = view.findViewById<Button>(R.id.updateProductButtonID)

                updateItem.editText?.setText(myData.item)
                updateItem.editText?.setSelection(myData.item.length)
                updateAmount.editText?.setText(myData.amount.toString())
                updateAmount.editText?.setSelection(myData.amount.toString().length)   // Int ko string me convert krke length nikali hai
                updateNote.editText?.setText(myData.note)
                updateNote.editText?.setSelection(myData.note.length)

                var myAlertDialog = AlertDialog.Builder(this@MainActivity)
                    .setView(view)
                    .setCancelable(true)
                    .create()

                myAlertDialog.show()

                updateButton.setOnClickListener(object : View.OnClickListener{
                    override fun onClick(v: View?) {
                        var updatedItem = updateItem.editText?.text.toString().trim()
                        var updatedAmount = updateAmount.editText?.text.toString().trim()
                        var updatedNote = updateNote.editText?.text.toString().trim()

                        if ( !TextUtils.isEmpty(updatedItem) && !TextUtils.isEmpty(updatedAmount)) {
                            updateItem.error = null
                            updateAmount.error = null

                            var date = DateFormat.getDateInstance().format(Date())
                            var postID = myData.id
                            var updatedData = ListData(updatedItem, updatedAmount.toInt(), updatedNote, date, postID)

                            myList.set(position, updatedData)
                            myAdapter.notifyDataSetChanged()
                            myReference.child("MainList").child(postID).setValue(updatedData)       // updating to Firebase
                            myAlertDialog.dismiss()
                            Snackbar.make(recyclerView, "Product Updated", Snackbar.LENGTH_SHORT).show()
                        }else{
                            if(updatedItem.isNotEmpty())
                                updateItem.error = null
                            else
                                updateItem.error = "Required"

                            if (updatedAmount.isNotEmpty())
                                updateAmount.error = null
                            else
                                updateAmount.error = "Required"
                        }
                    }
                })
            }
        }
    }

    private fun retrieveFromFirebase() {

        var myAlertDialog = AlertDialog.Builder(this).setCancelable(false)
            .setView(R.layout.progress_bar)
            .create()
        myAlertDialog.show()   //  progress bar layout in alertDialog

        myReference.child("MainList").addValueEventListener(object : ValueEventListener{

            override fun onDataChange(dataSnapshot : DataSnapshot) {

                myList.clear()
                var totalAmount: Int = 0
                for( myData in dataSnapshot.children){

                    var data= myData.getValue(ListData::class.java)
                    myList.add(data!!)
                    totalAmount += data.amount
                }
                myAlertDialog.dismiss()
                totalAmountTextView = findViewById(R.id.totalAmountID)
                totalAmountTextView.text = "₹ " + totalAmount.toString()
                myAdapter = MyAdapter(myList, myInterface)
                setUpCheckList()
                setUpRecyclerView()
            }

            override fun onCancelled(dataSnapshot: DatabaseError) {
                Toast.makeText(this@MainActivity, "Internet problem", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setUpFirebase() {
        myAuth = Firebase.auth
        myAuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
//            myUser = firebaseAuth.currentUser!!
//            myUserID = myUser.uid
        }
        myAuth.addAuthStateListener(myAuthStateListener)
        myUser = myAuth.currentUser!!
        myUserID = myUser.uid
        myReference = Firebase.database.reference.child("ShoppingList").child(myUserID!!)
    }

    override fun onStart() {
        super.onStart()
        myAuth.addAuthStateListener(myAuthStateListener)
    }

    private fun setUpRecyclerView() {
        recyclerView = findViewById(R.id.recyclerID)
        var linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager
        // setUp ItemTouchHelper
        var itemTouchHelper = ItemTouchHelper(simpleCallback)  // .attachToRecyclerView(recyclerView)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        recyclerView.adapter = myAdapter
    }

    private fun clickListeners() {
        floatingActionButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                // setUp out custom dialog
                setUpDialog()
            }
        })

        logoutImage.setOnClickListener(View.OnClickListener {
            var myBuilder = AlertDialog.Builder(this@MainActivity)
                .setTitle("Logout")
                .setMessage("Are you sure..??")
                .setIcon(R.drawable.ic_logout)
                .setCancelable(true)
                .setPositiveButton("Yes"){ _, _ ->
                    startActivity(Intent(this@MainActivity, Login::class.java))
                    Toast.makeText(this@MainActivity, "Logged Out", Toast.LENGTH_SHORT).show()
                    myAuth.signOut()
                    finish()
                }
                .setNegativeButton("No"){ dialog, _ ->
                    dialog.dismiss()
                }

            var myAlertDialog = myBuilder.create()
            myAlertDialog.show()
        })

        archiveImage.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this@MainActivity, ArchiveActvity::class.java))
        })
    }

    private fun setUpDialog() {
        // inflating the layout
        var view = LayoutInflater.from(this).inflate(R.layout.input_data, null)
        var inputItem = view.findViewById<TextInputLayout>(R.id.inputItemID)
        var inputAmount = view.findViewById<TextInputLayout>(R.id.inputAmountID)
        var inputNote = view.findViewById<TextInputLayout>(R.id.inputNoteID)
        var addProductButton = view.findViewById<Button>(R.id.addProductButtonID)

        var alertDialog = AlertDialog.Builder(this).setView(view).setCancelable(true).create()
        alertDialog.show()

        addProductButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                var itemValue = inputItem.editText?.text.toString().trim()
                var amountValue = inputAmount.editText?.text.toString().trim()
                var noteValue = inputNote.editText?.text.toString().trim()
                // Time generation
//                var date = SimpleDateFormat("yyyymmddhhmmss").format(Date())
//                var myTime = DateFormat.getTimeInstance().format(Date())                      //  10:12:07 am
                var myDate = DateFormat.getDateInstance().format(Date())                      //  15 Sep 2020
//                var myDateTime = DateFormat.getDateTimeInstance().format(Date())         // 15 Sep 2020 10:18:27 am

                if (!TextUtils.isEmpty(itemValue) && !TextUtils.isEmpty(amountValue)) {
                    inputItem.error = null
                    inputAmount.error = null

                    var amountInt = amountValue.toInt()
                    // Random ID generation
                    var randomPostID = myReference.push().key       // check by directly doing myRef.push().setValue()

                    var myData = ListData(itemValue, amountInt, noteValue, myDate, randomPostID!!)

//                    myReference.child(randomPostID).setValue(myData)      // yaani ke shayad jis child pe upload krna ho use direct use kr skte hai
                    myReference.child("MainList").child(randomPostID).setValue(myData)
                    alertDialog.dismiss()
                } else {
                    if(itemValue.isNotEmpty())
                        inputItem.error = null
                    else
                        inputItem.error = "Required"

                    if (amountValue.isNotEmpty())
                        inputAmount.error = null
                    else
                        inputAmount.error = "Required"
                }
            }
        })
    }

    private fun bindViews() {
        recyclerView = findViewById(R.id.recyclerID)
        floatingActionButton = findViewById(R.id.floatingButtonID)
        lottieAnimationBox = findViewById(R.id.emptyBoxAnimeID)
        myToolbar = findViewById(R.id.homeToolbarID)
        logoutImage = findViewById(R.id.LogoutImageID)
        archiveImage = findViewById(R.id.ArchiveImageID)
    }


//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        MenuInflater(this).inflate(R.menu.my_menu, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when(item.itemId){
//            R.id.menuLogoutID -> {
//
//            }
//
//            R.id.menuArchiveID -> {
//
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }
}