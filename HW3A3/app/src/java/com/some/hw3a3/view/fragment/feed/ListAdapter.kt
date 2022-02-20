package com.some.hw3a3.view.fragment.feed

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.some.hw3a3.R
import com.some.hw3a3.view.fragment.feed.dummy.DummyContent.DummyItem

import kotlinx.android.synthetic.main.fragment_feed_item.view.*

/**
 * @author Vad Nik.
 * @version dated Sep 11, 2018.
 * @link http://github.com/vadniks
 */
class ListAdapter(private val mValues: ArrayList<DummyItem>) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    var mOnClickListener: (pos: Int) -> Unit =  { pos ->
        // Notify the active callbacks interface (the activity, if the fragment is attached to
        // one) that an item has been selected.

        //TODO: add action.
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_feed_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.title.text = item.title
        holder.image.setImageBitmap(item.image)
        holder.details.text = item.details

        with(holder.mView) {
            tag = item
            setOnClickListener { mOnClickListener.invoke(position) }
        }
    }

    override fun getItemCount(): Int = mValues.size

    fun addItems(items: ArrayList<DummyItem>) {
        mValues.addAll(items)
        notifyDataSetChanged()
    }

    fun addItem(item: DummyItem) {
        mValues.add(item)
        notifyDataSetChanged()
    }

    fun removeItem(index: Int) {
        mValues.removeAt(index)
        notifyDataSetChanged()
    }

    fun removeAll() {
        mValues.removeAll(mValues)
        notifyDataSetChanged()
    }

    fun getItemAt(index: Int): DummyItem = mValues[index]

    fun replace(index: Int, item: DummyItem) {
        mValues[index] = item
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val title: TextView = mView.item_title
        val image: ImageView = mView.item_image
        val details: TextView = mView.item_details
    }
}
