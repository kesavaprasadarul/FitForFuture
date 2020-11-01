package com.bosch.vaehiclefitness.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.bosch.vaehiclefitness.R
import com.bosch.vaehiclefitness.util.Constants
import kotlinx.android.synthetic.main.fragment_audio_recorder.*
import java.io.IOException

class AudioRecorderDialog : DialogFragment() {

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private var fileName: String? = null
        const val KEY_AUDIO_FILE_PATH = "vehicle_audio_file_path"
    }

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private lateinit var viewModel: AudioRecorderViewModel

    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setStyle(STYLE_NO_TITLE, R.style.DialogStyle)
        // Record to the external cache directory for visibility
        fileName = activity?.externalCacheDir?.absolutePath
        fileName += "/vehicle_audio_record.3gp"
        viewModel = ViewModelProvider(this).get(AudioRecorderViewModel::class.java)
        requestPermissions(permissions, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_audio_recorder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnStartRecording?.setOnClickListener { startRecording() }
        btnStopRecording?.setOnClickListener { stopRecording() }
        btnPlay?.setOnClickListener { onPlayClicked() }
        btnDone?.setOnClickListener { onDoneClicked() }
        viewModel.getRecordTimerLiveData().observe(this, { tvTimer.text = it })
    }

    private fun startRecording() {
        isCancelable = false
        recordingGroup?.visibility = VISIBLE
        btnStartRecording?.visibility = GONE

        recorder = MediaRecorder()
        recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        recorder?.setOutputFile(fileName)
        recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        try {
            recorder?.prepare()
        } catch (e: IOException) {
            Log.e(Constants.TAG, "prepare() failed")
            dismiss()
        }
        recorder?.start()
        viewModel.startRecordingTimer()
    }

    private fun stopRecording() {
        recorder?.stop()
        recorder?.release()
        recorder = null
        recordingGroup?.visibility = GONE
        tvTimer?.setText(R.string.timer_default_0)
        playGroup?.visibility = VISIBLE
        viewModel.stopRecordingTimer()
    }

    private fun startPlaying() {
        player = MediaPlayer()
        try {
            player?.setDataSource(fileName)
            player?.prepare()
            player?.start()
            player?.setOnCompletionListener {
                onPlayClicked()
            }
        } catch (e: IOException) {
            Log.e(Constants.TAG, "prepare() failed")
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    private fun onPlayClicked() {
        if (player == null) {
            btnPlay?.setText(R.string.stop)
            startPlaying()
            viewModel.startRecordingTimer()
            tvTimer?.visibility = VISIBLE
        } else {
            btnPlay?.setText(R.string.play)
            stopPlaying()
            viewModel.stopRecordingTimer()
            tvTimer?.apply {
                setText(R.string.timer_default_0)
                visibility = GONE
            }
        }
    }

    private fun onDoneClicked() {
        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, Intent().apply {
            putExtra(KEY_AUDIO_FILE_PATH, fileName)
        })
        dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
        stopPlaying()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> permissionToRecordAccepted =
                grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        if (!permissionToRecordAccepted) dismissAllowingStateLoss()
    }

}