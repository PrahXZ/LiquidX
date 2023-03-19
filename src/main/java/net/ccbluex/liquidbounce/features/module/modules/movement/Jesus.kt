package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.jesus.JesusMode
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.minecraft.block.BlockLiquid
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos

@ModuleInfo(name = "Jesus", category = ModuleCategory.MOVEMENT)
class Jesus : Module() {
    private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.jesus", JesusMode::class.java)
        .map { it.newInstance() as JesusMode }
        .sortedBy { it.modeName }

    private val mode: JesusMode
        get() = modes.find { modeValue.equals(it.modeName) } ?: throw NullPointerException() // this should not happen

    val modeValue: ListValue = object : ListValue("Mode", modes.map { it.modeName }.toTypedArray(), "Vanilla") {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }
    private val noJumpValue = BoolValue("NoJump", false)

    fun isLiquidBlock(bb: AxisAlignedBB = mc.thePlayer.entityBoundingBox): Boolean {
        return BlockUtils.collideBlock(bb) { it is BlockLiquid }
    }
    override fun onEnable() {
        mode.onEnable()
    }

    override fun onDisable() {
        mode.onDisable()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mode.onUpdate(event)

        if (mc.thePlayer == null || mc.thePlayer.isSneaking) {
            return
        }

        val blockPos = mc.thePlayer.position.down()
        mode.onJesus(event, blockPos)
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        mode.onMotion(event)
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        mode.onPacket(event)
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        mode.onMove(event)
    }

    @EventTarget
    fun onBlockBB(event: BlockBBEvent) {
        mode.onBlockBB(event)
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        mode.onJump(event)

        if (mc.thePlayer == null) {
            return
        }

        val block = BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.01, mc.thePlayer.posZ))
        if (noJumpValue.get() && block is BlockLiquid) {
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onStep(event: StepEvent) {
        mode.onStep(event)
    }

    override val tag: String
        get() = modeValue.get()

    override val values = super.values.toMutableList().also {
        modes.map {
            mode -> mode.values.forEach { value ->
            val displayableFunction = value.displayableFunction
            it.add(value.displayable { displayableFunction.invoke() && modeValue.equals(mode.modeName) })
            }
        }
    }
}