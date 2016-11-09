package me.ialistannen.imagewings.wings

import com.perceivedev.perceivecore.particle.math.RotationMatrices
import org.bukkit.Material
import org.bukkit.entity.Entity
import java.util.*

/**
 * A wing to display
 */
class Wing(var points: MutableSet<ParticlePoint>, val playerVectorMultiplier: Double,
           val pitchRad: Double, val yawRadAddition: Double,
           val itemName: String, val itemMaterial: Material, val itemLore: List<String>,
           val permission: String) {

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Wing) return false

        if (points != other.points) return false
        if (playerVectorMultiplier != other.playerVectorMultiplier) return false
        if (pitchRad != other.pitchRad) return false
        if (yawRadAddition != other.yawRadAddition) return false
        if (itemName != other.itemName) return false
        if (itemMaterial != other.itemMaterial) return false
        if (itemLore != other.itemLore) return false
        if (permission != other.permission) return false

        return true
    }

    override fun hashCode(): Int {
        var result = points.hashCode()
        result = 31 * result + playerVectorMultiplier.hashCode()
        result = 31 * result + pitchRad.hashCode()
        result = 31 * result + yawRadAddition.hashCode()
        result = 31 * result + itemName.hashCode()
        result = 31 * result + itemMaterial.hashCode()
        result = 31 * result + itemLore.hashCode()
        result = 31 * result + permission.hashCode()
        return result
    }


}