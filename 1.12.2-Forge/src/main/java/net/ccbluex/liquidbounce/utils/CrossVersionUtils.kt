package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.api.enums.EnumFacingType
import net.ccbluex.liquidbounce.api.enums.WEnumHand
import net.ccbluex.liquidbounce.api.minecraft.item.IItemStack
import net.ccbluex.liquidbounce.api.minecraft.network.IPacket
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketEntityAction
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketPlayerDigging
import net.ccbluex.liquidbounce.api.minecraft.util.WBlockPos
import net.ccbluex.liquidbounce.injection.backend.Backend
import net.ccbluex.liquidbounce.injection.backend.WrapperImpl.classProvider


fun createUseItemPacket(hand: WEnumHand): IPacket {
    return classProvider.createCPacketTryUseItem(hand)
}

fun createOpenInventoryPacket(): IPacket {
    return classProvider.createCPacketEntityAction(
        Terra.wrapper.minecraft.thePlayer!!,
        ICPacketEntityAction.WAction.OPEN_INVENTORY
    )
}

inline fun createUseItemPacket(itemStack: IItemStack?, hand: WEnumHand): IPacket {
    @Suppress("ConstantConditionIf")
    return if (Backend.MINECRAFT_VERSION_MINOR == 8) {
        classProvider.createCPacketPlayerBlockPlacement(itemStack)
    } else {
        classProvider.createCPacketTryUseItem(hand)
    }
}

inline fun createblockc08c07(itemStack: IItemStack?, hand: WEnumHand): IPacket? {
    @Suppress("ConstantConditionIf")
    return if (Backend.MINECRAFT_VERSION_MINOR == 8) {
        classProvider.createCPacketTryUseItem(hand)
//        classProvider.createCPacketPlayerBlockPlacement(itemStack)
//        classProvider.createCPacketPlayerDigging(ICPacketPlayerDigging.WAction.RELEASE_USE_ITEM, WBlockPos.ORIGIN, MinecraftInstance.classProvider.getEnumFacing(EnumFacingType.DOWN))
    } else {
        classProvider.createCPacketPlayerDigging(
            ICPacketPlayerDigging.WAction.RELEASE_USE_ITEM, WBlockPos.ORIGIN, MinecraftInstance.classProvider.getEnumFacing(
                EnumFacingType.DOWN))
    }
}

inline fun createblockc07(itemStack: IItemStack?, hand: WEnumHand): IPacket? {
    @Suppress("ConstantConditionIf")
    return if (Backend.MINECRAFT_VERSION_MINOR == 8) {
        classProvider.createCPacketPlayerDigging(
            ICPacketPlayerDigging.WAction.RELEASE_USE_ITEM, WBlockPos.ORIGIN, classProvider.getEnumFacing(
                EnumFacingType.DOWN))
    } else {
        classProvider.createCPacketTryUseItem(hand)
    }
}
