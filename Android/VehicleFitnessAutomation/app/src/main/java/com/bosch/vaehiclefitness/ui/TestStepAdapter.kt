package com.bosch.vaehiclefitness.ui

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bosch.vaehiclefitness.R
import com.bosch.vaehiclefitness.model.AudioTestStep
import com.bosch.vaehiclefitness.model.TestStep
import com.bosch.vaehiclefitness.model.TestStep.Companion.TYPE_DRIVE_TEST
import com.bosch.vaehiclefitness.model.TestStep.Companion.TYPE_IMS_TEST
import com.bosch.vaehiclefitness.model.TestStep.Companion.TYPE_OBD_TEST
import com.bosch.vaehiclefitness.model.TestStep.Companion.TYPE_PICK_AUDIO
import com.bosch.vaehiclefitness.model.TestStep.Companion.TYPE_PICK_IMAGE
import com.bosch.vaehiclefitness.model.TestStep.Companion.TYPE_PICK_VIDEO
import com.bosch.vaehiclefitness.model.TestStep.Companion.TYPE_TEST_COMPLETED
import com.bosch.vaehiclefitness.model.TestStep.Companion.TYPE_TEST_MANUAL
import com.bosch.vaehiclefitness.model.VideoTestStep
import kotlinx.android.synthetic.main.item_manual_test.view.*
import kotlinx.android.synthetic.main.item_upload_image.view.*
import kotlinx.android.synthetic.main.item_with_button.view.*

