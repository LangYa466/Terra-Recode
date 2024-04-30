package net.ccbluex.liquidbounce.features.command.commands

import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.special.AntiForge
import net.ccbluex.liquidbounce.features.special.AutoReconnect.delay
import net.ccbluex.liquidbounce.features.special.BungeeCordSpoof
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.ui.client.GuiBackground.Companion.enabled
import net.ccbluex.liquidbounce.ui.client.GuiBackground.Companion.particles
import net.ccbluex.liquidbounce.ui.client.altmanager.sub.GuiDonatorCape.Companion.capeEnabled
import net.ccbluex.liquidbounce.ui.client.altmanager.sub.GuiDonatorCape.Companion.transferCode
import net.ccbluex.liquidbounce.ui.client.altmanager.sub.altgenerator.GuiTheAltening.Companion.apiKey
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.value.Value
import org.apache.commons.io.FileUtils
import java.awt.Desktop
import java.io.*
import java.util.function.Consumer

/**
@author ChengFeng
@since 2022/11/26
 */
class ConfigCommand : Command("config", *arrayOf("cfg")) {
    override fun execute(args: Array<String>) {
        if (args.size >= 2) {

            when (args[1].toLowerCase()) {
                "list" -> {
                    chat("§c可访问的配置文件列表:")

                    val settings = this.getLocalSettings() ?: return

                    for (file in settings)
                        chat(file.name)
                    return
                }

                "delete" -> {
                    if (args.size > 2) {
                        val file = File(Terra.fileManager.settingsDir, "${args[2]}.json")
                        if (file.exists()) {
                            try {
                                FileUtils.forceDelete(file)
                                chat("配置文件 " + args[2] + ".json 已删除。")
                                return
                            } catch (e: Exception) {
                                chat("删除配置文件 " + args[2] + ".json 发生错误。")
                                return
                            }
                        }
                        chat("配置文件 " + args[2] + ".json 不存在。")
                        return
                    }
                    chatSyntax("config delete <name>")
                }

                "load" -> {
                    if (args.size > 2) {
                        val file = File(Terra.fileManager.settingsDir, "${args[2]}.json")
                        if (file.exists()) {
                            val jsonElement = JsonParser().parse(BufferedReader(FileReader(file)))
                            try {
                                if (jsonElement is JsonNull) {
                                    chat("配置文件 " + args[2] + ".json 不存在。")
                                    return
                                }
                                val jsonObject = jsonElement as JsonObject

                                val iterator: Iterator<Map.Entry<String, JsonElement>> =
                                        jsonObject.entrySet().iterator()
                                while (iterator.hasNext()) {
                                    val (key, value) = iterator.next()
                                    if (key.equals("CommandPrefix", ignoreCase = true)) {
                                        Terra.commandManager.prefix = value.asCharacter
                                    } else if (key.equals("targets", ignoreCase = true)) {
                                        val jsonValue = value as JsonObject
                                        if (jsonValue.has("TargetPlayer")) EntityUtils.targetPlayer =
                                                jsonValue["TargetPlayer"].asBoolean
                                        if (jsonValue.has("TargetMobs")) EntityUtils.targetMobs =
                                                jsonValue["TargetMobs"].asBoolean
                                        if (jsonValue.has("TargetAnimals")) EntityUtils.targetAnimals =
                                                jsonValue["TargetAnimals"].asBoolean
                                        if (jsonValue.has("TargetInvisible")) EntityUtils.targetInvisible =
                                                jsonValue["TargetInvisible"].asBoolean
                                        if (jsonValue.has("TargetDead")) EntityUtils.targetDead =
                                                jsonValue["TargetDead"].asBoolean
                                    } else if (key.equals("features", ignoreCase = true)) {
                                        val jsonValue = value as JsonObject
                                        if (jsonValue.has("AntiForge")) AntiForge.enabled =
                                                jsonValue["AntiForge"].asBoolean
                                        if (jsonValue.has("AntiForgeFML")) AntiForge.blockFML =
                                                jsonValue["AntiForgeFML"].asBoolean
                                        if (jsonValue.has("AntiForgeProxy")) AntiForge.blockProxyPacket =
                                                jsonValue["AntiForgeProxy"].asBoolean
                                        if (jsonValue.has("AntiForgePayloads")) AntiForge.blockPayloadPackets =
                                                jsonValue["AntiForgePayloads"].asBoolean
                                        if (jsonValue.has("BungeeSpoof")) BungeeCordSpoof.enabled =
                                                jsonValue["BungeeSpoof"].asBoolean
                                        if (jsonValue.has("AutoReconnectDelay")) delay =
                                                jsonValue["AutoReconnectDelay"].asInt
                                    } else if (key.equals("thealtening", ignoreCase = true)) {
                                        val jsonValue = value as JsonObject
                                        if (jsonValue.has("API-Key")) apiKey = jsonValue["API-Key"].asString
                                    } else if (key.equals("DonatorCape", ignoreCase = true)) {
                                        val jsonValue = value as JsonObject
                                        if (jsonValue.has("TransferCode")) transferCode =
                                                jsonValue["TransferCode"].asString
                                        if (jsonValue.has("CapeEnabled")) capeEnabled =
                                                jsonValue["CapeEnabled"].asBoolean
                                    } else if (key.equals("Background", ignoreCase = true)) {
                                        val jsonValue = value as JsonObject
                                        if (jsonValue.has("Enabled")) enabled = jsonValue["Enabled"].asBoolean
                                        if (jsonValue.has("Particles")) particles = jsonValue["Particles"].asBoolean
                                    } else {
                                        val module = Terra.moduleManager.getModule(key)
                                        if (module != null) {
                                            val jsonModule = value as JsonObject
                                            for (moduleValue in module.values) {
                                                val element = jsonModule[moduleValue.name]
                                                if (element != null) moduleValue.fromJson(element)
                                            }
                                        }
                                        if (module != null) {
                                            val jsonModule = value as JsonObject
                                            module.state = jsonModule["State"].asBoolean
                                            module.keyBind = jsonModule["KeyBind"].asInt
                                            if (jsonModule.has("Array")) module.array = jsonModule["Array"].asBoolean
                                        }
                                    }
                                }

                                chat("配置文件 " + args[2] + ".json 已加载。")
                                Terra.hud.addNotification(Notification("Notifications", "Loaded config ${args[2]}.json.", NotifyType.WARNING))
                                playEdit()
                                return
                            } catch (e: Throwable) {
                                chat("§c加载配置文件 " + args[2] + ".json 失败。")
                                ClientUtils.getLogger().error(e)
                                return
                            }
                        }
                    }
                    chatSyntax("config load <name>")
                    return
                }

                "save" -> {
                    if (args.size > 2) {
                        try {

                            val jsonObject = JsonObject()
                            jsonObject.addProperty("CommandPrefix", Terra.commandManager.prefix)

                            val jsonTargets = JsonObject()
                            jsonTargets.addProperty("TargetPlayer", EntityUtils.targetPlayer)
                            jsonTargets.addProperty("TargetMobs", EntityUtils.targetMobs)
                            jsonTargets.addProperty("TargetAnimals", EntityUtils.targetAnimals)
                            jsonTargets.addProperty("TargetInvisible", EntityUtils.targetInvisible)
                            jsonTargets.addProperty("TargetDead", EntityUtils.targetDead)
                            jsonObject.add("targets", jsonTargets)

                            val jsonFeatures = JsonObject()
                            jsonFeatures.addProperty("AntiForge", AntiForge.enabled)
                            jsonFeatures.addProperty("AntiForgeFML", AntiForge.blockFML)
                            jsonFeatures.addProperty("AntiForgeProxy", AntiForge.blockProxyPacket)
                            jsonFeatures.addProperty("AntiForgePayloads", AntiForge.blockPayloadPackets)
                            jsonFeatures.addProperty("BungeeSpoof", BungeeCordSpoof.enabled)
                            jsonFeatures.addProperty("AutoReconnectDelay", delay)
                            jsonObject.add("features", jsonFeatures)

                            val theAlteningObject = JsonObject()
                            theAlteningObject.addProperty("API-Key", apiKey)
                            jsonObject.add("thealtening", theAlteningObject)

                            Terra.moduleManager.modules.stream()
                                    .forEach { module: Module ->
                                        val jsonModule = JsonObject()
                                        jsonModule.addProperty("State", module.state)
                                        jsonModule.addProperty("KeyBind", module.keyBind)
                                        jsonModule.addProperty("Array", module.array)

                                        module.values.forEach(Consumer { value: Value<*> ->
                                            jsonModule.add(
                                                    value.name,
                                                    value.toJson()
                                            )
                                        })
                                        jsonObject.add(module.name, jsonModule)
                                    }

                            val printWriter =
                                    PrintWriter(FileWriter(File(Terra.fileManager.settingsDir, "${args[2]}.json")))
                            printWriter.println(FileManager.PRETTY_GSON.toJson(jsonObject))
                            printWriter.close()
                            chat("配置文件 " + args[2] + ".json 已保存。")
                            return
                        } catch (e: Throwable) {
                            chat("§c保存配置文件 " + args[2] + ".json 失败。")
                            return
                        }
                    }
                    chatSyntax("config save <name>")
                    return
                }

                "folder" -> {
                    try {
                        Desktop.getDesktop().open(Terra.fileManager.settingsDir)
                        chat("成功打开了配置文件夹。")
                        return
                    } catch (t: Throwable) {
                        ClientUtils.getLogger().error("在打开配置文件夹时发生错误。", t)
                        chat("${t.javaClass.name}: ${t.message}")
                        return
                    }
                }
            }
        }
        chatSyntax("config <load/save/list/delete>")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> listOf("delete", "list", "load", "save", "folder").filter { it.startsWith(args[0], true) }
            2 -> {
                when (args[0].toLowerCase()) {
                    "delete", "load", "save" -> {
                        val settings = this.getLocalSettings() ?: return emptyList()

                        return settings
                                .map { it.name.replace(".json", "") }
                                .filter { it.startsWith(args[1], true) }
                    }
                }
                return emptyList()
            }

            else -> emptyList()
        }
    }

    private fun getLocalSettings(): Array<File>? = Terra.fileManager.settingsDir.listFiles()

}