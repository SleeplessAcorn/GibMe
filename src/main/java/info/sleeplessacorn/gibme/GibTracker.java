package info.sleeplessacorn.gibme;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = GibMe.GIB)
public final class GibTracker {

    private static final Map<UUID, Integer> TRACKER = new HashMap<>();

    protected static void addCooldownIfAbsent(EntityPlayer player) {
        TRACKER.putIfAbsent(player.getUniqueID(), 0);
    }

    protected static void resetCooldown(EntityPlayer player) {
        TRACKER.put(player.getUniqueID(), 0);
    }

    protected static void incrementCooldown(EntityPlayer player) {
        int oldValue = TRACKER.get(player.getUniqueID());
        TRACKER.put(player.getUniqueID(), ++oldValue);
    }

    protected static void removeCooldown(EntityPlayer player) {
        TRACKER.remove(player.getUniqueID());
    }

    protected static boolean hasCooldownExpired(EntityPlayer player) {
        return TRACKER.get(player.getUniqueID())
                >= GibConfig.attemptCooldown * 20;
    }

    @SubscribeEvent
    protected static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        GibTracker.removeCooldown(event.player);
    }

}
