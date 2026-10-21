package org.tdddd.epca.impl.compat;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.ModList;
import org.tdddd.epca.impl.overworld.registry.ModEffects;

/**
 * 效果替身：装了对应模组时，把本模组的效果换成它们的效果。
 *
 * <ul>
 *   <li>装了 <b>legendarysurvivaloverhaul</b> → {@code epic:viral} 换成它的
 *       {@code legendarysurvivaloverhaul:vulnerability}（同样表示"受伤加重"）。</li>
 *   <li>装了 <b>sa_core</b>（Survivor's Arsenal）→ {@code epca:bleeding} 换成它的
 *       {@code sa_core:bleeding}（它自带流血 DoT 与 HUD）。</li>
 * </ul>
 *
 * <p>设计要点：</p>
 * <ol>
 *   <li><b>不编译依赖</b>：只按 {@link ResourceLocation} 去 {@link BuiltInRegistries#MOB_EFFECT} 查，
 *       查不到就静默回落，不会因为对方改名/换版本而崩。</li>
 *   <li><b>只替换"施加"和"叠加"的判定</b>；我们自己的伤害结算逻辑
 *       （{@code LivingHurtEventHandler} / {@code ViralEffectEventHandler} / 流血 DoT）仍然只认
 *       {@code epca:viral} / {@code epca:bleeding}，避免和对方的实现叠加成双倍伤害。</li>
 *   <li>旧存档里残留的 {@code epca:viral} 仍然能被 {@link #activeViral(LivingEntity)} 识别，
 *       所以替换后老存档不会"掉层数"。</li>
 * </ol>
 */
public final class EffectCompat {

    private static final ResourceLocation LSO_VULNERABILITY =
            ResourceLocation.fromNamespaceAndPath("legendarysurvivaloverhaul", "vulnerability");
    private static final ResourceLocation SA_CORE_BLEEDING =
            ResourceLocation.fromNamespaceAndPath("sa_core", "bleeding");

    private static boolean lookedUp;
    private static Holder<MobEffect> viralSubstitute;
    private static Holder<MobEffect> bleedingSubstitute;

    private EffectCompat() {
    }

    private static void lookup() {
        if (lookedUp) {
            return;
        }
        lookedUp = true;

        viralSubstitute = find("legendarysurvivaloverhaul", LSO_VULNERABILITY);
        bleedingSubstitute = find("sa_core", SA_CORE_BLEEDING);
    }

    /** 只在该 mod 确实加载、且该效果确实存在时才返回；任何异常都回落。 */
    private static Holder<MobEffect> find(String modId, ResourceLocation id) {
        try {
            ModList modList = ModList.get();
            if (modList == null || !modList.isLoaded(modId)) {
                return null;
            }
            Holder<MobEffect> holder = BuiltInRegistries.MOB_EFFECT.getHolder(id).orElse(null);
            if (holder == null) {
                org.tdddd.epca.impl.epca.LOGGER.warn(
                        "mod {} is loaded but effect {} is missing; falling back to the epca effect",
                        modId, id);
            }
            return holder;
        } catch (Throwable t) {
            return null;
        }
    }

    public static boolean usingViralSubstitute() {
        lookup();
        return viralSubstitute != null;
    }

    public static boolean usingBleedingSubstitute() {
        lookup();
        return bleedingSubstitute != null;
    }

    /** 要"施加"的病毒/易伤效果（有替身时用替身）。 */
    public static Holder<MobEffect> viral() {
        lookup();
        return viralSubstitute != null ? viralSubstitute : ModEffects.VIRAL;
    }

    /** 要"施加"的流血效果（有替身时用替身）。 */
    public static Holder<MobEffect> bleeding() {
        lookup();
        return bleedingSubstitute != null ? bleedingSubstitute : ModEffects.BLEEDING;
    }

    // ------------------------------------------------------------------------------------------------
    // 构造实例（参数与原版 MobEffectInstance 构造器一一对应）
    // ------------------------------------------------------------------------------------------------

