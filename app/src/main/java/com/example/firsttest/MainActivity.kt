package com.example.firsttest

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val fixKoltsegek: MutableList<FixKoltseg> = mutableListOf<FixKoltseg>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fixKoltsegSorLetrehozo =
            FixKoltsegSorLetrehozo(fixKoltsegek, resources, this, mainLayout)

        fixKoltsegSorLetrehozo.ujSor()

        val hozzaadGomb = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        hozzaadGomb.setOnClickListener {
            fixKoltsegSorLetrehozo.ujSor()
        }

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

    fun calculate(fixKoltsegek: MutableList<FixKoltseg>): Int = fixKoltsegek.toList()
        .filter { it.koltseg.text.toString().toIntOrNull() != null }
        .sumBy { it.koltseg.text.toString().toInt() }
}
