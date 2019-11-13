package com.example.firsttest

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
        betoltveMegjelenitotBeallit("új, mentetlen")

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
            betoltveMegjelenitotBeallit(calculationName)
        }
    }

    private fun betoltveMegjelenitotBeallit(betoltott: String) {
        val betoltveTextField = findViewById<TextView>(R.id.betoltveTextField)
        betoltveTextField.text = "(betöltve: ${betoltott})"
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
        googleDrivebaSzinkronizalo.uploadFileToGoogleDrive(mentettFile)
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
        builder.setTitle("Milyen néven mentsem?")
        val dialogLayout = inflater.inflate(R.layout.alert_dialog_with_edittext, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.mentendoFajlNeve)
        builder.setView(dialogLayout)
        builder.setPositiveButton("OK") { _, i ->
            mentFajlba(editText.text.toString())
            betoltveMegjelenitotBeallit(editText.text.toString())
            Toast.makeText(
                applicationContext,
                "Sikeresen mentve '${editText.text}' néven",
                Toast.LENGTH_SHORT
            ).show()
        }
        builder.show()
    }

    fun createMentettKalkulacioLista(items: Array<String>) {
        val builder = AlertDialog.Builder(this)
        with(builder)
        {
            setTitle("Mentett kalkulációk")
            setItems(items) { dialog, which ->
                googleDrivebaSzinkronizalo.readFileFromGoogleDrive(items[which] + ".txt")
            }

            setPositiveButton("Mégse") { _, i -> Unit }
            show()
        }
    }

    val filesFromGoogleDrive = { dialog: DialogInterface, which: Int ->
        googleDrivebaSzinkronizalo.fajlListatLeker()
    }

    val createUjKalkulacio = { dialog: DialogInterface, which: Int ->
        fixKoltsegSorKezelo.torolMindenSort()
        fixKoltsegSorKezelo.ujSor()
        betoltveMegjelenitotBeallit("új, mentetlen")

        Unit
    }

    private fun betoltesKerdes(view: View, okFunction: (dialog: DialogInterface, which: Int) -> Unit) {

        val builder = AlertDialog.Builder(this)

        with(builder)
        {
            setTitle("Figyelem!")
            setMessage("Új betöltése esetén az aktuális adatok elvesznek!")
            setPositiveButton(
                "OK",
                DialogInterface.OnClickListener(function = okFunction)
            )
            setNegativeButton(android.R.string.no, { _, i -> Unit })
            show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        googleDrivebaSzinkronizalo.authActivityResultHandler(requestCode, resultCode, data)
    }
}
