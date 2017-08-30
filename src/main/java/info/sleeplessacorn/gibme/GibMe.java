package info.sleeplessacorn.gibme;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

@Mod(modid = GibMe.GIB, name = GibMe.GIB, dependencies = GibMe.DEPENDENCIES, version = GibMe.VERSION)
@Mod.EventBusSubscriber
public class GibMe {

    public static final String GIB = "༼ つ ◕_◕ ༽つ", ME = "gibme";
    public static final String DEPENDENCIES = "required-after:forge@[14.21.1.2387,)";
    public static final String VERSION = "[1.12,1.13)";

    @Nullable
    private static List<ItemStack> itemCache;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side != Side.SERVER || GibConfig.chanceToGib == 0)
            return;

        if (itemCache == null)
            itemCache = Stream.of(GibConfig.gibMeThese)
                    .map(GibMe::getStackFromString)
                    .filter(stack -> !stack.isEmpty())
                    .collect(Collectors.toList());

        if (!itemCache.isEmpty() && event.player.world.getTotalWorldTime() % (GibConfig.attemptCooldown * 20) == 0) {
            Random rand = event.player.world.rand;
            if (rand.nextDouble() <= GibConfig.chanceToGib) {
                ItemStack stack = itemCache.get(rand.nextInt(itemCache.size()));
                event.player.inventory.addItemStackToInventory(stack.copy());
                if (!GibConfig.displayMode.equals(GibConfig.DisplayMode.TOAST)) {
                    GibMe.sendGibMessage(event.player, stack);
                } else {
                    String msg = ChatFormatting.RED + "Toast Notification NYI";
                    event.player.sendStatusMessage(new TextComponentString(msg), false);
                    // FIXME Clientbound packets for toast notifications
                }
            }
        }
    }

    @SubscribeEvent @SideOnly(Side.CLIENT)
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

    private static void invalidateItemCache() {
        itemCache = null;
    }

    private static ItemStack getStackFromString(String string) {
        String[] values = string.split("@"), data = values[0].split(":");
        int amount = values.length > 1 ? Integer.valueOf(values[1]) : 1;
        int meta = data.length > 2 ? Integer.valueOf(data[2]) : 0;
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(data[0], data[1]));
        return item != null ? new ItemStack(item, amount, meta) : ItemStack.EMPTY;
    }

    public static void sendGibMessage(EntityPlayer player, ItemStack stack) {
        String loc = "message.gibme", name = stack.getDisplayName();
        ITextComponent message = new TextComponentTranslation(loc, name);
        boolean actionBar = GibConfig.displayMode.equals(GibConfig.DisplayMode.HOTBAR);
        player.sendStatusMessage(message, actionBar);
    }

    @Config(modid = GibMe.GIB, name = GibMe.ME, category = GibMe.GIB)
    @Mod.EventBusSubscriber
    protected static class GibConfig {

        @Config.Name("Attempt Cooldown")
        @Config.Comment("Seconds between each attempt to gib [default: 1]")
        @Config.LangKey("config.gibme.cooldown")
        @Config.RangeInt(min = 1)
        public static int attemptCooldown = 1;

        @Config.Name("Chance To Gib")
        @Config.Comment("Chance to gib an item from the list [default: 0.2]")
        @Config.LangKey("config.gibme.chance")
        @Config.RangeInt(min = 0)
        public static double chanceToGib = 0.2;

        @Config.Name("Gib Display Mode")
        @Config.Comment("The format that the gib message is displayed in [default: HOTBAR]")
        @Config.LangKey("config.gibme.displaymode")
        public static DisplayMode displayMode = DisplayMode.HOTBAR;

        @Config.Name("Gib Me These")
        @Config.Comment("List of things to gib [format: modid:name:meta@amount]")
        @Config.LangKey("config.gibme.list")
        public static String[] gibMeThese = { "minecraft:dirt@4", "minecraft:wool:14", "minecraft:brick" };

        @Config.Name("Replace Give Command Message")
        @Config.Comment("Replaces the give command chat message with a special gib message [default: true]")
        @Config.LangKey("config.gibme.givecmd")
        public static boolean replaceGiveCmdMsg = true;

        @SubscribeEvent
        protected static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (GibMe.GIB.equals(event.getModID())) {
                ConfigManager.sync(GibMe.GIB, Config.Type.INSTANCE);
                GibMe.invalidateItemCache();
            }
        }

        public enum DisplayMode { CHAT, HOTBAR, TOAST }

    }

}
