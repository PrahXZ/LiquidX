package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.*
import net.minecraft.block.BlockAir
import net.minecraft.util.AxisAlignedBB
import net.minecraft.client.settings.GameSettings

class OldUniversoFly : FlyMode("OldUniversoJump") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 0f, 0f, 0f)
    private val timescale = FloatValue("${valuePrefix}Timer", 1f, 0.1f, 10f)

    private var times = 0
    private var ticks = 0
    private var timer = MSTimer()

    override fun onEnable() {
        times = 0
        timer.reset()
        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.2, mc.thePlayer.posZ)
        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.1, mc.thePlayer.posZ)
    }

    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.jumpMovementFactor = 0.0255f
        if (!mc.thePlayer.onGround && ticks > 3) {

            mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
            if (MovementUtils.getSpeed() < 0.215f && !mc.thePlayer.onGround) {
                MovementUtils.strafe(0.215f)

                mc.gameSettings.keyBindJump.pressed = false
                if (mc.thePlayer.onGround && times < 10000) {
                    times++
                    timer.reset()
                    if (times < 10000) {
                        mc.thePlayer.jump()

                        fly.antiDesync = true
                        mc.timer.timerSpeed = timescale.get()

                        if (timer.hasTimePassed(3)) {
                            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.0E-5, mc.thePlayer.posZ)
                            timer.reset()
                        }
                    }
                }
            }
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= fly.launchY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }
}
