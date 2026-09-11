package net.firepixel.fun.listener;

import net.firepixel.fun.Firepixel;
import net.firepixel.fun.report.ReportMenuHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public class ReportMenuListener implements Listener {

    private static final int INFO_SLOT = 48;
    private static final int CLOSE_SLOT = 49;

    private final Firepixel plugin;

    public ReportMenuListener(Firepixel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (!(holder instanceof ReportMenuHolder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ReportMenuHolder report = (ReportMenuHolder) holder;
        int slot = event.getRawSlot();

        if (slot < 0 || slot >= event.getView().getTopInventory().getSize()) {
            return;
        }

        if (report.getType() == ReportMenuHolder.Type.REASON) {
            if (slot == CLOSE_SLOT || slot == INFO_SLOT) {
                if (slot == CLOSE_SLOT) {
                    player.closeInventory();
                }
                return;
            }

            String reasonKey = report.getSlot(slot);

            if (reasonKey == null) {
                return;
            }

            plugin.getReportManager().openPlayerMenu(player, reasonKey);
            return;
        }

        if (report.getType() == ReportMenuHolder.Type.PLAYER) {
            String target = report.getSlot(slot);

            if (target == null) {
                return;
            }

            plugin.getReportManager().openConfirmMenu(player, target, report.getReason());
            return;
        }

        if (report.getType() == ReportMenuHolder.Type.CONFIRM) {
            if (slot == 11) {
                plugin.getReportManager().submit(player, report.getTarget(), report.getReason());
                player.closeInventory();
            } else if (slot == 15) {
                player.closeInventory();
                String language = plugin.getPlayerDataManager().getLanguage(player.getUniqueId());
                player.sendMessage(plugin.getLanguageManager().getMessage(language, "report.cancelled"));
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView() != null && event.getView().getTopInventory() != null
                && event.getView().getTopInventory().getHolder() instanceof ReportMenuHolder) {
            event.setCancelled(true);
        }
    }
}