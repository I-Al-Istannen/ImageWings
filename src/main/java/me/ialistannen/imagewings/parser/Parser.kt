package me.ialistannen.imagewings.parser

import com.udojava.evalex.Expression
import me.ialistannen.imagewings.ImageWings
import me.ialistannen.imagewings.wings.Wing
import org.bukkit.configuration.ConfigurationSection
import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
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
        imagePath = Paths.get(ensureGetString(section, "image_path"))

        if (Files.notExists(imagePath)) {
            ImageWings.instance.logger.warning("The image file '${imagePath.toAbsolutePath()}' doesn't exist.")
            throw IllegalArgumentException("Image doesn't exist")
        }

        playerVectorMultiplier = ensureGetDouble(section, "player_vector_multiplier")

        pitchRad = Expression(ensureGetString(section, "pitch_rad")).eval().toDouble()
        yawRadAddition = Expression(ensureGetString(section, "yaw_rad_addition")).eval().toDouble()

        val image: BufferedImage

        try {
            image = ImageIO.read(imagePath.toFile())
        } catch (e: IOException) {
            ImageWings.instance.logger.warning("Couldn't read from file '${imagePath.toAbsolutePath()}'. Are you sure the image is valid. Jpg and png work," +
                    "others might")
            throw IllegalArgumentException("Image invalid", e)
        }

        val imageParser = ImageParser(section.getConfigurationSection("parser"), image)
        wing = Wing(imageParser.resultSet, playerVectorMultiplier, pitchRad, yawRadAddition)
    }
}

