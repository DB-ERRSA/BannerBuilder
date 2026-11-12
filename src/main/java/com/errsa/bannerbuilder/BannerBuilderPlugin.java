package com.errsa.bannerbuilder;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BannerBuilderPlugin extends JavaPlugin {

    private final Map<UUID, BannerSession> sessions = new HashMap<>();

    private Economy economy;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        setupEconomy();

        BannerCommand command = new BannerCommand(this);

        if (getCommand("bannerbuilder") != null) {
            getCommand("bannerbuilder").setExecutor(command);
        }

        getServer().getPluginManager().registerEvents(
                new BannerGuiListener(this, command),
                this
        );

        getServer().getPluginManager().registerEvents(
                new LoomLinkListener(this, command),
                this
        );

        getLogger().info("Loom+ BannerBuilder enabled.");
    }

    @Override
    public void onDisable() {
        sessions.clear();
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning(
                    "Vault was not found. Money pricing will be unavailable."
            );
            return;
        }

        RegisteredServiceProvider<Economy> registration =
                getServer()
                        .getServicesManager()
                        .getRegistration(Economy.class);

        if (registration == null) {
            getLogger().warning(
                    "Vault was found, but no economy provider was registered."
            );
            return;
        }

        economy = registration.getProvider();

        getLogger().info(
                "Connected to economy provider: "
                        + economy.getName()
        );
    }

    public BannerSession getSession(UUID uuid) {
        return sessions.computeIfAbsent(
                uuid,
                ignored -> new BannerSession()
        );
    }

    public Economy getEconomy() {
        return economy;
    }

    public boolean isInfinitePatterns() {
        return getConfig().getBoolean(
                "banner.infinite-patterns",
                false
        );
    }

    public int getMaxPatterns() {
        return Math.max(
                1,
                getConfig().getInt("banner.max-patterns", 16)
        );
    }

    public boolean isPricingEnabled() {
        return getConfig().getBoolean(
                "pricing.enabled",
                true
        );
    }

    public String getPricingMode() {
        return getConfig()
                .getString("pricing.mode", "VANILLA_PLUS")
                .toUpperCase();
    }

    public int getFreeLayers() {
        return Math.max(
                0,
                getConfig().getInt("pricing.free-layers", 6)
        );
    }

    public boolean isMoneyEnabled() {
        return isPricingEnabled()
                && getConfig().getBoolean(
                "pricing.money.enabled",
                false
        );
    }

    public double getCostPerPaidLayer() {
        return Math.max(
                0.0,
                getConfig().getDouble(
                        "pricing.money.cost-per-paid-layer",
                        0.0
                )
        );
    }

    public boolean areMaterialsEnabled() {
        return getConfig().getBoolean(
                "materials.enabled",
                true
        );
    }

    public boolean shouldConsumeDyes() {
        return getConfig().getBoolean(
                "materials.consume-dyes",
                true
        );
    }

    public boolean shouldConsumePatternItems() {
        return getConfig().getBoolean(
                "materials.consume-pattern-items",
                false
        );
    }

    public boolean isResetEnabled() {
        return getConfig().getBoolean(
                "reset.enabled",
                true
        );
    }

    public boolean isResetMoneyEnabled() {
        return getConfig().getBoolean(
                "reset.money.enabled",
                false
        );
    }

    public double getResetMoneyCost() {
        if (!isResetMoneyEnabled()) {
            return 0.0;
        }

        return Math.max(
                0.0,
                getConfig().getDouble(
                        "reset.money.cost",
                        0.0
                )
        );
    }

    public boolean resetRequiresBaseColorDye() {
        return getConfig().getBoolean(
                "reset.materials.require-base-color-dye",
                true
        );
    }

    public boolean consumeResetDye() {
        return getConfig().getBoolean(
                "reset.materials.consume-base-color-dye",
                true
        );
    }

    public boolean isLoomLinkEnabled() {
        return getConfig().getBoolean(
                "loom-link.enabled",
                true
        );
    }

    public boolean loomRequiresSneak() {
        return getConfig().getBoolean(
                "loom-link.require-sneak",
                true
        );
    }

    public boolean notifyOnLoomPlace() {
        return getConfig().getBoolean(
                "loom-link.notify-on-place",
                true
        );
    }

    public double getDiscountPercent(Player player) {
        Discount discount = findBestDiscount(player);
        return discount.percent();
    }

    public String getDiscountName(Player player) {
        Discount discount = findBestDiscount(player);
        return discount.name();
    }

    private Discount findBestDiscount(Player player) {
        ConfigurationSection section =
                getConfig().getConfigurationSection(
                        "discounts.ranks"
                );

        if (section == null) {
            return new Discount("", 0.0);
        }

        String bestName = "";
        double bestPercent = 0.0;

        for (String key : section.getKeys(false)) {
            String permission = section.getString(
                    key + ".permission",
                    ""
            );

            double percent = section.getDouble(
                    key + ".percent-off",
                    0.0
            );

            if (permission == null || permission.isBlank()) {
                continue;
            }

            if (player.hasPermission(permission)
                    && percent > bestPercent) {
                bestName = key;
                bestPercent = percent;
            }
        }

        bestPercent = Math.max(
                0.0,
                Math.min(100.0, bestPercent)
        );

        return new Discount(bestName, bestPercent);
    }

    public int getBedrockVisibleLayerLimit() {
        return Math.max(1, getConfig().getInt("bedrock.max-visible-layers", 6));
    }

    public boolean isBedrockPlayer(Player player) {
        try {
            Class<?> apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Object api = apiClass.getMethod("getInstance").invoke(null);
            Object result = apiClass
                    .getMethod("isFloodgatePlayer", UUID.class)
                    .invoke(api, player.getUniqueId());
            return result instanceof Boolean && (Boolean) result;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    public String message(String body) {
        String prefix = getConfig().getString(
                "messages.prefix",
                "&8[&bLoom+&8] &7"
        );
        return ChatColor.translateAlternateColorCodes(
                '&',
                (prefix == null ? "" : prefix) + body
        );
    }

    public void sendTransaction(
            Player player,
            String label,
            double delta,
            double before,
            double after
    ) {
        player.sendMessage(message("&6Transaction: &f" + label));
        player.sendMessage(message(
                "&7Bank: &f$" + BannerCommand.formatMoney(before)
                        + " &8→ &a$" + BannerCommand.formatMoney(after)
                        + " &7(" + (delta >= 0 ? "+" : "")
                        + "$" + BannerCommand.formatMoney(delta) + "&7)"
        ));
    }

    private record Discount(
            String name,
            double percent
    ) {
    }
}