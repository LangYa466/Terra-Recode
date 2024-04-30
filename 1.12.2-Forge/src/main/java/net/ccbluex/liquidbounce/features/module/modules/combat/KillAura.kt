/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.api.enums.EnumFacingType
import net.ccbluex.liquidbounce.api.enums.WEnumHand
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntity
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntityLivingBase
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketPlayerDigging
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketUseEntity
import net.ccbluex.liquidbounce.api.minecraft.potion.PotionType
import net.ccbluex.liquidbounce.api.minecraft.util.WBlockPos
import net.ccbluex.liquidbounce.api.minecraft.util.WVec3
import net.ccbluex.liquidbounce.api.minecraft.world.IWorldSettings
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.features.module.modules.misc.Teams
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.features.module.modules.render.FreeCam
import net.ccbluex.liquidbounce.features.module.modules.world.ScaHelp
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold
import net.ccbluex.liquidbounce.injection.backend.Backend
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import kotlin.math.*

@ModuleInfo(name = "KillAura", description = "Automatically attacks targets around you.", category = ModuleCategory.COMBAT, keyBind = Keyboard.KEY_R)
class KillAura : Module() {

    /**
     * OPTIONS
     */

    // CPS - Attack speed
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
    private val smartHurtTime = BoolValue("SmartHurtTime", false)
    private val cooldownValue = FloatValue("Cooldown", 1f, 0f, 1f)

    // Range
    val rangeValue = FloatValue("Range", 3.5f, 1f, 6f)
    private val throughWallsRangeValue = FloatValue("ThroughWallsRange", 3f, 0f, 6f)
    private val rangeSprintReducementValue = FloatValue("RangeSprintReducement", 0f, 0f, 0.4f)

    // Modes
    val rotations = ListValue("RotationMode", arrayOf("Vanilla", "Other", "BackTrack", "Terra"), "Terra")
    private val priorityValue = ListValue("Priority", arrayOf("Health", "Distance", "Direction", "LivingTime"), "Distance")
    private val targetModeValue = ListValue("TargetMode", arrayOf("Single", "Switch", "Multi"), "Switch")
    private val switchDelayValue = IntegerValue("SwitchDelay",300 ,1, 2000).displayable { targetModeValue.get().equals("Switch", ignoreCase = true) }
    private val limitedMultiTargetsValue = IntegerValue("LimitedMultiTargets", 0, 0, 50).displayable { targetModeValue.get().equals("Multi", ignoreCase = true) }

    // Bypass
    private val swingValue = BoolValue("Swing", true)
    private val keepSprintValue = BoolValue("KeepSprint", true)

    // AutoBlock
    val blockModeValue = ListValue("BlockMode", arrayOf("None", "Packet", "Fake"), "None")
    private val afterAttackValue = BoolValue("AutoBlock-AfterAttack", false).displayable { !blockModeValue.get().equals("None",true) }
    private val delayedBlockValue = BoolValue("DelayedBlock", false).displayable { !blockModeValue.get().equals("None",true) }
    private val blockRate = IntegerValue("BlockRate", 100, 1, 100).displayable { !blockModeValue.get().equals("None",true) }

    // Raycast
    private val raycastValue = BoolValue("RayCast", true)
    private val raycastIgnoredValue = BoolValue("RayCastIgnored", false)
    private val livingRaycastValue = BoolValue("LivingRayCast", false)

    // Bypass
    private val aacValue = BoolValue("AAC", true)
    private val noScaffoldValue = BoolValue("NoScaffold", true)
    private val silentFov = BoolValue("SilentFov", false)
    private val silentFovValue = FloatValue("SilentFovValue", 20f, 0f, 40f).displayable { silentFov.get() }
    private val jumpFix = BoolValue("JumpFix", false)

