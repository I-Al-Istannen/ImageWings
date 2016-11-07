package me.ialistannen.imagewings.interactiveeditor

import me.ialistannen.imagewings.ImageWings
import me.ialistannen.imagewings.parser.ColourMapper
import me.ialistannen.imagewings.parser.ImageParser
import me.ialistannen.imagewings.wings.Wing
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.EquipmentSlot
import java.awt.image.BufferedImage
import java.util.function.BiConsumer

/**
 * A dummy ArmorStand to review Wing changes
 */
class ArmorStandEditor(imageParser: ImageParser,
                       private val armorStand: ArmorStand) : DummyEditor {

    private var wing: Wing? = null

    private val image: BufferedImage = imageParser.image
    private val colourMapper: ColourMapper = imageParser.colourMapper

    private var xOffset: Double = imageParser.xOffsetAbsolute
    private var yOffset: Double = imageParser.yOffsetAbsolute

    private var xGranularity: Int = imageParser.xGranularity
    private var yGranularity: Int = imageParser.yGranularity

    private var xScale: Double = imageParser.xScale
    private var yScale: Double = imageParser.yScale

    private var vectorMultiplier: Double = 0.01

    private val myListener = MyListener(this)

    init {
        Bukkit.getPluginManager().registerEvents(this, ImageWings.instance)
        Bukkit.getPluginManager().registerEvents(myListener, ImageWings.instance)
        update()
    }

    //<editor-fold desc="Setter methods and updating">
    override fun setXOffset(offset: Double) {
        xOffset = offset
        update()
    }

    override fun getXOffset() = xOffset

    override fun setYOffset(offset: Double) {
        yOffset = offset
        update()
    }

    override fun getYOffset() = yOffset

    override fun setXScale(scale: Double) {
        xScale = scale
        update()
    }

    override fun getXScale() = xScale

    override fun setYScale(scale: Double) {
        yScale = scale
        update()
    }

    override fun getYScale() = yScale


    override fun setXGranularity(granularity: Int) {
        if (granularity <= 0) {
            xGranularity = 1
            return
        }
        xGranularity = granularity
        update()
    }

    override fun getXGranularity() = xGranularity

    override fun setYGranularity(granularity: Int) {
        if (granularity <= 0) {
            yGranularity = 1
            return
        }
        yGranularity = granularity
        update()
    }

    override fun getYGranularity() = yGranularity


    override fun setPlayerVectorMultiplier(multiplier: Double) {
        vectorMultiplier = multiplier
        update()
    }

    override fun getPlayerVectorMultiplier() = vectorMultiplier

    override fun getDummyLocation(): Location = armorStand.location

    override fun display() {
        if (!armorStand.isValid) {
            return
        }
        wing?.display(armorStand)
    }

    private fun update() {
        val parser = ImageParser(xScale, yScale, xOffset, yOffset, xGranularity, yGranularity, colourMapper, image)

        val resultSet = parser.resultSet

        wing = Wing(resultSet, vectorMultiplier, Math.PI, 0.0)
    }

    override fun destroy() {
        armorStand.remove()
        HandlerList.unregisterAll(myListener)
        HandlerList.unregisterAll(this)
    }

    override fun saveToConfig(config: ConfigurationSection) {

        config.set("player_vector_multiplier", getPlayerVectorMultiplier())

        val parserSection = config.getConfigurationSection("parser")
        parserSection.set("xScale", getXScale())
        parserSection.set("yScale", getYScale())

        parserSection.set("xOffsetAbsolute", getXOffset())
        parserSection.set("yOffsetAbsolute", getYOffset())

        parserSection.set("xGranularity", getXGranularity())
        parserSection.set("yGranularity", getYGranularity())
    }
    //</editor-fold>

    //##############################################
    //#                    EVENTS                  #
    //##############################################


    @EventHandler
    fun onArmorStandDamage(damageEvent: EntityDamageEvent) {
        if (damageEvent.entity == armorStand) {
            damageEvent.isCancelled = true
        }
    }

    private class MyListener(private val editor: DummyEditor) : Listener {
        private var currentState = States.X_OFFSET

        @EventHandler
        fun onInteract(interactEvent: PlayerInteractEvent) {
            if (interactEvent.hand == EquipmentSlot.HAND) {
                currentState.apply(interactEvent, editor)
            }
        }

        @EventHandler
        fun onScroll(changeSlot: PlayerItemHeldEvent) {
            var newOrdinal: Int

            if (changeSlot.newSlot > changeSlot.previousSlot
                    || changeSlot.newSlot == 0 && changeSlot.previousSlot == 8) {

                newOrdinal = currentState.ordinal + 1

                if (newOrdinal >= States.values().size) {
                    newOrdinal = 0
                }

            } else {
                newOrdinal = currentState.ordinal - 1
                if (newOrdinal < 0) {
                    newOrdinal = States.values().lastIndex
                }
            }

            currentState = States.values()[newOrdinal]
            changeSlot.player.sendMessage(ImageWings.trWithPrefix("editing.${currentState.name.toLowerCase().replace("_", ".")}"))
        }
    }

    private enum class States(val consumer: BiConsumer<PlayerInteractEvent, DummyEditor>) {
        X_OFFSET(BiConsumer { event, editor ->
            editor.setXOffset(editor.getXOffset() + event.getChangeAmount(10.0, 1.0))
            event.player.sendMessage(ImageWings.trWithPrefix("set.x.offset", editor.getXOffset()))
        }),
        Y_OFFSET(BiConsumer { event, editor ->
            editor.setYOffset(editor.getYOffset() + event.getChangeAmount(10.0, 1.0))
            event.player.sendMessage(ImageWings.trWithPrefix("set.y.offset", editor.getYOffset()))
        }),

        X_SCALE(BiConsumer { event, editor ->
            editor.setXScale(editor.getXScale() + event.getChangeAmount(0.1, 0.01))
            event.player.sendMessage(ImageWings.trWithPrefix("set.x.scale", editor.getXScale()))
        }),
        Y_SCALE(BiConsumer { event, editor ->
            editor.setYScale(editor.getYScale() + event.getChangeAmount(0.1, 0.01))
            event.player.sendMessage(ImageWings.trWithPrefix("set.y.scale", editor.getYScale()))
        }),

        X_GRANULARITY(BiConsumer { event, editor ->
            editor.setXGranularity(editor.getXGranularity() + event.getChangeAmount(10.0, 1.0).toInt())
            event.player.sendMessage(ImageWings.trWithPrefix("set.x.granularity", editor.getXGranularity()))
        }),
        Y_GRANULARITY(BiConsumer { event, editor ->
            editor.setYGranularity(editor.getYGranularity() + event.getChangeAmount(10.0, 1.0).toInt())
            event.player.sendMessage(ImageWings.trWithPrefix("set.y.granularity", editor.getYGranularity()))
        }),

        VECTOR_MULTIPLIER(BiConsumer { event, editor ->
            editor.setPlayerVectorMultiplier(editor.getPlayerVectorMultiplier() + event.getChangeAmount(1.0, 0.1))
            event.player.sendMessage(ImageWings.trWithPrefix("set.vector.multiplier", editor.getPlayerVectorMultiplier()))
        });

        fun apply(interactEvent: PlayerInteractEvent, dummyEditor: DummyEditor) {
            consumer.accept(interactEvent, dummyEditor)
        }
    }
}

private fun PlayerInteractEvent.getChangeAmount(normal: Double, sneak: Double): Double {
    var changeAmount = normal
    if (player.isSneaking) {
        changeAmount = sneak
    }

    if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
        changeAmount *= -1
    }
    return changeAmount
}