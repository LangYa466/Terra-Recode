/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.KeyEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.features.module.modules.client.*
import net.ccbluex.liquidbounce.features.module.modules.combat.*
import net.ccbluex.liquidbounce.features.module.modules.exploit.*
import net.ccbluex.liquidbounce.features.module.modules.misc.*
import net.ccbluex.liquidbounce.features.module.modules.movement.*
import net.ccbluex.liquidbounce.features.module.modules.player.*
import net.ccbluex.liquidbounce.features.module.modules.render.*
import net.ccbluex.liquidbounce.features.module.modules.world.*
import net.ccbluex.liquidbounce.features.module.modules.world.Timer
import net.ccbluex.liquidbounce.features.special.AutoDisable
import net.ccbluex.liquidbounce.utils.ClientUtils
import java.util.*


class ModuleManager : Listenable {

    val modules = TreeSet<Module> { module1, module2 -> module1.name.compareTo(module2.name) }
    private val moduleClassMap = hashMapOf<Class<*>, Module>()
    var toggleSoundMode = 0

    var toggleVolume = 0F

    init {
        Terra.eventManager.registerListener(this)
    }

    /**
     * Register all modules
     */
    fun registerModules() {
        ClientUtils.getLogger().info("[ModuleManager] Loading modules...")

        registerModules(
//Client
            ClickGUI::class.java,
            HitEffect::class.java,
            HUD::class.java,
            HudDesigner::class.java,
            HurtCam::class.java,
            NoAchievements::class.java,
            SilentDisconnect::class.java,
//Combat
            Aimbot::class.java,
            AuraHelper::class.java,
            AutoBlock::class.java,
            AutoClicker::class.java,
            AutoGapple::class.java,
            AutoL::class.java,
            AutoSoup::class.java,
            AutoWeapon::class.java,
            BowAimbot::class.java,
            Criticals::class.java,
            FastBow::class.java,
            HitBox::class.java,
            KillAura::class.java,
            NoFriends::class.java,
            SuperKnockback::class.java,
            Trigger::class.java,
            Velocity::class.java,
//Exploit
            AbortBreaking::class.java,
            AntiHunger::class.java,
            Disabler::class.java,
            ForceUnicodeChat::class.java,
            Ghost::class.java,
            GhostHand::class.java,
            KeepContainer::class.java,
            Kick::class.java,
            MultiActions::class.java,
            NoPitchLimit::class.java,
            Phase::class.java,
            PingSpoof::class.java,
            PortalMenu::class.java,
            MessageBuilder::class.java,
//Misc
            Alert::class.java,
            AntiBot::class.java,
            AntiFakePlayer::class.java,
            AutoGG::class.java,
            AutoLobby::class.java,
            MidClick::class.java,
            NameProtect::class.java,
            NoRotateSet::class.java,
            Spammer::class.java,
            Teams::class.java,
            Title::class.java,
//Movement
            AirJump::class.java,
            AntiVoid::class.java,
            EntitySpeed::class.java,
            FastClimb::class.java,
            Fly::class.java,
            Freeze::class.java,
            HighJump::class.java,
            HytFly::class.java,
            InventoryMove::class.java,
            LiquidWalk::class.java,
            LongJump::class.java,
            NoJumpDelay::class.java,
            NoObstacle::class.java,
            NoSlow::class.java,
            Parkour::class.java,
            SafeWalk::class.java,
            Sneak::class.java,
            Speed::class.java,
            Sprint::class.java,
            Step::class.java,
            Strafe::class.java,
            NoC03s::class.java,
//Player
            AntiAFK::class.java,
            AntiAim::class.java,
            AntiCactus::class.java,
            AutoRespawn::class.java,
            AutoTool::class.java,
            Blink::class.java,
            Eagle::class.java,
            FastUse::class.java,
            InvManager::class.java,
            NoFall::class.java,
            Reach::class.java,
            Regen::class.java,
            Stuck::class.java,
            Zoot::class.java,
//Render
            AntiBlind::class.java,
            BlockESP::class.java,
            BlockOverlay::class.java,
            Breadcrumbs::class.java,
            CameraClip::class.java,
            Cape::class.java,
            Chams::class.java,
            Crosshair::class.java,
            DMGParticle::class.java,
            EnchantEffect::class.java,
            ESP::class.java,
            FollowTargetHud::class.java,
            FreeCam::class.java,
            Fullbright::class.java,
            HealthHud::class.java,
            ItemESP::class.java,
            NameTags::class.java,
            NoBob::class.java,
            NoFOV::class.java,
            NoSwing::class.java,
            Particles::class.java,
            Projectiles::class.java,
            StorageESP::class.java,
            TerraESP::class.java,
            TNTESP::class.java,
            Tracers::class.java,
            TrueSight::class.java,
            XRay::class.java,
//World
            Ambience::class.java,
            CivBreak::class.java,
            FastBreak::class.java,
            FastPlace::class.java,
            Liquids::class.java,
            NoSlowBreak::class.java,
            Nuker::class.java,
            Scaffold::class.java,
            ScaHelp::class.java,
            Stealer::class.java,
            Timer::class.java,
            Tower::class.java
        )

        registerModule(NoScoreboard)
        registerModule(Fucker)
        registerModule(ChestAura)
        registerModule(Animations)
        registerModule(Wings)
        registerModule(ItemPhysics)
        registerModule(Rotations)
        Terra.eventManager.registerListener(AutoDisable)

        ClientUtils.getLogger().info("[ModuleManager] Loaded ${modules.size} modules.")
    }

    /**
     * Register [module]
     */
    fun registerModule(module: Module) {
        if (!module.isSupported)
            return

        modules += module
        moduleClassMap[module.javaClass] = module

        generateCommand(module)
        Terra.eventManager.registerListener(module)
    }

    /**
     * Register [moduleClass]
     */
    private fun registerModule(moduleClass: Class<out Module>) {
        try {
            registerModule(moduleClass.newInstance())
        } catch (e: Throwable) {
            ClientUtils.getLogger()
                .error("Failed to load module: ${moduleClass.name} (${e.javaClass.name}: ${e.message})")
        }
    }

    /**
     * Register a list of modules
     */
    @SafeVarargs
    fun registerModules(vararg modules: Class<out Module>) {
        modules.forEach(this::registerModule)
    }

    private fun registerModule(cbModule: Any?) {
        registerModule((cbModule as Class<out Module>).newInstance())
    }

    /**
     * Unregister module
     */
    fun unregisterModule(module: Module) {
        modules.remove(module)
        moduleClassMap.remove(module::class.java)
        Terra.eventManager.unregisterListener(module)
    }

    /**
     * Generate command for [module]
     */
    internal fun generateCommand(module: Module) {
        val values = module.values

        if (values.isEmpty())
            return

        Terra.commandManager.registerCommand(ModuleCommand(module, values))
    }

    /**
     * Legacy stuff
     *
     * TODO: Remove later when everything is translated to Kotlin
     */

    /**
     * Get module by [moduleClass]
     */
    fun <T : Module> getModule(moduleClass: Class<T>): T {
        return moduleClassMap[moduleClass]!! as T
    }

    /**
     * Get module by [moduleName]
     */
    fun getModule(moduleName: String?) = modules.find { it.name.equals(moduleName, ignoreCase = true) }

    /**
     * Module related events
     */

    /**
     * Handle incoming key presses
     */
    @EventTarget
    private fun onKey(event: KeyEvent) = modules.filter { it.keyBind == event.key }.forEach { it.toggle() }

    override fun handleEvents() = true

    operator fun <T : Module> get(clazz: Class<T>) = getModule(clazz)
}
