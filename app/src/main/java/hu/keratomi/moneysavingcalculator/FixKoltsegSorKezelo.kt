package hu.keratomi.moneysavingcalculator

import android.content.res.Resources
import android.text.InputType
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout


class FixKoltsegSorKezelo(
    val resources: Resources,
    val mainActivity: MainActivity,
    val mainLayout: LinearLayout
) {
    val fixKoltsegek: MutableList<FixKoltseg> = mutableListOf<FixKoltseg>()

    fun ujSor(leiras: String, koltseg: String) {
        ujSor()
        fixKoltsegek[fixKoltsegek.lastIndex].leiras.setText(leiras)
        fixKoltsegek[fixKoltsegek.lastIndex].koltseg.setText(koltseg)
    }

    fun ujSor() {
        val kontener = LinearLayout(mainActivity)
        kontener.tag = "contener_" + fixKoltsegek.size
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

        val ujLeiras = ujEditText(
            InputType.TYPE_CLASS_TEXT,
            resources.getDimensionPixelSize(R.dimen.edittext_leiras_width)
        )
        kontener.addView(ujLeiras)

        val ujKoltseg = ujEditText(
            InputType.TYPE_CLASS_NUMBER,
            resources.getDimensionPixelSize(R.dimen.edittext_koltseg_width)
        )
        kontener.addView(ujKoltseg)

        val ujFixKoltseg = FixKoltseg(ujLeiras, ujKoltseg)
        if (fixKoltsegek.size > 0) {
            val deleteGomb = ujDeleteGomb(kontener, ujFixKoltseg)
            kontener.addView(deleteGomb)
        }

        fixKoltsegek.add(ujFixKoltseg)
    }

    fun torolSort(kontener: LinearLayout, fixKoltseg: FixKoltseg) {
        mainLayout.removeView(kontener)
        fixKoltsegek.remove(fixKoltseg)
    }

    fun torolMindenSort() {
        fixKoltsegek.forEachIndexed { index, element ->
            mainLayout.removeView(mainLayout.findViewWithTag<LinearLayout>("contener_${index}"))
        }

        fixKoltsegek.clear()
    }

    private fun ujDeleteGomb(
        kontener: LinearLayout,
        fixKoltseg: FixKoltseg
    ): ImageButton {
        val deleteGomb = ImageButton(mainActivity)
        deleteGomb.setImageResource(android.R.drawable.ic_delete)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        deleteGomb.layoutParams = layoutParams

        deleteGomb.setOnClickListener {
            torolSort(kontener, fixKoltseg)
        }

        return deleteGomb
    }

    private fun ujEditText(inputType: Int, width: Int): EditText {
        val ujMezo = EditText(mainActivity)
        ujMezo.tag = "fi_" + inputType + "_" + fixKoltsegek.size
        ujMezo.inputType = inputType
        ujMezo.minWidth = width

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        ujMezo.layoutParams = layoutParams

        if (inputType == 1) {
            ujMezo.hint = mainActivity.getString(R.string.description)
        } else {
            ujMezo.hint = mainActivity.getString(R.string.cost)
        }

        return ujMezo
    }
}

data class FixKoltseg(val leiras: EditText, val koltseg: EditText)