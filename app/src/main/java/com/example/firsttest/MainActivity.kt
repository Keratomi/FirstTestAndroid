package com.example.firsttest

import android.app.AlertDialog
import android.content.DialogInterface
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fixKoltsegSorKezelo = FixKoltsegSorKezelo(resources, this, mainLayout)

        fixKoltsegSorKezelo.ujSor()
        betoltveMegjelenitotBeallit("új, mentetlen")

        createUjSortHozzaadGombKezelo()

        createOsszegGombKezelo()

        createMentGombKezelo()

        createBetoltGombKezelo()

        val ujUresGomb = findViewById<Button>(R.id.ujUres)
        ujUresGomb.setOnClickListener {

            betoltesKerdes(mainLayout, createUjKalkulacio)
        }
    }

    private fun createBetoltGombKezelo() {
        val betoltGomb = findViewById<Button>(R.id.betolt)
        betoltGomb.setOnClickListener {

            betoltesKerdes(mainLayout, createSavedCalculationChooser)
        }
    }

    private fun kivalasztottKalkulaciotBetolt(
        fajlNev: String
    ) {
        fixKoltsegSorKezelo.torolMindenSort()

        val fromFile = File(applicationContext.filesDir.path + "/" + fajlNev).readLines()

        val befolyoOsszeg = findViewById<TextView>(R.id.befolyoOsszeg)
        befolyoOsszeg.text = fromFile[0]

        fromFile.subList(1, fromFile.size).forEach {
            val (leiras, koltseg) = it.split(";")
            fixKoltsegSorKezelo.ujSor(leiras, koltseg)
            betoltveMegjelenitotBeallit(fajlNev.substringBeforeLast(".txt"))
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
            fixKoltsegSorKezelo.fixKoltsegek.joinToString(separator = "\n") { it.leiras.text.toString() + ";" + it.koltseg.text.toString() }

        File(applicationContext.filesDir.path + "/" + fajlNev + ".txt").writeText(befolyoOsszeg + "\n" + saveableString)
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

    val createSavedCalculationChooser = { dialog: DialogInterface, which: Int ->
        val items = File(applicationContext.filesDir.path)
            .listFiles { file -> file.name.endsWith(".txt") }
            .map { it.name.substringBeforeLast(".txt") }
            .toTypedArray()

        val builder = AlertDialog.Builder(this)
        with(builder)
        {
            setTitle("Mentett kalkulációk")
            setItems(items) { dialog, which ->
                kivalasztottKalkulaciotBetolt(items[which] + ".txt")
            }

            setPositiveButton("Mégse") { _, i -> Unit }
            show()
        }

        Unit
    }

    val createUjKalkulacio = { dialog: DialogInterface, which: Int ->
        fixKoltsegSorKezelo.torolMindenSort()
        fixKoltsegSorKezelo.ujSor()
        betoltveMegjelenitotBeallit("új, mentetlen")

        Unit
    }

    private fun betoltesKerdes(view: View, bigyo: (dialog: DialogInterface, which: Int) -> Unit) {

        val builder = AlertDialog.Builder(this)

        with(builder)
        {
            setTitle("Figyelem!")
            setMessage("Új betöltése esetén az aktuális adatok elvesznek!")
            setPositiveButton(
                "OK",
                DialogInterface.OnClickListener(function = bigyo)
            )
            setNegativeButton(android.R.string.no, { _, i -> Unit })
            show()
        }


    }
}
