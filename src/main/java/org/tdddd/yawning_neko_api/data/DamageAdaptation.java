package org.tdddd.yawning_neko_api.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.joml.Vector3f;
import org.tdddd.yawning_neko_api.events.AdaptationEffectEvent;
import org.tdddd.yawning_neko_api.network.ModNetwork;
import org.tdddd.yawning_neko_api.network.packet.AdaptationDataSyncPacket;
import org.tdddd.yawning_neko_api.network.packet.AdaptationEventSyncPacket;

/**
 * Core damage-adaptation logic (1.21.1 source port of the 1.20.1 Forge implementation).
 *
 * <p>Forge {@code LivingHurtEvent} is replaced by {@link LivingIncomingDamageEvent}; the entity
 * capability is replaced by the {@link IAdaptationData} data attachment.</p>
 */
public class DamageAdaptation {
    private static final WeakHashMap<LivingEntity, Long> spawnTimeMap = new WeakHashMap<>();
    private static final WeakHashMap<LivingEntity, Long> lastHurtTimeMap = new WeakHashMap<>();
    private static final WeakHashMap<LivingEntity, Long> delayedHurtTimeMap = new WeakHashMap<>();
    private static final WeakHashMap<LivingEntity, Map<String, Integer>> clientAdaptationData = new WeakHashMap<>();
    private static final WeakHashMap<LivingEntity, String> clientLastDamageTypeKey = new WeakHashMap<>();
    private static final WeakHashMap<LivingEntity, Boolean> clientLastDamageTypeAdaptable = new WeakHashMap<>();
    private static final WeakHashMap<LivingEntity, String> serverLastDamageTypeKey = new WeakHashMap<>();
    private static final WeakHashMap<LivingEntity, Integer> lastAdaptationEventType = new WeakHashMap<>();
    private static final WeakHashMap<LivingEntity, Long> lastAdaptationEventTime = new WeakHashMap<>();
    private static final long EVENT_DISPLAY_DURATION_TICKS = 10L;
    private static final WeakHashMap<LivingEntity, Long> clientEventExpireTick = new WeakHashMap<>();
    private static final WeakHashMap<LivingEntity, Integer> clientEventType = new WeakHashMap<>();
    private static final Map<String, Integer> CLIENT_EVENT_BY_KEY = new HashMap<>();
    private static final Map<String, Long> CLIENT_EVENT_EXPIRE_BY_KEY = new HashMap<>();
    private static final Map<Integer, Integer> CLIENT_EVENT_BY_ID = new HashMap<>();
    private static final Map<Integer, Long> CLIENT_EVENT_EXPIRE_BY_ID = new HashMap<>();
    private static Level lastClientEventLevel;
    private static final WeakHashMap<LivingEntity, Integer> clientMaxAdaptations = new WeakHashMap<>();

    public DamageAdaptation() {
    }

    private static String eventKey(Entity entity) {
        return entity.level().dimension().location() + "#" + entity.getId();
    }

    private static void ensureSameLevel(Level level) {
        if (level != lastClientEventLevel) {
            lastClientEventLevel = level;
            CLIENT_EVENT_BY_KEY.clear();
            CLIENT_EVENT_EXPIRE_BY_KEY.clear();
            CLIENT_EVENT_BY_ID.clear();
            CLIENT_EVENT_EXPIRE_BY_ID.clear();
            clientEventType.clear();
            clientEventExpireTick.clear();
        }
    }

    public static void updateClientAdaptationEventById(int entityId, int eventType, Level level) {
        ensureSameLevel(level);
        long expireTick = (level == null ? 0L : level.getGameTime()) + EVENT_DISPLAY_DURATION_TICKS;
        CLIENT_EVENT_BY_ID.put(entityId, eventType);
        CLIENT_EVENT_EXPIRE_BY_ID.put(entityId, expireTick);
        if (level != null) {
            CLIENT_EVENT_BY_KEY.put(level.dimension().location() + "#" + entityId, eventType);
            CLIENT_EVENT_EXPIRE_BY_KEY.put(level.dimension().location() + "#" + entityId, expireTick);
        }
    }

    private static String damageTypeKeyToString(Object key) {
        if (key instanceof ResourceLocation loc) {
            return loc.toString();
        } else {
            return key instanceof TagKey<?> tag ? "#" + tag.location() : null;
        }
    }

