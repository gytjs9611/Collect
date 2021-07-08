package com.hschoi.collect.util

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.hschoi.collect.R

class PermissionUtils {
    companion object{
        private val PERMISSIONS = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        const val REQ_STORAGE_PERMISSION = 100

        fun hasPermission(context : Context): Boolean{
            if(ActivityCompat.checkSelfPermission(context, PERMISSIONS[0])!= PackageManager.PERMISSION_GRANTED){
                return false
            }
            return true
        }

        fun requestPermission(activity: Activity, requestCode : Int){
            ActivityCompat.requestPermissions(activity, PERMISSIONS, requestCode)
        }

        fun showPermissionAlert(context: Context){
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle("")
            alertDialog.setMessage(context.getString(R.string.permission_denied_msg))
            alertDialog.setPositiveButton(context.getString(android.R.string.ok), DialogInterface.OnClickListener { dialog, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:${context.packageName}"))
                context.startActivity(intent)
                dialog.cancel()
            })
            alertDialog.setNegativeButton(context.getString(android.R.string.cancel), DialogInterface.OnClickListener { dialog, _ ->
                dialog.cancel()
            })
            alertDialog.show()
        }
    }
}