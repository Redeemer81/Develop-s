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
package server.maps;

import java.awt.Point;
import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import tools.MaplePacketCreator;

/**
 *
 * @author Jan
 */
public class MapleSummon extends AbstractAnimatedMapleMapObject {
    private MapleCharacter owner;
    private int skillLevel;
    private int skill;
    private int hp;
    private int duration;
    private SummonMovementType movementType;

    public MapleSummon(MapleCharacter owner, int skill, int duration, Point pos, SummonMovementType movementType) {
        this.owner = owner;
        this.skill = skill;
        this.duration = duration;
        this.skillLevel = owner.getSkillLevel(SkillFactory.getSkill(skill));
        if (skillLevel == 0) {
            throw new RuntimeException();
        }
        this.movementType = movementType;
        setPosition(pos);
    }

    public void sendSpawnData(MapleClient client) {
        if (this != null) {
            client.getSession().write(MaplePacketCreator.spawnSpecialMapObject(this, skillLevel, false));
        }
    }

    public void sendDestroyData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.removeSpecialMapObject(this, true));
    }

    public MapleCharacter getOwner() {
        return owner;
    }

    public int getSkill() {
        return skill;
    }

    public int getHP() {
        return hp;
    }

    public void addHP(int delta) {
        this.hp += delta;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public SummonMovementType getMovementType() {
        return movementType;
    }

    public boolean isStationary() {
        return (skill == 3111002 || skill == 3211002 || skill == 5211001 || skill == 13111004);
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public int getDuration() {
        return skillLevel;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.SUMMON;
    }
}