package me.ialistannen.imagewings.example

import me.ialistannen.imagewings.ImageWings
import java.nio.file.Files
import java.nio.file.Path


/**
 * Copies all the Example files, if they do not yet exist
 *
 * @param targetDir The directory the wing meta files will lay i´m
 */
fun copyExampleWing(targetDir: Path) {
    val resourceDir = targetDir.resolve("example-resources")

    if (Files.notExists(resourceDir)) {
        Files.createDirectories(resourceDir)
    }

    // ==== STATIC WING ====

    // Image
    if (Files.notExists(resourceDir.resolve("WingStatic.png"))) {
        Files.copy(
                ImageWings.instance.getResource("examplewings/examplewing/static/WingStatic.png"),
                resourceDir.resolve("WingStatic.png")
        )
    }
    // Wing meta
    if (Files.notExists(targetDir.resolve("ExampleWingStatic.wingMeta"))) {
        Files.copy(
                ImageWings.instance.getResource("examplewings/examplewing/wingmeta/ExampleWingStatic.wingMeta"),
                targetDir.resolve("ExampleWingStatic.wingMeta")
        )
    }


    // ==== ANIMATED WING ====

    // Image
    if (Files.notExists(resourceDir.resolve("AnimatedWing.gif"))) {
        Files.copy(
                ImageWings.instance.getResource("examplewings/examplewing/animated/AnimatedWing.gif"),
                resourceDir.resolve("AnimatedWing.gif")
        )
    }
    // Wing meta
    if (Files.notExists(targetDir.resolve("ExampleWingAnimated.wingMeta"))) {
        Files.copy(
                ImageWings.instance.getResource("examplewings/examplewing/wingmeta/ExampleWingAnimated.wingMeta"),
                targetDir.resolve("ExampleWingAnimated.wingMeta")
        )
    }
}