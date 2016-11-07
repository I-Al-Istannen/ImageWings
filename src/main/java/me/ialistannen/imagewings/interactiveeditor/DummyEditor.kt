package me.ialistannen.imagewings.interactiveeditor

import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.event.Listener

/**
 * A dummy to display the things on
 */
interface DummyEditor : Listener {

    fun setXOffset(offset: Double)
    fun getXOffset(): Double

    fun setYOffset(offset: Double)
    fun getYOffset(): Double


    fun setXScale(scale: Double)
    fun getXScale(): Double

    fun setYScale(scale: Double)
    fun getYScale(): Double


    fun setXGranularity(granularity: Int)
    fun getXGranularity(): Int

    fun setYGranularity(granularity: Int)
    fun getYGranularity(): Int


    fun setPlayerVectorMultiplier(multiplier: Double)
    fun getPlayerVectorMultiplier(): Double

    fun getDummyLocation(): Location

    fun display()

    fun destroy()

    fun saveToConfig(config: ConfigurationSection)
}