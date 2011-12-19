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
import client.MapleDisease;
import client.status.MonsterStatus;
import java.awt.Point;
import net.MaplePacket;
import net.SendPacketOpcode;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MobSkillFactory;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 *
 * @author Jvlaple, BubblesDev v75 (Moogra)
 */
public class MonsterCarnivalHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer().getParty() == null || !c.getPlayer().isAlive()) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        int tab = slea.readByte();
        int num = slea.readByte();
        int neededCP = getCPNeeded(tab, num);
        if (c.getPlayer().getCp() < neededCP) {
            c.getPlayer().message("You do not have enough CP to use this.");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (tab == 0) {
            MapleMonster mob = MapleLifeFactory.getMonster(9300127 + num);
            Point spawnPos = null;
//            Point spawnPos = c.getPlayer().getMap().getRandomSP(c.getPlayer().getTeam());
            if (spawnPos == null) {
                c.getPlayer().message("The monster cannot be summoned, as all spawn points are taken.");
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            mob.setPosition(spawnPos);
            c.getPlayer().getMap().addMonsterSpawn(mob.getId(), 1, spawnPos, true);
            c.getSession().write(MaplePacketCreator.enableActions());
        } else if (tab == 1) { //debuffs
            MapleDisease debuff = null;
            boolean wholeParty = false;
            int skillId = -1;
            int level = 6;
            switch (num) {
                case 0: //darkness
                    debuff = MapleDisease.DARKNESS;
                    wholeParty = true;
                    skillId = 121;
                    break;
                case 1: //weakness
                    debuff = MapleDisease.WEAKEN;
                    wholeParty = true;
                    skillId = 122;
                    level = 7;
                    break;
                case 2: //curse
                    debuff = MapleDisease.CURSE;
                    skillId = 124;
                    break;
                case 3: //poison
                    debuff = MapleDisease.POISON;
                    skillId = 125;
                    break;
                case 4: //slow
                    debuff = MapleDisease.SLOW;
                    wholeParty = true;
                    skillId = 126;
                    break;
                case 5: //seal
                    debuff = MapleDisease.SEAL;
                    skillId = 120;
                    level = 10;
                    break;
                case 6: //stun
                    debuff = MapleDisease.STUN;
                    skillId = 123;
                    level = 11;
                    break;
                case 7: //cancel buff
                    c.getPlayer().getMap().getRandomOpposingPlayer(c.getPlayer()).dispel();
                    break;
            }
            if (num != 7) {
                if (wholeParty) {
                    for (MapleCharacter mc : c.getChannelServer().getPartyMembers(c.getPlayer().getParty().getOpponent(), 1)) {
                        if (mc != null) {
                            mc.giveDebuff(debuff, MobSkillFactory.getMobSkill(skillId, level));
                        }
                    }
                } else if (c.getPlayer().getParty().getOpponent() != null) {
                    c.getChannelServer().getPartyMembers(c.getPlayer().getParty().getOpponent(), tab).get(Randomizer.getInstance().nextInt(c.getPlayer().getParty().getOpponent().getMembers().size())).giveDebuff(debuff, MobSkillFactory.getMobSkill(skillId, level));
                }
            }
            c.getSession().write(MaplePacketCreator.enableActions());
        } else if (tab == 2) { //protectors
            MonsterStatus status = null;
            if (num == 0) {
                status = MonsterStatus.WEAPON_ATTACK_UP;
            } else if (num == 1) {
                status = MonsterStatus.WEAPON_DEFENSE_UP;
            } else if (num == 2) {
                status = MonsterStatus.MAGIC_ATTACK_UP;
            } else if (num == 3) {
                status = MonsterStatus.MAGIC_DEFENSE_UP;
            } else if (num == 7) {
                status = MonsterStatus.WEAPON_IMMUNITY;
            } else if (num == 8) {
                status = MonsterStatus.MAGIC_IMMUNITY;
            }
            if (status != null) {
//                int success = c.getPlayer().getMap().spawnGuardian(status, c.getPlayer().getTeam());
                int success = 100;
                if (success < 1) {
                    if (success == -1) {
                        c.getPlayer().message("The protector cannot be summoned, as all protector spots are taken.");
                    } else {
                        c.getPlayer().message("The protector is already summoned.");
                    }
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
            } else {
                c.getPlayer().message("The protector cannot be summoned.");
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            c.getSession().write(MaplePacketCreator.enableActions());
        }
        c.getPlayer().increaseCp(-neededCP);
        c.getPlayer().getMap().broadcastMessage(playerSummoned(c.getPlayer().getName(), tab, num));
    }

    private int getCPNeeded(int tab, int pos) {
        if (tab == 0) {
            if (pos < 6) {
                return 7 + pos / 2;
            } else if (pos < 9) {
                return pos + 4;
            } else {
                return 30;
            }
        } else if (tab == 1) {
            int[] array = {17, 19, 12, 19, 16, 14, 22, 18};
            return array[pos];
        } else {
            if (pos == 4) {
                return 13;
            } else if (pos < 6) {
                return 17 - pos % 2;
            } else if (pos > 7) {
                return 35;
            } else {
                return 12;
            }
        }
    }

    private static MaplePacket playerSummoned(String name, int tab, int number) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_SUMMON.getValue());
        mplew.write(tab);
        mplew.write(number);
        mplew.writeMapleAsciiString(name);
        return mplew.getPacket();
    }
}
