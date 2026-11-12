package com.errsa.bannerbuilder;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.joml.Math;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BannerCommand implements CommandExecutor {

    public static final String GUI_TITLE =
            "§9§lLoom+ Banner Builder";

    public static final int UNDO_SLOT = 46;
    public static final int RESET_SLOT = 47;
    public static final int PREVIOUS_SLOT = 48;
    public static final int PREVIEW_SLOT = 49;
    public static final int NEXT_SLOT = 50;
    public static final int DUPLICATE_SLOT = 51;
    public static final int APPLY_SLOT = 52;

    private static final int[] COLOR_SLOTS = {
            9, 10, 11, 12, 13, 14, 15, 16, 17,


            19, 20, 21, 22, 23, 24, 25
    };

    private static final DyeColor[] COLORS = {
            DyeColor.WHITE,
            DyeColor.ORANGE,
            DyeColor.MAGENTA,
            DyeColor.LIGHT_BLUE,
            DyeColor.YELLOW,
            DyeColor.LIME,
            DyeColor.PINK,
            DyeColor.GRAY,
            DyeColor.LIGHT_GRAY,
            DyeColor.CYAN,
            DyeColor.PURPLE,
            DyeColor.BLUE,
            DyeColor.BROWN,
            DyeColor.GREEN,
            DyeColor.RED,
            DyeColor.BLACK
    };

    private static final int[] PAGE_ONE_PATTERN_SLOTS = {
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    };

    private static final int[] PAGE_TWO_PATTERN_SLOTS = {
            28, 29, 30, 31, 32, 33, 34,

            // Second row - 7 centered
            37, 38, 39, 40, 41, 42, 43
    };

    private static final int[] PAGE_THREE_PATTERN_SLOTS = {
            // Five centered
            29, 30, 31, 32, 33,

            // Five centered
            38, 39, 40, 41, 42
    };

    private int[] getPatternSlotsForPage(int page) {
        return switch (page) {
            case 2 -> PAGE_TWO_PATTERN_SLOTS;
            case 3 -> PAGE_THREE_PATTERN_SLOTS;
            default -> PAGE_ONE_PATTERN_SLOTS;
        };
    }

    private final BannerBuilderPlugin plugin;

    public BannerCommand(BannerBuilderPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    "This command can only be used by players."
            );
            return true;
        }

        openGui(player);
        return true;
    }

    public void openGui(Player player) {
        ItemStack heldItem =
                player.getInventory().getItemInMainHand();

        if (!isBanner(heldItem)) {
            player.sendMessage(
                    "§cHold a banner in your main hand first."
            );
            return;
        }

        if (!(heldItem.getItemMeta() instanceof BannerMeta meta)) {
            player.sendMessage(
                    "§cThe held item could not be read as a banner."
            );
            return;
        }

        BannerSession session =
                plugin.getSession(player.getUniqueId());

        session.loadBanner(
                heldItem.getType(),
                meta.getPatterns()
        );

        if (plugin.isBedrockPlayer(player)
                && session.totalLayerCount() > plugin.getBedrockVisibleLayerLimit()) {
            player.sendMessage(plugin.message(
                    "§eBedrock only displays the first §f"
                            + plugin.getBedrockVisibleLayerLimit()
                            + "§e banner layers. This banner has §f"
                            + session.totalLayerCount()
                            + "§e."
            ));
        }

        BannerGuiHolder holder = new BannerGuiHolder(player.getUniqueId());
        Inventory inventory = Bukkit.createInventory(holder, 54, GUI_TITLE);
        holder.attach(inventory);

        player.openInventory(inventory);
        refreshGui(player);
    }

    public void refreshGui(Player player) {
        if (!BannerGuiHolder.isEditor(player.getUniqueId(), player.getOpenInventory().getTopInventory())) {
            return;
        }

        Inventory inventory =
                player.getOpenInventory().getTopInventory();

        BannerSession session =
                plugin.getSession(player.getUniqueId());

        inventory.clear();

        drawBackground(inventory);
        drawColors(inventory, session);
        drawPatternPage(inventory, session);
        drawControls(inventory, player, session);
    }

    private void drawBackground(Inventory inventory) {
        // Intentionally leave unused slots empty. The old full pane background
        // made an already information-dense menu much harder to scan.
    }

    private void drawColors(
            Inventory inventory,
            BannerSession session
    ) {
        inventory.setItem(
                4,
                item(
                        Material.LOOM,
                        "§b§lSelect a Dye Color",
                        List.of(
                                "§7Selected: §f"
                                        + pretty(
                                        session
                                                .getSelectedColor()
                                                .name()
                                )
                        )
                )
        );

        for (int i = 0; i < COLORS.length; i++) {
            DyeColor color = COLORS[i];
            Material dye = toDyeMaterial(color);

            boolean selected =
                    session.getSelectedColor() == color;

            String prefix = selected ? "§a§l" : "§f";

            List<String> lore = new ArrayList<>();

            if (selected) {
                lore.add("§aCurrently selected");
            } else {
                lore.add("§7Click to select this color.");
            }

            inventory.setItem(
                    COLOR_SLOTS[i],
                    item(
                            dye,
                            prefix + pretty(color.name()),
                            lore
                    )
            );
        }
    }

    private void drawPatternPage(
            Inventory inventory,
            BannerSession session
    ) {
        List<BannerOption> options =
                BannerPatterns.getPage(session.getPage());

        int[] patternSlots =
                getPatternSlotsForPage(session.getPage());

        for (int i = 0;
             i < options.size() && i < patternSlots.length;
             i++) {

            BannerOption option = options.get(i);

            ItemStack icon;

            if (option.requiresPatternItem()) {
                icon = specialPatternItem(option);
            } else {
                icon = patternPreviewItem(option);
            }

            inventory.setItem(patternSlots[i], icon);
        }
    }

    private void drawControls(
            Inventory inventory,
            Player player,
            BannerSession session
    ) {
        inventory.setItem(
                UNDO_SLOT,
                item(
                        Material.BARRIER,
                        "§cUndo New Layer",
                        List.of(
                                "§7New layers: §f"
                                        + session.pendingLayerCount(),
                                "",
                                "§eClick to remove the",
                                "§emost recently added layer."
                        )
                )
        );

        if (shouldShowReset()) {
            inventory.setItem(
                    RESET_SLOT,
                    buildResetButton(player, session)
            );
        }

        inventory.setItem(
                PREVIOUS_SLOT,
                item(
                        Material.ARROW,
                        "§ePrevious Pattern Page",
                        List.of(
                                "§7Current page: §f"
                                        + session.getPage()
                                        + "§7/"
                                        + BannerPatterns.getPageCount()
                        )
                )
        );

        inventory.setItem(
                PREVIEW_SLOT,
                buildPreview(session)
        );

        inventory.setItem(
                NEXT_SLOT,
                item(
                        Material.ARROW,
                        "§eNext Pattern Page",
                        List.of(
                                "§7Page §f"
                                        + session.getPage()
                                        + "§7/"
                                        + BannerPatterns.getPageCount(),
                                "§7" + pageDescription(session.getPage())
                        )
                )
        );

        inventory.setItem(
                DUPLICATE_SLOT,
                buildDuplicateButton(player, session)
        );

        inventory.setItem(
                APPLY_SLOT,
                buildApplyButton(player, session)
        );
    }


    private ItemStack buildDuplicateButton(
            Player player,
            BannerSession session
    ) {
        ItemStack icon = new ItemStack(session.getBaseBannerMaterial());
        BannerMeta meta = (BannerMeta) icon.getItemMeta();
        meta.setDisplayName("§b§lDuplicate Banner");
        meta.setPatterns(session.getOriginalPatterns());

        int blanks = countBlankBanners(player, session.getBaseBannerMaterial());
        String baseName = pretty(session.getBaseBannerMaterial().name());

        List<String> lore = new ArrayList<>();
        lore.add("§7Copies the applied design onto");
        lore.add("§7one blank banner of the same color.");
        lore.add("");
        lore.add((blanks > 0 ? "§a" : "§c") + "1x " + baseName
                + " §7(Blank: " + blanks + ")");
        lore.add("§7No dye or money is used.");

        if (session.pendingLayerCount() > 0) {
            lore.add("");
            lore.add("§eApply your new layers first");
            lore.add("§eto include them in the copy.");
        }

        lore.add("");
        lore.add("§eClick to duplicate one banner.");
        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    private int countBlankBanners(
            Player player,
            Material bannerMaterial
    ) {
        int total = 0;
        int heldSlot = player.getInventory().getHeldItemSlot();
        ItemStack[] contents = player.getInventory().getStorageContents();

        for (int slot = 0; slot < contents.length; slot++) {
            if (slot == heldSlot) {
                continue;
            }

            ItemStack item = contents[slot];
            if (item == null || item.getType() != bannerMaterial) {
                continue;
            }

            if (item.getItemMeta() instanceof BannerMeta bannerMeta
                    && bannerMeta.getPatterns().isEmpty()) {
                total += item.getAmount();
            }
        }

        return total;
    }

    private ItemStack buildApplyButton(
            Player player,
            BannerSession session
    ) {
        ItemStack icon = new ItemStack(Material.EMERALD);
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(plugin.isMoneyEnabled()
                ? "§a§lBuy & Apply Banner"
                : "§a§lApply Changes");

        List<String> lore = new ArrayList<>();
        lore.add("§7Apply all pending changes");
        lore.add("§7to the banner in your hand.");
        lore.add("");
        lore.add("§7New layers: §f" + session.pendingLayerCount());
        lore.add("§7Total layers: §f" + session.totalLayerCount());
        lore.add("");

        MoneyQuote quote = getMoneyQuote(player, session);
        if (plugin.isMoneyEnabled()) {
            lore.add("§6Price:");
            appendMoneyLore(player, lore, quote);
            lore.add("");
        }

        lore.add("§eMaterials Needed:");
        Map<Material, Integer> materials = getMaterialBill(session);
        if (!plugin.areMaterialsEnabled() || materials.isEmpty()) {
            lore.add("§7None");
        } else {
            for (Map.Entry<Material, Integer> entry : materials.entrySet()) {
                int owned = countMaterial(player, entry.getKey());
                int required = entry.getValue();
                String color = owned >= required ? "§a" : "§c";
                lore.add(color + required + "x " + pretty(entry.getKey().name())
                        + " §7(You have " + owned + ")");
            }
        }

        lore.add("");
        lore.add(plugin.isMoneyEnabled()
                ? "§eClick to purchase and apply."
                : "§eClick to apply changes.");
        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    private ItemStack buildPreview(BannerSession session) {
        ItemStack banner = new ItemStack(
                session.getBaseBannerMaterial()
        );

        BannerMeta meta =
                (BannerMeta) banner.getItemMeta();

        meta.setDisplayName("§b§lLive Banner Preview");
        meta.setPatterns(session.getPreviewPatterns());

        meta.setLore(List.of(
                "§7Existing Layers: §f"
                        + session.originalLayerCount(),
                "§7New Layers: §f"
                        + session.pendingLayerCount(),
                "§7Total Layers: §f"
                        + session.totalLayerCount(),
                "§7Selected Color: §f"
                        + pretty(
                        session.getSelectedColor().name()
                )
        ));

        banner.setItemMeta(meta);
        return banner;
    }

    private ItemStack buildPriceInfo(
            Player player,
            BannerSession session
    ) {
        Material iconMaterial =
                plugin.isMoneyEnabled()
                        ? Material.GOLD_INGOT
                        : Material.CHEST;

        ItemStack icon = new ItemStack(iconMaterial);
        ItemMeta meta = icon.getItemMeta();

        if (plugin.isMoneyEnabled()) {
            meta.setDisplayName("§6§lPrice & Materials");
        } else {
            meta.setDisplayName("§e§lBill of Materials");
        }

        List<String> lore = new ArrayList<>();

        MoneyQuote quote = getMoneyQuote(player, session);

        if (plugin.isMoneyEnabled()) {
            appendMoneyLore(player, lore, quote);
            lore.add("");
        }

        lore.add("§eMaterials Needed:");

        Map<Material, Integer> materials =
                getMaterialBill(session);

        if (!plugin.areMaterialsEnabled()
                || materials.isEmpty()) {
            lore.add("§7None");
        } else {
            for (Map.Entry<Material, Integer> entry
                    : materials.entrySet()) {

                int owned = countMaterial(
                        player,
                        entry.getKey()
                );

                int required = entry.getValue();

                boolean hasEnough = owned >= required;

                String color =
                        hasEnough ? "§a" : "§c";

                lore.add(
                        color
                                + required
                                + "x "
                                + pretty(entry.getKey().name())
                                + " §7(You have "
                                + owned
                                + ")"
                );
            }
        }

        lore.add("");
        lore.add("§7Only newly added layers");
        lore.add("§7are included in this bill.");

        meta.setLore(lore);
        icon.setItemMeta(meta);

        return icon;
    }

    private void appendMoneyLore(
            Player player,
            List<String> lore,
            MoneyQuote quote
    ) {
        boolean economyAvailable =
                plugin.getEconomy() != null;

        double balance = economyAvailable
                ? plugin.getEconomy().getBalance(player)
                : 0.0;

        boolean canAfford =
                economyAvailable
                        && balance >= quote.finalPrice();

        if (quote.discountPercent() > 0) {
            lore.add(
                    "§a"
                            + pretty(quote.discountName())
                            + " Discount: §f"
                            + formatMoney(
                            quote.discountPercent()
                    )
                            + "%"
            );

            lore.add(
                    "§7Original Price: §m$"
                            + formatMoney(
                            quote.originalPrice()
                    )
            );

            lore.add(
                    (canAfford ? "§6Your Price: §e$"
                            : "§cYour Price: $")
                            + formatMoney(
                            quote.finalPrice()
                    )
            );
        } else {
            lore.add(
                    (canAfford || quote.finalPrice() <= 0
                            ? "§6Price: §e$"
                            : "§cPrice: $")
                            + formatMoney(
                            quote.finalPrice()
                    )
            );
        }

        if (economyAvailable) {
            lore.add(
                    "§7Balance: §f$"
                            + formatMoney(balance)
            );
        } else {
            lore.add("§cNo Vault economy provider found.");
        }
    }

    private ItemStack buildResetButton(
            Player player,
            BannerSession session
    ) {
        ItemStack icon =
                new ItemStack(Material.TNT);

        ItemMeta meta = icon.getItemMeta();

        meta.setDisplayName("§c§lReset Held Banner");

        List<String> lore = new ArrayList<>();

        lore.add("§7Remove every existing and");
        lore.add("§7pending banner layer.");
        lore.add("");

        double originalMoney =
                plugin.getResetMoneyCost();

        double discount =
                plugin.getDiscountPercent(player);

        double finalMoney =
                applyDiscount(originalMoney, discount);

        if (originalMoney > 0) {
            if (discount > 0) {
                lore.add(
                        "§a"
                                + pretty(
                                plugin.getDiscountName(player)
                        )
                                + " Discount: §f"
                                + formatMoney(discount)
                                + "%"
                );

                lore.add(
                        "§7Original: §m$"
                                + formatMoney(originalMoney)
                );

                lore.add(
                        "§6Your Cost: §e$"
                                + formatMoney(finalMoney)
                );
            } else {
                lore.add(
                        "§6Money Cost: §e$"
                                + formatMoney(finalMoney)
                );
            }
        }

        if (plugin.resetRequiresBaseColorDye()) {
            Material dye =
                    getBaseDye(session.getBaseBannerMaterial());

            int owned = countMaterial(player, dye);

            lore.add(
                    (owned >= 1 ? "§a" : "§c")
                            + "1x "
                            + pretty(dye.name())
                            + " §7(You have "
                            + owned
                            + ")"
            );
        }

        meta.setLore(lore);
        icon.setItemMeta(meta);

        return icon;
    }

    private ItemStack patternPreviewItem(
            BannerOption option
    ) {
        ItemStack banner =
                new ItemStack(Material.WHITE_BANNER);

        BannerMeta meta =
                (BannerMeta) banner.getItemMeta();

        meta.setDisplayName(
                "§f" + option.displayName()
        );

        meta.addPattern(
                new Pattern(
                        DyeColor.BLACK,
                        option.patternType()
                )
        );

        meta.setLore(List.of(
                "§7Preview shown in black.",
                "§7Applied using your selected color.",
                "",
                "§eClick to add this layer."
        ));

        banner.setItemMeta(meta);
        return banner;
    }

    private ItemStack specialPatternItem(
            BannerOption option
    ) {
        return item(
                option.patternItem(),
                "§d" + option.displayName(),
                List.of(
                        "§7Requires:",
                        "§f1x "
                                + pretty(
                                option.patternItem().name()
                        ),
                        "§f1x Selected Dye",
                        "",
                        "§eClick to add this layer."
                )
        );
    }

    public BannerOption getOptionForSlot(
            BannerSession session,
            int rawSlot
    ) {
        int[] patternSlots =
                getPatternSlotsForPage(session.getPage());

        int index = indexOf(patternSlots, rawSlot);

        if (index < 0) {
            return null;
        }

        List<BannerOption> page =
                BannerPatterns.getPage(session.getPage());

        if (index >= page.size()) {
            return null;
        }

        return page.get(index);
    }


    public DyeColor getColorForSlot(int rawSlot) {
        int index = indexOf(COLOR_SLOTS, rawSlot);

        if (index < 0 || index >= COLORS.length) {
            return null;
        }

        return COLORS[index];
    }

    public Map<Material, Integer> getMaterialBill(
            BannerSession session
    ) {
        Map<Material, Integer> needed =
                new LinkedHashMap<>();

        if (!plugin.areMaterialsEnabled()) {
            return needed;
        }

        List<BannerOption> allOptions =
                allPatternOptions();

        for (Pattern pattern
                : session.getPendingPatterns()) {

            addMaterial(
                    needed,
                    toDyeMaterial(pattern.getColor()),
                    1
            );

            BannerOption option =
                    findPatternOption(
                            allOptions,
                            pattern
                    );

            if (option != null
                    && option.requiresPatternItem()) {
                addMaterial(
                        needed,
                        option.patternItem(),
                        1
                );
            }
        }

        return needed;
    }

    public Map<Material, Integer> getConsumableMaterials(
            BannerSession session
    ) {
        Map<Material, Integer> consumable =
                new LinkedHashMap<>();

        if (!plugin.areMaterialsEnabled()) {
            return consumable;
        }

        for (Map.Entry<Material, Integer> entry
                : getMaterialBill(session).entrySet()) {

            Material material = entry.getKey();

            boolean isDye =
                    material.name().endsWith("_DYE");

            boolean isPatternItem =
                    material.name()
                            .endsWith("_BANNER_PATTERN");

            if (isDye && plugin.shouldConsumeDyes()) {
                consumable.put(
                        material,
                        entry.getValue()
                );
            }

            if (isPatternItem
                    && plugin.shouldConsumePatternItems()) {
                consumable.put(
                        material,
                        entry.getValue()
                );
            }
        }

        return consumable;
    }

    public MoneyQuote getMoneyQuote(
            Player player,
            BannerSession session
    ) {
        double originalPrice = 0.0;

        if (plugin.isMoneyEnabled()) {
            int paidLayers =
                    getNewPaidLayerCount(session);

            originalPrice =
                    paidLayers
                            * plugin.getCostPerPaidLayer();
        }

        double discountPercent =
                plugin.getDiscountPercent(player);

        double finalPrice =
                applyDiscount(
                        originalPrice,
                        discountPercent
                );

        return new MoneyQuote(
                originalPrice,
                finalPrice,
                discountPercent,
                plugin.getDiscountName(player)
        );
    }

    private int getNewPaidLayerCount(
            BannerSession session
    ) {
        int freeLimit;

        if (plugin.getPricingMode()
                .equals("FREE_UNTIL_LIMIT")) {
            freeLimit = plugin.getFreeLayers();
        } else {
            freeLimit = 6;
        }

        int originalPaid =
                Math.max(
                        0,
                        session.originalLayerCount()
                                - freeLimit
                );

        int finalPaid =
                Math.max(
                        0,
                        session.totalLayerCount()
                                - freeLimit
                );

        return Math.max(
                0,
                finalPaid - originalPaid
        );
    }

    private BannerOption findPatternOption(
            List<BannerOption> options,
            Pattern pattern
    ) {
        for (BannerOption option : options) {
            if (option.patternType()
                    == pattern.getPattern()) {
                return option;
            }
        }

        return null;
    }

    private List<BannerOption> allPatternOptions() {
        List<BannerOption> result =
                new ArrayList<>();

        result.addAll(BannerPatterns.PAGE_ONE);
        result.addAll(BannerPatterns.PAGE_TWO);
        result.addAll(BannerPatterns.PAGE_THREE);

        return result;
    }

    public boolean shouldShowReset() {
        return plugin.isResetEnabled()
                && (
                plugin.getResetMoneyCost() > 0
                        || plugin.resetRequiresBaseColorDye()
        );
    }

    public static Material toDyeMaterial(
            DyeColor color
    ) {
        return Material.valueOf(
                color.name() + "_DYE"
        );
    }

    public static Material getBaseDye(
            Material bannerMaterial
    ) {
        String dyeName =
                bannerMaterial
                        .name()
                        .replace(
                                "_WALL_BANNER",
                                "_DYE"
                        )
                        .replace(
                                "_BANNER",
                                "_DYE"
                        );

        return Material.valueOf(dyeName);
    }

    public static boolean isBanner(ItemStack item) {
        if (item == null) {
            return false;
        }

        String name = item.getType().name();

        return name.endsWith("_BANNER")
                && !name.endsWith("_WALL_BANNER");
    }

    private int countMaterial(
            Player player,
            Material material
    ) {
        int total = 0;

        for (ItemStack item
                : player.getInventory().getContents()) {

            if (item != null
                    && item.getType() == material) {
                total += item.getAmount();
            }
        }

        return total;
    }

    private void addMaterial(
            Map<Material, Integer> map,
            Material material,
            int amount
    ) {
        map.merge(material, amount, Integer::sum);
    }

    private int indexOf(
            int[] values,
            int target
    ) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == target) {
                return i;
            }
        }

        return -1;
    }

    private double applyDiscount(
            double original,
            double percent
    ) {
        return Math.max(
                0.0,
                original * (1.0 - percent / 100.0)
        );
    }

    private String pageDescription(int page) {
        return switch (page) {
            case 2 -> "§7Shapes, halves and gradients";
            case 3 -> "§7Special banner-pattern items";
            default -> "§7Corners, stripes and crosses";
        };
    }

    private static ItemStack item(
            Material material,
            String name
    ) {
        return item(material, name, List.of());
    }

    private static ItemStack item(
            Material material,
            String name,
            List<String> lore
    ) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        item.setItemMeta(meta);
        return item;
    }

    public static String pretty(String raw) {
        String[] words =
                raw.toLowerCase()
                        .replace('_', ' ')
                        .split(" ");

        StringBuilder result =
                new StringBuilder();

        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }

            if (!result.isEmpty()) {
                result.append(' ');
            }

            result.append(
                    Character.toUpperCase(word.charAt(0))
            );

            result.append(word.substring(1));
        }

        return result.toString();
    }

    public static String formatMoney(double value) {
        return String.format("%.2f", value);
    }

    public record MoneyQuote(
            double originalPrice,
            double finalPrice,
            double discountPercent,
            String discountName
    ) {
    }
}