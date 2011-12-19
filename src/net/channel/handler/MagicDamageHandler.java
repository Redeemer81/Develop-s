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

import java.util.List;
import client.ISkill;
import client.MapleCharacter;
import client.MapleCharacter.CancelCooldownAction;
import client.MapleClient;
import client.SkillFactory;
import net.MaplePacket;
import server.MapleStatEffect;
import server.TimerManager;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import constants.skills.Evan;

public final class MagicDamageHandler extends AbstractDealDamageHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        AttackInfo attack = parseDamage(slea, false, true);
        MapleCharacter player = c.getPlayer();
        if(c.getPlayer().getMap().getProperties().getProperty("skills").equals(Boolean.FALSE) && !player.isGM())
        {
            c.getPlayer().dropMessage("A GM has disabled skills in this map. You will still see your skills, however nobody else will, and attacks will not do damage.");
            return;
        }
        if((attack.skill == 2121007 || attack.skill == 2221007 || attack.skill == 2321008) && (c.getPlayer().getMap().getProperties().getProperty("aoe").equals(Boolean.FALSE)) && (!c.getPlayer().isGM()))
        {
            c.getPlayer().dropMessage("Large 4th job AoE skills have been disabled in this map by a GM.");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        MaplePacket packet = MaplePacketCreator.magicAttack(player.getId(), attack.skill, attack.stance, attack.numAttackedAndDamage, attack.allDamage, -1, attack.speed, attack.UNK80);
        if (attack.skill == 2121001 || attack.skill == 2221001 || attack.skill == 2321001 || attack.skill == Evan.ICE_BREATH || attack.skill == Evan.FIRE_BREATH) {
            packet = MaplePacketCreator.magicAttack(player.getId(), attack.skill, attack.stance, attack.numAttackedAndDamage, attack.allDamage, attack.charge, attack.speed, attack.UNK80);
        }
        player.getMap().broadcastMessage(player, packet, false, true);
        MapleStatEffect effect = attack.getAttackEffect(player, null);
        ISkill skill = SkillFactory.getSkill(attack.skill);
        MapleStatEffect effect_ = skill.getEffect(player.getSkillLevel(skill));
        if (effect_.getCooldown() > 0) {
            if (player.skillisCooling(attack.skill)) {
                return;
            } else {
                c.getSession().write(MaplePacketCreator.skillCooldown(attack.skill, effect_.getCooldown()));
                player.addCooldown(attack.skill, System.currentTimeMillis(), effect_.getCooldown() * 1000, TimerManager.getInstance().schedule(new CancelCooldownAction(player, attack.skill), effect_.getCooldown() * 1000));
            }
        }
        applyAttack(attack, player, effect.getAttackCount());
        ISkill eaterSkill = SkillFactory.getSkill((player.getJob().getId() - (player.getJob().getId() % 10)) * 10000);// MP Eater, works with right job
        int eaterLevel = player.getSkillLevel(eaterSkill);
        if (eaterLevel > 0) {
            for (Pair<Integer, List<Integer>> singleDamage : attack.allDamage) {
                eaterSkill.getEffect(eaterLevel).applyPassive(player, player.getMap().getMapObject(singleDamage.getLeft()), 0);
            }
        }
    }
}
