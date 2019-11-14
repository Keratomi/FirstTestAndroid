package hu.keratomi.moneysavingcalculator

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alert_dialog_with_edittext.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var descriptionAndCostRow: DescriptionAndCostRow
    private lateinit var googleDriveSyncHandler: GoogleDriveSyncHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        googleDriveSyncHandler = GoogleDriveSyncHandler(this)
        googleDriveSyncHandler.googleAuth()

        descriptionAndCostRow = DescriptionAndCostRow(resources, this, mainLayout)
        descriptionAndCostRow.newCostRow()

        setLoadedCalculationDisplay(getString(R.string.uj_mentetlen))
    }

    fun addNewCostRow(view: View) {
        descriptionAndCostRow.newCostRow()
    }

    fun kivalasztottKalkulaciotBetolt(calculationName: String, fileContent: List<String>) {
        descriptionAndCostRow.deleteAllRows()

        val befolyoOsszeg = findViewById<TextView>(R.id.befolyoOsszeg)
        befolyoOsszeg.text = fileContent[0]

        fileContent.subList(1, fileContent.size).forEach {
            val (leiras, koltseg) = it.split(CALCULATION_DATA_INLINE_SEPARATOR)
            descriptionAndCostRow.newCostRow(leiras, koltseg)
            setLoadedCalculationDisplay(
                calculationName.substringBeforeLast(
                    CALCULATION_DATA_FILE_EXTENSION
                )
            )
        }
    }

    fun createCalculation(view: View) {
        val fixKoltsegOsszeg = calculate(descriptionAndCostRow.fixCosts)
        val befolyoOsszeg = findViewById<TextView>(R.id.befolyoOsszeg)
        var befolyoOsszegSzamkent = befolyoOsszeg.text.toString().toIntOrNull()
        befolyoOsszegSzamkent = if (befolyoOsszegSzamkent == null) 0 else befolyoOsszegSzamkent

        val maradtText = findViewById<TextView>(R.id.megmaradtOsszg)
        val megtakaritas = befolyoOsszegSzamkent - fixKoltsegOsszeg
        maradtText.text = megtakaritas.toString()
    }

    fun getCalculationName(view: View) {
        createWindowForGetCalculationName(this, layoutInflater, saveCalculationAsAFile)
    }

    fun requestNewEmptyCalculation(view: View) {
        questionBeforeLoadCalculation(this, createNewEmptyCalculation)
    }

    fun startCalculationLoadingProcess(view: View) {
        questionBeforeLoadCalculation(this, filesFromGoogleDrive)
    }

    private fun setLoadedCalculationDisplay(betoltott: String) {
        val betoltveTextField = findViewById<TextView>(R.id.betoltveTextField)
        betoltveTextField.text = getString(R.string.betoltve, betoltott)
    }

    private fun mentFajlba(fajlNev: String) {
        val befolyoOsszeg = findViewById<EditText>(R.id.befolyoOsszeg).text.toString()

        val saveableString =
            descriptionAndCostRow.fixCosts.joinToString(separator = System.lineSeparator()) { it.description.text.toString() + CALCULATION_DATA_INLINE_SEPARATOR + it.cost.text.toString() }

        val mentettFile =
            File(applicationContext.filesDir.path + "/" + fajlNev + CALCULATION_DATA_FILE_EXTENSION)
        mentettFile.writeText(befolyoOsszeg + System.lineSeparator() + saveableString)
        googleDriveSyncHandler.uploadOrUpdateFile(mentettFile)
    }


    private fun calculate(fixKoltsegek: MutableList<FixCost>): Int = fixKoltsegek.toList()
        .filter { it.cost.text.toString().toIntOrNull() != null }
        .sumBy { it.cost.text.toString().toInt() }

    val filesFromGoogleDrive = { _: DialogInterface, _: Int ->
        googleDriveSyncHandler.queryFileList()
    }

    val createNewEmptyCalculation = { _: DialogInterface, _: Int ->
        descriptionAndCostRow.deleteAllRows()
        descriptionAndCostRow.newCostRow()
        setLoadedCalculationDisplay(getString(R.string.uj_mentetlen))
    }

    val saveCalculationAsAFile = { dialogInterface: DialogInterface, _: Int ->
        val mentendoFajlNeve = (dialogInterface as AlertDialog).mentendoFajlNeve

        mentFajlba(mentendoFajlNeve.text.toString())
        setLoadedCalculationDisplay(mentendoFajlNeve.text.toString())
        Toast.makeText(
            applicationContext,
            getString(R.string.sikeresen_mentve, mentendoFajlNeve.text),
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        googleDriveSyncHandler.authActivityResultHandler(requestCode, resultCode, data)
    }
}
