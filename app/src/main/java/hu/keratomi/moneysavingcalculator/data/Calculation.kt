package hu.keratomi.moneysavingcalculator.data

import java.math.BigDecimal

data class Calculation(val allInComingMoney: BigDecimal, val fixCosts: MutableList<FixCostContent>)