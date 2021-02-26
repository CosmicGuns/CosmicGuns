package net.velinquish.cosmicguns;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.velinquish.utils.Common;

public class RawCommand extends Command {

	CosmicGuns plugin;

	protected RawCommand(CosmicGuns plugin) {
		super("raw");
		setUsage("/raw <message>");
		setDescription("Broadcasts a colorized message.");
		this.plugin = plugin;
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!sender.hasPermission("velinquish.use.raw")) {
			plugin.getLangManager().getNode("no-permission").replace(Common.map("%permission%", "velinquish.use.raw")).execute(sender);
			return false;
		}

		if (args.length < 1) {
			plugin.getLangManager().getNode("command-raw-usage").execute(sender);
			return false;
		}

		Bukkit.broadcastMessage(Common.colorize(message(0, args)));

		return false;
	}

	private String message(int argsIndex, String[] args) {
		String msg = "";
		for (int i = argsIndex; i < args.length; i++)
			msg += args[i] + (i == args.length ? "" : " ");
		return msg;
	}
}
