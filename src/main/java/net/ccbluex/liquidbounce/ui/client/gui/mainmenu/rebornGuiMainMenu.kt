package net.ccbluex.liquidbounce.launch.data.modernui.mainmenu

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.font.FontLoaders
import net.ccbluex.liquidbounce.ui.client.gui.modernui.TestBtn
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import net.minecraft.client.gui.*
import net.minecraft.client.resources.I18n
import net.minecraftforge.fml.client.GuiModList
import java.awt.Color

class rebornGuiMainMenu : GuiScreen(), GuiYesNoCallback {
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
                        100,
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
                        103,
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
                0,
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
                        4,
                        (this.width / 2) - (120 / 2),
                        this.height / 2 + 80,
                        130,
                        23,
                        LanguageManager.get("Exit the game"),
                        null,
                        2,
                        Color(20, 20, 20, 130)
                )
        )

       // this.buttonList.add(
         //       TestBtn(
           //             0,
             //           this.width - 640,
        //         252,
          //              25,
            //            25,
              //          I18n.format("menu.options").replace(".", ""),
                //        ResourceLocation("fdpclient/imgs/icon/setting.png"),
                  //      2,
                    //    Color(20, 20, 20, 130)
                //)
        //)

       // this.buttonList.add(
         //       TestBtn(
           //             104,
             //           this.width - 610,
               //         252,
                 //       25,
                   //     25,
                     //   I18n.format("ui.background"),
                       // ResourceLocation("fdpclient/imgs/icon/wallpaper.png"),
                        // 2,
                        // Color(20, 20, 20, 130)
                //)

        //)




        drawed = true
    }


    /* For modification, please keep "Designed by SkidderMC" */
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
       // FontLoaders.F16.drawString(
       //         LiquidBounce.CLIENT_NAME,
        //           10f,
        //         this.height - 25f,
        //         Color(1, 1, 1, 200).rgb
        //  )
        //  var versionMsg =
        //
        //         "Version: " + LiquidBounce.CLIENT_VERSION
        //   FontLoaders.F16.drawString(
        //         versionMsg,
        //         this.width - FontLoaders.F16.getStringWidth(versionMsg) - 622f,
        //         this.height - 15f,
        //         Color(1, 1, 1, 200).rgb
        // )
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(p_mouseClicked_1_: Int, i2: Int, i3: Int) {
        clicked = true
        super.mouseClicked(p_mouseClicked_1_, i2, i3)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            1 -> mc.displayGuiScreen(GuiSelectWorld(this))
            2 -> mc.displayGuiScreen(GuiMultiplayer(this))
            4 -> mc.shutdown()
            100 -> mc.displayGuiScreen(GuiAltManager(this))
            102 -> displayed = false
            103 -> mc.displayGuiScreen(GuiModList(this))
            104 -> MiscUtils.showURL("https://${LiquidBounce.CLIENT_RELEASE}/discord.html")
            514 -> MiscUtils.showURL("https://${LiquidBounce.CLIENT_RELEASE}/discord.html")
            114 -> MiscUtils.showURL("https://${LiquidBounce.CLIENT_RELEASE}")
            191 -> LiquidBounce.Darkmode = !LiquidBounce.Darkmode
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {}
}