package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement

class Universo : FlyMode("Universocraft") {
    private val onlyDamageValue = BoolValue("${valuePrefix}OnlyDamage", true)
    private val selfDamageValue = BoolValue("${valuePrefix}SelfDamage", true)


    var stage = 0
    var ticks = 0
    var timer = MSTimer()
    var movespeed = 0.0
    private var waitFlag = false
    private var isStarted = false
    var isDamaged = false
    var dmgJumpCount = 0
    var flyTicks = 0


    fun runSelfDamageCore(): Boolean {
        mc.timer.timerSpeed = 1.0f
        if (!onlyDamageValue.get() || !selfDamageValue.get()) {
            if (onlyDamageValue.get()) {
                if (mc.thePlayer.hurtTime > 0 || isDamaged) {
                    isDamaged = true
                    dmgJumpCount = 999
                    return false
                }else {
                    return true
                }
            }
            isDamaged = true
            dmgJumpCount = 999
            return false
        }
        if (isDamaged) {
            dmgJumpCount = 999
            return false
        }
        mc.thePlayer.jumpMovementFactor = 0.00f
        if (mc.thePlayer.onGround) {
            if (dmgJumpCount >= 4) {
                mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                isDamaged = true
                mc.timer.timerSpeed = 0.05f
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.25, mc.thePlayer.posZ);
                dmgJumpCount = 999
                return false
            }
            dmgJumpCount++
            MovementUtils.resetMotion(true)
            mc.thePlayer.jump()
        }
        MovementUtils.resetMotion(false)
        return true
    }

    override fun onUpdate(event: UpdateEvent) {
        if (runSelfDamageCore()) {
            return
        }
        mc.thePlayer.jumpMovementFactor = 0.00f
        if (!isStarted && !waitFlag) {
            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.0784, mc.thePlayer.posZ, false))
            waitFlag = true
            mc.timer.timerSpeed = 0.05f
        }

        if (isStarted) {
                mc.timer.timerSpeed = 1.0f
                if (!mc.gameSettings.keyBindSneak.isKeyDown) {
                    MovementUtils.resetMotion(true)
                    if (mc.gameSettings.keyBindJump.isKeyDown) {
                        mc.thePlayer.motionY = 0.42
                    }
                }
            }


            flyTicks++
            if (flyTicks > 4) {
                flyTicks = 4
            }
        }

    override fun onEnable() {
        flyTicks = 0
        movespeed = 1.0
        waitFlag = false
        isStarted = false
        isDamaged = false
        dmgJumpCount = 0
        MovementUtils.resetMotion(true);
        timer.reset()
        runSelfDamageCore()
    }


    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer && (dmgJumpCount < 4 && selfDamageValue.get())) {
            packet.onGround = false
        }
    }

    override fun onDisable() {
        MovementUtils.resetMotion(true)
        timer.reset()
    }

    override fun onMove(event: MoveEvent) {
        if (isDamaged) {
            val pos = mc.thePlayer.position.add(0.0, 3.0, 0.0)
            PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(pos, 1, ItemStack(Blocks.sand.getItem(mc.theWorld, pos)), 0.0F, 0.5F + Math.random().toFloat() * 0.44.toFloat(), 0.0F))
            event.y = 0.0
        }
        movespeed -= movespeed / 156.0
        MovementUtils.strafe(movespeed.toFloat())
    }
}