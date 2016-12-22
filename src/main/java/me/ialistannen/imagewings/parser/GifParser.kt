package me.ialistannen.imagewings.parser

import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.IOException
import java.util.*
import javax.imageio.ImageReader
import javax.imageio.metadata.IIOMetadataNode

/**
 * Pareses a gif
 *
 * See [this StackOverFlow](http://stackoverflow.com/a/18425922)
 */
class GifParser {

    /**
     * Reads a gif
     *
     * @param reader The [ImageReader]
     *
     * @return An array with [ImageFrame]s
     */
    @Throws(IOException::class)
    fun readGIF(reader: ImageReader): Array<ImageFrame> {
        val frames = ArrayList<ImageFrame>(2)

        var width = -1
        var height = -1

        val metadata = reader.streamMetadata
        if (metadata != null) {
            val globalRoot = metadata.getAsTree(metadata.nativeMetadataFormatName) as IIOMetadataNode

            val globalScreenDescriptor = globalRoot.getElementsByTagName("LogicalScreenDescriptor")

            if (globalScreenDescriptor != null && globalScreenDescriptor.length > 0) {
                val screenDescriptor = globalScreenDescriptor.item(0) as IIOMetadataNode?

                if (screenDescriptor != null) {
                    width = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenWidth"))
                    height = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenHeight"))
                }
            }
        }

        var master: BufferedImage? = null
        var masterGraphics: Graphics2D? = null

        var frameIndex = 0
        while (true) {
            val image: BufferedImage
            try {
                image = reader.read(frameIndex)
            } catch (io: IndexOutOfBoundsException) {
                break
            }

            if (width == -1 || height == -1) {
                width = image.width
                height = image.height
            }

            val root = reader.getImageMetadata(frameIndex)
                    .getAsTree("javax_imageio_gif_image_1.0") as IIOMetadataNode
            val gce = root.getElementsByTagName("GraphicControlExtension").item(0) as IIOMetadataNode
            val delay = Integer.valueOf(gce.getAttribute("delayTime"))!!
            val disposal = gce.getAttribute("disposalMethod")

            var x = 0
            var y = 0

            if (master == null) {
                master = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                masterGraphics = master.createGraphics()
                masterGraphics!!.background = Color(0, 0, 0, 0)
            } else {
                val children = root.childNodes
                for (nodeIndex in 0..children.length - 1) {
                    val nodeItem = children.item(nodeIndex)
                    if (nodeItem.nodeName == "ImageDescriptor") {
                        val map = nodeItem.attributes
                        x = Integer.valueOf(map.getNamedItem("imageLeftPosition").nodeValue)!!
                        y = Integer.valueOf(map.getNamedItem("imageTopPosition").nodeValue)!!
                    }
                }
            }
            masterGraphics!!.drawImage(image, x, y, null)

            val copy = BufferedImage(master.colorModel, master.copyData(null), master
                    .isAlphaPremultiplied, null)
            frames.add(ImageFrame(copy, delay, disposal))

            if (disposal == "restoreToPrevious") {
                var from: BufferedImage? = null
                for (i in frameIndex - 1 downTo 0) {
                    if (frames[i].disposal != "restoreToPrevious" || frameIndex == 0) {
                        from = frames[i].image
                        break
                    }
                }

                master = BufferedImage(from!!.colorModel, from.copyData(null), from.isAlphaPremultiplied,
                        null)
                masterGraphics = master.createGraphics()
                masterGraphics!!.background = Color(0, 0, 0, 0)
            } else if (disposal == "restoreToBackgroundColor") {
                masterGraphics.clearRect(x, y, image.width, image.height)
            }
            frameIndex++
        }
        reader.dispose()

        return frames.toTypedArray()
    }

    data class ImageFrame(val image: BufferedImage, val delay: Int, val disposal: String)
}