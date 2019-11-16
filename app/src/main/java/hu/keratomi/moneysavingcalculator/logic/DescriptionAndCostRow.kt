package hu.keratomi.moneysavingcalculator.logic

import android.content.Context
import android.content.res.Resources
import android.text.InputType
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout
import hu.keratomi.moneysavingcalculator.MainActivity
import hu.keratomi.moneysavingcalculator.R
import hu.keratomi.moneysavingcalculator.data.FixCost
import kotlinx.android.synthetic.main.activity_main.*


class DescriptionAndCostRow(
    val resources: Resources,
    val mainActivity: MainActivity,
    val mainLayout: LinearLayout
) {
    val fixCosts: MutableList<FixCost> = mutableListOf<FixCost>()

    fun newCostRow(description: String, cost: String) {
        newCostRow()
        fixCosts[fixCosts.lastIndex].description.setText(description)
        fixCosts[fixCosts.lastIndex].cost.setText(cost)
    }

    fun newCostRow() {
        val rowContainer = createNewRowContainer()

        val descriptionField = newEditText(
            InputType.TYPE_CLASS_TEXT,
            resources.getDimensionPixelSize(R.dimen.edittext_leiras_width)
        )
        rowContainer.addView(descriptionField)

        val costField = newEditText(
            InputType.TYPE_CLASS_NUMBER,
            resources.getDimensionPixelSize(R.dimen.edittext_koltseg_width)
        )
        rowContainer.addView(costField)

        val newFixCost =
            FixCost(descriptionField, costField)

        val deleteButton = createRowDeleteButton(rowContainer, newFixCost)
        rowContainer.addView(deleteButton)


        fixCosts.add(newFixCost)
    }

    fun createAddNewRow() {
        val rowContainer = createNewRowContainer(true)

        val addNewRowButton = ImageButton(mainActivity)
        addNewRowButton.setImageResource(android.R.drawable.ic_input_add)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        addNewRowButton.layoutParams = layoutParams

        addNewRowButton.setOnClickListener {
            newCostRow()
            mainActivity.scrollView2.post(Runnable {
                mainActivity.scrollView2.fullScroll(ScrollView.FOCUS_DOWN)
                fixCosts.last().description.requestFocus()
                val imm = mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(fixCosts.last().description, InputMethodManager.SHOW_IMPLICIT)
            })
        }

        rowContainer.addView(addNewRowButton)
    }

    fun deleteOneCostRow(rowContainer: LinearLayout, fixCost: FixCost) {
        mainLayout.removeView(rowContainer)
        fixCosts.remove(fixCost)
    }

    fun deleteAllRows() {
        fixCosts.forEachIndexed { index, element ->
            mainLayout.removeView(mainLayout.findViewWithTag<LinearLayout>("contener_${index}"))
        }

        fixCosts.clear()
    }

    private fun createNewRowContainer(forAddNewRowButton: Boolean = false): LinearLayout {
        val rowContainer = LinearLayout(mainActivity)
        rowContainer.tag = "contener_" + fixCosts.size
        val layoutParams = ConstraintLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            resources.getDimensionPixelSize(R.dimen.kontener_height)
        )
        layoutParams.orientation = LinearLayout.HORIZONTAL
        rowContainer.setLayoutParams(
            layoutParams
        )
        rowContainer.x = 0F

        if (forAddNewRowButton) {
            mainLayout.addView(rowContainer)
        } else {
            mainLayout.addView(rowContainer, mainLayout.childCount - 1)
        }
        return rowContainer
    }

    private fun createRowDeleteButton(
        rowContainer: LinearLayout,
        fixCost: FixCost
    ): ImageButton {
        val deleteButton = ImageButton(mainActivity)
        deleteButton.setImageResource(android.R.drawable.ic_delete)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        deleteButton.layoutParams = layoutParams

        deleteButton.setOnClickListener {
            deleteOneCostRow(rowContainer, fixCost)
        }

        return deleteButton
    }

    private fun newEditText(inputType: Int, width: Int): EditText {
        val newlyCreatedField = EditText(mainActivity)
        newlyCreatedField.tag = "fi_" + inputType + "_" + fixCosts.size
        newlyCreatedField.inputType = inputType
        newlyCreatedField.minWidth = width

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        newlyCreatedField.layoutParams = layoutParams

        if (inputType == 1) {
            newlyCreatedField.hint = mainActivity.getString(R.string.description)
        } else {
            newlyCreatedField.hint = mainActivity.getString(R.string.cost)
        }

        return newlyCreatedField
    }
}