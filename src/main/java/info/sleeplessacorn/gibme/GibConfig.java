package info.sleeplessacorn.gibme;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Stream;

@Config(modid = GibMe.ID, name = GibMe.NAME, category = GibMe.ID)
@Mod.EventBusSubscriber(modid = GibMe.ID)
final class GibConfig {
    @Name("Attempt Cooldown")
    @Comment("Seconds between each attempt to gib. [default: 1]")
    @LangKey("config.gibme.cooldown")
    @RangeInt(min = 1)
    public static int attemptCooldown = 1;

    @Name("Chance To Gib")
    @Comment("Chance to gib an item from the list. [default: 0.2]")
    @LangKey("config.gibme.chance")
    @RangeInt(min = 0)
    public static double chanceToGib = 0.2;

    @Name("Gib Display Mode")
    @Comment("The format that the gib message is displayed in. [default: HOTBAR]")
    @LangKey("config.gibme.displaymode")
    public static DisplayMode displayMode = DisplayMode.HOTBAR;

    @Name("Gib Me These")
    @Comment("List of things to gib. [format: modid:name:meta@amount]")
    @LangKey("config.gibme.list")
    public static String[] gibMeThese = { "minecraft:dirt@4", "minecraft:wool:14", "minecraft:brick" };

    @Name("Replace Give Command Message")
    @Comment("Replaces the give command chat message with a special gib message. [default: true]")
    @LangKey("config.gibme.givecmd")
    public static boolean replaceGiveCmdMsg = true;

    @Nullable
    private static volatile ImmutableList<ItemStack> itemCache;

    private GibConfig() {}

    @SubscribeEvent
    static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (GibMe.ID.equals(event.getModID())) {
            ConfigManager.sync(GibMe.ID, Config.Type.INSTANCE);
            GibConfig.invalidateItemCache();
        }
    }

    static boolean hasItems() {
        return getItems().size() > 0;
    }

    static synchronized ImmutableList<ItemStack> getItems() {
        if (itemCache == null) {
            itemCache = Stream.of(GibConfig.gibMeThese)
                .map(GibConfig::getStackFromString)
                .filter(it -> !it.isEmpty())
                .collect(ImmutableList.toImmutableList());
        }
        return Objects.requireNonNull(itemCache, "Item cache cannot be null");
    }

    private static ItemStack getStackFromString(String string) {
        final String[] values = string.split("@"), data = values[0].split(":");
        final int amount = values.length > 1 ? Integer.valueOf(values[1]) : 1;
        final int meta = data.length > 2 ? Integer.valueOf(data[2]) : 0;
        final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(data[0], data[1]));
        return item != null ? new ItemStack(item, amount, meta) : ItemStack.EMPTY;
    }

    private static synchronized void invalidateItemCache() {
        GibConfig.itemCache = null;
    }

    enum DisplayMode {
        CHAT, HOTBAR, TOAST;

        public final boolean usesActionBar() {
            return HOTBAR == this;
        }

        public final boolean isToast() {
            return TOAST == this;
        }
    }
}
