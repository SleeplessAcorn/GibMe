package info.sleeplessacorn.gibme;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.UUID;

@UtilityClass
@Mod.EventBusSubscriber(modid = GibMe.ID)
class GibTracker {
    private final Object2IntMap<UUID> UUID_COOLDOWN_MAP = new Object2IntOpenHashMap<>();

    void addCooldownIfAbsent(EntityPlayer player) {
        UUID_COOLDOWN_MAP.putIfAbsent(player.getUniqueID(), 0);
    }

    void resetCooldown(EntityPlayer player) {
        UUID_COOLDOWN_MAP.put(player.getUniqueID(), 0);
    }

    void incrementCooldown(EntityPlayer player) {
        val oldValue = UUID_COOLDOWN_MAP.getInt(player.getUniqueID());
        UUID_COOLDOWN_MAP.put(player.getUniqueID(), oldValue + 1);
    }

    void removeCooldown(EntityPlayer player) {
        UUID_COOLDOWN_MAP.removeInt(player.getUniqueID());
    }

    boolean hasCooldownExpired(EntityPlayer player) {
        val ticks = GibConfig.attemptCooldown * 20;
        return UUID_COOLDOWN_MAP.getInt(player.getUniqueID()) >= ticks;
    }

    @SubscribeEvent
    void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        GibTracker.removeCooldown(event.player);
    }
}
