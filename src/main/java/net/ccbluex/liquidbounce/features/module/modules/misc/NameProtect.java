/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.TextEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.file.configs.FriendsConfig;
import net.ccbluex.liquidbounce.utils.misc.StringUtils;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.features.value.*;
import net.minecraft.client.network.NetworkPlayerInfo;

@ModuleInfo(name = "NameProtect", category = ModuleCategory.CLIENT)
public class NameProtect extends Module {

    private final TextValue fakeNameValue = new TextValue("FakeName", "&cPlayer");
    private final TextValue allFakeNameValue = new TextValue("AllPlayersFakeName", "LiquidX User");
    public final BoolValue selfValue = new BoolValue("Yourself", true);
    public final BoolValue tagValue = new BoolValue("Tag", false);
    public final BoolValue allPlayersValue = new BoolValue("AllPlayers", false);

    @EventTarget
    public void onText(final TextEvent event) {
        if (mc.thePlayer == null || event.getText().contains(LiquidBounce.CLIENT_NAME + " §f") || event.getText().startsWith("/") || event.getText().startsWith(LiquidBounce.commandManager.getPrefix() + ""))
            return;

        for (final FriendsConfig.Friend friend : LiquidBounce.fileManager.getFriendsConfig().getFriends())
            event.setText(StringUtils.replace(event.getText(), friend.getPlayerName(), ColorUtils.translateAlternateColorCodes(friend.getAlias()) + "§f"));

        event.setText(StringUtils.replace(
                event.getText(),
                mc.thePlayer.getName(),
                (selfValue.get() ? (tagValue.get() ? StringUtils.injectAirString(mc.thePlayer.getName()) + " §7(§r" + ColorUtils.translateAlternateColorCodes(fakeNameValue.get() + "§r§7)") : ColorUtils.translateAlternateColorCodes(fakeNameValue.get()) + "§r") : mc.thePlayer.getName())
        ));

        if(allPlayersValue.get())
            for(final NetworkPlayerInfo playerInfo : mc.getNetHandler().getPlayerInfoMap())
                event.setText(StringUtils.replace(event.getText(), playerInfo.getGameProfile().getName(), ColorUtils.translateAlternateColorCodes(allFakeNameValue.get()) + "§f"));
    }

}