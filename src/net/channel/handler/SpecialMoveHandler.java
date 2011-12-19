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

import java.awt.Point;
import client.ISkill;
import client.MapleCharacter;
import client.MapleCharacter.CancelCooldownAction;
import client.MapleClient;
import client.MapleDisease;
import client.MapleStat;
import client.SkillFactory;
import constants.skills.*;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import server.MapleStatEffect;
import server.TimerManager;
import server.life.MapleMonster;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import server.maps.MapleSummon;
import server.maps.SummonMovementType;

public final class SpecialMoveHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer().getDiseases().contains(MapleDisease.SEAL)) {
            return;
        }

        if(c.getPlayer().getMap().getProperties().getProperty("skills").equals(Boolean.FALSE) && !c.getPlayer().isGM())
        {
            c.getPlayer().dropMessage("A GM has disabled skills in this map. You will still see your skills, however nobody else will, and attacks will not do damage.");
            return;
        }
        slea.readInt();
        int skillid = slea.readInt();
        Point pos = null;
        int __skillLevel = slea.readByte();
        ISkill skill = SkillFactory.getSkill(skillid);
        int skillLevel = c.getPlayer().getSkillLevel(skill);
        if (skillid % 10000000 == 1010 || skillid % 10000000 == 1011) {
            skillLevel = 1;
            c.getPlayer().setDojoEnergy(0);
            c.getSession().write(MaplePacketCreator.getEnergy(0));
        }
        MapleStatEffect effect = skill.getEffect(skillLevel);
        if (effect.getCooldown() > 0) {
            if (c.getPlayer().skillisCooling(skillid)) {
                return;
            } else if (skillid != Corsair.BATTLE_SHIP) {
                c.getSession().write(MaplePacketCreator.skillCooldown(skillid, effect.getCooldown()));
                c.getPlayer().addCooldown(skillid, System.currentTimeMillis(), effect.getCooldown() * 1000, TimerManager.getInstance().schedule(new CancelCooldownAction(c.getPlayer(), skillid), effect.getCooldown() * 1000));
            }
        }
        if (skillid == Hero.MONSTER_MAGNET || skillid == Paladin.MONSTER_MAGNET || skillid == DarkKnight.MONSTER_MAGNET) { // Monster Magnet
            int num = slea.readInt();
            int mobId;
            for (int i = 0; i < num; i++) {
                mobId = slea.readInt();
                c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showMagnet(mobId, slea.readByte()), false);
                MapleMonster monster = c.getPlayer().getMap().getMonsterByOid(mobId);
                if (monster != null) {
                    monster.switchController(c.getPlayer(), monster.isControllerHasAggro());
                }
            }
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showBuffeffect(c.getPlayer(), c.getPlayer().getId(), skillid, 1), false);
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        } else if (skillid == Buccaneer.TIME_LEAP) {
            if (c.getPlayer().getParty() != null) {
                for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                    for (MapleCharacter mc : cserv.getPartyMembers(c.getPlayer().getParty(), c.getPlayer().getMapId())) {
                        mc.removeAllCooldownsExcept(Buccaneer.TIME_LEAP);
                    }
                }
            } else {
                c.getPlayer().removeAllCooldownsExcept(Buccaneer.TIME_LEAP);
            }
        } else if (skillid == Brawler.MP_RECOVERY) {// MP Recovery
            ISkill s = SkillFactory.getSkill(skillid);
            MapleStatEffect ef = s.getEffect(c.getPlayer().getSkillLevel(s));
            int lose = c.getPlayer().getMaxHp() / ef.getX();
            c.getPlayer().setHp(c.getPlayer().getHp() - lose);
            c.getPlayer().updateSingleStat(MapleStat.HP, c.getPlayer().getHp());
            int gain = lose * (ef.getY() / 100);
            c.getPlayer().setMp(c.getPlayer().getMp() + gain);
            c.getPlayer().updateSingleStat(MapleStat.MP, c.getPlayer().getMp());
        } else if (skillid == WhiteKnight.HP_RECOVERY) {
            ISkill s = SkillFactory.getSkill(skillid);
            MapleStatEffect ef = s.getEffect(c.getPlayer().getSkillLevel(s));
            int gain = (c.getPlayer().getMaxHp() / ef.getX()) * 10;
            c.getPlayer().setHp(c.getPlayer().getHp() + gain);
            c.getPlayer().updateSingleStat(MapleStat.HP, c.getPlayer().getHp());
        } else if (skillid % 10000000 == 1004) {
            slea.readShort();
        }
        if (slea.available() >= 4) {
            pos = new Point(slea.readShort(), slea.readShort());
        }
        if (skillLevel == 0 || skillLevel != __skillLevel) {
            return;
        } else if (c.getPlayer().isAlive()) {
            if (skill.getId() != Priest.MYSTIC_DOOR || c.getPlayer().canDoor()) {
                skill.getEffect(skillLevel).applyTo(c.getPlayer(), pos);
            } else {
                c.getPlayer().message("Please wait 5 seconds before casting Mystic Door again");
                c.getSession().write(MaplePacketCreator.enableActions());
            }
        } else {
            c.getSession().write(MaplePacketCreator.enableActions());
        }
    }
}
