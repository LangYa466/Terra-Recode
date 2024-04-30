/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.script.api

import jdk.nashorn.api.scripting.JSObject
import jdk.nashorn.api.scripting.ScriptUtils
import net.ccbluex.liquidbounce.api.minecraft.item.IItemStack
import net.ccbluex.liquidbounce.api.util.WrappedCreativeTabs
import net.ccbluex.liquidbounce.injection.backend.WrapperImpl.functions

@Suppress("UNCHECKED_CAST", "unused")
class ScriptTab(private val tabObject: JSObject) : WrappedCreativeTabs(tabObject.getMember("name") as String) {
    val items = ScriptUtils.convert(tabObject.getMember("items"), Array<IItemStack>::class.java) as Array<IItemStack>

    override fun getTabIconItem() = functions.getItemByName(tabObject.getMember("icon") as String)!!

    override fun getTranslatedTabLabel() = tabObject.getMember("name") as String

    override fun displayAllReleventItems(items: MutableList<IItemStack>) {
        items.forEach { items.add(it) }
    }
}