package net.ccbluex.liquidbounce.features.module.modules.movement.flys.zonecraft

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.*
import net.minecraft.block.BlockAir
import net.minecraft.stats.StatList
import net.minecraft.util.AxisAlignedBB

class ZoneFly : FlyMode("ZoneCraftFast") {

    private val TickLow = FloatValue("Ticks", 0f, 0f, 10f)
    private val Speed1 = FloatValue("Speed1", 0.57f, 0f, 10f)
    private val Speed2 = FloatValue("Speed2", 0.80f, 0f, 10f)


    private var waitTicks = 0

    override fun onEnable() {
        waitTicks = 0
    }

    override fun onMove(event: MoveEvent) {
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                MovementUtils.strafe(Speed1.get())
                waitTicks++
                mc.timer.timerSpeed = 0.56f
                if (waitTicks >= TickLow.get()) {
                    waitTicks = 0
                    mc.thePlayer.triggerAchievement(StatList.jumpStat)
                    mc.thePlayer.motionY = 0.00
                    event.y = 0.13999998688698
                }
            } else {
                MovementUtils.strafe(Speed2.get())
            }
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= fly.launchY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }

    override fun onJump(event: JumpEvent) = event.cancelEvent()
}