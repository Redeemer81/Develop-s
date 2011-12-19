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
package client;

import java.util.ArrayList;
import java.util.List;

import server.MapleStatEffect;
import server.life.Element;

public class Skill implements ISkill {
    public int id;
    public List<MapleStatEffect> effects = new ArrayList<MapleStatEffect>();
    public Element element;
    public int animationTime;
    public int masterLevel;
    public int maxLevel;
    public boolean isBuff;
    public boolean isInvisible;
    public boolean isCommon;

    public Skill(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public MapleStatEffect getEffect(int level) {
        return effects.get(level - 1);
    }

    @Override
    public int getMaxLevel() {
        return effects.size();
    }

    @Override
    public boolean isFourthJob() {
        return (id / 10000) % 10 == 2;
    }

    @Override
    public Element getElement() {
        return element;
    }

    @Override
    public int getAnimationTime() {
        return animationTime;
    }

    @Override
    public boolean isBeginnerSkill() {
        return id % 10000000 < 10000;
    }

    public int getMasterLevel()
    {
        return masterLevel;
    }

    public void setIsBuff(boolean isBuff)
    {
        this.isBuff = isBuff;
    }
    
    public boolean isBuff()
    {
        return isBuff;
    }
    public void setIsInvisible(boolean isInvisible)
    {
        this.isInvisible = isInvisible;
    }

    public boolean isInvisible()
    {
        return isInvisible;
    }

    @Override
    public boolean hasMastery() {
        /*if (masterLevel > 0) {
            return true;
        }
        final int jobId = id / 10000;
        if (jobId == 0 || jobId == 2000 || jobId == 2001)
            return false;
        if (jobId < 430) {
            if (jobId % 10 == 2) {
                return true;
            }
        } else if (jobId > 440 && jobId < 2200) {
            if (jobId % 10 == 2) {
                return true;
            }
        } else if (jobId == 434) {
            return true;
        } else if (id == 22170001 || id == 22171003 || id == 22171004 || id == 22181002 || id == 22181003)  {
            return true;
        }
        return false;*/
        if (id == 1120012 || id == 1220013 || id == 1320011 || id == 2121009 || id == 2221009 || id == 2321010 || id == 3120011 || id == 3220009 || id == 4220009 || id == 5120011 || id == 5220012)
            return false;
        if (id / 10000 >= 2212 && id / 10000 < 3000) { //evan skill
                return ((id / 10000) % 10) >= 7;
        }
        if (id / 10000 >= 430 && id / 10000 <= 434) { //db skill
                return ((id / 10000) % 10) == 4;
        }
        return ((id / 10000) % 10) == 2;
    }
    
}