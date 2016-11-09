package me.ialistannen.imagewings

import com.perceivedev.perceivecore.command.CommandSystemUtil
import com.perceivedev.perceivecore.command.CommandTree
import com.perceivedev.perceivecore.command.DefaultCommandExecutor
import com.perceivedev.perceivecore.command.DefaultTabCompleter
import com.perceivedev.perceivecore.language.I18N
import me.ialistannen.imagewings.command.CommandImageWings
import me.ialistannen.imagewings.display.WingDisplayManager
import me.ialistannen.imagewings.wings.WingIndexer
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

/**
 * The main class
 */
class ImageWings : JavaPlugin() {

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
            return language.tr(key, *formattingObjects)
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
            return language.tr("prefix") + language.tr(key, *formattingObjects)
        }
    }

    override fun onEnable() {
        instance = this

        dataFolder.mkdirs()
        I18N.copyDefaultFiles(this, true, "me.ialistannen.imagewings.language")

        language = I18N(this, "me.ialistannen.imagewings.language")

        reloadConfigs()
        reloadWings()
    }

    private fun reloadWings() {
        wingDisplayManager = WingDisplayManager()
        val wingIndexer = WingIndexer(dataFolder.toPath().resolve("images"))
        wingIndexer.index(wingDisplayManager)

        logger.info("Loaded ${wingDisplayManager.getAllWings().size} wings.")
    }

    private fun reloadConfigs() {
        reloadConfig()

        // un-register old command
        CommandSystemUtil.unregisterCommand(language.tr("command.main.keyword"))

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