package hu.keratomi.moneysavingcalculator.logic

import android.content.res.Resources
import android.widget.LinearLayout
import hu.keratomi.moneysavingcalculator.MainActivity
import hu.keratomi.moneysavingcalculator.R
import hu.keratomi.moneysavingcalculator.data.FixCost


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

        val newFixCost =
            FixCost(rowContainer.findViewById(R.id.descriptionField), rowContainer.findViewById(R.id.costField))

        fixCosts.add(newFixCost)

        rowContainer.tag = newFixCost
    }

    fun createAddNewRowButton() {
        createNewRowContainer(true)
    }

    fun deleteOneCostRow(rowContainer: LinearLayout) {
        mainLayout.removeView(rowContainer)
        fixCosts.remove(rowContainer.tag)
    }

    fun deleteAllRows() {
        fixCosts.forEachIndexed { index, element ->
            mainLayout.removeView(mainLayout.findViewWithTag<LinearLayout>(element))
        }

        fixCosts.clear()
    }

    private fun createNewRowContainer(forAddNewRowButton: Boolean = false): LinearLayout {
        lateinit var rowContainer: LinearLayout
        if (forAddNewRowButton) {
            rowContainer = mainActivity.layoutInflater.inflate(R.layout.row_container_with_add_button, null) as LinearLayout
            mainLayout.addView(rowContainer)
        } else {
            rowContainer = mainActivity.layoutInflater.inflate(R.layout.row_container_with_input_fields, null) as LinearLayout
            mainLayout.addView(rowContainer, mainLayout.childCount - 1)
        }
        return rowContainer
    }

}