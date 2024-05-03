/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.world


import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.api.enums.BlockType
import net.ccbluex.liquidbounce.api.enums.EnumFacingType
import net.ccbluex.liquidbounce.api.minecraft.item.IItemBlock
import net.ccbluex.liquidbounce.api.minecraft.item.IItemStack
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketEntityAction
import net.ccbluex.liquidbounce.api.minecraft.util.*
import net.ccbluex.liquidbounce.api.minecraft.util.WMathHelper.wrapAngleTo180_float
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.injection.backend.unwrap
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.InventoryUtils.Companion.findAutoBlockBlock
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.block.BlockUtils.canBeClicked
import net.ccbluex.liquidbounce.utils.block.BlockUtils.isReplaceable
import net.ccbluex.liquidbounce.utils.block.PlaceInfo
import net.ccbluex.liquidbounce.utils.block.PlaceInfo.Companion.get
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// code by lvziqiao
@ModuleInfo(
    name = "Scaffold",
    description = "ez",
    category = ModuleCategory.WORLD,
    keyBind = Keyboard.KEY_F,
    autoDisable = EnumAutoDisableType.GAME_END
)
class Scaffold : Module() {
    /**
     * OPTIONS
     */
    // Mode
    val modeValue: ListValue =
        object : ListValue("Mode", arrayOf("Normal", "HuaYuTing", "Rewinside", "Expand"), "HuaYuTing") {
            override fun onChange(oldValue: String, newValue: String) {
                if (newValue.equals("HuaYuTing", ignoreCase = true)) {
                    sprintModeValue.set("HuaYuTing")
                    placeConditionValue.set("HuaYuTing")
                }
            }
        }

    // Delay
    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 80, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minDelayValue.get()

