package io.github.chlorojoules.command;

import io.github.chlorojoules.CJMod;
import io.github.chlorojoules.block.CJBlockMachineBase;
import io.github.chlorojoules.machine.CJIMachine;
import io.github.chlorojoules.machine.CJMachineBuilder;
import net.minecraft.common.command.Command;
import net.minecraft.common.command.CommandErrorHandler;
import net.minecraft.common.command.ICommandListener;
import net.minecraft.common.command.completion.CommandCompletion;
import net.minecraft.common.command.completion.CommandCompletionEntity;
import net.minecraft.common.util.ChatColors;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.EntityPlayerMP;

public class CommandCJ extends Command {
	public CommandCJ() {
		super("cj", true, false);
	}

	@Override
	public void onExecute(String[] args, ICommandListener commandExecutor) {
		if(args.length < 2) sendUsage(commandExecutor);

		switch(args[1]) {
			case "reload": {
				if(args.length < 3) sendUsage(commandExecutor);

				CJBlockMachineBase machine = CJMod.machines.get(args[1]);
				String name = machine.machineBuilder.name;
				machine.machineBuilder = new CJMachineBuilder(
						"/machines/" + name + ".json");

				commandExecutor.sendNoticeToOps(
						"Reloading machine config '" + name + "'");
			}

			default: return;
		}
	}

	@Override
	public void printHelpInformation(ICommandListener commandExecutor) {
	}

	private void sendUsage(ICommandListener commandExecutor) {
		CommandErrorHandler.commandUsageMessage(
				this.commandSyntax(), commandExecutor);
	}

	@Override
	public String commandSyntax() {
		return ChatColors.YELLOW + "/cj <reload> ...";
	}

	@Override
	protected CommandCompletion commandCompletion() {
		return CommandCompletionEntity.PLAYER_STRICT;
	}
}
