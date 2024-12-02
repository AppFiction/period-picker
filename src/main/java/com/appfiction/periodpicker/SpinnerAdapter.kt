package com.appfiction.periodpicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.appfiction.periodpicker.model.Period
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener
import com.skydoves.powerspinner.PowerSpinnerInterface
import com.skydoves.powerspinner.PowerSpinnerView

class SpinnerAdapter(
    powerSpinnerView: PowerSpinnerView
) : RecyclerView.Adapter<SpinnerAdapter.MySpinnerViewHolder>(),
    PowerSpinnerInterface<Period> {

    private val items: MutableList<Period> = mutableListOf()

    // Required properties for PowerSpinnerInterface
    override var index: Int = powerSpinnerView.selectedIndex
    override val spinnerView: PowerSpinnerView = powerSpinnerView
    override var onSpinnerItemSelectedListener: OnSpinnerItemSelectedListener<Period>? = null

    override fun setItems(newItems: List<Period>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MySpinnerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.spinner_item_drop_view_date_filter, parent, false)
        return MySpinnerViewHolder(view)
    }

    override fun onBindViewHolder(holder: MySpinnerViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)

        holder.itemView.setOnClickListener {
            index = position // Update the selected index
            notifyItemSelected(position) // Notify listeners
        }
    }

    fun setSelectedIndex(index: Int) {
        this.index = index
    }

    fun getSelectedIndex(): Int = index

    fun getSelectedItem(): Period = items[index]

    fun getItemIndex(item: Period): Int {
        return items.indexOf(item)
    }

    override fun notifyItemSelected(index: Int) {
        val oldIndex = this.index
        val oldItem = items.getOrNull(oldIndex)

        this.index = index
        val newItem = items[index]

        // Notify the listener about the selection change
        onSpinnerItemSelectedListener?.onItemSelected(oldIndex, oldItem, index, newItem)
    }


    class MySpinnerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val label1: AppCompatTextView = itemView.findViewById(R.id.label1)
        private val label2: AppCompatTextView = itemView.findViewById(R.id.label2)

        fun bind(dateRange: Period) {
            label1.text = dateRange.name
            label2.text = dateRange.description
//            label2.visibility = if (dateRange.description != null) View.VISIBLE else View.GONE
        }
    }
}