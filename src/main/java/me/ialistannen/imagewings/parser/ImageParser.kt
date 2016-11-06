package me.ialistannen.imagewings.parser

import me.ialistannen.imagewings.wings.ParticlePoint
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.util.Vector
import java.awt.image.BufferedImage
import java.util.*

/**
 * An image parser
 */
class ImageParser(var xScale: Double, var yScale: Double,
                  var xOffsetAbsolute: Double, var yOffsetAbsolute: Double,
                  var xGranularity: Int, var yGranularity: Int,
                  var colourMapper: ColourMapper,
                  val image: BufferedImage) {

    /**
     * All points in this image.
     * Do not modify it
     */
    val resultSet: MutableSet<ParticlePoint> = HashSet()

    constructor(section: ConfigurationSection, image: BufferedImage) : this(
            xScale = ensureGetDouble(section, "xScale"), yScale = ensureGetDouble(section, "yScale"),
            xOffsetAbsolute = ensureGetDouble(section, "xOffsetAbsolute"), yOffsetAbsolute = ensureGetDouble(section, "yOffsetAbsolute"),
            xGranularity = ensureGetInt(section, "xGranularity"), yGranularity = ensureGetInt(section, "yGranularity"),
            colourMapper = ColourMapper(ensureGetStringList(section, "colourMapper")),
            image = image) {
    }

    init {
        parse()
    }

    private fun parse() {
        val width = image.width
        val height = image.height

        for (x in 0..width - 1 step xGranularity) {
            for (y in 0..height - 1 step yGranularity) {
                val xScaled = (x - xOffsetAbsolute) * xScale
                val yScaled = (y - yOffsetAbsolute) * yScale

                val particle = colourMapper.getParticle(image.getRGB(x, y).getRgb()) ?: continue

                resultSet.add(ParticlePoint(Vector(xScaled, yScaled, 0.0), particle))
            }
        }
    }

    /**
     * Converts an RGB value in ONE int to three RGB ints
     *
     * @return The triple with the colours
     */
    private fun Int.getRgb(): Triple<Int, Int, Int> {
        return Triple(this and 0x00ff0000 shr 16, this and 0x0000ff00 shr 8, this and 0x000000ff)
    }
}