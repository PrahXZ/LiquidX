package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.TickEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.minecraft.client.settings.KeyBinding
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.potion.Potion


@ModuleInfo(name = "Sprint", category = ModuleCategory.MOVEMENT, defaultOn = true)
class Sprint : Module() {
    val modeValue = ListValue("Mode", arrayOf("Legit", "Custom"), "Legit")


    val jumpDirectionsValue = BoolValue("JumpDirectionFix", true)
    val useItemValue = BoolValue("UseItem", false)
    val allDirectionsValue = BoolValue("AllDirections", false)
    val blindnessValue = BoolValue("Blindness", false)
    val foodValue = BoolValue("Food", false)
    val noPacketValue = BoolValue("NoPacket", false)
    val checkServerSide = BoolValue("CheckServerSide", false)
    val checkServerSideGround = BoolValue("CheckServerSideOnlyGround", false)
    val packetomniSprint = BoolValue("PacketOmniSprint", false)

    override val tag: String
        get() = modeValue.get()

    @EventTarget
    fun onTick(event: TickEvent?) {
        if (modeValue.get().equals("legit", ignoreCase = true)) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.keyCode, true)
        }
    }


    override fun onEnable() {
        if(packetomniSprint.get()) {
            mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING))
        }
    }

    override fun onDisable() {
        if (modeValue.get().equals("legit", ignoreCase = true)) {
            val keyCode = mc.gameSettings.keyBindSprint.keyCode
            KeyBinding.setKeyBindState(keyCode, keyCode > 0 && mc.gameSettings.keyBindSprint.isKeyDown)
        }
        if(packetomniSprint.get()) {
            mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING))
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {

        if (modeValue.equals("legit")) {
            allDirectionsValue.value = false
            blindnessValue.value = false
            foodValue.value = false
            checkServerSide.value = false
            checkServerSideGround.value = false
            noPacketValue.value = false
            packetomniSprint.value = false
            useItemValue.value = false
        }


        if (modeValue.get().equals("custom", ignoreCase = true)) {
            if (!isMoving() || mc.thePlayer.isSneaking || blindnessValue.get() && mc.thePlayer.isPotionActive(Potion.blindness) || foodValue.get() && !(mc.thePlayer.foodStats.foodLevel > 6.0f || mc.thePlayer.capabilities.allowFlying) || (checkServerSide.get() && (mc.thePlayer.onGround || !checkServerSideGround.get())
                            && !allDirectionsValue.get() && RotationUtils.targetRotation != null) && RotationUtils.getRotationDifference(Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)) > 30) {
                mc.thePlayer.isSprinting = false
                return
            }
            if (allDirectionsValue.get() || mc.thePlayer.movementInput.moveForward >= 0.8f) mc.thePlayer.isSprinting = true
        }
    }


    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (noPacketValue.get() && packet is C0BPacketEntityAction && (packet.action == C0BPacketEntityAction.Action.START_SPRINTING || packet.action == C0BPacketEntityAction.Action.STOP_SPRINTING)) {
            event.cancelEvent()
        }
    }
}