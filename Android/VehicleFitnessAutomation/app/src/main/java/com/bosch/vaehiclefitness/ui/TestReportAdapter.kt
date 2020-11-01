package com.bosch.vaehiclefitness.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bosch.vaehiclefitness.R
import com.bosch.vaehiclefitness.model.TestStep
import kotlinx.android.synthetic.main.item_report.view.*

class TestReportAdapter(private val testSteps: ArrayList<TestStep>?) :
    RecyclerView.Adapter<TestReportAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_report, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(testSteps!![position])
    }

    override fun getItemCount(): Int = testSteps?.size ?: 0

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(testStep: TestStep) {
            itemView.tvTestName?.setText(testStep.titleResId)
            itemView.tvTestResult?.apply {
                if (testStep.status == true) {
                    setText(R.string.test_result_pass)
                    setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
                } else {
                    setText(R.string.test_result_fail)
                    setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
                }
            }
        }
    }
}