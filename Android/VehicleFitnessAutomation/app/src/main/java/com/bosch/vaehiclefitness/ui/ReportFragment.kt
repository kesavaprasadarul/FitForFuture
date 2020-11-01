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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bosch.vaehiclefitness.R
import com.bosch.vaehiclefitness.model.TestStep
import kotlinx.android.synthetic.main.fragment_report.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class ReportFragment : BaseFragment() {

    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        return inflater.inflate(R.layout.fragment_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val vehicleFitness = mainViewModel.vehicleFitness
        tvVehicleDetails?.text = getString(
            R.string.description_vehicle_details,
            vehicleFitness.vehicleNumber,
            vehicleFitness.chassisNumber
        )
        val testForReport: ArrayList<TestStep> =
            vehicleFitness.testSteps!!.clone() as ArrayList<TestStep>
        testForReport.removeAt(testForReport.size - 1)
        testResultRecyclerView?.apply {
            adapter = TestReportAdapter(testForReport)
            layoutManager = LinearLayoutManager(activity)
        }
        btnViewCertificate?.setOnClickListener {
            getMainActivity()?.addFragment(
                CertificateFragment(), addToBackStack = false,
                clearBackStack = true
            )
        }
        getMainActivity()?.setupToolbar(R.string.title_report)
        saveAsImage()
    }

    private fun saveAsImage() {
        showProgress()
        containerLinearLayout?.post {
            saveMediaToStorage(getBitmapFromView(containerLinearLayout))
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
            showToast(R.string.report_saved)
        }
    }

}