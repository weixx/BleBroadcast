package com.study.blebroadcast.adpater

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.*

abstract class BaseRecyclerViewAdapter<D>(var dataList: ArrayList<D>) : RecyclerView.Adapter<BaseRecyclerViewAdapter.ViewHolder>() {

    var mItemClickListener: MyItemClickListener? = null
    var mItemLongClickListener: MyItemLongClickListener? = null

    abstract fun getLayoutId () : Int

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(getLayoutId(), parent, false)
        val vh = ViewHolder(view,mItemClickListener,mItemLongClickListener)
        return vh
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        createHolder(holder, dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    abstract fun createHolder(holder: ViewHolder, d: D)

    @Suppress("UNCHECKED_CAST")
    class ViewHolder(val rootView: View, var mItemClickListener: MyItemClickListener?,
                     var mItemLongClickListener: MyItemLongClickListener?) :
            RecyclerView.ViewHolder(rootView), View.OnClickListener, View.OnLongClickListener {

        private val views = SparseArray<View>()

        fun <T : View> get(id: Int): T {
            var view = views.get(id)
            if (view == null) {
                view = rootView.findViewById(id)
                views.put(id, view)
            }
            return view as T
        }

        fun getContext() : Context{
            return rootView.context
        }

        override fun onClick(v: View) {
            mItemClickListener?.onItemClick(v, layoutPosition)
        }

        override fun onLongClick(arg0: View): Boolean {
            mItemLongClickListener?.onItemLongClick(arg0, layoutPosition)
            return true
        }

        init {
            rootView.setOnClickListener(this)
            rootView.setOnLongClickListener(this)
        }
    }

    interface MyItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    interface MyItemLongClickListener {
        fun onItemLongClick(view: View, position: Int)
    }

    fun setOnItemClickListener(listener: MyItemClickListener) {
        this.mItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: MyItemLongClickListener) {
        this.mItemLongClickListener = listener
    }

    fun addData(dataList: ArrayList<D>) {
        this.dataList.addAll(dataList)
        notifyDataSetChanged()
    }

    fun setData(dataList: ArrayList<D>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }

    fun getItem(position: Int): D {
        return dataList[position]
    }
    fun clear(){
        dataList.clear()
    }
}
