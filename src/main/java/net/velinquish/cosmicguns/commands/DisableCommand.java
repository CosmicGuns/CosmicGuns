package net.velinquish.cosmicguns.commands;

import org.bukkit.command.CommandSender;

import net.velinquish.cosmicguns.CosmicGuns;
import net.velinquish.utils.AnyCommand;

public class DisableCommand extends AnyCommand {

	CosmicGuns plugin = CosmicGuns.getInstance();

	@Override
	protected void run(CommandSender sender, String[] args, boolean silent) {
		checkPermission(plugin.getPermission());

		plugin.pauseCycle();
		tell(plugin.getLangManager().getNode("cycle-disabled"));
	}

}
