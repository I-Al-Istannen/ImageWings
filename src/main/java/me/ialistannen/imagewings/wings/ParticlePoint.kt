package me.ialistannen.imagewings.wings

import org.bukkit.Particle
import org.bukkit.util.Vector

/**
 * A point displayed by a given [Particle]
 */
data class ParticlePoint(val offset: Vector, val particle: Particle)
