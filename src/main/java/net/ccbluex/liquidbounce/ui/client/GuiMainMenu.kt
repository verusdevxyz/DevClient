/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.LiquidBounce.CLIENT_NAME2
import net.ccbluex.liquidbounce.LiquidBounce.CLIENT_WEBSITE
import net.ccbluex.liquidbounce.LiquidBounce.clientVersionText
import net.ccbluex.liquidbounce.api.messageOfTheDay
import net.ccbluex.liquidbounce.lang.translationMenu
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedBorderRect
import net.minecraft.client.gui.*
import net.minecraft.client.resources.I18n

class GuiMainMenu : GuiScreen() {

    override fun initGui() {
        val defaultHeight = height / 4 + 48

        buttonList.run {
            add(GuiButton(100, width / 2 - 100, defaultHeight + 24, 98, 20, translationMenu("altManager")))
            add(GuiButton(103, width / 2 + 2, defaultHeight + 24, 98, 20, translationMenu("mods")))
            add(GuiButton(101, width / 2 - 100, defaultHeight + 24 * 2, 98, 20, translationMenu("serverStatus")))
            add(GuiButton(102, width / 2 + 2, defaultHeight + 24 * 2, 98, 20, translationMenu("configuration")))

            add(GuiButton(1, width / 2 - 100, defaultHeight, 98, 20, I18n.format("menu.singleplayer")))
            add(GuiButton(2, width / 2 + 2, defaultHeight, 98, 20, I18n.format("menu.multiplayer")))

            // Minecraft Realms
            //		this.buttonList.add(new GuiButton(14, this.width / 2 - 100, j + 24 * 2, I18n.format("menu.online", new Object[0])));

            //add(GuiButton(108, width / 2 - 100, defaultHeight + 24 * 3, translationMenu("contributors")))
            add(GuiButton(0, width / 2 - 100, defaultHeight + 24 * 4, 98, 20, I18n.format("menu.options")))
            add(GuiButton(4, width / 2 + 2, defaultHeight + 24 * 4, 98, 20, I18n.format("menu.quit")))
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        drawRoundedBorderRect(width / 2f - 115, height / 4f + 35, width / 2f + 115, height / 4f + 175,
            2f,
            Integer.MIN_VALUE,
            Integer.MIN_VALUE,
            3F
        )

        Fonts.fontBold180.drawCenteredString(CLIENT_NAME2, width / 2F, height / 8F, 0xBF3F8C, true)
        Fonts.font35.drawCenteredString(clientVersionText, width / 2F + 176, height / 8F + Fonts.font35.fontHeight, 0xffffff, true)

        val messageOfTheDay = messageOfTheDay?.message
        if (messageOfTheDay?.isNotBlank() == true) {
            val lines = messageOfTheDay.lines()

            drawRoundedBorderRect(width / 2f - 115,
                height / 4f + 190,
                width / 2f + 115,
                height / 4f + 192 + (Fonts.font35.fontHeight * lines.size),
                2f,
                Integer.MIN_VALUE,
                Integer.MIN_VALUE,
                3F
            )

            // Draw rect below main rect and within draw MOTD text
            for ((index, line) in lines.withIndex()) {
                Fonts.font35.drawCenteredString(line, width / 2F, height / 4f + 197.5f
                        + (Fonts.font35.fontHeight * index), 0xffffff, true)
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        // When clicking the message of the day text
        val messageOfTheDay = messageOfTheDay?.message
        if (messageOfTheDay?.isNotBlank() == true) {
            val lines = messageOfTheDay.lines()
            val motdHeight = height / 4f + 190
            val motdWidth = width / 2f - 115
            val motdHeightEnd = motdHeight + 192 + (Fonts.font35.fontHeight * lines.size)
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            1 -> mc.displayGuiScreen(GuiSelectWorld(this))
            2 -> mc.displayGuiScreen(GuiMultiplayer(this))
            4 -> mc.shutdown()
            100 -> mc.displayGuiScreen(GuiAltManager(this))
            101 -> mc.displayGuiScreen(GuiServerStatus(this))
            102 -> mc.displayGuiScreen(GuiClientConfiguration(this))
            103 -> mc.displayGuiScreen(GuiModsMenu(this))
            108 -> mc.displayGuiScreen(GuiContributors(this))
        }
    }
}