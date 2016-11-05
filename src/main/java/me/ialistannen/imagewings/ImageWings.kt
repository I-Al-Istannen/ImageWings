package me.ialistannen.imagewings

import me.ialistannen.imagewings.parser.Parser
import me.ialistannen.imagewings.wings.Wing
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
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

/**
 * The main class
 */
class ImageWings : JavaPlugin() {

    companion object {
        lateinit var instance: ImageWings
    }

    override fun onEnable() {
        dataFolder.mkdirs()
        
        instance = this

        loadData()
    }

    private fun loadData() {
        val images = ArrayList<Wing>()
        
        val path = dataFolder.toPath().resolve("images")
        
        if(Files.notExists(path)) {
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
}