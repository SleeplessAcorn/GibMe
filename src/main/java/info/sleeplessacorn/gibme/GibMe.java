package info.sleeplessacorn.gibme;

import lombok.val;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = GibMe.ID, name = GibMe.ID, version = GibMe.VERSION)
@EventBusSubscriber(modid = GibMe.ID)
public final class GibMe {
    public static final String ID = "༼ つ ◕_◕ ༽つ";
    public static final String NAME = "gibme";
    public static final String VERSION = "%VERSION%";

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent event) {
        if (Side.SERVER == event.side && Phase.END == event.phase && GibConfig.chanceToGib > 0) {
            GibTracker.addCooldownIfAbsent(event.player);
            GibTracker.incrementCooldown(event.player);
            if (GibConfig.hasItems() && GibTracker.hasCooldownExpired(event.player)) {
                val rand = event.player.world.rand;
                if (rand.nextDouble() <= GibConfig.chanceToGib) {
                    val items = GibConfig.getItems();
                    val stack = items.get(rand.nextInt(items.size()));
                    event.player.inventory.addItemStackToInventory(stack.copy());
                    if (GibConfig.displayMode.isToast()) {
                        val msg = new TextComponentString("Toast Notification NYI");
                        msg.getStyle().setColor(TextFormatting.RED);
                        event.player.sendStatusMessage(msg, false);
                        // FIXME Client-bound packets for toast notifications
                    } else {
                        val name = stack.getDisplayName();
                        val msg = new TextComponentTranslation("message.gibme", name);
                        event.player.sendStatusMessage(msg, GibConfig.displayMode.usesActionBar());
                    }
                    GibTracker.resetCooldown(event.player);
                }
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    static void onClientChatReceived(ClientChatReceivedEvent event) {
        if (GibConfig.replaceGiveCmdMsg && event.getMessage() instanceof TextComponentTranslation) {
            val msg = (TextComponentTranslation) event.getMessage();
            if ("commands.give.success".equals(msg.getKey())) {
                val args = msg.getFormatArgs();
                event.setMessage(new TextComponentTranslation("command.gibme", args[2], args[0], args[1]));
            }
        }
    }
}
