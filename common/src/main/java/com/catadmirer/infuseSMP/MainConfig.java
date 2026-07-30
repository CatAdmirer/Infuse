package com.catadmirer.infuseSMP;

import com.catadmirer.infuseSMP.effects.InfuseEffect;
import net.kyori.adventure.key.Key;

import java.util.List;

public interface MainConfig {
    List<Key> getBlacklistedWorlds(InfuseEffect effect);

    String lang();

    boolean allowInfiniteEffects();

    int ritualDuration();

    int ritualDurationEnder();

    boolean ritualBeacon();

    boolean emptyEffectIcon();

    boolean playerHeadDrops();

    boolean enableDiscordBroadcasts();

    String discordWebhookUrl();

    boolean brewingGui();

    String effectDrops();

    boolean joinEffectsEnabled();

    List<InfuseEffect> joinEffects();

    boolean enableApophis();

    boolean regularBroadcast();

    boolean enableThief();

    /**
     * Gets the amount of each effect that can be crafted
     *
     * @param effect The effect to check
     * @return The number of effects that can be crafted of the specified {@link InfuseEffect}.
     */
    int getCraftLimit(InfuseEffect effect);

    double emeraldLockDurationSeconds();

    boolean invisHideKills();

    boolean invisHideDeaths();

    long cooldown(InfuseEffect effect);

    long duration(InfuseEffect effect);

    int speedDashMultiplier();

    int speedPlayerVelocityMultiplier();

    int oceanPullInterval();

    int oceanPullRadius();

    double oceanPullStrength();

    int hitCounterDecaySeconds();

    int emeraldExpPerHit();

    float emeraldExpPercent();

    float emeraldPercentExpToShare();

    int apophisExpPerHit();

    float apophisExpPercent();

    float apophisPercentExpToShare();

    double apophisLockDurationSeconds();

    int apophisLootingLevel();

    double apophisSparkRadius();

    double apophisSparkExplosionRadius();

    double apophisLavaWalkSpeed();

    int apophisXpMultiplierStandard();

    int apophisXpMultiplierSpark();

    int emeraldLootingLevel();

    int hasteFortuneLevel();

    int hasteEfficiencyLevel();

    int hasteUnbreakingLevel();

    double emeraldMultiplierStandard();

    double emeraldMultiplierUseEffect();

    double enderPassiveRadius();

    int enderSparkMaxDistance();

    double featherLandRadius();

    double featherLandDamage();

    double firePassiveWalkSpeed();

    double fireSparkRadius();

    double fireSparkExplosionRadius();

    int frostPassiveSnowChangingRadius();

    double frostPassiveWalkSpeed();

    double frostSparkRadius();

    int oceanPassiveDrownStrength();

    int oceanPassiveDrownDamage();

    int oceanSparkDrownStrength();

    int oceanSparkDrownDamage();

    double regenSparkHealTrustedRadius();

    double thunderSparkBaseRadius();

    double thunderSparkPerPlayerBoostRadius();
}
