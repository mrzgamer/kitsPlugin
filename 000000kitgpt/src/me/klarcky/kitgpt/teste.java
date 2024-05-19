package me.klarcky.kitgpt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class teste extends JavaPlugin {

    private FileConfiguration config;
    private Map<UUID, Map<String, Long>> playerCooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        // Carrega a configuração dos kits
        config = getConfig();
        config.options().copyDefaults(true);
        saveDefaultConfig();

        // Registra o comando
        getCommand("kit").setExecutor(new KitCommandExecutor());
    }

    private class KitCommandExecutor implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Este comando só pode ser executado por jogadores.");
                return true;
            }

            Player player = (Player) sender;
            UUID playerId = player.getUniqueId();

            if (cmd.getName().equalsIgnoreCase("kit")) {
                if (args.length < 1) {
                    sender.sendMessage("Uso: /kit <nome_do_kit>");
                    return true;
                }

                String kitName = args[0].toLowerCase(); // Supondo que o nome do kit é passado como argumento

                if (!config.contains("kits." + kitName)) {
                    player.sendMessage("O kit especificado não existe na configuração.");
                    return true;
                }

                if (!playerCooldowns.containsKey(playerId)) {
                    playerCooldowns.put(playerId, new HashMap<>());
                }

                Map<String, Long> playerKitsCooldowns = playerCooldowns.get(playerId);
                if (playerKitsCooldowns.containsKey(kitName) && !isCooldownOver(playerId, kitName)) {
                    long remainingCooldown = playerKitsCooldowns.get(kitName) - System.currentTimeMillis();
                    player.sendMessage("Aguarde " + (remainingCooldown / 1000) + " segundos para resgatar o kit " + kitName + " novamente.");
                    return true;
                }

                List<Map<?, ?>> items = config.getMapList("kits." + kitName + ".items");
                for (Map<?, ?> item : items) {
                    ItemStack stack = createItemStack(item);
                    player.getInventory().addItem(stack);
                }

                long delaySeconds = config.getLong("kits." + kitName + ".delay");
                playerKitsCooldowns.put(kitName, System.currentTimeMillis() + (delaySeconds * 1000));

                player.sendMessage("Você resgatou o kit " + kitName + ".");
            }

            return true;
        }
    }

    private ItemStack createItemStack(Map<?, ?> itemData) {
        Material type = Material.valueOf((String) itemData.get("type"));
        String name = (String) itemData.get("name");
        List<String> lore = (List<String>) itemData.get("lore");
        int amount = itemData.containsKey("amount") ? (int) itemData.get("amount") : 1;

        ItemStack stack = new ItemStack(type, amount);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        stack.setItemMeta(meta);

        if (itemData.containsKey("enchants")) {
            Map<String, Integer> enchants = (Map<String, Integer>) itemData.get("enchants");
            for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
                meta.addEnchant(Enchantment.getByName(entry.getKey()), entry.getValue(), true);
            }
        }

        return stack;
    }

    private boolean isCooldownOver(UUID playerId, String kitName) {
        Map<String, Long> playerKitsCooldowns = playerCooldowns.get(playerId);
        if (playerKitsCooldowns == null || !playerKitsCooldowns.containsKey(kitName)) {
            return true; // Se não houver cooldown registrado para o jogador ou kit, significa que o cooldown acabou
        }

        long cooldownEnd = playerKitsCooldowns.get(kitName);
        return System.currentTimeMillis() >= cooldownEnd;
    }
}
