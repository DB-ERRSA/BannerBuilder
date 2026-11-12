package com.errsa.bannerbuilder;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class LoomLinkListener implements Listener {

    private final BannerBuilderPlugin plugin;
    private final BannerCommand command;

    public LoomLinkListener(
            BannerBuilderPlugin plugin,
            BannerCommand command
    ) {
        this.plugin = plugin;
        this.command = command;
    }

    @EventHandler
    public void onLoomPlace(BlockPlaceEvent event) {
        if (!plugin.notifyOnLoomPlace()) {
            return;
        }

        if (event.getBlockPlaced().getType()
                != Material.LOOM) {
            return;
        }

        event.getPlayer().sendMessage(
                "§9§lLoom+ §7is available on this loom."
        );

        event.getPlayer().sendMessage(
                "§fSneak + right-click §7the loom to open the enhanced banner builder."
        );
    }

    @EventHandler
    public void onLoomInteract(
            PlayerInteractEvent event
    ) {
        if (!plugin.isLoomLinkEnabled()) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (event.getAction()
                != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getClickedBlock() == null
                || event.getClickedBlock().getType()
                != Material.LOOM) {
            return;
        }

        if (plugin.loomRequiresSneak()
                && !event.getPlayer().isSneaking()) {
            return;
        }

        event.setCancelled(true);
        command.openGui(event.getPlayer());
    }
}