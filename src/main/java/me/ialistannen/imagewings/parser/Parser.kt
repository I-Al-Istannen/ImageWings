package me.ialistannen.imagewings.parser

import com.udojava.evalex.Expression
import me.ialistannen.imagewings.ImageWings
import me.ialistannen.imagewings.wings.Wing
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO

/**
 * A parser
 */
class Parser(section: ConfigurationSection) {

    private val imagePath: Path

    val wing: Wing
    private val playerVectorMultiplier: Double
    private val pitchRad: Double
    private val yawRadAddition: Double

    init {
        // This will actually work. If you pass an absolute path, it will use the absolute, else it will make it relative
        imagePath = ImageWings.instance.dataFolder.toPath().resolve("images").resolve(ensureGetString(section, "image_path"))

        if (Files.notExists(imagePath)) {
            ImageWings.instance.logger.warning("The image file '${imagePath.toAbsolutePath()}' doesn't exist.")
            throw IllegalArgumentException("Image doesn't exist")
        }

        playerVectorMultiplier = ensureGetDouble(section, "player_vector_multiplier")

        pitchRad = Expression(ensureGetString(section, "pitch_rad")).eval().toDouble()
        yawRadAddition = Expression(ensureGetString(section, "yaw_rad_addition")).eval().toDouble()

        val itemName = ensureGetString(section, "item_name")
        val loreList = ensureGetStringList(section, "item_lore")
        val itemMaterial = Material.matchMaterial(ensureGetString(section, "item_material"))
                ?: throw IllegalArgumentException("Material '${ensureGetString(section, "item_material")}' is unknown.")

        val permission = ensureGetString(section, "permission")

        val image: BufferedImage

        try {
            image = ImageIO.read(imagePath.toFile())
        } catch (e: IOException) {
            ImageWings.instance.logger.warning("Couldn't read from file '${imagePath.toAbsolutePath()}'. Are you sure the image is valid. Jpg and png work," +
                    "others might")
            throw IllegalArgumentException("Image invalid", e)
        }

        val imageParser = ImageParser(section.getConfigurationSection("parser"), image)
        wing = Wing(imageParser.resultSet, playerVectorMultiplier, pitchRad, yawRadAddition, itemName, itemMaterial, loreList, permission)
    }
}

