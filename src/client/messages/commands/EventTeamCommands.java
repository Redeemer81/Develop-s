/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client.messages.commands;
import client.messages.Command;
import client.MapleClient;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import client.messages.CommandDefinition;

/**
 * Provides commands to allow GMs to create event instances and teams, for them to assign members to these
 * teams, for them to disband these teams, destroy events and to manage points.
 * @author Simon
 */
public class EventTeamCommands implements Command {
        @Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception, IllegalCommandSyntaxException {

        }

        @Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("createteam", "[left/right] name", "", 1),
			new CommandDefinition("disbandteam", "name", "", 1),
			new CommandDefinition("setteampoints", "name amount", "", 1),
			new CommandDefinition("addchartoteam", "charname teamname", "", 1),
			new CommandDefinition("remcharfromteam", "charname teamname", "", 1),
			new CommandDefinition("remcharfromteam", " team name", "", 1),
		};
	}
}
