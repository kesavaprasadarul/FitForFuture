package com.bosch.vaehiclefitness.ui

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bosch.vaehiclefitness.R
import com.bosch.vaehiclefitness.model.VehicleFitness
import com.bosch.vaehiclefitness.util.SharedPreferenceUtil
import kotlinx.android.synthetic.main.fragment_certificate.view.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

class CertificateFragment : BaseFragment() {

    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        return inflater.inflate(R.layout.fragment_certificate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val vehicleFitness = mainViewModel.vehicleFitness
        view.tvInfoLine1?.text = getString(R.string.info_cert_line_1, vehicleFitness.vehicleNumber)
        view.testResultRecyclerView1?.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = CertificateAdapter(createListDataMap(vehicleFitness))
        }
        view.testResultRecyclerView2?.apply {
            layoutManager = GridLayoutManager(activity, 2)
            adapter = CertificateAdapter(createGridDataMap(vehicleFitness))
        }
        getMainActivity()?.setupToolbar(R.string.title_certificate)
        saveAsImage(view.containerLinearLayout)
    }

    private fun createListDataMap(vehicleFitness: VehicleFitness): HashMap<String, String> {
        return linkedMapOf(
            "Certificate will expire on" to ": ${createExpiryDate()}",
            "Next Inspection Due Data" to ": ${createNextInspectionDate()}",
            "Inspection Fee Receipt No" to ": Dl10002300023",
            "Application No" to ": ${vehicleFitness.applicationNumber}",
            "Receipt Date" to ": 23 Sep 2015",
            "Chassis Number" to ": ${vehicleFitness.chassisNumber}",
            "Engine Number" to ": 095682278",
            "Type of body" to ": STATION WAGON"
        )
    }

    private fun createGridDataMap(vehicleFitness: VehicleFitness): HashMap<String, String> {
        return linkedMapOf(
            "Registration No" to ": ${vehicleFitness.vehicleNumber}",
            "Manufacturing Year" to ":2019",
            "Category of Vehicle" to ":3WT",
            "Inspected on" to ":${vehicleFitness.getApplicationDateString()}",
            "Remark" to ":DSF",
            "" to "",
            "Inspected by " to ":DL TESTING",
            "Inspected by" to ":PAWAN KR"

        )
    }

    private fun saveAsImage(view: View?) {
        showProgress()
        view?.post {
            saveMediaToStorage(getBitmapFromView(view))
            hideProgress()
        }
    }

    private fun getBitmapFromView(view: View): Bitmap? {
        //Define a bitmap with the same size as the view
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas)
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        }
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }

    private fun saveMediaToStorage(bitmap: Bitmap?) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= 29/*Build.VERSION_CODES.Q*/) {
            activity?.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put("relative_path", Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos.use {
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
            showToast(R.string.certificate_saved)
        }
    }

    private fun createExpiryDate(): String {
        return formatDate(
            System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2 * 365)
        )
    }

    private fun createNextInspectionDate(): String {
        return formatDate(
            System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2 * 365)
                    - TimeUnit.DAYS.toMillis(2 * 30)
        )
    }

    private fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd-MMM-YYYY")
        return sdf.format(Date().apply { time = millis })
    }

}