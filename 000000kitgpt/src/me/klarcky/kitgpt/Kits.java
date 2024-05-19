package me.klarcky.kitgpt;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Kits implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Este comando só pode ser executado por jogadores.");
			return true;
		}

		Player player = (Player) sender;

		if (cmd.getName().equalsIgnoreCase("kit")) {
			if (args.length < 1) {
				sender.sendMessage("Uso: /kit <nome_do_kit>");
				return true;
			}

			String kitName = args[0].toLowerCase(); // Supondo que o nome do kit é passado como argumento

			if (!Main.config.contains("kits." + kitName)) {
				player.sendMessage("O kit especificado não existe na configuração.");
				return true;
			}

			if (!Main.config.contains("kits." + kitName + ".items")) {
				player.sendMessage("O kit especificado não contém itens na configuração.");
				return true;
			}

			List<?> items = Main.config.getList("kits." + kitName + ".items");
			for (Object itemObj : items) {
				if (itemObj instanceof Map) {
					Map<?, ?> item = (Map<?, ?>) itemObj;
					ItemStack stack = createItemStack(item);
					if (stack != null) {
						player.getInventory().addItem(stack);
					} else {
						player.sendMessage("Um item no kit " + kitName + " está mal configurado.");
					}
				}
			}

			player.sendMessage("Você resgatou o kit " + kitName + ".");
		}

		return true;
	}

	private ItemStack createItemStack(Map<?, ?> itemData) {
		Material type = Material.getMaterial((String) itemData.get("type"));
		if (type == null) {
			return null;
		}
		String name = ChatColor.translateAlternateColorCodes('&', (String) itemData.get("name"));
		List<String> lore = (List<String>) itemData.get("lore");
		int amount = itemData.containsKey("amount") ? (int) itemData.get("amount") : 1;

		ItemStack stack = new ItemStack(type, amount);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(name);
		if (lore != null) {
			List<String> coloredLore = lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line))
					.collect(Collectors.toList());
			meta.setLore(coloredLore);
		}
		stack.setItemMeta(meta);

		if (itemData.containsKey("enchants")) {
			Map<?, ?> enchants = (Map<?, ?>) itemData.get("enchants");
			for (Map.Entry<?, ?> entry : enchants.entrySet()) {
				if (entry.getKey() instanceof String && entry.getValue() instanceof Integer) {
					String enchantName = (String) entry.getKey();
					enchantName = converterEncantos(enchantName); // Converte o nome do encantamento
					Enchantment enchantment = Enchantment.getByName(enchantName.toUpperCase());
					if (enchantment != null) {
						stack.addUnsafeEnchantment(enchantment, (int) entry.getValue());
					} else {
						Bukkit.getLogger().warning("Encantamento inválido: " + enchantName);
					}
				}
			}
		}

		return stack;
	}

// Método para converter os nomes dos encantamentos
	public static String converterEncantos(String e) {
		e = e.toLowerCase();
		switch (e) {
		case "protecao":
			return "protection_environmental";
		case "inquebravel":
			return "durability";
		case "espinhos":
			return "thorns";
		case "forca":
			return "arrow_damage";
		case "julgamento":
			return "damage_undead";
		case "ruinas":
			return "damage_arthropods";
		case "afiada":
			return "damage_all";
		case "impacto":
			return "arrow_knockback";
		case "infinidade":
			return "arrow_infinite";
		case "chama":
			return "arrow_fire";
		case "eficiencia":
			return "dig_speed";
		case "afinidade":
			return "water_worker";
		case "afinidade aquatica":
			return "water_worker";
		case "toque":
			return "silk_touch";
		case "projeteis":
			return "protection_projectile";
		case "protecao contra fogo":
			return "protection_fire";
		case "fogo":
			return "protection_fire";
		case "pena":
			return "protection_fall";
		case "explosoes":
			return "protection_explosions";
		case "respiracao":
			return "oxygen";
		case "pilhagem":
			return "loot_bonus_mobs";
		case "fortuna":
			return "loot_bonus_blocks";
		case "repulsao":
			return "knockback";
		case "flamejante":
			return "fire_aspect";
		case "remendo":
			return "mending";
		default:
			return e;
		}
	}
}
