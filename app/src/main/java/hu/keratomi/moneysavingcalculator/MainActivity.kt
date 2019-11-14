package hu.keratomi.moneysavingcalculator

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var fixKoltsegSorKezelo: FixKoltsegSorKezelo
    private lateinit var googleDrivebaSzinkronizalo: GoogleDrivebaSzinkronizalo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        googleDrivebaSzinkronizalo = GoogleDrivebaSzinkronizalo(this)
        googleDrivebaSzinkronizalo.googleAuth()

        fixKoltsegSorKezelo = FixKoltsegSorKezelo(resources, this, mainLayout)
        fixKoltsegSorKezelo.ujSor()
        betoltveMegjelenitotBeallit(getString(R.string.uj_mentetlen))

        createUjSortHozzaadGombKezelo()

        createOsszegGombKezelo()

        createMentGombKezelo()

        createBetoltGombKezelo()

        createUjUresGombKezelo()
    }

    private fun createUjUresGombKezelo() {
        val ujUresGomb = findViewById<Button>(R.id.ujUres)
        ujUresGomb.setOnClickListener {

            betoltesKerdes(mainLayout, createUjKalkulacio)
        }
    }

    private fun createBetoltGombKezelo() {
        val betoltGomb = findViewById<Button>(R.id.betolt)
        betoltGomb.setOnClickListener {

            betoltesKerdes(mainLayout, filesFromGoogleDrive)
        }
    }

    fun kivalasztottKalkulaciotBetolt(calculationName: String, fileContent: List<String>) {
        fixKoltsegSorKezelo.torolMindenSort()

        val befolyoOsszeg = findViewById<TextView>(R.id.befolyoOsszeg)
        befolyoOsszeg.text = fileContent[0]

        fileContent.subList(1, fileContent.size).forEach {
            val (leiras, koltseg) = it.split(";")
            fixKoltsegSorKezelo.ujSor(leiras, koltseg)
            betoltveMegjelenitotBeallit(calculationName.substringBeforeLast(".txt"))
        }
    }

    private fun betoltveMegjelenitotBeallit(betoltott: String) {
        val betoltveTextField = findViewById<TextView>(R.id.betoltveTextField)
        betoltveTextField.text = getString(R.string.betoltve, betoltott)
    }

    private fun createMentGombKezelo() {
        val mentGomb = findViewById<Button>(R.id.ment)
        mentGomb.setOnClickListener {
            createFajlNevBekero(mainLayout)
        }
    }

    private fun mentFajlba(fajlNev: String) {
        val befolyoOsszeg = findViewById<EditText>(R.id.befolyoOsszeg).text.toString()

        val saveableString =
            fixKoltsegSorKezelo.fixKoltsegek.joinToString(separator = System.lineSeparator()) { it.leiras.text.toString() + ";" + it.koltseg.text.toString() }

        val mentettFile = File(applicationContext.filesDir.path + "/" + fajlNev + ".txt")
        mentettFile.writeText(befolyoOsszeg + System.lineSeparator() + saveableString)
        googleDrivebaSzinkronizalo.uploadOrUpdateFile(mentettFile)
    }

    private fun createUjSortHozzaadGombKezelo() {
        val hozzaadGomb = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        hozzaadGomb.setOnClickListener {
            fixKoltsegSorKezelo.ujSor()
        }
    }

    private fun createOsszegGombKezelo() {
        val osszegGomb = findViewById<Button>(R.id.osszegez)
        osszegGomb.setOnClickListener {
            val fixKoltsegOsszeg = calculate(fixKoltsegSorKezelo.fixKoltsegek)
            val befolyoOsszeg = findViewById<TextView>(R.id.befolyoOsszeg)
            var befolyoOsszegSzamkent = befolyoOsszeg.text.toString().toIntOrNull()
            befolyoOsszegSzamkent = if (befolyoOsszegSzamkent == null) 0 else befolyoOsszegSzamkent

            val maradtText = findViewById<TextView>(R.id.megmaradtOsszg)
            val megtakaritas = befolyoOsszegSzamkent - fixKoltsegOsszeg
            maradtText.text = megtakaritas.toString()
        }
    }

    private fun calculate(fixKoltsegek: MutableList<FixKoltseg>): Int = fixKoltsegek.toList()
        .filter { it.koltseg.text.toString().toIntOrNull() != null }
        .sumBy { it.koltseg.text.toString().toInt() }

    private fun createFajlNevBekero(view: View) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle(getString(R.string.milyen_neven_mentsem))
        val dialogLayout = inflater.inflate(R.layout.alert_dialog_with_edittext, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.mentendoFajlNeve)
        builder.setView(dialogLayout)
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            mentFajlba(editText.text.toString())
            betoltveMegjelenitotBeallit(editText.text.toString())
            Toast.makeText(
                applicationContext,
                getString(R.string.sikeresen_mentve, editText.text),
                Toast.LENGTH_SHORT
            ).show()
        }
        builder.show()
    }

    fun createMentettKalkulacioLista(items: Array<String>) {
        val builder = AlertDialog.Builder(this)
        with(builder)
        {
            setTitle(R.string.mentett_kalkulaciok)
            setItems(items) { _, which ->
                googleDrivebaSzinkronizalo.readFileFromGoogleDrive(items[which] + ".txt")
            }

            setPositiveButton(android.R.string.cancel) { _, _ -> Unit }
            show()
        }
    }

    val filesFromGoogleDrive = { _: DialogInterface, _: Int ->
        googleDrivebaSzinkronizalo.fajlListatLeker()
    }

    val createUjKalkulacio = { _: DialogInterface, _: Int ->
        fixKoltsegSorKezelo.torolMindenSort()
        fixKoltsegSorKezelo.ujSor()
        betoltveMegjelenitotBeallit(getString(R.string.uj_mentetlen))

        Unit
    }

    private fun betoltesKerdes(view: View, okFunction: (dialog: DialogInterface, which: Int) -> Unit) {

        val builder = AlertDialog.Builder(this)

        with(builder)
        {
            setTitle(android.R.string.dialog_alert_title)
            setMessage(R.string.loose_current)
            setPositiveButton(
                android.R.string.ok,
                DialogInterface.OnClickListener(function = okFunction)
            )
            setNegativeButton(android.R.string.no) { _, _ -> Unit }
            show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        googleDrivebaSzinkronizalo.authActivityResultHandler(requestCode, resultCode, data)
    }
}
