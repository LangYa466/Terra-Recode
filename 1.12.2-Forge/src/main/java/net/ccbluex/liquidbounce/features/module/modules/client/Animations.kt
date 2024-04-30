/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(name = "Animations", description = "Item & Client's Animations.", category = ModuleCategory.CLIENT, canEnable = false)
object Animations : Module() {
    private val modeValue = ListValue("Mode", arrayOf("1.7", "1.8", "Old", "New1.8", "Push", "WindMill", "Flux", "ETB", "SigmaOld", "Zoom"), "1.8")
    @JvmField
    val noBlockDestroyParticles = BoolValue("NoBlockDestroyParticles", false)
    private val tabShowPlayerSkinValue = BoolValue("TabShowPlayerSkin", true)
    val blockValue = BoolValue("Blocking", true)
    val SPValue = BoolValue("Progress", false)

    val SpeedSwing = IntegerValue("Swing-Speed", 1, 0, 12)
    private val RotateItemsValue = BoolValue("Rotate-Items", false)
    val RotateItemsWhenEatingOrDrinkingValue = BoolValue("Rotate-Items-When-Eating-or-Drinking", false).displayable { RotateItemsValue.get() }
    val RotateItemWhenBlockingValue = BoolValue("Rotate-Items-When-Blocking", false).displayable { RotateItemsValue.get() }
    val transformFirstPersonRotate = ListValue("RotateMode", arrayOf("RotateY", "RotateXY", "Custom", "None"), "RotateY").displayable { RotateItemsValue.get() }
    val SpeedRotate = IntegerValue("Rotate-Speed", 1, 0, 10).displayable { RotateItemsValue.get() }
    val RotateX = FloatValue("RotateXAxis", 0f, -180f, 180f).displayable { RotateItemsValue.get() && transformFirstPersonRotate.get().equals("custom", ignoreCase = true) }
    val RotateY = FloatValue("RotateYAxis", 0f, -180f, 180f).displayable { RotateItemsValue.get() && transformFirstPersonRotate.get().equals("custom", ignoreCase = true) }
    val RotateZ = FloatValue("RotateZAxis", 0f, -180f, 180f).displayable { RotateItemsValue.get() && transformFirstPersonRotate.get().equals("custom", ignoreCase = true) }

    @JvmField
    val guiAnimations: ListValue = ListValue("GuiAnimation", arrayOf("None", "Zoom", "VSlide", "HSlide", "HVSlide"), "Zoom")

    @JvmField
    val tabAnimations = ListValue("Tab-Animation", arrayOf("None", "Zoom", "Slide"), "Zoom")

    fun getModeValue(): ListValue {
        return modeValue
    }

    fun getRotateItems(): Boolean {
        return RotateItemsValue.get()
    }

    var flagRenderTabOverlay = false
        get() = field && tabShowPlayerSkinValue.get()


    @JvmField
    var itemPosX = FloatValue("itemPosX", 0f, -1f, 1f)

    @JvmField
    var itemPosY = FloatValue("itemPosY", 0f, -1f, 1f)

    @JvmField
    var itemPosZ = FloatValue("itemPosZ", 0f, -1f, 1f)

    @JvmField
    var Scale = FloatValue("Scale", 1f, 0f, 2f)
}
