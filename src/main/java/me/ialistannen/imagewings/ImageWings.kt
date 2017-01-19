package me.ialistannen.imagewings

import com.perceivedev.perceivecore.command.CommandSystemUtil
import com.perceivedev.perceivecore.command.CommandTree
import com.perceivedev.perceivecore.command.DefaultCommandExecutor
import com.perceivedev.perceivecore.command.DefaultTabCompleter
import com.perceivedev.perceivecore.language.I18N
import me.ialistannen.imagewings.command.CommandImageWings
import me.ialistannen.imagewings.display.WingDisplayManager
import me.ialistannen.imagewings.example.copyExampleWing
import me.ialistannen.imagewings.wings.WingIndexer
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

/**
 * The main class
 */
class ImageWings : JavaPlugin() {

    private var wingManagerIsInitialized = false

    companion object {
        lateinit var instance: ImageWings
            private set
        lateinit var language: I18N
            private set
        lateinit var wingDisplayManager: WingDisplayManager
            private set

        /**
         * Translates a String
         *
         * @param key The key of the message
         * @param formattingObjects The objects to format the message with
         *
         * @return The translated Message
         */
        fun tr(key: String, vararg formattingObjects: Any): String {
            return language.translate(key, *formattingObjects)
        }

        /**
         * Translates a String and appends the prefix defined in the language file
         *
         * @param key The key of the message
         * @param formattingObjects The objects to format the message with
         *
         * @return The translated Message
         */
        fun trWithPrefix(key: String, vararg formattingObjects: Any): String {
            return language.translate("prefix") + language.translate(key, *formattingObjects)
        }
    }

    override fun onEnable() {
        instance = this

        saveDefaultConfig()

        // TODO: TRUE -> FALSE
        I18N.copyDefaultFiles(this, false, "me.ialistannen.imagewings.language")

        language = I18N(this, "me.ialistannen.imagewings.language")

        copyExampleWing(dataFolder.toPath().resolve("images"))
        
        reloadConfigs()
        reloadWings()
    }

    /**
     * Reloads all wings.
     *
     * Destroys all current wings before that, but tries to keep the player's wings intact.
     *
     * @return False if an error occurred
     */
    fun reloadWings(): Boolean {
        val old = if (wingManagerIsInitialized) {
            wingDisplayManager
        } else {
            null
        }

        wingDisplayManager = WingDisplayManager()
        wingManagerIsInitialized = true

        val wingIndexer = WingIndexer(dataFolder.toPath().resolve("images"))
        val result = wingIndexer.index(wingDisplayManager)

        logger.info("Loaded ${wingDisplayManager.getAllWings().size} wings.")

        if (old != null) {
            // re-add the old wings
            old.getAllPlayersWing().toMap()
                    .mapKeys { Bukkit.getPlayer(it.key) }
                    .filterKeys { it != null }
                    .forEach { wingDisplayManager.addPlayer(it.key, it.value) }
            old.destroy()
        }

        return result
    }

    /**
     * Reloads all configs ([getConfig], [language] and commands)
     */
    fun reloadConfigs() {
        reloadConfig()

        // un-register old command
        CommandSystemUtil.unregisterCommand(language.translate("command.main.keyword"))

        language.reload()
        language.language = Locale.forLanguageTag(config.getString("language"))

        val tree = CommandTree()
        val executor = DefaultCommandExecutor(tree, language)
        val tabCompleter = DefaultTabCompleter(tree)

        val mainCommand = CommandImageWings()

        tree.addTopLevelChildAndRegister(mainCommand, executor, tabCompleter, this)
        tree.attachHelp(mainCommand, config.getString("permissions.commands.help"), language)
    }
}