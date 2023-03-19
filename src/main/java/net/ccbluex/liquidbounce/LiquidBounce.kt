// LiquidX Development by PrahXZ and Haflin with FDP Base modified. v2.0 R1
package net.ccbluex.liquidbounce

import net.ccbluex.liquidbounce.event.ClientShutdownEvent
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.features.macro.MacroManager
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.special.*
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.file.config.ConfigManager
import net.ccbluex.liquidbounce.ui.client.gui.EnumLaunchFilter
import net.ccbluex.liquidbounce.ui.client.gui.LaunchFilterInfo
import net.ccbluex.liquidbounce.ui.client.gui.LaunchOption
import net.ccbluex.liquidbounce.ui.client.gui.GuiLaunchOptionSelectMenu
import net.ccbluex.liquidbounce.script.ScriptManager
import net.ccbluex.liquidbounce.ui.cape.GuiCapeManager
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.ccbluex.liquidbounce.ui.sound.TipSoundManager
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.math.MathUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ResourceLocation
import java.util.*

object LiquidBounce {

    // Client information

    const val CLIENT_NAME = "LiquidX"

    const val CLIENT_PREFIX = "§3§lLiquidX §8» "
    const val BIG_NAME = "§3§lLiquidX §f§lClient"
    const val CLIENT_CREATOR = "Prah"
    const val CLIENT_RELEASE = "Legacy Release"
    const val CLIENT_IP = "www.liquidx.net"
    val UID = MathUtils.randomNumber(600, 1)


    // Dark mode :skull:
    var Darkmode = true
    
    @JvmField
    val gitInfo = Properties().also {
        val inputStream = LiquidBounce::class.java.classLoader.getResourceAsStream("git.properties")
        if (inputStream != null) {
            it.load(inputStream)
        } else {
            it["git.branch"] = "main"
        }
    }

    @JvmField

    val CLIENT_VERSION = "v3.1"


    @JvmField
    val CLIENT_BRANCH = (gitInfo["git.branch"] ?: "unknown").let {
        if (it == "main") "Diana Edition" else it
    }

    var isStarting = true
    var isLoadingConfig = true

    // Managers
    lateinit var moduleManager: ModuleManager

    lateinit var commandManager: CommandManager
    lateinit var eventManager: EventManager
    lateinit var fileManager: FileManager
    lateinit var scriptManager: ScriptManager
    lateinit var tipSoundManager: TipSoundManager
    lateinit var combatManager: CombatManager
    lateinit var macroManager: MacroManager
    lateinit var configManager: ConfigManager

    // Some UI things
    lateinit var hud: HUD
    lateinit var mainMenu: GuiScreen

    // Menu Background
    var background: ResourceLocation? = ResourceLocation("liquidx/background.png")

    val launchFilters = mutableListOf<EnumLaunchFilter>()
    private val dynamicLaunchOptions: Array<LaunchOption>
        get() = ClassUtils.resolvePackage(
            "${LaunchOption::class.java.`package`.name}.options",
            LaunchOption::class.java
        )
            .filter {
                val annotation = it.getDeclaredAnnotation(LaunchFilterInfo::class.java)
                if (annotation != null) {
                    return@filter annotation.filters.toMutableList() == launchFilters
                }
                false
            }
            .map {
                try {
                    it.newInstance()
                } catch (e: IllegalAccessException) {
                    ClassUtils.getObjectInstance(it) as LaunchOption
                }
            }.toTypedArray()

    /**
     * Execute if client will be started
     */
    fun initClient() {
        ClientUtils.logInfo("Loading $CLIENT_NAME $CLIENT_VERSION, by $CLIENT_CREATOR")
        ClientUtils.logInfo("Initialzing...")
        val startTime = System.currentTimeMillis()
        // Create file manager
        fileManager = FileManager()
        configManager = ConfigManager()
        // Create event manager
        eventManager = EventManager()

        // Load language
        LanguageManager.switchLanguage(Minecraft.getMinecraft().gameSettings.language)

        // Register listeners
        eventManager.registerListener(RotationUtils())
        eventManager.registerListener(AntiForge)
        eventManager.registerListener(InventoryUtils)
        eventManager.registerListener(BungeeCordSpoof())
        eventManager.registerListener(ServerSpoof)
        eventManager.registerListener(SessionUtils())
        eventManager.registerListener(StatisticsUtils())
        eventManager.registerListener(LocationCache())

        // Create command manager
        commandManager = CommandManager()

        fileManager.loadConfigs(
            fileManager.accountsConfig,
            fileManager.friendsConfig,
            fileManager.specialConfig,
            fileManager.subscriptsConfig
        )
        // Load client fonts
        Fonts.loadFonts()

        macroManager = MacroManager()
        eventManager.registerListener(macroManager)

        // Setup module manager and register modules
        moduleManager = ModuleManager()
        moduleManager.registerModules()

        try {
            // ScriptManager, Remapper will be lazy loaded when scripts are enabled
            scriptManager = ScriptManager()
            scriptManager.loadScripts()
            scriptManager.enableScripts()
        } catch (throwable: Throwable) {
            ClientUtils.logError("Failed to load scripts.", throwable)
        }

        // Register commands
        commandManager.registerCommands()

        tipSoundManager = TipSoundManager()


        combatManager = CombatManager()
        eventManager.registerListener(combatManager)

        GuiCapeManager.load()

        mainMenu = GuiLaunchOptionSelectMenu()

        // Set HUD
        hud = HUD.createDefault()

        fileManager.loadConfigs(fileManager.hudConfig, fileManager.xrayConfig)

        // Load Scripts
        ClientUtils.setTitle()
        ClientUtils.logInfo("$CLIENT_NAME $CLIENT_VERSION loaded in ${(System.currentTimeMillis() - startTime)}ms!")
    }


    /**
     * Execute if client ui type is selected
     */

    fun startClient() {
        dynamicLaunchOptions.forEach {
            it.start()
        }

        // Load configs
        configManager.loadLegacySupport()
        configManager.loadConfigSet()

        // Set is starting status
        isStarting = false
        isLoadingConfig = false

        ClientUtils.logInfo("$CLIENT_NAME $CLIENT_VERSION started!")
    }

    /**
     * Execute if client will be stopped
     */
    fun stopClient() {
        if (!isStarting && !isLoadingConfig) {
            ClientUtils.logInfo("Shutting down $CLIENT_NAME $CLIENT_VERSION!")

            // Call client shutdown
            eventManager.callEvent(ClientShutdownEvent())

            // Save all available configs
            GuiCapeManager.save()
            configManager.save(true, true)
            fileManager.saveAllConfigs()

            dynamicLaunchOptions.forEach {
                it.stop()
            }
        }
        try {
            DiscordRPC.stop()
        } catch (e: Throwable) {
            ClientUtils.logError("Failed to shutdown DiscordRPC.", e)
        }
    }
}
