package com.thomascook.core

import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

object RecyclerViewBase {
    /**
     * Recycle view adapter data class used to store application model and report which view holder
     * should be used for view rendering.
     */
    interface ViewModelBase {
        val renderWithViewTypeId: Int
    }

    /**
     * Interface for implementing a view holder callback. Used to send notifications to a listener
     * when a recycle view element has been clicked
     */
    interface ViewHolderCallback

    /**
     * Custom view holder class which extends [RecyclerView.ViewHolder] and servers as a base
     * for all our view holders
     */
    abstract class ViewHolderBase(itemView: View) : RecyclerView.ViewHolder(itemView) {

        abstract fun bindAdapterData(data: ViewModelBase)
    }

    /**
     * Abstract class for implementing Recycle view holder creators.
     */
    interface ViewHolderBaseCreator<out VH : ViewHolderBase> {
        fun createViewHolder(recycleView: ViewGroup, callback: ViewHolderCallback?): VH
    }

    /**
     * @param <VH> View holder class that extends [ViewHolderBase] base class
     * [RecyclerView] adapter used to manager our custom view holders
    </VH> */
    abstract class ViewAdapter<VH : ViewHolderBase>(
        private val viewHolderCreator: SparseArray<out ViewHolderBaseCreator<VH>>,
        private val dataList: MutableList<ViewModelBase>) : RecyclerView.Adapter<VH>() {

        private var viewHolderCallback: ViewHolderCallback? = null

        fun setViewHolderCallback(viewHolderCallback: ViewHolderCallback?) {
            this.viewHolderCallback = viewHolderCallback
        }

        fun getData(position: Int): ViewModelBase? {
            return if (position >= dataList.size) null else dataList[position]
        }

        override fun getItemViewType(position: Int): Int {
            return this.dataList[position].renderWithViewTypeId
        }

        override fun getItemCount(): Int {
            return this.dataList.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            //Possibly add click listeners
            val creator = viewHolderCreator.get(viewType)
            return creator?.createViewHolder(parent, this.viewHolderCallback)
                ?: throw IllegalStateException("Recycler view holder could not be inflated.")
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val viewData = getData(position)
            if (viewData != null) {
                holder.bindAdapterData(viewData)
            } else {
                throw IllegalStateException("Failed to find get view data to bind for position: $position")
            }
        }

        //Call to add an element to the recycle view
        fun addData(data: ViewModelBase) {
            this.dataList.add(data)
        }

        fun addData(position: Int, data: ViewModelBase) {
            this.dataList.add(position, data)
        }

        fun removeData(position: Int): ViewModelBase? {
            return if (position >= 0 && position < this.dataList.size) this.dataList.removeAt(position) else null
        }

        /**
         * Used to clear current data and set adapter data to the provided one
         *
         * @param data Reference to a collection to copy data from.
         */
        fun setData(data: Collection<ViewModelBase>) {
            this.dataList.clear()
            this.dataList.addAll(data)
        }

        /**
         * @return Reference to a [List] that holds adapter data.
         */
        val list: List<ViewModelBase>
            get() = this.dataList
    }
}
