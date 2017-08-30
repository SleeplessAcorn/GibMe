package info.sleeplessacorn.gibme;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Config(modid = GibMe.GIB, name = GibMe.ME, category = GibMe.GIB)
@Mod.EventBusSubscriber(modid = GibMe.GIB)
public class GibConfig {

    @Nullable
    private static volatile ImmutableList<ItemStack> itemCache;

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
    public static String[] gibMeThese = {"minecraft:dirt@4", "minecraft:wool:14", "minecraft:brick"};

    @Config.Name("Replace Give Command Message")
    @Config.Comment("Replaces the give command chat message with a special gib message [default: true]")
    @Config.LangKey("config.gibme.givecmd")
    public static boolean replaceGiveCmdMsg = true;

    @SubscribeEvent
    protected static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (GibMe.GIB.equals(event.getModID())) {
            ConfigManager.sync(GibMe.GIB, Config.Type.INSTANCE);
            invalidateItemCache();
        }
    }

    private static synchronized void invalidateItemCache() {
        itemCache = null;
    }

    public static synchronized ImmutableList<ItemStack> getItems() {
        if (itemCache == null) {
            itemCache = ImmutableList.copyOf(Stream.of(GibConfig.gibMeThese)
                    .map(GibConfig::getStackFromString)
                    .filter(stack -> !stack.isEmpty())
                    .collect(Collectors.toList()));
        }
        return itemCache;
    }

    public static boolean hasItems() {
        return getItems().size() > 0;
    }

    private static ItemStack getStackFromString(String string) {
        String[] values = string.split("@"), data = values[0].split(":");
        int amount = values.length > 1 ? Integer.valueOf(values[1]) : 1;
        int meta = data.length > 2 ? Integer.valueOf(data[2]) : 0;
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(data[0], data[1]));
        return item != null ? new ItemStack(item, amount, meta) : ItemStack.EMPTY;
    }

    public enum DisplayMode {
        CHAT, HOTBAR, TOAST;

        protected static boolean useActionBar() {
            return HOTBAR.equals(GibConfig.displayMode);
        }

        protected static boolean isToastDisplay() {
            return TOAST.equals(GibConfig.displayMode);
        }
    }

}
