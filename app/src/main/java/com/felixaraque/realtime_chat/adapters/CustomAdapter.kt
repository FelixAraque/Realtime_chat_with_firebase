package com.felixaraque.realtime_chat.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.auth.User
import com.felixaraque.realtime_chat.R
import com.felixaraque.realtime_chat.model.Message

class CustomAdapter(val context: Context,
                    val layout: Int,
                    val nick: String?
                    ) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    private var dataList: List<Message> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val viewlayout = layoutInflater.inflate(layout, parent, false)
        return ViewHolder(viewlayout, context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.bind(item, nick)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    internal fun setMessages(messages: List<Message>) {
        this.dataList = messages
        notifyDataSetChanged()
    }


    class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Message, nick: String?){

            val tvmessage = itemView.findViewById(R.id.tvmessage) as TextView
            val cardviewrow = itemView.findViewById(R.id.cardviewrow) as CardView
            val tvuser = itemView.findViewById(R.id.tvuser) as TextView
            val tvuser2 = itemView.findViewById(R.id.tvuser2) as TextView
            val tvfecha = itemView.findViewById(R.id.tvfecha) as TextView

            if (nick == dataItem.user) {
                cardviewrow.setCardBackgroundColor(Color.rgb(120,255,180))
                tvuser.text = "Tú"
                tvuser2.text = "(${dataItem.user})"
            }
            else {
                cardviewrow.setCardBackgroundColor(Color.rgb(153,252,255))
                tvuser.text = dataItem.user
                tvuser2.text = ""
            }
            tvmessage.text = dataItem.message
            tvfecha.text = dataItem.fechahorastring

            itemView.tag = dataItem

        }
    }
}
