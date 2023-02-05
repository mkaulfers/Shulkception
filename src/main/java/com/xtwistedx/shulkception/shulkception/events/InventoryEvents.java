package com.xtwistedx.shulkception.shulkception.events;

import com.xtwistedx.shulkception.shulkception.ShulkceptionPlugin;
import com.xtwistedx.shulkception.shulkception.models.PlayerInteractionManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class InventoryEvents implements Listener {
    /**
     * The key used to store the UUID of the shulker box in the item's persistent data container.
     */
    public static final String SHULKER_BOX_KEY = "shulkception_shulker_box_uuid_key";

    /**
     * The HashMap that is used to track the player's interactions with shulker boxes.
     * It holds the data for the player's current open shulker box.
     */
    HashMap<UUID, PlayerInteractionManager> playerInteractions = new HashMap<>();

    //MARK: - Events
    /**
     * Handles the event when a player opens a shulker box.
     * @param e - The inventory open event.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ClickType clickType = e.getClick();
        ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null) {
            return;
        }

        ItemClickType itemClickType = getShulkerClickType(e);

        switch (itemClickType) {
            case OTHER:
                handleOtherItemClick(player);
                break;
            case DROP:
                handleDropItemClick(e, player, clickedItem);
                break;
            case SHULKER_CLICK_TOP:
                e.setCancelled(true);
                break;
            case SHULKER_CLICK_BOTTOM:
                handleShulkerClickBottom(e, player, clickType, clickedItem);
                break;
        }
    }

    /**
     * Handles the event when a player closes a shulker box.
     * @param e - The inventory close event.
     */
    @EventHandler
    public void onShulkerClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        PlayerInteractionManager playerInteraction = playerInteractions.get(player.getUniqueId());
        if (playerInteraction == null) return;

        Inventory inventory = e.getInventory();
        if (!inventory.getType().equals(InventoryType.SHULKER_BOX)) return;

        playerInteraction.performCloseTask();
        playerInteractions.remove(player.getUniqueId());
    }

    /**
     * Handles the event when a player drags an item into a shulker box.
     * @param e - The inventory drag event.
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        Player player = (Player) e.getWhoClicked();
        PlayerInteractionManager playerInteraction = playerInteractions.get(player.getUniqueId());
        if (playerInteraction == null) return;
        playerInteraction.performUpdateTask();
    }

    //MARK: - Private Event Handlers

    //TODO: - Add support for opening shulkers from the top inventory in containers, such as chests, hoppers, barrels, etc..
//    private void handleShulkerClickTop(InventoryClickEvent e, Player player, ClickType clickType, ItemStack clickedItem) {
//     if (clickType == ClickType.SHIFT_RIGHT) {
//         if (playerInteractions.containsKey(player.getUniqueId())) {
//             e.setCancelled(true);
//             return;
//         }
//
//         ShulkerBox shulkerBox = (ShulkerBox) ((BlockStateMeta) Objects.requireNonNull(clickedItem.getItemMeta())).getBlockState();
//
//         String uuid = UUID.randomUUID().toString();
//         PersistentDataContainer data = shulkerBox.getPersistentDataContainer();
//         data.set(getNameSpacedKey(), PersistentDataType.STRING, uuid);
//
//         ItemMeta shulkerMeta = clickedItem.getItemMeta();
//         PersistentDataContainer itemData = shulkerMeta.getPersistentDataContainer();
//         itemData.set(getNameSpacedKey(), PersistentDataType.STRING, uuid);
//         clickedItem.setItemMeta(shulkerMeta);
//
//         PlayerInteractionManager playerInteraction = new PlayerInteractionManager(player, shulkerBox, e.getSlot());
//     }
// }

    /**
     * Handles allowing the opening of a shulker box from the lower inventory. This is done by creating a new shulker box.
     * @param e - The inventory click event.
     * @param player - The player who clicked the shulker box.
     * @param clickType - The type of click. (EX: SHIFT_RIGHT)
     * @param clickedItem - The item that was clicked.
     */
    private void handleShulkerClickBottom(InventoryClickEvent e, Player player, ClickType clickType, ItemStack clickedItem) {
        if (clickType == ClickType.SHIFT_RIGHT) {
            if (playerInteractions.containsKey(player.getUniqueId())) {
                e.setCancelled(true);
                return;
            }

            ShulkerBox shulkerBox = (ShulkerBox) ((BlockStateMeta) Objects.requireNonNull(clickedItem.getItemMeta())).getBlockState();

            String uuid = UUID.randomUUID().toString();
            PersistentDataContainer data = shulkerBox.getPersistentDataContainer();
            data.set(getNameSpacedKey(), PersistentDataType.STRING, uuid);

            ItemMeta shulkerMeta = clickedItem.getItemMeta();
            PersistentDataContainer itemData = shulkerMeta.getPersistentDataContainer();
            itemData.set(getNameSpacedKey(), PersistentDataType.STRING, uuid);
            clickedItem.setItemMeta(shulkerMeta);

            PlayerInteractionManager playerInteraction = new PlayerInteractionManager(player, shulkerBox, e.getSlot());

            playerInteraction.performOpenTask();
            e.setCancelled(true);

            playerInteractions.put(player.getUniqueId(), playerInteraction);
        } else if (playerInteractions.containsKey(player.getUniqueId())) {
            e.setCancelled(true);
        }
    }

    /**
     * Allows the user to interact with other items as normal, however updates the tracked player interaction event.
     * @param player - The player who clicked the shulker box.
     */
    private void handleOtherItemClick(Player player) {
        PlayerInteractionManager playerInteraction = playerInteractions.get(player.getUniqueId());
        if (playerInteraction == null) return;
        playerInteraction.performUpdateTask();
    }

    /**
     * Handles the event when a player drops an item from their inventory. If the item is a shulker box, it will be cancelled, while they have a shulker box open.
     * @param e - The inventory click event.
     * @param player - The player who clicked the shulker box.
     * @param clickedItem - The item that was clicked.
     */
    private void handleDropItemClick(InventoryClickEvent e, Player player, ItemStack clickedItem) {
        if (playerInteractions.containsKey(player.getUniqueId()) && isShulkerBox(clickedItem.getType())) {
            e.setCancelled(true);
        }
    }

    //MARK: - Utility Methods

    /**
     * Gets a NamespacedKey for a persistent container related to this plugin.
     * @return - The NamespacedKey for this plugin.
     */
    public static NamespacedKey getNameSpacedKey() {
        return new NamespacedKey(ShulkceptionPlugin.getPlugin(), SHULKER_BOX_KEY);
    }
    private boolean isShulkerBox(Material m) {
        switch (m) {
            case SHULKER_BOX:
            case LIGHT_GRAY_SHULKER_BOX:
            case BLACK_SHULKER_BOX:
            case BLUE_SHULKER_BOX:
            case BROWN_SHULKER_BOX:
            case CYAN_SHULKER_BOX:
            case GRAY_SHULKER_BOX:
            case GREEN_SHULKER_BOX:
            case LIGHT_BLUE_SHULKER_BOX:
            case LIME_SHULKER_BOX:
            case MAGENTA_SHULKER_BOX:
            case ORANGE_SHULKER_BOX:
            case PINK_SHULKER_BOX:
            case PURPLE_SHULKER_BOX:
            case RED_SHULKER_BOX:
            case WHITE_SHULKER_BOX:
            case YELLOW_SHULKER_BOX:
                return true;
            default:
                return false;
        }
    }

    /**
     * @param e InventoryClickEvent
     * @return ItemClickType - Represents either a shulker box click top/bottom or other. If the item is not a shulker box, it will return OTHER.
     */
    private ItemClickType getShulkerClickType(InventoryClickEvent e) {
        if (e.getClick().equals(ClickType.DROP)) return ItemClickType.DROP;

        int topSize = e.getView().getTopInventory().getSize();
        int slot = e.getRawSlot();

        ItemStack item = e.getCurrentItem();
        if (item == null) return ItemClickType.OTHER;
        Material itemType = e.getCurrentItem().getType();

        if (!isShulkerBox(itemType) || e.getClick() != ClickType.SHIFT_RIGHT) return ItemClickType.OTHER;
        if (topSize <= 5 && isShulkerBox(itemType) || (slot >= topSize && isShulkerBox(itemType))) return ItemClickType.SHULKER_CLICK_BOTTOM;
        return ItemClickType.SHULKER_CLICK_TOP;
    }


    //MARK: - Private Enums
    private enum ItemClickType {
        SHULKER_CLICK_TOP,
        SHULKER_CLICK_BOTTOM,
        OTHER,
        DROP
    }

    //MARK: - Constructor
    public InventoryEvents(ShulkceptionPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}
