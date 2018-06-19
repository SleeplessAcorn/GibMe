package info.sleeplessacorn.gibme;

import com.google.common.collect.ImmutableList;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Stream;

@UtilityClass
@Config(modid = GibMe.ID, name = GibMe.NAME, category = GibMe.ID)
@EventBusSubscriber(modid = GibMe.ID)
class GibConfig {
    @Name("Attempt Cooldown")
    @Comment("Seconds between each attempt to gib. [default: 1]")
    @LangKey("config.gibme.cooldown")
    @RangeInt(min = 1)
    public int attemptCooldown = 1;

    @Name("Chance To Gib")
    @Comment("Chance to gib an item from the list. [default: 0.2]")
    @LangKey("config.gibme.chance")
    @RangeInt(min = 0)
    public double chanceToGib = 0.2;

    @Name("Gib Display Mode")
    @Comment("The format that the gib message is displayed in. [default: HOTBAR]")
    @LangKey("config.gibme.displaymode")
    public DisplayMode displayMode = DisplayMode.HOTBAR;

    @Name("Gib Me These")
    @Comment("List of things to gib. [format: modid:name:meta@amount]")
    @LangKey("config.gibme.list")
    public String[] gibMeThese = { "minecraft:dirt@4", "minecraft:wool:14", "minecraft:brick" };

    @Name("Replace Give Command Message")
    @Comment("Replaces the give command chat message with a special gib message. [default: true]")
    @LangKey("config.gibme.givecmd")
    public boolean replaceGiveCmdMsg = true;

    @Nullable
    private volatile ImmutableList<ItemStack> items;

    @SubscribeEvent
    void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (GibMe.ID.equals(event.getModID())) {
            ConfigManager.sync(GibMe.ID, Config.Type.INSTANCE);
            GibConfig.invalidateItemCache();
        }
    }

    boolean hasItems() {
        return getItems().size() > 0;
    }

    synchronized ImmutableList<ItemStack> getItems() {
        if (items == null) {
            items = Stream.of(GibConfig.gibMeThese)
                .map(GibConfig::getStackFromString)
                .filter(it -> !it.isEmpty())
                .collect(ImmutableList.toImmutableList());
        }
        return Objects.requireNonNull(items, "Item cache cannot be null");
    }

    private ItemStack getStackFromString(String string) {
        val values = string.split("@"), data = values[0].split(":");
        val amount = values.length > 1 ? Integer.valueOf(values[1]) : 1;
        val meta = data.length > 2 ? Integer.valueOf(data[2]) : 0;
        val name = new ResourceLocation(data[0], data[1]);
        val item = ForgeRegistries.ITEMS.getValue(name);
        return item != null ? new ItemStack(item, amount, meta) : ItemStack.EMPTY;
    }

    private synchronized void invalidateItemCache() {
        GibConfig.items = null;
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
