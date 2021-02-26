package net.velinquish.cosmicguns;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import lombok.Getter;
import net.velinquish.cosmicguns.commands.CommandManager;
import net.velinquish.cosmicguns.commands.DiscordCommand;
import net.velinquish.utils.Common;
import net.velinquish.utils.VelinquishPlugin;
import net.velinquish.utils.lang.LangManager;

public class CosmicGuns extends JavaPlugin implements Listener, VelinquishPlugin {

	@Getter
	private static CosmicGuns instance;
	@Getter
	private LangManager langManager;

	@Getter
	private String prefix;
	@Getter
	private String permission;
	@Getter
	private World world;

	private static boolean debug;

	@Getter
	private YamlConfiguration config;
	private File configFile;

	@Getter
	private YamlConfiguration lang;
	private File langFile;

	private BukkitTask cycle;
	private List<BukkitTask> subcycles;
	private String originalDoCycle; //saved value of doDayLightCycle before cycle begins

	private List<String> bossCycle;
	private int bossIndex;

	private int cometInterval;
	private int cycleIndex;

	@Override
	public void onEnable() {
		instance = this;
		Common.setInstance(this);

		langManager = new LangManager();

		subcycles = new ArrayList<>();

		try {
			loadFiles();
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

		getServer().getPluginManager().registerEvents(this, this);
		Common.registerCommand(new CommandManager(getConfig().getString("main-command")));
		Common.registerCommand(new DiscordCommand("discord"));
		Common.registerCommand(new RawCommand(this));
		Common.registerCommand(new RawtoCommand(this));
	}

	@Override
	public void onDisable() {
		instance = null;
	}

	public void loadFiles() throws IOException, InvalidConfigurationException {
		if (cycle != null)
			pauseCycle();
		configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			configFile.getParentFile().mkdirs();
			saveResource("config.yml", false);
		}
		config = new YamlConfiguration();
		config.load(configFile);

		prefix = getConfig().getString("plugin-prefix");
		debug = getConfig().getBoolean("debug");
		permission = getConfig().getString("permission");
		world = Bukkit.getWorld(getConfig().getString("world"));
		bossCycle = getConfig().getStringList("boss-cycle");
		cometInterval = getConfig().getInt("comet.comet-interval");

		bossIndex = 0;
		cycleIndex = 0;

		langFile = new File(getDataFolder(), "lang.yml");
		if (!langFile.exists()) {
			langFile.getParentFile().mkdirs();
			saveResource("lang.yml", false);
		}
		lang = new YamlConfiguration();
		lang.load(langFile);

		langManager.clear();
		langManager.setPrefix(prefix);
		langManager.loadLang(lang);
		beginCycle();
	}

	public void pauseCycle() {
		cycle.cancel();
		world.setGameRuleValue("doDaylightCycle", originalDoCycle);
		cycle = null;
		for (BukkitTask subtask : subcycles)
			subtask.cancel();
	}

