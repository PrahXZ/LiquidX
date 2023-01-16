package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.universo

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.NewKillAura
import net.ccbluex.liquidbounce.features.module.modules.exploit.ABlink
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.Value
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.settings.GameSettings

class UniversoNEW : SpeedMode("UniversoNEW") {
    private var ticks = 0
    private var loli = 0
    private val motiony = FloatValue("MY", 0.22f,0f, 1f)

    override fun onDisable() {
        LiquidBounce.moduleManager[NewKillAura::class.java]!!.keepSprintValue.get().equals(false)
    }
    override fun onUpdate() {

        ticks++
        if (!mc.thePlayer.onGround && ticks == 3) {
            mc.thePlayer.motionY = motiony.get().toDouble()

        }
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            mc.gameSettings.keyBindJump.pressed = false
            mc.thePlayer.jump()
            mc.thePlayer.motionY = 0.42
            //mc.thePlayer.motionY = 0.3001145141919810
            if (MovementUtils.getSpeed() < 0.22) {
                MovementUtils.strafe(0.52f)
            }else {
                MovementUtils.strafe(0.62f)
            }
        }
        if (!MovementUtils.isMoving()) {
            mc.thePlayer.motionY = 0.33319999363422365
            mc.thePlayer.motionX = 1.01
            mc.thePlayer.motionZ = 1.01
        }
    }
    }
