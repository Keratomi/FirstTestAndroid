package com.example.firsttest

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    val fixKoltsegek: MutableList<FixKoltseg> = mutableListOf<FixKoltseg>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fixKoltsegSorLetrehozo =
            FixKoltsegSorKezelo(fixKoltsegek, resources, this, mainLayout)

        fixKoltsegSorLetrehozo.ujSor()

        createUjSortHozzaadGombKezelo(fixKoltsegSorLetrehozo)

        createOsszegGombKezelo()

        val mentGomb = findViewById<Button>(R.id.ment)
        mentGomb.setOnClickListener {

            val saveableString = fixKoltsegek.joinToString(separator = "\n") { it.leiras.text.toString() + ";" + it.koltseg.text.toString() }

            File(applicationContext.filesDir.path + "elsoCsakTeszt.txt").writeText(saveableString)
        }

        val betoltGomb = findViewById<Button>(R.id.betolt)
        betoltGomb.setOnClickListener {

            fixKoltsegSorLetrehozo.torolMindenSort()

            val fromFile = File(applicationContext.filesDir.path + "elsoCsakTeszt.txt").readLines()

            fromFile.forEach {
                val (leiras, koltseg) = it.split(";")
                fixKoltsegSorLetrehozo.ujSor(leiras, koltseg)
            }
        }
    }

    private fun createUjSortHozzaadGombKezelo(fixKoltsegSorKezelo: FixKoltsegSorKezelo) {
        val hozzaadGomb = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        hozzaadGomb.setOnClickListener {
            fixKoltsegSorKezelo.ujSor()
        }
    }

    private fun createOsszegGombKezelo() {
        val osszegGomb = findViewById<Button>(R.id.osszegez)
        osszegGomb.setOnClickListener {
            val fixKoltsegOsszeg = calculate(fixKoltsegek)
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
}
