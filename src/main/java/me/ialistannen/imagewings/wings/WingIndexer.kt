package me.ialistannen.imagewings.wings

import me.ialistannen.imagewings.ImageWings
import me.ialistannen.imagewings.display.WingDisplayManager
import me.ialistannen.imagewings.parser.Parser
import org.bukkit.configuration.file.YamlConfiguration
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.logging.Level

/**
 * Indexes all wings from a folder
 */
class WingIndexer(val path: Path) {

    /**
     * Reads the folder and sub dirs and indexes the [Wing]s
     *
     * @param wingDisplayManager The [WingDisplayManager] to add the wings too
     *
     * @return False if an error occurred
     */
    fun index(wingDisplayManager: WingDisplayManager): Boolean {
        if (Files.notExists(path)) {
            return true
        }

        var result: Boolean = true

        Files.walkFileTree(path, object : FileVisitor<Path> {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                return FileVisitResult.CONTINUE
            }

            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                val fileName: String = file.fileName.toString()

                if (!fileName.endsWith(".wingMeta")) {
                    return FileVisitResult.CONTINUE
                }

                try {
                    val parser = Parser(YamlConfiguration.loadConfiguration(file.toFile()))
                    wingDisplayManager.addWing(parser.wing, fileName.replace(".wingMeta", ""))
                } catch (e: IllegalArgumentException) {
                    ImageWings.instance.logger.log(Level.WARNING, "Couldn't parse file '$fileName'", e)
                    result = false
                }

                return FileVisitResult.CONTINUE
            }

            override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
                ImageWings.instance.logger.log(Level.WARNING, "Couldn't index file '${file.fileName}'", exc)
                result = false
                return FileVisitResult.CONTINUE
            }

            override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                return FileVisitResult.CONTINUE
            }
        })

        return result
    }
}