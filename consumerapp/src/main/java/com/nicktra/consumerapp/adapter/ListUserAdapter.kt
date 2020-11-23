package com.nicktra.consumerapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.nicktra.consumerapp.DetailUserActivity
import com.nicktra.consumerapp.R
import com.nicktra.consumerapp.model.User
import kotlinx.android.synthetic.main.item_row_user.view.*

class ListUserAdapter(private val listUser: ArrayList<User>) : RecyclerView.Adapter<ListUserAdapter.ListViewHolder>() {
    private var onItemClickCallback: OnItemClickCallback? = null

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    var listUserFilter = ArrayList<User>()


    init {
        listUserFilter = listUser
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ListViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_row_user, viewGroup, false)
        return ListViewHolder(view)
    }

    override fun getItemCount(): Int = listUserFilter.size

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(listUserFilter[position])
    }

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(user: User) {
            with(itemView){
                Glide.with(itemView.context)
                    .load(user.avatar)
                    .apply(RequestOptions().override(55, 55))
                    .into(img_item_photo)
                tv_item_name.text = user.username
                tv_item_html_url.text = user.htmlUrl

                itemView.setOnClickListener {
                    Toast.makeText(context, "You select " + user.username, Toast.LENGTH_SHORT).show()
                    val intentWithParcelable = Intent(context, DetailUserActivity::class.java)
                    intentWithParcelable.putExtra(DetailUserActivity.EXTRA_USER, user)
                    context.startActivity(intentWithParcelable)
                }

            }
        }
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: User)
    }
}