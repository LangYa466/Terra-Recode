package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketEntityAction
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.HUD
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.client.gui.inventory.GuiInventory

@ModuleInfo(name = "AutoLobby", description = "Automatic Hub when Health is Low.", category = ModuleCategory.MISC)
class AutoLobby : Module() {
    private val health = FloatValue("Health", 5F, 0F, 20F)
    private val send = BoolValue("SendMessage", false)
    private val message = TextValue("Message", "[${HUD.customClientName.get()}] WTF")
    private val keepArmor = BoolValue("KeepArmor", true)
    private val hubDelayTime = MSTimer()

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer!!.health < health.get()) {


            if (send.get()) {
                mc.thePlayer!!.sendChatMessage(message.get())
            }

            if (hubDelayTime.hasTimePassed(300)) {
                if (keepArmor.get()) {
                    for (i in 0..3) {
                        val armorSlot = 3 - i
                        move(8 - armorSlot, true)
                    }
                }

            }

            if (hubDelayTime.hasTimePassed(1000)) {
                mc.thePlayer!!.sendChatMessage("/hub")
                hubDelayTime.reset()
            }


        }
    }

    private fun move(item: Int, isArmorSlot: Boolean) {
        if (item != -1) {
            val openInventory = mc.currentScreen !is GuiInventory
            if (openInventory) mc.netHandler.addToSendQueue(classProvider.createCPacketEntityAction(mc.thePlayer!!,
                    ICPacketEntityAction.WAction.OPEN_INVENTORY))
            mc.playerController.windowClick(
                    mc.thePlayer!!.inventoryContainer.windowId, if (isArmorSlot) item else if (item < 9) item + 36 else item, 0, 1, mc.thePlayer!!
            )
            if (openInventory) mc.netHandler.addToSendQueue(classProvider.createCPacketCloseWindow())
        }
    }

    override val tag: String
        get() = "HuaYuTing"
}