package me.ialistannen.imagewings.parser

import me.ialistannen.imagewings.ImageWings
import org.bukkit.Particle
import java.util.*
import java.util.regex.Pattern

/**
 * Maps pixel colour to a particle type
 */
class ColourMapper(lines: Collection<String>) {

    private val pattern = Pattern.compile("""(\d{1,3},\d{1,3},\d{1,3}) to (\d{1,3},\d{1,3},\d{1,3}) is (\w+)""", Pattern.CASE_INSENSITIVE)

    private var colourMap: MutableMap<ColourRange, Particle> = HashMap()

    init {
        for (string in lines) {
            val matcher = pattern.matcher(string)

            if (!matcher.matches()) {
                ImageWings.instance.logger.warning("The line '$string' doesn't follow the format!")
                continue
            }
            val min = matcher.group(1).toTriple(",")
            val max = matcher.group(2).toTriple(",")
            val particleName = matcher.group(3)
            var particle: Particle
            try {
                particle = Particle.valueOf(particleName)
            } catch (e: IllegalArgumentException) {
                ImageWings.instance.logger.warning("Particle '$particleName' not found!")
                continue
            }

            colourMap.put(ColourRange(min, max), particle)
        }
    }

    /**
     * The particle for the given rgb value
     *
     * @param rgb The [Triple] containing Red, Green and Blue
     *
     * @return The Particle or null if none
     */
    fun getParticle(rgb: Triple<Int,Int,Int>): Particle? {
        return colourMap.filter { it.key.contains(rgb) }.asSequence().map { it.value }.firstOrNull()
    }

    private fun String.toTriple(delimiter: String): Triple<Int, Int, Int> {
        val split = this.split(delimiter)
        if (split.size != 3) {
            return Triple(-1, -1, -1)
        }
        return Triple(split[0].toInt(), split[1].toInt(), split[2].toInt())
    }
}

data class ColourRange(val min: Triple<Int, Int, Int>, val max: Triple<Int, Int, Int>) {

    operator fun contains(value: Triple<Int, Int, Int>): Boolean {
        return (value.first >= min.first && value.second >= min.second && value.third >= min.third)
                && (value.first <= max.first && value.second <= max.second && value.third <= max.third)
    }
}