/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package net.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import net.world.remote.WorldChannelInterface;
import net.world.MapleParty;
import net.world.MaplePartyCharacter;
import net.world.PartyOperation;

public final class DenyPartyRequestHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte action = slea.readByte();
        int fromCID = slea.readInt();
        MapleParty party;
        MapleCharacter player = c.getPlayer();
        WorldChannelInterface wci = c.getChannelServer().getWorldInterface();
        MapleCharacter cfrom = c.getChannelServer().getPlayerStorage().getCharacterById(fromCID);
        MaplePartyCharacter partyplayer = new MaplePartyCharacter(player);
        if (cfrom != null) {
            switch(action) {
                case 30: //deny
                    cfrom.getClient().getSession().write(MaplePacketCreator.partyMessage(c.getPlayer().getName() + "님이 파티 초대를 거절하였습니다"));
                    break;
                case 31: { // accept invitation (lol used to be 3)
                    int partyid = cfrom.getPartyId();
                    if (c.getPlayer().getParty() == null) {
                        try {
                            party = wci.getParty(partyid);
                            if (party != null) {
                                if (party.getMembers().size() < 6) {
                                    wci.updateParty(party.getId(), PartyOperation.JOIN, partyplayer);
                                    player.receivePartyMemberHP();
                                    player.updatePartyMemberHP();
                                } else {
                                    c.getSession().write(MaplePacketCreator.partyStatusMessage(17));
                                }
                            } else {
                                c.getPlayer().dropMessage(5, "The party you are trying to join does not exist");
                            }
                        } catch (Exception e) {
                            c.getChannelServer().reconnectWorld();
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "You can't join the party as you are already in one");
                    }
                    break;
                }
            }
        }
    }
}
