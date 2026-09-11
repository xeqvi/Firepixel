package net.firepixel.fun.listener;

import net.firepixel.fun.Firepixel;
import net.firepixel.fun.report.ReportMenuHolder;
import net.firepixel.fun.util.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public class ReportMenuListener implements Listener {

    private static final int INFO_SLOT = 48;
    private static final int CLOSE_SLOT = 49;
    private static final int BACK_SLOT = 49;
    private static final int CONFIRM_BACK_SLOT = 22;

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
            handleReason(player, report, slot);
            return;
        }

        if (report.getType() == ReportMenuHolder.Type.PLAYER) {
            handlePlayer(player, report, slot);
            return;
        }

        if (report.getType() == ReportMenuHolder.Type.CONFIRM) {
            handleConfirm(player, report, slot);
        }
    }

    private void handleReason(Player player, ReportMenuHolder report, int slot) {
        if (slot == CLOSE_SLOT) {
            player.closeInventory();
            return;
        }

        if (slot == INFO_SLOT) {
            return;
        }

        String reasonKey = report.getSlot(slot);

        if (reasonKey == null) {
            return;
        }

        if (report.isDirect()) {
            plugin.getReportManager().openConfirmMenu(player, report.getTarget(), reasonKey, true);
            return;
        }

        plugin.getReportManager().openPlayerMenu(player, reasonKey);
    }

    private void handlePlayer(Player player, ReportMenuHolder report, int slot) {
        if (slot == BACK_SLOT) {
            plugin.getReportManager().openReasonMenu(player);
            return;
        }

        String target = report.getSlot(slot);

        if (target == null) {
            return;
        }

        plugin.getReportManager().openConfirmMenu(player, target, report.getReason(), false);
    }

    private void handleConfirm(Player player, ReportMenuHolder report, int slot) {
        if (slot == 11) {
            plugin.getReportManager().submit(player, report.getTarget(), report.getReason());
            player.closeInventory();
            return;
        }

        if (slot == 15) {
            player.closeInventory();
            String language = plugin.getPlayerDataManager().getLanguage(player.getUniqueId());
            player.sendMessage(ColorUtil.color(plugin.getLanguageManager().getMessage(language, "report.cancelled")));
            return;
        }

        if (slot == CONFIRM_BACK_SLOT) {
            if (report.isDirect()) {
                plugin.getReportManager().openReasonMenu(player, report.getTarget());
            } else {
                plugin.getReportManager().openPlayerMenu(player, report.getReason());
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