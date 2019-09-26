package com.papco.sundar.papcojoballotment.common

import android.app.Activity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import com.papco.sundar.papcojoballotment.BuildConfig

fun Activity.toast(msg: String) {
  Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Fragment.toast(msg: String) {
  Toast.makeText(requireActivity(), msg, Toast.LENGTH_SHORT).show()
}

fun AndroidViewModel.toast(msg: String) {
  Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
}

fun Fragment.setTitle(msg: String) {
  (requireActivity() as AppCompatActivity).supportActionBar?.title = msg
}

fun Fragment.setSubTitle(msg: String) {
  (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = msg
}

fun Fragment.enableUpNavigation() {
  (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
}

fun Fragment.disableUpNavigation() {
  (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
}

fun isPrinterVersionApp():Boolean{
  return BuildConfig.FLAVOR=="printer"
}