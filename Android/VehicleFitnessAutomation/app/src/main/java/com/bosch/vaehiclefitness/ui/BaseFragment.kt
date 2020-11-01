package com.bosch.vaehiclefitness.ui

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bosch.vaehiclefitness.R

open class BaseFragment : Fragment() {

    protected lateinit var mLoader: ProgressDialog;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mLoader = ProgressDialog(activity)
        mLoader.setMessage(getString(R.string.loading))
        mLoader.setCancelable(false)
    }

    protected fun showToast(message: String?) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }

    protected fun showToast(@StringRes stringResId: Int) {
        showToast(getString(stringResId))
    }

    open fun showProgress() {
        mLoader.show()
    }

    open fun hideProgress() {
        if (mLoader.isShowing) {
            mLoader.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideProgress()
    }

    protected open fun showAlertDialog(@StringRes title: Int, @StringRes msg: Int) {
        showAlertDialog(getString(title), getString(msg))
    }

    protected open fun showAlertDialog(title: String?, msg: String?) {
        if (activity == null || activity?.isFinishing == true) {
            return
        }
        AlertDialog.Builder(activity ?: return)
            .setMessage(msg)
            .setTitle(title)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    protected fun getMainActivity(): MainActivity? = activity as? MainActivity

}