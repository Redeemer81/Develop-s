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
package server;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import client.IItem;
import client.ISkill;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleInventory;
import client.MapleInventoryType;
import client.MapleJob;
import client.MapleMount;
import client.MapleStat;
import client.SkillFactory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.InventoryConstants;
import constants.skills.*;
import constants.ServerConstants;
import net.channel.ChannelServer;
import provider.MapleData;
import provider.MapleDataTool;
import server.life.MapleMonster;
import server.maps.MapleDoor;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import server.maps.MapleSummon;
import server.maps.SummonMovementType;
import net.world.PlayerCoolDownValueHolder;
import tools.ArrayMap;
import tools.MaplePacketCreator;
import tools.Pair;
import constants.skills.*;
import net.MaplePacket;
import server.maps.FieldLimit;
import tools.StringCalculator;

/**
 * @author Matze
 * @author Frz
 */
public class MapleStatEffect implements Serializable {
    private static final long serialVersionUID = 3692756402846632237L;
    private short watk, matk, wdef, mdef, acc, avoid, speed, jump;
    private short hp, mp, mhpR, mmpR;
    private double hpR, mpR;
    private short mpCon, hpCon;
    private int duration;
    private boolean overTime;
    private int sourceid;
    private int moveTo;
    private boolean skill;
    private List<Pair<MapleBuffStat, Integer>> statups;
    private Map<MonsterStatus, Integer> monsterStatus;
    private int x, y;
    private double prop;
    private int itemCon, itemConNo;
    private int damage, attackCount, bulletCount, bulletConsume;
    private Point lt, rb;
    private int mobCount;
    private int moneyCon;
    private int cooldown;
    private int morphId = 0;
    private boolean isGhost;
    private int fatigue;
    private int mastery;
    private int iprop;
    private short str;
    private short epad;

    public static MapleStatEffect loadSkillEffectFromData(MapleData source, int skillid, int skillLevel, boolean overtime) {
        return loadFromData(source, skillid, skillLevel, true, overtime);
    }
    
    public static MapleStatEffect loadSkillEffectFromData(MapleData source, int skillid, boolean overtime) {
        return loadFromData(source, skillid, 0, true, overtime);
    }

    public static MapleStatEffect loadItemEffectFromData(MapleData source, int itemid) {
        return loadFromData(source, itemid, 0, false, false);
    }

    private static void addBuffStatPairToListIfNotZero(List<Pair<MapleBuffStat, Integer>> list, MapleBuffStat buffstat, Integer val) {
        if (val.intValue() != 0) {
            list.add(new Pair<MapleBuffStat, Integer>(buffstat, val));
        }
    }

