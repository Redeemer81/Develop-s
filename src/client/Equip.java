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

import constants.ExpTable;
import java.util.LinkedList;
import tools.MaplePacketCreator;
import tools.Randomizer;

public class Equip extends Item implements IEquip {
    private byte upgradeSlots;
    private byte level, flag;
    private short str, dex, _int, luk, hp, mp, watk, matk, wdef, mdef, acc, avoid, hands, speed, jump, vicious, potential, pstars, potential_1, potential_2, potential_3;
    private int ringid;
    private int itemExp, itemLevel;

    public Equip(int id, byte position) {
        super(id, position, (short) 1);
        this.itemExp = 0;
        this.itemLevel = 1;
        this.ringid = -1;
    }

    public Equip(int id, byte position, int slots, int ringid) {
        super(id, position, (short) 1);
        this.upgradeSlots = (byte) slots;
        this.itemExp = 0;
        this.itemLevel = 1;
        this.ringid = ringid;
    }

    public Equip(int id, byte position, int ringid) {
        super(id, position, (short) 1);
        this.ringid = ringid;
        this.itemExp = 0;
        this.itemLevel = 1;
    }

    @Override
    public IItem copy() {
        Equip ret = new Equip(getItemId(), getPosition(), getUpgradeSlots(), ringid);
        ret.str = str;
        ret.dex = dex;
        ret._int = _int;
        ret.luk = luk;
        ret.hp = hp;
        ret.mp = mp;
        ret.matk = matk;
        ret.mdef = mdef;
        ret.watk = watk;
        ret.wdef = wdef;
        ret.acc = acc;
        ret.avoid = avoid;
        ret.hands = hands;
        ret.speed = speed;
        ret.jump = jump;
        ret.flag = flag;
        ret.vicious = vicious;
        ret.upgradeSlots = upgradeSlots;
        ret.level = level;
        ret.log = new LinkedList<String>(log);
        ret.setOwner(getOwner());
        ret.setQuantity(getQuantity());
        return ret;
    }

    @Override
    public byte getFlag() {
        return flag;
    }

    @Override
    public byte getType() {
        return IItem.EQUIP;
    }

    public byte getUpgradeSlots() {
        return upgradeSlots;
    }

    public int getRingId() {
        return ringid;
    }

    public short getStr() {
        return str;
    }

    public short getDex() {
        return dex;
    }

    public short getInt() {
        return _int;
    }

    public short getLuk() {
        return luk;
    }

    public short getHp() {
        return hp;
    }

    public short getMp() {
        return mp;
    }

    public short getWatk() {
        return watk;
    }

    public short getMatk() {
        return matk;
    }

    public short getWdef() {
        return wdef;
    }

    public short getMdef() {
        return mdef;
    }

    public short getAcc() {
        return acc;
    }

    public short getAvoid() {
        return avoid;
    }

    public short getHands() {
        return hands;
    }

    public short getSpeed() {
        return speed;
    }

    public short getJump() {
        return jump;
    }

    public short getVicious() {
        return vicious;
    }

    @Override
    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public void setStr(short str) {
        this.str = str;
    }

    public void setDex(short dex) {
        this.dex = dex;
    }

    public void setInt(short _int) {
        this._int = _int;
    }

    public void setLuk(short luk) {
        this.luk = luk;
    }

    public void setHp(short hp) {
        this.hp = hp;
    }

    public void setMp(short mp) {
        this.mp = mp;
    }

    public void setWatk(short watk) {
        this.watk = watk;
    }

    public void setMatk(short matk) {
        this.matk = matk;
    }

    public void setWdef(short wdef) {
        this.wdef = wdef;
    }

    public void setMdef(short mdef) {
        this.mdef = mdef;
    }

    public void setAcc(short acc) {
        this.acc = acc;
    }

    public void setAvoid(short avoid) {
        this.avoid = avoid;
    }

    public void setHands(short hands) {
        this.hands = hands;
    }

    public void setSpeed(short speed) {
        this.speed = speed;
    }

    public void setJump(short jump) {
        this.jump = jump;
    }

    public void setVicious(short vicious) {
        this.vicious = vicious;
    }

    public void setUpgradeSlots(byte upgradeSlots) {
        this.upgradeSlots = upgradeSlots;
    }

    public byte getLevel() {
        return level;
    }

    public void setLevel(byte level) {
        this.level = level;
    }

    public void gainLevel(MapleClient c, boolean timeless) {
        if (level < 6) {
            if (c.getPlayer().getJob().isA(MapleJob.MAGICIAN)) {
                this.matk += Randomizer.getInstance().nextInt(5);
                this._int += Randomizer.getInstance().nextInt(1) + 1;
                this.luk += Randomizer.getInstance().nextInt(1);
            } else {
                this.watk += Randomizer.getInstance().nextInt(3);
            }
        }
        this.level++;
    }

    public int getItemExp() {
        return itemExp;
    }

    public void gainItemExp(MapleClient c, int gain, boolean timeless) {
        itemExp += gain;
        int expNeeded = 0;
        if (timeless) {
            expNeeded = ExpTable.getTimelessItemExpNeededForLevel(itemLevel + 1);
        } else {
            expNeeded = ExpTable.getReverseItemExpNeededForLevel(itemLevel + 1);
        }
        if (itemExp >= expNeeded) {
            gainLevel(c, timeless);
            c.getSession().write(MaplePacketCreator.showItemLevelup());
        }
    }

    public void setItemExp(int itemExp) {
        this.itemExp = itemExp;
        this.itemLevel = Math.max((itemExp - 70) / 10, 0);
    }

    public int getItemLevel() {
        return itemLevel;
    }

    @Override
    public void setQuantity(short quantity) {
        if (quantity < 0 || quantity > 1) {
            throw new RuntimeException("Setting the quantity to " + quantity + " on an equip (itemid: " + getItemId() + ")");
        }
        super.setQuantity(quantity);
    }

    public void setUpgradeSlots(int i) {
        this.upgradeSlots = (byte) i;
    }

    public void setVicious(int i) {
        this.vicious = (short) i;
    }
    
    public short getPotential() {
        return potential;
    }
    
    public short getPStars() {
        return pstars;
    }
    
    public short getPotential_1() {
        return potential_1;
    }
    
    public short getPotential_2() {
        return potential_2;
    }
    
    public short getPotential_3() {
        return potential_3;
    }
    
    public void setPotential(int i) {
        potential = (short) i;
    }
    
    public void setPStars(int i) {
        pstars = (short) i;
    }
    
    public void setPotential_1(int i) {
        potential_1 = (short) i;
    }
    
    public void setPotential_2(int i) {
        potential_2 = (short) i;
    }
    
    public void setPotential_3(int i) {
        potential_3 = (short) i;
    }
}