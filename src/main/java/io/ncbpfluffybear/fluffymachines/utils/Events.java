package io.ncbpfluffybear.fluffymachines.utils;

import io.ncbpfluffybear.fluffymachines.items.Barrel;
import io.ncbpfluffybear.fluffymachines.items.FireproofRune;
import io.ncbpfluffybear.fluffymachines.items.HelicopterHat;
import io.ncbpfluffybear.fluffymachines.items.tools.WateringCan;
import io.ncbpfluffybear.fluffymachines.machines.AlternateElevatorPlate;
import javax.annotation.Nonnull;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Events implements Listener {

    final HelicopterHat helicopterHat = (HelicopterHat) FluffyItems.HELICOPTER_HAT.getItem();
    final WateringCan wateringCan = (WateringCan) FluffyItems.WATERING_CAN.getItem();

    @EventHandler
    public void onAdminJoin(PlayerJoinEvent e) {
        if (e.getPlayer().isOp() && !Constants.SLIMEFUN_UPDATED) {
            Utils.send(e.getPlayer(), "&c您运行的是DEV 844之前的Slimefun版本/RC版本的Slimefun." +
                "FluffyMachines要求您更新Slimefun版本，" +
                "以便让储存箱子可以正常工作，请更新到210415后续版本，否则玩家可能会遇到问题." +
                "如果您未更改，作者将不负责蓬松机器，您也可以忽略这条报错:" +
                "FluffyMachines文件夹内的config.yml里面改成ignore-outdated-warning: true" +
                "如果您使用的是汉化版,请忽略这条报错(但请更新到最新).");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHelicopterHatUse(PlayerToggleSneakEvent e) {
        Player p = e.getPlayer();
        if (helicopterHat.isItem(p.getEquipment().getHelmet())) {
            if (e.isSneaking()) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 1000000, 4));
            } else {
                p.removePotionEffect(PotionEffectType.LEVITATION);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWateringCanSplash(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();

        // For some reason player interact events trigger twice, probably after a method returns false
        if (wateringCan.isItem(item)) {
            e.setCancelled(true);
            Entity target = e.getRightClicked();
            if (target instanceof Player && WateringCan.updateUses(wateringCan, p, item, 3)) {
                Utils.send(p, "&b喷水!");
                Utils.send((Player) target, "&b你被水溅到了" + p.getDisplayName() + "!");
                ((Player) target).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && ((Player) e.getEntity()).getEquipment() != null
            && e.getCause() == EntityDamageEvent.DamageCause.FALL
        ) {
            Player p = (Player) e.getEntity();
            ItemStack helmet = p.getEquipment().getHelmet();
            if (helmet != null && helicopterHat.isItem(helmet)
            ) {
                e.setCancelled(true);
            }
        }
    }

    // This is used to make the non clickable GUI items non clickable
    @EventHandler(ignoreCancelled = true)
    public void onNonClickableClick(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        if (item != null && item.getType() != Material.AIR && item.getItemMeta().hasCustomModelData()
            && item.getItemMeta().getCustomModelData() == 6969) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHeadRemove(PlayerArmorStandManipulateEvent e) {
        if (e.getRightClicked().getCustomName() != null
            && e.getRightClicked().getCustomName().equals("hehexdfluff"))
            e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDamage(EntityDamageEvent e) {
        Entity en = e.getEntity();
        if (en instanceof Item) {
            ItemStack item = ((Item) en).getItemStack();
            if (FireproofRune.isFireproof(item)
                && (e.getCause() == EntityDamageEvent.DamageCause.FIRE
                || e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK
                || e.getCause() == EntityDamageEvent.DamageCause.LAVA
                || e.getCause() == EntityDamageEvent.DamageCause.LIGHTNING)
                && !en.isDead()
            ) {
                en.remove();
                en.getLocation().getWorld().dropItem(en.getLocation(), item);

            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerWarp(PlayerToggleSneakEvent e) {
        if (e.isSneaking()) {
            Player p = e.getPlayer();
            Block b = p.getLocation().subtract(0, 1, 0).getBlock();

            if (BlockStorage.hasBlockInfo(b) && BlockStorage.check(b) == FluffyItems.WARP_PAD.getItem()
                && BlockStorage.getLocationInfo(b.getLocation(), "type").equals("origin")) {

                Location l = b.getLocation();
                Location destination = new Location(b.getWorld(),
                    Integer.parseInt(BlockStorage.getLocationInfo(l, "x")),
                    Integer.parseInt(BlockStorage.getLocationInfo(l, "y")),
                    Integer.parseInt(BlockStorage.getLocationInfo(l, "z")));

                float yaw = p.getLocation().getYaw();
                float pitch = p.getLocation().getPitch();

                if (BlockStorage.hasBlockInfo(destination) && BlockStorage.getLocationInfo(destination, "type") != null
                    && BlockStorage.getLocationInfo(destination, "type").equals("destination")
                    && destination.getBlock().getRelative(BlockFace.UP).getType() == Material.AIR
                    && destination.getBlock().getRelative(BlockFace.UP, 2).getType() == Material.AIR) {

                    destination.setPitch(pitch);
                    destination.setYaw(yaw);
                    p.teleport(destination.add(0.5, 1, 0.5));

                    p.playSound(p.getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 0.5F, 0.5F);
                    p.spawnParticle(Particle.DRAGON_BREATH, p.getLocation(), 10);

                } else {
                    Utils.send(p, "&c缺少传送装置!");

                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPressurePlateEnter(PlayerInteractEvent e) {
        if (e.getAction() != Action.PHYSICAL || e.getClickedBlock() == null) {
            return;
        }

        String id = BlockStorage.checkID(e.getClickedBlock());
        if (id != null && id.equals(FluffyItems.ALTERNATE_ELEVATOR_PLATE.getItemId())) {
            AlternateElevatorPlate elevator = ((AlternateElevatorPlate) FluffyItems.ALTERNATE_ELEVATOR_PLATE.getItem());
            elevator.openInterface(e.getPlayer(), e.getClickedBlock());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFMInfoClick(InventoryClickEvent e) {
        if (SlimefunItem.getByItem(e.getCurrentItem()) != null
            && SlimefunItem.getByItem(e.getCurrentItem()) == FluffyItems.FLUFFYMACHINES_INFO.getItem()) {

            Player p = (Player) e.getWhoClicked();
            p.closeInventory();

            if (e.getClick() == ClickType.LEFT) {
                Utils.send(p, "&7Wiki地址: https://github.com/NCBPFluffyBear/FluffyMachines/blob/master/README.md");
            } else if (e.getClick() == ClickType.RIGHT) {
                Utils.send(p, "&7反馈Bugs地址: https://github.com/NCBPFluffyBear/FluffyMachines/issues");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onExtractionNodePlace(BlockPlaceEvent e) {
        if ((e.getBlock().getY() != e.getBlockAgainst().getY() || e.getBlockAgainst().getType() != Material.ENDER_CHEST)
            && isExtractionNode(e.getItemInHand())) {
            Utils.send(e.getPlayer(), "&c你只能把这个放在末影箱旁边!");
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDollyDispense(BlockDispenseEvent e) {
        SlimefunItem sfItem = SlimefunItem.getByItem(e.getItem());
        if (sfItem != null && sfItem.getId().equals(FluffyItems.DOLLY.getItemId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBarrelBurn(BlockBurnEvent e) {
        if (BlockStorage.check(e.getBlock()) instanceof Barrel) {
            e.setCancelled(true);
        }
    }

    private boolean isExtractionNode(@Nonnull ItemStack item) {
        SlimefunItem sfItem = SlimefunItem.getByItem(item);

        if (sfItem == null) {
            return false;
        }
        return sfItem.getId().equals(FluffyItems.ENDER_CHEST_EXTRACTION_NODE.getItemId()) || sfItem.getId().equals(FluffyItems.ENDER_CHEST_INSERTION_NODE.getItemId());
    }

    @EventHandler(ignoreCancelled = true)
    private void onCancelPlace(BlockPlaceEvent e) {
        ItemStack item = e.getItemInHand();
        SlimefunItem sfItem = SlimefunItem.getByItem(item);
        if (sfItem instanceof CancelPlace) {
            e.setCancelled(true);
        }
    }
}
