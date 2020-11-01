package com.bosch.vaehiclefitness.model

import android.graphics.Bitmap
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil
import java.io.File

open class TestStep(
    val type: Int,
    @StringRes val titleResId: Int,
    var status: Boolean? = null
) {
    companion object {

        const val TYPE_PICK_IMAGE = 2000
        const val TYPE_PICK_AUDIO = 2001
        const val TYPE_PICK_VIDEO = 2002
        const val TYPE_OBD_TEST = 2003
        const val TYPE_DRIVE_TEST = 2004
        const val TYPE_TEST_COMPLETED = 2005
        const val TYPE_TEST_MANUAL = 2006
        const val TYPE_IMS_TEST = 2007

        val DIFF_UTIL: DiffUtil.ItemCallback<TestStep> =
            object : DiffUtil.ItemCallback<TestStep>() {
                override fun areItemsTheSame(oldItem: TestStep, newItem: TestStep): Boolean =
                    oldItem === newItem

                override fun areContentsTheSame(oldItem: TestStep, newItem: TestStep): Boolean =
                    oldItem == newItem
            }
    }

    var bitMap: Bitmap? = null
    var url:String? = null
    var responseKey: String? = null

    override fun hashCode(): Int {
        var result = type
        result = 31 * result + titleResId
        result = 31 * result + (status?.hashCode() ?: 0)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TestStep

        if (type != other.type) return false
        if (titleResId != other.titleResId) return false
        if (status != other.status) return false

        return true
    }
}

class ImageTestStep(
    @StringRes titleResId: Int,
) : TestStep(TYPE_PICK_IMAGE, titleResId) {
}

class VideoTestStep(
    @StringRes titleResId: Int,
) : TestStep(TYPE_PICK_VIDEO, titleResId) {
    var videoFrames: ArrayList<File>? = null
}

class AudioTestStep(
    @StringRes titleResId: Int,
) : TestStep(TYPE_PICK_AUDIO, titleResId) {
    var audioFilePath: String? = null
}

class ManualTestStep(
    @StringRes titleResId: Int,
) : TestStep(TYPE_TEST_MANUAL, titleResId) {
}