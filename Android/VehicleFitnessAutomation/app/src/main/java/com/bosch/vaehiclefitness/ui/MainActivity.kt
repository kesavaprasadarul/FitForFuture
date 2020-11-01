package com.bosch.vaehiclefitness.ui

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bosch.vaehiclefitness.R
import kotlinx.android.synthetic.main.activity_main.*

/*

DESIGN ID:
Feature Name:

 */


class MainActivity : AppCompatActivity() {

    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupToolbar(R.string.app_name)
        if (savedInstanceState == null) {
            addFragment(MainFragment.newInstance(), false)
        }
    }

    fun setupToolbar(@StringRes titleResId: Int) {
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(titleResId)
    }

    fun addFragment(
        fragment: Fragment,
        addToBackStack: Boolean = true,
        clearBackStack: Boolean = false
    ) {
        if (clearBackStack) clearBackStack()
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
        currentFragment = fragment
    }

    private fun clearBackStack() {
        supportFragmentManager.popBackStack(
            null,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    override fun onBackPressed() {
        if (currentFragment is ReportFragment || currentFragment is CertificateFragment) {
            clearBackStack()
            addFragment(MainFragment.newInstance(), false)
        } else {
            super.onBackPressed()
        }
    }

}