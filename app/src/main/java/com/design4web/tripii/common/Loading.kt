package com.design4web.tripii.common

import android.content.Context
import android.graphics.Color
import cn.pedant.SweetAlert.SweetAlertDialog

class Loading {

    fun dialog(ctx: Context,str :String?):SweetAlertDialog{

        val pDialog = SweetAlertDialog(ctx, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = (str)
        pDialog.setCancelable(false)
//        pDialog.show()

        return pDialog
    }
}