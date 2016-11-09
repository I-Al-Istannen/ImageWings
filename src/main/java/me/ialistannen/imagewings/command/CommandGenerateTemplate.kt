package me.ialistannen.imagewings.command

import com.perceivedev.perceivecore.command.CommandResult
import com.perceivedev.perceivecore.command.CommandSenderType
import com.perceivedev.perceivecore.command.TranslatedCommandNode
import me.ialistannen.imagewings.ImageWings
import org.bukkit.command.CommandSender
import org.bukkit.permissions.Permission
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.StandardOpenOption

/**
 * Generates a WingMeta template
 */
class CommandGenerateTemplate : TranslatedCommandNode(
        Permission(ImageWings.instance.config.getString("permissions.commands.generate_template")),
        "command.generate.template",
        ImageWings.language,
        CommandSenderType.ALL) {


    override fun tabComplete(sender: CommandSender, chat: MutableList<String>, index: Int): List<String> {
        return emptyList()
    }

    override fun executeGeneral(sender: CommandSender, vararg args: String): CommandResult {
        if (args.isEmpty()) {
            return CommandResult.SEND_USAGE
        }

        val name = args[0]

        val path = ImageWings.instance.dataFolder.toPath().resolve("images").resolve("$name.wingMeta")

        if (Files.exists(path)) {
            sender.sendMessage(ImageWings.trWithPrefix("command.generate.template.file.already.exists", path.fileName))
            return CommandResult.SUCCESSFULLY_INVOKED
        }

        InputStreamReader(ImageWings.instance.getResource("wingMetaTemplate.yml")).use {
            BufferedReader(it).useLines {
                Files.write(path, it.asIterable(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
            }
        }

        sender.sendMessage(ImageWings.trWithPrefix("command.generate.template.generated", name))

        return CommandResult.SUCCESSFULLY_INVOKED
    }
}