    private static MapleStatEffect loadFromData(MapleData source, int sourceid, int level, boolean skill, boolean overTime) {
        MapleStatEffect ret = new MapleStatEffect();
        if (level == 0) {
            ret.duration = MapleDataTool.getIntConvert("time", source, -1);
            ret.mastery = MapleDataTool.getIntConvert("mastery", source, -1);
            ret.hp = (short) MapleDataTool.getInt("hp", source, 0);
            ret.hpR = MapleDataTool.getInt("hpR", source, 0) / 100.0;
            ret.mp = (short) MapleDataTool.getInt("mp", source, 0);
            ret.mpR = MapleDataTool.getInt("mpR", source, 0) / 100.0;
            ret.mpCon = (short) MapleDataTool.getInt("mpCon", source, 0);
            ret.hpCon = (short) MapleDataTool.getInt("hpCon", source, 0);
            ret.iprop = MapleDataTool.getInt("prop", source, 100);
            ret.prop = ret.iprop / 100.0;
            ret.mobCount = MapleDataTool.getInt("mobCount", source, 1);
            ret.cooldown = MapleDataTool.getInt("cooltime", source, 0);
            ret.morphId = MapleDataTool.getInt("morph", source, 0);
            ret.isGhost = MapleDataTool.getInt("ghost", source, 0) != 0;
            ret.fatigue = MapleDataTool.getInt("incFatigue", source, 0);
            ret.sourceid = sourceid;
            ret.skill = skill;
            ret.watk = (short) MapleDataTool.getInt("pad", source, 0);
            ret.wdef = (short) MapleDataTool.getInt("pdd", source, 0);
            ret.matk = (short) MapleDataTool.getInt("mad", source, 0);
            ret.mdef = (short) MapleDataTool.getInt("mdd", source, 0);
            ret.acc = (short) MapleDataTool.getIntConvert("acc", source, 0);
            ret.avoid = (short) MapleDataTool.getInt("eva", source, 0);
            ret.speed = (short) MapleDataTool.getInt("speed", source, 0);
            ret.jump = (short) MapleDataTool.getInt("jump", source, 0);
            MapleData ltd = source.getChildByPath("lt");
            if (ltd != null) {
                ret.lt = (Point) ltd.getData();
                ret.rb = (Point) source.getChildByPath("rb").getData();
            }
            int x = MapleDataTool.getInt("x", source, 0);
            ret.x = x;
            ret.y = MapleDataTool.getInt("y", source, 0);
            ret.damage = MapleDataTool.getIntConvert("damage", source, 100);
            ret.attackCount = MapleDataTool.getIntConvert("attackCount", source, 1);
            ret.bulletCount = MapleDataTool.getIntConvert("bulletCount", source, 1);
            ret.bulletConsume = MapleDataTool.getIntConvert("bulletConsume", source, 0);
            ret.moneyCon = MapleDataTool.getIntConvert("moneyCon", source, 0);
            ret.itemCon = MapleDataTool.getInt("itemCon", source, 0);
            ret.itemConNo = MapleDataTool.getInt("itemConNo", source, 0);
            ret.moveTo = MapleDataTool.getInt("moveTo", source, -1);
        } else {
            try {
                ret.duration = getArgument(MapleDataTool.getString("time", source, "-1"), level);
                ret.mastery = getArgument(MapleDataTool.getString("mastery", source, "-1"), level);
                ret.hp = (short) getArgument(MapleDataTool.getString("hp", source, "0"), level);
                ret.hpR = getArgument(MapleDataTool.getString("hpR", source, "0"), level) / 100.0;
                ret.mp = (short) getArgument(MapleDataTool.getString("mp", source, "0"), level);
                ret.mpR = getArgument(MapleDataTool.getString("mpR", source, "0"), level) / 100.0;
                ret.mpCon = (short) getArgument(MapleDataTool.getString("mpCon", source, "0"), level);
                ret.hpCon = (short) getArgument(MapleDataTool.getString("hpCon", source, "0"), level);
                ret.iprop = getArgument(MapleDataTool.getString("prop", source, "100"), level);
                ret.prop = ret.iprop / 100.0;
                ret.mobCount = getArgument(MapleDataTool.getString("mobCount", source, "1"), level);
                ret.cooldown = getArgument(MapleDataTool.getString("cooltime", source, "0"), level);
                ret.morphId = getArgument(MapleDataTool.getString("morph", source, "0"), level);
                ret.isGhost = getArgument(MapleDataTool.getString("ghost", source, "0"), level) != 0;
                ret.fatigue = getArgument(MapleDataTool.getString("incFatigue", source, "0"), level);
                ret.sourceid = sourceid;
                ret.skill = skill;
                ret.str = (short) getArgument(MapleDataTool.getString("str", source, "0"), level);
                ret.watk = (short) getArgument(MapleDataTool.getString("pad", source, "0"), level);
                ret.wdef = (short) getArgument(MapleDataTool.getString("pdd", source, "0"), level);
                ret.matk = (short) getArgument(MapleDataTool.getString("mad", source, "0"), level);
                ret.mdef = (short) getArgument(MapleDataTool.getString("mdd", source, "0"), level);
                ret.acc = (short) getArgument(MapleDataTool.getString("acc", source, "0"), level);
                ret.avoid = (short) getArgument(MapleDataTool.getString("eva", source, "0"), level);
                ret.speed = (short) getArgument(MapleDataTool.getString("speed", source, "0"), level);
                ret.jump = (short) getArgument(MapleDataTool.getString("jump", source, "0"), level);
                MapleData ltd = source.getChildByPath("lt");
                if (ltd != null) {
                    ret.lt = (Point) ltd.getData();
                    ret.rb = (Point) source.getChildByPath("rb").getData();
                }
                ret.epad = (short) getArgument(MapleDataTool.getString("epad", source, "0"), level);
                ret.x = getArgument(MapleDataTool.getString("x", source, "0"), level);
                ret.y = getArgument(MapleDataTool.getString("y", source, "0"), level);
                ret.damage = getArgument(MapleDataTool.getString("damage", source, "100"), level);
                ret.attackCount = getArgument(MapleDataTool.getString("attackCount", source, "1"), level);
                ret.bulletCount = getArgument(MapleDataTool.getString("bulletCount", source, "1"), level);
                ret.bulletConsume = getArgument(MapleDataTool.getString("bulletConsume", source, "0"), level);
                ret.moneyCon = getArgument(MapleDataTool.getString("moneyCon", source, "0"), level);
                ret.itemCon = getArgument(MapleDataTool.getString("itemCon", source, "0"), level);
                ret.itemConNo = getArgument(MapleDataTool.getString("itemConNo", source, "0"), level);
                ret.moveTo = getArgument(MapleDataTool.getString("moveTo", source, "-1"), level);
            } catch (Exception e) {
                
            }
        }
        if (!ret.skill && ret.duration > -1) {
            ret.overTime = true;
        } else {
            ret.duration *= 1000; // items have their times stored in ms, of course
            ret.overTime = overTime;
        }
        ArrayList<Pair<MapleBuffStat, Integer>> statups = new ArrayList<Pair<MapleBuffStat, Integer>>();
        if (ret.overTime && ret.getSummonMovementType() == null) {
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.WATK, Integer.valueOf(ret.watk));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.WDEF, Integer.valueOf(ret.wdef));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MATK, Integer.valueOf(ret.matk));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MDEF, Integer.valueOf(ret.mdef));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.ACC, Integer.valueOf(ret.acc));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.AVOID, Integer.valueOf(ret.avoid));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.SPEED, Integer.valueOf(ret.speed));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.JUMP, Integer.valueOf(ret.jump));
        }
        Map<MonsterStatus, Integer> monsterStatus = new ArrayMap<MonsterStatus, Integer>();
        if (skill) {
            switch (sourceid) {
                // BEGINNER
                case Beginner.RECOVERY:
                case Noblesse.RECOVERY:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.RECOVERY, Integer.valueOf(ret.x)));
                    break;
                case Beginner.ECHO_OF_HERO:
                case Noblesse.ECHO_OF_HERO:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ECHO_OF_HERO, Integer.valueOf(ret.x)));
                    break;
                case Beginner.MONSTER_RIDER:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MONSTER_RIDING, Integer.valueOf(1)));
                    break;
                case Beginner.BERSERK_FURY:
                case Noblesse.BERSERK_FURY:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BERSERK_FURY, Integer.valueOf(1)));
                    break;
                case Beginner.INVINCIBLE_BARRIER:
                case Noblesse.INVINCIBLE_BARRIER:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DIVINE_BODY, Integer.valueOf(1)));
                    break;
                case Fighter.POWER_GUARD:
                case Page.POWER_GUARD:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.POWERGUARD, Integer.valueOf(ret.x)));
                    break;
                case Spearman.HYPER_BODY:
                case GM.HYPER_BODY:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HYPERBODYHP, Integer.valueOf(ret.x)));
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HYPERBODYMP, Integer.valueOf(ret.y)));
                    break;
                case Crusader.COMBO:
                case DawnWarrior.COMBO:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, Integer.valueOf(1)));
                    break;
                case WhiteKnight.BW_FIRE_CHARGE:
                case WhiteKnight.BW_ICE_CHARGE:
                case WhiteKnight.BW_LIT_CHARGE:
                case WhiteKnight.SWORD_FIRE_CHARGE:
                case WhiteKnight.SWORD_ICE_CHARGE:
                case WhiteKnight.SWORD_LIT_CHARGE:
                case Paladin.BW_HOLY_CHARGE:
                case Paladin.SWORD_HOLY_CHARGE:
                case DawnWarrior.SOUL_CHARGE:
                case ThunderBreaker.LIGHTNING_CHARGE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.WK_CHARGE, Integer.valueOf(ret.x)));
                    break;
                case Aran.SNOW_CHARGE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.WK_CHARGE, Integer.valueOf(ret.x)));
                    monsterStatus.put(MonsterStatus.SPEED, Integer.valueOf(ret.x));
                    break;
                case DragonKnight.DRAGON_STRANGS:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DRAGON_STRANS, Integer.valueOf(ret.str)));
                    break;
                case DragonKnight.DRAGON_ROAR:
                    ret.hpR = -ret.x / 100.0;
                    break;
                case Hero.STANCE:
                case Paladin.STANCE:
                case DarkKnight.STANCE:
                case Aran.FREEZE_STANDING:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.STANCE, Integer.valueOf(ret.iprop)));
                    break;
                case DawnWarrior.FINAL_ATTACK:
                case WindArcher.FINAL_ATTACK:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.FINALATTACK, Integer.valueOf(ret.x)));
                    break;
                case WhiteKnight.COMBAT_ORDERS:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBAT_ORDERS, Integer.valueOf(ret.x)));
                    break;
                // MAGICIAN
                case Magician.MAGIC_GUARD:
                case BlazeWizard.MAGIC_GUARD:
                case Evan.MAGIC_GUARD:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAGIC_GUARD, Integer.valueOf(ret.x)));
                    break;
                case Cleric.INVINCIBLE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.INVINCIBLE, Integer.valueOf(ret.x)));
                    break;
                case Priest.HOLY_SYMBOL:
                case GM.HOLY_SYMBOL:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HOLY_SYMBOL, Integer.valueOf(ret.x)));
                    break;
                case Cleric.BLESS:
                case GM.BLESS:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BLESS, Integer.valueOf(ret.x)));
                    break;
                case FPArchMage.INFINITY:
                case ILArchMage.INFINITY:
                case Bishop.INFINITY:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.INFINITY, Integer.valueOf(ret.x)));
                    break;
                case FPArchMage.MANA_REFLECTION:
                case ILArchMage.MANA_REFLECTION:
                case Bishop.MANA_REFLECTION:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MANA_REFLECTION, Integer.valueOf(1)));
                    break;
                case Bishop.HOLY_SHIELD:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HOLY_SHIELD, Integer.valueOf(ret.x)));
                    break;

                //EVAN
                case Evan.MAGIC_RESISTANCE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAGIC_RESISTANCE, Integer.valueOf(ret.x)));
                case Evan.ELEMENTAL_RESET:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.ELEMENTAL_RESET, Integer.valueOf(ret.x)));
                case Evan.MAGIC_SHIELD:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAGIC_SHIELD, Integer.valueOf(ret.x)));
                // BOWMAN
                case Priest.MYSTIC_DOOR:
                case Hunter.SOUL_ARROW:
                case Crossbowman.SOUL_ARROW:
                case WindArcher.SOUL_ARROW:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SOULARROW, Integer.valueOf(ret.x)));
                    break;
                case Ranger.PUPPET:
                case Sniper.PUPPET:
                case WindArcher.PUPPET:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.PUPPET, Integer.valueOf(1)));
                    break;
                case Ranger.CONCENTRATE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.CONCENTRATE, Integer.valueOf(ret.epad)));
                    break;
                case Bowmaster.HAMSTRING:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HAMSTRING, Integer.valueOf(ret.x)));
                    monsterStatus.put(MonsterStatus.SPEED, ret.x);
                    break;
                case Marksman.BLIND:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BLIND, Integer.valueOf(ret.x)));
                    monsterStatus.put(MonsterStatus.ACC, ret.x);
                    break;
                case Bowmaster.SHARP_EYES:
                case Marksman.SHARP_EYES:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SHARP_EYES, Integer.valueOf(ret.x << 8 | ret.y)));
                    break;
                // THIEF
                case Rogue.DARK_SIGHT:
                case WindArcher.WIND_WALK:
                case NightWalker.DARK_SIGHT:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARKSIGHT, Integer.valueOf(ret.x)));
                    break;
                case Hermit.MESO_UP:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MESOUP, Integer.valueOf(ret.x)));
                    break;
                case Hermit.SHADOW_PARTNER:
                case NightWalker.SHADOW_PARTNER:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SHADOWPARTNER, Integer.valueOf(ret.x)));
                    break;
                case ChiefBandit.MESO_GUARD:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MESOGUARD, Integer.valueOf(ret.x)));
                    break;
                case ChiefBandit.PICKPOCKET:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.PICKPOCKET, Integer.valueOf(ret.x)));
                    break;
                case NightLord.SHADOW_STARS:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SHADOW_CLAW, Integer.valueOf(0)));
                    break;
                    //DUAL BLADE
                case BladeLord.OWL_SPIRIT:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.OWL_SPIRIT, Integer.valueOf(ret.x)));
                    break;
                case BladeMaster.THORNS:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.THORNS, Integer.valueOf(ret.x << 8 | ret.y)));
                    break;
                case BladeMaster.FINAL_CUT:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.FINAL_CUT, Integer.valueOf(ret.y)));
                    break;
                case BladeLord.MIRROR_IMAGE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MIRROR_IMAGE, Integer.valueOf(ret.x)));
                    break;
