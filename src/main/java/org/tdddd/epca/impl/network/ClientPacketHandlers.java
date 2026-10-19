package org.tdddd.epca.impl.network;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.tdddd.epca.impl.client.ClientColorEffect;
import org.tdddd.epca.impl.client.ClientErosionData;
import org.tdddd.epca.impl.client.ClientEvolutionData;
import org.tdddd.epca.impl.client.WaterColorEffectsManager;
import org.tdddd.epca.impl.client.effect.ArayaClientCache;
import org.tdddd.epca.impl.client.effect.ArayaSlashClientCache;
import org.tdddd.epca.impl.client.effect.BioTortClientState;
import org.tdddd.epca.impl.client.effect.SacrificeRitualClientCache;
import org.tdddd.epca.impl.client.effect.SoulProtectionClientCache;
import org.tdddd.epca.impl.client.organ.NestLeaderOrganClientData;
import org.tdddd.epca.impl.overworld.data.BiomassClientData;
import org.tdddd.epca.impl.overworld.data.EPCANoteTabData;
import org.tdddd.epca.impl.overworld.data.NestLeaderClientCache;

/**
 * Client-only bodies of every {@code S2C} payload.
 *
 * <p>This class exists so that the payload handler methods (which live in common payload classes and
 * are therefore loaded on a dedicated server too) never resolve a client-only class on the server:
 * the handler is registered for {@code playToClient} and additionally checks
 * {@code context.flow() == PacketFlow.CLIENTBOUND}, so a reference to this class is only ever resolved
 * on a physical client. This is the {@code DistExecutor}-free equivalent of the old
 * {@code @OnlyIn(Dist.CLIENT)} handlers.</p>
 */
public final class ClientPacketHandlers {

    private ClientPacketHandlers() {
    }

    public static void handleAcidWaterColor(BlockPos waterPos, BlockPos acidPos, int distance, boolean add) {
        if (add) {
            WaterColorEffectsManager.updateClientEffect(waterPos, acidPos, distance);
        } else {
            WaterColorEffectsManager.removeClientEffect(waterPos);
        }
    }

    public static void handleBiomassSync(boolean isNestLeader, int points) {
        BiomassClientData.update(isNestLeader, points);
    }

    public static void handleClearErosionDisplay(UUID playerUUID) {
        ClientErosionData.clearErosionData(playerUUID);
    }

    public static void handleColorEffect(int entityId, int type, int duration) {
        ClientColorEffect.setEffect(entityId, type, duration);
    }

    public static void handleEnderErosionSync(UUID playerUUID, float erosionValue, int effectLevel,
                                              float maxHealth, float currentHealth) {
        ClientErosionData.setErosionData(playerUUID, erosionValue, effectLevel, maxHealth, currentHealth);
    }

    public static void handleAddInfestedSource(BlockPos pos) {
        WaterColorEffectsManager.addInfestedSource(pos);
    }

    public static void handleRemoveInfestedSource(BlockPos pos) {
        WaterColorEffectsManager.removeInfestedSource(pos);
    }

    public static void handleSyncAllInfestedSources(Collection<BlockPos> positions) {
        WaterColorEffectsManager.clearInfestedCache();
        WaterColorEffectsManager.addInfestedSourcesBatch(positions);
    }

    public static void handleArayaAura(int[] entityIds, double[] positions) {
        ArayaClientCache.applyHolders(entityIds, positions);
    }

    public static void handleArayaFire(int[] xs, int[] ys, int[] zs, long rolledAtTick,
                                       long[] bornAgoTicks, int[] lifetimes, float[] heights) {
        ArayaClientCache.applyFires(xs, ys, zs, rolledAtTick, bornAgoTicks, lifetimes, heights);
    }

    public static void handleEvolutionStage(ResourceLocation dimension, int stage) {
        ClientEvolutionData.updateStage(dimension, stage);
    }

    public static void handleHotbarMark(int slotMask, int durationTicks) {
        BioTortClientState.applyHotbarMark(slotMask, durationTicks);
    }

    public static void handleNestLeaderOrgans(CompoundTag organData, boolean torsoInnerOpen,
                                              boolean headInnerOpen, boolean openScreen) {
        // Kept from the 1.20.1 handler: never re-open a screen that is already up.
        boolean screenAlreadyOpen = NestLeaderOrganClientData.isScreenOpen();
        NestLeaderOrganClientData.onSync(organData, torsoInnerOpen, headInnerOpen, screenAlreadyOpen);
        if (openScreen && !screenAlreadyOpen) {
            NestLeaderOrganClientData.openScreen();
        }
    }

    public static void handleNestLeaders(Set<UUID> leaders) {
        NestLeaderClientCache.updateLeaders(leaders);
    }

    public static void handleNoteTabs(List<EPCANoteTabData.ParentTab> tabs) {
        EPCANoteTabData.setClientTabs(tabs);
    }

    public static void handleRitualAura(BlockPos[] centers, int[] remainingTicks) {
        SacrificeRitualClientCache.applyBatch(centers, remainingTicks);
    }

    public static void handleSoulProtection(int[] entityIds, int[] remainingTicks) {
        SoulProtectionClientCache.applyBatch(entityIds, remainingTicks);
    }

    public static void handleScreenCorruption(boolean active, int corruptionTicks, int decayTicks) {
        if (active) {
            BioTortClientState.startCorruption(corruptionTicks, decayTicks);
        } else {
            BioTortClientState.stopCorruption();
        }
    }

    public static void handleArayaSlash(double x, double y, double z, float directionX,
                                        float directionZ, long startTick) {
        ArayaSlashClientCache.add(new Vec3(x, y, z), new Vec3(directionX, 0.0D, directionZ), startTick);
    }
}
