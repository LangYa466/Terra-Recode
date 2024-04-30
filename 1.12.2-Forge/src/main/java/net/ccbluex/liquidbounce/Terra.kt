/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce

import me.qingyou.terra.sound.Sound
import me.qingyou.terra.sound.TipSoundManager
import net.ccbluex.liquidbounce.api.Wrapper
import net.ccbluex.liquidbounce.api.minecraft.util.IResourceLocation
import net.ccbluex.liquidbounce.cape.CapeAPI.registerCapeService
import net.ccbluex.liquidbounce.event.ClientShutdownEvent
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.special.AntiForge
import net.ccbluex.liquidbounce.features.special.BungeeCordSpoof
import net.ccbluex.liquidbounce.features.special.CombatManager
import net.ccbluex.liquidbounce.features.special.DonatorCape
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.script.ScriptManager
import net.ccbluex.liquidbounce.script.remapper.Remapper.loadSrg
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.client.hud.HUD.Companion.createDefault
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.RotationUtils

object Terra {
    // Client information
    const val CLIENT_NAME = "Terra"
    const val CLIENT_VERSION = 14
    const val CLIENT_CREATOR = "Qingyou || LangYa"
    const val CLIENT_CLOUD = "https://cloud.liquidbounce.net/LiquidBounce"
    val UPDATE_LIST = arrayListOf(
        "b13",
        "Fly Recode",
        "Mr.CAJ DaTou",
        "KillAura add SwitchDelay (for HYT Pit)",
        "AutoWeapon OnlySword",
        "New Font",
        "NoSlow Add HytPit Mode",
        "Criticals Recode",
        "Anime GuiMainMenu",
        "Recode TargetHud",
        "Reduced the volume of the project",
        "Fix Notification",
        "Fix NameTags",
        "Add MessageBuilder"
    )

    var isStarting = false

    // Managers
    lateinit var moduleManager: ModuleManager
    lateinit var commandManager: CommandManager
    lateinit var eventManager: EventManager
    lateinit var fileManager: FileManager
    lateinit var scriptManager: ScriptManager
    lateinit var tipSoundManager: TipSoundManager
    lateinit var combatManager: CombatManager

    // HUD & ClickGUI
    lateinit var hud: HUD

    lateinit var clickGui: ClickGui

    // Menu Background
    var background: IResourceLocation? = null

    lateinit var wrapper: Wrapper

    /**
     * Execute if client will be started
     */
    fun startClient() {
        isStarting = true

        ClientUtils.getLogger().info("Starting $CLIENT_NAME b$CLIENT_VERSION, by $CLIENT_CREATOR")

        // Create file manager
        fileManager = FileManager()

        // Crate event manager
        eventManager = EventManager()

        // init CombatManager
        combatManager = CombatManager()

        // Register listeners
        eventManager.registerListener(RotationUtils())
        eventManager.registerListener(AntiForge())
        eventManager.registerListener(BungeeCordSpoof())
        eventManager.registerListener(DonatorCape())
        eventManager.registerListener(InventoryUtils())
        eventManager.registerListener(combatManager)

        // Create command manager
        commandManager = CommandManager()

        // Init SoundManager
        tipSoundManager = TipSoundManager()

        // Load client fonts
        Fonts.loadFonts()

        // Setup module manager and register modules
        moduleManager = ModuleManager()
        moduleManager.registerModules()

        // Remapper
        try {
            loadSrg()
            // ScriptManager
            scriptManager = ScriptManager()
            scriptManager.loadScripts()
            scriptManager.enableScripts()
        } catch (throwable: Throwable) {
            ClientUtils.getLogger().error("Failed to load scripts.", throwable)
        }

        // Register commands
        commandManager.registerCommands()

        // Load configs
        fileManager.loadConfigs(fileManager.modulesConfig, fileManager.valuesConfig, fileManager.accountsConfig,
                fileManager.friendsConfig, fileManager.xrayConfig, fileManager.shortcutsConfig)

        // ClickGUI
        clickGui = ClickGui()
        fileManager.loadConfig(fileManager.clickGuiConfig)

        // Register capes service
        try {
            registerCapeService()
        } catch (throwable: Throwable) {
            ClientUtils.getLogger().error("Failed to register cape service", throwable)
        }

        // Set HUD
        hud = createDefault()
        fileManager.loadConfig(fileManager.hudConfig)

        // Disable optifine fastrender
        ClientUtils.disableFastRender()

        // Load generators
        GuiAltManager.loadGenerators()

        //Play SPEC Sound
        Sound()

        // Set is starting status
        isStarting = false
    }

    /**
     * Execute if client will be stopped
     */
    fun stopClient() {
        // Call client shutdown
        eventManager.callEvent(ClientShutdownEvent())

        // Save all available configs
        fileManager.saveAllConfigs()
    }

}