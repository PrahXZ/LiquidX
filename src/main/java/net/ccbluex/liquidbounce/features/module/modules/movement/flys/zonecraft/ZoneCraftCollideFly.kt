package net.ccbluex.liquidbounce.features.module.modules.movement.flys.zonecraft

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.combat.NewKillAura
import net.ccbluex.liquidbounce.features.module.modules.exploit.ABlink
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos

class ZoneCraftCollideFly: FlyMode("ZoneCraftCollide") {
    private val timer = FloatValue("${valuePrefix}Timer", 2f, 1f, 10f)
    private val timerconfig = FloatValue("Timer-1", 5f,0f, 10f )
    private val timerconfig2 = FloatValue("Timer-2", 10f, 0f, 10f)
    private val TP = BoolValue("TP-Agressive", false)
    private val timer2 = BoolValue("Boost", true)

    override fun onEnable() {
        if(mc.thePlayer.posY % 1 != 0.0) {
            fly.state = false
            LiquidBounce.hud.addNotification(Notification("Fly", "Necesitas estar en el suelo para volar.", NotifyType.ERROR))
            return
        }
        LiquidBounce.moduleManager[ABlink::class.java]!!.state = true
        if (timer2.get()) {
            mc.timer.timerSpeed = timer.get();
        }
        if (TP.get()) {
            timer2.value = false
        }   else {
            TP.value = false
        }

    }

    override fun onUpdate(event: UpdateEvent) {
        RotationUtils.setTargetRotation(Rotation(mc.thePlayer.rotationYaw, 90f))
        if((LiquidBounce.moduleManager[KillAura::class.java]!!.target == null || LiquidBounce.moduleManager[NewKillAura::class.java]!!.target == null) && !mc.thePlayer.isBlocking) {
            mc.netHandler.networkManager.sendPacket(C08PacketPlayerBlockPlacement(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ), 1, null, 0f, 1f, 0f))
        }
        if (TP.get()) {
            if(mc.thePlayer.ticksExisted % 15 < 10) {
                mc.timer.timerSpeed = timerconfig2.get()
            } else {
            mc.timer.timerSpeed = timerconfig.get()
            }
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1F;
        LiquidBounce.moduleManager[ABlink::class.java]!!.state = false
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= fly.launchY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }

    override fun onJump(event: JumpEvent) = event.cancelEvent()
}