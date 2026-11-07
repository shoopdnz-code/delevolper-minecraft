package br.com.elomid.essentialsleve;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class EssentialsLeve extends JavaPlugin implements Listener, CommandExecutor {
    private static final String PREFIX = "§8[§aEssentialsLeve§8] §7";
    private final Map<UUID, Request> requests = new HashMap<>();
    private final Map<UUID, Long> rtpCooldown = new HashMap<>();
    private final Random random = new Random();

    @Override public void onEnable() {
        saveDefaultConfig();
        for (String name : Arrays.asList("rtpgui", "tpa", "tpahere", "tpaccept", "home", "sethome", "delhome")) {
            if (getCommand(name) != null) getCommand(name).setExecutor(this);
        }
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("EssentialsLeve ativado!");
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage("Apenas jogadores podem usar este comando."); return true; }
        Player player = (Player) sender;
        switch (command.getName().toLowerCase()) {
            case "rtpgui":
                if (!player.hasPermission("essentialsleve.rtp")) { deny(player); return true; }
                showRtpGui(player); return true;
            case "tpa": return requestTp(player, args, false);
            case "tpahere": return requestTp(player, args, true);
            case "tpaccept": return acceptTp(player);
            case "sethome":
                if (!player.hasPermission("essentialsleve.home")) { deny(player); return true; }
                getConfig().set("homes." + player.getUniqueId(), player.getLocation());
                saveConfig();
                player.sendMessage(PREFIX + "Sua home foi definida com sucesso!"); return true;
            case "home":
                if (!player.hasPermission("essentialsleve.home")) { deny(player); return true; }
                Location location = getConfig().getLocation("homes." + player.getUniqueId());
                if (location == null || location.getWorld() == null) {
                    player.sendMessage(PREFIX + "Voce ainda nao tem uma home valida. Use §a/sethome§7."); return true;
                }
                player.teleport(location);
                player.sendMessage(PREFIX + "Teleportado para sua home!"); return true;
            case "delhome":
                if (!player.hasPermission("essentialsleve.home")) { deny(player); return true; }
                String key = "homes." + player.getUniqueId();
                if (!getConfig().contains(key)) { player.sendMessage(PREFIX + "Voce nao possui home definida."); return true; }
                getConfig().set(key, null);
                saveConfig();
                player.sendMessage(PREFIX + "Sua home foi removida!"); return true;
            default: return false;
        }
    }

    private void deny(Player player) { player.sendMessage(PREFIX + "§cVoce nao tem permissao para isso."); }

    private boolean requestTp(Player player, String[] args, boolean here) {
        if (!player.hasPermission("essentialsleve.tpa")) { deny(player); return true; }
        if (args.length != 1) {
            player.sendMessage(PREFIX + "Use §a/" + (here ? "tpahere" : "tpa") + " <jogador>§7."); return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) { player.sendMessage(PREFIX + "§cJogador nao encontrado."); return true; }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(PREFIX + "§cVoce nao pode enviar um pedido para si mesmo."); return true;
        }
        long expiresAt = System.currentTimeMillis() + (getConfig().getLong("tpa.expiracao-segundos", 60L) * 1000L);
        requests.put(target.getUniqueId(), new Request(player.getUniqueId(), here, expiresAt));
        player.sendMessage(PREFIX + "Pedido enviado para §a" + target.getName() + "§7.");
        target.sendMessage(PREFIX + "§a" + player.getName() + (here ? " §7quer que voce se teleporte ate ele." : " §7quer se teleportar ate voce."));
        target.sendMessage(PREFIX + "Digite §a/tpaccept §7para aceitar. O pedido expira em breve.");
        return true;
    }

    private boolean acceptTp(Player target) {
        if (!target.hasPermission("essentialsleve.tpa")) { deny(target); return true; }
        Request request = requests.remove(target.getUniqueId());
        if (request == null || System.currentTimeMillis() > request.expiresAt) {
            target.sendMessage(PREFIX + "§cVoce nao tem pedidos de teleporte validos."); return true;
        }
        Player requester = Bukkit.getPlayer(request.sender);
        if (requester == null || !requester.isOnline()) {
            target.sendMessage(PREFIX + "§cO jogador que enviou o pedido saiu do servidor."); return true;
        }
        Player moving = request.here ? target : requester;
        Player destination = request.here ? requester : target;
        if (!moving.teleport(destination.getLocation())) {
            target.sendMessage(PREFIX + "§cNao foi possivel concluir o teleporte."); return true;
        }
        requester.sendMessage(PREFIX + "§aPedido de teleporte aceito!");
        target.sendMessage(PREFIX + "§aTeleporte realizado com sucesso!");
        return true;
    }

    private static final class Request {
        final UUID sender;
        final boolean here;
        final long expiresAt;
        Request(UUID sender, boolean here, long expiresAt) {
            this.sender = sender; this.here = here; this.expiresAt = expiresAt;
        }
    }

    private static final class RtpMenu implements InventoryHolder {
        Inventory inventory;
        @Override public Inventory getInventory() { return inventory; }
    }

    private void showRtpGui(Player player) {
        RtpMenu holder = new RtpMenu();
        Inventory inv = Bukkit.createInventory(holder, 27, "§8Teleporte Aleatorio");
        holder.inventory = inv;
        inv.setItem(11, item(Material.GRASS_BLOCK, "§aMundo Normal", "§7Clique para teleportar"));
        inv.setItem(13, item(Material.NETHERRACK, "§cNether", "§7Clique para teleportar"));
        inv.setItem(15, item(Material.END_STONE, "§dEnd", "§7Clique para teleportar"));
        player.openInventory(inv);
    }

    private ItemStack item(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler public void onInventoryClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof RtpMenu)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getRawSlot() < 0 || event.getRawSlot() >= top.getSize()) return;
        Player player = (Player) event.getWhoClicked();
        World.Environment env;
        switch (event.getRawSlot()) {
            case 11: env = World.Environment.NORMAL; break;
            case 13: env = World.Environment.NETHER; break;
            case 15: env = World.Environment.THE_END; break;
            default: return;
        }
        player.closeInventory();
        startRtp(player, env);
    }

    private void startRtp(Player player, World.Environment environment) {
        World world = null;
        for (World candidate : Bukkit.getWorlds()) {
            if (candidate.getEnvironment() == environment) { world = candidate; break; }
        }
        if (world == null) {
            player.sendMessage(PREFIX + "§cEsse mundo nao esta carregado no servidor."); return;
        }
        long cooldownMillis = Math.max(0L, getConfig().getLong("rtp.cooldown-segundos", 30L)) * 1000L;
        long remaining = rtpCooldown.getOrDefault(player.getUniqueId(), 0L) - System.currentTimeMillis();
        if (remaining > 0 && !player.hasPermission("essentialsleve.rtp.bypass")) {
            player.sendMessage(PREFIX + "§cAguarde " + ((remaining + 999) / 1000) + "s para usar RTP novamente."); return;
        }
        final World finalWorld = world;
        int delay = Math.max(0, getConfig().getInt("rtp.espera-segundos", 3));
        Location origin = player.getLocation().clone();
        player.sendMessage(PREFIX + "Procurando local seguro em §a" + world.getName() + "§7. Teleporte em §e" + delay + "s§7; nao se mova.");
        new BukkitRunnable() {
            @Override public void run() {
                if (!player.isOnline()) return;
                if (!player.getWorld().equals(origin.getWorld()) || player.getLocation().distanceSquared(origin) > 1.0) {
                    player.sendMessage(PREFIX + "§cRTP cancelado: voce se moveu."); return;
                }
                Location safe = findSafeLocation(finalWorld);
                if (safe == null) {
                    player.sendMessage(PREFIX + "§cNao encontrei um local seguro. Tente novamente."); return;
                }
                if (player.teleport(safe)) {
                    rtpCooldown.put(player.getUniqueId(), System.currentTimeMillis() + cooldownMillis);
                    player.sendMessage(PREFIX + "§aTeleportado com sucesso para " + finalWorld.getName() + "!");
                } else {
                    player.sendMessage(PREFIX + "§cNao foi possivel teleportar.");
                }
            }
        }.runTaskLater(this, delay * 20L);
    }

    private Location findSafeLocation(World world) {
        int radius = Math.max(100, getConfig().getInt("rtp.raio-maximo", 3000));
        int minRadius = Math.max(0, Math.min(radius - 1, getConfig().getInt("rtp.raio-minimo", 150)));
        WorldBorder border = world.getWorldBorder();
        Location center = border.getCenter();
        double half = Math.max(0, border.getSize() / 2.0 - 20.0);
        if (half < 10) return null;
        for (int i = 0; i < 40; i++) {
            int dx = random.nextInt(radius * 2 + 1) - radius;
            int dz = random.nextInt(radius * 2 + 1) - radius;
            if (Math.abs(dx) < minRadius && Math.abs(dz) < minRadius) continue;
            int x = (int) Math.floor(center.getX() + dx);
            int z = (int) Math.floor(center.getZ() + dz);
            if (Math.abs(x - center.getX()) >= half || Math.abs(z - center.getZ()) >= half) continue;
            if (world.getEnvironment() == World.Environment.NETHER) {
                int maxY = Math.min(115, world.getMaxHeight() - 3);
                int minY = Math.max(world.getMinHeight() + 3, 35);
                if (maxY <= minY) return null;
                for (int j = 0; j < 10; j++) {
                    int y = minY + random.nextInt(maxY - minY + 1);
                    Location candidate = safeAt(world, x, y, z);
                    if (candidate != null) return candidate;
                }
            } else {
                int y = world.getHighestBlockYAt(x, z) + 1;
                Location candidate = safeAt(world, x, y, z);
                if (candidate != null) return candidate;
            }
        }
        return null;
    }

    private Location safeAt(World world, int x, int y, int z) {
        if (y <= world.getMinHeight() + 1 || y + 2 >= world.getMaxHeight()) return null;
        Block below = world.getBlockAt(x, y - 1, z);
        Block feet = world.getBlockAt(x, y, z);
        Block head = world.getBlockAt(x, y + 1, z);
        Material floor = below.getType();
        if (!floor.isSolid() || floor.name().contains("LEAVES") || floor == Material.MAGMA_BLOCK
                || floor == Material.CACTUS || floor == Material.CAMPFIRE || floor == Material.SOUL_CAMPFIRE
                || floor == Material.POWDER_SNOW || floor == Material.BEDROCK) return null;
        if (!feet.isPassable() || !head.isPassable() || feet.isLiquid() || head.isLiquid()) return null;
        if (feet.getType() == Material.FIRE || feet.getType() == Material.SOUL_FIRE
                || head.getType() == Material.FIRE || head.getType() == Material.SOUL_FIRE) return null;
        return new Location(world, x + 0.5, y, z + 0.5, random.nextFloat() * 360, 0);
    }
}
