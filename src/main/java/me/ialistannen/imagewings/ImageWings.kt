package me.ialistannen.imagewings

import com.perceivedev.perceivecore.language.I18N
import me.ialistannen.imagewings.interactiveeditor.ArmorStandEditor
import me.ialistannen.imagewings.parser.ImageParser
import me.ialistannen.imagewings.parser.Parser
import me.ialistannen.imagewings.wings.Wing
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.logging.Level
import javax.imageio.ImageIO

/**
 * The main class
 */
class ImageWings : JavaPlugin() {

    companion object {
        lateinit var instance: ImageWings
            private set
        lateinit var language: I18N
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

        // loadData()
    }

    private fun loadData() {
        val images = ArrayList<Wing>()

        val path = dataFolder.toPath().resolve("images")

        if (Files.notExists(path)) {
            println("No folder!")
            return
        }

        Files.walkFileTree(path, object : FileVisitor<Path> {
            override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
                if (!file.fileName.toString().endsWith(".wingMeta")) {
                    println("NO: '${file.fileName}'")
                    return FileVisitResult.CONTINUE
                }

                val parser = Parser(YamlConfiguration.loadConfiguration(file.toFile()))

                images.add(parser.wing)

                return FileVisitResult.CONTINUE
            }

            override fun visitFileFailed(file: Path, exc: IOException?): FileVisitResult {
                logger.log(Level.WARNING, "Error reading file '$file'", exc)

                return FileVisitResult.CONTINUE
            }

            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes?): FileVisitResult {
                return FileVisitResult.CONTINUE
            }

            override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                return FileVisitResult.CONTINUE
            }
        })

        println("Loaded ${images.size} wings")

        object : BukkitRunnable() {
            var counter = 0

            override fun run() {
                counter++
                if (counter > 20) {
                    cancel()
                }
                for (wing in images) {
                    Bukkit.getOnlinePlayers().forEach { wing.display(it) }
                }
            }
        }.runTaskTimer(this, 0, 10)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name != "testCommand") {
            return true
        }

        val config = YamlConfiguration.loadConfiguration(dataFolder.toPath().resolve("images").resolve("coolwong.wingMeta").toFile())

        val image = ImageIO.read(dataFolder.toPath().resolve(config.getString("image_path")).toFile())

        val armorStand: ArmorStand = (sender as Player).world.spawnEntity(sender.location, EntityType.ARMOR_STAND) as ArmorStand

        val imageParser = ImageParser(config.getConfigurationSection("parser"), image)

        val editor: ArmorStandEditor = ArmorStandEditor(imageParser, armorStand)

        object : BukkitRunnable() {
            override fun run() {
                editor.display()
            }
        }.runTaskTimer(this, 0, 10)
        return true
    }
}