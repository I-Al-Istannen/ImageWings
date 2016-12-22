package me.ialistannen.imagewings.wings

import org.bukkit.Location
import org.bukkit.Material

/**
 * An animated Wing
 */
class AnimatedWing(val frames: List<AnimatedWingFrame>,
                   playerVectorMultiplier: Double,
                   pitchRad: Double,
                   yawRadAddition: Double,
                   itemName: String,
                   itemMaterial: Material,
                   itemLore: List<String>,
                   permission: String)
    : Wing(emptyList<ParticlePoint>().toMutableSet(),
        playerVectorMultiplier,
        pitchRad, yawRadAddition,
        itemName, itemMaterial, itemLore,
        permission) {

    private var currentFrame: Int = 0
    private var passedTime: Long = 0L
    private var lastTickTime: Long = System.currentTimeMillis()

    override fun display(center: Location, yawDegree: Double) {
        passedTime += System.currentTimeMillis() - lastTickTime

        val wingFrame = frames[currentFrame]

        // set the points to the current frame
        points = wingFrame.points.toMutableSet()

        // relay displaying to super class
        super.display(center, yawDegree)

        if (passedTime > getDelayMillis()) {
            if (currentFrame >= frames.lastIndex) {
                currentFrame = 0
            } else {
                currentFrame++
            }
            passedTime = 0
        }

        lastTickTime = System.currentTimeMillis()
    }

    /**
     * The delay until the next image should be shown in Milliseconds
     */
    fun getDelayMillis(): Long {
        return frames[currentFrame].delay
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AnimatedWing) return false
        if (!super.equals(other)) return false

        if (frames != other.frames) return false

        return true
    }

    override fun hashCode(): Int {
        val result = 31 * frames.hashCode()
        return result
    }

}

/**
 * The data for one animated frame
 *
 * @param points The [ParticlePoint]s to display
 * @param delay The delay until the next one is shown in Milliseconds
 */
data class AnimatedWingFrame(val points: Set<ParticlePoint>, val delay: Long)