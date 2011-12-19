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

import constants.skills.*;
import java.util.HashMap;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.MapleStatEffect;
import server.life.Element;
import tools.StringUtil;

public class SkillFactory {
    private static Map<Integer, ISkill> skills = new HashMap<Integer, ISkill>();
    private static MapleDataProvider datasource = MapleDataProviderFactory.getDataProvider(MapleDataProviderFactory.fileInWZPath("Skill.wz"));

    public static ISkill getSkill(int id) {
        ISkill ret = skills.get(id);
        if (ret == null) {
            MapleData skillData = datasource.getData(StringUtil.getLeftPaddedStr(String.valueOf(id / 10000), '0', 3) + ".img").getChildByPath("skill/" + StringUtil.getLeftPaddedStr(String.valueOf(id), '0', 7));
            if (skillData != null) {
                ret = loadFromData(id, skillData);
                skills.put(id, ret);
            } else {
                return null;
            }
        }
        return ret;
    }

    public static Skill loadFromData(int id, MapleData data) {
        Skill ret = new Skill(id);
        boolean isBuff = false;
        int skillType = MapleDataTool.getInt("skillType", data, -1);
        ret.masterLevel = MapleDataTool.getInt("masterLevel", data, -1);
        ret.isInvisible = MapleDataTool.getInt("invisible", data, -1) == -1;
        ret.isCommon = data.getChildByPath("common") != null;
        if (ret.isCommon)
            ret.maxLevel = MapleDataTool.getInt(data.getChildByPath("common").getChildByPath("maxLevel"), 1); 
        /*if(ret.masterLevel != -1)
            System.out.println("Skill " + id + " in data found to have master level " + ret.getMasterLevel());*/
        String elem = MapleDataTool.getString("elemAttr", data, null);
        if (elem != null) {
            ret.element = Element.getFromChar(elem.charAt(0));
        } else {
            ret.element = Element.NEUTRAL;
        }
        MapleData effect = data.getChildByPath("effect");
        if (skillType != -1) {
            if (skillType == 2) {
                isBuff = true;
            }
        } else {
            MapleData action = data.getChildByPath("action");
            MapleData hit = data.getChildByPath("hit");
            MapleData ball = data.getChildByPath("ball");
            isBuff = effect != null && hit == null && ball == null;
            isBuff |= action != null && MapleDataTool.getString("0", action, "").equals("alert2");
            switch (id) {
                case Hero.RUSH:
                case Paladin.RUSH:
                case DarkKnight.RUSH:
                case DragonKnight.SACRIFICE:
                case FPMage.EXPLOSION:
                case FPMage.POISON_MIST:
                case Cleric.HEAL:
                case Ranger.MORTAL_BLOW:
                case Sniper.MORTAL_BLOW:
                case Assassin.DRAIN:
                case Hermit.SHADOW_WEB:
                case Bandit.STEAL:
                case Shadower.SMOKE_SCREEN:
                case GM.HEAL_PLUS_DISPEL:
                case Hero.MONSTER_MAGNET:
                case Paladin.MONSTER_MAGNET:
                case DarkKnight.MONSTER_MAGNET:
                case Gunslinger.RECOIL_SHOT:
                case Marauder.ENERGY_DRAIN:
                case BlazeWizard.FLAME_GEAR:
                case NightWalker.SHADOW_WEB:
                case NightWalker.POISON_BOMB:
                case NightWalker.VAMPIRE:
                    isBuff = false;
                    break;
                case Beginner.RECOVERY:
                case Beginner.NIMBLE_FEET:
                case Beginner.MONSTER_RIDER:
                case Beginner.ECHO_OF_HERO:
                case Swordsman.IRON_BODY:
                case Fighter.AXE_BOOSTER:
                case Fighter.POWER_GUARD:
                case Fighter.RAGE:
                case Fighter.SWORD_BOOSTER:
                case Crusader.ARMOR_CRASH:
                case Crusader.COMBO:
                case Hero.ENRAGE:
                case Hero.HEROS_WILL:
                case Hero.MAPLE_WARRIOR:
                case Hero.STANCE:
                case Page.BW_BOOSTER:
                case Page.POWER_GUARD:
                case Page.SWORD_BOOSTER:
                case Page.THREATEN:
                case WhiteKnight.BW_FIRE_CHARGE:
                case WhiteKnight.BW_ICE_CHARGE:
                case WhiteKnight.BW_LIT_CHARGE:
                case WhiteKnight.MAGIC_CRASH:
                case WhiteKnight.SWORD_FIRE_CHARGE:
                case WhiteKnight.SWORD_ICE_CHARGE:
                case WhiteKnight.SWORD_LIT_CHARGE:
                case Paladin.BW_HOLY_CHARGE:
                case Paladin.HEROS_WILL:
                case Paladin.MAPLE_WARRIOR:
                case Paladin.STANCE:
                case Paladin.SWORD_HOLY_CHARGE:
                case Spearman.HYPER_BODY:
                case Spearman.IRON_WILL:
                case Spearman.POLEARM_BOOSTER:
                case Spearman.SPEAR_BOOSTER:
                case DragonKnight.DRAGON_STRANGS:
                case DragonKnight.POWER_CRASH:
                case DarkKnight.AURA_OF_BEHOLDER:
                case DarkKnight.BEHOLDER:
                case DarkKnight.HEROS_WILL:
                case DarkKnight.HEX_OF_BEHOLDER:
                case DarkKnight.MAPLE_WARRIOR:
                case DarkKnight.STANCE:
                case Magician.MAGIC_GUARD:
                case Magician.MAGIC_ARMOR:
                case FPWizard.MEDITATION:
                case FPWizard.SLOW:
                case FPMage.SEAL:
                case FPMage.SPELL_BOOSTER:
                case FPArchMage.ELQUINES:
                case FPArchMage.HEROS_WILL:
                case FPArchMage.INFINITY:
                case FPArchMage.MANA_REFLECTION:
                case FPArchMage.MAPLE_WARRIOR:
                case ILWizard.MEDITATION:
                case ILMage.SEAL:
                case ILWizard.SLOW:
                case ILMage.SPELL_BOOSTER:
                case ILArchMage.HEROS_WILL:
                case ILArchMage.IFRIT:
                case ILArchMage.INFINITY:
                case ILArchMage.MANA_REFLECTION:
                case ILArchMage.MAPLE_WARRIOR:
                case Cleric.INVINCIBLE:
                case Cleric.BLESS:
                case Priest.DISPEL:
                case Priest.DOOM:
                case Priest.HOLY_SYMBOL:
                case Priest.SUMMON_DRAGON:
                case Bishop.BAHAMUT:
                case Bishop.HEROS_WILL:
                case Bishop.HOLY_SHIELD:
                case Bishop.INFINITY:
                case Bishop.MANA_REFLECTION:
                case Bishop.MAPLE_WARRIOR:
                case Archer.FOCUS:
                case Hunter.BOW_BOOSTER:
                case Hunter.SOUL_ARROW:
                case Ranger.PUPPET:
                case Ranger.SILVER_HAWK:
                case Bowmaster.CONCENTRATE:
                case Bowmaster.HEROS_WILL:
                case Bowmaster.MAPLE_WARRIOR:
                case Bowmaster.PHOENIX:
                case Bowmaster.SHARP_EYES:
                case Crossbowman.CROSSBOW_BOOSTER:
                case Crossbowman.SOUL_ARROW:
                case Sniper.GOLDEN_EAGLE:
                case Sniper.PUPPET:
                case Marksman.BLIND:
                case Marksman.FROST_PREY:
                case Marksman.HEROS_WILL:
                case Marksman.MAPLE_WARRIOR:
                case Marksman.SHARP_EYES:
                case Rogue.DARK_SIGHT:
                case Assassin.CLAW_BOOSTER:
                case Assassin.HASTE:
                case Hermit.MESO_UP:
                case Hermit.SHADOW_PARTNER:
                case NightLord.HEROS_WILL:
                case NightLord.MAPLE_WARRIOR:
                case NightLord.NINJA_AMBUSH:
                case NightLord.SHADOW_STARS:
                case Bandit.DAGGER_BOOSTER:
                case Bandit.HASTE:
                case ChiefBandit.MESO_GUARD:
                case ChiefBandit.PICKPOCKET:
                case Shadower.HEROS_WILL:
                case Shadower.MAPLE_WARRIOR:
                case Shadower.NINJA_AMBUSH:
                case Pirate.DASH:
                case Marauder.TRANSFORMATION:
                case Buccaneer.SUPER_TRANSFORMATION:
                case Outlaw.GAVIOTA:
                case Outlaw.OCTOPUS:
                case Corsair.BATTLE_SHIP:
                case Corsair.WRATH_OF_THE_OCTOPI:
                case GM.HASTE:
                case GM.HOLY_SYMBOL:
                case GM.BLESS:
                case GM.HIDE:
                case GM.HYPER_BODY:
                case Noblesse.BLESSING_OF_THE_FAIRY:
                case Noblesse.ECHO_OF_HERO:
                case Noblesse.MONSTER_RIDER:
                case Noblesse.NIMBLE_FEET:
                case Noblesse.RECOVERY:
                case DawnWarrior.COMBO:
                case DawnWarrior.FINAL_ATTACK:
                case DawnWarrior.IRON_BODY:
                case DawnWarrior.RAGE:
                case DawnWarrior.SOUL:
                case DawnWarrior.SOUL_CHARGE:
                case DawnWarrior.SWORD_BOOSTER:
                case BlazeWizard.ELEMENTAL_RESET:
                case BlazeWizard.FLAME:
                case BlazeWizard.IFRIT:
                case BlazeWizard.MAGIC_ARMOR:
                case BlazeWizard.MAGIC_GUARD:
                case BlazeWizard.MEDITATION:
                case BlazeWizard.SEAL:
                case BlazeWizard.SLOW:
                case BlazeWizard.SPELL_BOOSTER:
                case WindArcher.BOW_BOOSTER:
                case WindArcher.EAGLE_EYE:
                case WindArcher.FINAL_ATTACK:
                case WindArcher.FOCUS:
                case WindArcher.PUPPET:
                case WindArcher.SOUL_ARROW:
                case WindArcher.STORM:
                case WindArcher.WIND_WALK:
                case NightWalker.CLAW_BOOSTER:
                case NightWalker.DARKNESS:
                case NightWalker.DARK_SIGHT:
                case NightWalker.HASTE:
                case NightWalker.SHADOW_PARTNER:
                case ThunderBreaker.DASH:
                case ThunderBreaker.ENERGY_CHARGE:
                case ThunderBreaker.ENERGY_DRAIN:
                case ThunderBreaker.KNUCKLER_BOOSTER:
                case ThunderBreaker.LIGHTNING:
                case ThunderBreaker.LIGHTNING_CHARGE:
                case ThunderBreaker.SPEED_INFUSION:
                case ThunderBreaker.TRANSFORMATION:
                case Aran.BODY_PRESSURE:
                case Aran.ARAN_POLEARM_BOOSTER:
                case Aran.COMBO_DRAIN:
                case Aran.FREEZE_STANDING:
                case Aran.SNOW_CHARGE:
                case Aran.COMBO_BARRIER:
                case Evan.MAGIC_BOOSTER:
                case Evan.ELEMENTAL_RESET:
                case Evan.MAGIC_RESISTANCE:
                case Evan.MAGIC_SHIELD:
                case Evan.MAPLE_WARRIOR:
                case BladeRecruit.KATARA_BOOSTER:
                case BladeAcolyte.SELF_HASTE:
                case BladeAcolyte.ENDURE:
                case BladeLord.OWL_SPIRIT:
                case BladeMaster.THORNS:
                case BladeMaster.FINAL_CUT:
                case BladeLord.MIRROR_IMAGE:
                case BladeMaster.MAPLE_WARRIOR:
                case FPMage.TELEPORT_MASTERY:
                case ILMage.TELEPORT_MASTERY:
                case Priest.TELEPORT_MASTERY:
                    isBuff = true;
                    break;
            }
        }
        if (!ret.isCommon) {
            for (MapleData level : data.getChildByPath("level")) {
                ret.effects.add(MapleStatEffect.loadSkillEffectFromData(level, id, isBuff));
            }
        } else {
            for (int level = 1; level <= ret.maxLevel; level++) {
                ret.effects.add(MapleStatEffect.loadSkillEffectFromData(data.getChildByPath("common"), id, level, isBuff));
            }
        }
        ret.animationTime = 0;
        if (effect != null) {
            for (MapleData effectEntry : effect) {
                ret.animationTime += MapleDataTool.getIntConvert("delay", effectEntry, 0);
            }
        }
        ret.setIsBuff(isBuff);
        return ret;
    }
}
