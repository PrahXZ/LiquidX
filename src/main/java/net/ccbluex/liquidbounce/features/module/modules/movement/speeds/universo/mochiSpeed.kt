package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.universo

import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.potion.Potion
import java.lang.Math.*

class mochiSpeed : SpeedMode("UniversoMochi") {

    var ticks = 0

    override fun onEnable() {
        ticks = 0
    }
    override fun onMotion(event: MotionEvent) {

    }

    override fun onMove(event: MoveEvent) {
        if (mc.thePlayer.onGround) {
            eventjump(event)
            movevent(event, msqrt(event) *  (if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) 1.1995f else 1.0f))
            ticks = 0
        } else {
            if (event.y == 0.08307781780646721) {
                setY(event, -0.007)
            }
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                if (event.y > 0) {
                    setX(event, event.x * 1.0085)
                    setZ(event, event.z * 1.0085)
                } else if (ticks == 7) {
                    setX(event, event.x * 1.0254)
                    setZ(event, event.z * 1.0254)
                }
            }
            ticks++;
        }
    }

    fun setY(event: MoveEvent, y: Double) {
        event.y = y
        mc.thePlayer.motionY = y
    }
    fun setX(event: MoveEvent, x: Double) {
        event.x = x
        mc.thePlayer.motionX = x
    }
    fun setZ(event: MoveEvent, z: Double) {
        event.z = z
        mc.thePlayer.motionZ = z
    }

    fun msqrt(event: MoveEvent): Double {
        return sqrt(event.x * event.x + event.z * event.z)
    }
    fun movevent(event:MoveEvent, speed: Double) {
        if (!MovementUtils.isMoving()) return
        val angle = MovementUtils.direction
        setX(event, -sin(angle)*speed)
        setZ(event, cos(angle)*speed)
    }

    fun eventjump(event: MoveEvent) {
        setY(event,0.42f.toDouble())
        setX(event, event.x-sin(MovementUtils.getDirectioon()*0.017453292f)*0.2)
        setZ(event, event.z+cos(MovementUtils.getDirectioon()*0.017453292f)*0.2)
    }
}