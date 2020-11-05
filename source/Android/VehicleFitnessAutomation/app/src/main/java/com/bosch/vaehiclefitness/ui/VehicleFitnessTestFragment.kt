package com.bosch.vaehiclefitness.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaMetadataRetriever
import android.net.ParseException
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bosch.vaehiclefitness.R
import com.bosch.vaehiclefitness.model.*
import com.bosch.vaehiclefitness.model.TestStep.Companion.TYPE_DRIVE_TEST
import com.bosch.vaehiclefitness.model.TestStep.Companion.TYPE_IMS_TEST
import com.bosch.vaehiclefitness.model.TestStep.Companion.TYPE_OBD_TEST
import com.bosch.vaehiclefitness.model.TestStep.Companion.TYPE_PICK_AUDIO
import com.bosch.vaehiclefitness.model.TestStep.Companion.TYPE_PICK_IMAGE
import com.bosch.vaehiclefitness.model.TestStep.Companion.TYPE_PICK_VIDEO
import com.bosch.vaehiclefitness.model.TestStep.Companion.TYPE_TEST_COMPLETED
import com.bosch.vaehiclefitness.model.TestStep.Companion.TYPE_TEST_MANUAL
import com.bosch.vaehiclefitness.ui.AudioRecorderDialog.Companion.KEY_AUDIO_FILE_PATH
import com.bosch.vaehiclefitness.util.Constants
import com.bosch.vaehiclefitness.util.Constants.Companion.KEY_RESULT
import kotlinx.android.synthetic.main.fragment_vehicle_fitness_test.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class VehicleFitnessTestFragment : BaseFragment(), TestStepAdapter.OnItemClickListener,
    LocationListener {

    companion object {
        private const val REQUEST_CODE_IMAGE_GALLERY = 3000
        private const val REQUEST_CODE_IMAGE_CAMERA = 3001
        private const val REQUEST_CODE_VIDEO_CAMERA = 3003
        private const val REQUEST_CODE_VIDEO_GALLERY = 3004
        private const val REQUEST_CODE_AUDIO_RECORDER = 3005
        private const val REQUEST_CODE_AUDIO_GALLERY = 3006

        private const val REQUEST_CODE_PERMISSIONS = 7000
        private val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private var currentStepPosition: Int = 0
    private lateinit var testSteps: ArrayList<TestStep>
    private lateinit var testStepAdapter: TestStepAdapter
    private lateinit var mainViewModel: MainViewModel
    private var locationManager: LocationManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        initSteps()
        return inflater.inflate(R.layout.fragment_vehicle_fitness_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        testStepAdapter = TestStepAdapter(this)
        stepsRecyclerView.adapter = testStepAdapter
        testStepAdapter.submitList(testSteps)
        val vehicleFitness = mainViewModel.vehicleFitness
        tvVehicleDetails.text = getString(
            R.string.description_vehicle_details,
            vehicleFitness.vehicleNumber, vehicleFitness.chassisNumber
        )
        getMainActivity()?.setupToolbar(R.string.title_vehicle_fitness_test)
        requestPermissions(permissions, REQUEST_CODE_PERMISSIONS)
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var permissionsAccepted = true
        when (requestCode) {
            REQUEST_CODE_PERMISSIONS -> {
                for (granted in grantResults) {
                    if (granted != PackageManager.PERMISSION_GRANTED) {
                        permissionsAccepted = false
                        break
                    }
                }
            }
        }
        if (!permissionsAccepted) {
            showToast("Please grant required permissions.")
            parentFragmentManager.popBackStack()
        } else {
            // Location Init
            locationManager =
                activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
            val loc = getLastKnownLocation()
            if (loc != null) {
                showDistanceFromRTO(loc.latitude, loc.longitude)
            }
        }
    }


    private fun initSteps() {
        testSteps = arrayListOf(
            ImageTestStep(R.string.number_plate).apply {
                url = "/api/number_plate_check"; responseKey = "number_plate_check"
            },
            ImageTestStep(R.string.chassis_number).apply {
                url = "/api/chassis_check"; responseKey = "chassis_number_check"
            },
            ImageTestStep(R.string.pollution_cert).apply {
                url = "/api/poll_cert_check"; responseKey = "pollution_certificate_check"
            },
            ImageTestStep(R.string.vehicle_front_image).apply {
                url = "/api/ext_body_check"; responseKey = "result"
            },
            ImageTestStep(R.string.vehicle_front_right_corner_image).apply {
                url = "/api/ext_body_check"; responseKey = "result"
            },
            ImageTestStep(R.string.vehicle_front_left_corner_image).apply {
                url = "/api/ext_body_check"; responseKey = "result"
            },
            ImageTestStep(R.string.vehicle_left_image).apply {
                url = "/api/ext_body_check"; responseKey = "result"
            },
            ImageTestStep(R.string.vehicle_right_image).apply {
                url = "/api/ext_body_check"; responseKey = "result"
            },
            ImageTestStep(R.string.vehicle_back_image).apply {
                url = "/api/ext_body_check"; responseKey = "result"
            },
            ImageTestStep(R.string.vehicle_back_right_corner_image).apply {
                url = "/api/ext_body_check"; responseKey = "result"
            },
            ImageTestStep(R.string.vehicle_back_left_corner_image).apply {
                url = "/api/ext_body_check"; responseKey = "result"
            },
            ImageTestStep(R.string.vehicle_front_right_tyre).apply {
                url = "/api/tyre_check"; responseKey = "tyre_status"
            },
            ImageTestStep(R.string.vehicle_front_left_tyre).apply {
                url = "/api/tyre_check"; responseKey = "tyre_status"
            },
            ImageTestStep(R.string.vehicle_back_right_tyre).apply {
                url = "/api/tyre_check"; responseKey = "tyre_status"
            },
            ImageTestStep(R.string.vehicle_back_left_tyre).apply {
                url = "/api/tyre_check"; responseKey = "tyre_status"
            },
            ImageTestStep(R.string.vehicle_engine_bay).apply {
                url = "/api/eng_bay_check"; responseKey = "eng_bay_status"
            },
            VideoTestStep(R.string.head_lamp).apply {
                url = "/api/wiper_check"; responseKey = "wiper_status"
            },
            VideoTestStep(R.string.wiper).apply {
                url = "/api/wiper_check"; responseKey = "wiper_status"
            },
            VideoTestStep(R.string.indicators).apply {
                url = "/api/wiper_check"; responseKey = "wiper_status"
            },
            VideoTestStep(R.string.break_light).apply {
                url = "/api/wiper_check"; responseKey = "wiper_status"
            },
            AudioTestStep(R.string.engine_start).apply {
                url = "/api/crank_audio_check"; responseKey = "crank_status"
            },
            AudioTestStep(R.string.horn).apply {
                url = "/api/horn_check"; responseKey = "horn_status"
            },
            //FR_VFT_006 - Manual Tests
            ManualTestStep(R.string.reflectors),
            ManualTestStep(R.string.mirrors),
            ManualTestStep(R.string.silencer),
            ManualTestStep(R.string.dashboard),
            ManualTestStep(R.string.steering_gear),
            ManualTestStep(R.string.priority_seating),
            ManualTestStep(R.string.bulbs),
            ManualTestStep(R.string.spark_plugs),
            ManualTestStep(R.string.vehicle_color),
            ManualTestStep(R.string.wheel_chair_entry),
            ManualTestStep(R.string.rear_under_run_protection),
            ManualTestStep(R.string.lateral_side_protection),
            TestStep(TYPE_IMS_TEST, R.string.ims_test),
            TestStep(TYPE_OBD_TEST, R.string.obd_test),
            TestStep(TYPE_DRIVE_TEST, R.string.drive_test)

        )
        if (mainViewModel.isOfficialLogIn) {
            testSteps.add(TestStep(TYPE_TEST_COMPLETED, R.string.test_completed))
        }
    }

    override fun onItemClicked(testStep: TestStep, position: Int) {
        currentStepPosition = position
        when (testStep.type) {
            TYPE_PICK_IMAGE -> AlertDialog.Builder(activity)
                .setTitle(R.string.choose_image)
                .setItems(resources.getStringArray(R.array.array_pick_image)) { _, index ->
                    if (index == 0) { //Gallery
                        pickImageFromGallery()
                    } else { //Camera
                        dispatchTakePictureIntent()
                    }
                }
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, null)
                .show()
            TYPE_PICK_VIDEO -> AlertDialog.Builder(activity)
                .setTitle(R.string.choose_video)
                .setItems(resources.getStringArray(R.array.array_pick_image)) { _, index ->
                    if (index == 0) { //Gallery
                        pickVideoFromGallery()
                    } else { //Camera
                        dispatchTakeVideoIntent()
                    }
                }
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, null)
                .show()
            TYPE_PICK_AUDIO -> AlertDialog.Builder(activity)
                .setTitle(R.string.choose_video)
                .setItems(resources.getStringArray(R.array.array_pick_audio)) { _, index ->
                    if (index == 0) { //Gallery
                        pickAudioFromGallery()
                    } else { //Camera
                        val audioRecorderDialog = AudioRecorderDialog()
                        audioRecorderDialog.setTargetFragment(this, REQUEST_CODE_AUDIO_RECORDER)
                        audioRecorderDialog.show(parentFragmentManager, null)
                    }
                }
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, null)
                .show()

        }
    }

    override fun onVerifyClicked(testStep: TestStep, position: Int) {
        currentStepPosition = position
        when (testStep.type) {
            TYPE_PICK_IMAGE -> {
                if (testStep.titleResId == R.string.number_plate) {
                    verifyNumberPlate(testStep)
                } else if (testStep.titleResId == R.string.chassis_number) {
                    verifyChassisNumber(testStep)
                } else if (testStep.titleResId == R.string.pollution_cert) {
                    verifyPollCert(testStep)
                } else {
                    verifyImage(testStep)
                }
            }
            TYPE_PICK_VIDEO -> verifyVideo(testStep)
            TYPE_PICK_AUDIO -> verifyAudio(testStep)
            TYPE_IMS_TEST -> startActivityForResult(
                Intent(activity, OBDTestActivity::class.java).apply {
                    putExtra("TYPE_IMS_TEST", true)
                },
                TYPE_OBD_TEST
            )
            TYPE_OBD_TEST -> startActivityForResult(
                Intent(activity, OBDTestActivity::class.java), TYPE_OBD_TEST
            )
            TYPE_DRIVE_TEST -> startActivityForResult(
                Intent(activity, VehicleDriveTestActivity::class.java), TYPE_DRIVE_TEST
            )
            TYPE_TEST_COMPLETED -> onTestCompleted()
            TYPE_TEST_MANUAL -> {
                gotoNextStep(testStep.status) //Already updated from the adapter
            }
        }
    }

    private fun onTestCompleted() {
        showProgress()
        mainViewModel.vehicleFitness.testSteps = testSteps
        for (test in testSteps) {
            if (test.status != true) {
                getMainActivity()?.addFragment(
                    ReportFragment(),
                    addToBackStack = false,
                    clearBackStack = true
                )
                return
            }
        }
        getMainActivity()?.addFragment(
            CertificateFragment(),
            addToBackStack = false,
            clearBackStack = true
        )
    }

    private fun gotoNextStep(isCurrentStepSuccess: Boolean?) {
        if (isCurrentStepSuccess != null) {
            testSteps[currentStepPosition].apply {
                status = isCurrentStepSuccess
            }
            testStepAdapter.expandNextStep()
        } else {
            showToast(R.string.something_wrong)
        }
        hideProgress()
    }

    private fun verifyImage(testStep: TestStep) {
        if (testStep is ImageTestStep && testStep.bitMap != null) {
            if (testStep.url.isNullOrBlank()) {
                showToast("API not available!")
                return
            }
            showProgress()
            mainViewModel.verifyImage(testStep).observe(this, {
                if (testStep.titleResId == R.string.pollution_cert && it != null) {
                    showToast(if (it) R.string.polution_cert_valid else R.string.polution_cert_invalid)
                }
                gotoNextStep(it)
            })
        } else {
            showToast(R.string.info_choose_image_to_verify)
        }
    }

    private fun verifyVideo(testStep: TestStep) {
        if (testStep is VideoTestStep && testStep.videoFrames != null) {
            if (testStep.url.isNullOrBlank()) {
                showToast("API not available!")
                return
            }
            showProgress()
            mainViewModel.verifyVideo(testStep).observe(this, {
                gotoNextStep(it)
            })
        } else {
            showToast(R.string.info_choose_video_to_verify)
        }
    }

    private fun verifyAudio(testStep: TestStep) {
        if (testStep is AudioTestStep && testStep.audioFilePath != null) {
            if (testStep.url.isNullOrBlank()) {
                showToast("API not available!")
                return
            }
            showProgress()
            mainViewModel.verifyAudio(testStep).observe(this, {
                gotoNextStep(it)
            })
        } else {
            showToast(R.string.info_choose_audio_to_verify)
        }
    }

    private fun verifyNumberPlate(testStep: TestStep) {
        if (testStep is ImageTestStep && testStep.bitMap != null) {
            showProgress()
            mainViewModel.verifyNumberPlate(testStep).observe(this, {
                if (it != null) {
                    val vehicleFitness = mainViewModel.vehicleFitness
                    val testResult = it.toUpperCase()
                        .contains(mainViewModel.vehicleFitness.vehicleNumber.toUpperCase())
                    showAlertDialog(
                        null,
                        getString(
                            R.string.result_message, it, vehicleFitness.vehicleNumber,
                            if (testResult) getString(R.string.vehicle_num_matched) else getString(R.string.vehicle_num_not_matched)
                        )
                    )
                    gotoNextStep(testResult)
                } else {
                    gotoNextStep(null)
                }
            })
        } else {
            showToast(R.string.info_choose_image_to_verify)
        }
    }

    private fun verifyChassisNumber(testStep: TestStep) {
        if (testStep is ImageTestStep && testStep.bitMap != null) {
            showProgress()
            mainViewModel.verifyChassisNumber(testStep).observe(this, {
                if (it != null) {
                    val vehicleFitness = mainViewModel.vehicleFitness
                    val testResult = it.toUpperCase().replace("O", "0")
                        .contains(mainViewModel.vehicleFitness.chassisNumber.toUpperCase())
                    showAlertDialog(
                        null,
                        getString(
                            R.string.result_message, it, vehicleFitness.chassisNumber,
                            if (testResult) getString(R.string.chassis_num_matched) else getString(R.string.chassis_num_not_matched)
                        )
                    )
                    gotoNextStep(testResult)
                } else {
                    gotoNextStep(null)
                }
            })
        } else {
            showToast(R.string.info_choose_image_to_verify)
        }
    }

    private fun verifyPollCert(testStep: TestStep) {
        if (testStep is ImageTestStep && testStep.bitMap != null) {
            showProgress()
            mainViewModel.verifyPollCert(testStep).observe(this, {
                if (it != null) {
                    val date = parsePollCertExpiry(it)
                    val testResult = (date?.time ?: 0) > System.currentTimeMillis()
                    showAlertDialog(
                        null,
                        getString(
                            R.string.result_poll_cert_message, it,
                            if (date != null)
                                if (testResult) getString(R.string.polution_cert_valid)
                                else getString(R.string.polution_cert_invalid)
                            else ""
                        )
                    )
                    gotoNextStep(testResult)
                } else {
                    gotoNextStep(null)
                }
            })
        } else {
            showToast(R.string.info_choose_image_to_verify)
        }
    }

    private fun parsePollCertExpiry(dateString: String): Date? {
        val format = SimpleDateFormat("dd-MM-yyyy")
        return try {
            format.parse(dateString)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }

    private fun pickImageFromGallery() {
        val galleryIntent = Intent()
        galleryIntent.type = "image/jpeg"
        galleryIntent.action = Intent.ACTION_GET_CONTENT
        if (galleryIntent.resolveActivity(activity?.packageManager ?: return) != null) {
            startActivityForResult(
                Intent.createChooser(galleryIntent, "Choose image..."), REQUEST_CODE_IMAGE_GALLERY
            )
        } else {
            showToast("No applications available in your device to browse images.")
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(activity?.packageManager ?: return) != null) {
            startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_CAMERA)
        } else {
            showToast("No applications available in your device to take picture.")
        }
    }

    private fun pickVideoFromGallery() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_PICK
        startActivityForResult(
            Intent.createChooser(intent, "Select Video"),
            REQUEST_CODE_VIDEO_GALLERY
        )
    }

    private fun pickAudioFromGallery() {
        val intent = Intent()
        intent.type = "audio/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Audio"),
            REQUEST_CODE_AUDIO_GALLERY
        )
    }

    private fun dispatchTakeVideoIntent() {
        val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (takeVideoIntent.resolveActivity(activity?.packageManager ?: return) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_CODE_VIDEO_CAMERA)
        } else {
            showToast("No applications available in your device to take video.")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_IMAGE_GALLERY, REQUEST_CODE_IMAGE_CAMERA -> {
                    try {
                        var selectedBitmap: Bitmap? = null
                        if (requestCode == REQUEST_CODE_IMAGE_GALLERY) {
                            val selectedImgUri = data?.data
                            if (null != selectedImgUri) {
                                //Storing the selected bitmap
                                selectedBitmap = BitmapFactory.decodeStream(
                                    activity?.contentResolver?.openInputStream(selectedImgUri)
                                )
                            }
                        } else {
                            selectedBitmap = data?.extras?.get("data") as? Bitmap
                        }
                        if (selectedBitmap != null) {
                            /*val targetImageView = getImageViewByRequestCode(requestCode)
                            //Setting the selected image in UI
                            val imageWidth = selectedBitmap.width
                            val imageHeight = selectedBitmap.height
                            val newWidth =
                                targetImageView?.width
                                    ?: 0 //this method should return the width of device screen.
                            val scaleFactor = newWidth.toFloat() / imageWidth.toFloat()
                            val newHeight = (imageHeight * scaleFactor).toInt()
                            val resizedBitmap = Bitmap.createScaledBitmap(
                                selectedBitmap,
                                newWidth,
                                newHeight,
                                true
                            )
                            targetImageView?.setImageBitmap(resizedBitmap)
                            selectedBitmaps[requestCode] = resizedBitmap*/
                            (testSteps[currentStepPosition] as ImageTestStep).bitMap =
                                selectedBitmap
                            testStepAdapter.notifyItemChanged(currentStepPosition)
                            return
                        }
                        showToast(R.string.something_wrong)
                    } catch (e: Exception) {
                        Log.e(Constants.TAG, "Exception in onActivityResult " + e.message)
                        showToast(R.string.something_wrong)
                    } catch (e: OutOfMemoryError) {
                        showToast(R.string.large_image_error_msg)
                        return
                    }
                }
                REQUEST_CODE_VIDEO_GALLERY, REQUEST_CODE_VIDEO_CAMERA -> onVideoCaptured(data?.data)
                REQUEST_CODE_AUDIO_RECORDER -> onAudioRecorded(
                    data?.getStringExtra(KEY_AUDIO_FILE_PATH) ?: return
                )
                REQUEST_CODE_AUDIO_GALLERY -> onAudioPickedFromGallery(data?.data)
                TYPE_DRIVE_TEST -> onDriveTestResultReceived(data?.getIntExtra(KEY_RESULT, 0))
                TYPE_OBD_TEST, TYPE_IMS_TEST ->
                    onOBDResultReceived(data?.getIntExtra(KEY_RESULT, 0))
                else -> super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun onOBDResultReceived(result: Int?) {
        gotoNextStep(result == 1)
    }

    private fun onDriveTestResultReceived(result: Int?) {
        gotoNextStep(result == 1)
    }

    private fun onAudioPickedFromGallery(audioFileUri: Uri?) {
        var audioFilePath: String? = null
        try {
            audioFilePath = getRealPathFromAudioURI(audioFileUri)
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(audioFilePath)
            val duration: String? =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMillisec = duration?.toInt() ?: 0 //duration in millisec
            if (durationMillisec > TimeUnit.SECONDS.toMillis(20)) {
                showToast("Video duration cannot be more than 20 sec.")
                return
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        if (!audioFilePath.isNullOrBlank() && File(audioFilePath).exists()) {
            (testSteps[currentStepPosition] as AudioTestStep).audioFilePath = audioFilePath
            testStepAdapter.notifyItemChanged(currentStepPosition)
        } else {
            showToast("Failed to extract the file.")
        }
    }

    private fun onAudioRecorded(audioFilePath: String) {
        if (!audioFilePath.isBlank() && File(audioFilePath).exists()) {
            (testSteps[currentStepPosition] as AudioTestStep).audioFilePath = audioFilePath
            testStepAdapter.notifyItemChanged(currentStepPosition)
        } else {
            showToast("Failed to extract the file.")
        }
    }

    private fun onVideoCaptured(videoUri: Uri?) {
        showProgress()
        val frames = splitFrames(getRealPathFromURI(videoUri))
        if (frames == null) {
            showToast(R.string.error_frame_extraction_failed)
        } else if (frames.isEmpty()) {
            showToast("Video duration cannot be more than 20 sec.")
        } else {
            (testSteps[currentStepPosition] as VideoTestStep).videoFrames = frames
            testStepAdapter.notifyItemChanged(currentStepPosition)
        }
        hideProgress()
    }

    private fun getRealPathFromURI(contentUri: Uri?): String? {
        var cursor: Cursor? = null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        cursor = activity?.managedQuery(contentUri, proj, null, null, null)
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor?.moveToFirst()
        val path = cursor?.getString(columnIndex!!)
        return path
    }

    private fun getRealPathFromAudioURI(uri: Uri?): String? {
        var cursor: Cursor? = null
        val proj = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.SIZE)
        val docId: String = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":").toTypedArray()
        val contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "_id=?"
        val selectionArgs = arrayOf(
            split[1]
        )
        cursor = activity?.managedQuery(contentUri, proj, selection, selectionArgs, null)
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        cursor?.moveToFirst()
        val path = cursor?.getString(columnIndex!!)
        return path
    }

    private fun splitFrames(actPath: String?): ArrayList<File>? {
        try {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(actPath)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            val frameList: ArrayList<Bitmap> = ArrayList()
            val files: ArrayList<File> = ArrayList()
            val duration: String =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?: return null
            val durationMillisec = duration.toInt() //duration in millisec
            if (durationMillisec > TimeUnit.SECONDS.toMillis(20)) {
                return arrayListOf()
            }
            val durationSecond = durationMillisec / 1000 //millisec to sec.
            val framesPerSecond = 2 //no. of frames want to retrieve per second
            val numberOfFramesCaptured = framesPerSecond * durationSecond
            var count = 0
            for (i in 0 until numberOfFramesCaptured) {
                //setting time position at which you want to retrieve frames
                val currentBMP = retriever.getFrameAtTime(
                    5 * 100000 * i.toLong(),
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
                frameList.add(currentBMP)
                var outStream: OutputStream? = null
                val outputDir: File = activity?.cacheDir!! // context being the Activity pointer
                try {
                    val outFile = File.createTempFile("frame$count", ".jpg", outputDir)
                    count++
                    outStream = FileOutputStream(outFile)
                    currentBMP.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                    outStream.close()
                    files.add(outFile)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return files
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(): Location? {
        val providers: List<String> = locationManager?.getProviders(true) ?: return null
        var bestLocation: Location? = null
        for (provider in providers) {
            val l: Location = locationManager?.getLastKnownLocation(provider)
                ?: return null
            if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                // Found best last known location: %s", l);
                bestLocation = l
            }
        }
        return bestLocation
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager?.removeUpdates(this)
    }

    //FR_VFT_03
    private fun showDistanceFromRTO(currLat: Double, currLng: Double) {
        val rtoLat = 11.016010
        val rtoLng = 76.970310

        val result = FloatArray(1)
        Location.distanceBetween(currLat, currLng, rtoLat, rtoLng, result)
        val status = result[0] < (3 * 1000)
        tvLocationDetails?.apply {
            text = getString(
                R.string.info_rto_geo_fencing,
                "$currLat,$currLng", "$rtoLat,$rtoLng", result[0].toString(),
                if (status) "PASS" else "FAIL"
            )
            visibility = VISIBLE
            setTextColor(
                ContextCompat.getColor(
                    context,
                    if (status) android.R.color.holo_green_dark else android.R.color.holo_red_dark
                )
            )
        }
    }

    override fun onLocationChanged(location: Location?) {
        Log.d("Vishnu", "onLocationChanged $location")
        location ?: return
        val currLat = location.latitude
        val currLng = location.longitude
        showDistanceFromRTO(currLat, currLng)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.d("Vishnu", "onStatusChanged")
    }

    override fun onProviderEnabled(provider: String?) {
        Log.d("Vishnu", "onProviderEnabled")
    }

    override fun onProviderDisabled(provider: String?) {
        Log.d("Vishnu", "onProviderDisabled")
    }

}