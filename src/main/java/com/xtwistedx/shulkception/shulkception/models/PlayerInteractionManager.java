package com.xtwistedx.shulkception.shulkception.models;

import com.xtwistedx.shulkception.shulkception.ShulkceptionPlugin;
import com.xtwistedx.shulkception.shulkception.events.InventoryEvents;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerInteractionManager {
    Player player;
    public ShulkerBox trackedShulkerBox;
    public int currentSlot;

    public PlayerInteractionManager(Player player, ShulkerBox trackedShulkerBox, int currentSlot) {
        this.player = player;
        this.trackedShulkerBox = trackedShulkerBox;
        this.currentSlot = currentSlot;
    }

    public void performOpenTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.openInventory(trackedShulkerBox.getInventory());
            }
        }.runTask(ShulkceptionPlugin.getPlugin());
    }

    public void performUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                trackedShulkerBox.getInventory().setContents(player.getOpenInventory().getTopInventory().getContents());
                ItemStack shulkerItem = player.getInventory().getItem(currentSlot);
                if (shulkerItem == null) return;

                ItemMeta shulkerItemMeta = shulkerItem.getItemMeta();
                BlockStateMeta shulkerBlockStateMeta = (BlockStateMeta) shulkerItemMeta;
                if (shulkerBlockStateMeta == null) return;

                ShulkerBox shulkerBox = (ShulkerBox) shulkerBlockStateMeta.getBlockState();
                shulkerBox.getInventory().setContents(trackedShulkerBox.getInventory().getContents());

                shulkerBlockStateMeta.setBlockState(shulkerBox);
                shulkerItem.setItemMeta(shulkerBlockStateMeta);
            }
        }.runTask(ShulkceptionPlugin.getPlugin());
    }

    public void performCloseTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack[] playerInventoryContents = player.getInventory().getContents();
                ItemStack shulkerItem = null;

                for (ItemStack item : playerInventoryContents) {
                    if (item == null || item.getType() != Material.SHULKER_BOX) continue;

                    ItemMeta itemMeta = item.getItemMeta();
                    if (itemMeta == null) continue;

                    PersistentDataContainer container = itemMeta.getPersistentDataContainer();
                    String currentItemUUID = container.get(InventoryEvents.getNameSpacedKey(), PersistentDataType.STRING);

                    if (currentItemUUID == null) continue;

                    PersistentDataContainer trackedShulkerBoxPersistentDataContainer = trackedShulkerBox.getPersistentDataContainer();
                    String trackedShulkerUUID = trackedShulkerBoxPersistentDataContainer.get(InventoryEvents.getNameSpacedKey(), PersistentDataType.STRING);

                    if (currentItemUUID.equals(trackedShulkerUUID)) {
                        shulkerItem = item;
                        break;
                    }

                    if (item == playerInventoryContents[playerInventoryContents.length - 1]) {
                        trackedShulkerBoxPersistentDataContainer.remove(InventoryEvents.getNameSpacedKey());
                    }
                }

                if (shulkerItem == null) return;

                BlockStateMeta shulkerMeta = (BlockStateMeta) shulkerItem.getItemMeta();

                if (shulkerMeta == null) {
                    player.sendMessage(ChatColor.RED + "Something went wrong, please try again. (ERROR: shulkerMeta is null.)");
                    return;
                }

                ShulkerBox shulkerBox = (ShulkerBox) shulkerMeta.getBlockState();
                shulkerBox.getInventory().setContents(trackedShulkerBox.getInventory().getContents());

                PersistentDataContainer shulkerContainer = shulkerMeta.getPersistentDataContainer();
                shulkerContainer.remove(InventoryEvents.getNameSpacedKey());

                shulkerMeta.setBlockState(shulkerBox);
                shulkerItem.setItemMeta(shulkerMeta);
            }
        }.runTask(ShulkceptionPlugin.getPlugin());
    }
}
