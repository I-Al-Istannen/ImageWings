package me.ialistannen.imagewings.display

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.perceivedev.perceivecore.time.DurationParser
import me.ialistannen.imagewings.ImageWings
import me.ialistannen.imagewings.wings.Wing
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Level

/**
 * Displays player's wings
 */
class WingDisplayManager {
    private val playerWingsMap: MutableMap<UUID, PlayerWing> = HashMap()
    private val wings: MutableCollection<Wing> = HashSet()
    private var runner: BukkitRunnable? = null

    init {
        startRunner()
    }

    /**
     * Adds a wing
     *
     * @param wing The wing to add
     */
    fun addWing(wing: Wing) {
        wings.add(wing)
    }

    /**
     * Removes a wing
     *
     * @param wing The [Wing] to remove
     */
    fun removeWing(wing: Wing) {
        wings.remove(wing)
    }

    /**
     * Gives the player the wing
     *
     * @param player The player to add thr wing to
     * @param wing The [Wing] to add
     */
    fun addPlayer(player: Player, wing: Wing) {
        playerWingsMap.put(player.uniqueId, PlayerWing(wing))

        if (runner == null) {
            startRunner()
        }
    }

    /**
     * Removes the wing for the given player
     *
     * @param player The player to remove the wings for
     */
    fun removePlayer(player: Player) {
        playerWingsMap.remove(player.uniqueId)
        if (playerWingsMap.isEmpty()) {
            stopRunner()
        }
    }

    /**
     * Returns the wing for the player
     *
     * @param player The wing for the player
     *
     * @return The [Wing] of the player or null if none
     */
    fun getPlayerWing(player: Player): Wing? {
        val playerWing = playerWingsMap[player.uniqueId]
        if (playerWing == null || !wings.contains(playerWing.wing)) {
            return null
        }
        return playerWing.wing
    }

    /**
     * @return All the wings
     */
    fun getAllWings(): Collection<Wing> {
        return Collections.unmodifiableCollection(wings)
    }


    private fun startRunner() {
        if (runner != null) {
            stopRunner()
        }
        runner = object : BukkitRunnable() {
            override fun run() {
                tick()
            }
        }
        val delayString = ImageWings.instance.config.getString("wing_display_delay")

        val delayTicks: Long
        try {
            delayTicks = DurationParser.parseDurationToTicks(delayString)
        } catch (e: RuntimeException) {
            ImageWings.instance.logger.log(Level.WARNING, "Couldn't parse duration: 'wing_display_delay'", e)
            return
        }

        runner?.runTaskTimer(ImageWings.instance, 0, delayTicks)
    }

    private fun stopRunner() {
        if (runner == null) {
            return
        }
        runner!!.cancel()
        runner = null
    }

    private val cache: Cache<UUID, Location> = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.SECONDS)
            .build()

    private fun tick() {
        val iterator = playerWingsMap.iterator()
        for ((uuid) in iterator) {
            val player: Player? = Bukkit.getPlayer(uuid)
            if (player == null) {
                iterator.remove()
                continue
            }

            val oldLocation = cache.getIfPresent(uuid)
            cache.put(uuid, player.location)

            if (oldLocation != null) {
                if (oldLocation.distance(player.location) > 0.5) {
                    continue
                }
            }

            val wing = getPlayerWing(player)

            if (wing == null) {
                iterator.remove()
            } else {
                wing.display(player)
            }
        }
    }


    private data class PlayerWing(var wing: Wing)
}