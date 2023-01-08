// LiquidX Development by PrahXZ and Haflin with FDP Base modified. v2.0 R1
package net.ccbluex.liquidbounce.utils.timer

class TickTimer {
    private var tick = 0

    fun update() {
        tick++
    }

    fun reset() {
        tick = 0
    }

    fun hasTimePassed(ticks: Int): Boolean {
        return tick >= ticks
    }
}