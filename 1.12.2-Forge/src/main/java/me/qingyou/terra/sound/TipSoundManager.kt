package me.qingyou.terra.sound

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.utils.FileUtils
import java.io.File

class TipSoundManager {
    var enableSoundTerra: TipSoundPlayer
    var disableSoundTerra: TipSoundPlayer

    var enableSoundRise: TipSoundPlayer
    var disableSoundRise: TipSoundPlayer

    init {
        val enableSoundFileTerra = File(Terra.fileManager.soundsDir, "enableTerra.wav")
        val disableSoundFileTerra = File(Terra.fileManager.soundsDir, "disableTerra.wav")
        if (!enableSoundFileTerra.exists()) FileUtils.unpackFile(enableSoundFileTerra, "assets/minecraft/terra/sound/enableTerra.wav")
        if (!disableSoundFileTerra.exists()) FileUtils.unpackFile(disableSoundFileTerra, "assets/minecraft/terra/sound/disableTerra.wav")
        enableSoundTerra = TipSoundPlayer(enableSoundFileTerra)
        disableSoundTerra = TipSoundPlayer(disableSoundFileTerra)

        val enableSoundFileRise = File(Terra.fileManager.soundsDir, "enableRise.wav")
        val disableSoundFileRise = File(Terra.fileManager.soundsDir, "disableRise.wav")
        if (!enableSoundFileRise.exists()) FileUtils.unpackFile(enableSoundFileRise, "assets/minecraft/terra/sound/enableRise.wav")
        if (!disableSoundFileRise.exists()) FileUtils.unpackFile(disableSoundFileRise, "assets/minecraft/terra/sound/disableRise.wav")
        enableSoundRise = TipSoundPlayer(enableSoundFileRise)
        disableSoundRise = TipSoundPlayer(disableSoundFileRise)
    }
}