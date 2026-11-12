package com.errsa.bannerbuilder;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BannerGuiListener implements Listener {

    private final BannerBuilderPlugin plugin;
    private final BannerCommand command;

    public BannerGuiListener(
            BannerBuilderPlugin plugin,
            BannerCommand command
    ) {
        this.plugin = plugin;
        this.command = command;
    }

    @EventHandler
    public void onInventoryClick(
            InventoryClickEvent event
    ) {
        if (!BannerGuiHolder.isEditor(event.getWhoClicked().getUniqueId(), event.getView().getTopInventory())) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked()
                instanceof Player player)) {
            return;
        }

        int rawSlot = event.getRawSlot();

        if (rawSlot < 0 || rawSlot >= 54) {
            return;
        }

        BannerSession session =
                plugin.getSession(player.getUniqueId());

        DyeColor color =
                command.getColorForSlot(rawSlot);

        if (color != null) {
            session.setSelectedColor(color);

            player.sendMessage(
                    "§bSelected color: §f"
                            + BannerCommand.pretty(
                            color.name()
                    )
            );

            command.refreshGui(player);
            return;
        }

        BannerOption option =
                command.getOptionForSlot(
                        session,
                        rawSlot
                );

        if (option != null) {
            addPattern(player, session, option);
            return;
        }

        switch (rawSlot) {
            case BannerCommand.UNDO_SLOT -> {
                if (session.pendingLayerCount() <= 0) {
                    player.sendMessage(
                            "§cThere are no new layers to undo."
                    );
                    return;
                }

                session.undoPending();

                player.sendMessage(
                        "§eRemoved the most recent new layer."
                );

                command.refreshGui(player);
            }

            case BannerCommand.RESET_SLOT ->
                    resetHeldBanner(player, session);

            case BannerCommand.PREVIOUS_SLOT -> {
                session.previousPage();
                command.refreshGui(player);
            }

            case BannerCommand.NEXT_SLOT -> {
                session.nextPage();
                command.refreshGui(player);
            }

            case BannerCommand.DUPLICATE_SLOT ->
                    duplicateBanner(player, session);

            case BannerCommand.APPLY_SLOT ->
                    applyBanner(player, session);

            default -> {
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(
            InventoryDragEvent event
    ) {
        if (BannerGuiHolder.isEditor(event.getWhoClicked().getUniqueId(), event.getView().getTopInventory())) {
            event.setCancelled(true);
        }
    }

    private void addPattern(
            Player player,
            BannerSession session,
            BannerOption option
    ) {
        if (plugin.isBedrockPlayer(player)
                && session.totalLayerCount() >= plugin.getBedrockVisibleLayerLimit()) {
            player.sendMessage(
                    plugin.message("§cBedrock can only display the first §f"
                            + plugin.getBedrockVisibleLayerLimit()
                            + "§c banner layers. No additional layer was added.")
            );
            return;
        }

        if (!plugin.isInfinitePatterns()
                && session.totalLayerCount()
                >= plugin.getMaxPatterns()) {

            player.sendMessage(
                    "§cThis banner has reached the maximum of "
                            + plugin.getMaxPatterns()
                            + " layers."
            );

            return;
        }

        session.addPattern(option.patternType());

        player.sendMessage(
                "§aAdded "
                        + option.displayName()
                        + " as layer §f"
                        + session.totalLayerCount()
                        + "§a."
        );

        command.refreshGui(player);
    }

    private void applyBanner(
            Player player,
            BannerSession session
    ) {
        ItemStack heldItem =
                player.getInventory().getItemInMainHand();

        if (!BannerCommand.isBanner(heldItem)) {
            player.sendMessage(
                    "§cHold the banner being edited in your main hand."
            );
            return;
        }

        if (heldItem.getType()
                != session.getBaseBannerMaterial()) {
            player.sendMessage(
                    "§cThe banner in your hand changed while Loom+ was open."
            );
            return;
        }

        if (session.pendingLayerCount() <= 0) {
            player.sendMessage(
                    "§cThere are no new layers to apply."
            );
            return;
        }

        int extraBannerCount = heldItem.getAmount() - 1;
        if (extraBannerCount > 0
                && !canFitRemainingStack(player, heldItem, extraBannerCount)) {
            player.sendMessage(
                    "§cLoom+ edits one banner at a time. Make one inventory slot "
                            + "available or separate a single banner from the stack."
            );
            return;
        }

        BannerCommand.MoneyQuote moneyQuote =
                command.getMoneyQuote(player, session);

        Map<Material, Integer> requiredMaterials =
                command.getMaterialBill(session);

        List<String> missingMaterials =
                findMissingMaterials(
                        player,
                        requiredMaterials
                );

        boolean insufficientMoney =
                !hasEnoughMoney(
                        player,
                        moneyQuote.finalPrice()
                );

        if (insufficientMoney) {
            double balance =
                    plugin.getEconomy() == null
                            ? 0.0
                            : plugin.getEconomy()
                            .getBalance(player);

            player.sendMessage(
                    "§cInsufficient funds: §f$"
                            + BannerCommand.formatMoney(
                            moneyQuote.finalPrice()
                    )
                            + " required, but you have §f$"
                            + BannerCommand.formatMoney(balance)
                            + "§c."
            );
        }

        for (String missing : missingMaterials) {
            player.sendMessage(
                    "§cMissing material: §f" + missing
            );
        }

        if (insufficientMoney
                || !missingMaterials.isEmpty()) {
            command.refreshGui(player);
            return;
        }

        if (!withdrawMoney(
                player,
                moneyQuote.finalPrice(),
                "Banner purchase"
        )) {
            return;
        }

        consumeMaterials(
                player,
                command.getConsumableMaterials(session)
        );

        ItemStack updatedBanner = heldItem.clone();
        updatedBanner.setAmount(1);

        BannerMeta meta =
                (BannerMeta) updatedBanner.getItemMeta();

        meta.setPatterns(session.getPreviewPatterns());
        updatedBanner.setItemMeta(meta);

        ItemStack remainder = heldItem.clone();
        remainder.setAmount(extraBannerCount);

        player.getInventory().setItemInMainHand(updatedBanner);
        if (extraBannerCount > 0) {
            player.getInventory().addItem(remainder);
        }

        session.commit();

        player.sendMessage(
                "§aBanner successfully updated with §f"
                        + session.totalLayerCount()
                        + "§a layers."
        );

        player.closeInventory();
    }


    private void duplicateBanner(
            Player player,
            BannerSession session
    ) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (!BannerCommand.isBanner(heldItem)
                || heldItem.getType() != session.getBaseBannerMaterial()) {
            player.sendMessage("§cHold the banner being copied in your main hand.");
            return;
        }

        if (!(heldItem.getItemMeta() instanceof BannerMeta sourceMeta)
                || sourceMeta.getPatterns().isEmpty()) {
            player.sendMessage("§cAdd at least one pattern before duplicating this banner.");
            return;
        }

        int blankSlot = findBlankBannerSlot(player, heldItem.getType());
        if (blankSlot < 0) {
            player.sendMessage(
                    "§cYou need one blank §f"
                            + BannerCommand.pretty(heldItem.getType().name())
                            + "§c in your inventory."
            );
            command.refreshGui(player);
            return;
        }

        ItemStack blankStack = player.getInventory().getItem(blankSlot);
        if (blankStack == null) {
            return;
        }

        ItemStack copy = heldItem.clone();
        copy.setAmount(1);

        if (blankStack.getAmount() == 1) {
            player.getInventory().setItem(blankSlot, copy);
        } else {
            blankStack.setAmount(blankStack.getAmount() - 1);
            player.getInventory().setItem(blankSlot, blankStack);

            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(copy);
            if (!leftovers.isEmpty()) {
                blankStack.setAmount(blankStack.getAmount() + 1);
                player.getInventory().setItem(blankSlot, blankStack);
                player.sendMessage("§cMake one inventory slot available to duplicate the banner.");
                return;
            }
        }

        player.sendMessage("§aDuplicated the banner using §f1 blank banner§a.");
        command.refreshGui(player);
    }

    private int findBlankBannerSlot(
            Player player,
            Material bannerMaterial
    ) {
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

            if (item.getItemMeta() instanceof BannerMeta meta
                    && meta.getPatterns().isEmpty()) {
                return slot;
            }
        }

        return -1;
    }

    private boolean canFitRemainingStack(
            Player player,
            ItemStack original,
            int amount
    ) {
        if (amount <= 0) {
            return true;
        }

        int heldSlot = player.getInventory().getHeldItemSlot();
        int room = 0;
        ItemStack[] contents = player.getInventory().getStorageContents();

        ItemStack one = original.clone();
        one.setAmount(1);

        for (int slot = 0; slot < contents.length; slot++) {
            if (slot == heldSlot) {
                continue;
            }

            ItemStack item = contents[slot];
            if (item == null || item.getType().isAir()) {
                room += one.getMaxStackSize();
            } else if (item.isSimilar(one)) {
                room += Math.max(0, item.getMaxStackSize() - item.getAmount());
            }

            if (room >= amount) {
                return true;
            }
        }

        return false;
    }

    private void resetHeldBanner(
            Player player,
            BannerSession session
    ) {
        if (!command.shouldShowReset()) {
            return;
        }

        ItemStack heldItem =
                player.getInventory().getItemInMainHand();

        if (!BannerCommand.isBanner(heldItem)) {
            player.sendMessage(
                    "§cHold a banner in your main hand first."
            );
            return;
        }

        double originalMoney =
                plugin.getResetMoneyCost();

        double discount =
                plugin.getDiscountPercent(player);

        double finalMoney =
                originalMoney
                        * (1.0 - discount / 100.0);

        Map<Material, Integer> requiredMaterials =
                new LinkedHashMap<>();

        if (plugin.resetRequiresBaseColorDye()) {
            requiredMaterials.put(
                    BannerCommand.getBaseDye(
                            heldItem.getType()
                    ),
                    1
            );
        }

        List<String> missingMaterials =
                findMissingMaterials(
                        player,
                        requiredMaterials
                );

        boolean insufficientMoney =
                !hasEnoughMoney(player, finalMoney);

        if (insufficientMoney) {
            double balance =
                    plugin.getEconomy() == null
                            ? 0.0
                            : plugin.getEconomy()
                            .getBalance(player);

            player.sendMessage(
                    "§cReset requires §f$"
                            + BannerCommand.formatMoney(finalMoney)
                            + "§c, but you have §f$"
                            + BannerCommand.formatMoney(balance)
                            + "§c."
            );
        }

        for (String missing : missingMaterials) {
            player.sendMessage(
                    "§cMissing reset material: §f"
                            + missing
            );
        }

        if (insufficientMoney
                || !missingMaterials.isEmpty()) {
            command.refreshGui(player);
            return;
        }

        if (!withdrawMoney(player, finalMoney, "Banner reset")) {
            return;
        }

        if (plugin.consumeResetDye()) {
            consumeMaterials(
                    player,
                    requiredMaterials
            );
        }

        BannerMeta meta =
                (BannerMeta) heldItem.getItemMeta();

        meta.setPatterns(List.of());
        heldItem.setItemMeta(meta);

        session.resetAll();

        player.sendMessage(
                "§aThe held banner was reset."
        );

        command.refreshGui(player);
    }

    private boolean hasEnoughMoney(
            Player player,
            double amount
    ) {
        if (amount <= 0) {
            return true;
        }

        Economy economy = plugin.getEconomy();

        return economy != null
                && economy.has(player, amount);
    }

    private boolean withdrawMoney(
            Player player,
            double amount,
            String label
    ) {
        if (amount <= 0) {
            return true;
        }

        Economy economy = plugin.getEconomy();

        if (economy == null) {
            player.sendMessage(
                    "§cVault or the server economy provider is unavailable."
            );
            return false;
        }

        var response =
                economy.withdrawPlayer(player, amount);

        if (!response.transactionSuccess()) {
            player.sendMessage(
                    "§cThe payment failed: §f"
                            + response.errorMessage
            );
            return false;
        }

        double remainingBalance =
                economy.getBalance(player);
        double beforeBalance = remainingBalance + amount;

        plugin.sendTransaction(
                player,
                label,
                -amount,
                beforeBalance,
                remainingBalance
        );

        return true;
    }

    private List<String> findMissingMaterials(
            Player player,
            Map<Material, Integer> required
    ) {
        List<String> missing = new ArrayList<>();

        for (Map.Entry<Material, Integer> entry
                : required.entrySet()) {

            int owned =
                    countMaterial(
                            player,
                            entry.getKey()
                    );

            int needed =
                    entry.getValue();

            if (owned < needed) {
                missing.add(
                        needed
                                + "x "
                                + BannerCommand.pretty(
                                entry.getKey().name()
                        )
                                + " §7(you have "
                                + owned
                                + ")"
                );
            }
        }

        return missing;
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

    private void consumeMaterials(
            Player player,
            Map<Material, Integer> materials
    ) {
        for (Map.Entry<Material, Integer> entry
                : materials.entrySet()) {

            removeMaterial(
                    player,
                    entry.getKey(),
                    entry.getValue()
            );
        }
    }

    private void removeMaterial(
            Player player,
            Material material,
            int amount
    ) {
        int remaining = amount;

        ItemStack[] contents =
                player.getInventory().getContents();

        for (int slot = 0;
             slot < contents.length && remaining > 0;
             slot++) {

            ItemStack item = contents[slot];

            if (item == null
                    || item.getType() != material) {
                continue;
            }

            int removed =
                    Math.min(
                            remaining,
                            item.getAmount()
                    );

            int newAmount =
                    item.getAmount() - removed;

            remaining -= removed;

            if (newAmount <= 0) {
                player.getInventory()
                        .setItem(slot, null);
            } else {
                item.setAmount(newAmount);

                player.getInventory()
                        .setItem(slot, item);
            }
        }
    }
}