package me.klarcky.kitgpt;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    static FileConfiguration config;
    private FileConfiguration delaysConfig;

    @Override
    public void onEnable() {
        // Carrega a configura��o dos kits
        config = getConfig();
        config.options().copyDefaults(true);
        saveDefaultConfig();

        // Carrega a configura��o dos delays
        delaysConfig = getConfiguration("delays.yml");
        delaysConfig.options().copyDefaults(true);
        saveDelaysConfig();

        // Registra o comando
        getCommand("kit").setExecutor(new Kits());
    }

    private FileConfiguration getConfiguration(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	// M�todo para salvar a configura��o dos delays
    private void saveDelaysConfig() {
        saveConfig();
    }
}
