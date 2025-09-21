package fr.openmc.core.features.tpa;

import fr.openmc.core.CommandsManager;
import fr.openmc.core.OMCPlugin;
import fr.openmc.core.features.tpa.commands.TPACancelCommand;
import fr.openmc.core.features.tpa.commands.TPACommand;
import fr.openmc.core.features.tpa.commands.TPADenyCommand;
import fr.openmc.core.features.tpa.commands.TPAcceptCommand;

public class TPAManager {
	
	public TPAManager() {
		CommandsManager.getHandler().register(
				new TPAcceptCommand(),
				new TPACommand(OMCPlugin.getInstance()),
				new TPADenyCommand(),
				new TPACancelCommand()
		);
	}
}
