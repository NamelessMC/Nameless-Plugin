package com.namelessmc.plugin.bukkit;

import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.audiences.AbstractAudienceProvider;
import com.namelessmc.plugin.common.audiences.NamelessCommandSender;
import com.namelessmc.plugin.common.command.CommonCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.derkades.derkutils.bukkit.reflection.ReflectionUtil;

import java.util.Collections;
import java.util.List;

import static com.namelessmc.plugin.common.LanguageHandler.Term.COMMAND_NO_PERMISSION;

public class BukkitCommandProxy {

	static void registerCommands(final NamelessPlugin plugin,
								 final BukkitNamelessPlugin bukkitPlugin) {
		CommonCommand.commands(plugin).forEach(command -> {
			final String name = command.actualName();
			if (name == null) {
				return; // Command is disabled
			}

			final PlainTextComponentSerializer ser = PlainTextComponentSerializer.plainText();
			final String description = ser.serialize(command.description());
			final String usage = ser.serialize(command.usage());

			final Command spigotCommand = new SpigotCommand(bukkitPlugin, plugin.audiences(), command, name, description, usage);

			ReflectionUtil.registerCommand("nameless", spigotCommand);
		});
	}

	private static class SpigotCommand extends Command implements PluginIdentifiableCommand {

		private final BukkitNamelessPlugin bukkitPlugin;
		private final AbstractAudienceProvider audiences;
		private final CommonCommand command;

		protected SpigotCommand(final BukkitNamelessPlugin bukkitPlugin,
								final AbstractAudienceProvider audiences,
								final CommonCommand command,
								final String name,
								final String plainDescription,
								final String plainUsage) {
			super(name, plainDescription, plainUsage, Collections.emptyList());

			this.bukkitPlugin = bukkitPlugin;
			this.audiences = audiences;
			this.command = command;
		}

		private @Nullable NamelessCommandSender bukkitToNamelessSender(final CommandSender bukkitCommandSender) {
			if (bukkitCommandSender instanceof Player) {
				return this.audiences.player(((Player) bukkitCommandSender).getUniqueId());
			} else if (bukkitCommandSender instanceof ConsoleCommandSender) {
				return this.audiences.console();
			} else {
				return null;
			}
		}

		@Override
		public boolean execute(final CommandSender bukkitCommandSender, final String commandLabel, final String[] args) {
			final NamelessCommandSender sender = bukkitToNamelessSender(bukkitCommandSender);
			if (sender == null) {
				bukkitCommandSender.sendMessage("ERROR: Audience is null");
				return true;
			}

			command.verifyPermissionThenExecute(sender, args);
			return true;
		}

		@Override
		public List<String> tabComplete(final CommandSender bukkitCommandSender, final String alias, final String[] args) throws IllegalArgumentException {
			final NamelessCommandSender sender = bukkitToNamelessSender(bukkitCommandSender);
			if (sender == null) {
				return Collections.singletonList("ERROR: Audience is null");
			}
			return command.complete(sender, args);
		}

		@Override
		public Plugin getPlugin() {
			return this.bukkitPlugin;
		}

	}

}