//                // PIRATE
                case Pirate.DASH:
                case ThunderBreaker.DASH:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DASH, Integer.valueOf(ret.x)));
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DASH2, Integer.valueOf(ret.y)));
                    break;
                case Corsair.SPEED_INFUSION:
                case Buccaneer.SPEED_INFUSION:
                case ThunderBreaker.SPEED_INFUSION:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SPEED_INFUSION, Integer.valueOf(ret.x)));
                    break;
                case Outlaw.HOMING_BEACON:
                case Corsair.BULLSEYE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.HOMING_BEACON, Integer.valueOf(ret.x)));
                    break;
             /*   case Corsair.BATTLE_SHIP:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MONSTER_RIDING, Integer.valueOf(sourceid)));
                    break;*/
                // GM
                case GM.HIDE:
                    ret.duration = 7200000;
                    ret.overTime = true;
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARKSIGHT, Integer.valueOf(ret.x)));
                    break;

                //ARAN
                case Aran.COMBO_BARRIER:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO_BARRIER, Integer.valueOf(ret.x)));
                    break;
                case Aran.COMBO_DRAIN:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO_DRAIN, Integer.valueOf(ret.x)));
                    break;
                case Aran.SMART_KNOCKBACK:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SMART_KNOCKBACK, Integer.valueOf(ret.x)));
                    break;
                case Aran.BODY_PRESSURE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BODY_PRESSURE, Integer.valueOf(ret.x)));
                    break;
                // MULTIPLE
                case Fighter.AXE_BOOSTER:
                case Fighter.SWORD_BOOSTER:
                case Page.BW_BOOSTER:
                case Page.SWORD_BOOSTER:
                case Spearman.POLEARM_BOOSTER:
                case Spearman.SPEAR_BOOSTER:
                case Hunter.BOW_BOOSTER:
                case Crossbowman.CROSSBOW_BOOSTER:
                case Assassin.CLAW_BOOSTER:
                case Bandit.DAGGER_BOOSTER:
                case FPMage.SPELL_BOOSTER:
                case ILMage.SPELL_BOOSTER:
                case Brawler.KNUCKLER_BOOSTER:
                case Gunslinger.GUN_BOOSTER:
                case DawnWarrior.SWORD_BOOSTER:
                case BlazeWizard.SPELL_BOOSTER:
                case WindArcher.BOW_BOOSTER:
                case NightWalker.CLAW_BOOSTER:
                case ThunderBreaker.KNUCKLER_BOOSTER:
                case Aran.ARAN_POLEARM_BOOSTER:
                case BladeRecruit.KATARA_BOOSTER:
                case Evan.MAGIC_BOOSTER:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.BOOSTER, Integer.valueOf(ret.x)));
                    break;
                case Hero.MAPLE_WARRIOR:
                case Paladin.MAPLE_WARRIOR:
                case DarkKnight.MAPLE_WARRIOR:
                case FPArchMage.MAPLE_WARRIOR:
                case ILArchMage.MAPLE_WARRIOR:
                case Bishop.MAPLE_WARRIOR:
                case Bowmaster.MAPLE_WARRIOR:
                case Marksman.MAPLE_WARRIOR:
                case NightLord.MAPLE_WARRIOR:
                case Shadower.MAPLE_WARRIOR:
                case Corsair.MAPLE_WARRIOR:
                case Buccaneer.MAPLE_WARRIOR:
                case Aran.ARAN_MAPLE_WARRIOR:
                case Evan.MAPLE_WARRIOR:
                case BladeMaster.MAPLE_WARRIOR:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MAPLE_WARRIOR, Integer.valueOf(ret.x)));
                    break;
                // SUMMON
                case Ranger.SILVER_HAWK:
                case Sniper.GOLDEN_EAGLE:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SUMMON, Integer.valueOf(1)));
                    monsterStatus.put(MonsterStatus.STUN, Integer.valueOf(1));
                    break;
                case FPArchMage.ELQUINES:
                case Marksman.FROST_PREY:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SUMMON, Integer.valueOf(1)));
                    monsterStatus.put(MonsterStatus.FREEZE, Integer.valueOf(1));
                    break;
                case Priest.SUMMON_DRAGON:
                case Bowmaster.PHOENIX:
                case ILArchMage.IFRIT:
                case Bishop.BAHAMUT:
                case DarkKnight.BEHOLDER:
                case Outlaw.OCTOPUS:
                case Corsair.WRATH_OF_THE_OCTOPI:
                case Outlaw.GAVIOTA:
                case DawnWarrior.SOUL:
                case BlazeWizard.FLAME:
                case WindArcher.STORM:
                case NightWalker.DARKNESS:
                case ThunderBreaker.LIGHTNING:
                case BlazeWizard.IFRIT:
                case BladeMaster.MIRRORED_TARGET:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SUMMON, Integer.valueOf(1)));
                    break;
                case FPMage.TELEPORT_MASTERY:
                case ILMage.TELEPORT_MASTERY:
                case Priest.TELEPORT_MASTERY:
                    statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.TELEPORT_MASTERY, Integer.valueOf(-1)));
                    break;
                // ----------------------------- MONSTER STATUS ---------------------------------- //
                case Rogue.DISORDER:
                    monsterStatus.put(MonsterStatus.WATK, Integer.valueOf(ret.x));
                    monsterStatus.put(MonsterStatus.WDEF, Integer.valueOf(ret.y));
                    break;
                case Corsair.HYPNOTIZE:
                    monsterStatus.put(MonsterStatus.INERTMOB, 1);
                    break;
                case NightLord.NINJA_AMBUSH:
                case Shadower.NINJA_AMBUSH:
                    monsterStatus.put(MonsterStatus.NINJA_AMBUSH, Integer.valueOf(1));
                    break;
                case Page.THREATEN:
                    monsterStatus.put(MonsterStatus.WATK, Integer.valueOf(ret.x));
                    monsterStatus.put(MonsterStatus.WDEF, Integer.valueOf(ret.y));
                    break;
                case Crusader.AXE_COMA:
                case Crusader.SWORD_COMA:
                case Crusader.SHOUT:
                case WhiteKnight.CHARGE_BLOW:
                case Hunter.ARROW_BOMB:
                case ChiefBandit.ASSAULTER:
                case Shadower.BOOMERANG_STEP:
                case Brawler.BACK_SPIN_BLOW:
                case Brawler.DOUBLE_UPPERCUT:
                case Buccaneer.DEMOLITION:
                case Buccaneer.SNATCH:
                case Buccaneer.BARRAGE:
                case Gunslinger.BLANK_SHOT:
                case DawnWarrior.COMA:
                case Evan.FIRE_BREATH:
                    monsterStatus.put(MonsterStatus.STUN, Integer.valueOf(1));
                    break;
                case NightLord.TAUNT:
                case Shadower.TAUNT:
                    monsterStatus.put(MonsterStatus.SHOWDOWN, Integer.valueOf(1));
                    break;
                case ILWizard.COLD_BEAM:
                case ILMage.ICE_STRIKE:
                case ILArchMage.BLIZZARD:
                case ILMage.ELEMENT_COMPOSITION:
                case Sniper.BLIZZARD:
                case Outlaw.ICE_SPLITTER:
                case FPArchMage.PARALYZE:
                case Evan.ICE_BREATH:
                    monsterStatus.put(MonsterStatus.FREEZE, Integer.valueOf(1));
                    ret.duration *= 2; // freezing skills are a little strange
                    break;
                case FPWizard.SLOW:
                case ILWizard.SLOW:
                case BlazeWizard.SLOW:
                case Evan.SLOW:
                    monsterStatus.put(MonsterStatus.SPEED, Integer.valueOf(ret.x));
                    break;
                case FPWizard.POISON_BREATH:
                case FPMage.ELEMENT_COMPOSITION:
                    monsterStatus.put(MonsterStatus.POISON, Integer.valueOf(1));
                    break;
                case Priest.DOOM:
                    monsterStatus.put(MonsterStatus.DOOM, Integer.valueOf(1));
                    break;
                case ILMage.SEAL:
                case FPMage.SEAL:
                    monsterStatus.put(MonsterStatus.SEAL, 1);
                    break;
                case Hermit.SHADOW_WEB: // shadow web
                case NightWalker.SHADOW_WEB:
                    monsterStatus.put(MonsterStatus.SHADOW_WEB, 1);
                    break;
                case FPArchMage.FIRE_DEMON:
                case ILArchMage.ICE_DEMON:
                    monsterStatus.put(MonsterStatus.POISON, Integer.valueOf(1));
                    monsterStatus.put(MonsterStatus.FREEZE, Integer.valueOf(1));
                    break;
                default:
                    break;
            }
        }
        if(ret.isNonItemMountSkill()) {
                statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MONSTER_RIDING, Integer.valueOf(sourceid)));
        }
        if (ret.isMorph()) {
            statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MORPH, Integer.valueOf(ret.getMorph())));
        }
        if (ret.isGhost && !skill) {
            statups.add(new Pair<MapleBuffStat, Integer>(MapleBuffStat.GHOST_MORPH, Integer.valueOf(1)));
        }
        ret.monsterStatus = monsterStatus;
        statups.trimToSize();
        ret.statups = statups;
        return ret;
    }

    /**
     * @param applyto
     * @param obj
     * @param attack damage done by the skill
     */
    public void applyPassive(MapleCharacter applyto, MapleMapObject obj, int attack) {
        if (makeChanceResult()) {
            switch (sourceid) { // MP eater
                case FPWizard.MP_EATER:
                case ILWizard.MP_EATER:
                case Cleric.MP_EATER:
                    if (obj == null || obj.getType() != MapleMapObjectType.MONSTER) {
                        return;
                    }
                    MapleMonster mob = (MapleMonster) obj; // x is absorb percentage
                    if (!mob.isBoss()) {
                        int absorbMp = Math.min((int) (mob.getMaxMp() * (getX() / 100.0)), mob.getMp());
                        if (absorbMp > 0) {
                            mob.setMp(mob.getMp() - absorbMp);
                            applyto.addMP(absorbMp);
                            applyto.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(sourceid, 1));
                            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showBuffeffect(applyto, applyto.getId(), sourceid, 1), false);
                        }
                    }
                    break;
            }
        }
    }

    public boolean applyTo(MapleCharacter chr) {
        return applyTo(chr, chr, true, null);
    }

    public boolean applyTo(MapleCharacter chr, Point pos) {
        return applyTo(chr, chr, true, pos);
    }

    private boolean applyTo(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary, Point pos) {
        int hpchange = calcHPChange(applyfrom, primary);
        int mpchange = calcMPChange(applyfrom, primary);
        if (primary) {
            if (itemConNo != 0) {
                MapleInventoryManipulator.removeById(applyto.getClient(), MapleItemInformationProvider.getInstance().getInventoryType(itemCon), itemCon, itemConNo, false, true);
            }
        }
        List<Pair<MapleStat, Integer>> hpmpupdate = new ArrayList<Pair<MapleStat, Integer>>(2);
        if (!primary && isResurrection()) {
            hpchange = applyto.getMaxHp();
            applyto.setStance(0); //TODO fix death bug, player doesnt spawn on other screen
        }
        if(isDrain())
            applyto.setCombo(0);
        if (isDispel() && makeChanceResult()) {
            applyto.dispelDebuffs();
        } else if (isHeroWill()) {
            applyto.dispelSeduce();
        }
        if (hpchange != 0) {
            if (hpchange < 0 && -hpchange > applyto.getHp()) {
                return false;
            }
            int newHp = applyto.getHp() + hpchange;
            if (newHp < 1) {
                newHp = 1;
            }
            applyto.setHp(newHp);
            hpmpupdate.add(new Pair<MapleStat, Integer>(MapleStat.HP, Integer.valueOf(applyto.getHp())));
        }
        if (mpchange != 0) {
            if (mpchange < 0 && -mpchange > applyto.getMp()) {
                return false;
            }
            applyto.setMp(applyto.getMp() + mpchange);
            hpmpupdate.add(new Pair<MapleStat, Integer>(MapleStat.MP, Integer.valueOf(applyto.getMp())));
        }
        applyto.getClient().getSession().write(MaplePacketCreator.updatePlayerStats(hpmpupdate, true));
        if (moveTo != -1) {
            if (applyto.getMap().getReturnMapId() != applyto.getMapId()) {
                MapleMap target;
                if (moveTo == 999999999) {
                    target = applyto.getMap().getReturnMap();
                } else {
                    target = ChannelServer.getInstance(applyto.getClient().getChannel()).getMapFactory().getMap(moveTo);
                    int targetid = target.getId() / 10000000;
                    if (targetid != 60 && applyto.getMapId() / 10000000 != 61 && targetid != applyto.getMapId() / 10000000 && targetid != 21 && targetid != 20) {
                        return false;
                    }
                }
                applyto.changeMap(target);
            } else {
                return false;
            }
        }
        if (isShadowClaw()) {
            int projectile = 0;
            MapleInventory use = applyto.getInventory(MapleInventoryType.USE);
            for (int i = 0; i < 97; i++) { // impose order...
                IItem item = use.getItem((byte) i);
                if (item != null) {
                    if (InventoryConstants.isThrowingStar(item.getItemId()) && item.getQuantity() >= 200) {
                        projectile = item.getItemId();
                        break;
                    }
                }
            }
            if (projectile == 0) {
                return false;
            } else {
                MapleInventoryManipulator.removeById(applyto.getClient(), MapleInventoryType.USE, projectile, 200, false, true);
            }
        }
        if (overTime || isCygnusFA()) {
            applyBuffEffect(applyfrom, applyto, primary);
        }
        if (primary && (overTime || isHeal())) {
            applyBuff(applyfrom);
        }
        if (primary && isMonsterBuff()) {
            applyMonsterBuff(applyfrom);
        }
        if (this.getFatigue() != 0) {
            applyto.getMount().setTiredness(applyto.getMount().getTiredness() + this.getFatigue());
        }
        SummonMovementType summonMovementType = getSummonMovementType();
        if (summonMovementType != null && pos != null) {
            final MapleSummon tosummon = new MapleSummon(applyfrom, sourceid, duration, pos, summonMovementType);
            applyfrom.getMap().spawnSummon(tosummon);
            applyfrom.addSummon(sourceid, tosummon);
            tosummon.addHP(x);
            if (isBeholder()) {
                tosummon.addHP(1);
            }
        }
        if (isMagicDoor() && !FieldLimit.DOOR.check(applyto.getMap().getFieldLimit())) { // Magic Door
            Point doorPosition = new Point(applyto.getPosition());
            MapleDoor door = new MapleDoor(applyto, doorPosition);
            applyto.getMap().spawnDoor(door);
            applyto.addDoor(door);
            door = new MapleDoor(door);
            applyto.addDoor(door);
            door.getTown().spawnDoor(door);
            if (applyto.getParty() != null) {// update town doors
                applyto.silentPartyUpdate();
            }
            applyto.disableDoor();
        } else if (isMist() && sourceid != NightWalker.POISON_BOMB) {
            applyfrom.getMap().spawnMist(new MapleMist(calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft()), applyfrom, this), getDuration(), sourceid != Shadower.SMOKE_SCREEN, false);
        } else if (isTimeLeap()) { // Time Leap
            for (PlayerCoolDownValueHolder i : applyto.getAllCooldowns()) {
                if (i.skillId != Buccaneer.TIME_LEAP) {
                    applyto.removeCooldown(i.skillId);
                }
            }
        }
        return true;
    }

    private void applyBuff(MapleCharacter applyfrom) {
        if ((isPartyBuff() && (applyfrom.getParty() != null || isGmBuff())) || ((sourceid == Bowmaster.SHARP_EYES || sourceid == Marksman.SHARP_EYES) && (applyfrom.isGM()))) {
            Rectangle bounds = calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft());
            List<MapleMapObject> affecteds = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.PLAYER));
            List<MapleCharacter> affectedp = new ArrayList<MapleCharacter>(affecteds.size());
            for (MapleMapObject affectedmo : affecteds) {
                MapleCharacter affected = (MapleCharacter) affectedmo;
                if (affected != applyfrom && (isGmBuff() || applyfrom.getParty().equals(affected.getParty()) || ((sourceid == Bowmaster.SHARP_EYES || sourceid == Marksman.SHARP_EYES) && (applyfrom.isGM())))) {
                    if ((isResurrection() && !affected.isAlive()) || (!isResurrection() && affected.isAlive())) {
                        affectedp.add(affected);
                    }
                    if (isTimeLeap()) {
                        for (PlayerCoolDownValueHolder i : affected.getAllCooldowns()) {
                            affected.removeCooldown(i.skillId);
                        }
                    }
                }
            }
            for (MapleCharacter affected : affectedp) {
                applyTo(applyfrom, affected, false, null);
                affected.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(sourceid, 2));
                affected.getMap().broadcastMessage(affected, MaplePacketCreator.showBuffeffect(affected, affected.getId(), sourceid, 2), false);
            }
        }
    }

    private void applyMonsterBuff(MapleCharacter applyfrom) {
        Rectangle bounds = calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft());
        List<MapleMapObject> affected = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.MONSTER));
        ISkill skill_ = SkillFactory.getSkill(sourceid);
        int i = 0;
        for (MapleMapObject mo : affected) {
            MapleMonster monster = (MapleMonster) mo;
            if (makeChanceResult()) {
                monster.applyStatus(applyfrom, new MonsterStatusEffect(getMonsterStati(), skill_, false), isPoison(), getDuration());
            }
            i++;
            if (i >= mobCount) {
                break;
            }
        }
    }

    private Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
        Point mylt;
        Point myrb;
        if (facingLeft) {
            mylt = new Point(lt.x + posFrom.x, lt.y + posFrom.y);
            myrb = new Point(rb.x + posFrom.x, rb.y + posFrom.y);
        } else {
            mylt = new Point(-rb.x + posFrom.x, lt.y + posFrom.y);
            myrb = new Point(-lt.x + posFrom.x, rb.y + posFrom.y);
        }
        Rectangle bounds = new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
        return bounds;
    }

    public void silentApplyBuff(MapleCharacter chr, long starttime) {
        int localDuration = duration;
        localDuration = alchemistModifyVal(chr, localDuration, false);
        CancelEffectAction cancelAction = new CancelEffectAction(chr, this, starttime);
        ScheduledFuture<?> schedule = TimerManager.getInstance().schedule(cancelAction, ((starttime + localDuration) - System.currentTimeMillis()));
        chr.registerEffect(this, starttime, schedule);

    }

    private void applyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary) {
        applyBuffEffect(applyfrom, applyto, primary, -1);
    }

    private void applyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary, int oid) {
        if (sourceid != Corsair.BATTLE_SHIP) {
            if (!this.isMonsterRiding()) {
                if (isHomingBeacon()) {
                    applyto.offBeacon(true);
                } else {
                    applyto.cancelEffect(this, true, -1);
                }
            }
        } else {
            applyto.cancelEffect(this, true, -1);
        }
        List<Pair<MapleBuffStat, Integer>> localstatups = statups;
        int localDuration = duration;
        int localsourceid = sourceid;
        int seconds = localDuration / 1000;
        MapleMount givemount = null;
        if (isMonsterRiding()) {
            int ridingLevel = 0;
            IItem mount = applyfrom.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18);
            if (mount != null) {
                ridingLevel = mount.getItemId();
            }
            if (isNonItemMountSkill()) {
                ridingLevel = getMountIDFromSkill();
            } else {
                if (applyto.getMount() == null) {
                    applyto.mount(ridingLevel, sourceid);
                }
                applyto.getMount().startSchedule();
            }
            if (isNonItemMountSkill()) {
                givemount = new MapleMount(applyto, ridingLevel, sourceid);
            } else {
                givemount = applyto.getMount();
            }
            localDuration = sourceid;
            localsourceid = ridingLevel;
            localstatups = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MONSTER_RIDING, 0));
        } else if (isSkillMorph()) {
            localstatups = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MORPH, getMorph(applyto)));
        }
        if (primary) {
            localDuration = alchemistModifyVal(applyfrom, localDuration, false);
        }
        if (localstatups.size() > 0) {
            MaplePacket buff = MaplePacketCreator.giveBuff((skill ? sourceid : -sourceid), localDuration, localstatups, false);
            if (isDash()) {
                if ((applyto.getJob().getId() / 100) % 10 != 5) {
                    applyto.changeSkillLevel(SkillFactory.getSkill(sourceid), 0, 10);
                } else {
                    buff = MaplePacketCreator.givePirateBuff(sourceid, localDuration / 1000, localstatups);
                }
            } else if (isInfusion()) {
                buff = MaplePacketCreator.giveSpeedInfusion(sourceid, localDuration / 1000, localstatups);
            } else if (isMonsterRiding()) {
                buff = MaplePacketCreator.giveMount(givemount.getItemId(), sourceid, localstatups);
            } else if (isCygnusFA()) {
                buff = MaplePacketCreator.giveFinalAttack(sourceid, seconds);
            } else if (isHomingBeacon()) {
                applyto.setBeacon(oid);
                applyto.getClient().getSession().write(MaplePacketCreator.giveHomingBeacon(localsourceid, oid));
            }
            applyto.getClient().getSession().write(buff);
        }
        if (isDash()) {
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showPirateBuff(applyto.getId(), sourceid, localDuration, localstatups), false);
        } else if (isInfusion()) {
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showSpeedInfusion(applyto.getId(), sourceid, localDuration, localstatups), false);
        } else if (isDs()) {
            List<Pair<MapleBuffStat, Integer>> dsstat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.DARKSIGHT, 0));
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), sourceid, dsstat, false), false);
        } else if (isCombo()) {
            List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, 1));
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), sourceid, stat, false), false);
        } else if (isMonsterRiding()) {
            List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MONSTER_RIDING, 1));
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showMonsterRiding(applyto.getId(), stat, givemount), true);
            localDuration = duration;

        } else if (isShadowPartner()) {
            //List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SHADOWPARTNER, 0));
            //applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), sourceid, localstatups, false), false);
        } else if (sourceid == BladeLord.MIRROR_IMAGE) {
            List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MIRROR_IMAGE, 0));
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), sourceid, stat, false), false);
        } else if (isSoulArrow()) {
            List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.SOULARROW, 0));
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), sourceid, stat, false), false);
        } else if (isEnrage()) {
            applyto.handleOrbconsume();
        } else if (isMorph()) {
            List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MORPH, Integer.valueOf(getMorph(applyto))));
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.giveForeignBuff(applyto.getId(), sourceid, stat, true), false);
        } else if (isTimeLeap()) {
            for (PlayerCoolDownValueHolder i : applyto.getAllCooldowns()) {
                if (i.skillId != Buccaneer.TIME_LEAP) {
                    applyto.removeCooldown(i.skillId);
                }
            }
        }
        if (localstatups.size() > 0) {
            long starttime = System.currentTimeMillis();
            CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime);
            ScheduledFuture<?> schedule = isHomingBeacon() ? null : TimerManager.getInstance().schedule(cancelAction, localDuration);
            applyto.registerEffect(this, starttime, schedule);
        }
        if (primary && !isHomingBeacon()) {
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showBuffeffect(applyto, applyto.getId(), sourceid, 1, (byte) 3), false);
        }
    }

    private int calcHPChange(MapleCharacter applyfrom, boolean primary) {
        int hpchange = 0;
        if (hp != 0) {
            if (!skill) {
                if (primary) {
                    hpchange += alchemistModifyVal(applyfrom, hp, true);
                } else {
                    hpchange += hp;
                }
            } else {
                hpchange += makeHealHP(hp / 100.0, applyfrom.getTotalMagic(), 3, 5);
            }
        }
        if (hpR != 0) {
            hpchange += (int) (applyfrom.getCurrentMaxHp() * hpR);
            applyfrom.checkBerserk();
        }
        if (primary) {
            if (hpCon != 0) {
                hpchange -= hpCon;
            }
        }
        if (isChakra()) {
            hpchange += makeHealHP(getY() / 100.0, applyfrom.getTotalLuk(), 2.3, 3.5);
        }
        return hpchange;
    }

    private int makeHealHP(double rate, double stat, double l, double u) {
        return (int) ((Math.random() * ((int) ((u - l) * stat * rate)) + 1) + (int) (stat * l * rate));
    }

    private int calcMPChange(MapleCharacter applyfrom, boolean primary) {
        int mpchange = 0;
        if (mp != 0) {
            if (primary) {
                mpchange += alchemistModifyVal(applyfrom, mp, true);
            } else {
                mpchange += mp;
            }
        }
        if (mpR != 0) {
            mpchange += (int) (applyfrom.getCurrentMaxMp() * mpR);
        }
        if (primary) {
            if (mpCon != 0) {
                double mod = 1.0;
                boolean isAFpMage = applyfrom.getJob().isA(MapleJob.FP_MAGE);
                boolean isCygnus = applyfrom.getJob().isA(MapleJob.BLAZEWIZARD2);
                if (isAFpMage || isCygnus || applyfrom.getJob().isA(MapleJob.IL_MAGE)) {
                    ISkill amp = isAFpMage ? SkillFactory.getSkill(FPMage.ELEMENT_AMPLIFICATION) : (isCygnus ? SkillFactory.getSkill(BlazeWizard.ELEMENT_AMPLIFICATION) : SkillFactory.getSkill(ILMage.ELEMENT_AMPLIFICATION));
                    int ampLevel = applyfrom.getSkillLevel(amp);
                    if (ampLevel > 0) {
                        mod = amp.getEffect(ampLevel).getX() / 100.0;
                    }
                }
                mpchange -= mpCon * mod;
                if (applyfrom.getBuffedValue(MapleBuffStat.INFINITY) != null) {
                    mpchange = 0;
                } else if (applyfrom.getBuffedValue(MapleBuffStat.CONCENTRATE) != null) {
                    mpchange -= (int) (mpchange * (applyfrom.getBuffedValue(MapleBuffStat.CONCENTRATE).doubleValue() / 100));
                }
            }
        }
        return mpchange;
    }

    private int alchemistModifyVal(MapleCharacter chr, int val, boolean withX) {
        if (!skill && (chr.getJob().isA(MapleJob.HERMIT) || chr.getJob().isA(MapleJob.NIGHTWALKER3))) {
            MapleStatEffect alchemistEffect = getAlchemistEffect(chr);
            if (alchemistEffect != null) {
                return (int) (val * ((withX ? alchemistEffect.getX() : alchemistEffect.getY()) / 100.0));
            }
        }
        return val;
    }

    private MapleStatEffect getAlchemistEffect(MapleCharacter chr) {
        int id = Hermit.ALCHEMIST;
        if (chr.isCygnus()) {
            id = NightWalker.ALCHEMIST;
        }
        int alchemistLevel = chr.getSkillLevel(SkillFactory.getSkill(id));
        return alchemistLevel == 0 ? null : SkillFactory.getSkill(id).getEffect(alchemistLevel);
    }

    private boolean isGmBuff() {
        switch (sourceid) {
            case Beginner.ECHO_OF_HERO:
            case Noblesse.ECHO_OF_HERO:
            case GM.HEAL_PLUS_DISPEL:
            case GM.HASTE:
            case GM.HOLY_SYMBOL:
            case GM.BLESS:
            case GM.RESURRECTION:
            case GM.HYPER_BODY:
                return true;
            default:
                return false;
        }
    }

    private boolean isMonsterBuff() {
        if (!skill) {
            return false;
        }
        switch (sourceid) {
            case Page.THREATEN:
            case FPWizard.SLOW:
            case ILWizard.SLOW:
            case FPMage.SEAL:
            case ILMage.SEAL:
            case Priest.DOOM:
            case Hermit.SHADOW_WEB:
            case NightLord.NINJA_AMBUSH:
            case Shadower.NINJA_AMBUSH:
            case BlazeWizard.SLOW:
            case BlazeWizard.SEAL:
            case NightWalker.SHADOW_WEB:
                return true;
        }
        return false;
    }

    private boolean isPartyBuff() {
        if (lt == null || rb == null) {
            return false;
        }
        if ((sourceid >= 1211003 && sourceid <= 1211008) || sourceid == Paladin.SWORD_HOLY_CHARGE || sourceid == Paladin.BW_HOLY_CHARGE || sourceid == DawnWarrior.SOUL_CHARGE) {// wk charges have lt and rb set but are neither player nor monster buffs
            return false;
        }
        return true;
    }

    private boolean isHeal() {
        return sourceid == Cleric.HEAL || sourceid == GM.HEAL_PLUS_DISPEL;
    }

    private boolean isResurrection() {
        return sourceid == Bishop.RESURRECTION || sourceid == GM.RESURRECTION || sourceid == GM.RESURRECTION;
    }

    private boolean isTimeLeap() {
        return sourceid == Buccaneer.TIME_LEAP;
    }

    public boolean isHide() {
        return skill && sourceid == GM.HIDE;
    }

    /*public boolean isDragonBlood() {
        return skill && sourceid == DragonKnight.DRAGON_BLOOD;
    }*/

    public boolean isBerserk() {
        return skill && sourceid == DarkKnight.BERSERK;
    }

    private boolean isDs() {
        return skill && (sourceid == Rogue.DARK_SIGHT || sourceid == WindArcher.WIND_WALK || sourceid == NightWalker.DARK_SIGHT || sourceid == BladeLord.ADVANCED_DARK_SIGHT);
    }

    private boolean isCombo() {
        return skill && (sourceid == Crusader.COMBO || sourceid == DawnWarrior.COMBO);
    }

    private boolean isEnrage() {
        return skill && sourceid == Hero.ENRAGE;
    }

    public boolean isBeholder() {
        return skill && sourceid == DarkKnight.BEHOLDER;
    }

    private boolean isShadowPartner() {
        return skill && (sourceid == Hermit.SHADOW_PARTNER || sourceid == NightWalker.SHADOW_PARTNER);
    }

    private boolean isChakra() {
        return skill && sourceid == ChiefBandit.CHAKRA;
    }

    public boolean isHomingBeacon() {
        return skill && (sourceid == Outlaw.HOMING_BEACON || sourceid == Corsair.BULLSEYE);
    }

    public boolean isMonsterRiding() {
        return skill && (sourceid == 80001000 || isNonItemMountSkill());
    }

    public boolean isMagicDoor() {
        return skill && sourceid == Priest.MYSTIC_DOOR;
    }

    public boolean isPoison() {
        return skill && (sourceid == FPMage.POISON_MIST || sourceid == FPWizard.POISON_BREATH || sourceid == FPMage.ELEMENT_COMPOSITION || sourceid == NightWalker.POISON_BOMB);
    }

    private boolean isMist() {
        return skill && (sourceid == FPMage.POISON_MIST || sourceid == Shadower.SMOKE_SCREEN || sourceid == BlazeWizard.FLAME_GEAR || sourceid == NightWalker.POISON_BOMB || sourceid == Evan.RECOVERY_AURA);
    }

    private boolean isSoulArrow() {
        return skill && (sourceid == Hunter.SOUL_ARROW || sourceid == Crossbowman.SOUL_ARROW || sourceid == WindArcher.SOUL_ARROW);
    }

    private boolean isShadowClaw() {
        return skill && sourceid == NightLord.SHADOW_STARS;
    }

    private boolean isDispel() {
        return skill && (sourceid == Priest.DISPEL || sourceid == GM.HEAL_PLUS_DISPEL);
    }

    private boolean isHeroWill() {
        if (skill) {
            switch (sourceid) {
                case Hero.HEROS_WILL:
                case Paladin.HEROS_WILL:
                case DarkKnight.HEROS_WILL:
                case FPArchMage.HEROS_WILL:
                case ILArchMage.HEROS_WILL:
                case Bishop.HEROS_WILL:
                case Bowmaster.HEROS_WILL:
                case Marksman.HEROS_WILL:
                case NightLord.HEROS_WILL:
                case Shadower.HEROS_WILL:
                case Buccaneer.PIRATES_RAGE:
                case Corsair.SPEED_INFUSION:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    private boolean isDash() {
        return skill && (sourceid == Pirate.DASH || sourceid == ThunderBreaker.DASH);
    }

    private boolean isSkillMorph() {
        return skill && (sourceid == Buccaneer.SUPER_TRANSFORMATION || sourceid == Marauder.TRANSFORMATION || sourceid == WindArcher.EAGLE_EYE || sourceid == ThunderBreaker.TRANSFORMATION);
    }

    private boolean isInfusion() {
        return skill && (sourceid == Buccaneer.SPEED_INFUSION || sourceid == ThunderBreaker.SPEED_INFUSION);
    }

    private boolean isCygnusFA() {
        return skill && (sourceid == DawnWarrior.FINAL_ATTACK || sourceid == WindArcher.FINAL_ATTACK);
    }

    private boolean isMorph() {
        return morphId > 0;
    }

    private boolean isDrain() {
        return sourceid == Aran.COMBO_DRAIN;
    }

    private int getFatigue() {
        return fatigue;
    }

    private int getMorph() {
        return morphId;
    }

    private int getMorph(MapleCharacter chr) {
        if (morphId % 10 == 0) {
            return morphId + chr.getGender();
        }
        return morphId + 100 * chr.getGender();
    }

    public SummonMovementType getSummonMovementType() {
        if (!skill) {
            return null;
        }
        switch (sourceid) {
            case Ranger.PUPPET:
            case Sniper.PUPPET:
            case WindArcher.PUPPET:
            case Outlaw.OCTOPUS:
            case Corsair.WRATH_OF_THE_OCTOPI:
            case BladeMaster.MIRRORED_TARGET:
                return SummonMovementType.STATIONARY;
            case Ranger.SILVER_HAWK:
            case Sniper.GOLDEN_EAGLE:
            case Priest.SUMMON_DRAGON:
            case Marksman.FROST_PREY:
            case Bowmaster.PHOENIX:
            case Outlaw.GAVIOTA:
                return SummonMovementType.CIRCLE_FOLLOW;
            case DarkKnight.BEHOLDER:
            case FPArchMage.ELQUINES:
            case ILArchMage.IFRIT:
            case Bishop.BAHAMUT:
            case DawnWarrior.SOUL:
            case BlazeWizard.FLAME:
            case BlazeWizard.IFRIT:
            case WindArcher.STORM:
            case NightWalker.DARKNESS:
            case ThunderBreaker.LIGHTNING:
                return SummonMovementType.FOLLOW;
        }
        return null;
    }

    public boolean isSkill() {
        return skill;
    }

    public int getSourceId() {
        return sourceid;
    }

    public boolean makeChanceResult() {
        return prop == 1.0 || Math.random() < prop;
    }

    private static class CancelEffectAction implements Runnable {
        private MapleStatEffect effect;
        private WeakReference<MapleCharacter> target;
        private long startTime;

        public CancelEffectAction(MapleCharacter target, MapleStatEffect effect, long startTime) {
            this.effect = effect;
            this.target = new WeakReference<MapleCharacter>(target);
            this.startTime = startTime;
        }

        @Override
        public void run() {
            MapleCharacter realTarget = target.get();
            if (realTarget != null) {
                realTarget.cancelEffect(effect, false, startTime);
            }
        }
    }

    public short getHp() {
        return hp;
    }

    public short getMp() {
        return mp;
    }

    public short getMatk() {
        return matk;
    }

    public int getDuration() {
        return duration;
    }

    public int getMastery() {
        return mastery;
    }

    public List<Pair<MapleBuffStat, Integer>> getStatups() {
        return statups;
    }

    public boolean sameSource(MapleStatEffect effect) {
        return this.sourceid == effect.sourceid && this.skill == effect.skill;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    public int getmhpR() {
        return mhpR;
    }
    
    public int getmmpR() {
        return mmpR;
    }

    public int getDamage() {
        return damage;
    }

    public int getAttackCount() {
        return attackCount;
    }

    public int getBulletCount() {
        return bulletCount;
    }

    public int getBulletConsume() {
        return bulletConsume;
    }

    public int getMoneyCon() {
        return moneyCon;
    }

    public int getCooldown() {
        return cooldown;
    }

    public Map<MonsterStatus, Integer> getMonsterStati() {
        return monsterStatus;
    }

    public boolean isNonItemMountSkill()
    {
        if(getMountIDFromSkill() == -1)
            return false;
        return true;
    }

    public int getMountIDFromSkill()
    {
        if(sourceid == Corsair.BATTLE_SHIP)
            return 1932000;
        
        int skillmod = -1;
        if(sourceid / 10000 == 2001)
            skillmod = sourceid % 20010000;
        else
            skillmod = sourceid % 10000000;

        switch(skillmod)
        {
            case 1018: //yeti
            case 1022: //yeti
            case 1050:
                return 1932003;
            case 1023: //witch
                return 1932005;
            case 1025: //pony
                return 1932006;
            case 1027: //croc
                return 1932007;
            case 1028: //black scooter
                return 1932008;
            case 1029: //pink scooter
                return 1932009;
            case 1030: //cloud
                return 1932011;
            case 1031: //ROGGG :D
                return 1932010;
            case 1034: //tiger
                return 1932014;
            case 1035: //mist rog
                return 1932012;
            case 1036: //cute lion
                return 1932017;
            case 1037: //unicorn
                return 1932018;
            case 1038: //low rider
                return 1932019;
            case 1039: //red truck
                return 1932020;
            case 1040: //gargoyle
                return 1932021;
            case 1042: //shinjo
                return 1932022;
            case 1044: //shinjo
                return 1932023;
            case 1046: //spaceship
                return 1932001;
            case 1049: //spaceship
                return 1932025;
            case 1051: //ostrich
                return 1932026;
            case 1052: //hot air balloon
                return 1932027;
            case 1053: //robot
                return 1932028;
            case 1054: //chicken
                return 1932029;
            default:
                return -1;
        }
    }
    
    public static int getArgument(String args, int level) throws Exception {
        String x[], a[], b, c;
        int result, index;
        if (args.contains("x")) {
            b = args.replace("x", String.valueOf(level));
        } else {
            b = args;
        }
        if (b.startsWith("-")) {
            b = "0" + b; 
        }
        if (b.contains("d")) {
            a = b.split("d");
            index = b.lastIndexOf("d");
            c = b.substring(0, index);
            result = (int) Math.floor(StringCalculator.postfixCalc(StringCalculator.postfix(a[1])));
            c += String.valueOf(result);
            return StringCalculator.postfixCalc(StringCalculator.postfix(c));
        } else if (args.contains("u")) { //?©ª?
            a = b.split("u");
            index = b.lastIndexOf("u");
            c = b.substring(0, index);
            result = (int) Math.ceil(StringCalculator.postfixCalc(StringCalculator.postfix(a[1])));
            c += String.valueOf(result);
            return StringCalculator.postfixCalc(StringCalculator.postfix(c));
        }
        return StringCalculator.postfixCalc(StringCalculator.postfix(b));
    }
}
