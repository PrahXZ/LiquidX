// Import LB++ best Client, LiquidX R2WS2
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.Target
import net.ccbluex.liquidbounce.features.module.modules.exploit.Disabler
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.features.module.modules.misc.Teams
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.ccbluex.liquidbounce.features.module.modules.movement.TargetStrafe
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.features.module.modules.render.FreeCam
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.*
import net.minecraft.network.play.client.*
import net.minecraft.potion.Potion
import net.minecraft.util.*
import net.minecraft.world.WorldSettings
import org.lwjgl.opengl.GL11
import java.util.*
import kotlin.math.max
import kotlin.math.min


@ModuleInfo(name = "KillAura", category = ModuleCategory.COMBAT)
class KillAura : Module() {

    /**
     * OPTIONS
     */

    // CPS
    private val maxCPS: IntegerValue = object : IntegerValue("MaxCPS", 8, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minCPS.get()
            if (i > newValue) set(i)

            attackDelay = TimeUtils.randomClickDelay(minCPS.get(), this.get())
        }
    }

    private val minCPS: IntegerValue = object : IntegerValue("MinCPS", 5, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxCPS.get()
            if (i < newValue) set(i)

            attackDelay = TimeUtils.randomClickDelay(this.get(), maxCPS.get())
        }
    }

    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)

    // Range
    val rangeValue = FloatValue("Range", 3.7f, 1f, 8f)
    private val throughWallsRangeValue = object : FloatValue("ThroughWallsRange", 1.5f, 0f, 8f) {
            override fun onChanged(oldValue: Float, newValue: Float) {
                val i = rangeValue.get()
                if (i < newValue) set(i)
            }
        }
    private val rangeSprintReducementValue = FloatValue("RangeSprintReducement", 0f, 0f, 0.4f)

    // Modes
    private val rotations = ListValue("RotationMode", arrayOf("Vanilla", "BackTrack", "Spin", "Down", "None"), "BackTrack")

    private val spinHurtTimeValue = IntegerValue("Spin-HitHurtTime", 10, 0, 10, { rotations.equals("Spin") })

    // Spin Speed
    private val maxSpinSpeed = FloatValue("MaxSpinSpeed", 180f, 0f, 180f).displayable { rotations.equals("Spin") }

    private val minSpinSpeed = FloatValue("MinSpinSpeed", 180f, 0f, 180f).displayable { rotations.equals("Spin") }

    // Turn Speed
    private val maxTurnSpeed = FloatValue("MaxTurnSpeed", 180f, 0f, 180f).displayable { !rotations.equals("None") }

    private val minTurnSpeed = FloatValue("MinTurnSpeed", 180f, 0f, 180f).displayable { !rotations.equals("None") }

    private val roundTurnAngle = BoolValue("RoundAngle", false).displayable { !rotations.equals("None") }
    private val roundAngleDirs = IntegerValue("RoundAngle-Directions", 4, 2, 45).displayable { !rotations.equals("None") && roundTurnAngle.get() }

    private val noSendRot = BoolValue("NoSendRotation", true).displayable { rotations.equals("Spin") }
    private val noHitCheck = BoolValue("NoHitCheck", false).displayable { !rotations.equals("Spin") }

    private val rotTest = BoolValue("rotTest", false).displayable { rotations.equals("Down") }

    private val priorityValue = ListValue("Priority", arrayOf("Health", "Distance", "Direction", "LivingTime", "Armor", "HurtResistance", "HurtTime", "HealthAbsorption", "RegenAmplifier"), "Distance")
    public val rangeModeValue = ListValue("RangeMode", arrayOf("None", "Universocraft", "UniversocraftHvH", "Verus"), "None")
    private val autoconfig = BoolValue("AutoConfiguration", false).displayable { rangeModeValue.equals("Universocraft") }

    val targetModeValue = ListValue("TargetMode", arrayOf("Single", "Switch", "Multi"), "Switch")

    //reverted in old LB. idk why they removed it.
    private val switchDelayValue = IntegerValue("SwitchDelay", 1000, 1, 2000, "ms").displayable { targetModeValue.equals("Switch") }

    // Bypass
    private val swingValue = BoolValue("Swing", true)
    public val keepSprintValue = BoolValue("KeepSprint", true)

    private val autoBlockModeValue = ListValue("AutoBlock", arrayOf("None", "Fake", "Packet", "AfterTick", "AAC", "NCP", "NCP2", "OldHypixel", "TestHypixel","Vanilla"), "None")

    private val displayAutoBlockSettings = BoolValue("Open-AutoBlock-Settings", false).displayable { !autoBlockModeValue.equals("None") }
    private val interactAutoBlockValue = BoolValue("InteractAutoBlock", true).displayable { !autoBlockModeValue.equals("None") && displayAutoBlockSettings.get() }
    // smart autoblock stuff
    private val smartAutoBlockValue = BoolValue("SmartAutoBlock", false).displayable { !autoBlockModeValue.equals("None") && displayAutoBlockSettings.get() }
    private val smartABItemValue = BoolValue("SmartAutoBlock-ItemCheck", true).displayable { !autoBlockModeValue.equals("None") && smartAutoBlockValue.get() && displayAutoBlockSettings.get() }
    private val smartABFacingValue = BoolValue("SmartAutoBlock-FacingCheck", true).displayable { !autoBlockModeValue.equals("None") && smartAutoBlockValue.get() && displayAutoBlockSettings.get() }
    private val smartABRangeValue = FloatValue("SmartAB-Range", 3.5F, 3F, 8F).displayable { !autoBlockModeValue.equals("None") && smartAutoBlockValue.get() && displayAutoBlockSettings.get() }
    private val smartABTolerationValue = FloatValue("SmartAB-Toleration", 0F, 0F, 2F).displayable { !autoBlockModeValue.equals("None") && smartAutoBlockValue.get() && displayAutoBlockSettings.get() }

    private val afterTickPatchValue = BoolValue("AfterTickPatch", true).displayable { autoBlockModeValue.equals("AfterTick") && displayAutoBlockSettings.get() }
    private val blockRate = IntegerValue("BlockRate", 100, 1, 100).displayable { !autoBlockModeValue.equals("None") && displayAutoBlockSettings.get() }

    // Raycast
    private val raycastValue = BoolValue("RayCast", true)
    private val raycastIgnoredValue = BoolValue("RayCastIgnored", false)
    private val livingRaycastValue = BoolValue("LivingRayCast", true)

    // Bypass
    private val aacValue = BoolValue("AAC", false)

    private val silentRotationValue = BoolValue("SilentRotation", true).displayable { !rotations.equals("None") }
    val rotationStrafeValue = ListValue("Strafe", arrayOf("Off", "Strict", "Silent"), "Off")

    private val fovValue = FloatValue("FOV", 180f, 0f, 180f)

    // Predict
    private val predictValue = BoolValue("Predict", true)

    private val maxPredictSize = FloatValue("MaxPredictSize", 1f, 0.1f, 5f).displayable { predictValue.get() }

    private val minPredictSize = FloatValue("MinPredictSize", 1f, 0.1f, 5f).displayable { predictValue.get() }

    private val randomCenterValue = BoolValue("RandomCenter", false).displayable { !rotations.equals("None") }
    private val randomCenterNewValue = BoolValue("NewCalc", true).displayable { !rotations.equals("none") && randomCenterValue.get() }
    private val minRand = FloatValue("MinMultiply", 0.8f, 0f, 2f).displayable { !rotations.equals("none") && randomCenterValue.get() }

    private val maxRand = FloatValue("MaxMultiply", 0.8f, 0f, 2f).displayable { !rotations.equals("none") && randomCenterValue.get() }

    private val outborderValue = BoolValue("Outborder", false)

    // Bypass
    private val failRateValue = FloatValue("FailRate", 0f, 0f, 100f)
    private val fakeSwingValue = BoolValue("FakeSwing", true)
    private val noInventoryAttackValue = BoolValue("NoInvAttack", false)
    private val noInventoryDelayValue = IntegerValue("NoInvDelay", 200, 0, 500).displayable { noInventoryAttackValue.get() }
    private val limitedMultiTargetsValue = IntegerValue("LimitedMultiTargets", 0, 0, 50).displayable { targetModeValue.equals("Multi") }

    // Avoid bans
    private val noBlocking = BoolValue("NoBlocking", false)
    private val noEat = BoolValue("NoEat", true)
    private val blinkCheck = BoolValue("BlinkCheck", true)
    private val noScaffValue = BoolValue("NoScaffold", true)
    private val noFlyValue = BoolValue("NoFly", false)

    // Debug - Dev Tool
    private val debugValue = BoolValue("Debug", false)

    // Visuals
    private val circleValue = BoolValue("Circle", true)
    private val accuracyValue = IntegerValue("Accuracy", 59, 0, 59).displayable { circleValue.get() }
    private val fakeSharpValue = BoolValue("FakeSharp", true)
    private val fakeSharpSword = BoolValue("FakeSharp-SwordOnly", true).displayable { fakeSharpValue.get() }
    private val red = IntegerValue("Red", 255, 0, 255).displayable { circleValue.get() }
    private val green = IntegerValue("Green", 255, 0, 255).displayable { circleValue.get() }
    private val blue = IntegerValue("Blue", 255, 0, 255).displayable { circleValue.get() }
    private val alpha = IntegerValue("Alpha", 255, 0, 255).displayable { circleValue.get() }
    private val inRangeDiscoveredTargets = mutableListOf<EntityLivingBase>()
    private val discoveredTargets = mutableListOf<EntityLivingBase>()
    val canFakeBlock: Boolean
        get() = inRangeDiscoveredTargets.isNotEmpty()

    /**
     * MODULE
     */

    // Target
    var target: EntityLivingBase? = null
    var currentTarget: EntityLivingBase? = null
    var hitable = false
    private val prevTargetEntities = mutableListOf<Int>()

    private var markEntity: EntityLivingBase? = null
    private val markTimer = MSTimer()

    var strictStrafe = false


    // Attack delay
    private val attackTimer = MSTimer()
    private var attackDelay = 0L
    private var clicks = 0

    private var lastHitTick = 0

    // Container Delay
    private var containerOpen = -1L

    // Fake block status
    var blockingStatus = false

    var smartBlocking = false

    private val canSmartBlock: Boolean
        get() = !smartAutoBlockValue.get() || smartBlocking


    val displayBlocking: Boolean
        get() = blockingStatus || (autoBlockModeValue.equals("Fake") && canFakeBlock)

    var spinYaw = 0F


    /**
     * Enable kill aura module
     */
    override fun onEnable() {

        if (rangeModeValue.equals("Universocraft") && autoconfig.get()) {
            autoconfig.set(false)
            minCPS.set(6)
            maxCPS.set(12)
            hurtTimeValue.set(10)
            rotations.set("BackTrack")
            throughWallsRangeValue.set(0)
            priorityValue.set("Distance")
            predictValue.set(true)
            maxPredictSize.set(1f)
            minPredictSize.set(0.5f)
            autoBlockModeValue.set("Fake")
            rotationStrafeValue.set("Silent")
            targetModeValue.set("Single")
            maxTurnSpeed.set(70f)
            minTurnSpeed.set(65f)
            keepSprintValue.set(false)
            fovValue.set(110)
            LiquidBounce.hud.addNotification(Notification("KillAura", "New settings applied", NotifyType.WARNING, 1000, 500))

        }

        mc.thePlayer ?: return
        mc.theWorld ?: return

        updateTarget()
        smartBlocking = false
    }

    /**
     * Disable kill aura module
     */
    override fun onDisable() {
        target = null
        currentTarget = null
        hitable = false
        prevTargetEntities.clear()
        inRangeDiscoveredTargets.clear()
        discoveredTargets.clear()
        attackTimer.reset()
        clicks = 0

        stopBlocking()
    }

    /**
     * Motion event
     */
    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.POST) {
            target ?: return
            currentTarget ?: return

            // Update hitable
            updateHitable()

            // AutoBlocks

            if (autoBlockModeValue.equals("NCP2") && (mc.thePlayer.isBlocking || blockingStatus)) {
                PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
            }

            if (autoBlockModeValue.equals("AfterTick") && canBlock)
                startBlocking(currentTarget!!, hitable)

        }


        if (autoBlockModeValue.equals("TestHypixel") && canBlock)
        if (mc.thePlayer.swingProgressInt == 1) {
            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
        } else if (mc.thePlayer.swingProgressInt == 2) {
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()))
        }



        if (rotationStrafeValue.equals("Off"))
            update()
    }


    /**
     * Strafe event
     */
    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        val targetStrafe = LiquidBounce.moduleManager.getModule(TargetStrafe::class.java)!!
        if (rotationStrafeValue.equals("Off") && !targetStrafe.state)
            return

        update()

        if (currentTarget != null && RotationUtils.targetRotation != null) {
            when (rotationStrafeValue.get().lowercase()) {
                "strict" -> {
                    val (yaw) = RotationUtils.targetRotation ?: return
                    var strafe = event.strafe
                    var forward = event.forward
                    val friction = event.friction

                    var f = strafe * strafe + forward * forward

                    if (f >= 1.0E-4F) {
                        f = MathHelper.sqrt_float(f)

                        if (f < 1.0F)
                            f = 1.0F

                        f = friction / f
                        strafe *= f
                        forward *= f

                        val yawSin = MathHelper.sin((yaw * Math.PI / 180F).toFloat())
                        val yawCos = MathHelper.cos((yaw * Math.PI / 180F).toFloat())

                        mc.thePlayer.motionX += strafe * yawCos - forward * yawSin
                        mc.thePlayer.motionZ += forward * yawCos + strafe * yawSin
                    }
                    event.cancelEvent()
                }

                "silent" -> {
                    update()

                    RotationUtils.targetRotation.applyStrafeToPlayer(event)
                    event.cancelEvent()
                }
            }
        }
    }

    fun update() {
        if (cancelRun || (noInventoryAttackValue.get() && (mc.currentScreen is GuiContainer ||
                        System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())))
            return

        // Update target
        updateTarget()

        if (target == null) {
            stopBlocking()
            return
        }
        if (discoveredTargets.isEmpty()) {
            stopBlocking()
            return
        }

        // Target
        currentTarget = target

        if (!targetModeValue.equals("Switch") && isEnemy(currentTarget))
            target = currentTarget
    }

    /**
     * Update event
     */
    @EventTarget
    fun onUpdate(event: UpdateEvent) {

        smartBlocking = false
        if (smartAutoBlockValue.get() && target != null) {
            val smTarget = target!!
            if (!smartABItemValue.get() || (smTarget.heldItem != null && smTarget.heldItem.getItem() != null && (smTarget.heldItem.getItem() is ItemSword || smTarget.heldItem.item is ItemAxe))) {
                if (mc.thePlayer.getDistanceToEntityBox(smTarget) < smartABRangeValue.get()) {
                    if (smartABFacingValue.get()) {
                        if (smTarget.rayTrace(smartABRangeValue.get().toDouble(), 1F).typeOfHit == MovingObjectPosition.MovingObjectType.MISS) {
                            val eyesVec = smTarget.getPositionEyes(1F)
                            val lookVec = smTarget.getLook(1F)
                            val pointingVec = eyesVec.addVector(lookVec.xCoord * smartABRangeValue.get(), lookVec.yCoord * smartABRangeValue.get(), lookVec.zCoord * smartABRangeValue.get())
                            val border = mc.thePlayer.getCollisionBorderSize() + smartABTolerationValue.get()
                            val bb = mc.thePlayer.entityBoundingBox.expand(border.toDouble(), border.toDouble(), border.toDouble())
                            smartBlocking = bb.calculateIntercept(eyesVec, pointingVec) != null || bb.intersectsWith(smTarget.entityBoundingBox)
                        }
                    } else
                        smartBlocking = true
                }
            }
        }

        updateKA()


    }

    private fun updateKA() {
        if (cancelRun) {
            target = null
            currentTarget = null
            hitable = false
            discoveredTargets.clear()
            inRangeDiscoveredTargets.clear()
            stopBlocking()
            return
        }

        if (noInventoryAttackValue.get() && (mc.currentScreen is GuiContainer ||
                        System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())) {
            target = null
            currentTarget = null
            hitable = false
            if (mc.currentScreen is GuiContainer) containerOpen = System.currentTimeMillis()
            return
        }

        if (target != null && currentTarget != null) {
            while (clicks > 0) {
                runAttack()
                clicks--
            }
        }
    }

    /**
     * Render event
     */
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (circleValue.get()) {
            GL11.glPushMatrix()
            GL11.glTranslated(
                    mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosX,
                    mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosY,
                    mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks - mc.getRenderManager().renderPosZ
            )
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

            GL11.glLineWidth(1F)
            GL11.glColor4f(red.get().toFloat() / 255.0F, green.get().toFloat() / 255.0F, blue.get().toFloat() / 255.0F, alpha.get().toFloat() / 255.0F)
            GL11.glRotatef(90F, 1F, 0F, 0F)
            GL11.glBegin(GL11.GL_LINE_STRIP)

            for (i in 0..360 step 60 - accuracyValue.get()) { // You can change circle accuracy  (60 - accuracy)
                GL11.glVertex2f(Math.cos(i * Math.PI / 180.0).toFloat() * rangeValue.get(), (Math.sin(i * Math.PI / 180.0).toFloat() * rangeValue.get()))
            }

            GL11.glEnd()

            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)

            GL11.glPopMatrix()
        }

        if (cancelRun) {
            target = null
            currentTarget = null
            hitable = false
            discoveredTargets.clear()
            inRangeDiscoveredTargets.clear()
            stopBlocking()
            return
        }

        if (noInventoryAttackValue.get() && (mc.currentScreen is GuiContainer ||
                        System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())) {
            target = null
            currentTarget = null
            hitable = false
            if (mc.currentScreen is GuiContainer) containerOpen = System.currentTimeMillis()
            return
        }

        target ?: return

        if (currentTarget != null && attackTimer.hasTimePassed(attackDelay) &&
                currentTarget!!.hurtTime <= hurtTimeValue.get()) {
            clicks++
            attackTimer.reset()
            attackDelay = TimeUtils.randomClickDelay(minCPS.get(), maxCPS.get())
        }
    }

    /**
     * Handle entity move
     */
    @EventTarget
    fun onEntityMove(event: EntityMovementEvent) {
        val movedEntity = event.movedEntity

        if (target == null || movedEntity != currentTarget)
            return

        updateHitable()
    }

    /**
     * Attack enemy
     */
    private fun runAttack() {
        target ?: return
        currentTarget ?: return

        // Settings
        val failRate = failRateValue.get()
        val swing = swingValue.get()
        val multi = targetModeValue.equals("Multi")
        val openInventory = aacValue.get() && mc.currentScreen is GuiInventory
        val failHit = failRate > 0 && Random().nextInt(100) <= failRate

        // Close inventory when open
        if (openInventory)
            mc.netHandler.addToSendQueue(C0DPacketCloseWindow())

        // Verus and Universocraft Bypass
        when (rangeModeValue.get().lowercase()) {
            "none" -> {

            }

            "universocraft" -> {
                if (mc.thePlayer.ticksExisted % 13 < 10) {
                    rangeValue.set(2.85)
                    failRateValue.set(3)

                }
                if (mc.thePlayer.ticksExisted % 55 < 10) {
                    rangeValue.set(2.90)
                    failRateValue.set(5)
                }
                if (mc.thePlayer.ticksExisted % 70 < 10) {
                    rangeValue.set(2.95)
                    mc.thePlayer.ticksExisted = 0
                    failRateValue.set(7)
                }
            }
            "universocrafthvh" -> {
                if (mc.thePlayer.ticksExisted % 13 < 10) {
                    maxCPS.set(20)
                    minCPS.set(15)
                    rangeValue.set(2.95)
                    failRateValue.set(1)

                }
                if (mc.thePlayer.ticksExisted % 55 < 10) {
                    rangeValue.set(3.0)
                    failRateValue.set(2)
                }
                if (mc.thePlayer.ticksExisted % 70 < 10) {
                    rangeValue.set(3.05)
                    mc.thePlayer.ticksExisted = 0
                    failRateValue.set(3)
                }

            }
            "verus" -> {
                if (mc.thePlayer.ticksExisted % 13 < 10) {
                    rangeValue.set(3.2)
                }
                if (mc.thePlayer.ticksExisted % 40 < 10) {
                    rangeValue.set(3.35)
                }
                if (mc.thePlayer.ticksExisted % 60 < 10) {
                    rangeValue.set(3.6)
                }

            }
        }

        // Check is not hitable or check failrate
        if (!hitable || failHit) {
            if (swing && (fakeSwingValue.get() || failHit))
                mc.thePlayer.swingItem()
        } else {
            // Attack
            if (!multi) {
                attackEntity(currentTarget!!)
            } else {
                var targets = 0

                for (entity in mc.theWorld.loadedEntityList) {
                    val distance = mc.thePlayer.getDistanceToEntityBox(entity)

                    if (entity is EntityLivingBase && isEnemy(entity) && distance <= getRange(entity)) {
                        attackEntity(entity)

                        targets += 1

                        if (limitedMultiTargetsValue.get() != 0 && limitedMultiTargetsValue.get() <= targets)
                            break
                    }
                }
            }

            prevTargetEntities.add(if (aacValue.get()) target!!.entityId else currentTarget!!.entityId)

            if (target == currentTarget)
                target = null
        }

        if (targetModeValue.equals("Switch") && attackTimer.hasTimePassed((switchDelayValue.get()).toLong())) {
            if (switchDelayValue.get() != 0) {
                prevTargetEntities.add(if (aacValue.get()) target!!.entityId else currentTarget!!.entityId)
                attackTimer.reset()
            }
        }

        // Open inventory
        if (openInventory)
            mc.netHandler.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))
    }

    /**
     * Update current target
     */
    private fun updateTarget() {
        // Reset fixed target to null
        var searchTarget = null

        // Settings
        val hurtTime = hurtTimeValue.get()
        val fov = fovValue.get()
        val switchMode = targetModeValue.equals("Switch")

        // Find possible targets
        discoveredTargets.clear()
        val targets = mutableListOf<EntityLivingBase>()
        val lookingTargets = mutableListOf<EntityLivingBase>()

        for (entity in mc.theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase || !isEnemy(entity) || (switchMode && prevTargetEntities.contains(entity.entityId))/* || (!focusEntityName.isEmpty() && !focusEntityName.contains(entity.name.lowercase()))*/)
                continue

            val distance = mc.thePlayer.getDistanceToEntityBox(entity)
            val entityFov = RotationUtils.getRotationDifference(entity)

            if (distance <= maxRange && (fov == 180F || entityFov <= fov) && entity.hurtTime <= hurtTime)
                targets.add(entity)
            discoveredTargets.add(entity)
        }

        // Sort targets by priority // targets.sortBy
        when (priorityValue.get().lowercase()) {
            "distance" -> discoveredTargets.sortBy { mc.thePlayer.getDistanceToEntityBox(it) } // Sort by distance
            "health" -> discoveredTargets.sortBy { it.health } // Sort by health
            "direction" -> discoveredTargets.sortBy { RotationUtils.getRotationDifference(it) } // Sort by FOV
            "livingtime" -> discoveredTargets.sortBy { -it.ticksExisted } // Sort by existence
            "hurtresistance" -> discoveredTargets.sortBy { it.hurtResistantTime } // Sort by armor hurt time
            "hurttime" -> discoveredTargets.sortBy { it.hurtTime } // Sort by hurt time
            "healthabsorption" -> discoveredTargets.sortBy { it.health + it.absorptionAmount } // Sort by full health with absorption effect
            "regenamplifier" -> discoveredTargets.sortBy { if (it.isPotionActive(Potion.regeneration)) it.getActivePotionEffect(Potion.regeneration).amplifier else -1 }
        }
        inRangeDiscoveredTargets.clear()
        inRangeDiscoveredTargets.addAll(discoveredTargets.filter { mc.thePlayer.getDistanceToEntityBox(it) < getRange(it) })

        // Cleanup last targets when no targets found and try again
        if (inRangeDiscoveredTargets.isEmpty() && prevTargetEntities.isNotEmpty()) {
            prevTargetEntities.clear()
            updateTarget()
            return
        }

        var found = false

        // Find best target
        for (entity in targets) {
            // Update rotations to current target
            if (!updateRotations(entity)) // when failed then try another target
                continue

            // Set target to current entity
            target = entity
            found = true
            break
        }

        if (found) {
            if (rotations.equals("Spin")) {
                spinYaw += RandomUtils.nextFloat(minSpinSpeed.get(), maxSpinSpeed.get())
                spinYaw = MathHelper.wrapAngleTo180_float(spinYaw)
                val rot = Rotation(spinYaw, 90F)
                RotationUtils.setTargetRotation(rot, 0)
            }
            return
        }

        if (searchTarget != null) {
            if (target != searchTarget) target = searchTarget
            return
        } else {
            target = null
        }

        // Cleanup last targets when no target found and try again
        if (prevTargetEntities.isNotEmpty()) {
            prevTargetEntities.clear()
            updateTarget()
        }
    }

    /**
     * Check if [entity] is selected as enemy with current target options and other modules
     */
    public fun isEnemy(entity: Entity?): Boolean {
        if (entity is EntityLivingBase && (LiquidBounce.moduleManager[Target::class.java]!!.deadValue.get() || isAlive(entity)) && entity != mc.thePlayer) {
            if (!LiquidBounce.moduleManager[Target::class.java]!!.invisibleValue.get() && entity.isInvisible())
                return false

            if (LiquidBounce.moduleManager[Target::class.java]!!.playerValue.get() && entity is EntityPlayer) {
                if (entity.isSpectator || AntiBot.isBot(entity))
                    return false

                if (EntityUtils.isFriend(entity))
                    return false

                val teams = LiquidBounce.moduleManager[Teams::class.java] as Teams

                return !teams.state || !teams.isInYourTeam(entity)
            }

            return LiquidBounce.moduleManager[Target::class.java]!!.mobValue.get() && EntityUtils.isMob(entity) || LiquidBounce.moduleManager[Target::class.java]!!.animalValue.get() &&
                    EntityUtils.isAnimal(entity)
        }

        return false
    }

    /**
     * Attack [entity]
     */
    private fun attackEntity(entity: EntityLivingBase) {
        // Stop blocking
        if (mc.thePlayer.isBlocking || blockingStatus)
            stopBlocking()

        // Call attack event
        LiquidBounce.eventManager.callEvent(AttackEvent(entity))

        markEntity = entity

        // Vanilla
        if (autoBlockModeValue.equals("Vanilla") && (mc.thePlayer.isBlocking || blockingStatus)) {
            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
        }


        // AAC
        if (autoBlockModeValue.equals("AAC") && (mc.thePlayer.isBlocking || blockingStatus)) {
            if (mc.thePlayer.ticksExisted % 2 == 0) {
                mc.playerController.interactWithEntitySendPacket(mc.thePlayer, target)
                PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement (mc.thePlayer.getHeldItem()))
            }
        }

        // Get rotation and send packet if possible
        if (rotations.equals("Spin") && !noSendRot.get()) {
            val targetedRotation = getTargetRotation(entity) ?: return
            mc.netHandler.addToSendQueue(C03PacketPlayer.C05PacketPlayerLook(targetedRotation.yaw, targetedRotation.pitch, mc.thePlayer.onGround))

            if (debugValue.get())
                ClientUtils.displayChatMessage("[KillAura] Silent rotation change.")
        }

        if (autoBlockModeValue.equals("Vanilla") && (mc.thePlayer.isBlocking || blockingStatus)) {
            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
        }

        mc.thePlayer.swingItem()


        if (keepSprintValue.get()) {
            mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))
            // Critical Effect
            if (mc.thePlayer.fallDistance > 0F && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder &&
                    !mc.thePlayer.isInWater && !mc.thePlayer.isPotionActive(Potion.blindness) && !mc.thePlayer.isRiding)
                mc.thePlayer.onCriticalHit(entity)

            // Enchant Effect
            if (EnchantmentHelper.getModifierForCreature(mc.thePlayer.heldItem, entity.creatureAttribute) > 0F)
                mc.thePlayer.onEnchantmentCritical(entity)
        } else {
            if (mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR)
                mc.thePlayer.attackTargetEntityWithCurrentItem(entity)
                mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))
        }

        // Extra critical effects
        val criticals = LiquidBounce.moduleManager[Criticals::class.java] as Criticals

        for (i in 0..2) {
            // Critical Effect
            if (mc.thePlayer.fallDistance > 0F && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder && !mc.thePlayer.isInWater && !mc.thePlayer.isPotionActive(Potion.blindness) && mc.thePlayer.ridingEntity == null || criticals.state && criticals.msTimer.hasTimePassed(criticals.delayValue.get().toLong()) && !mc.thePlayer.isInWater && !mc.thePlayer.isInLava && !mc.thePlayer.isInWeb)
                mc.thePlayer.onCriticalHit(target)

            // Enchant Effect
            if (EnchantmentHelper.getModifierForCreature(mc.thePlayer.heldItem, target!!.creatureAttribute) > 0.0f || (fakeSharpValue.get() && (!fakeSharpSword.get() || canBlock)))
                mc.thePlayer.onEnchantmentCritical(target)
        }
        // Start blocking after attack
        if ((!afterTickPatchValue.get() || !autoBlockModeValue.equals("AfterTick")) && (mc.thePlayer.isBlocking || canBlock))
            startBlocking(entity, interactAutoBlockValue.get())
    }

    /**
     * Update killaura rotations to enemy
     */
    private fun updateRotations(entity: Entity): Boolean {
        if (rotations.equals("None")) return true

        var defRotation = getTargetRotation(entity) ?: return false

        if (defRotation != RotationUtils.serverRotation && roundTurnAngle.get())
            defRotation.yaw = RotationUtils.roundRotation(defRotation.yaw, roundAngleDirs.get())

        if (silentRotationValue.get()) {
            RotationUtils.setTargetRotation(defRotation, if (aacValue.get() && !rotations.equals("Spin")) 15 else 0)
        } else {
            defRotation.toPlayer(mc.thePlayer!!)
        }

        return true
    }

    private fun getTargetRotation(entity: Entity): Rotation? {
        var boundingBox = entity.entityBoundingBox
        if (rotations.equals("Vanilla")) {
            if (maxTurnSpeed.get() <= 0F)
                return RotationUtils.serverRotation

            if (predictValue.get())
                boundingBox = boundingBox.offset(
                        (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                        (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                        (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
                )

            val (_, rotation) = RotationUtils.searchCenter(
                    boundingBox,
                    outborderValue.get() && !attackTimer.hasTimePassed(attackDelay / 2),
                    randomCenterValue.get(),
                    predictValue.get(),
                    mc.thePlayer.getDistanceToEntityBox(entity) <= throughWallsRangeValue.get(),
                    maxRange,
                    RandomUtils.nextFloat(minRand.get(), maxRand.get()),
                    randomCenterNewValue.get()
            ) ?: return null

            val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation, rotation,
                    (Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat())

            return limitedRotation
        }
        if (rotations.equals("Down")) {
            if (maxTurnSpeed.get() <= 0F)
                return RotationUtils.serverRotation

            if (predictValue.get())
                boundingBox = boundingBox.offset(
                        (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                        (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                        (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
                )

            if (rotTest.get())
                boundingBox = boundingBox.offset(
                        (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                        90.toDouble(),
                        (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
                )

            val (_, rotation) = RotationUtils.downRot(
                    boundingBox,
                    outborderValue.get() && !attackTimer.hasTimePassed(attackDelay / 2),
                    randomCenterValue.get(),
                    predictValue.get(),
                    mc.thePlayer.getDistanceToEntityBox(entity) <= throughWallsRangeValue.get(),
                    maxRange,
                    RandomUtils.nextFloat(minRand.get(), maxRand.get()),
                    randomCenterNewValue.get()
            ) ?: return null

            val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation, rotation,
                    (Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat())

            return limitedRotation
        }
        if (rotations.equals("Spin")) {
            if (maxTurnSpeed.get() <= 0F)
                return RotationUtils.serverRotation

            if (predictValue.get())
                boundingBox = boundingBox.offset(
                        (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                        (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                        (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
                )

            val (_, rotation) = RotationUtils.searchCenter(
                    boundingBox,
                    false,
                    false,
                    false,
                    mc.thePlayer.getDistanceToEntityBox(entity) <= throughWallsRangeValue.get()
            ) ?: return null

            return rotation
        }
        if (rotations.equals("BackTrack")) {
            if (predictValue.get())
                boundingBox = boundingBox.offset(
                        (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                        (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                        (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
                )

            val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation,
                    RotationUtils.OtherRotation(boundingBox, RotationUtils.getCenter(entity.entityBoundingBox), predictValue.get(),
                            mc.thePlayer.getDistanceToEntityBox(entity) <= throughWallsRangeValue.get(), maxRange), (Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat())

            return limitedRotation
        }
        return RotationUtils.serverRotation
    }

    /**
     * Check if enemy is hitable with current rotations
     */
    private fun updateHitable() {
        if (rotations.equals("None")) {
            hitable = true
            return
        }

        val disabler = LiquidBounce.moduleManager.getModule(Disabler::class.java)!! as Disabler

        // Modify hit check for some situations
        if (rotations.equals("Spin")) {
            hitable = target!!.hurtTime <= spinHurtTimeValue.get()
            return
        }

        // Completely disable rotation check if turn speed equals to 0 or NoHitCheck is enabled
        if (maxTurnSpeed.get() <= 0F || noHitCheck.get()) {
            hitable = true
            return
        }

        val reach = min(maxRange.toDouble(), mc.thePlayer.getDistanceToEntityBox(target!!)) + 1

        if (raycastValue.get()) {
            val raycastedEntity = RaycastUtils.raycastEntity(reach) {
                (!livingRaycastValue.get() || it is EntityLivingBase && it !is EntityArmorStand) &&
                        (isEnemy(it) || raycastIgnoredValue.get() || aacValue.get() && mc.theWorld.getEntitiesWithinAABBExcludingEntity(it, it.entityBoundingBox).isNotEmpty())
            }

            if (raycastValue.get() && raycastedEntity is EntityLivingBase
                    && (!EntityUtils.isFriend(raycastedEntity)))
                currentTarget = raycastedEntity

            hitable = if (maxTurnSpeed.get() > 0F) currentTarget == raycastedEntity else true
        } else
            hitable = RotationUtils.isFaced(currentTarget, reach)
    }

    /**
     * Start blocking
     */
    private fun startBlocking(interactEntity: Entity, interact: Boolean) {
        if (!canSmartBlock || autoBlockModeValue.equals("Fake") || autoBlockModeValue.equals("None") || !(blockRate.get() > 0 && Random().nextInt(100) <= blockRate.get()))
            return

        if (blockingStatus) {
            return
        }

        if (autoBlockModeValue.equals("NCP")) {
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, null, 0.0f, 0.0f, 0.0f))
            blockingStatus = true
            return
        }

        if (autoBlockModeValue.equals("OldHypixel")) {
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, mc.thePlayer.inventory.getCurrentItem(), 0.0f, 0.0f, 0.0f))
            blockingStatus = true
            return
        }

        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
        blockingStatus = true
    }

    /**
     * Stop blocking
     */
    private fun stopBlocking() {
        if (blockingStatus) {
            if (autoBlockModeValue.equals("OldHypixel"))
                mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos(1.0, 1.0, 1.0), EnumFacing.DOWN))
            else
                mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))

            blockingStatus = false
        }
    }

    /**
     * Check if run should be cancelled
     */

    private val cancelRun: Boolean
        get() = mc.thePlayer.isSpectator || !isAlive(mc.thePlayer)
                || (blinkCheck.get() && LiquidBounce.moduleManager[Blink::class.java]!!.state)
                || LiquidBounce.moduleManager[FreeCam::class.java]!!.state
                || (noScaffValue.get() && LiquidBounce.moduleManager[Scaffold::class.java]!!.state)
                || (noFlyValue.get() && LiquidBounce.moduleManager[Fly::class.java]!!.state)
                || (noEat.get() && mc.thePlayer.isUsingItem && (mc.thePlayer.heldItem?.item is ItemFood || mc.thePlayer.heldItem?.item is ItemBucketMilk))
                || (noBlocking.get() && mc.thePlayer.isUsingItem && mc.thePlayer.heldItem?.item is ItemBlock)

    /**
     * Check if [entity] is alive
     */
    private fun isAlive(entity: EntityLivingBase) = entity.isEntityAlive && entity.health > 0 ||
            aacValue.get() && entity.hurtTime > 5


    /**
     * Check if player is able to block
     */
    private val canBlock: Boolean
        get() = mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemSword

    /**
     * Range
     */
    private val maxRange: Float
        get() = max(rangeValue.get(), throughWallsRangeValue.get())

    private fun getRange(entity: Entity) =
            (if (mc.thePlayer.getDistanceToEntityBox(entity) >= throughWallsRangeValue.get()) rangeValue.get() else throughWallsRangeValue.get()) - if (mc.thePlayer.isSprinting) rangeSprintReducementValue.get() else 0F

    /**
     * HUD Tag
     */
    override val tag: String?
        get() = targetModeValue.get()

}