package net.velinquish.cosmicguns;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.velinquish.utils.Common;

public class RawtoCommand extends Command {

	CosmicGuns plugin;

	protected RawtoCommand(CosmicGuns plugin) {
		super("rawto");
		setUsage("/rawto <player> <message>");
		setDescription("Sends a colorized message to the specified player.");
		this.plugin = plugin;
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (!sender.hasPermission("velinquish.use.raw")) {
			plugin.getLangManager().getNode("no-permission").replace(Common.map("%permission%", "velinquish.use.raw")).execute(sender);
			return false;
		}

		if (args.length < 1) {
			plugin.getLangManager().getNode("command-rawto-usage").execute(sender);
			return false;
		}

		Player player = Bukkit.getPlayer(args[0]);

		if (player == null) {
			plugin.getLangManager().getNode("player-not-found").execute(sender);
			return false;
		}

		Common.tell(player, message(1, args));

		return false;
	}

	private String message(int argsIndex, String[] args) {
		String msg = "";
		for (int i = argsIndex; i < args.length; i++)
			msg += args[i] + (i == args.length ? "" : " ");
		return msg;
	}

}
