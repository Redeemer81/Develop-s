/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package client.messages.commands;

import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.CommandProcessor;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;

public class HelpCommand implements Command {
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splittedLine) throws Exception,
																					IllegalCommandSyntaxException {
		int page = CommandProcessor.getOptionalIntArg(splittedLine, 1, 1);
		CommandProcessor.getInstance().dropHelp(c.getPlayer(), mc, page);
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("help", "[page - defaults to 1]", "Shows the help", 3),
		};
	}
}
