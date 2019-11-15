package hu.keratomi.moneysavingcalculator.logic

import com.google.gson.Gson
import hu.keratomi.moneysavingcalculator.data.Calculation
import hu.keratomi.moneysavingcalculator.data.FixCost
import hu.keratomi.moneysavingcalculator.data.FixCostContent
import java.math.BigDecimal

fun createJsonString(
    allIncomingMoney: String,
    fixCosts: MutableList<FixCost>
): String {

    val calculation = Calculation(
        BigDecimal(allIncomingMoney),
        fixCosts
            .map { FixCostContent(it.description.text.toString(), BigDecimal(it.cost.text.toString())) }
            .toMutableList())


    return Gson().toJson(calculation)
}

fun createCalculationFromJson(jsonString: String): Calculation {
    return Gson().fromJson(jsonString, Calculation::class.java)
}