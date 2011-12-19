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

import java.rmi.RemoteException;
import static client.messages.CommandProcessor.getNamedIntArg;
import static client.messages.CommandProcessor.joinAfterString;

import java.text.DateFormat;
import java.util.Calendar;

import client.MapleCharacter;
import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import net.channel.ChannelServer;
import tools.MaplePacketCreator;
import tools.StringUtil;

public class BanningCommands implements Command {
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception {
		ChannelServer cserv = c.getChannelServer();
		if (splitted[0].equals("!ban")) {
			if (splitted.length < 3) {
				throw new IllegalCommandSyntaxException(3);
			}
			String originalReason = StringUtil.joinStringFrom(splitted, 2);
                        String reason = c.getPlayer().getName() + " banned " + splitted[1] + ": " + originalReason;
			MapleCharacter target = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
			if (target != null) {
				String readableTargetName = MapleCharacter.makeMapleReadable(target.getName());
				String ip = target.getClient().getSession().getRemoteAddress().toString().split(":")[0];
				reason += " (IP: " + ip + ")";
				target.ban(reason, true);
                            cserv.broadcastPacket(MaplePacketCreator.serverNotice(6, readableTargetName + " has been banned for " + originalReason));

                mc.dropMessage(readableTargetName + "'s IP: " + ip + "!");
			} else {
		if (MapleCharacter.ban(splitted[1], reason, false)) {
                    @SuppressWarnings("unused")
					String readableTargetName = MapleCharacter.makeMapleReadable(target.getName());
                    String ip = target.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                    reason += " (IP: " + ip + ")";
                    cserv.broadcastPacket(MaplePacketCreator.serverNotice(6, readableTargetName + " has been banned for " + originalReason));

				} else {
					mc.dropMessage("Failed to ban " + splitted[1]);
				}
			}
		} else if (splitted[0].equals("!tempban")) {
			Calendar tempB = Calendar.getInstance();
			String originalReason = joinAfterString(splitted, ":");

			if (splitted.length < 4 || originalReason == null) {
				// mc.dropMessage("Syntax helper: !tempban <name> [i / m / w / d / h] <amount> [r [reason id] : Text
				// Reason");
				throw new IllegalCommandSyntaxException(4);
			}

			int yChange = getNamedIntArg(splitted, 1, "y", 0);
			int mChange = getNamedIntArg(splitted, 1, "m", 0);
			int wChange = getNamedIntArg(splitted, 1, "w", 0);
			int dChange = getNamedIntArg(splitted, 1, "d", 0);
			int hChange = getNamedIntArg(splitted, 1, "h", 0);
			int iChange = getNamedIntArg(splitted, 1, "i", 0);
			int gReason = getNamedIntArg(splitted, 1, "r", 7);

			String reason = c.getPlayer().getName() + " tempbanned " + splitted[1] + ": " + originalReason;

			if (gReason > 14) {
				mc.dropMessage("You have entered an incorrect ban reason ID, please try again.");
				return;
			}

			DateFormat df = DateFormat.getInstance();
			tempB.set(tempB.get(Calendar.YEAR) + yChange, tempB.get(Calendar.MONTH) + mChange, tempB.get(Calendar.DATE) +
				(wChange * 7) + dChange, tempB.get(Calendar.HOUR_OF_DAY) + hChange, tempB.get(Calendar.MINUTE) +
				iChange);

			MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);

			if (victim == null) {
				int accId = MapleClient.findAccIdForCharacterName(splitted[1]);
				if (accId >= 0 && MapleCharacter.tempban(reason, tempB, gReason, accId)) {
                                String readableTargetName = MapleCharacter.makeMapleReadable(victim.getName());
                                cserv.broadcastPacket(MaplePacketCreator.serverNotice(6, readableTargetName + " has been banned for " + originalReason));
                                
				} else {
					mc.dropMessage("There was a problem offline banning character " + splitted[1] + ".");
				}
			} else {
				victim.tempban(reason, tempB, gReason);
				mc.dropMessage("The character " + splitted[1] + " has been successfully tempbanned till " +
					df.format(tempB.getTime()));
			}
                } else if (splitted[0].equals("!strike")) {
                        MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                        victim.addStrike(c.getPlayer().getName());
                        victim.getClient().disconnect();
                } else if (splitted[0].equals("!resetstrikes")) {
                        MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                        victim.resetStrikes();
		} else if (splitted[0].equals("!dc")) {
			int level = 0;
			MapleCharacter victim;
			if (splitted[1].charAt(0) == '-') {
				level = StringUtil.countCharacters(splitted[1], 'f');
				victim = cserv.getPlayerStorage().getCharacterByName(splitted[2]);
			} else {
				victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
			}
                        
                        if (level < 2)
                        {
                          victim.getClient().getSession().close();
                          if (level >= 1) {
                    victim.getClient().disconnect();
                }
			}
                        else {
                                mc.dropMessage("Please use dc -f instead.");
                                //This, apparently, crashes the server. (Credits to Alysha, rofl =P)
                                
				/*victim.saveToDB(true, victim.getMap().getForcedReturnId());
				cserv.removePlayer(victim);*/
			}
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("ban", "charname reason", "Permanently ip, mac and accountbans the given character", 1),
			new CommandDefinition("tempban", "<name> [i / m / w / d / h] <amount> [r  [reason id]] : Text Reason", "Tempbans the given account", 1),
			new CommandDefinition("dc", "[-f] name", "Disconnects player matching name provided. Use -f only if player is persistant!", 1),
			new CommandDefinition("strike", "name", "Adds a strike to the user and tempbans for an amount of time decided by their current strike number.", 1),
			new CommandDefinition("resetstrikes", "name", "Resets a player's strike number.", 2),
		};
	}

}
