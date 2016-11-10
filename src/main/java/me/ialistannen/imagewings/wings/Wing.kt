package me.ialistannen.imagewings.wings

import com.perceivedev.perceivecore.particle.math.RotationMatrices
import me.ialistannen.imagewings.nmsmapping.NmsMapperBodyYaw
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
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
    fun display(entity: LivingEntity) {
        val bodyYawDegree = NmsMapperBodyYaw.getBodyYaw(entity) ?: return

        val yawRad = Math.toRadians(bodyYawDegree.toDouble()) + yawRadAddition

        val center = entity.location
        run {
            // inefficient code: 
            val cloneCenter = center.clone()
            cloneCenter.yaw = Math.toDegrees(yawRad).toFloat() * -1 // do not ask me why it is flipped. PLEASE

            val addingVector = cloneCenter.direction.setY(0).normalize().multiply(playerVectorMultiplier)
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