class TestStepAdapter(private val onItemClickListener: OnItemClickListener?) :
    ListAdapter<TestStep, RecyclerView.ViewHolder>(TestStep.DIFF_UTIL) {

    private var expandedItemPosition: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_PICK_IMAGE, TYPE_PICK_VIDEO, TYPE_PICK_AUDIO ->
                createImageViewHolder(inflater, parent)
            TYPE_OBD_TEST, TYPE_IMS_TEST, TYPE_DRIVE_TEST, TYPE_TEST_COMPLETED -> createButtonViewHolder(
                inflater,
                parent
            )
            TYPE_TEST_MANUAL -> createManualViewHolder(inflater, parent)
            else -> throw IllegalStateException("Unknown ViewType $viewType")
        }
    }

    private fun createImageViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ImageViewHolder =
        ImageViewHolder(inflater.inflate(R.layout.item_upload_image, parent, false))

    private fun createButtonViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ButtonViewHolder =
        ButtonViewHolder(inflater.inflate(R.layout.item_with_button, parent, false))

    private fun createManualViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ManualTestViewHolder =
        ManualTestViewHolder(inflater.inflate(R.layout.item_manual_test, parent, false))


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ImageViewHolder) {
            holder.bind(getItem(position))
        } else if (holder is ButtonViewHolder) {
            holder.bind(getItem(position))
        }  else if (holder is ManualTestViewHolder) {
            holder.bind(getItem(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type
    }

    fun expandNextStep() {
        expandedItemPosition++
        notifyItemRangeChanged(expandedItemPosition - 1, 2)
    }

    interface OnItemClickListener {
        fun onItemClicked(testStep: TestStep, position: Int)

        fun onVerifyClicked(testStep: TestStep, position: Int)
    }

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(testStep: TestStep) {
            itemView.tvTitle?.setText(testStep.titleResId)
            itemView.tvDescription?.setText(getDescription(testStep.type))
            val isImageSelected = isImageSelected(testStep)
            if (testStep.status == null) {
                itemView.testStatusImage?.visibility = GONE
            } else {
                itemView.testStatusImage?.apply {
                    setImageResource(
                        if (testStep.status == true) R.drawable.bosch_ic_alert_success_filled
                        else R.drawable.bosch_ic_alert_error_filled
                    )
                    visibility = VISIBLE
                }
            }
            itemView.btnVerify?.setText(
                if (isImageSelected) R.string.verify else R.string.upload
            )
            itemView.btnVerify?.setOnClickListener {
                if (isImageSelected) {
                    onItemClickListener?.onVerifyClicked(testStep, adapterPosition)
                } else {
                    onItemClickListener?.onItemClicked(testStep, adapterPosition)
                }
            }

            itemView.docImage?.apply {
                if (testStep.bitMap != null) {
                    setImageBitmap(testStep.bitMap)
                } else {
                    setImageResource(getDefaultIcon(testStep))
                }
                setOnClickListener { onItemClickListener?.onItemClicked(testStep, adapterPosition) }
            }
            if (expandedItemPosition == adapterPosition) {
                itemView.expandedGroup?.visibility = VISIBLE
            } else {
                itemView.expandedGroup?.visibility = GONE
            }
            itemView.setOnClickListener {
                if (expandedItemPosition != adapterPosition) {
                    val prevSelectedPosition = expandedItemPosition
                    expandedItemPosition = adapterPosition
                    notifyItemChanged(prevSelectedPosition)
                    notifyItemChanged(expandedItemPosition)
                }
            }
        }

        private fun isImageSelected(testStep: TestStep): Boolean {
            return when (testStep.type) {
                TYPE_PICK_IMAGE -> testStep.bitMap != null
                TYPE_PICK_VIDEO -> !(testStep as VideoTestStep).videoFrames.isNullOrEmpty()
                TYPE_PICK_AUDIO -> !(testStep as AudioTestStep).audioFilePath.isNullOrBlank()
                else -> false
            }
        }

        private fun getDescription(type: Int): Int {
            return when (type) {
                TYPE_PICK_VIDEO -> R.string.info_choose_video_to_verify
                TYPE_PICK_AUDIO -> R.string.info_choose_audio_to_verify
                TYPE_PICK_IMAGE -> R.string.info_choose_image_to_verify
                else -> R.string.info_choose_image_to_verify
            }
        }

        private fun getDefaultIcon(testStep: TestStep): Int {
            return when (testStep.type) {
                TYPE_PICK_VIDEO -> getVideoIcon(testStep as VideoTestStep)
                TYPE_PICK_AUDIO -> getAudioIcon(testStep as AudioTestStep)
                TYPE_PICK_IMAGE -> R.drawable.ic_add_image
                else -> R.string.info_choose_image_to_verify
            }
        }

        private fun getVideoIcon(testStep: VideoTestStep) =
            if (testStep.videoFrames.isNullOrEmpty()) R.drawable.ic_add_video
            else R.drawable.ic_video_added

        private fun getAudioIcon(testStep: AudioTestStep) =
            if (testStep.audioFilePath.isNullOrEmpty()) R.drawable.ic_add_audio
            else R.drawable.ic_audio_recorded

    }

    inner class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(testStep: TestStep) {
            itemView.tvHeader?.setText(testStep.titleResId)
            if (testStep.status == null) {
                itemView.testResultImage?.visibility = GONE
            } else {
                itemView.testResultImage?.apply {
                    setImageResource(
                        if (testStep.status == true) R.drawable.bosch_ic_alert_success_filled
                        else R.drawable.bosch_ic_alert_error_filled
                    )
                    visibility = VISIBLE
                }
            }
            if (expandedItemPosition == adapterPosition) {
                itemView.buttonExpandedGroup?.visibility = VISIBLE
            } else {
                itemView.buttonExpandedGroup?.visibility = GONE
            }
            itemView.setOnClickListener {
                if (expandedItemPosition != adapterPosition) {
                    val prevSelectedPosition = expandedItemPosition
                    expandedItemPosition = adapterPosition
                    notifyItemChanged(prevSelectedPosition)
                    notifyItemChanged(expandedItemPosition)
                }
            }
            itemView.btnStartTest?.setOnClickListener {
                onItemClickListener?.onVerifyClicked(testStep, adapterPosition)
            }
            when (testStep.type) {
                TYPE_DRIVE_TEST, TYPE_OBD_TEST, TYPE_IMS_TEST -> {
                    itemView.tvInfo?.setText(R.string.info_start_test)
                    itemView.btnStartTest?.setText(R.string.start_test)
                }
                TYPE_TEST_COMPLETED -> {
                    itemView.tvInfo?.setText(R.string.info_generate_report)
                    itemView.btnStartTest?.setText(R.string.generate_report)
                }
            }
        }
    }

    inner class ManualTestViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(testStep: TestStep) {
            itemView.tvHeaderLabel?.setText(testStep.titleResId)
            if (testStep.status == null) {
                itemView.testResultImg?.visibility = GONE
            } else {
                itemView.testResultImg?.apply {
                    setImageResource(
                        if (testStep.status == true) R.drawable.bosch_ic_alert_success_filled
                        else R.drawable.bosch_ic_alert_error_filled
                    )
                    visibility = VISIBLE
                }
            }
            if (expandedItemPosition == adapterPosition) {
                itemView.manualExpandedGroup?.visibility = VISIBLE
            } else {
                itemView.manualExpandedGroup?.visibility = GONE
            }
            itemView.setOnClickListener {
                if (expandedItemPosition != adapterPosition) {
                    val prevSelectedPosition = expandedItemPosition
                    expandedItemPosition = adapterPosition
                    notifyItemChanged(prevSelectedPosition)
                    notifyItemChanged(expandedItemPosition)
                }
            }
            itemView.btnPass?.setOnClickListener {
                testStep.status = true
                onItemClickListener?.onVerifyClicked(testStep, adapterPosition)
            }
            itemView.btnFail?.setOnClickListener {
                testStep.status = false
                onItemClickListener?.onVerifyClicked(testStep, adapterPosition)
            }
        }
    }
}