package me.ialistannen.imagewings.command

import com.perceivedev.perceivecore.command.CommandResult
import com.perceivedev.perceivecore.command.CommandSenderType
import com.perceivedev.perceivecore.command.TranslatedCommandNode
import me.ialistannen.imagewings.ImageWings
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.permissions.Permission
import java.util.logging.Level

/**
 * Reloads the particle effects
 */
class CommandReload : TranslatedCommandNode(
        Permission(ImageWings.instance.config.getString("permissions.commands.reload")),
        "command.reload",
        ImageWings.language,
        CommandSenderType.ALL) {

    override fun tabComplete(sender: CommandSender, chat: MutableList<String>, index: Int): List<String> {
        return emptyList()
    }

    override fun executeGeneral(sender: CommandSender, vararg args: String): CommandResult {
        try {
            ImageWings.instance.reloadConfigs()
        } catch (e: Exception) {
            ImageWings.instance.logger.log(Level.SEVERE, "Error reloading configs", e)

            sender.sendMessage(ImageWings.trWithPrefix("command.reload.error.config", e.message ?: "N/A"))

            Bukkit.getPluginManager().disablePlugin(ImageWings.instance)
            return CommandResult.SUCCESSFULLY_INVOKED
        }

        if (!ImageWings.instance.reloadWings()) {
            sender.sendMessage(ImageWings.trWithPrefix("command.reload.error.wings"))

            return CommandResult.SUCCESSFULLY_INVOKED
        }

        sender.sendMessage(ImageWings.trWithPrefix("command.reload.reloaded"))

        return CommandResult.SUCCESSFULLY_INVOKED
    }
}