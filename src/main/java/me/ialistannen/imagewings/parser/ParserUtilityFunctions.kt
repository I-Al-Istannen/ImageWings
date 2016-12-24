package me.ialistannen.imagewings.parser

import org.bukkit.configuration.ConfigurationSection

/**
 * Contains some utility functions for the parser
 */

/**
 * Returns the String from the [ConfigurationSection], if any. Throws an exception if an error occurred.
 *
 * @param key The key
 * @param section The [ConfigurationSection] to get it from
 *
 * @throws IllegalArgumentException if the key is missing or not a String
 */
fun ensureGetString(section: ConfigurationSection, key: String): String {
    if (!section.isString(key)) {
        throw IllegalArgumentException("Section '${section.currentPath}' misses key '$key' or it is not a String")
    }
    return section.getString(key)
}

/**
 * Returns the Double from the [ConfigurationSection], if any. Throws an exception if an error occurred.
 *
 * @param key The key
 * @param section The [ConfigurationSection] to get it from
 *
 * @throws IllegalArgumentException if the key is missing or not a Double
 */
fun ensureGetDouble(section: ConfigurationSection, key: String): Double {
    if (!section.isDouble(key)) {
        throw IllegalArgumentException("Section '${section.currentPath}' misses key '$key' or it is not a Double")
    }
    return section.getDouble(key)
}

/**
 * Returns the Int from the [ConfigurationSection], if any. Throws an exception if an error occurred.
 *
 * @param key The key
 * @param section The [ConfigurationSection] to get it from
 *
 * @throws IllegalArgumentException if the key is missing or not a Int
 */
fun ensureGetInt(section: ConfigurationSection, key: String): Int {
    if (!section.isInt(key)) {
        throw IllegalArgumentException("Section '${section.currentPath}' misses key '$key' or it is not a Int")
    }
    return section.getInt(key)
}

/**
 * Returns the Int from the [ConfigurationSection], if any. Throws an exception if an error occurred.
 *
 * @param key The key
 * @param section The [ConfigurationSection] to get it from
 * @param range The [IntRange] the number must be inside
 *
 * @throws IllegalArgumentException if the key is missing or not a Int
 *
 * @see ensureGetInt
 */
fun ensureGetBoundedInt(section: ConfigurationSection, key: String, range: IntRange): Int {
    val int = ensureGetInt(section, key)
    if (int !in range) {
        throw IllegalArgumentException("The key '$key' in '${section.currentPath}' is not in the accepted range ($range)")
    }
    return int
}

/**
 * Returns the StringList from the [ConfigurationSection], if any. Throws an exception if an error occurred.
 *
 * @param key The key
 * @param section The [ConfigurationSection] to get it from
 *
 * @throws IllegalArgumentException if the key is missing or not a StringList
 */
fun ensureGetStringList(section: ConfigurationSection, key: String): List<String> {
    if (!section.isList(key)) {
        throw IllegalArgumentException("Section '${section.currentPath}' misses key '$key' or it is not a StringList")
    }
    return section.getStringList(key)
}