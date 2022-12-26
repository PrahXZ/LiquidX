package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.ccbluex.liquidbounce.features.value.*
import net.minecraft.block.BlockAir
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB
import kotlin.math.sqrt
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.stats.StatList
import kotlin.math.cos
import kotlin.math.sin


class UniversoTSWFly : FlyMode("UniversoTSW") {
    private val timescale = FloatValue("${valuePrefix}Timer", 1f, 0.1f, 10f)
    private val timer = TickTimer()
    private val SpeedValue = FloatValue("UniSpeedFloat", 0.67f, 0.1f, 1f)
    private val GroundValue = FloatValue("UniSpeedGround", 0.37f, 0.1f, 1f)
    private val JumpValue = FloatValue("UniSpeedJumpDelay", 4f, 1f, 10f)

    private var times = 0
    private var waitTicks = 0

    override fun onEnable() {
        times = 0

        ClientUtils.displayChatMessage("§8[§6§lUniverso§b§lTSW§8] §fSolo usalo en TeamSkyWars para mas seguridad.")
    }

    override fun onUpdate (event: UpdateEvent) {
        fly.antiDesync = true
        mc.timer.timerSpeed = timescale.get()

    }

    override fun onMove(event: MoveEvent) {
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                MovementUtils.strafe(GroundValue.get())
                waitTicks++
                mc.timer.timerSpeed = timescale.get()
                if (waitTicks >= JumpValue.get()) {
                    waitTicks = 0
                    mc.thePlayer.triggerAchievement(StatList.jumpStat)
                    mc.thePlayer.motionY = 0.0
                    event.y = 1.1999998688698
                }
            } else {
                MovementUtils.strafe(SpeedValue.get())
            }
        }
    }


    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= fly.launchY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }

    override fun onJump(event: JumpEvent) {
        event.cancelEvent()
    }

    override fun onStep(event: StepEvent) {
        event.stepHeight = 0f
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            packet.onGround = false

        }
        if (packet is S08PacketPlayerPosLook) {
            val deltaX = packet.x - mc.thePlayer.posX
            val deltaY = packet.y - mc.thePlayer.posY
            val deltaZ = packet.z - mc.thePlayer.posZ
            if (sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) < 10) {
                event.cancelEvent()
                PacketUtils.sendPacketNoEvent(
                        C03PacketPlayer.C06PacketPlayerPosLook(
                                packet.x,
                                packet.y,
                                packet.z,
                                packet.getYaw(),
                                packet.getPitch(),
                                false
                        )
                )
            }
        }
    }
}

