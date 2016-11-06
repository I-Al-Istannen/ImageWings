package me.ialistannen.imagewings.wings

import com.perceivedev.perceivecore.particle.math.RotationMatrices
import org.bukkit.entity.Entity
import java.util.*

/**
 * A wing to display
 */
class Wing(var points: MutableSet<ParticlePoint>, val playerVectorMultiplier: Double,
           val pitchRad: Double, val yawRadAddition: Double) {

    init {
        points = HashSet(points)
    }


    /**
     * Displays this wing for a [Entity]
     *
     * @param entity The [Entity] to display it for
     */
    fun display(entity: Entity) {
        val yawRad = Math.toRadians(entity.location.yaw.toDouble()) + yawRadAddition
        val center = entity.location
        run {
            val addingVector = center.direction.clone().multiply(playerVectorMultiplier)
            addingVector.setY(0)
            center.add(addingVector)
        }
        val world = center.world

        for ((offset, particle) in points) {
            val rotatedOffset = RotationMatrices.rotateRadian(offset, yawRad, pitchRad)

            world.spawnParticle(particle, center.clone().add(rotatedOffset), 1, 0.0, 0.0, 0.0, 0.0)
        }
    }
}