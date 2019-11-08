package com.example.firsttest

import android.content.res.Resources
import android.text.InputType
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.activity_main.*


data class FixKoltsegSorLetrehozo(
    val fixKoltsegek: MutableList<FixKoltseg>,
    val resources: Resources,
    val mainActivity: MainActivity,
    val mainLayout: LinearLayout
)

fun FixKoltsegSorLetrehozo.ujSor() {
    //val kontener = ConstraintLayout(mainActivity)
    val kontener = LinearLayout(mainActivity)
    kontener.id = fixKoltsegek.size
    val layoutParams = ConstraintLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        resources.getDimensionPixelSize(R.dimen.kontener_height)
    )
    layoutParams.orientation = LinearLayout.HORIZONTAL
    kontener.setLayoutParams(
        layoutParams
    )
    kontener.x = 0F
    mainLayout.addView(kontener)

    val ujLeiras = ujEditText(InputType.TYPE_CLASS_TEXT, resources.getDimensionPixelSize(R.dimen.edittext_leiras_width))
    kontener.addView(ujLeiras)

    val ujKoltseg = ujEditText(InputType.TYPE_CLASS_NUMBER, resources.getDimensionPixelSize(R.dimen.edittext_koltseg_width))
    kontener.addView(ujKoltseg)

    val ujFixKoltseg = FixKoltseg(ujLeiras, ujKoltseg)
    if (fixKoltsegek.size > 0) {
        val deleteGomb = ujDeleteGomb(kontener, ujFixKoltseg)
        kontener.addView(deleteGomb)
    }

    fixKoltsegek.add(ujFixKoltseg)
}

private fun FixKoltsegSorLetrehozo.ujDeleteGomb(kontener: LinearLayout, fixKoltseg: FixKoltseg): ImageButton {
    val deleteGomb = ImageButton(mainActivity)
    deleteGomb.setImageResource(android.R.drawable.ic_delete)

    val layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )

    deleteGomb.layoutParams = layoutParams

    deleteGomb.setOnClickListener {
        mainLayout.removeView(kontener)
        fixKoltsegek.remove(fixKoltseg)
    }

    return deleteGomb
}

private fun FixKoltsegSorLetrehozo.ujEditText(inputType: Int, width: Int):EditText {
    val ujMezo = EditText(mainActivity)
    ujMezo.inputType = inputType
    ujMezo.minWidth = width

    val layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )

    ujMezo.layoutParams = layoutParams

    return ujMezo
}