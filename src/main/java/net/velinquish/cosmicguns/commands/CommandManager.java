package net.velinquish.cosmicguns.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.velinquish.cosmicguns.CosmicGuns;
import net.velinquish.utils.AnyCommand;

public class CommandManager extends Command {

	private CosmicGuns plugin = CosmicGuns.getInstance();

	public CommandManager(String name) {
		super(name);
		setAliases(plugin.getConfig().getStringList("plugin-aliases"));
		setDescription("Main command for GuardNPC");
	}

	@Override
	public final boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (args.length > 0)
			if ("enable".equalsIgnoreCase(args[0])) {
				handle(new EnableCommand(), sender, args);
				return true;
			} else if ("disable".equalsIgnoreCase(args[0]) || "pause".equalsIgnoreCase(args[0]) || "stop".equalsIgnoreCase(args[0])) {
				handle(new DisableCommand(), sender, args);
				return true;
			} else if ("reload".equalsIgnoreCase(args[0])) {
				handle(new ReloadCommand(), sender, args);
				return true;
			} else if ("ver".equalsIgnoreCase(args[0]) || "version".equalsIgnoreCase(args[0]) || "about".equalsIgnoreCase(args[0])) {
				new VersionCommand().execute(sender, args, false);
				return true;
			}
		plugin.getLangManager().getNode("command-message").execute(sender);

		return false;
	}

	public void handle(AnyCommand cmd, CommandSender sender, String[] args) {
		cmd.execute(sender, args, Arrays.asList(args).contains("-s"));
	}
}
