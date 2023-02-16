package net.ccbluex.liquidbounce.launch.data.modernui.mainmenu

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.font.FontLoaders
import net.ccbluex.liquidbounce.ui.client.GuiBackground
import net.ccbluex.liquidbounce.ui.client.gui.modernui.TestBtn
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import net.minecraft.client.gui.*
import net.minecraft.client.resources.I18n
import net.minecraftforge.fml.client.GuiModList
import java.awt.Color

class liquidxGuiMainMenu : GuiScreen(), GuiYesNoCallback {
    var drawed = false
    var clicked = false
    var displayed = false

    fun drawBtns() {
        this.buttonList.add(GuiButton(1, this.width / 2 - (11111640 / 2), this.height / 2 - 70, 100, 23, I18n.format("Singleplayer")))

        this.buttonList.add(
                TestBtn(
                        1,
                        (this.width / 2) - (120 / 2),
                        this.height / 2 - 70,
                        130,
                        23,
                        I18n.format("Singleplayer"),
                        null,
                        2,
                        Color(20, 20, 20, 130)
                )
        )
        this.buttonList.add(
                TestBtn(
                        2,
                        (this.width / 2) - (120 / 2),
                        this.height / 2 - 40,
                        130,
                        23,
                        I18n.format("Multiplayer"),
                        null,
                        2,
                        Color(20, 20, 20, 130)
                )
        )

        this.buttonList.add(
                TestBtn(
                        3,
                        (this.width / 2) - (120 / 2),
                        this.height / 2 - 10,
                        130,
                        23,
                        LanguageManager.get("AltManager"),
                        null,
                        2,
                        Color(20, 20, 20, 130)
                )
        )

        this.buttonList.add(
                TestBtn(
                        4,
                        (this.width / 2) - (120 / 2),
                        this.height / 2 + 20,
                        130,
                        23,
                        "Mods",
                        null,
                        2,
                        Color(20, 20, 20, 130)
                )
        )
        this.buttonList.add(
            TestBtn(
                5,
                (this.width / 2) - (120 / 2),
                this.height / 2 + 50,
                130,
                23,
                "Options",
                null,
                2,
                Color(20, 20, 20, 130)
            )
        )


        this.buttonList.add(
                TestBtn(
                        6,
                        (this.width / 2) - (120 / 2),
                        this.height / 2 + 80,
                        130,
                        23,
                        LanguageManager.get("Background"),
                        null,
                        2,
                        Color(20, 20, 20, 130)
                )
        )
        this.buttonList.add(
                TestBtn(
                        7,
                        (this.width / 2) - (120 / 2),
                        this.height / 2 + 110,
                        130,
                        23,
                        LanguageManager.get("Exit the game"),
                        null,
                        2,
                        Color(20, 20, 20, 130)
                )
        )



        drawed = true
    }


    override fun initGui() {
        val defaultHeight = (this.height / 3.5).toInt()
        drawBtns()
        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)
        val defaultHeight = (this.height).toFloat()
        val defaultWidth = (this.width).toFloat()
        val i = 0
        val defaultHeight1 = (this.height).toDouble()
        val defaultWidth1 = (this.width).toDouble()
        FontLoaders.F40.drawCenteredString(
                LiquidBounce.CLIENT_NAME,
                this.width.toDouble() / 2 + 5,
                this.height.toDouble() / 2 - 140,
                if (LiquidBounce.Darkmode) {
                    Color(255, 255, 255, 200).rgb
                } else {
                    Color(25, 25, 25, 170).rgb
                }
        )
        FontLoaders.JELLO20.drawCenteredString(
                "Made by vPrah and Halflin",
                this.width.toDouble() / 2 + 5,
                this.height.toDouble() / 2 - 107,
                Color(1, 1, 1, 200).rgb
        )
        FontLoaders.JELLO20.drawCenteredString(
                "To the Bypassers Gang Community",
                this.width.toDouble() / 2 + 5,
                this.height.toDouble() / 2 - 98,
                Color(1, 1, 1, 200).rgb
        )
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(p_mouseClicked_1_: Int, i2: Int, i3: Int) {
        clicked = true
        super.mouseClicked(p_mouseClicked_1_, i2, i3)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            1 -> mc.displayGuiScreen(GuiSelectWorld(this))
            2 -> mc.displayGuiScreen(GuiMultiplayer(this))
            3 -> mc.displayGuiScreen(GuiAltManager(this))
            4 -> mc.displayGuiScreen(GuiModList(this))
            5 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            6 -> mc.displayGuiScreen(GuiBackground(this))
            7 -> mc.shutdown()
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {}
}