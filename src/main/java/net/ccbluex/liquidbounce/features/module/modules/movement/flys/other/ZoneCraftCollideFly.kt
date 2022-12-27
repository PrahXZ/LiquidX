package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.script.api.global.Notifications
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.stats.StatList
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper

class ZoneCraftCollideFly: FlyMode("ZoneCraftCollide") {

    private var waitTicks = 0

    override fun onEnable() {
        if(mc.thePlayer.posY % 1 != 0.0) {
            fly.state = false
            LiquidBounce.hud.addNotification(Notification("Fly", "Necesitas estar en el suelo para volar.", NotifyType.ERROR))
            return
        }
        waitTicks = 0
    }

    override fun onUpdate(event: UpdateEvent) {
        RotationUtils.setTargetRotation(Rotation(mc.thePlayer.rotationYaw, 90f))
        if(LiquidBounce.moduleManager[KillAura::class.java]!!.target == null && !mc.thePlayer.isBlocking) {
            mc.netHandler.networkManager.sendPacket(C08PacketPlayerBlockPlacement(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ), 1, null, 0f, 1f, 0f))
        }
    }

    override fun onMove(event: MoveEvent) {
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                MovementUtils.strafe(0.2f)
                waitTicks++
                if (waitTicks >= 3) {
                    waitTicks = 0
                    mc.thePlayer.triggerAchievement(StatList.jumpStat)
                    mc.thePlayer.motionY = 0.0
                    event.y = 0.41999998688698
                }
            } else {
                MovementUtils.strafe(0.8f)
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