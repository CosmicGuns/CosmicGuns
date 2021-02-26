package net.velinquish.cosmicguns.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.velinquish.cosmicguns.CosmicGuns;
import net.velinquish.utils.Common;

public class DiscordCommand extends Command {

	CosmicGuns plugin = CosmicGuns.getInstance();

	public DiscordCommand(String name) {
		super(name);
		setDescription("The server's Dicord llink");
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		plugin.getLangManager().getNode("discord-format").replace(Common.map("%link%", plugin.getConfig().getString("discord"))).execute(sender);
		return false;
	}

}
