package me.ialistannen.imagewings.parser

import com.perceivedev.perceivecore.utilities.item.ItemFactory
import me.ialistannen.imagewings.ImageWings
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.material.MaterialData
import java.util.*
import java.util.logging.Level
import java.util.regex.Pattern

/**
 * Maps pixel colour to a particle type
 */
class ColourMapper(lines: Collection<String>) {

    private val basePattern = Pattern.compile("""(\d{1,3},\d{1,3},\d{1,3}) to (\d{1,3},\d{1,3},\d{1,3}) is (\w+)""", Pattern.CASE_INSENSITIVE)

    private var colourMap: MutableMap<ColourRange, ParticleData> = HashMap()

    init {
        for (string in lines) {
            val matcher = basePattern.matcher(string)

            if (!matcher.find()) {
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

            val specialParticles = StandardSpecialParticles.getForParticle(particle)
            if (specialParticles == null) {
                colourMap.put(ColourRange(min, max), ParticleData(particle, null))
                continue
            }

            if (string.lastIndex == matcher.regionEnd()) {
//                ImageWings.instance.logger.warning("The line '$string' needs some more data. The particle is special!")
                colourMap.put(ColourRange(min, max), ParticleData(particle, null))
                continue
            }

            val cutString = string.substring(matcher.group().length)

            val specialParticle: SpecialParticle?
            try {
                specialParticle = specialParticles.parse(cutString, particle)
            } catch (e: Exception) {
                ImageWings.instance.logger.log(Level.WARNING, "The line '$string' contained invalid data for the special particle", e)
                continue
            }

            if (specialParticle == null) {
                ImageWings.instance.logger.warning("The line '$string' contained invalid data for the special particle")
                continue
            }

            colourMap.put(ColourRange(min, max), ParticleData(particle, specialParticle))
        }
    }

    /**
     * The particle for the given rgb value
     *
     * @param rgb The [Triple] containing Red, Green and Blue
     *
     * @return The Particle or null if none
     */
    fun getParticle(rgb: Triple<Int, Int, Int>): ParticleData? {
        return colourMap.filter { it.key.contains(rgb) }.asSequence().map { it.value }.firstOrNull()
    }
}

data class ColourRange(val min: Triple<Int, Int, Int>, val max: Triple<Int, Int, Int>) {

    operator fun contains(value: Triple<Int, Int, Int>): Boolean {
        return (value.first >= min.first && value.second >= min.second && value.third >= min.third)
                && (value.first <= max.first && value.second <= max.second && value.third <= max.third)
    }
}

private fun String.toTriple(delimiter: String): Triple<Int, Int, Int> {
    val split = this.split(delimiter)
    if (split.size != 3) {
        return Triple(-1, -1, -1)
    }
    return Triple(split[0].toInt(), split[1].toInt(), split[2].toInt())
}


data class ParticleData(val particle: Particle, val specialParticle: SpecialParticle?) {

    fun display(location: Location) {
        if (specialParticle != null) {
            specialParticle.display(location)
        } else {
            location.world.spawnParticle(particle, location,
                    1, // count
                    0.0, // xOff
                    0.0, // yOff
                    0.0, // zOff
                    0.0) // speed
        }
    }
}

/**
 * A special particle
 */
interface SpecialParticle {
    fun display(location: Location)
}

enum class StandardSpecialParticles(val particles: Set<Particle>) {
    COLOURED_DUST(EnumSet.of(Particle.REDSTONE, Particle.SPELL_MOB, Particle.SPELL_MOB_AMBIENT)) {
        override fun parse(data: String, particle: Particle): SpecialParticle? {
            val rgb = data
                    .replace("with rgb ", "")
                    .trim()
                    .toTriple(",")

            val speed = 1.0
            val count = 0
            val xOffset: Double = if (rgb.first == 0) 0.01 else rgb.first / 255.0
            val yOffset: Double = rgb.second / 255.0
            val zOffset: Double = rgb.third / 255.0

            return object : SpecialParticle {
                override fun display(location: Location) {
                    location.world.spawnParticle(particle, location,
                            count,
                            xOffset,
                            yOffset,
                            zOffset,
                            speed)
                }
            }
        }
    },
    COLOURED_NOTE(EnumSet.of(Particle.NOTE)) {
        override fun parse(data: String, particle: Particle): SpecialParticle? {
            val noteIndex = data
                    .replace("with note index ", "")
                    .trim()
                    .toShort()

            val xOffset: Double = noteIndex / 25.0

            return object : SpecialParticle {
                override fun display(location: Location) {
                    location.world.spawnParticle(particle, location,
                            0,
                            xOffset,
                            0.0,
                            0.0,
                            1.0)
                }
            }
        }
    },
    MATERIAL_DATA(EnumSet.of(Particle.BLOCK_DUST, Particle.BLOCK_CRACK)) {
        private val PATTERN = Pattern.compile("""as (\w+):(\d{1,5})""", Pattern.CASE_INSENSITIVE)

        override fun parse(data: String, particle: Particle): SpecialParticle? {
            val matcher = PATTERN.matcher(data.trim())

            if (!matcher.matches()) {
                throw IllegalArgumentException("Not enough data given or in wrong format." +
                        " Correct: \"as <MATERIAL>:<DURABILITY>\"" +
                        "  Given: \"$data\"")
            }

            val material = Material.matchMaterial(
                    matcher.group(1)
            ) ?: return null

            val dataByte = matcher.group(2).toByte()

            @Suppress("DEPRECATION")
            val materialData = MaterialData(material, dataByte)

            return object : SpecialParticle {
                override fun display(location: Location) {
                    location.world.spawnParticle(particle, location,
                            1,
                            0.0,
                            0.0,
                            0.0,
                            0.0,
                            materialData)
                }
            }
        }
    },
    ITEM_DATA(EnumSet.of(Particle.ITEM_CRACK)) {
        private val PATTERN = Pattern.compile("""as (\w+):(\d{1,5})""", Pattern.CASE_INSENSITIVE)

        override fun parse(data: String, particle: Particle): SpecialParticle? {
            val matcher = PATTERN.matcher(data.trim())

            if (!matcher.matches()) {
                throw IllegalArgumentException("Not enough data given or in wrong format." +
                        " Correct: \"<MATERIAL>:<DURABILITY>\"" +
                        "  Given: \"$data\"")
            }

            val material = Material.matchMaterial(
                    matcher.group(1)
            ) ?: return null

            val durability = matcher.group(2).toShort()

            val item = ItemFactory.builder(material).setDurability(durability).build()

            return object : SpecialParticle {
                override fun display(location: Location) {
                    location.world.spawnParticle(particle, location,
                            1,
                            0.0,
                            0.0,
                            0.0,
                            0.0,
                            item)
                }
            }
        }
    };

    abstract fun parse(data: String, particle: Particle): SpecialParticle?

    companion object {
        /**
         * A [SpecialParticle] for the given Bukkit [Particle]
         *
         * @return The [SpecialParticle] or null if not found
         */
        fun getForParticle(particle: Particle): StandardSpecialParticles? {
            return values().filter { it.particles.contains(particle) }.asSequence().firstOrNull()
        }
    }
}