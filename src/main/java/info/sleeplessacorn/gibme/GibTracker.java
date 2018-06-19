package info.sleeplessacorn.gibme;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = GibMe.ID)
final class GibTracker {
    private static final Object2IntMap<UUID> UUID_COOLDOWN_MAP = new Object2IntOpenHashMap<>();

    static void addCooldownIfAbsent(EntityPlayer player) {
        UUID_COOLDOWN_MAP.putIfAbsent(player.getUniqueID(), 0);
    }

    static void resetCooldown(EntityPlayer player) {
        UUID_COOLDOWN_MAP.put(player.getUniqueID(), 0);
    }

    static void incrementCooldown(EntityPlayer player) {
        int oldValue = UUID_COOLDOWN_MAP.getInt(player.getUniqueID());
        UUID_COOLDOWN_MAP.put(player.getUniqueID(), ++oldValue);
    }

    static void removeCooldown(EntityPlayer player) {
        UUID_COOLDOWN_MAP.removeInt(player.getUniqueID());
    }

    static boolean hasCooldownExpired(EntityPlayer player) {
        final long ticks = GibConfig.attemptCooldown * 20;
        return UUID_COOLDOWN_MAP.getInt(player.getUniqueID()) >= ticks;
    }

    @SubscribeEvent
    static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        GibTracker.removeCooldown(event.player);
    }
}
