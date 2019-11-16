package hu.keratomi.moneysavingcalculator

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import kotlinx.android.synthetic.main.alert_dialog_with_edittext.view.*
import android.widget.ArrayAdapter



fun questionBeforeDoActionWithLoadedCalculation(messageId: Int, context: Context, okFunction: (dialog: DialogInterface, which: Int) -> Unit) {

    val builder = AlertDialog.Builder(context)

    with(builder)
    {
        setTitle(android.R.string.dialog_alert_title)
        setMessage(messageId)
        setPositiveButton(
            android.R.string.ok,
            DialogInterface.OnClickListener(function = okFunction)
        )
        setNegativeButton(android.R.string.no) { _, _ -> Unit }
        show()
    }
}

fun createWindowWithSavedCalculationList(context: Context, items: Array<String>, okFunction: (dialog: DialogInterface, which: Int) -> Unit) {
    val builder = AlertDialog.Builder(context)
    with(builder)
    {
        setTitle(R.string.saved_calculations)
        setItems(items, DialogInterface.OnClickListener(function = okFunction))

        setPositiveButton(android.R.string.cancel) { _, _ -> Unit }
        show()
    }
}

fun createWindowForGetCalculationName(context: Context, inflater: LayoutInflater, preLoadedFilesFromGoogleDrive: List<String>, okFunction: (dialog: DialogInterface, which: Int) -> Unit) {
    val builder = AlertDialog.Builder(context)
    with(builder)
    {
        setTitle(R.string.calculation_name)
        val dialogLayout = inflater.inflate(R.layout.alert_dialog_with_edittext, null)

        val adapter = ArrayAdapter<String>(context, android.R.layout.select_dialog_item, preLoadedFilesFromGoogleDrive)
        dialogLayout.calculationName.setThreshold(1) //will start working from first character
        dialogLayout.calculationName.setAdapter(adapter)

        setView(dialogLayout)
        setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener(function = okFunction))
        show()
    }
}