            if (i > newValue) set(i)
        }
    }

    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 50, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxDelayValue.get()

            if (i < newValue) set(i)
        }
    }
    private val placeableDelay = BoolValue("PlaceableDelay", false)

    // AutoBlock
    private val autoBlockValue = ListValue("AutoBlock", arrayOf("Off", "Spoof", "Pick", "Switch"), "Pick")

    val sprintModeValue =
        ListValue("Sprint-Mode", arrayOf("Normal", "OnGround", "HuaYuTing", "OffGround", "Off"), "OnGround")
    private val placeConditionValue =
        ListValue("Place-Condition", arrayOf("Air", "FallDown", "NegativeMotion", "HuaYuTing", "Always"), "HuaYuTing")
    private val swingValue = BoolValue("Swing", true)
    private val searchValue = BoolValue("Search", true)
    private val downValue = BoolValue("Down", true)
    private val picker = BoolValue("Picker", false)
    private val placeModeValue = ListValue("PlaceTiming", arrayOf("Pre", "Post"), "Post")

    // Eagle
    private val eagleValue = ListValue("Eagle", arrayOf("Normal", "EdgeDistance", "Silent", "Off"), "Off")
    private val blocksToEagleValue = IntegerValue("BlocksToEagle", 0, 0, 10)
    private val edgeDistanceValue = FloatValue("EagleEdgeDistance", 0.2f, 0f, 0.5f)

    // Expand
    private val expandLengthValue = IntegerValue("ExpandLength", 0, 1, 6)

    // RotationStrafe
    private val rotationStrafeValue = BoolValue("RotationStrafe", true)

    // Rotations
    private val rotationModeValue =
        ListValue("RotationMode", arrayOf("Basic", "Normal", "Static", "StaticPitch", "StaticYaw", "Off"), "StaticYaw")
    private val silentRotation = BoolValue("SilentRotation", true)
    private val keepRotationValue = BoolValue("KeepRotation", true)
    private val keepLengthValue = IntegerValue("KeepRotationLength", 0, 0, 20)
    private val staticPitchValue = FloatValue("StaticPitchOffset", 86f, 70f, 90f)
    private val staticYawOffsetValue = FloatValue("StaticYawOffset", 0f, 0f, 90f)


    // Other
    private val xzRangeValue = FloatValue("xzRange", 0.2f, 0.1f, 1.0f)
    private val yRangeValue = FloatValue("yRange", 0.2f, 0.1f, 1.0f)

    // SearchAccuracy
    private val searchAccuracyValue: IntegerValue = object : IntegerValue("SearchAccuracy", 8, 1, 24) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            if (maximum < newValue) {
                set(maximum)
            } else if (minimum > newValue) {
                set(minimum)
            }
        }
    }

    // Turn Speed
    private val maxTurnSpeedValue: FloatValue = object : FloatValue("MaxTurnSpeed", 180f, 1f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minTurnSpeedValue.get()
            if (v > newValue) set(v)
            if (maximum < newValue) {
                set(maximum)
            } else if (minimum > newValue) {
                set(minimum)
            }
        }
    }
    private val minTurnSpeedValue: FloatValue = object : FloatValue("MinTurnSpeed", 180f, 1f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxTurnSpeedValue.get()
            if (v < newValue) set(v)
            if (maximum < newValue) {
                set(maximum)
            } else if (minimum > newValue) {
                set(minimum)
            }
        }
    }

    // Zitter
    private val zitterValue = BoolValue("Zitter", false)
    private val zitterModeValue = ListValue("ZitterMode", arrayOf("Teleport", "Smooth"), "Teleport")
    private val zitterSpeed = FloatValue("ZitterSpeed", 0.13f, 0.1f, 0.3f)
    private val zitterStrength = FloatValue("ZitterStrength", 0.072f, 0.05f, 0.2f)

    // Game
    private val timerValue = FloatValue("Timer", 1f, 0.1f, 10f)
    private val speedModifierValue = FloatValue("SpeedModifier", 1f, 0f, 2f)
    private val slowValue: BoolValue = object : BoolValue("Slow", false) {
        override fun onChanged(oldValue: Boolean, newValue: Boolean) {
            if (newValue) sprintModeValue.set("Off")
        }
    }
    private val slowSpeed = FloatValue("SlowSpeed", 0.6f, 0.2f, 0.8f)

    // Safety
    private val lastMS = 0L
    private var progress = 0f
    private val sameYValue = BoolValue("SameY", false)

    private val smartSpeedValue = BoolValue("SmartSpeed", false)
    private val autoJumpValue = BoolValue("AutoJump", true)
    private val safeWalkValue = BoolValue("SafeWalk", true)
    private val airSafeValue = BoolValue("AirSafe", false)

    // Visuals
    private val counterDisplayValue = BoolValue("Counter", true)
    private val markValue = BoolValue("Mark", false)

    private val autoTimer = BoolValue("AutoTimer", false)

    /**
     * MODULE
     */
    private var lastSprint = 0

    // Target block
    private var targetPlace: PlaceInfo? = null

    // Launch position
    private var launchY = 0

    // Rotation lock
    private var lockRotation: Rotation? = null
    private var limitedRotation: Rotation? = null
    private var facesBlock = false

    // Auto block slot
    private var slot = 0

    // Zitter Smooth
    private var zitterDirection = false

    // Delay
    private val delayTimer = MSTimer()
    private val zitterTimer = MSTimer()
    private var delay: Long = 0

    // Eagle
    private var placedBlocksWithoutEagle = 0
    private var eagleSneaking = false

    // Down
    private var shouldGoDown = false

    fun shouldSprint(): Boolean {
        return ((sprintModeValue.equals("Normal")) || (sprintModeValue.equals(
            "OffGround"
        ) && !mc.thePlayer!!.onGround) || (sprintModeValue.equals(
            "OnGround"
        ) && mc.thePlayer!!.onGround) || (sprintModeValue.equals(
            "HuaYuTing"
        ) && mc.thePlayer!!.ticksExisted >= lastSprint))
    }

    fun shouldPlace(): Boolean {
        val placeWhenAir = placeConditionValue.get().equals("air", ignoreCase = true)
        val placeWhenFall = placeConditionValue.get().equals("falldown", ignoreCase = true)
        val placeWhenNegativeMotion = placeConditionValue.get().equals("negativemotion", ignoreCase = true)
        val huayuting = placeConditionValue.get().equals("huayuting", ignoreCase = true)
        val alwaysPlace = placeConditionValue.get().equals("always", ignoreCase = true)
        return Terra.moduleManager.getModule(Tower::class.java)
            .state || alwaysPlace || (placeWhenAir && !mc.thePlayer!!.onGround) || (placeWhenFall && mc.thePlayer!!.fallDistance > 0) || (placeWhenNegativeMotion && mc.thePlayer!!.motionY < 0) || (huayuting && mc.thePlayer!!.ticksExisted >= lastSprint)
    }

    /**
     * Enable module
     */
    override fun onEnable() {
        if (mc.thePlayer == null) return
        if (autoTimer.get()) {
            val timer: Timer = Terra.moduleManager.getModule(Timer::class.java) as Timer
            timer.state = (true)
        }
        progress = 0f
        lastSprint = 0
        launchY = mc.thePlayer!!.posY.toInt()
    }

    /**
     * Tick event
     * @param event
     */
    @EventTarget
    fun onTick(event: TickEvent?) {
        mc.timer.timerSpeed = timerValue.get()
    }

    /**
     * Update event
     *
     * @param event
     */
    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (modeValue.get().equals("HuaYuTing", ignoreCase = true)) {
            if (mc.thePlayer!!.onGround && !mc.gameSettings.keyBindJump.pressed) {
                if (isMoving) {
                    lockRotation = null
                    limitedRotation = null
                    RotationUtils.reset()
                    facesBlock = false
                    shouldGoDown = false
                    lastSprint = mc.thePlayer!!.ticksExisted + 2
                    mc.thePlayer!!.sprinting = true
                    mc.gameSettings.keyBindJump.pressed = true
                }
            } else if (!mc.thePlayer!!.onGround) {
                mc.gameSettings.keyBindJump.pressed = false
            }
        }

        if (blocksAmount == 0) {
            this.toggle()
        }
        bestBlocks

        shouldGoDown =
            downValue.get() && !sameYValue.get() && mc.gameSettings.keyBindSneak.isKeyDown && blocksAmount > 1
        if (shouldGoDown) mc.gameSettings.keyBindSneak.pressed = false

        if (slowValue.get()) {
            mc.thePlayer!!.motionX = mc.thePlayer!!.motionX * slowSpeed.get()
            mc.thePlayer!!.motionZ = mc.thePlayer!!.motionZ * slowSpeed.get()
        }

        if (shouldSprint()) {
            if (!mc.gameSettings.isKeyDown(mc.gameSettings.keyBindSprint)) {
                mc.gameSettings.keyBindSprint.pressed = false
            }
            if (mc.gameSettings.isKeyDown(mc.gameSettings.keyBindSprint)) {
                mc.gameSettings.keyBindSprint.pressed = true
            }
            if (mc.gameSettings.keyBindSprint.isKeyDown) {
                mc.thePlayer!!.sprinting = true
            }
            if (!mc.gameSettings.keyBindSprint.isKeyDown) {
                mc.thePlayer!!.sprinting = false
            }
        }

        if (mc.thePlayer!!.onGround) {
            val mode = modeValue.get()

            // Rewinside scaffold mode
            if (mode.equals("Rewinside", ignoreCase = true)) {
                strafe(0.2f)
                mc.thePlayer!!.motionY = 0.0
            }

            // Smooth Zitter
            if (zitterValue.get() && zitterModeValue.get().equals("smooth", ignoreCase = true)) {
                if (!mc.gameSettings.isKeyDown(mc.gameSettings.keyBindRight)) mc.gameSettings.keyBindRight.pressed =
                    false

                if (!mc.gameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) mc.gameSettings.keyBindLeft.pressed = false

                if (zitterTimer.hasTimePassed(100)) {
                    zitterDirection = !zitterDirection
                    zitterTimer.reset()
                }

                if (zitterDirection) {
                    mc.gameSettings.keyBindRight.pressed = true
                    mc.gameSettings.keyBindLeft.pressed = false
                } else {
                    mc.gameSettings.keyBindRight.pressed = false
                    mc.gameSettings.keyBindLeft.pressed = true
                }
            }

            // Eagle
            if (!eagleValue.get().equals("Off", ignoreCase = true) && !shouldGoDown) {
                var dif = 0.5
                if (eagleValue.get().equals("EdgeDistance", ignoreCase = true) && !shouldGoDown) {
                    for (i in 0..3) {
                        when (i) {
                            0 -> {
                                run {
                                    val blockPos = WBlockPos(
                                        mc.thePlayer!!.posX - 1,
                                        mc.thePlayer!!.posY - (if (mc.thePlayer!!.posY == mc.thePlayer!!.posY.toInt() + 0.5) 0.0 else 1.0),
                                        mc.thePlayer!!.posZ
                                    )
                                    val placeInfo = get(blockPos)
                                    if (isReplaceable(blockPos) && placeInfo != null) {
                                        var calcDif = mc.thePlayer!!.posX - blockPos.x
                                        calcDif -= 0.5

                                        if (calcDif < 0) calcDif *= -1.0
                                        calcDif -= 0.5

                                        if (calcDif < dif) dif = calcDif
                                    }
                                }
                                run {
                                    val blockPos = WBlockPos(
                                        mc.thePlayer!!.posX + 1,
                                        mc.thePlayer!!.posY - (if (mc.thePlayer!!.posY == mc.thePlayer!!.posY.toInt() + 0.5) 0.0 else 1.0),
                                        mc.thePlayer!!.posZ
                                    )
                                    val placeInfo = get(blockPos)
                                    if (isReplaceable(blockPos) && placeInfo != null) {
                                        var calcDif = mc.thePlayer!!.posX - blockPos.x
                                        calcDif -= 0.5

                                        if (calcDif < 0) calcDif *= -1.0
                                        calcDif -= 0.5

                                        if (calcDif < dif) dif = calcDif
                                    }
                                }
                                run {
                                    val blockPos = WBlockPos(
                                        mc.thePlayer!!.posX,
                                        mc.thePlayer!!.posY - (if (mc.thePlayer!!.posY == mc.thePlayer!!.posY.toInt() + 0.5) 0.0 else 1.0),
                                        mc.thePlayer!!.posZ - 1
                                    )
                                    val placeInfo = get(blockPos)
                                    if (isReplaceable(blockPos) && placeInfo != null) {
                                        var calcDif = mc.thePlayer!!.posZ - blockPos.z
                                        calcDif -= 0.5

                                        if (calcDif < 0) calcDif *= -1.0
                                        calcDif -= 0.5

                                        if (calcDif < dif) dif = calcDif
                                    }
                                }
                                run {
                                    val blockPos = WBlockPos(
                                        mc.thePlayer!!.posX,
                                        mc.thePlayer!!.posY - (if (mc.thePlayer!!.posY == mc.thePlayer!!.posY.toInt() + 0.5) 0.0 else 1.0),
                                        mc.thePlayer!!.posZ + 1
                                    )
                                    val placeInfo = get(blockPos)
                                    if (isReplaceable(blockPos) && placeInfo != null) {
                                        var calcDif = mc.thePlayer!!.posZ - blockPos.z
                                        calcDif -= 0.5

                                        if (calcDif < 0) calcDif *= -1.0
                                        calcDif -= 0.5

                                        if (calcDif < dif) dif = calcDif
                                    }
                                }
                            }

                            1 -> {
                                run {
                                    val blockPos = WBlockPos(
                                        mc.thePlayer!!.posX + 1,
                                        mc.thePlayer!!.posY - (if (mc.thePlayer!!.posY == mc.thePlayer!!.posY.toInt() + 0.5) 0.0 else 1.0),
                                        mc.thePlayer!!.posZ
                                    )
                                    val placeInfo = get(blockPos)
                                    if (isReplaceable(blockPos) && placeInfo != null) {
                                        var calcDif = mc.thePlayer!!.posX - blockPos.x
                                        calcDif -= 0.5

                                        if (calcDif < 0) calcDif *= -1.0
                                        calcDif -= 0.5

                                        if (calcDif < dif) dif = calcDif
                                    }
                                }
                                run {
                                    val blockPos = WBlockPos(
                                        mc.thePlayer!!.posX,
                                        mc.thePlayer!!.posY - (if (mc.thePlayer!!.posY == mc.thePlayer!!.posY.toInt() + 0.5) 0.0 else 1.0),
                                        mc.thePlayer!!.posZ - 1
                                    )
                                    val placeInfo = get(blockPos)
                                    if (isReplaceable(blockPos) && placeInfo != null) {
                                        var calcDif = mc.thePlayer!!.posZ - blockPos.z
                                        calcDif -= 0.5

                                        if (calcDif < 0) calcDif *= -1.0
                                        calcDif -= 0.5

                                        if (calcDif < dif) dif = calcDif
                                    }
                                }
                                run {
                                    val blockPos = WBlockPos(
                                        mc.thePlayer!!.posX,
                                        mc.thePlayer!!.posY - (if (mc.thePlayer!!.posY == mc.thePlayer!!.posY.toInt() + 0.5) 0.0 else 1.0),
                                        mc.thePlayer!!.posZ + 1
                                    )
                                    val placeInfo = get(blockPos)
                                    if (isReplaceable(blockPos) && placeInfo != null) {
                                        var calcDif = mc.thePlayer!!.posZ - blockPos.z
                                        calcDif -= 0.5

                                        if (calcDif < 0) calcDif *= -1.0
                                        calcDif -= 0.5

                                        if (calcDif < dif) dif = calcDif
                                    }
                                }
                            }

                            2 -> {
                                run {
                                    val blockPos = WBlockPos(
                                        mc.thePlayer!!.posX,
                                        mc.thePlayer!!.posY - (if (mc.thePlayer!!.posY == mc.thePlayer!!.posY.toInt() + 0.5) 0.0 else 1.0),
                                        mc.thePlayer!!.posZ - 1
                                    )
                                    val placeInfo = get(blockPos)
                                    if (isReplaceable(blockPos) && placeInfo != null) {
                                        var calcDif = mc.thePlayer!!.posZ - blockPos.z
                                        calcDif -= 0.5

                                        if (calcDif < 0) calcDif *= -1.0
                                        calcDif -= 0.5

                                        if (calcDif < dif) dif = calcDif
                                    }
                                }
                                run {
                                    val blockPos = WBlockPos(
                                        mc.thePlayer!!.posX,
                                        mc.thePlayer!!.posY - (if (mc.thePlayer!!.posY == mc.thePlayer!!.posY.toInt() + 0.5) 0.0 else 1.0),
                                        mc.thePlayer!!.posZ + 1
                                    )
                                    val placeInfo = get(blockPos)
                                    if (isReplaceable(blockPos) && placeInfo != null) {
                                        var calcDif = mc.thePlayer!!.posZ - blockPos.z
                                        calcDif -= 0.5

                                        if (calcDif < 0) calcDif *= -1.0
                                        calcDif -= 0.5

                                        if (calcDif < dif) dif = calcDif
                                    }
                                }
                            }

                            3 -> {
                                val blockPos = WBlockPos(
                                    mc.thePlayer!!.posX,
                                    mc.thePlayer!!.posY - (if (mc.thePlayer!!.posY == mc.thePlayer!!.posY.toInt() + 0.5) 0.0 else 1.0),
                                    mc.thePlayer!!.posZ + 1
                                )
                                val placeInfo = get(blockPos)

                                if (isReplaceable(blockPos) && placeInfo != null) {
                                    var calcDif = mc.thePlayer!!.posZ - blockPos.z
                                    calcDif -= 0.5

                                    if (calcDif < 0) calcDif *= -1.0
                                    calcDif -= 0.5

                                    if (calcDif < dif) dif = calcDif
                                }
                            }
                        }
                    }
                }

                if (placedBlocksWithoutEagle >= blocksToEagleValue.get()) {
                    val shouldEagle = mc.theWorld!!.getBlockState(
                        WBlockPos(
                            mc.thePlayer!!.posX,
                            mc.thePlayer!!.posY - 1.0, mc.thePlayer!!.posZ
                        )
                    ).block == classProvider.getBlockEnum(BlockType.AIR) || (dif < edgeDistanceValue.get() && eagleValue.get()
                        .equals("EdgeDistance", ignoreCase = true))

                    if (eagleValue.get().equals("Silent", ignoreCase = true) && !shouldGoDown) {
                        if (eagleSneaking != shouldEagle) {
                            mc.netHandler.addToSendQueue(
                                classProvider.createCPacketEntityAction(
                                    mc.thePlayer!!,
                                    if (shouldEagle) ICPacketEntityAction.WAction.START_SNEAKING else ICPacketEntityAction.WAction.STOP_SNEAKING
                                )
                            )
                        }

                        eagleSneaking = shouldEagle
                    } else mc.gameSettings.keyBindSneak.pressed = shouldEagle

                    placedBlocksWithoutEagle = 0
                } else placedBlocksWithoutEagle++
            }

            // Zitter
            if (zitterValue.get() && zitterModeValue.get().equals("teleport", ignoreCase = true)) {
                strafe(zitterSpeed.get())


                val yaw = Math.toRadians(mc.thePlayer!!.rotationYaw + (if (zitterDirection) 90.0 else -90.0))
                mc.thePlayer!!.motionX = mc.thePlayer!!.motionX - sin(yaw) * zitterStrength.get()
                mc.thePlayer!!.motionZ = mc.thePlayer!!.motionZ + cos(yaw) * zitterStrength.get()
                zitterDirection = !zitterDirection
            }
        }
        //Auto Jump thingy
        if (shouldGoDown) launchY = mc.thePlayer!!.posY.toInt() - 1
        else if (!sameYValue.get()) {
            mc.thePlayer!!.jump()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null) return

        val packet = event.packet

        // AutoBlock
        if (classProvider.isCPacketHeldItemChange(packet)) {
            val packetHeldItemChange = packet.asCPacketHeldItemChange()

            slot = packetHeldItemChange.slotId
        }
    }

    @EventTarget
    private fun onStrafe(event: StrafeEvent) {
        if (!rotationStrafeValue.get()) return
        RotationUtils.serverRotation.applyStrafeToPlayer(event)
        event.cancelEvent()
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val eventState = event.eventState

        // Lock Rotation
        if (!rotationModeValue.get()
                .equals("Off", ignoreCase = true) && keepRotationValue.get() && lockRotation != null
        ) setRotation(
            lockRotation!!
        )


        if (shouldPlace() && (facesBlock || rotationModeValue.get()
                .equals("Off", ignoreCase = true)) && placeModeValue.get()
                .equals(eventState.stateName, ignoreCase = true)
        ) place()

        // Update and search for new block
        if (eventState == EventState.PRE) update()

        // Reset placeable delay
        if (targetPlace == null && placeableDelay.get()) delayTimer.reset()
    }

    private fun update() {
        val isHeldItemBlock = mc.thePlayer!!.heldItem != null && classProvider.isItemBlock(
            mc.thePlayer!!.heldItem!!.item
        )
        if (if (!shouldPlace() || !autoBlockValue.get()
                    .equals("Off", ignoreCase = true)
            ) findAutoBlockBlock() == -1 && !isHeldItemBlock else !isHeldItemBlock
        ) return

        findBlock(modeValue.get().equals("expand", ignoreCase = true))
    }

    private fun setRotation(rotation: Rotation, keepRotation: Int) {
        if (silentRotation.get()) {
            RotationUtils.setTargetRotation(rotation, keepRotation)
        } else {
            mc.thePlayer!!.rotationYaw = rotation.yaw
            mc.thePlayer!!.rotationPitch = rotation.pitch
        }
    }

    private fun setRotation(rotation: Rotation) {
        setRotation(rotation, 0)
    }

    /**
     * Search for new target block
     */
    private fun findBlock(expand: Boolean) {
        /*final WBlockPos blockPosition = shouldGoDown ? (mc.getThePlayer().getPosY() == (int) mc.getThePlayer().getPosY() + 0.5D ?
                new WBlockPos(mc.getThePlayer().getPosX(), mc.getThePlayer().getPosY() - 0.6D, mc.getThePlayer().getPosZ())
                : new WBlockPos(mc.getThePlayer().getPosX(), mc.getThePlayer().getPosY() - 0.6, mc.getThePlayer().getPosZ()).down()) :
                (mc.getThePlayer().getPosY() == (int) mc.getThePlayer().getPosY() + 0.5D ? new WBlockPos(mc.getThePlayer())
                        : new WBlockPos(mc.getThePlayer().getPosX(), mc.getThePlayer().getPosY(), mc.getThePlayer().getPosZ()).down());*/

        val blockPosition = if (shouldGoDown) (if (mc.thePlayer!!.posY == mc.thePlayer!!.posY.toInt() + 0.5) WBlockPos(
            mc.thePlayer!!.posX, mc.thePlayer!!.posY - 0.6, mc.thePlayer!!.posZ
        )
        else WBlockPos(
            mc.thePlayer!!.posX,
            mc.thePlayer!!.posY - 0.6,
            mc.thePlayer!!.posZ
        ).down()) else ((if (sameYValue.get() || ((autoJumpValue.get() || (smartSpeedValue.get() && Terra.moduleManager.getModule(
                Speed::class.java
            ).state)) && !(mc.gameSettings.keyBindJump.isKeyDown)) && launchY <= mc.thePlayer!!.posY
        ) (WBlockPos(
            mc.thePlayer!!.posX, (launchY - 1).toDouble(), mc.thePlayer!!.posZ
        )) else (if (mc.thePlayer!!.posY == mc.thePlayer!!.posY.toInt() + 0.5) WBlockPos(
            mc.thePlayer!!
        )
        else WBlockPos(mc.thePlayer!!.posX, mc.thePlayer!!.posY, mc.thePlayer!!.posZ).down())))


        if (!expand && (!isReplaceable(blockPosition) || search(blockPosition, !shouldGoDown))) return

        if (expand) {
            for (i in 0 until expandLengthValue.get()) {
                if (search(
                        blockPosition.add(
                            if (mc.thePlayer!!.horizontalFacing == classProvider.getEnumFacing(EnumFacingType.WEST)) -i else if (mc.thePlayer!!.horizontalFacing == classProvider.getEnumFacing(
                                    EnumFacingType.EAST
                                )
                            ) i else 0,
                            0,
                            if (mc.thePlayer!!.horizontalFacing == classProvider.getEnumFacing(EnumFacingType.NORTH)) -i else if (mc.thePlayer!!.horizontalFacing == classProvider.getEnumFacing(
                                    EnumFacingType.SOUTH
                                )
                            ) i else 0
                        ), false
                    )
                ) return
            }
        } else if (searchValue.get()) {
            for (x in -1..1) for (z in -1..1) if (search(blockPosition.add(x, 0, z), !shouldGoDown)) return
        }
    }

    /**
     * Place target block
     */
    private fun place() {
        if (targetPlace == null) {
            if (placeableDelay.get()) delayTimer.reset()
            return
        }

        if (!delayTimer.hasTimePassed(delay) || (sameYValue.get() && launchY - 1 != targetPlace!!.vec3.yCoord.toInt())) return

        var blockSlot = -1
        var itemStack = mc.thePlayer!!.heldItem

        if (itemStack == null || !classProvider.isItemBlock(itemStack.item) ||
            classProvider.isBlockBush(itemStack.item!!.asItemBlock().block) || mc.thePlayer!!.heldItem!!.stackSize <= 0
        ) {
            if (autoBlockValue.get().equals("Off", ignoreCase = true)) return

            blockSlot = findAutoBlockBlock()

            if (blockSlot == -1) return

            if (autoBlockValue.get()
                    .equals("Pick", ignoreCase = true)
            ) mc.netHandler.addToSendQueue(classProvider.createCPacketHeldItemChange(blockSlot - 36))
            mc.playerController.updateController()


            if (autoBlockValue.get().equals("Spoof", ignoreCase = true)) {
                if (blockSlot - 36 != slot) mc.netHandler.addToSendQueue(
                    classProvider.createCPacketHeldItemChange(
                        blockSlot - 36
                    )
                )
            } else if (autoBlockValue.get().equals("Switch", ignoreCase = true)) {
                mc.thePlayer!!.inventory.currentItem = blockSlot - 36
                mc.playerController.updateController()
            } else {
                mc.thePlayer!!.inventory.currentItem = blockSlot - 36
                mc.playerController.updateController()
            }
            itemStack = mc.thePlayer!!.inventoryContainer.getSlot(blockSlot).stack
        }


        if (mc.playerController.onPlayerRightClick(
                mc.thePlayer!!,
                mc.theWorld!!, itemStack, targetPlace!!.blockPos, targetPlace!!.enumFacing, targetPlace!!.vec3
            )
        ) {
            delayTimer.reset()
            delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())

            if (mc.thePlayer!!.onGround) {
                val modifier = speedModifierValue.get()

                mc.thePlayer!!.motionX = mc.thePlayer!!.motionX * modifier
                mc.thePlayer!!.motionZ = mc.thePlayer!!.motionZ * modifier
            }

            if (swingValue.get()) mc.thePlayer!!.swingItem()
            else mc.netHandler.addToSendQueue(classProvider.createCPacketAnimation())
        }

        /*
        if (!stayAutoBlock.get() && blockSlot >= 0)
            mc.getNetHandler().addToSendQueue(classProvider.createCPacketHeldItemChange(mc.getThePlayer().getInventory().getCurrentItem()));
         */

        // Reset
        this.targetPlace = null
    }

    /**
     * Disable scaffold module
     */
    override fun onDisable() {
        if (mc.thePlayer == null) return

        if (!mc.gameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            mc.gameSettings.keyBindSneak.pressed = false

            if (eagleSneaking) mc.netHandler.addToSendQueue(
                classProvider.createCPacketEntityAction(
                    mc.thePlayer!!, ICPacketEntityAction.WAction.STOP_SNEAKING
                )
            )
        }

        if (!mc.gameSettings.isKeyDown(mc.gameSettings.keyBindRight)) mc.gameSettings.keyBindRight.pressed = false

        if (!mc.gameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) mc.gameSettings.keyBindLeft.pressed = false
        if (autoTimer.get()) {
            val timer: Timer = Terra.moduleManager.getModule(Timer::class.java) as Timer
            timer.state = (false)
        }
        lockRotation = null
        limitedRotation = null
        facesBlock = false
        mc.timer.timerSpeed = 1f
        shouldGoDown = false

        if (slot != mc.thePlayer!!.inventory.currentItem) mc.netHandler.addToSendQueue(
            classProvider.createCPacketHeldItemChange(
                mc.thePlayer!!.inventory.currentItem
            )
        )
    }

    /**
     * Entity movement event
     *
     * @param event
     */
    @EventTarget
    fun onMove(event: MoveEvent) {
        if (!safeWalkValue.get() || shouldGoDown) return

        if (airSafeValue.get() || mc.thePlayer!!.onGround) event.isSafeWalk = true
    }

    /**
     * Scaffold visuals
     *
     * @param event
     */
    @EventTarget
    fun onRender2D(event: Render2DEvent?) {
        if (counterDisplayValue.get()) {
            progress = (System.currentTimeMillis() - lastMS).toFloat() / 100f
            if (progress >= 1) progress = 1f
            val scaledResolution = ScaledResolution(mc2)
            val info = blocksAmount.toString() + " blocks"
            val infoWidth = Fonts.font40.getStringWidth(info)
            val infoWidth2 = Fonts.minecraftFont.getStringWidth(blocksAmount.toString() + "")
            /*GL11.glPushMatrix();

            final BlockOverlay blockOverlay = (BlockOverlay) Terra.moduleManager.getModule(BlockOverlay.class);
            if (blockOverlay.getState() && blockOverlay.getInfoValue().get() && blockOverlay.getCurrentBlock() != null)
                GL11.glTranslatef(0, 15F, 0);

            final String info = "Blocks: §7" + getBlocksAmount();
            final IScaledResolution scaledResolution = classProvider.createScaledResolution(mc);

            RenderUtils.drawBorderedRect((scaledResolution.getScaledWidth() / 2.0f) - 2,
                    (scaledResolution.getScaledHeight() / 2.0f) + 5,
                    (scaledResolution.getScaledWidth() / 2.0f) + Fonts.font40.getStringWidth(info) + 2,
                    (scaledResolution.getScaledHeight() / 2.0f) + 16, 3, Color.BLACK.getRGB(), Color.BLACK.getRGB());

            classProvider.getGlStateManager().resetColor();

            Fonts.font40.drawString(info, scaledResolution.getScaledWidth() / 2.0f,
                    scaledResolution.getScaledHeight() / 2.0f + 7, Color.WHITE.getRGB());

            GL11.glPopMatrix();*/
            GlStateManager.translate(0f, -14f - (progress * 4f), 0f)
            //GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glColor4f(0.15f, 0.15f, 0.15f, progress)
            GL11.glBegin(GL11.GL_TRIANGLE_FAN)
            GL11.glVertex2d(
                (scaledResolution.scaledWidth / 2 - 3).toDouble(),
                (scaledResolution.scaledHeight - 60).toDouble()
            )
            GL11.glVertex2d(
                (scaledResolution.scaledWidth / 2).toDouble(),
                (scaledResolution.scaledHeight - 57).toDouble()
            )
            GL11.glVertex2d(
                (scaledResolution.scaledWidth / 2 + 3).toDouble(),
                (scaledResolution.scaledHeight - 60).toDouble()
            )
            GL11.glEnd()
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            //GL11.glPopMatrix();
            RenderUtils.drawRoundedRect(
                (scaledResolution.scaledWidth / 2 - (infoWidth / 2) - 4).toFloat(),
                (scaledResolution.scaledHeight - 60).toFloat(),
                (scaledResolution.scaledWidth / 2 + (infoWidth / 2) + 4).toFloat(),
                (scaledResolution.scaledHeight - 74).toFloat(),
                2f,
                Color(0.15f, 0.15f, 0.15f, progress).rgb
            )
            GlStateManager.resetColor()
            Fonts.font35.drawCenteredString(
                info,
                scaledResolution.scaledWidth / 2 + 0.1f,
                (scaledResolution.scaledHeight - 70).toFloat(),
                Color(1f, 1f, 1f, 0.8f * progress).rgb,
                false
            )
            GlStateManager.translate(0f, 14f + (progress * 4f), 0f)
        }
    }

    /**
     * Scaffold visuals
     *
     * @param event
     */
    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        if (!markValue.get()) return

        for (i in 0 until (if (modeValue.get()
                .equals("Expand", ignoreCase = true)
        ) expandLengthValue.get() + 1 else 2)) {
            val blockPos = WBlockPos(
                mc.thePlayer!!.posX + (if (mc.thePlayer!!.horizontalFacing == classProvider.getEnumFacing(EnumFacingType.WEST)) -i else if (mc.thePlayer!!.horizontalFacing == classProvider.getEnumFacing(
                        EnumFacingType.EAST
                    )
                ) i else 0),
                mc.thePlayer!!.posY - (if (mc.thePlayer!!.posY == mc.thePlayer!!.posY.toInt() + 0.5) 0.0 else 1.0) - (if (shouldGoDown) 1.0 else 0.0),
                mc.thePlayer!!.posZ + (if (mc.thePlayer!!.horizontalFacing == classProvider.getEnumFacing(EnumFacingType.NORTH)) -i else if (mc.thePlayer!!.horizontalFacing == classProvider.getEnumFacing(
                        EnumFacingType.SOUTH
                    )
                ) i else 0)
            )
            val placeInfo = get(blockPos)

            if (isReplaceable(blockPos) && placeInfo != null) {
                RenderUtils.drawBlockBox(blockPos, Color(68, 117, 255, 100), false)
                break
            }
        }
    }

    /**
     * Search for placeable block
     *
     * @param blockPosition pos
     * @param checks        visible
     * @return
     */
    private fun search(blockPosition: WBlockPos, checks: Boolean): Boolean {
        if (!isReplaceable(blockPosition)) return false
        // StaticModes
        val basicMode = rotationModeValue.get().equals("Basic", ignoreCase = true)
        val staticMode = rotationModeValue.get().equals("Static", ignoreCase = true)
        val staticPitchMode = staticMode || rotationModeValue.get().equals("StaticPitch", ignoreCase = true)
        val staticYawMode = staticMode || rotationModeValue.get().equals("StaticYaw", ignoreCase = true)
        val staticPitch = staticPitchValue.get()
        val staticYawOffset = staticYawOffsetValue.get()

        // SearchRanges
        val xzRV = xzRangeValue.get().toDouble()
        val xzSSV = calcStepSize(xzRV)
        val yRV = yRangeValue.get().toDouble()
        val ySSV = calcStepSize(yRV)

        var xSearchFace = 0.0
        var ySearchFace = 0.0
        var zSearchFace = 0.0


        val eyesPos = WVec3(
            mc.thePlayer!!.posX,
            mc.thePlayer!!.entityBoundingBox.minY + mc.thePlayer!!.eyeHeight,
            mc.thePlayer!!.posZ
        )

        var placeRotation: PlaceRotation? = null

        for (facingType in EnumFacingType.values()) {
            val side = classProvider.getEnumFacing(facingType)
            val neighbor = blockPosition.offset(side)

            if (!canBeClicked(neighbor)) continue

            val dirVec = WVec3(side.directionVec)

            var xSearch = 0.5 - (xzRV / 2)
            while (xSearch <= 0.5 + (xzRV / 2)) {
                var ySearch = 0.5 - (yRV / 2)
                while (ySearch <= 0.5 + (yRV / 2)) {
                    var zSearch = 0.5 - (xzRV / 2)
                    while (zSearch <= 0.5 + (xzRV / 2)) {
                        val posVec = WVec3(blockPosition).addVector(xSearch, ySearch, zSearch)
                        val distanceSqPosVec = eyesPos.squareDistanceTo(posVec)
                        val hitVec = posVec.add(WVec3(dirVec.xCoord * 0.5, dirVec.yCoord * 0.5, dirVec.zCoord * 0.5))

                        if (checks && (eyesPos.squareDistanceTo(hitVec) > 18.0 || distanceSqPosVec > eyesPos.squareDistanceTo(
                                posVec.add(dirVec)
                            ) || mc.theWorld!!.rayTraceBlocks(
                                eyesPos,
                                hitVec,
                                false,
                                true,
                                false
                            ) != null)
                        ) {
                            zSearch += xzSSV
                            continue
                        }

                        // face block
                        for (i in 0 until (if (staticYawMode) 2 else 1)) {
                            val diffX = if (staticYawMode && i == 0) 0.0 else hitVec.xCoord - eyesPos.xCoord
                            val diffY = hitVec.yCoord - eyesPos.yCoord
                            val diffZ = if (staticYawMode && i == 1) 0.0 else hitVec.zCoord - eyesPos.zCoord

                            val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)

                            //                            final float pitch = staticPitchMode ? staticPitch : WMathHelper.wrapAngleTo180_float((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)));
                            val facePos = getRotations(BlockPos(neighbor.x, neighbor.y, neighbor.z), side.unwrap())
                            var pitch = if (staticPitchMode) {
                                staticPitch
                            } else if (basicMode) {
                                facePos[1]
                            } else {
                                wrapAngleTo180_float(-Math.toDegrees(atan2(diffY, diffXZ)).toFloat())
                            }
                            var yaw: Float
                            val degrees = Math.toDegrees(atan2(diffZ, diffX))
                            yaw = if (staticYawMode) {
                                wrapAngleTo180_float(
                                    degrees.toFloat() - 90f +
                                            staticYawOffset
                                )
                            } else if (basicMode) {
                                facePos[0]
                            } else {
                                wrapAngleTo180_float(degrees.toFloat() - 90f)
                            }
                            val rotation = Rotation(
                                yaw, pitch
                            )

                            val rotationVector = RotationUtils.getVectorForRotation(rotation)
                            val vector = eyesPos.addVector(
                                rotationVector.xCoord * 4,
                                rotationVector.yCoord * 4,
                                rotationVector.zCoord * 4
                            )
                            val obj = mc.theWorld!!.rayTraceBlocks(eyesPos, vector, false, false, true)

                            if (obj!!.typeOfHit != IMovingObjectPosition.WMovingObjectType.BLOCK || !obj.blockPos!!.equals(
                                    neighbor
                                )
                            ) continue

                            if (placeRotation == null || RotationUtils.getRotationDifference(rotation) < RotationUtils.getRotationDifference(
                                    placeRotation.rotation
                                )
                            ) {
                                placeRotation = PlaceRotation(PlaceInfo(neighbor, side.opposite, hitVec), rotation)
                            }
                            xSearchFace = xSearch
                            ySearchFace = ySearch
                            zSearchFace = zSearch
                        }
                        zSearch += xzSSV
                    }
                    ySearch += ySSV
                }
                xSearch += xzSSV
            }
        }

        if (placeRotation == null || !shouldPlace()) return false

        if (!rotationModeValue.get().equals("Off", ignoreCase = true)) {
            if (minTurnSpeedValue.get() < 180) {
                limitedRotation = RotationUtils.limitAngleChange(
                    RotationUtils.serverRotation, placeRotation.rotation,
                    (Math.random() * (maxTurnSpeedValue.get() - minTurnSpeedValue.get()) + minTurnSpeedValue.get()).toFloat()
                )
                setRotation(limitedRotation!!, keepLengthValue.get())
                lockRotation = limitedRotation

                facesBlock = false
                for (facingType in EnumFacingType.values()) {
                    val side = classProvider.getEnumFacing(facingType)
                    val neighbor = blockPosition.offset(side)

                    if (!canBeClicked(neighbor)) continue

                    val dirVec = WVec3(side.directionVec)

                    val posVec = WVec3(blockPosition).addVector(xSearchFace, ySearchFace, zSearchFace)
                    val distanceSqPosVec = eyesPos.squareDistanceTo(posVec)
                    val hitVec = posVec.add(WVec3(dirVec.xCoord * 0.5, dirVec.yCoord * 0.5, dirVec.zCoord * 0.5))

                    if (checks && (eyesPos.squareDistanceTo(hitVec) > 18.0 || distanceSqPosVec > eyesPos.squareDistanceTo(
                            posVec.add(dirVec)
                        ) || mc.theWorld!!.rayTraceBlocks(
                            eyesPos,
                            hitVec,
                            false,
                            true,
                            false
                        ) != null)
                    ) continue

                    val rotationVector = RotationUtils.getVectorForRotation(limitedRotation)
                    val vector = eyesPos.addVector(
                        rotationVector.xCoord * 4,
                        rotationVector.yCoord * 4,
                        rotationVector.zCoord * 4
                    )
                    val obj = mc.theWorld!!.rayTraceBlocks(eyesPos, vector, false, false, true)

                    if (!(obj!!.typeOfHit == IMovingObjectPosition.WMovingObjectType.BLOCK && obj.blockPos!!.equals(
                            neighbor
                        ))
                    ) continue
                    facesBlock = true
                    break
                }
            } else {
                lockRotation = placeRotation.rotation
                setRotation(lockRotation!!, keepLengthValue.get())
                facesBlock = true
            }
        }
        targetPlace = placeRotation.placeInfo
        return true
    }

    private fun calcStepSize(range: Double): Double {
        var accuracy = searchAccuracyValue.get().toDouble()
        accuracy += accuracy % 2 // If it is set to uneven it changes it to even. Fixes a bug
        if (range / accuracy < 0.01) return 0.01
        return range / accuracy
    }

    private val blocksAmount: Int
        /**
         * @return hotbar blocks amount
         */
        get() {
            var amount = 0

            for (i in 36..44) {
                val itemStack = mc.thePlayer!!.inventoryContainer.getSlot(i).stack

                if (itemStack != null && classProvider.isItemBlock(itemStack.item)) {
                    val block = itemStack.item!!.asItemBlock().block

                    val heldItem = mc.thePlayer!!.heldItem

                    if (heldItem != null && heldItem == itemStack || !InventoryUtils.BLOCK_BLACKLIST.contains(block) && !classProvider.isBlockBush(
                            block
                        )
                    ) amount += itemStack.stackSize
                }
            }

            return amount
        }

    override val tag: String
        get() = modeValue.get()

    val bestBlocks: Unit
        get() {
            if (blocksAmount == 0) return
            if (picker.get()) {
                val bestInvSlot = biggestBlockSlotInv
                val bestHotbarSlot = biggestBlockSlotHotbar
                var bestSlot = if (biggestBlockSlotHotbar > 0) biggestBlockSlotHotbar else biggestBlockSlotInv
                var spoofSlot = 42
                if (bestHotbarSlot > 0 && bestInvSlot > 0) {
                    if (mc.thePlayer!!.inventoryContainer.getSlot(bestInvSlot).hasStack && mc.thePlayer!!.inventoryContainer.getSlot(
                            bestHotbarSlot
                        ).hasStack
                    ) {
                        if (mc.thePlayer!!.inventoryContainer.getSlot(bestHotbarSlot).stack!!.stackSize < mc.thePlayer!!.inventoryContainer.getSlot(
                                bestInvSlot
                            ).stack!!.stackSize
                        ) {
                            bestSlot = bestInvSlot
                        }
                    }
                }
                if (hotbarContainBlock()) {
                    for (a in 36..44) {
                        if (mc.thePlayer!!.inventoryContainer.getSlot(a).hasStack) {
                            val item = mc.thePlayer!!.inventoryContainer.getSlot(a).stack!!.item
                            if (item is IItemBlock) {
                                spoofSlot = a
                                break
                            }
                        }
                    }
                } else {
                    for (a in 36..44) {
                        if (!mc.thePlayer!!.inventoryContainer.getSlot(a).hasStack) {
                            spoofSlot = a
                            break
                        }
                    }
                }

                if (mc.thePlayer!!.inventoryContainer.getSlot(spoofSlot).slotNumber != bestSlot) {
                    swap(bestSlot, spoofSlot - 36)
                    mc.playerController.updateController()
                }
            } else {
                if (invCheck()) {
                    val `is`: IItemStack = functions.getItemById(261)!!.getDefaultInstance()
                    for (i in 9..35) {
                        if (mc.thePlayer!!.inventoryContainer.getSlot(i).hasStack) {
                            val item = mc.thePlayer!!.inventoryContainer.getSlot(i).stack!!.item
                            var count = 0
                            if (item is IItemBlock) {
                                for (a in 36..44) {
                                    if (functions.canAddItemToSlot(
                                            mc.thePlayer!!.inventoryContainer.getSlot(a),
                                            `is`,
                                            true
                                        )
                                    ) {
                                        swap(i, a - 36)
                                        count++
                                        break
                                    }
                                }

                                if (count == 0) {
                                    swap(i, 7)
                                }
                                break
                            }
                        }
                    }
                }
            }
        }

    private fun hotbarContainBlock(): Boolean {
        var i = 36

        while (i < 45) {
            try {
                val stack = mc.thePlayer!!.inventoryContainer.getSlot(i).stack
                if ((stack == null) || (stack.item == null) || stack.item !is IItemBlock) {
                    i++
                    continue
                }
                return true
            } catch (e: Exception) {
            }
        }

        return false
    }

    val biggestBlockSlotHotbar: Int
        get() {
            var slot = -1
            var size = 0
            if (blocksAmount == 0) return -1
            for (i in 36..44) {
                if (mc.thePlayer!!.inventoryContainer.getSlot(i).hasStack) {
                    val item = mc.thePlayer!!.inventoryContainer.getSlot(i).stack!!.item
                    val `is` = mc.thePlayer!!.inventoryContainer.getSlot(i).stack
                    if (item is IItemBlock) {
                        if (`is`!!.stackSize > size) {
                            size = `is`.stackSize
                            slot = i
                        }
                    }
                }
            }
            return slot
        }

    protected fun swap(slot: Int, hotbarNum: Int) {
        mc.playerController.windowClick(
            mc.thePlayer!!.inventoryContainer.windowId, slot, hotbarNum, 2,
            mc.thePlayer!!
        )
    }

    private fun invCheck(): Boolean {
        for (i in 36..44) {
            if (mc.thePlayer!!.inventoryContainer.getSlot(i).hasStack) {
                val item = mc.thePlayer!!.inventoryContainer.getSlot(i).stack!!.item
                if (item is IItemBlock) {
                    return false
                }
            }
        }
        return true
    }

    val biggestBlockSlotInv: Int
        get() {
            var slot = -1
            var size = 0
            if (blocksAmount == 0) return -1
            for (i in 9..35) {
                if (mc.thePlayer!!.inventoryContainer.getSlot(i).hasStack) {
                    val item = mc.thePlayer!!.inventoryContainer.getSlot(i).stack!!.item
                    val `is` = mc.thePlayer!!.inventoryContainer.getSlot(i).stack
                    if (item is IItemBlock) {
                        if (`is`!!.stackSize > size) {
                            size = `is`.stackSize
                            slot = i
                        }
                    }
                }
            }
            return slot
        }

    companion object {
        fun getRotations(block: BlockPos, face: EnumFacing): FloatArray {
            val x = block.x + 0.5 - mc2.player.posX + face.frontOffsetX.toDouble() / 2
            val z = block.z + 0.5 - mc2.player.posZ + face.frontOffsetZ.toDouble() / 2
            val y = (block.y + 0.5)
            val d1 = mc2.player.posY + mc2.player.getEyeHeight() - y
            val d3 = sqrt(x * x + z * z)
            var yaw = (atan2(z, x) * 180.0 / Math.PI).toFloat() - 82.0f
            val pitch = (atan2(d1, d3) * 180.0 / Math.PI).toFloat()
            if (yaw < 0.0f) {
                yaw += 360f
            }
            return floatArrayOf(yaw, pitch)
        }
    }
}