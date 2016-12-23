package me.ialistannen.imagewings.parser

import me.ialistannen.imagewings.ImageWings
import me.ialistannen.imagewings.wings.AnimatedWing
import me.ialistannen.imagewings.wings.AnimatedWingFrame
import me.ialistannen.imagewings.wings.Wing
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
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

        pitchRad = ensureGetString(section, "pitch_rad")
                .replace("PI", Math.PI.toString())
                .toDouble()
        yawRadAddition = ensureGetString(section, "yaw_rad_addition")
                .replace("PI", Math.PI.toString())
                .toDouble()

        val itemName = ensureGetString(section, "item_name")
        val loreList = ensureGetStringList(section, "item_lore")
        val itemMaterial = Material.matchMaterial(ensureGetString(section, "item_material"))
                ?: throw IllegalArgumentException("Material '${ensureGetString(section, "item_material")}' is unknown.")

        val permission = ensureGetString(section, "permission")

        val type = ensureGetString(section, "parser_type")

        when (type) {
            "normal" -> {
                wing = parseNormal(section,
                        playerVectorMultiplier, pitchRad, yawRadAddition,
                        itemName, itemMaterial, loreList,
                        permission)
            }
            "animated" -> {
                wing = parseAnimated(section,
                        playerVectorMultiplier, pitchRad, yawRadAddition,
                        itemName, itemMaterial, loreList,
                        permission)
            }
            else -> {
                throw IllegalArgumentException("Parser type not known: '$type'!")
            }
        }
    }

    private fun parseAnimated(section: ConfigurationSection,
                              playerVectorMultiplier: Double,
                              pitchRad: Double, yawRadAddition: Double,
                              itemName: String, itemMaterial: Material, loreList: List<String>,
                              permission: String): Wing {

        val frames: LinkedList<AnimatedWingFrame> = LinkedList()
        val parserSection = section.getConfigurationSection("parser")

        val imageReader = ImageIO.getImageReadersByFormatName("gif").next()
        ImageIO.createImageInputStream(imagePath.toFile()).use {
            imageReader.input = it

            val readFrames = GifParser().readGIF(imageReader)

            for ((readImage, delay) in readFrames) {
                val imageParser = ImageParser(parserSection, readImage)

                val delayTime = delay * 10L

                frames.add(AnimatedWingFrame(imageParser.resultSet, delayTime))
            }
        }

        return AnimatedWing(frames,
                playerVectorMultiplier, pitchRad, yawRadAddition,
                itemName, itemMaterial, loreList,
                permission)
    }

    private fun parseNormal(section: ConfigurationSection,
                            playerVectorMultiplier: Double,
                            pitchRad: Double, yawRadAddition: Double,
                            itemName: String, itemMaterial: Material, loreList: List<String>,
                            permission: String): Wing {

        val image: BufferedImage

        try {
            image = ImageIO.read(imagePath.toFile())
        } catch (e: IOException) {
            ImageWings.instance.logger.warning("Couldn't read from file '${imagePath.toAbsolutePath()}'." +
                    " Are you sure the image is valid. Jpg and png work, others might")
            throw IllegalArgumentException("Image invalid", e)
        }

        val imageParser = ImageParser(section.getConfigurationSection("parser"), image)
        return Wing(imageParser.resultSet, playerVectorMultiplier, pitchRad, yawRadAddition, itemName, itemMaterial, loreList, permission)
    }
}

