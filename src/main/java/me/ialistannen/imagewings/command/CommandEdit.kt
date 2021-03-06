package me.ialistannen.imagewings.command

import com.perceivedev.perceivecore.command.CommandResult
import com.perceivedev.perceivecore.command.CommandSenderType
import com.perceivedev.perceivecore.command.TranslatedCommandNode
import com.perceivedev.perceivecore.coreplugin.PerceiveCore
import com.perceivedev.perceivecore.utilities.item.ItemFactory
import me.ialistannen.imagewings.ImageWings
import me.ialistannen.imagewings.interactiveeditor.ArmorStandEditor
import me.ialistannen.imagewings.interactiveeditor.DummyEditor
import me.ialistannen.imagewings.parser.ImageParser
import me.ialistannen.imagewings.parser.Parser
import me.ialistannen.imagewings.wings.WingIndexer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.permissions.Permission
import org.bukkit.scheduler.BukkitRunnable
import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.imageio.ImageIO

/**
 * Allows you to edit the wings in-game
 */
class CommandEdit : Listener, TranslatedCommandNode(
        Permission(ImageWings.instance.config.getString("permissions.commands.edit")),
        "command.edit",
        ImageWings.language,
        CommandSenderType.PLAYER) {

    init {
        Bukkit.getPluginManager().registerEvents(this, ImageWings.instance)

        PerceiveCore.getInstance().disableManager.addListener {
            ->
            playerMap.values.forEach {
                it.dummyEditor.destroy()
            }
        }
    }

    private val playerMap: MutableMap<UUID, Displayer> = HashMap()

    override fun tabComplete(sender: CommandSender, chat: MutableList<String>, index: Int): List<String> {
        val list = ArrayList<String>()

        if (sender is Player && sender.uniqueId in playerMap) {
            list.add(ImageWings.tr("command.edit.stop.save.keyword"))
            list.add(ImageWings.tr("command.edit.stop.discard.keyword"))
        } else if (index == 0) {
            list.addAll(ImageWings.wingDisplayManager.getAllWingFileNames())
        }
        return list
    }

    override fun executePlayer(player: Player, vararg args: String): CommandResult {

        if (player.uniqueId in playerMap) {
            if (args.isEmpty()) {
                player.sendMessage(ImageWings.trWithPrefix("command.edit.stop.usage"))
                return CommandResult.SUCCESSFULLY_INVOKED
            }
            val decision = args[0]

            val displayer: Displayer = playerMap[player.uniqueId]!!
            val wingFileName = displayer.configPath.fileName.toString().replace(".wingMeta", "")

            if (decision == ImageWings.tr("command.edit.stop.save.keyword")) {
                val config = YamlConfiguration.loadConfiguration(displayer.configPath.toFile())

                // delete old wing
                try {
                    val parser = Parser(YamlConfiguration.loadConfiguration(displayer.configPath.toFile()))
                    ImageWings.wingDisplayManager.removeWing(parser.wing)
                } catch (ignore: IllegalArgumentException) {
                    // the file was modified (and damaged) while being edited. Will cause some glitches (wing appears twice) if I ignore it,
                    // but it is probably nicer than the alternative: Logging an error
                }

                displayer.dummyEditor.saveToConfig(config)

                config.save(displayer.configPath.toFile())

                // reindex the wing
                val indexer = WingIndexer(displayer.configPath)
                indexer.index(ImageWings.wingDisplayManager)

                player.sendMessage(ImageWings.trWithPrefix("command.edit.stopped.editing.saved", wingFileName))
            } else if (decision == ImageWings.tr("command.edit.stop.discard.keyword")) {
                // do nothing ==> Doesn't save changes too :)
                player.sendMessage(ImageWings.trWithPrefix("command.edit.stopped.editing.discarded", wingFileName))
            } else {
                player.sendMessage(ImageWings.trWithPrefix("command.edit.stop.usage"))
                return CommandResult.SUCCESSFULLY_INVOKED
            }

            displayer.dummyEditor.destroy()
            playerMap.remove(player.uniqueId)

            return CommandResult.SUCCESSFULLY_INVOKED
        }

        if (args.isEmpty()) {
            return CommandResult.SEND_USAGE
        }

        val name = args[0]

        val wingMetaPath = ImageWings.instance.dataFolder.toPath().resolve("images").resolve(name + ".wingMeta")

        if (Files.notExists(wingMetaPath)) {
            player.sendMessage(ImageWings.trWithPrefix("command.edit.wing.data.not.found", name))
            return CommandResult.SUCCESSFULLY_INVOKED
        }

        val wingMetaConfiguration = YamlConfiguration.loadConfiguration(wingMetaPath.toFile())


        // ====== PLAYER VECTOR MULTIPLIER ======
        if (!wingMetaConfiguration.isDouble("player_vector_multiplier")) {
            player.sendMessage(ImageWings.trWithPrefix("command.edit.wing.meta.misses.player.vector.multiplier", name))
            return CommandResult.SUCCESSFULLY_INVOKED
        }
        val vectorMultiplier = wingMetaConfiguration.getDouble("player_vector_multiplier")


        // ====== IMAGE ======
        if (!wingMetaConfiguration.isString("image_path")) {
            player.sendMessage(ImageWings.trWithPrefix("command.edit.wing.meta.misses.image.path", name))
            return CommandResult.SUCCESSFULLY_INVOKED
        }

        val imagePath = ImageWings.instance.dataFolder.toPath().resolve("images").resolve(wingMetaConfiguration.getString("image_path"))

        if (Files.notExists(imagePath)) {
            player.sendMessage(ImageWings.trWithPrefix("command.edit.wing.image.not.found", name))
            return CommandResult.SUCCESSFULLY_INVOKED
        }

        val image: BufferedImage?
        try {
            image = ImageIO.read(imagePath.toFile())
        } catch (e: IOException) {
            player.sendMessage(ImageWings.trWithPrefix("command.edit.wing.image.error.reading.image", name))
            return CommandResult.SUCCESSFULLY_INVOKED
        }

        if (image == null) {
            player.sendMessage(ImageWings.trWithPrefix("command.edit.wing.image.not.valid.format", name))
            return CommandResult.SUCCESSFULLY_INVOKED
        }


        // ====== PARSER ======
        val parserSection = wingMetaConfiguration.getConfigurationSection("parser")

        if (parserSection == null) {
            player.sendMessage(ImageWings.trWithPrefix("command.edit.wing.meta.misses.parser.section", name))
            return CommandResult.SUCCESSFULLY_INVOKED
        }

        val imageParser: ImageParser
        try {
            imageParser = ImageParser(parserSection, image)
        } catch(e: IllegalArgumentException) {
            player.sendMessage(ImageWings.trWithPrefix("command.edit.wing.meta.corrupted", name, e.message ?: "&3&lCheck console&r"))
            return CommandResult.SUCCESSFULLY_INVOKED
        }


        // ====== DISPLAYER ======
        val armorStandLocation = player.location.clone()
        armorStandLocation.yaw = 0f
        armorStandLocation.pitch = 0f
        val armorStand: ArmorStand = player.world.spawnEntity(armorStandLocation, EntityType.ARMOR_STAND) as ArmorStand

        armorStand.setGravity(false)
        armorStand.isInvulnerable = true
        armorStand.helmet = ItemFactory.builder(Material.SKULL_ITEM)
                .setDurability(3.toShort())
                .setSkullOwner(player)
                .build()

        val displayer = Displayer(playerID = player.uniqueId, dummyEditor = ArmorStandEditor(imageParser, armorStand, vectorMultiplier), configPath = wingMetaPath)

        playerMap.put(player.uniqueId, displayer)

        displayer.runTaskTimer(ImageWings.instance, 0, 10)

        player.sendMessage(ImageWings.trWithPrefix("command.edit.editor.created", name))

        return CommandResult.SUCCESSFULLY_INVOKED
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        if (!playerMap.containsKey(event.player.uniqueId)) {
            return
        }
        playerMap[event.player.uniqueId]?.dummyEditor?.destroy()
    }

    private class Displayer(val playerID: UUID, val dummyEditor: DummyEditor, val configPath: Path) : BukkitRunnable() {

        override fun run() {

            val player = Bukkit.getPlayer(playerID)
            if (player == null || player.location.distance(dummyEditor.getDummyLocation()) > 100) {
                dummyEditor.destroy()
                cancel()
                return
            }

            dummyEditor.display()
        }
    }
}