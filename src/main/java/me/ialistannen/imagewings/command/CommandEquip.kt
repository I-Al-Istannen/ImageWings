package me.ialistannen.imagewings.command

import com.perceivedev.perceivecore.command.CommandResult
import com.perceivedev.perceivecore.command.CommandSenderType
import com.perceivedev.perceivecore.command.TranslatedCommandNode
import com.perceivedev.perceivecore.gui.ClickEvent
import com.perceivedev.perceivecore.gui.Gui
import com.perceivedev.perceivecore.gui.base.Component
import com.perceivedev.perceivecore.gui.base.Pane
import com.perceivedev.perceivecore.gui.components.Button
import com.perceivedev.perceivecore.gui.components.panes.AnchorPane
import com.perceivedev.perceivecore.gui.components.simple.DisplayColor
import com.perceivedev.perceivecore.gui.components.simple.SimpleLabel
import com.perceivedev.perceivecore.gui.components.simple.StandardDisplayTypes
import com.perceivedev.perceivecore.gui.util.Dimension
import com.perceivedev.perceivecore.util.ItemFactory
import me.ialistannen.imagewings.ImageWings
import me.ialistannen.imagewings.wings.Wing
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import java.util.*
import java.util.function.Consumer
import kotlin.comparisons.compareBy

/**
 * Lets the player equip a wing
 */
class CommandEquip : TranslatedCommandNode(
        Permission(ImageWings.instance.config.getString("permissions.commands.equip")),
        "command.equip",
        ImageWings.language,
        CommandSenderType.PLAYER) {

    override fun tabComplete(sender: CommandSender, chat: MutableList<String>, index: Int): List<String> {
        return emptyList()
    }

    override fun executePlayer(player: Player, vararg args: String): CommandResult {

        val pagedPane = PagedPane(9, 6)

        val gui = Gui(ImageWings.tr("command.equip.gui.title"), 6, pagedPane)

        ImageWings.wingDisplayManager.getAllWings()
                .filter {
                    if (ImageWings.instance.config.getBoolean("show_wings_without_permission")) {
                        true
                    } else {
                        player.hasPermission(it.permission)
                    }
                }
                .sortedWith(compareBy({ it.itemName.replace(Regex("&[0-9a-flmnor]", RegexOption.IGNORE_CASE), "") }))
                .forEach { wing ->
                    pagedPane.addComponentSimple(WingButton(
                            ItemFactory.builder(wing.itemMaterial)
                                    .setName(wing.itemName)
                                    .setLore(wing.itemLore)
                                    .build(),
                            Consumer {
                                if (!player.hasPermission(wing.permission)) {
                                    player.sendMessage(ImageWings.trWithPrefix("general.status.no.permission"))
                                    return@Consumer
                                }

                                val playerWing = ImageWings.wingDisplayManager.getPlayerWing(player)
                                if (playerWing == wing) {
                                    ImageWings.wingDisplayManager.removePlayer(player)
                                    player.sendMessage(ImageWings.trWithPrefix("command.unequipped.message"))
                                } else {
                                    ImageWings.wingDisplayManager.addPlayer(player, wing)
                                    player.sendMessage(ImageWings.trWithPrefix("command.equip.message", wing.itemName))
                                }

                                gui.reRender()
                            },
                            Dimension.ONE,
                            wing
                    ))
                }

        gui.open(player)

        return CommandResult.SUCCESSFULLY_INVOKED
    }
}

/**
 * A Button that changes its enchanted state based on if the wing it represents is selected!
 */
class WingButton(itemStack: ItemStack, clickHandler: Consumer<ClickEvent>, size: Dimension, val wing: Wing)
    : Button(itemStack, clickHandler, size) {

    override fun render(inventory: Inventory, player: Player, xOffset: Int, yOffset: Int) {
        val currentWing = ImageWings.wingDisplayManager.getPlayerWing(player)

        if (currentWing == null) {
            itemStack = ItemFactory.builder(itemStack).removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL).build()
            super.render(inventory, player, xOffset, yOffset)
            return
        }

        val itemFactory = ItemFactory.builder(itemStack)

        if (currentWing == wing) {
            itemFactory.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
        } else {
            itemFactory.removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL)
        }

        itemStack = itemFactory.build()

        super.render(inventory, player, xOffset, yOffset)
    }
}

