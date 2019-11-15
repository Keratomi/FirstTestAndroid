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

        setLoadedCalculationDisplay(getString(R.string.new_unsaved))
    }

    fun addNewCostRow(view: View) {
        descriptionAndCostRow.newCostRow()
    }

    fun loadSelectedCalculation(calculationName: String, fileContent: List<String>) {
        descriptionAndCostRow.deleteAllRows()

        val allInComingMoney = findViewById<TextView>(R.id.allInComingMoney)
        allInComingMoney.text = fileContent[0]

        fileContent.subList(1, fileContent.size).forEach {
            val (description, cost) = it.split(CALCULATION_DATA_INLINE_SEPARATOR)
            descriptionAndCostRow.newCostRow(description, cost)
            setLoadedCalculationDisplay(
                calculationName.substringBeforeLast(
                    CALCULATION_DATA_FILE_EXTENSION
                )
            )
        }
    }

    fun createCalculation(view: View) {
        val fixCostsSum = descriptionAndCostRow.fixCosts.toList()
            .filter { it.cost.text.toString().toIntOrNull() != null }
            .sumBy { it.cost.text.toString().toInt() }

        var allInComingMoneyAsNumber = findViewById<TextView>(R.id.allInComingMoney).text.toString().toIntOrNull()
        allInComingMoneyAsNumber = if (allInComingMoneyAsNumber == null) 0 else allInComingMoneyAsNumber

        val releaseForSaving = allInComingMoneyAsNumber - fixCostsSum
        val savableMoneyField = findViewById<TextView>(R.id.savableMoney)
        savableMoneyField.text = releaseForSaving.toString()
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

    private fun setLoadedCalculationDisplay(loadedCalcuclationName: String) {
        val loadedCalcuclationNameDisplay = findViewById<TextView>(R.id.loadedCalcuclationNameDisplay)
        loadedCalcuclationNameDisplay.text = getString(R.string.loaded, loadedCalcuclationName)
    }

    private fun saveToLocalDriveThenUploadToGoogleDrive(fileName: String) {
        val allIncomingMoney = findViewById<EditText>(R.id.allInComingMoney).text.toString()

        val saveableString =
            descriptionAndCostRow.fixCosts.joinToString(separator = System.lineSeparator()) { it.description.text.toString() + CALCULATION_DATA_INLINE_SEPARATOR + it.cost.text.toString() }

        val locallySavedFile =
            File(applicationContext.filesDir.path + "/" + fileName + CALCULATION_DATA_FILE_EXTENSION)
        locallySavedFile.writeText(allIncomingMoney + System.lineSeparator() + saveableString)
        googleDriveSyncHandler.uploadOrUpdateFile(locallySavedFile)
    }

    val filesFromGoogleDrive = { _: DialogInterface, _: Int ->
        googleDriveSyncHandler.queryFileList()
    }

    val createNewEmptyCalculation = { _: DialogInterface, _: Int ->
        descriptionAndCostRow.deleteAllRows()
        descriptionAndCostRow.newCostRow()
        setLoadedCalculationDisplay(getString(R.string.new_unsaved))
    }

    val saveCalculationAsAFile = { dialogInterface: DialogInterface, _: Int ->
        val calclulationName = (dialogInterface as AlertDialog).calculationName

        saveToLocalDriveThenUploadToGoogleDrive(calclulationName.text.toString())
        setLoadedCalculationDisplay(calclulationName.text.toString())
        Toast.makeText(
            applicationContext,
            getString(R.string.saved_successfully, calclulationName.text),
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        googleDriveSyncHandler.authActivityResultHandler(requestCode, resultCode, data)
    }
}
