package com.example.firsttest

import android.app.AlertDialog
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

    private lateinit var fixKoltsegSorKezelo:FixKoltsegSorKezelo

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
    }

    private fun createBetoltGombKezelo() {
        val betoltGomb = findViewById<Button>(R.id.betolt)
        betoltGomb.setOnClickListener {

            betoltesKerdes(mainLayout)
        }
    }

    private fun kivalasztottKalkulaciotBetolt(fixKoltsegSorLetrehozo: FixKoltsegSorKezelo, fajlNev: String) {
        fixKoltsegSorLetrehozo.torolMindenSort()

        val fromFile = File(applicationContext.filesDir.path + "/" + fajlNev).readLines()

        fromFile.forEach {
            val (leiras, koltseg) = it.split(";")
            fixKoltsegSorLetrehozo.ujSor(leiras, koltseg)
            betoltveMegjelenitotBeallit(fajlNev.substringBeforeLast(".txt"))
        }
    }

    private fun betoltveMegjelenitotBeallit(betoltott: String) {
        val betoltveTextField = findViewById<TextView>(R.id.betoltveTextField)
        betoltveTextField.text = "(betöltve: ${ betoltott })"
    }

    private fun createMentGombKezelo() {
        val mentGomb = findViewById<Button>(R.id.ment)
        mentGomb.setOnClickListener {
            createFajlNevBekero(mainLayout)
        }
    }

    private fun mentFajlba(fajlNev: String) {
        val saveableString =
            fixKoltsegSorKezelo.fixKoltsegek.joinToString(separator = "\n") { it.leiras.text.toString() + ";" + it.koltseg.text.toString() }

        File(applicationContext.filesDir.path + "/" + fajlNev + ".txt").writeText(saveableString)
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
                Toast.makeText(applicationContext, "Sikeresen mentve '${editText.text}' néven", Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }

    private fun createSavedCalculationChooser(view: View) {

        val items = File(applicationContext.filesDir.path)
            .listFiles { file -> file.name.endsWith(".txt") }
            .map { it.name.substringBeforeLast(".txt") }
            .toTypedArray()

        val builder = AlertDialog.Builder(this)
        with(builder)
        {
            setTitle("Mentett kalkulációk")
            setItems(items) { dialog, which ->
                kivalasztottKalkulaciotBetolt(fixKoltsegSorKezelo, items[which] + ".txt")
            }

            setPositiveButton("Mégse") { _, i -> Unit}
            show()
        }
    }

    private fun betoltesKerdes(view: View) {

        val builder = AlertDialog.Builder(this)

        with(builder)
        {
            setTitle("Figyelem!")
            setMessage("Új betöltése esetén az aktuális adatok elvesznek!")
            setPositiveButton("OK") { _, i -> createSavedCalculationChooser(mainLayout) }
            setNegativeButton(android.R.string.no, { _, i -> Unit})
            show()
        }


    }
}
