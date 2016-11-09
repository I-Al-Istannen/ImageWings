package me.ialistannen.imagewings.command

import com.perceivedev.perceivecore.command.CommandSenderType
import com.perceivedev.perceivecore.command.TranslatedCommandNode
import me.ialistannen.imagewings.ImageWings
import org.bukkit.command.CommandSender
import org.bukkit.permissions.Permission

/**
 * The main command for ImageWings
 */
class CommandImageWings : TranslatedCommandNode(
        Permission(ImageWings.instance.config.getString("permissions.commands.main")),
        "command.main",
        ImageWings.language,
        CommandSenderType.ALL) {

    init {
        addChild(CommandEdit())
        addChild(CommandEquip())
        addChild(CommandGenerateTemplate())
    }

    override fun tabComplete(sender: CommandSender, chat: MutableList<String>, index: Int): List<String> {
        return children
                .filter { it.acceptsCommandSender(sender) }
                .filter { it.hasPermission(sender) }
                .map { it.keyword }
    }
}