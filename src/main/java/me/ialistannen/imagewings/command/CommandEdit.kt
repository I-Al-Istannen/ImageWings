package me.ialistannen.imagewings.command

import com.perceivedev.perceivecore.command.CommandResult
import com.perceivedev.perceivecore.command.CommandSenderType
import com.perceivedev.perceivecore.command.TranslatedCommandNode
import me.ialistannen.imagewings.ImageWings
import me.ialistannen.imagewings.interactiveeditor.DummyEditor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

/**
 * Allows you to edit the wings in-game
 */
class CommandEdit : TranslatedCommandNode(
        Permission(ImageWings.instance.config.getString("permissions.commands.edit")),
        "command.edit",
        ImageWings.language,
        CommandSenderType.PLAYER) {

    override fun tabComplete(sender: CommandSender, chat: MutableList<String>, index: Int): List<String> {
        return children
                .filter { it.acceptsCommandSender(sender) }
                .filter { it.hasPermission(sender) }
                .map { it.keyword }
    }

    override fun executePlayer(player: Player?, vararg args: String?): CommandResult {

        // TODO: Continue here!
        return CommandResult.SUCCESSFULLY_INVOKED
    }

    private class Displayer(val playerID: UUID, val dummyEditor: DummyEditor) : BukkitRunnable() {

        override fun run() {

            val player = Bukkit.getPlayer(playerID);
            if (player == null || player.location.distance(dummyEditor.getDummyLocation()) > 100) {
                dummyEditor.destroy()
                cancel()
                return
            }

            dummyEditor.display()
        }
    }
}