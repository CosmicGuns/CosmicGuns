package net.velinquish.cosmicguns.commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.velinquish.cosmicguns.CosmicGuns;
import net.velinquish.utils.AnyCommand;

public class SpawnerCommand extends AnyCommand {

	CosmicGuns plugin = CosmicGuns.getInstance();

	@Override
	protected void run(CommandSender sender, String[] args, boolean silent) {
		checkPermission(plugin.getPermission());

		ItemStack spawner = new ItemStack(Material.MOB_SPAWNER);
		ItemMeta data = spawner.getItemMeta();
	}

}
