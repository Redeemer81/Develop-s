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

public enum MapleJob {
    BEGINNER(0),
    WARRIOR(100),
    FIGHTER(110),
    CRUSADER(111),
    HERO(112),
    PAGE(120),
    WHITEKNIGHT(121),
    PALADIN(122),
    SPEARMAN(130),
    DRAGONKNIGHT(131),
    DARKKNIGHT(132),
    MAGICIAN(200),
    FP_WIZARD(210),
    FP_MAGE(211),
    FP_ARCHMAGE(212),
    IL_WIZARD(220),
    IL_MAGE(221),
    IL_ARCHMAGE(222),
    CLERIC(230),
    PRIEST(231),
    BISHOP(232),
    BOWMAN(300),
    HUNTER(310),
    RANGER(311),
    BOWMASTER(312),
    CROSSBOWMAN(320),
    SNIPER(321),
    MARKSMAN(322),
    THIEF(400),
    ASSASSIN(410),
    HERMIT(411),
    NIGHTLORD(412),
    BANDIT(420),
    CHIEFBANDIT(421),
    SHADOWER(422),
    BLADE_RECRUIT(430),
    BLADE_ACOLYTE(431),
    BLADE_SPECIALIST(432),
    BLADE_LORD(433),
    BLADE_MASTER(434),
    PIRATE(500),
    CSHOOTER1(501),
    BRAWLER(510),
    MARAUDER(511),
    BUCCANEER(512),
    GUNSLINGER(520),
    OUTLAW(521),
    CORSAIR(522),
    CSHOOTER2(530),
    CSHOOTER3(531),
    CSHOOTER(532),
    MAPLELEAF_BRIGADIER(800),
    GM(900),
    SUPERGM(910),
    NOBLESSE(1000),
    DAWNWARRIOR1(1100),
    DAWNWARRIOR2(1110),
    DAWNWARRIOR3(1111),
    DAWNWARRIOR4(1112),
    BLAZEWIZARD1(1200),
    BLAZEWIZARD2(1210),
    BLAZEWIZARD3(1211),
    BLAZEWIZARD4(1212),
    WINDARCHER1(1300),
    WINDARCHER2(1310),
    WINDARCHER3(1311),
    WINDARCHER4(1312),
    NIGHTWALKER1(1400),
    NIGHTWALKER2(1410),
    NIGHTWALKER3(1411),
    NIGHTWALKER4(1412),
    THUNDERBREAKER1(1500),
    THUNDERBREAKER2(1510),
    THUNDERBREAKER3(1511),
    THUNDERBREAKER4(1512),
    LEGEND(2000),
    EVAN1(2001),
    ARAN2(2100),
    ARAN3(2110),
    ARAN4(2111),
    ARAN5(2112),
    EVAN2(2200),
    EVAN3(2210),
    EVAN4(2211),
    EVAN5(2212),
    EVAN6(2213),
    EVAN7(2214),
    EVAN8(2215),
    EVAN9(2216),
    EVAN10(2217),
    EVAN11(2218),
    MER1(2002),
    MER2(2300),
    MER3(2310),
    MER4(2311),
    MER5(2312),
    PHANTOM1(2003),
    PHANTOM2(2400),
    PHANTOM3(2410),
    PHANTOM4(2411),
    PHANTOM5(2412),
    CITIZEN(3000),
    DSLAYER1(3001),
    DSLAYER2(3100),
    DSLAYER3(3110),
    DSLAYER4(3111),
    DSLAYER5(3112),
    BATTLEMAGE1(3200),
    BATTLEMAGE2(3210),
    BATTLEMAGE3(3211),
    BATTLEMAGE4(3212),
    WILDHUNTER1(3300),
    WILDHUNTER2(3310),
    WILDHUNTER3(3311),
    WILDHUNTER4(3312),
    MECHANIC1(3500),
    MECHANIC2(3510),
    MECHANIC3(3511),
    MECHANIC4(3512),
    ADDITIONAL_SKILLS(9000);
    final int jobid;

    private MapleJob(int id) {
        jobid = id;
    }

    public int getId() {
        return jobid;
    }

    public static MapleJob getById(int id) {
        for (MapleJob l : MapleJob.values()) {
            if (l.getId() == id) {
                return l;
            }
        }
        return null;
    }

    public static MapleJob getBy5ByteEncoding(int encoded) {
        switch (encoded) {
            case 2:
                return WARRIOR;
            case 4:
                return MAGICIAN;
            case 8:
                return BOWMAN;
            case 16:
                return THIEF;
            case 32:
                return PIRATE;
            case 1024:
                return NOBLESSE;
            case 2048:
                return DAWNWARRIOR1;
            case 4096:
                return BLAZEWIZARD1;
            case 8192:
                return WINDARCHER1;
            case 16384:
                return NIGHTWALKER1;
            case 32768:
                return THUNDERBREAKER1;
            case 65536:
                return LEGEND;
            default:
                return BEGINNER;
        }
    }

    public boolean isA(MapleJob basejob) {
        return getId() >= basejob.getId() && getId() / 100 == basejob.getId() / 100;
    }

    public boolean isAnEvan() {
        return getId() == 2001 || getId() / 100 == 22;
    }

    public static boolean isDemonSlayer(int jobId) {
        return jobId / 100 == 31 || jobId == 3001;
    }

    public static boolean isPhantom(int jobId) {
        return jobId == 2003 || jobId / 100 == 24;
    }

     public static boolean isExtendSPJob(int jobId) {
      return jobId / 1000 == 3 || jobId / 100 == 22 || jobId == 2001 || jobId / 100 == 23 || jobId == 2002 || jobId == 2003 || jobId / 100 == 24;
    }

     public static boolean isExtendSPJob(MapleJob job) {
         return isExtendSPJob(job.getId());
    }

}