class PagedPane(width: Int, height: Int) : AnchorPane(width, height) {

    private val pages: MutableList<AnchorPane> = ArrayList()
    private var currentPage: Int = 0
    private val innerPaneSize: Dimension

    init {
        addNewPane()
        innerPaneSize = Dimension(width, height - 2)
    }

    override fun addComponent(component: Component, x: Int, y: Int): Boolean {
        @Suppress("LoopToCallChain")
        for ((index, page) in pages.withIndex()) {
            val addedComponent = page.addComponent(component, x, y)

            if (!addedComponent) {
                // will never fit
                if (!component.size.fitsInside(innerPaneSize)) {
                    return false
                }

                addNewPane()
                return addComponent(component, x, y)
            } else {
                if (index == currentPage) {
                    requestReRender()
                }
            }
        }
        return true
    }

    /**
     * Adds a [Component]
     *
     * Will only fail if the component is too big.
     *
     * @param component The [Component] to add
     *
     * @return True if the component was added
     */
    fun addComponentSimple(component: Component): Boolean {
        // will never fit
        if (!component.size.fitsInside(innerPaneSize)) {
            return false
        }

        for ((index, page) in pages.withIndex()) {
            for (y in 0..page.height - 2) {
                for (x in 0..page.width - 1) {
                    val added = page.addComponent(component, x, y)
                    if (added) {
                        if (index == currentPage) {
                            requestReRender()
                        }
                        return true
                    }
                }
            }
        }

        addNewPane()
        return addComponentSimple(component)
    }

    private fun addNewPane() {
        val pane = AnchorPane(width, height)

        pane.addComponent(SimpleLabel(Dimension(9, 1), StandardDisplayTypes.FLAT, DisplayColor.BLACK, " "), 0, height - 2)
        pages.add(pane)
    }

    private fun addBaseComponents(page: AnchorPane, index: Int) {
        if (index > 0) {
            addButton(page, index, 0, height - 1, "pageable.gui.back.button", -1)
        }

        if (index < pages.lastIndex) {
            addButton(page, index, width - 1, height - 1, "pageable.gui.next.button", 1)
        }

        if (width > 2) {
            val xPosition = width / 2
            addButton(page, index, xPosition, height - 1, "pageable.gui.current.label", 0)
        }
    }

    private fun addButton(page: AnchorPane, index: Int, x: Int, y: Int, baseKey: String, pageMod: Int) {
        val originalButton = page.getComponentAtPoint(x, y)

        originalButton.ifPresent { page.removeComponent(it) }

        val buttonName = ImageWings.tr("$baseKey.name", index + pageMod + 1, pages.size)
        val buttonLoreOne = ImageWings.tr("$baseKey.lore.one", index + pageMod + 1, pages.size)
        val buttonLoreTwo = ImageWings.tr("$baseKey.lore.two", index + pageMod + 1, pages.size)
        val materialName = ImageWings.tr("$baseKey.material", index + pageMod + 1, pages.size)
        val material = Material.matchMaterial(materialName) ?: Material.WOOL

        val itemStack: ItemStack = ItemFactory.builder(material)
                .setName(buttonName)
                .addLore(buttonLoreOne, buttonLoreTwo)
                .build()

        val button = Button(itemStack, {
            currentPage = Math.max(currentPage + pageMod, 0)
            requestReRender()
        }, Dimension.ONE)

        page.addComponent(button, x, y)
    }

    override fun removeComponent(component: Component): Boolean {
        for ((index, page) in pages.withIndex()) {
            if (page.removeComponent(component)) {
                if (page.isEmpty() && pages.size > 1) {
                    pages.remove(page)
                    if (currentPage > pages.lastIndex) {
                        currentPage--
                    }
                }
                if (index == currentPage) {
                    requestReRender()
                }
                return true
            }
        }
        return false
    }

    override fun render(inventory: Inventory, player: Player, x: Int, y: Int) {
        addBaseComponents(pages[currentPage], currentPage)

        pages[currentPage].render(inventory, player, x, y)
    }

    override fun onClick(clickEvent: ClickEvent) {
        pages[currentPage].onClick(clickEvent)
    }

    /**
     * Checks if a Pane is empty (i.e. has no components)
     *
     * @return True if this pane is empty
     */
    private fun Pane.isEmpty(): Boolean {
        return this.children.isEmpty()
    }
}