package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.api.enums.EnumFacingType
import net.ccbluex.liquidbounce.api.minecraft.util.IMovingObjectPosition
import net.ccbluex.liquidbounce.api.minecraft.util.WBlockPos
import net.ccbluex.liquidbounce.api.minecraft.util.WMathHelper.wrapAngleTo180_float
import net.ccbluex.liquidbounce.api.minecraft.util.WVec3
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.block.BlockUtils.canBeClicked
import net.ccbluex.liquidbounce.utils.block.BlockUtils.isReplaceable
import net.ccbluex.liquidbounce.utils.block.PlaceInfo
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.BlockBush
import net.minecraft.client.settings.GameSettings
import net.minecraft.item.ItemBlock
import net.minecraft.util.EnumFacing
import java.awt.Color
import kotlin.math.*

@ModuleInfo(name = "Scaffold", description = "New", category = ModuleCategory.WORLD)
class Scaffold : Module() {
    /**
     * 选项
     */
    //模式
    val modeValue = ListValue("Mode", arrayOf("Normal", "Expand"),"Normal")

    //放置延迟
    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 0, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val minDelay = minDelayValue.get()
            if (minDelay > newValue) {
                set(minDelay)
            }
        }
    }
    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 0, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val maxDelay = maxDelayValue.get()
            if (maxDelay < newValue) {
                set(maxDelay)
            }
        }
    }
    private val placeDelay = BoolValue("PlaceDelay", false)
    private val fallDownDelay = IntegerValue("FallDownDelay", 0, 0, 1000)

    //获取方块模式
    private val autoBlockMode = ListValue("AutoBlock", arrayOf("Switch", "Spoof", "None"),"Switch")

    //疾跑
    @JvmField
    val sprintValue = BoolValue("Sprint",true)
    private val sprintModeValue = ListValue("SprintMode", arrayOf("Always", "Ground", "Air"),"Always").displayable { sprintValue.get() }

    //移动
    private val strafeValue = BoolValue("Strafe",false)

    //放置方块
    private val placeModeValue = ListValue("PlaceMode", arrayOf("Post", "Pre"),"Post")
    private val placeConditionValue = ListValue("PlaceCondition", arrayOf("Always", "DelayAir", "FallDown"), "DelayAir")
    private val rotConditionValue = ListValue("RotCondition", arrayOf("Always", "DelayAir", "FallDown"), "DelayAir")
    private val airTicks = IntegerValue("PlaceAirTime",0,0,10)
    private val rotAirTicks = IntegerValue("RotAirTime",0,0,10)
    private var airtime = 0
    private var f = false
    private var n = false

    private val swingValue = BoolValue("Swing", true)
    private val searchValue = BoolValue("Search", true)
    private val downValue = BoolValue("Down", true)

    //转头
    private val rotationValue = BoolValue("Rotation",true)
    private val silentRotationValue = BoolValue("SilentRotation", true)
    private val keepRotationValue = BoolValue("KeepRotation", true)
    private val keepRotationLengthValue = IntegerValue("KeepRotationLength", 20, 0, 20)
    //转头速度
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

    // XZ/Y range
    private val searchMode = ListValue("XYZSearch", arrayOf("Auto", "AutoCenter", "Manual"), "AutoCenter")
    private val xzRangeValue = FloatValue("xzRange", 0f, 0f, 1f)
    private var yRangeValue = FloatValue("yRange", 0f, 0f, 1f)
    private val minDistValue = FloatValue("MinDist", 0.0f, 0.0f, 0.2f)
    // Search Accuracy
    private val searchAccuracyValue: IntegerValue = object : IntegerValue("SearchAccuracy", 6, 1, 16) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            if (maximum < newValue) {
                set(maximum)
            } else if (minimum > newValue) {
                set(minimum)
            }
        }
    }

    //其他
    private val expandLengthValue = IntegerValue("ExpandLength", 5, 1, 6).displayable { modeValue.get().equals("Expand",true) }
    private val omniDirectionalExpand = BoolValue("OmniDirectionalExpand", false).displayable { modeValue.get().equals("Expand",true) }
    private val timerValue = FloatValue("Timer", 1f, 0.1f, 10f)
    private val speedModifierValue = FloatValue("SpeedModifier", 1f, 0f, 2f)
    private val parkourValue = BoolValue("Parkour",false)
    private val sameYValue = BoolValue("SameY", false)
    private val safeWalkValue = BoolValue("SafeWalk", false)

    //视觉类
    private val counterDisplayValue = BoolValue("Counter",true)
    private val markValue = BoolValue("Mark", true)
    private val redValue = IntegerValue("Red", 0, 0, 255).displayable { markValue.get() }
    private val greenValue = IntegerValue("Green", 120, 0, 255).displayable { markValue.get() }
    private val blueValue = IntegerValue("Blue", 255, 0, 255).displayable { markValue.get() }
    private val alphaValue = IntegerValue("Alpha", 45, 0, 255).displayable { markValue.get() }

    /**
     * 属性
     */
    // Auto block slot
    private var slot = 0
    private val lastSlot = 0

    // Delay
    private val delayTimer = MSTimer()
    private var delay = 0L

    // Target block
    private var targetPlace: PlaceInfo? = null

    // Launch position
    private var launchY = 0
    private var facesBlock = false

    // Downwards
    private var shouldGoDown = false

    //Rotation
    private var canRot = false
    private var canPlace = false

    // Rotation lock
    private var lockRotation: Rotation? = null
    private var lockRotationTimer = TickTimer()

    /**
     * 方法
     */
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.thePlayer ?: return

        if (sprintValue.get()) {
            if ((sprintModeValue.get().equals("Ground",true) && !mc.thePlayer!!.onGround) || (sprintModeValue.get().equals("Air",true) && mc.thePlayer!!.onGround)) {
                mc.thePlayer!!.sprinting = false
            } else {
                mc.thePlayer!!.sprinting = true
            }
        }

        if (parkourValue.get()) {
            if (MovementUtils.isMoving && player.onGround
                && !player.sneaking && !mc.gameSettings.keyBindSneak.isKeyDown && !mc.gameSettings.keyBindJump.isKeyDown
                && mc.theWorld!!.getCollidingBoundingBoxes(player, player.entityBoundingBox.offset(0.0, -0.5, 0.0).expand(-0.001, 0.0, -0.001)).isEmpty()) {
                player.jump()
            }
        }

        if (!player.onGround) {
            airtime++
        } else {
            if (placeConditionValue.get().equals("FallDown", ignoreCase = true) || (placeConditionValue.get().equals("DelayAir", ignoreCase = true))) {
                delay = 0L
                delayTimer.reset()
                shouldGoDown = false
                canPlace = false
                canRot = false
                f = false
                launchY = player.posY.roundToInt()
                slot = player.inventory.currentItem
                facesBlock = false
            }
            airtime = 0
        }
        f = airtime > airTicks.get()
        n = airtime > rotAirTicks.get()
        mc.timer.timerSpeed = timerValue.get()
        canPlace = (((placeConditionValue.get().equals("FallDown", ignoreCase = true)) && mc.thePlayer!!.fallDistance > 0)
                || (placeConditionValue.get().equals("Always", ignoreCase = true))
                || (placeConditionValue.get().equals("DelayAir", ignoreCase = true) && !mc.thePlayer!!.onGround && f))
        canRot = (((rotConditionValue.get().equals("FallDown", ignoreCase = true)) && mc.thePlayer!!.fallDistance > 0)
                || (rotConditionValue.get().equals("Always", ignoreCase = true))
                || (rotConditionValue.get().equals("DelayAir", ignoreCase = true) && !mc.thePlayer!!.onGround && n))

        shouldGoDown = downValue.get() && !sameYValue.get() && GameSettings.isKeyDown(mc2.gameSettings.keyBindSneak) && getBlocksAmount() > 1
        if (shouldGoDown) {
            mc.gameSettings.keyBindSneak.pressed = false
        }
    }

    //Strafe
    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (!strafeValue.get()) return
        if(!canRot){ return }
        update()
        val rotation = lockRotation ?: return

        if (rotationValue.get() && (keepRotationValue.get() || !lockRotationTimer.hasTimePassed(keepRotationLengthValue.get()))) {
            if (targetPlace == null) {
                rotation.yaw = wrapAngleTo180_float((rotation.yaw / 45f).roundToInt() * 45f)
            }
            setRotation(rotation)
            lockRotationTimer.update()

            rotation.applyStrafeToPlayer(event)
            event.cancelEvent()
            return
        }

        val targetRotation = RotationUtils.targetRotation ?: return
        targetRotation.applyStrafeToPlayer(event)
        event.cancelEvent()
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val eventState = event.eventState
        if(!canRot){ return }
        // Lock Rotation
        if (rotationValue.get() && (keepRotationValue.get() || !lockRotationTimer.hasTimePassed(keepRotationLengthValue.get())) && lockRotation != null && !strafeValue.get()) {
            setRotation(lockRotation!!)
            if (eventState == EventState.POST) {
                lockRotationTimer.update()
            }
        }

        // Face block
        if ((facesBlock || !rotationValue.get()) && placeModeValue.get().equals(eventState.stateName, true)) {
            if(!canPlace) return
            doPlace()
        }
        // Update and search for a new block
        if (eventState == EventState.PRE && !strafeValue.get()) {
            update()
        }

        // Reset placeable delay
        if (targetPlace == null && placeDelay.get()) {
            delayTimer.reset()
        }
    }

    // Entity movement event
    @EventTarget
    fun onMove(event: MoveEvent) {
        val player = mc.thePlayer ?: return

        if (!safeWalkValue.get() || shouldGoDown) {
            return
        }
        if (player.onGround) event.isSafeWalk = true
    }


    /**
     * //设置转头方法
     */
    @EventTarget
    private fun setRotation(rotation: Rotation) {
        if(!canRot){ return }
        val player = mc.thePlayer ?: return

        if (silentRotationValue.get()) {
            RotationUtils.setTargetRotation(rotation, 0)
        } else {
            player.rotationYaw = rotation.yaw
            player.rotationPitch = rotation.pitch
        }
    }

    /**
     * 渲染方块数量面板
     */
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val scaledResolution = classProvider.createScaledResolution(mc)
        if (counterDisplayValue.get()) {
            Fonts.minecraftFont.drawString(getBlocksAmount().toString() + " Blocks",
                (scaledResolution.scaledWidth / 2).toFloat(),
                (scaledResolution.scaledHeight / 2 + 20).toFloat(),
                -1,
                true
            )
        }
    }

    /**
     * 渲染方块标记
     */
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val player = mc.thePlayer ?: return
        if (!markValue.get()) {
            return
        }
        for (i in 0 until if (modeValue.get().equals("Expand", true)) expandLengthValue.get() + 1 else 2) {
            val yaw = Math.toRadians(player.rotationYaw.toDouble())
            val x = if (omniDirectionalExpand.get()) -sin(yaw).roundToInt() else player.horizontalFacing.directionVec.x
            val z = if (omniDirectionalExpand.get()) cos(yaw).roundToInt() else player.horizontalFacing.directionVec.z
            val blockPos = WBlockPos(
                player.posX + x * i,
                if (sameYValue.get() && launchY <= player.posY) launchY - 1.0 else player.posY - (if (player.posY == player.posY + 0.5) 0.0 else 1.0) - if (shouldGoDown) 1.0 else 0.0,
                player.posZ + z * i
            )
            val placeInfo = PlaceInfo.get(blockPos)
            if (isReplaceable(blockPos) && placeInfo != null) {
                RenderUtils.drawBlockBox(blockPos, Color(redValue.get(), greenValue.get(), blueValue.get(), alphaValue.get()), false)
                break
            }
        }
    }

    /**
     * 寻找可放置方块
     */
    private fun search(blockPosition: WBlockPos, raycast: Boolean): Boolean {
        facesBlock = false
        val player = mc.thePlayer ?: return false
        val world = mc.theWorld ?: return false

        if (!isReplaceable(blockPosition)) {
            return false
        }

        // Search Ranges
        val xzRV = xzRangeValue.get().toDouble()
        val xzSSV = calcStepSize(xzRV.toFloat())
        val yRV = yRangeValue.get().toDouble()
        val ySSV = calcStepSize(yRV.toFloat())
        val eyesPos = WVec3(player.posX, player.entityBoundingBox.minY + player.eyeHeight, player.posZ)
        var placeRotation: PlaceRotation? = null
        for (facingType in EnumFacingType.values()) {
            val side = classProvider.getEnumFacing(facingType)
            val neighbor = blockPosition.offset(side)
            if (!canBeClicked(neighbor)) {
                continue
            }
            val dirVec = WVec3(side.directionVec)
            val auto = searchMode.get().equals("Auto", true)
            val center = searchMode.get().equals("AutoCenter", true)
            var xSearch = if (auto) 0.1 else 0.5 - xzRV / 2
            while (xSearch <= if (auto) 0.9 else 0.5 + xzRV / 2) {
                var ySearch = if (auto) 0.1 else 0.5 - yRV / 2
                while (ySearch <= if (auto) 0.9 else 0.5 + yRV / 2) {
                    var zSearch = if (auto) 0.1 else 0.5 - xzRV / 2
                    while (zSearch <= if (auto) 0.9 else 0.5 + xzRV / 2) {
                        val posVec = WVec3(blockPosition).addVector(
                            if (center) 0.5 else xSearch, if (center) 0.5 else ySearch, if (center) 0.5 else zSearch
                        )
                        val distanceSqPosVec = eyesPos.squareDistanceTo(posVec)
                        val hitVec = posVec.add(WVec3(dirVec.xCoord * 0.5, dirVec.yCoord * 0.5, dirVec.zCoord * 0.5))
                        if (raycast && (eyesPos.distanceTo(hitVec) > 4.25 || distanceSqPosVec > eyesPos.squareDistanceTo(
                                posVec.add(dirVec)
                            ) || world.rayTraceBlocks(
                                eyesPos,
                                hitVec,
                                false,
                                true,
                                false
                            ) != null)
                        ) {
                            zSearch += if (auto) 0.1 else xzSSV
                            continue
                        }

                        // Face block
                        val diffX = hitVec.xCoord - eyesPos.xCoord
                        val diffY = hitVec.yCoord - eyesPos.yCoord
                        val diffZ = hitVec.zCoord - eyesPos.zCoord
                        val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)
                        if (facingType != EnumFacing.UP && facingType != EnumFacing.DOWN) {
                            val diff = abs(if (facingType == EnumFacing.NORTH || facingType == EnumFacing.SOUTH) diffZ else diffX)
                            if (diff < minDistValue.get()) {
                                zSearch += if (auto) 0.1 else xzSSV
                                continue
                            }
                        }
                        val rotation = Rotation(
                            wrapAngleTo180_float(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f),
                            wrapAngleTo180_float(-Math.toDegrees(atan2(diffY, diffXZ)).toFloat())
                        )
                        val rotationVector = RotationUtils.getVectorForRotation(rotation)
                        val vector = eyesPos.addVector(
                            rotationVector.xCoord * 4.25, rotationVector.yCoord * 4.25, rotationVector.zCoord * 4.25
                        )

                        val obj = world.rayTraceBlocks(
                            eyesPos,
                            vector,
                            false,
                            false,
                            true
                        ) ?: continue

                        if (obj.typeOfHit != IMovingObjectPosition.WMovingObjectType.BLOCK || obj.blockPos != neighbor) {
                            zSearch += if (auto) 0.1 else xzSSV
                            continue
                        }
                        if (placeRotation == null || RotationUtils.getRotationDifference(rotation) < RotationUtils.getRotationDifference(
                                placeRotation.rotation
                            )
                        ) {
                            placeRotation = PlaceRotation(PlaceInfo(neighbor, side.opposite, hitVec), rotation)
                        }

                        zSearch += if (auto) 0.1 else xzSSV
                    }
                    ySearch += if (auto) 0.1 else ySSV
                }
                xSearch += if (auto) 0.1 else xzSSV
            }
        }
        if (placeRotation == null) {
            return false
        }
        if (rotationValue.get() && canRot) {
            if (minTurnSpeedValue.get() < 180) {
                val limitedRotation = RotationUtils.limitAngleChange(
                    RotationUtils.serverRotation,
                    placeRotation.rotation,
                    (Math.random() * (maxTurnSpeedValue.get() - minTurnSpeedValue.get()) + minTurnSpeedValue.get()).toFloat()
                )

                if ((10 * wrapAngleTo180_float(limitedRotation.yaw)).roundToInt() == (10 * wrapAngleTo180_float(
                        placeRotation.rotation.yaw
                    )).roundToInt() && (10 * wrapAngleTo180_float(limitedRotation.pitch)).roundToInt() == (10 * wrapAngleTo180_float(
                        placeRotation.rotation.pitch
                    )).roundToInt()
                ) {
                    setRotation(placeRotation.rotation)
                    lockRotation = placeRotation.rotation
                    facesBlock = true
                } else {
                    setRotation(limitedRotation)
                    lockRotation = limitedRotation
                    facesBlock = false
                }
            } else {
                setRotation(placeRotation.rotation)
                lockRotation = placeRotation.rotation
                facesBlock = true
            }
            lockRotationTimer.reset()
        }
        targetPlace = placeRotation.placeInfo
        return true
    }

    /**
     * 放置方块方法
     */
    fun doPlace() {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return
        if (targetPlace == null) {
            if (placeDelay.get()) {
                delayTimer.reset()
            }
            return
        }

        if (!delayTimer.hasTimePassed(delay) || sameYValue.get() && launchY - 1 != targetPlace!!.vec3.yCoord.toInt()) {
            return
        }

        val itemStack = player.heldItem
        if (itemStack == null || itemStack.item !is ItemBlock || (itemStack.item!! as ItemBlock).block is BlockBush || player.heldItem!!.stackSize <= 0) {
            val blockSlot = InventoryUtils.findAutoBlockBlock()
            if (blockSlot == -1) return

            when (autoBlockMode.get().toLowerCase()) {
                "switch" -> {
                    mc.thePlayer!!.inventory.currentItem = blockSlot - 36
                    mc.playerController.updateController()
                }
                "spoof" -> {
                    mc.netHandler.addToSendQueue(classProvider.createCPacketHeldItemChange(blockSlot - 36))
                    mc.thePlayer!!.inventoryContainer.getSlot(blockSlot).stack
                }
                "none" -> return
            }
        }

        if (mc.playerController.onPlayerRightClick(player, world, itemStack, targetPlace!!.blockPos, targetPlace!!.enumFacing, targetPlace!!.vec3)) {
            delayTimer.reset()
            delay = if (!placeDelay.get()) {
                0
            } else {
                if(mc.thePlayer!!.fallDistance > 0){
                    fallDownDelay.get().toLong()
                } else {
                    TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
                }
            }


            if (player.onGround) {
                val modifier = speedModifierValue.get()
                player.motionX = player.motionX * modifier
                player.motionZ = player.motionZ * modifier
            }

            if (swingValue.get()) {
                player.swingItem()
            } else {
                mc.netHandler.addToSendQueue(classProvider.createCPacketAnimation())
            }
        }
    }

    /**
     * 寻找方块
     */
    private fun findBlock(expand: Boolean) {
        val player = mc.thePlayer ?: return
        if(!canRot) return
        val blockPosition = if (shouldGoDown) {
            (if (player.posY == player.posY.roundToInt() + 0.5) {
                WBlockPos(player.posX, player.posY - 0.6, player.posZ)
            } else {
                WBlockPos(player.posX, player.posY - 0.6, player.posZ).down()
            })
        } else (if (sameYValue.get() && launchY <= player.posY) {
            WBlockPos(player.posX, launchY - 1.0, player.posZ)
        } else (if (player.posY == player.posY.roundToInt() + 0.5) {
            WBlockPos(player)
        } else {
            WBlockPos(player.posX, player.posY, player.posZ).down()
        }))
        if (!expand && (!isReplaceable(blockPosition) || search(blockPosition, !shouldGoDown))) {
            return
        }

        if (expand) {
            val yaw = Math.toRadians(player.rotationYaw.toDouble() + 180)
            val x = if (omniDirectionalExpand.get()) -sin(yaw).roundToInt() else player.horizontalFacing.directionVec.x
            val z = if (omniDirectionalExpand.get()) cos(yaw).roundToInt() else player.horizontalFacing.directionVec.z
            for (i in 0 until expandLengthValue.get()) {
                if (search(blockPosition.add(x * i, 0, z * i), false)) {
                    return
                }
            }
        } else if (searchValue.get()) {
            for (x in -1..1) {
                for (z in -1..1) {
                    if (search(blockPosition.add(x, 0, z), !shouldGoDown)) {
                        return
                    }
                }
            }
        }
    }

    fun update() {
        if (!canRot) return
        val player = mc.thePlayer ?: return

        val holdingItem = player.heldItem != null && player.heldItem!!.item is ItemBlock
        if (if (!autoBlockMode.get().equals("None", true)) InventoryUtils.findAutoBlockBlock() == -1 && !holdingItem else !holdingItem) return

        findBlock(modeValue.get().equals("Expand", true))
    }

    /**
     * 获取方块数量
     */
    private fun getBlocksAmount(): Int {
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

    private fun calcStepSize(range: Float): Double {
        var accuracy = searchAccuracyValue.get().toDouble()
        accuracy += accuracy % 2 // If it is set to uneven it changes it to even. Fixes a bug
        return if (range / accuracy < 0.01) 0.01 else (range / accuracy)
    }

    override fun onEnable() {
        val player = mc.thePlayer ?: return
        canPlace = false
        canRot = false
        f = false
        airtime = 0
        launchY = player.posY.roundToInt()
        slot = player.inventory.currentItem
        facesBlock = false
    }

    override fun onDisable() {
        val player = mc.thePlayer ?: return
        if (lastSlot != mc.thePlayer!!.inventory.currentItem && autoBlockMode.get().equals("Switch", ignoreCase = true)) {
            mc.thePlayer!!.inventory.currentItem = lastSlot
            mc.playerController.updateController()
        }
        lockRotation = null
        facesBlock = false
        mc.timer.timerSpeed = 1f
        shouldGoDown = false
        if (slot != player.inventory.currentItem) {
            mc.netHandler.addToSendQueue(classProvider.createCPacketHeldItemChange(player.inventory.currentItem))
        }
    }

    override val tag: String
        get() = modeValue.get()

}