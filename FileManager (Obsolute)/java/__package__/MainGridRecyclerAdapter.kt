/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

/**
 * @author Vad Nik.
 * @version dated Dec 11, 2018.
 * @link http://github.com/vadniks
 */
internal class MainGridRecyclerAdapter(
    private val items: ArrayList<FileView>,
    private val onClickListener: (id: Int, file: FileView, item: View) -> Unit,
    private val onLongClickListener: (item: View, file: FileView, vg: ViewGroup) -> Unit) :
    RecyclerView.Adapter<MainGridRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view: View = LayoutInflater.from(p0.context).inflate(R.layout.file_item_grid, p0, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener { _ -> onClickListener.invoke(
            viewHolder.adapterPosition,
            getItem(viewHolder.adapterPosition),
            viewHolder.itemView) }
        viewHolder.itemView.setOnLongClickListener { _ ->
            onLongClickListener.invoke(
                viewHolder.itemView,
                getItem(viewHolder.adapterPosition),
                p0)
            true
        }
        return viewHolder
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.image.setImageBitmap(items[p1].getDisplayedImage(p0.itemView.context))
        p0.name.text = items[p1].name
    }

    private fun getItem(id: Int): FileView = items[id]

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.item_image)
        val name: TextView = itemView.findViewById(R.id.item_name)
        val item = itemView
    }
}
