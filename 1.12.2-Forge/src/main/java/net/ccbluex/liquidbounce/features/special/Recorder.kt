package net.ccbluex.liquidbounce.features.special

import net.ccbluex.liquidbounce.event.Listenable

object Recorder : Listenable {
    //Combat Manager
    var killCounts = 0
    var totalPlayed = 0
    var win = 0

    //Title Time
    var second = 0
    var tick = 0
    var minute = 0
    var hour = 0
    var startTime = System.currentTimeMillis()

    override fun handleEvents() = true
}