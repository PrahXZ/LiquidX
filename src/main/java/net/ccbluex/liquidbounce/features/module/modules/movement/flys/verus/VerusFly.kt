package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.block.BlockAir
import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.AxisAlignedBB

class VerusFly : FlyMode("Verus") {

    private var flys = ListValue("Verus-Mode", arrayOf("VerusJump", "VerusCollide", "VerusBasic"), "VerusJump")

    // Val
    private val boostValue = BoolValue("VerusJump-Boost", false).displayable { flys.equals("VerusJump") }
    private val speedValue = FloatValue("VerusJump-Speed", 2f, 0f, 3f).displayable { boostValue.get() && flys.equals("VerusJump") }
    private val boostLength = IntegerValue("VerusJump-BoostTime", 500, 300, 1000).displayable { boostValue.get() && flys.equals("VerusJump") }
    private val moveBeforeDamage = BoolValue("VerusJump-MoveBeforeDamage", true).displayable { boostValue.get() && flys.equals("VerusJump") }
    private val airStrafeValue = BoolValue("VerusJump-AirStrafe", true).displayable { flys.equals("VerusJump") }

    private val verusBasicMode = ListValue("VerusBasic-Mode", arrayOf("Packet1", "Packet2"), "Packet1").displayable { flys.equals("VerusBasic") }

    // Var
    private var times = 0 // VerusJump
    private var timer = MSTimer() // VerusJump
    private var ticks = 0 // VerusCollide
    private var justEnabled = true // VerusCollide


    override fun onEnable() {
        times = 0
        timer.reset()

        ticks = 0
        justEnabled = true
    }

    override fun onUpdate(event: UpdateEvent) {
        when (flys.get()) {
            "VerusJump" -> {
                if (boostValue.get()) {
                    mc.gameSettings.keyBindJump.pressed = false
                    if (times < 5 && !moveBeforeDamage.get()) {
                        MovementUtils.strafe(0f)
                    }
                    if (mc.thePlayer.onGround && times < 5) {
                        times++
                        timer.reset()
                        if (times <5) {
                            mc.thePlayer.jump()
                            MovementUtils.strafe(0.48F)
                        }
                    }

                    if (times >= 5) {
                        if (!timer.hasTimePassed(boostLength.get().toLong())) {
                            MovementUtils.strafe(speedValue.get())
                        } else {
                            times = 0
                        }
                    }
                } else {
                    mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                    if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
                        mc.gameSettings.keyBindJump.pressed = false
                        mc.thePlayer.jump()
                        MovementUtils.strafe(0.48F)
                    } else if(airStrafeValue.get()) {
                        MovementUtils.strafe()
                    }
                }
            }
            "VerusBasic" -> {
                if(verusBasicMode.get() === "Packet1") {
                    if(mc.thePlayer.motionY < 0.4) {
                        mc.thePlayer.motionY = 0.0
                    }
                    mc.thePlayer.onGround = true
                }
            }
        }
    }

    override fun onMove(event: MoveEvent) {
        when (flys.get()) {
            "VerusCollide" -> {
                mc.gameSettings.keyBindJump.pressed = false
                mc.gameSettings.keyBindSneak.pressed = false
                if (ticks % 14 == 0 && mc.thePlayer.onGround) {
                    justEnabled = false
                    MovementUtils.strafe(0.69f)
                    event.y = 0.42
                    ticks = 0
                    mc.thePlayer.motionY = -(mc.thePlayer.posY - Math.floor(mc.thePlayer.posY))
                } else {
                    if (GameSettings.isKeyDown(mc.gameSettings.keyBindJump) && ticks % 2 == 1) {
                        if (mc.thePlayer.ticksExisted % 2 == 0) {
                            mc.thePlayer.motionY = 0.42;
                            MovementUtils.strafe(0.3f);
                        }
                    }
                    if (mc.thePlayer.onGround) {
                        if (!justEnabled) {
                            MovementUtils.strafe(1.01f)
                        }
                    } else {
                        MovementUtils.strafe(0.41f)
                    }
                }
                ticks++
            }
            "VerusBasic" -> {
                val pos = mc.thePlayer.position.add(0.0, -1.5, 0.0)
                PacketUtils.sendPacketNoEvent(
                        C08PacketPlayerBlockPlacement(pos, 1,
                                ItemStack(Blocks.stone.getItem(mc.theWorld, pos)), 0.0F, 0.5F + Math.random().toFloat() * 0.44.toFloat(), 0.0F)
                )
                if(mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    event.y = 0.42
                }else {
                    event.y = 0.0
                    MovementUtils.strafe(0.35f)
                }
            }
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        when (flys.get()) {
            "VerusJump" -> {
                if(boostValue.get()) {
                    if (packet is C03PacketPlayer) {
                        packet.onGround = (times >= 5 && !timer.hasTimePassed(boostLength.get().toLong()))
                    }
                }
            }
            "VerusBasic" -> {
                if(packet is C03PacketPlayer) {
                    if(verusBasicMode.get() === "Packet1") {
                        packet.onGround = true
                    }
                }
            }
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {

        if (flys.equals("VerusJump") || flys.equals("VerusCollide")) {
            if (event.block is BlockAir && event.y <= fly.launchY) {
                event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
            }
        }
    }

    override fun onJump(event: JumpEvent) {
        if (flys.equals("VerusCollide")) event.cancelEvent()
    }

}