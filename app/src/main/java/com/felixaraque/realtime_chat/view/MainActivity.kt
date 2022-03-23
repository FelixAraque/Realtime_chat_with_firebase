package com.felixaraque.realtime_chat.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.felixaraque.realtime_chat.R
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.felixaraque.realtime_chat.adapters.CustomAdapter
import com.felixaraque.realtime_chat.model.Message
import com.felixaraque.realtime_chat.util.DataConverter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "realtime_chat"
    }

    private lateinit var titulo: CharSequence
    private var nick: String?= null
    private lateinit var shareP: SharedPreferences
    private lateinit var btnsend: ImageButton
    private lateinit var edmessage: EditText
    private lateinit var adapter: CustomAdapter
    private lateinit var rv: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var messages: List<Message>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        titulo = "Chat"

        shareP = getSharedPreferences("datos", Context.MODE_PRIVATE)


        btnsend = findViewById<ImageButton>(R.id.btnsend)
        edmessage = findViewById<EditText>(R.id.edmessage)

        rv = findViewById<RecyclerView>(R.id.rv)
        setSupportActionBar(toolbar)

        db = FirebaseFirestore.getInstance()
        getUsuarioSH()

        btnsend.setOnClickListener { onClickSend()}
    }

    private fun getUsuarioSH() {
        nick = shareP.getString("nick","").toString()
        if (nick.equals("")){
            dialogoNick()
        }
        else {
            initRV()
            getDataSnapshot()
        }
        if (nick != null || !nick.equals("")) {
            title = "${titulo} - ${nick}"
        }
    }

    private fun dialogoNick() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Elije tu nombre")
        val ll = LinearLayout(this)
        ll.setPadding(30,30,30,30)
        ll.orientation = LinearLayout.VERTICAL

        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        lp.setMargins(0,50,0,50)

        val textInputLayoutNick = TextInputLayout(this)
        textInputLayoutNick.layoutParams = lp
        val etnick = EditText(this)
        etnick.setPadding(0, 80, 0, 80)
        etnick.textSize = 20.0F
        etnick.hint = "Nombre"
        textInputLayoutNick.addView(etnick)

        ll.addView(textInputLayoutNick)

        builder.setCancelable(false)
        builder.setView(ll)

        builder.setPositiveButton("Aceptar") { dialog, which ->
            saveUsuarioSH(etnick.text.toString())
        }


        builder.show()
    }

    private fun dialogoNickAccount() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cambia de nombre")
        val ll = LinearLayout(this)
        ll.setPadding(30,30,30,30)
        ll.orientation = LinearLayout.VERTICAL

        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        lp.setMargins(0,50,0,50)

        val textInputLayoutNick = TextInputLayout(this)
        textInputLayoutNick.layoutParams = lp
        val etnick = EditText(this)
        etnick.setText(nick)
        etnick.setPadding(0, 80, 0, 80)
        etnick.textSize = 20.0F
        etnick.hint = "Nuevo nombre"
        textInputLayoutNick.addView(etnick)

        ll.addView(textInputLayoutNick)

        builder.setView(ll)

        builder.setPositiveButton("Aceptar") { dialog, which ->
            saveUsuarioSH(etnick.text.toString())
        }

        builder.setNegativeButton("Cancelar") { dialog, which ->
        }

        builder.show()
    }

    private fun msg(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun saveUsuarioSH(nick: String) {
        if (nick.isEmpty() || nick.equals("")) {
            msg("Tienes que elegir nombre.....")
            return
        }
        title = "${titulo} - ${nick}"
        val editor = shareP.edit()
        editor.putString("nick", nick)
        editor.commit()
        this.nick = nick
        initRV()
        getDataSnapshot()
    }

    private fun initRV() {
        adapter = CustomAdapter(this, R.layout.row, nick)
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(this)
    }

    private fun getDataSnapshot() {
        db.collection("messages").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                documentToList(snapshot.documents)
                adapter.setMessages(messages)
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }

    private fun documentToList(documents: List<DocumentSnapshot>) {
        val messagesaux = ArrayList<Message>()
        documents.forEach { d ->
            val message = d["message"] as String
            val fechahora = d["fechahora"] as Long
            val user = d["user"] as String
            val fechahorastring = d["fechahorastring"] as String
            messagesaux.add(Message(message = message, fechahora = fechahora, user = user, fechahorastring = fechahorastring))
        }
        messages = messagesaux
        messages = messages.sortedByDescending { it.fechahora }
    }

    private fun onClickSend() {
        if (nick!!.isEmpty() || nick.equals("")) {
            msg("Tienes que elegir nombre....")
        }
        else {
            val message: MutableMap<String, Any> = HashMap()
            val timestamp = DataConverter.dateToTimestamp(Date())

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm")
            val currentDate = sdf.format(Date())

            message["message"] = edmessage.text.toString()
            message["fechahora"] = timestamp!!.toLong()
            message["user"] = "$nick"
            message["fechahorastring"] = "$currentDate"


            db.collection("messages")
                .add(message)
                .addOnSuccessListener { documentReference ->
                    Log.d(
                        TAG,
                        "DocumentSnapshot added with ID: " + documentReference.id
                    )
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
            edmessage.setText("")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_login -> {
                dialogoNickAccount()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}