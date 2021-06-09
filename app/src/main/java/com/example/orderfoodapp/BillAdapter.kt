package com.example.orderfoodapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.bill_item.view.*
import kotlinx.android.synthetic.main.order_history_card.view.*

class BillAdapter (
    private val billList: MutableList<BillItem>
): RecyclerView.Adapter<BillAdapter.BillViewHolder>()  {

    class BillViewHolder (itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        return BillViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.bill_item,
                parent,
                false
            )
        )
    }

    fun addBill(bill: BillItem) {
        billList.add(bill)
        notifyItemInserted(billList.size - 1)
    }

    fun deleteAll() {
        billList.clear()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val curBill = billList[position]
        holder.itemView.apply {
            num_textView.text = curBill.num.toString()
            name_textView.text = curBill.name
            unitPrice_textView.text = curBill.unitPrice.toString()
        }
    }

    override fun getItemCount(): Int {
        return billList.size
    }
}