	private void spawnBoss() {
		String boss = bossCycle.get(bossIndex % bossCycle.size());
		if (!boss.equals("")) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), getConfig().getString("boss-command").replaceAll("%mythicmob%", boss)
					.replaceAll("%loc%", getConfig().getString("boss-locations." + boss + ".x") + " " +
							getConfig().getString("boss-locations." + boss + ".y") + " " +
							getConfig().getString("boss-locations." + boss + ".z") + " " +
							getConfig().getString("boss-locations." + boss + ".world")));
			Bukkit.broadcastMessage(Common.colorize(langManager.getNode("boss-spawned").toString()));
		}
		bossIndex++;
	}

	/**
	 * Mainly used for spawning dimension-specific bosses that aren't part of the usual boss cycle
	 * @param boss The MythicMobs ID for the boss
	 */
	private void spawnBoss(String boss) {
		if (!boss.equals("")) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), getConfig().getString("boss-command").replaceAll("%mythicmob%", boss)
					.replaceAll("%loc%", getConfig().getString("boss-locations." + boss + ".x") + " " +
							getConfig().getString("boss-locations." + boss + ".y") + " " +
							getConfig().getString("boss-locations." + boss + ".z") + " " +
							getConfig().getString("boss-locations." + boss + ".world")));
			Bukkit.broadcast(Common.colorize(getConfig().getString("dimensional-bosses." + boss + ".broadcast")),
					getConfig().getString("dimensional-bosses." + boss + ".permission"));
		}
	}

	public void beginCycle() {
		if (cycle != null)
			return;
		originalDoCycle = world.getGameRuleValue("doDaylightCycle");
		world.setGameRuleValue("doDaylightCycle", "false");
		cycle = Bukkit.getScheduler().runTaskTimer(this, () -> {
			world.setTime(config.getInt("day-time")); // 4pm is 10,000 ticks
			Bukkit.broadcastMessage(Common.colorize(langManager.getNode("day").toString()));

			// Decides which dimensional bosses to spawn
			for (String boss : config.getConfigurationSection("dimensional-bosses").getKeys(false))
				if (cycleIndex % config.getInt("dimensional-bosses." + boss + ".frequency") == 0) {
					debug("Boss " + boss + " will spawn during this cycle");
					int begin = config.getInt("dimensional-bosses." + boss + ".begin");
					int end = config.getInt("dimensional-bosses." + boss + ".end");
					subcycles.add(Bukkit.getScheduler().runTaskLater(instance, () -> {
						spawnBoss(boss);
					}, (int) (Math.random() * (end - begin) + 1) + begin));
				}

			// Runs dawn after day
			subcycles.add(Bukkit.getScheduler().runTaskLater(instance, () -> world.setTime(config.getInt("dawn-time")), config.getInt("dawn-start")));

			// Runs night
			subcycles.add(Bukkit.getScheduler().runTaskLater(instance, () -> {
				world.setTime(config.getInt("night-time"));
				Bukkit.broadcastMessage(Common.colorize(langManager.getNode("night").toString()));

				// Runs dawn after night
				subcycles.add(Bukkit.getScheduler().runTaskLater(instance, () -> world.setTime(config.getInt("dawn-time")), config.getInt("dawn-start")));
			}, config.getInt("night-start")));

			int begin = config.getInt("meteor.begin"), end = config.getInt("meteor.end");
			int delay = (int) (Math.random() * (end - begin) + 1) + begin;

			// Runs meteor warning
			subcycles.add(Bukkit.getScheduler().runTaskLater(instance, () -> {
				if (cycleIndex % (cometInterval + 1) == 0)
					//broadcast comet-warning to all with permission
					for (Player player : Bukkit.getOnlinePlayers())
						if (player.hasPermission("worlds.access.space"))
							langManager.getNode("comet-spawning").execute(player);
						else
							langManager.getNode("meteor-spawning").execute(player);
				else
					Bukkit.broadcastMessage(Common.colorize(langManager.getNode("meteor-spawning").toString()));
			}, delay - getConfig().getInt("meteor-warning")));

			// Runs meteors
			subcycles.add(Bukkit.getScheduler().runTaskLater(instance, () -> {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "met spawn");
				if (cycleIndex % (cometInterval + 1) == 0) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "comet spawn");
					for (Player player : Bukkit.getOnlinePlayers())
						if (player.hasPermission("worlds.access.space"))
							langManager.getNode("comet-spawned").execute(player);
						else
							langManager.getNode("meteor-spawned").execute(player);
				} else
					Bukkit.broadcastMessage(Common.colorize(langManager.getNode("meteor-spawned").toString()));
			}, delay));

			begin = config.getInt("boss.begin"); end = config.getInt("boss.end");

			// Spawns bosses
			subcycles.add(Bukkit.getScheduler().runTaskLater(instance, () -> {
				spawnBoss();
			}, (int) (Math.random() * (end - begin) + 1) + begin));

			cycleIndex++;

		}, 0, config.getInt("cycle-length"));
	}

	@EventHandler
	public void craftItem(CraftItemEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void useAnvil(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.ANVIL)
			e.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void findMobUUID(PlayerInteractEntityEvent e) {
		debug("UUID of Right Clicked: " + e.getRightClicked().getUniqueId());
		if (e.getPlayer().hasPermission("velinquish.uuid"))
			Common.tell(e.getPlayer(), "UUID of Right Clicked: " + e.getRightClicked().getUniqueId());
	}

	public static void debug(String message) {
		if (debug == true)
			Common.log(message);
	}
}
