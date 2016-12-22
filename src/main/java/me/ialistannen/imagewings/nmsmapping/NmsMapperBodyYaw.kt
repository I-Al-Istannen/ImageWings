package me.ialistannen.imagewings.nmsmapping

import com.perceivedev.perceivecore.reflection.ReflectionUtil
import me.ialistannen.imagewings.ImageWings
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.LivingEntity
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.regex.Pattern

/**
 * A wrapper for the yaw of the player body
 */
object NmsMapperBodyYaw {

    private val currentName: String?
    private val filePath = ImageWings.instance.dataFolder.toPath().resolve("mappings").resolve("mappings.yml")
    private var handleMethod: Method? = null
    private var yawField: Field? = null

    init {
        if (Files.notExists(filePath)) {
            Files.createDirectories(filePath.parent)
            Files.copy(ImageWings.instance.getResource("mappings/mappings.yml"), filePath)
        }
        val mapperReader = MapperReader(filePath)
        currentName = mapperReader.getName(ReflectionUtil.getMajorVersion(), ReflectionUtil.getMinorVersion(), ReflectionUtil.getPatchVersion())
    }

    /**
     * Returns the yaw of the body
     *
     * @param entity The entity to get the yaw for
     *
     * @return The yaw of the entities body or null
     */
    fun getBodyYaw(entity: LivingEntity): Float? {
        if (currentName == null) {
            return null
        }

        if (handleMethod == null) {
            handleMethod = entity.javaClass.getMethod("getHandle")
        }

        val handle = handleMethod!!.invoke(entity)

        if (yawField == null) {
            yawField = handle.javaClass.getField(currentName)
        }

        return 180 - yawField!!.getFloat(handle)
    }

    private class MapperReader(path: Path) {

        /**
         * ***Groups***:
         * 1. major from
         * 2. minor from
         * 3. patch from
         * 4. major to
         * 5. minor to
         * 6. patch to
         */
        private val MATCHER_REGEX: Pattern = Pattern.compile("""version (\d+)\.(\d+)\.(\d+) to (\d+)\.(\d+)\.(\d+) is (\w+)""", Pattern.CASE_INSENSITIVE)

        private val versionMap: MutableMap<Mapping, String> = HashMap()

        fun getName(major: Int, minor: Int, patch: Int): String? {
            return versionMap.filter { it.key.isInside(major, minor, patch) }.map { it.value }.asSequence().firstOrNull()
        }

        init {
            parseMapperFile(path)
        }

        private fun parseMapperFile(path: Path) {
            val configuration = YamlConfiguration.loadConfiguration(path.toFile())

            if (!configuration.isList("mappings")) {
                ImageWings.instance.logger.warning("mappings file misses key 'mappings' or it is not a list")
                return
            }

            for (mappingString in configuration.getStringList("mappings")) {
                val matcher = MATCHER_REGEX.matcher(mappingString)
                if (matcher.matches()) {
                    val mapping = Mapping(
                            majorRange = matcher.group(1).toInt()..matcher.group(4).toInt(),
                            minorRange = matcher.group(2).toInt()..matcher.group(5).toInt(),
                            patchRange = matcher.group(3).toInt()..matcher.group(6).toInt())

                    versionMap.put(mapping, matcher.group(7))
                } else {
                    ImageWings.instance.logger.warning("NmsMapperBodyYaw: Line '$mappingString' couldn't be parsed.")
                }
            }
        }


        private data class Mapping(val majorRange: IntRange, val minorRange: IntRange, val patchRange: IntRange) {

            fun isInside(major: Int, minor: Int, patch: Int): Boolean {
                return major in majorRange && minor in minorRange && patch in patchRange
            }
        }
    }
}