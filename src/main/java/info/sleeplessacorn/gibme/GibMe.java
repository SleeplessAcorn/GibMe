package info.sleeplessacorn.gibme;

import com.google.common.collect.ImmutableList;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@Mod(modid = GibMe.GIB, name = GibMe.GIB, dependencies = GibMe.DEPENDENCIES, version = GibMe.VERSION)
@Mod.EventBusSubscriber
public class GibMe {

    public static final String GIB = "༼ つ ◕_◕ ༽つ", ME = "gibme";
    public static final String DEPENDENCIES = "required-after:forge@[14.21.1.2387,)";
    public static final String VERSION = "[1.12,1.13)";

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if (event.side != Side.SERVER || GibConfig.chanceToGib == 0 || !Phase.END.equals(event.phase))
            return;

        GibTracker.addCooldownIfAbsent(event.player);
        GibTracker.incrementCooldown(event.player);

        if (GibConfig.hasItems() && GibTracker.hasCooldownExpired(event.player)) {
            Random rand = event.player.world.rand;
            if (rand.nextDouble() <= GibConfig.chanceToGib) {
                ImmutableList<ItemStack> items = GibConfig.getItems();
                ItemStack stack = items.get(rand.nextInt(items.size()));
                event.player.inventory.addItemStackToInventory(stack.copy());
                if (GibConfig.DisplayMode.isToastDisplay()) {
                    String msg = ChatFormatting.RED + "Toast Notification NYI";
                    event.player.sendStatusMessage(new TextComponentString(msg), false);
                    // FIXME Client-bound packets for toast notifications
                } else {
                    GibMe.sendGibMessage(event.player, stack);
                }
                GibTracker.resetCooldown(event.player);
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onClientChatReceived(ClientChatReceivedEvent event) {
        if (!GibConfig.replaceGiveCmdMsg)
            return;

        if (event.getMessage() instanceof TextComponentTranslation) {
            TextComponentTranslation msg = (TextComponentTranslation) event.getMessage();
            if ("commands.give.success".equals(msg.getKey())) {
                Object[] args = msg.getFormatArgs();
                event.setMessage(new TextComponentTranslation("command.gibme", args[2], args[0], args[1]));
            }
        }
    }

    public static void sendGibMessage(EntityPlayer player, ItemStack stack) {
        String loc = "message.gibme", name = stack.getDisplayName();
        ITextComponent message = new TextComponentTranslation(loc, name);
        player.sendStatusMessage(message, GibConfig.DisplayMode.useActionBar());
    }

}