    public static MobEffectInstance viralInstance(int duration, int amplifier) {
        return new MobEffectInstance(viral(), duration, amplifier);
    }

    public static MobEffectInstance viralInstance(int duration, int amplifier, boolean ambient, boolean visible) {
        return new MobEffectInstance(viral(), duration, amplifier, ambient, visible);
    }

    public static MobEffectInstance viralInstance(int duration, int amplifier, boolean ambient, boolean visible,
                                                  boolean showIcon) {
        return new MobEffectInstance(viral(), duration, amplifier, ambient, visible, showIcon);
    }

    public static MobEffectInstance bleedingInstance(int duration, int amplifier) {
        return new MobEffectInstance(bleeding(), duration, amplifier);
    }

    public static MobEffectInstance bleedingInstance(int duration, int amplifier, boolean ambient, boolean visible) {
        return new MobEffectInstance(bleeding(), duration, amplifier, ambient, visible);
    }

    public static MobEffectInstance bleedingInstance(int duration, int amplifier, boolean ambient, boolean visible,
                                                     boolean showIcon) {
        return new MobEffectInstance(bleeding(), duration, amplifier, ambient, visible, showIcon);
    }

    // ------------------------------------------------------------------------------------------------
    // 读取当前生效的那一个（用于"叠层"与"免疫"判定）
    // ------------------------------------------------------------------------------------------------

    /** 实体身上当前生效的病毒/易伤效果；替身与本体都认（兼容旧存档）。 */
    public static MobEffectInstance activeViral(LivingEntity entity) {
        lookup();
        if (viralSubstitute != null) {
            MobEffectInstance substituted = entity.getEffect(viralSubstitute);
            if (substituted != null) {
                return substituted;
            }
        }
        return entity.getEffect(ModEffects.VIRAL);
    }

    /** 实体身上当前生效的流血效果；替身与本体都认（兼容旧存档）。 */
    public static MobEffectInstance activeBleeding(LivingEntity entity) {
        lookup();
        if (bleedingSubstitute != null) {
            MobEffectInstance substituted = entity.getEffect(bleedingSubstitute);
            if (substituted != null) {
                return substituted;
            }
        }
        return entity.getEffect(ModEffects.BLEEDING);
    }

    /** {@link Holder} 形式：用于事件里"这个效果要不要拦"的判定。 */
    public static boolean isViral(Holder<MobEffect> effect) {
        lookup();
        return effect.is(ModEffects.VIRAL) || (viralSubstitute != null && effect.is(viralSubstitute));
    }

    public static boolean isBleeding(Holder<MobEffect> effect) {
        lookup();
        return effect.is(ModEffects.BLEEDING) || (bleedingSubstitute != null && effect.is(bleedingSubstitute));
    }

    // ------------------------------------------------------------------------------------------------
    // 数据文件（entity_attack_effects/*.json 等）里的 id 重映射
    // ------------------------------------------------------------------------------------------------

    /**
     * 数据文件里写的是 {@code epca:bleeding} / {@code epca:viral}；装了替身时把它们映射到替身 id，
     * 这样 {@code entity_attack_effects/*.json} 不需要为两种环境各写一份。
     */
    public static String mapDataEffectId(String id) {
        lookup();
        if (viralSubstitute != null && "epca:viral".equals(id)) {
            return LSO_VULNERABILITY.toString();
        }
        if (bleedingSubstitute != null && "epca:bleeding".equals(id)) {
            return SA_CORE_BLEEDING.toString();
        }
        return id;
    }

    /**
     * 免疫判定用：替身效果应当继承 {@code epca:viral} / {@code epca:bleeding} 的免疫名单，
     * 否则装了 LSO 之后虫族会突然被"内伤"影响。
     */
    public static String immunityAlias(String id) {
        lookup();
        if (viralSubstitute != null && LSO_VULNERABILITY.toString().equals(id)) {
            return "epca:viral";
        }
        if (bleedingSubstitute != null && SA_CORE_BLEEDING.toString().equals(id)) {
            return "epca:bleeding";
        }
        return id;
    }

}