    private static Object stringToDamageTypeKey(String str) {
        if (str == null) {
            return null;
        } else if (str.startsWith("#")) {
            String tagName = str.substring(1);
            ResourceLocation location = ResourceLocation.tryParse(tagName);
            return location == null ? null : TagKey.create(Registries.DAMAGE_TYPE, location);
        } else {
            return ResourceLocation.tryParse(str);
        }
    }

    /**
     * Returns the resolved adaptation config for an entity, or {@code null} on the client / when the
     * entity has no config.
     */
    public static DamageAdaptationConfig getEntityConfig(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return null;
        }
        return living.level().isClientSide ? null : AdaptationConfigResolver.getConfig(living);
    }

    public static boolean isInvulnerable(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }
        DamageAdaptationConfig config = getEntityConfig(living);
        if (config == null) {
            return false;
        } else {
            long currentTime = living.level().getGameTime();
            int minKillDuration = config.getMinimumKillDuration();
            if (minKillDuration > 0) {
                Long spawnTime = spawnTimeMap.get(living);
                if (spawnTime != null && currentTime - spawnTime < (long) minKillDuration) {
                    return true;
                }
            }

            int minAttackInterval = config.getMinimumAttackInterval();
            if (minAttackInterval > 0) {
                Long delayedHurt = delayedHurtTimeMap.get(living);
                if (delayedHurt != null && currentTime - delayedHurt < (long) minAttackInterval) {
                    return true;
                }
            }

            return false;
        }
    }

    public static boolean isInBrokenAdaptation(LivingEntity entity) {
        return IAdaptationData.get(entity).isInBrokenAdaptation(entity.level().getGameTime());
    }

    public static float getRemainingBrokenAdaptationTime(LivingEntity entity) {
        IAdaptationData data = IAdaptationData.get(entity);
        long currentTick = entity.level().getGameTime();
        long remaining = Math.max(0L, data.getBrokenAdaptationEndTick() - currentTick);
        return (float) remaining / 20.0F;
    }

    public static int getDeathCount(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return 0;
        }
        return IAdaptationData.get(living).getDeathCount();
    }

    public static void recordDeath(Entity entity) {
        if (entity instanceof LivingEntity living) {
            IAdaptationData.get(living).incrementDeathCount();
        }
    }

    private static float getDamageMultiplier(LivingEntity entity, DamageType damageType) {
        DamageAdaptationConfig config = getEntityConfig(entity);
        if (config == null) {
            return 1.0F;
        } else {
            Object damageTypeKey = getDamageTypeKey(entity, damageType, config);
            return damageTypeKey == null ? 1.0F : config.getDamageMultipliers().getOrDefault(damageTypeKey, 1.0F);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object getDamageTypeKey(
            LivingEntity entity, DamageType damageType, DamageAdaptationConfig config) {
        Registry<DamageType> registry = entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        ResourceLocation damageTypeId = registry.getKey(damageType);
        if (damageTypeId == null) {
            return null;
        } else {
            Optional<Holder.Reference<DamageType>> holder =
                    registry.getHolder(ResourceKey.create(Registries.DAMAGE_TYPE, damageTypeId));

            for (Object key : config.getDamageMultipliers().keySet()) {
                if (key instanceof ResourceLocation loc && damageTypeId.equals(loc)) {
                    return key;
                }

                if (key instanceof TagKey<?> tag && holder.isPresent()
                        && holder.get().is((TagKey<DamageType>) tag)) {
                    return key;
                }
            }

            return damageTypeId;
        }
    }

    /**
     * Replacement for the old {@code LivingHurtEvent} handler.
     */
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        DamageAdaptationConfig config = getEntityConfig(entity);
        if (isInvulnerable(entity)) {
            event.setCanceled(true);
        } else if (config != null) {
            DamageType damageType = event.getSource().type();
            Object damageTypeKey = getDamageTypeKey(entity, damageType, config);
            float multiplier = getDamageMultiplier(entity, damageType);
            float originalDamage = event.getAmount();
            float multipliedDamage = originalDamage * multiplier;
            boolean shouldTriggerBroken = false;
            if (multipliedDamage > 0.0F && damageTypeKey != null
                    && config.shouldTriggerBrokenAdaptation(damageTypeKey, multipliedDamage)) {
                shouldTriggerBroken = true;
                IAdaptationData data = IAdaptationData.get(entity);
                long durationTicks = (long) (config.getBrokenAdaptationDuration() * 20.0F);
                data.setBrokenAdaptationEndTick(entity.level().getGameTime() + durationTicks);
            }

            if (multiplier < 0.0F && originalDamage < 0.0F) {
                float finalDamage = Math.abs(originalDamage) * Math.abs(multiplier);
                event.setAmount(finalDamage);
                recordHurtTime(entity);
                scheduleDelayedHurtTime(entity);
                processDamageAdaptation(entity, config, event, finalDamage, shouldTriggerBroken, damageTypeKey);
            } else if (multiplier < 0.0F && originalDamage > 0.0F) {
                float healing = Math.abs(multipliedDamage);
                handleNegativeDamage(entity, event.getSource(), -healing);
                event.setCanceled(true);
            } else if (multiplier > 0.0F && originalDamage < 0.0F) {
                event.setAmount(multipliedDamage);
                handleNegativeDamage(entity, event.getSource(), multipliedDamage);
                event.setCanceled(true);
            } else {
                event.setAmount(multipliedDamage);
                recordHurtTime(entity);
                scheduleDelayedHurtTime(entity);
                processDamageAdaptation(entity, config, event, multipliedDamage, shouldTriggerBroken, damageTypeKey);
            }

            String keyStr = damageTypeKeyToString(damageTypeKey);
            if (keyStr != null) {
                serverLastDamageTypeKey.put(entity, keyStr);
            }
        }
    }

    public static int getLastAdaptationEventType(LivingEntity entity) {
        if (entity.level().isClientSide) {
            Long expireTick = clientEventExpireTick.get(entity);
            if (expireTick == null) {
                return 0;
            } else if (entity.level().getGameTime() > expireTick) {
                clientEventExpireTick.remove(entity);
                clientEventType.remove(entity);
                return 0;
            } else {
                return clientEventType.getOrDefault(entity, Integer.valueOf(0));
            }
        } else {
            Long time = lastAdaptationEventTime.get(entity);
            if (time == null) {
                return 0;
            } else if (entity.level().getGameTime() - time > EVENT_DISPLAY_DURATION_TICKS) {
                lastAdaptationEventType.remove(entity);
                lastAdaptationEventTime.remove(entity);
                return 0;
            } else {
                return lastAdaptationEventType.getOrDefault(entity, Integer.valueOf(0));
            }
        }
    }

    private static void setLastAdaptationEventType(LivingEntity entity, int type) {
        lastAdaptationEventType.put(entity, type);
        lastAdaptationEventTime.put(entity, entity.level().getGameTime());
        if (!entity.level().isClientSide) {
            ModNetwork.sendToAllTracking(new AdaptationEventSyncPacket(entity.getId(), type), entity);
        }
    }

    private static void processDamageAdaptation(
            LivingEntity entity,
            DamageAdaptationConfig config,
            LivingIncomingDamageEvent event,
            float currentDamage,
            boolean isBrokenTriggered,
            Object damageTypeKey) {
        IAdaptationData data = IAdaptationData.get(entity);
        long currentTick = entity.level().getGameTime();
        if (!data.isInBrokenAdaptation(currentTick) && !isBrokenTriggered) {
            String keyStr = damageTypeKeyToString(damageTypeKey);
            if (keyStr != null && config.isDamageTypeAdaptable(damageTypeKey)) {
                int oldAdapt = data.getAdaptationLevel(keyStr);
                int newAdapt = oldAdapt;
                boolean canAdapt = canAdaptDamageType(entity, config, damageTypeKey, data);
                float chance = config.getAdaptationProbability();
                if (canAdapt && oldAdapt < config.getMaxAdaptations()
                        && entity.getRandom().nextFloat() < chance) {
                    newAdapt = oldAdapt + 1;
                    data.setAdaptationLevel(keyStr, newAdapt);
                    syncAdaptationData(entity, keyStr, newAdapt, config.getMaxAdaptations());
                }

                float reduction = 1.0F - (float) newAdapt * config.getAdaptationPercentagePerInjury();
                float finalDamage = currentDamage * reduction;
                event.setAmount(finalDamage);
                if (!entity.level().isClientSide) {
                    int maxAdapt = data.getMaxAdaptations();
                    if (newAdapt >= maxAdapt) {
                        NeoForge.EVENT_BUS.post(new AdaptationEffectEvent(
                                entity, AdaptationEffectEvent.Type.FULL_ADAPTATION));
                        setLastAdaptationEventType(entity, 2);
                    } else if (newAdapt > oldAdapt) {
                        NeoForge.EVENT_BUS.post(new AdaptationEffectEvent(
                                entity, AdaptationEffectEvent.Type.PARTIAL_ADAPTATION));
                        setLastAdaptationEventType(entity, 1);
                    }
                }
            } else {
                event.setAmount(currentDamage);
            }
        } else {
            event.setAmount(currentDamage);
        }
    }

    private static boolean canAdaptDamageType(
            LivingEntity entity, DamageAdaptationConfig config, Object damageTypeKey, IAdaptationData data) {
        if (config.getMaxAdaptableDamageTypes() == -1) {
            return true;
        } else {
            String keyStr = damageTypeKeyToString(damageTypeKey);
            long adaptedCount = data.getAllAdaptations()
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue() > 0
                            && config.isDamageTypeAdaptable(stringToDamageTypeKey(e.getKey())))
                    .count();
            return data.getAdaptationLevel(keyStr) > 0
                    ? true
                    : adaptedCount < (long) config.getMaxAdaptableDamageTypes();
        }
    }

    private static void handleNegativeDamage(LivingEntity entity, DamageSource source, float amount) {
        if (amount < 0.0F) {
            entity.heal(-amount);
        }
    }

    public static void recordHurtTime(LivingEntity entity) {
        lastHurtTimeMap.put(entity, entity.level().getGameTime());
    }

    private static void scheduleDelayedHurtTime(LivingEntity entity) {
        if (!entity.level().isClientSide) {
            entity.level().getServer().execute(() -> {
                if (entity.isAlive()) {
                    delayedHurtTimeMap.put(entity, entity.level().getGameTime());
                }
            });
        }
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        DamageAdaptationConfig config = getEntityConfig(entity);
        if (config != null) {
            int minKills = config.getMinimumKillCount();
            if (minKills > 0) {
                IAdaptationData data = IAdaptationData.get(entity);
                int currentDeaths = data.getDeathCount();
                data.setDeathCount(++currentDeaths);
                long currentTime = entity.level().getGameTime();
                if (currentDeaths < minKills) {
                    event.setCanceled(true);
                    entity.setHealth(entity.getMaxHealth());
                    spawnTimeMap.put(entity, currentTime);
                    if (!entity.level().isClientSide && entity.level() instanceof ServerLevel serverLevel) {
                        RandomSource random = entity.getRandom();
                        int count = 7 + random.nextInt(6);
                        AABB bb = entity.getBoundingBox();
                        DustParticleOptions dust = new DustParticleOptions(new Vector3f(0.2F, 1.0F, 0.2F), 1.0F);

                        for (int i = 0; i < count; i++) {
                            double x = bb.minX + random.nextDouble() * (bb.maxX - bb.minX);
                            double y = bb.minY + random.nextDouble() * (bb.maxY - bb.minY);
                            double z = bb.minZ + random.nextDouble() * (bb.maxZ - bb.minZ);
                            serverLevel.sendParticles(dust, x, y, z, 1, 0.0, 0.0, 0.0, 0.1);
                        }
                    }
                } else {
                    data.setDeathCount(0);
                    spawnTimeMap.remove(entity);
                }
            }
        }
    }

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity living) {
            DamageAdaptationConfig config = getEntityConfig(living);
            if (config != null) {
                spawnTimeMap.put(living, living.level().getGameTime());
                IAdaptationData data = IAdaptationData.get(living);
                if (data.getMaxAdaptations() == 0) {
                    data.setMaxAdaptations(config.getMaxAdaptations());
                }
            }
        }
    }

    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity living) {
            onEntityRemoved(living);
        }
    }

    public static void onEntityRemoved(LivingEntity entity) {
        spawnTimeMap.remove(entity);
        lastHurtTimeMap.remove(entity);
        delayedHurtTimeMap.remove(entity);
        clientAdaptationData.remove(entity);
        clientLastDamageTypeKey.remove(entity);
        clientLastDamageTypeAdaptable.remove(entity);
        clientEventExpireTick.remove(entity);
        clientEventType.remove(entity);
        clientMaxAdaptations.remove(entity);
    }

    private static void syncAdaptationData(
            LivingEntity entity, String damageTypeKeyStr, int level, int maxAdaptations) {
        if (!entity.level().isClientSide) {
            ModNetwork.sendToAllTracking(
                    new AdaptationDataSyncPacket(entity.getId(), level, damageTypeKeyStr, maxAdaptations), entity);
        }
    }

    public static void updateClientAdaptationData(
            LivingEntity entity, int adaptationLevel, String damageTypeKeyStr, int maxAdaptations) {
        if (entity.level().isClientSide) {
            Map<String, Integer> entityMap = clientAdaptationData.computeIfAbsent(entity, k -> new HashMap<>());
            entityMap.put(damageTypeKeyStr, adaptationLevel);
            clientLastDamageTypeKey.put(entity, damageTypeKeyStr);
            clientMaxAdaptations.put(entity, maxAdaptations);
            DamageAdaptationConfig config = getEntityConfig(entity);
            if (config != null) {
                Object keyObj = stringToDamageTypeKey(damageTypeKeyStr);
                boolean adaptable = keyObj != null && config.isDamageTypeAdaptable(keyObj);
                clientLastDamageTypeAdaptable.put(entity, adaptable);
            } else {
                clientLastDamageTypeAdaptable.put(entity, true);
            }
        }
    }

    public static int getClientMaxAdaptations(LivingEntity entity) {
        return entity.level().isClientSide
                ? clientMaxAdaptations.getOrDefault(entity, Integer.valueOf(0))
                : 0;
    }

    public static String debugEventState(LivingEntity entity) {
        return "id="
                + entity.getId()
                + " byId="
                + CLIENT_EVENT_BY_ID.get(entity.getId())
                + " byIdExpire="
                + CLIENT_EVENT_EXPIRE_BY_ID.get(entity.getId())
                + " byKey="
                + CLIENT_EVENT_BY_KEY.get(eventKey(entity))
                + " instance="
                + clientEventType.get(entity)
                + " now="
                + entity.level().getGameTime();
    }

    public static int getActiveAdaptationEvent(Entity entity) {
        if (!(entity instanceof LivingEntity living) || !entity.level().isClientSide) {
            return -1;
        } else {
            long now = entity.level().getGameTime();
            ensureSameLevel(entity.level());
            Integer byId = CLIENT_EVENT_BY_ID.get(entity.getId());
            Long byIdExpire = CLIENT_EVENT_EXPIRE_BY_ID.get(entity.getId());
            if (byId != null && byIdExpire != null && byIdExpire > now) {
                return byId;
            } else {
                if (byIdExpire != null) {
                    CLIENT_EVENT_BY_ID.remove(entity.getId());
                    CLIENT_EVENT_EXPIRE_BY_ID.remove(entity.getId());
                }

                String key = eventKey(entity);
                Long keyedExpire = CLIENT_EVENT_EXPIRE_BY_KEY.get(key);
                Integer keyedType = CLIENT_EVENT_BY_KEY.get(key);
                if (keyedExpire != null && keyedType != null) {
                    if (keyedExpire > now) {
                        return keyedType;
                    }

                    CLIENT_EVENT_EXPIRE_BY_KEY.remove(key);
                    CLIENT_EVENT_BY_KEY.remove(key);
                }

                Long expire = clientEventExpireTick.get(living);
                if (expire != null && expire > now) {
                    Integer type = clientEventType.get(living);
                    return type == null ? -1 : type;
                } else {
                    clientEventType.remove(living);
                    clientEventExpireTick.remove(living);
                    return -1;
                }
            }
        }
    }

    public static void updateClientAdaptationEvent(LivingEntity entity, int eventType) {
        if (entity.level().isClientSide) {
            long expireTick = entity.level().getGameTime() + EVENT_DISPLAY_DURATION_TICKS;
            clientEventExpireTick.put(entity, expireTick);
            clientEventType.put(entity, eventType);
            CLIENT_EVENT_BY_KEY.put(eventKey(entity), eventType);
            CLIENT_EVENT_EXPIRE_BY_KEY.put(eventKey(entity), expireTick);
        }
    }

    private static int getAdaptationDataInternal(LivingEntity entity) {
        if (entity.level().isClientSide) {
            String lastKey = clientLastDamageTypeKey.get(entity);
            if (lastKey != null) {
                Boolean adaptable = clientLastDamageTypeAdaptable.get(entity);
                if (adaptable != null && adaptable) {
                    Map<String, Integer> map = clientAdaptationData.get(entity);
                    return map == null ? 0 : map.getOrDefault(lastKey, 0);
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        } else {
            String lastKey = serverLastDamageTypeKey.get(entity);
            if (lastKey != null) {
                return IAdaptationData.get(entity).getAdaptationLevel(lastKey);
            }

            return 0;
        }
    }

    public static int getAdaptationData(LivingEntity entity) {
        return getAdaptationDataInternal(entity);
    }

    public static boolean shouldShowAdaptationEffect(LivingEntity entity) {
        return getAdaptationData(entity) > 0 && entity.hurtTime > 0;
    }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new DamageAdaptationManager());
        event.addListener(new EntityAdaptationMapping());
        event.addListener(new AdaptationPriorityRuleLoader());
    }
}
