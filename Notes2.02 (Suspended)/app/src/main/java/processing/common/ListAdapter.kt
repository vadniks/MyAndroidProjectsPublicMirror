/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import .R
import .common.NUM_UNDEF
import .common.visibleOrGone

/**
 * @author Vad Nik
 * @version dated Jul 12, 2019.
 * @link https://github.com/vadniks
 */
class ListAdapter(
    private val onClick: (n: Note, item: View) -> Unit,
    private val onLongClick: (n: Note, vg: View) -> Unit
) : PagedListAdapter<Note, ListAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_common, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val n = getItem(position) ?: return
        holder.title.text = n.title
        holder.text.text = n.text

        if (n.color != NUM_UNDEF) {
            holder.color.visibility = visibleOrGone(true)
            holder.color.setBackgroundColor(n.color)
        } else
            holder.color.visibility = visibleOrGone(false)

        holder.item.setOnClickListener {
            onClick(n, holder.item)
        }
        holder.item.setOnLongClickListener {
            onLongClick(n, holder.item)
            true
        }

        val w = n.wid != NUM_UNDEF.toLong()
        val notif = n.nid != NUM_UNDEF.toLong()
        val r = n.rid != NUM_UNDEF.toLong()
        val s = n.sid != NUM_UNDEF.toLong() && n.sid2 != NUM_UNDEF.toLong()

        holder.indiW.visibility = visibleOrGone(w)
        holder.indiN.visibility = visibleOrGone(notif)
        holder.indiR.visibility = visibleOrGone(r)
        holder.indiS.visibility = visibleOrGone(s)

        holder.indicators.visibility = visibleOrGone(w || notif || r || s)
    }

    class ViewHolder(val item: View) : RecyclerView.ViewHolder(item) {
        val title: TextView = item.findViewById(R.id.list_item_common_title)
        val text: TextView = item.findViewById(R.id.list_item_common_text)
        val color: ImageView = item.findViewById(R.id.list_item_common_color)
        val indiW: ImageView = item.findViewById(R.id.list_item_common_indi_widget)
        val indiN: ImageView = item.findViewById(R.id.list_item_common_indi_cons)
        val indiR: ImageView = item.findViewById(R.id.list_item_common_indi_rem)
        val indiS: ImageView = item.findViewById(R.id.list_item_common_indi_sch)
        val indicators: LinearLayout = item.findViewById(R.id.list_item_common_indicators)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Note>() {

            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean =
                oldItem.title === newItem.title

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean =
                oldItem == newItem
        }
    }
}