    // Turn Speed
    private val maxTurnSpeed: FloatValue = object : FloatValue("MaxTurnSpeed", 180f, 0f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minTurnSpeed.get()
            if (v > newValue) set(v)
        }
    }

    private val minTurnSpeed: FloatValue = object : FloatValue("MinTurnSpeed", 180f, 0f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxTurnSpeed.get()
            if (v < newValue) set(v)
        }
    }

    private val silentRotationValue = BoolValue("SilentRotation", true)
    private val rotationStrafeValue = ListValue("Strafe", arrayOf("Off", "Strict", "Silent"), "Off")
    private val moveFixValue = BoolValue("MoveFix", true)
    private val randomCenterValue = BoolValue("RandomCenter", true)
    private val outborderValue = BoolValue("Outborder", false)
    val fovValue = FloatValue("FOV", 180f, 0f, 180f)
    private val hitableValue = BoolValue("AlwaysHitable", true)

    // Predict
    private val predictValue = BoolValue("Predict", true)

    private val maxPredictSize: FloatValue = object : FloatValue("MaxPredictSize", 1f, 0.1f, 5f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minPredictSize.get()
            if (v > newValue) set(v)
        }
    }.displayable { predictValue.get() } as FloatValue

    private val minPredictSize: FloatValue = object : FloatValue("MinPredictSize", 1f, 0.1f, 5f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxPredictSize.get()
            if (v < newValue) set(v)
        }
    }.displayable { predictValue.get() } as FloatValue

    // Bypass
    private val failRateValue = FloatValue("FailRate", 0f, 0f, 100f)
    private val fakeSwingValue = BoolValue("FakeSwing", false)
    private val noInventoryAttackValue = BoolValue("NoInvAttack", false)
    private val noInventoryDelayValue = IntegerValue("NoInvDelay", 200, 0, 500).displayable { noInventoryAttackValue.get() }

    // Visuals
    private val markValue = ListValue("Mark", arrayOf("Liquid", "Block", "Jello", "Forever", "Lies", "None"),"Liquid")
    private val fakeSharpValue = BoolValue("FakeSharp", false)

    private val circleValue = BoolValue("Circle", true)
    private val circleRedValue = IntegerValue("CircleRed", 255, 0, 255).displayable { circleValue.get() && circleValue.displayable }
    private val circleGreenValue = IntegerValue("CircleGreen", 255, 0, 255).displayable { circleValue.get() && circleValue.displayable }
    private val circleBlueValue = IntegerValue("CircleBlue", 255, 0, 255).displayable { circleValue.get() && circleValue.displayable }
    private val circleAlphaValue = IntegerValue("CircleAlpha", 255, 0, 255).displayable { circleValue.get() && circleValue.displayable }
    private val circleThicknessValue = FloatValue("CircleThickness", 2F, 1F, 5F).displayable { circleValue.get() && circleValue.displayable }
    private val circleAccuracy = IntegerValue("CircleAccuracy", 15, 0, 60).displayable { circleValue.get() && circleValue.displayable }

    /**
     * MODULE
     */

    // Target
    var target: IEntityLivingBase? = null
    var currentTarget: IEntityLivingBase? = null
    var hitable = false
    private val prevTargetEntities = mutableListOf<Int>()
    private val inRangeDiscoveredTargets = mutableListOf<EntityLivingBase>()
    private val canFakeBlock: Boolean
        get() = inRangeDiscoveredTargets.isNotEmpty()

    // Attack delay
    private val attackTimer = MSTimer()
    private var attackDelay = 0L
    private var clicks = 0

    // Container Delay
    private var containerOpen = -1L

    // Fake block status
    var blockingStatus = false

    private var jump = 0

    //Switch
    private val switchTimer = MSTimer()

    @EventTarget
    fun onJump(event: JumpEvent) {
        jump = 0
    }

    /**
     * Enable kill aura module
     */
    override fun onEnable() {
        mc.thePlayer ?: return
        mc.theWorld ?: return
        jump = 0
        updateTarget()
    }

    /**
     * Disable kill aura module
     */
    override fun onDisable() {
        target = null
        currentTarget = null
        hitable = false
        prevTargetEntities.clear()
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

            // AutoBlock
            if (!blockModeValue.get().equals("None", true) && delayedBlockValue.get() && canBlock)
                startBlocking(currentTarget!!)

            return
        }

        if (rotationStrafeValue.get().equals("Off", true))
            update()
    }

    /**
     * Strafe event
     */
    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (rotationStrafeValue.get().equals("Off", true))
            return

        update()

        if (currentTarget != null && RotationUtils.targetRotation != null) {
            when (rotationStrafeValue.get().toLowerCase()) {
                "strict" -> {
                    val (yaw) = RotationUtils.targetRotation ?: return
                    var strafe = event.strafe
                    var forward = event.forward
                    val friction = event.friction

                    var f = strafe * strafe + forward * forward

                    if (f >= 1.0E-4F) {
                        f = sqrt(f)

                        if (f < 1.0F)
                            f = 1.0F

                        f = friction / f
                        strafe *= f
                        forward *= f

                        val yawSin = sin((yaw * Math.PI / 180F).toFloat())
                        val yawCos = cos((yaw * Math.PI / 180F).toFloat())

                        val player = mc.thePlayer!!

                        player.motionX += strafe * yawCos - forward * yawSin
                        player.motionZ += forward * yawCos + strafe * yawSin
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
        if (cancelRun || (noInventoryAttackValue.get() && (classProvider.isGuiContainer(mc.currentScreen) ||
                        System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())))
            return

        // Update target
        updateTarget()

        if (target == null) {
            stopBlocking()
            return
        }

        // Target
        currentTarget = target

        if (!targetModeValue.get().equals("Switch", ignoreCase = true) && isEnemy(currentTarget))
            target = currentTarget
    }

    /**
     * Fix movement event
     */
    @EventTarget
    fun onMoveInput(event: MoveInputEvent) {
        if (moveFixValue.get()) {
            MoveUtil.fixMovement(event, RotationUtils.targetRotation.yaw)
        }
    }

    @EventTarget
    fun onPreUpdate(event: PreUpdateEvent) {
        update()
    }

    /**
     * Update event
     */
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        jump++

        //SilentFov
        if (silentFov.get()) {
            if (RotationUtils.getRotationDifference(target) > silentFovValue.get()) {
                if (rotationStrafeValue.get() != "Strict") rotationStrafeValue.set("Strict")
            } else {
                if (rotationStrafeValue.get() != "Silent") rotationStrafeValue.set("Silent")
            }
        }

        // AutoHurtTime
        if (smartHurtTime.get()) {
            for (entity in mc.theWorld!!.loadedEntityList) {
                val distance = mc.thePlayer!!.getDistanceToEntityBox(entity)
                val hurtTime = when {
                    distance <= 3.2f -> 10
                    distance <= 3.3f -> 9
                    distance <= 3.4f -> 8
                    distance <= 3.5f -> 7
                    distance <= 3.6f -> 4
                    distance <= 3.7f -> 3
                    distance <= 3.8f -> 2
                    distance <= 4.0f -> 1
                    else -> null
                }

                hurtTime?.let {
                    if (hurtTimeValue.get() != it) hurtTimeValue.set(it)
                }
            }
        }

        if (cancelRun) {
            target = null
            currentTarget = null
            hitable = false
            stopBlocking()
            return
        }

        if (noInventoryAttackValue.get() && (classProvider.isGuiContainer(mc.currentScreen) ||
                        System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())) {
            target = null
            currentTarget = null
            hitable = false
            if (classProvider.isGuiContainer(mc.currentScreen)) containerOpen = System.currentTimeMillis()
            return
        }

        if (target != null && currentTarget != null && (Backend.MINECRAFT_VERSION_MINOR == 8 || mc.thePlayer!!.getCooledAttackStrength(0.0F) >= cooldownValue.get())) {
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
                mc.thePlayer!!.lastTickPosX + (mc.thePlayer!!.posX - mc.thePlayer!!.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX,
                mc.thePlayer!!.lastTickPosY + (mc.thePlayer!!.posY - mc.thePlayer!!.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY,
                mc.thePlayer!!.lastTickPosZ + (mc.thePlayer!!.posZ - mc.thePlayer!!.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
            )
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

            GL11.glLineWidth(circleThicknessValue.get())
            GL11.glColor4f(
                circleRedValue.get().toFloat() / 255.0F,
                circleGreenValue.get().toFloat() / 255.0F,
                circleBlueValue.get().toFloat() / 255.0F,
                circleAlphaValue.get().toFloat() / 255.0F
            )
            GL11.glRotatef(90F, 1F, 0F, 0F)
            GL11.glBegin(GL11.GL_LINE_STRIP)

            for (i in 0..360 step 61 - circleAccuracy.get()) { // You can change circle accuracy  (60 - accuracy)
                GL11.glVertex2f(cos(i * Math.PI / 180.0).toFloat() * rangeValue.get(), (sin(i * Math.PI / 180.0).toFloat() * rangeValue.get()))
            }
            GL11.glVertex2f(cos(360 * Math.PI / 180.0).toFloat() * rangeValue.get(), (sin(360 * Math.PI / 180.0).toFloat() * rangeValue.get()))

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
            stopBlocking()
            return
        }

        if (noInventoryAttackValue.get() && (classProvider.isGuiContainer(mc.currentScreen) ||
                        System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())) {
            target = null
            currentTarget = null
            hitable = false
            if (classProvider.isGuiContainer(mc.currentScreen)) containerOpen = System.currentTimeMillis()
            return
        }

        target ?: return

        //Mark
        if (!targetModeValue.get().equals("Multi", ignoreCase = true)) {
            when (markValue.get().toLowerCase()) {
                "liquid" -> {
                    RenderUtils.drawPlatform(target, if (hitable) Color(37, 126, 255, 70) else Color(255, 0, 0, 70))
                }

                "block" -> {
                    val bb = target!!.entityBoundingBox
                    target!!.entityBoundingBox = bb.expand(0.2, 0.2, 0.2)
                    RenderUtils.drawEntityBox(target!!, if (target!!.hurtTime <= 0) Color.GREEN else Color.RED, false)
                    target!!.entityBoundingBox = bb
                }

                "jello" -> {
                    val drawTime = (System.currentTimeMillis() % 2000).toInt()
                    val drawMode = drawTime > 1000
                    var drawPercent = drawTime / 1000.0
                    //true when goes up
                    if (!drawMode) {
                        drawPercent = 1.5 - drawPercent
                    } else {
                        drawPercent -= 1.5
                    }
                    drawPercent = EaseUtils.easeInOutQuad(drawPercent)
                    val points = mutableListOf<WVec3>()
                    val bb = target!!.entityBoundingBox
                    val radius = bb.maxX - bb.minX
                    val height = bb.maxY - bb.minY
                    val posX = target!!.lastTickPosX + (target!!.posX - target!!.lastTickPosX) * MinecraftInstance.mc.timer.renderPartialTicks
                    var posY = target!!.lastTickPosY + (target!!.posY - target!!.lastTickPosY) * MinecraftInstance.mc.timer.renderPartialTicks
                    if (drawMode) {
                        posY -= 0.5
                    } else {
                        posY += 0.5
                    }
                    val posZ = target!!.lastTickPosZ + (target!!.posZ - target!!.lastTickPosZ) * MinecraftInstance.mc.timer.renderPartialTicks
                    for (i in 0..360 step 7) {
                        points.add(WVec3(posX - sin(i * Math.PI / 180F) * radius, posY + height * drawPercent, posZ + cos(i * Math.PI / 180F) * radius))
                    }
                    points.add(points[0])
                    //draw
                    MinecraftInstance.mc.entityRenderer.disableLightmap()
                    GL11.glPushMatrix()
                    GL11.glDisable(GL11.GL_TEXTURE_2D)
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                    GL11.glEnable(GL11.GL_LINE_SMOOTH)
                    GL11.glEnable(GL11.GL_BLEND)
                    GL11.glDisable(GL11.GL_DEPTH_TEST)
                    GL11.glBegin(GL11.GL_LINE_STRIP)
                    val baseMove = (if (drawPercent > 0.5) {
                        1 - drawPercent
                    } else {
                        drawPercent
                    }) * 2
                    val min = (height / 60) * 20 * (1 - baseMove) * (if (drawMode) {
                        -1
                    } else {
                        1
                    })
                    for (i in 0..20) {
                        var moveFace = (height / 60F) * i * baseMove
                        if (drawMode) {
                            moveFace = -moveFace
                        }
                        val firstPoint = points[0]
                        GL11.glVertex3d(
                            firstPoint.xCoord - MinecraftInstance.mc.renderManager.viewerPosX, firstPoint.yCoord - moveFace - min - MinecraftInstance.mc.renderManager.viewerPosY,
                            firstPoint.zCoord - MinecraftInstance.mc.renderManager.viewerPosZ
                        )
                        GL11.glColor4f(1F, 1F, 1F, 0.7F * (i / 20F))
                        for (vec3 in points) {
                            GL11.glVertex3d(
                                vec3.xCoord - MinecraftInstance.mc.renderManager.viewerPosX, vec3.yCoord - moveFace - min - MinecraftInstance.mc.renderManager.viewerPosY,
                                vec3.zCoord - MinecraftInstance.mc.renderManager.viewerPosZ
                            )
                        }
                        GL11.glColor4f(0F, 0F, 0F, 0F)
                    }
                    GL11.glEnd()
                    GL11.glEnable(GL11.GL_DEPTH_TEST)
                    GL11.glDisable(GL11.GL_LINE_SMOOTH)
                    GL11.glDisable(GL11.GL_BLEND)
                    GL11.glEnable(GL11.GL_TEXTURE_2D)
                    GL11.glPopMatrix()
                }

                "forever" -> {
                    RenderUtils.drawCircleESP(target, 0.67, Color.RED.rgb, true)
                }

                "lies" -> {
                    val everyTime = 3000
                    val drawTime = (System.currentTimeMillis() % everyTime).toInt()
                    val drawMode = drawTime > (everyTime / 2)
                    var drawPercent = drawTime / (everyTime / 2.0)
                    // true when goes up
                    if (!drawMode) {
                        drawPercent = 1 - drawPercent
                    } else {
                        drawPercent -= 1
                    }
                    drawPercent = EaseUtils.easeInOutQuad(drawPercent)
                    MinecraftInstance.mc2.entityRenderer.disableLightmap()
                    GL11.glPushMatrix()
                    GL11.glDisable(GL11.GL_TEXTURE_2D)
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                    GL11.glEnable(GL11.GL_LINE_SMOOTH)
                    GL11.glEnable(GL11.GL_BLEND)
                    GL11.glDisable(GL11.GL_DEPTH_TEST)
                    GL11.glDisable(GL11.GL_CULL_FACE)
                    GL11.glShadeModel(7425)
                    MinecraftInstance.mc2.entityRenderer.disableLightmap()

                    val bb = target!!.entityBoundingBox
                    val radius = ((bb.maxX - bb.minX) + (bb.maxZ - bb.minZ)) * 0.5f
                    val height = bb.maxY - bb.minY
                    val x =
                        target!!.lastTickPosX + (target!!.posX - target!!.lastTickPosX) * event.partialTicks - MinecraftInstance.mc2.renderManager.viewerPosX
                    val y =
                        (target!!.lastTickPosY + (target!!.posY - target!!.lastTickPosY) * event.partialTicks - MinecraftInstance.mc2.renderManager.viewerPosY) + height * drawPercent
                    val z =
                        target!!.lastTickPosZ + (target!!.posZ - target!!.lastTickPosZ) * event.partialTicks - MinecraftInstance.mc2.renderManager.viewerPosZ
                    val eased = (height / 3) * (if (drawPercent > 0.5) {
                        1 - drawPercent
                    } else {
                        drawPercent
                    }) * (if (drawMode) {
                        -1
                    } else {
                        1
                    })
                    for (i in 5..360 step 5) {
                        val color = Color.getHSBColor(
                            if (i < 180) {
                                0.55F + (0.85F - 0.55F) * (i / 180f)
                            } else {
                                0.55F + (0.85F - 0.55F) * (-(i - 360) / 180f)
                            }, 0.7f, 1.0f
                        )
                        val x1 = x - sin(i * Math.PI / 180F) * radius
                        val z1 = z + cos(i * Math.PI / 180F) * radius
                        val x2 = x - sin((i - 5) * Math.PI / 180F) * radius
                        val z2 = z + cos((i - 5) * Math.PI / 180F) * radius
                        GL11.glBegin(GL11.GL_QUADS)
                        RenderUtils.glColor(color, 0f)
                        GL11.glVertex3d(x1, y + eased, z1)
                        GL11.glVertex3d(x2, y + eased, z2)
                        RenderUtils.glColor(color, 150f)
                        GL11.glVertex3d(x2, y, z2)
                        GL11.glVertex3d(x1, y, z1)
                        GL11.glEnd()
                    }

                    GL11.glEnable(GL11.GL_CULL_FACE)
                    GL11.glShadeModel(7424)
                    GL11.glColor4f(1f, 1f, 1f, 1f)
                    GL11.glEnable(GL11.GL_DEPTH_TEST)
                    GL11.glDisable(GL11.GL_LINE_SMOOTH)
                    GL11.glDisable(GL11.GL_BLEND)
                    GL11.glEnable(GL11.GL_TEXTURE_2D)
                    GL11.glPopMatrix()
                }

            }
        }

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
        val thePlayer = mc.thePlayer ?: return
        val theWorld = mc.theWorld ?: return

        // Settings
        val failRate = failRateValue.get()
        val swing = swingValue.get()
        val multi = targetModeValue.get().equals("Multi", ignoreCase = true)
        val openInventory = aacValue.get() && classProvider.isGuiContainer(mc.currentScreen)
        val failHit = failRate > 0 && Random().nextInt(100) <= failRate

        // Close inventory when open
        if (openInventory)
            mc.netHandler.addToSendQueue(classProvider.createCPacketCloseWindow())

        // Check is not hitable or check failrate

        if (!hitable || failHit) {
            if (swing && (fakeSwingValue.get() || failHit))
                thePlayer.swingItem()
        } else {
            // Attack
            if (!multi) {
                attackEntity(currentTarget!!)
            } else {
                var targets = 0

                for (entity in theWorld.loadedEntityList) {
                    val distance = thePlayer.getDistanceToEntityBox(entity)

                    if (classProvider.isEntityLivingBase(entity) && isEnemy(entity) && distance <= getRange(entity)) {
                        attackEntity(entity.asEntityLivingBase())

                        targets += 1

                        if (limitedMultiTargetsValue.get() != 0 && limitedMultiTargetsValue.get() <= targets)
                            break
                    }
                }
            }

            if(targetModeValue.get().equals("Switch", true)){
                if(switchTimer.hasTimePassed(switchDelayValue.get().toLong())){
                    prevTargetEntities.add(if (aacValue.get()) target!!.entityId else currentTarget!!.entityId)
                    switchTimer.reset()
                }
            }else{
                prevTargetEntities.add(if (aacValue.get()) target!!.entityId else currentTarget!!.entityId)
            }

            if (target == currentTarget)
                target = null
        }

        // Open inventory
        if (openInventory)
            mc.netHandler.addToSendQueue(createOpenInventoryPacket())
    }

    /**
     * Update current target
     */
    private fun updateTarget() {
        // Reset fixed target to null
        target = null

        // Settings
        val hurtTime = hurtTimeValue.get()
        val fov = fovValue.get()
        val switchMode = targetModeValue.get().equals("Switch", ignoreCase = true)

        // Find possible targets
        val targets = mutableListOf<IEntityLivingBase>()

        val theWorld = mc.theWorld!!
        val thePlayer = mc.thePlayer!!

        for (entity in theWorld.loadedEntityList) {
            if (!classProvider.isEntityLivingBase(entity) || !isEnemy(entity) || (switchMode && prevTargetEntities.contains(entity.entityId)))
                continue

            val distance = thePlayer.getDistanceToEntityBox(entity)
            val entityFov = RotationUtils.getRotationDifference(entity)

            if (distance <= maxRange && (fov == 180F || entityFov <= fov) && entity.asEntityLivingBase().hurtTime <= hurtTime)
                targets.add(entity.asEntityLivingBase())
        }

        // Sort targets by priority
        when (priorityValue.get().toLowerCase()) {
            "distance" -> targets.sortBy { thePlayer.getDistanceToEntityBox(it) } // Sort by distance
            "health" -> targets.sortBy { it.health } // Sort by health
            "direction" -> targets.sortBy { RotationUtils.getRotationDifference(it) } // Sort by FOV
            "livingtime" -> targets.sortBy { -it.ticksExisted } // Sort by existence
        }

        // Find best target
        for (entity in targets) {
            // Update rotations to current target
            if (!updateRotations(entity)) // when failed then try another target
                continue

            // Set target to current entity
            target = entity
            return
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
    private fun isEnemy(entity: IEntity?): Boolean {
        if (classProvider.isEntityLivingBase(entity) && entity != null && (EntityUtils.targetDead || isAlive(entity.asEntityLivingBase())) && entity != mc.thePlayer) {
            if (!EntityUtils.targetInvisible && entity.invisible)
                return false

            if (EntityUtils.targetPlayer && classProvider.isEntityPlayer(entity)) {
                if (entity.asEntityPlayer().spectator || AntiBot.isBot(entity.asEntityLivingBase()))
                    return false

                if (EntityUtils.isFriend(entity) && !Terra.moduleManager[NoFriends::class.java].state)
                    return false

                val teams = Terra.moduleManager[Teams::class.java] as Teams

                return !teams.state || !teams.isInYourTeam(entity.asEntityLivingBase())
            }

            return EntityUtils.targetMobs && EntityUtils.isMob(entity) || EntityUtils.targetAnimals &&
                    EntityUtils.isAnimal(entity)
        }

        return false
    }

    /**
     * Attack [entity]
     */
    private fun attackEntity(entity: IEntityLivingBase) {
        // Stop blocking
        val thePlayer = mc.thePlayer!!

        if (!blockModeValue.get().equals("None", true) && (mc.thePlayer!!.isBlocking || blockingStatus)) {
            mc.netHandler.addToSendQueue(classProvider.createCPacketPlayerDigging(ICPacketPlayerDigging.WAction.RELEASE_USE_ITEM,
                WBlockPos.ORIGIN, classProvider.getEnumFacing(EnumFacingType.DOWN)))
            if (afterAttackValue.get()) blockingStatus = false
        }

        // Call attack event
        Terra.eventManager.callEvent(AttackEvent(entity))

        // Attack target
        if (swingValue.get() && Backend.MINECRAFT_VERSION_MINOR == 8)
            thePlayer.swingItem()

        mc.netHandler.addToSendQueue(classProvider.createCPacketUseEntity(entity, ICPacketUseEntity.WAction.ATTACK))

        if (swingValue.get() && Backend.MINECRAFT_VERSION_MINOR != 8)
            thePlayer.swingItem()

        if (keepSprintValue.get()) {
            // Critical Effect
            if (thePlayer.fallDistance > 0F && !thePlayer.onGround && !thePlayer.isOnLadder &&
                    !thePlayer.isInWater && !thePlayer.isPotionActive(classProvider.getPotionEnum(PotionType.BLINDNESS)) && !thePlayer.isRiding)
                thePlayer.onCriticalHit(entity)

            // Enchant Effect
            if (functions.getModifierForCreature(thePlayer.heldItem, entity.creatureAttribute) > 0F)
                thePlayer.onEnchantmentCritical(entity)
        } else {
            if (mc.playerController.currentGameType != IWorldSettings.WGameType.SPECTATOR)
                thePlayer.attackTargetEntityWithCurrentItem(entity)
        }

        // Extra critical effects
        val criticals = Terra.moduleManager[Criticals::class.java] as Criticals

        for (i in 0..2) {
            // Critical Effect
            if (thePlayer.fallDistance > 0F && !thePlayer.onGround && !thePlayer.isOnLadder && !thePlayer.isInWater && !thePlayer.isPotionActive(classProvider.getPotionEnum(PotionType.BLINDNESS)) && thePlayer.ridingEntity == null || criticals.state && criticals.msTimer.hasTimePassed(criticals.delayValue.get().toLong()) && !thePlayer.isInWater && !thePlayer.isInLava && !thePlayer.isInWeb)
                thePlayer.onCriticalHit(target!!)

            // Enchant Effect
            if (functions.getModifierForCreature(thePlayer.heldItem, target!!.creatureAttribute) > 0.0f || fakeSharpValue.get())
                thePlayer.onEnchantmentCritical(target!!)
        }

        // Start blocking after attack
        if (thePlayer.isBlocking || (!blockModeValue.get().equals("None",ignoreCase = true) && canBlock)) {
            if (!(blockRate.get() > 0 && Random().nextInt(100) <= blockRate.get()))
                return

            if (delayedBlockValue.get())
                return

            startBlocking(entity)
        }

        @Suppress("ConstantConditionIf")
        if (Backend.MINECRAFT_VERSION_MINOR != 8) {
            thePlayer.resetCooldown()
        }
    }

    /**
     * Update killaura rotations to enemy
     */
    private fun updateRotations(entity: IEntity): Boolean {
        var boundingBox = entity.entityBoundingBox
        if (jumpFix.get() && jump == 0) return false
        if (rotations.get().equals("Vanilla", ignoreCase = true)){
            if (maxTurnSpeed.get() <= 0F)
                return true

            if (predictValue.get())
                boundingBox = boundingBox.offset(
                        (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                        (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                        (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
                )

            val (vec, rotation) = RotationUtils.searchCenter(
                    boundingBox,
                    outborderValue.get() && !attackTimer.hasTimePassed(attackDelay / 2),
                    randomCenterValue.get(),
                    predictValue.get(),
                    mc.thePlayer!!.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(),
                    maxRange
            ) ?: return false

            val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation, rotation,
                    (Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat())

            if (silentRotationValue.get())
                RotationUtils.setTargetRotation(limitedRotation, if (aacValue.get()) 15 else 0)
            else
                limitedRotation.toPlayer(mc.thePlayer!!)

            return true
        }
        if (rotations.get().equals("BackTrack", ignoreCase = true)) {
            if (predictValue.get())
                boundingBox = boundingBox.offset(
                        (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                        (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                        (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
                )

            val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation,
                    RotationUtils.OtherRotation(boundingBox,RotationUtils.getCenter(entity.entityBoundingBox), predictValue.get(),
                            mc.thePlayer!!.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(),maxRange), (Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat())

            if (silentRotationValue.get()) {
                RotationUtils.setTargetRotation(limitedRotation, if (aacValue.get()) 15 else 0)
            }else {
                limitedRotation.toPlayer(mc.thePlayer!!)
                return true
            }
        }
        if (rotations.get().equals("Terra", ignoreCase = true)) {
            var boundingBox = entity.entityBoundingBox
            if (maxTurnSpeed.get() <= 0F)
                return true

            if (predictValue.get())
                boundingBox = boundingBox.offset(
                    (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get()),
                    (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(minPredictSize.get(), maxPredictSize.get())
                )

            val (_) = RotationUtils.lockView(
                boundingBox,
                outborderValue.get() && !attackTimer.hasTimePassed(attackDelay / 2),
                randomCenterValue.get(),
                predictValue.get(),
                mc.thePlayer!!.getDistanceToEntityBox(entity) < throughWallsRangeValue.get(),
                maxRange
            ) ?: return false

            val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation, RotationUtils.getNewRotations(RotationUtils.getCenter(entity.entityBoundingBox), false),
                (Math.random() * (maxTurnSpeed.get() - minTurnSpeed.get()) + minTurnSpeed.get()).toFloat())

            if (silentRotationValue.get())
                RotationUtils.setTargetRotation(limitedRotation, if (aacValue.get()) 15 else 0)
            else
                limitedRotation.toPlayer(mc.thePlayer!!)

            return true
        }
        return true
    }

    /**
     * Check if enemy is hitable with current rotations
     */
    private fun updateHitable() {
        if (hitableValue.get()) {
            hitable = true
            return
        }
        // Disable hitable check if turn speed is zero
        if (maxTurnSpeed.get() <= 0F) {
            hitable = true
            return
        }

        val reach = min(maxRange.toDouble(), mc.thePlayer!!.getDistanceToEntityBox(target!!)) + 1

        if (raycastValue.get()) {
            val raycastedEntity = RaycastUtils.raycastEntity(reach, object : RaycastUtils.EntityFilter {
                override fun canRaycast(entity: IEntity?): Boolean {
                    return (!livingRaycastValue.get() || (classProvider.isEntityLivingBase(entity) && !classProvider.isEntityArmorStand(entity))) &&
                            (isEnemy(entity) || raycastIgnoredValue.get() || aacValue.get() && mc.theWorld!!.getEntitiesWithinAABBExcludingEntity(entity, entity!!.entityBoundingBox).isNotEmpty())
                }

            })


            if (raycastValue.get() && raycastedEntity != null && classProvider.isEntityLivingBase(raycastedEntity)
                    && (Terra.moduleManager[NoFriends::class.java].state || !EntityUtils.isFriend(raycastedEntity)))
                currentTarget = raycastedEntity.asEntityLivingBase()

            hitable = if (maxTurnSpeed.get() > 0F) currentTarget == raycastedEntity else true
        } else
            hitable = RotationUtils.isFaced(currentTarget, reach)
    }

    /**
     * Start blocking
     */
    private fun startBlocking(interactEntity: IEntity) {
        if (blockModeValue.get().equals("Packet", true)) {
            mc.netHandler.addToSendQueue(createUseItemPacket(mc.thePlayer!!.inventory.getCurrentItemInHand(), WEnumHand.MAIN_HAND))
            mc.netHandler.addToSendQueue(createUseItemPacket(mc.thePlayer!!.inventory.getCurrentItemInHand(), WEnumHand.OFF_HAND))
        }

        blockingStatus = true
    }


    /**
     * Stop blocking
     */
    private fun stopBlocking() {
        if (blockingStatus) {
            if (blockModeValue.get().equals("Packet", true)) {
                mc.netHandler.addToSendQueue(
                    classProvider.createCPacketPlayerDigging(
                        ICPacketPlayerDigging.WAction.RELEASE_USE_ITEM,
                        WBlockPos.ORIGIN,
                        classProvider.getEnumFacing(EnumFacingType.DOWN)
                    )
                )
            }
            blockingStatus = false
        }
    }

    /**
     * Check if run should be cancelled
     */
    private val cancelRun: Boolean
        inline get() = mc.thePlayer!!.spectator || !isAlive(mc.thePlayer!!)
                || Terra.moduleManager[Blink::class.java].state
                || Terra.moduleManager[FreeCam::class.java].state
                || noScaffoldValue.get() && Terra.moduleManager.getModule(Scaffold::class.java).state
                || noScaffoldValue.get() && Terra.moduleManager.getModule(ScaHelp::class.java).state

    /**
     * Check if [entity] is alive
     */
    private fun isAlive(entity: IEntityLivingBase) = entity.entityAlive && entity.health > 0 ||
            aacValue.get() && entity.hurtTime > 5


    /**
     * Check if player is able to block
     */
    private val canBlock: Boolean
        inline get() = mc.thePlayer!!.heldItem != null && classProvider.isItemSword(mc.thePlayer!!.heldItem!!.item)

    /**
     * Range
     */

    private val maxRange: Float
        get() = max(rangeValue.get(), 3F)

    private fun getRange(entity: IEntity) =
            (if (mc.thePlayer!!.getDistanceToEntityBox(entity) >= 3F) rangeValue.get() else rangeValue.get()) - if (mc.thePlayer!!.sprinting) rangeSprintReducementValue.get() else 0F
    /**
     * HUD Tag
     */
    override val tag: String?
        get() = targetModeValue.get()
}