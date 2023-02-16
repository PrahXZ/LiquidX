package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.exploit.ABlink
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.java.games.input.Keyboard
import net.minecraft.item.ItemEnderPearl
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.BlockPos

class PearlFly : FlyMode("Pearl") {

    private val pearlActivateCheck = ListValue("PearlActiveCheck", arrayOf("Teleport", "Damage"), "Teleport")
    private var speedvalue = FloatValue ("Speed", 1f, 0f, 10f)


    // XD

    private var pearlState = 0


    // Maraca conchetumare
    private val pearlSlot: Int
        private get() {
            for (i in 36..44) {
                val stack = mc.thePlayer.inventoryContainer.getSlot(i).stack
                if (stack != null && stack.item is ItemEnderPearl) {
                    return i - 36
                }
            }
            return -1
        }

    override fun onEnable() {
        pearlState = 0
    }

    override fun onUpdate(event: UpdateEvent) {
            mc.thePlayer.capabilities.isFlying = false
            run {
                mc.thePlayer.motionZ = 0.0
                mc.thePlayer.motionY = mc.thePlayer.motionZ
                mc.thePlayer.motionX = mc.thePlayer.motionY
            }
            val enderPearlSlot = pearlSlot
            if (pearlState == 0) {
                if (enderPearlSlot == -1) {
                    LiquidBounce.hud.addNotification(Notification("PearlFly", "You don't have any ender pearl!", NotifyType.ERROR,1000, 5000))
                    pearlState = -1
                    LiquidBounce.moduleManager[Fly::class.java]!!.state = false
                    return
                }
                if (mc.thePlayer.inventory.currentItem != enderPearlSlot) {
                    mc.thePlayer.sendQueue.addToSendQueue(C09PacketHeldItemChange(enderPearlSlot))
                }
                mc.thePlayer.sendQueue.addToSendQueue(
                        C03PacketPlayer.C05PacketPlayerLook(
                                mc.thePlayer.rotationYaw,
                                90f,
                                mc.thePlayer.onGround
                        )
                )
                mc.thePlayer.sendQueue.addToSendQueue(
                        C08PacketPlayerBlockPlacement(
                                BlockPos(-1, -1, -1),
                                255,
                                mc.thePlayer.inventoryContainer.getSlot(enderPearlSlot + 36).stack,
                                0f,
                                0f,
                                0f
                        )
                )
                if (enderPearlSlot != mc.thePlayer.inventory.currentItem) {
                    mc.thePlayer.sendQueue.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                }
                pearlState = 1
            }
            if (pearlActivateCheck.get()
                            .equals("damage", ignoreCase = true) && pearlState == 1 && mc.thePlayer.hurtTime > 0
            ) pearlState = 2
            if (pearlState == 2) {
                if (mc.gameSettings.keyBindJump.isKeyDown) {
                    mc.thePlayer.motionY += speedvalue.get().toDouble()
                }
                if (mc.gameSettings.keyBindSneak.isKeyDown) {
                    mc.thePlayer.motionY -= speedvalue.get().toDouble()
                    mc.gameSettings.keyBindSneak.pressed = false
                }
                MovementUtils.strafe(speedvalue.get())
            }
        }

    override fun onPacket(event: PacketEvent) {
        var packet = event.packet

        if (packet is S08PacketPlayerPosLook) {

            if (pearlActivateCheck.equals("Teleport") && pearlState == 1) {
                pearlState = 2
            }
        }
    }

    override fun onMove(event: MoveEvent) {
        if (pearlState != 2 && pearlState != -1) {
            event.cancelEvent()
        }
    }

}