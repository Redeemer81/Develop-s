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
package constants;

public final class ExpTable {
    private static final int[] exp = {1, 15, 34, 57, 92, 135, 372, 560, 840, 1242, 1242, 1242, 1242, 1242, 1242, 1490, 1788, 2146, 2575, 3090, 3708, 4450, 5340, 6408, 7690, 9228, 11074, 13289, 15947, 19136, 19136, 19136, 19136, 19136, 19136, 22963, 27556, 33067, 39680, 47616, 51425, 55539, 59982, 64781, 69963, 75560, 81605, 88133, 95184, 102799, 111023, 119905, 129497, 139857, 151046, 163130, 176180, 190274, 205496, 221936, 239691, 258866, 279575, 301941, 326096, 352184, 380359, 410788, 443651, 479143, 479143, 479143, 479143, 479143, 479143, 512683, 548571, 586971, 628059, 672023, 719065, 769400, 823258, 880886, 942548, 1008526, 1079123, 1154662, 1235488, 1321972, 1414510, 1513526, 1619473, 1732836, 1854135, 1983924, 2122799, 2271395, 2430393, 2600521, 2782557, 2977336, 3185750, 3408753, 3647366, 3902682, 4175870, 4468181, 4780954, 5115621, 5473714, 5856874, 6266855, 6705535, 7174922, 7677167, 8214569, 8789589, 9404860, 10063200, 10063200, 10063200, 10063200, 10063200, 10063200, 10767624, 11521358, 12327853, 13190803, 14114159, 15102150, 16159301, 17290452, 18500784, 19795839, 21181548, 22664256, 24250754, 25948307, 27764688, 29708216, 31787791, 34012936, 36393842, 38941411, 41667310, 44584022, 47704904, 51044247, 54617344, 58440558, 62531397, 66908595, 71592197, 79903651, 81965907, 87703520, 93842766, 100411760, 107440583, 113887018, 120720239, 127963453, 135641260, 143779736, 152406520, 161550911, 171243966, 181518604, 192409720, 203954303, 216191561, 229163055, 242912838, 257487608, 272936864, 289313076, 306671861, 325072173, 344576503, 365251093, 387166159, 410396129, 435019897, 461121091, 488788356, 518115657, 549202596, 582154752, 617084037, 654109079, 693355624, 734956961, 779054379, 825797642, 875345501, 927866231, 983538205, 1042550497, 1105103527};
    private static final int[] pet = {1, 1, 3, 6, 14, 31, 60, 108, 181, 287, 434, 632, 891, 1224, 1642, 2161, 2793, 3557, 4467, 5542, 6801, 8263, 9950, 11882, 14084, 16578, 19391, 22547, 26074, 30000, 2147483647};
    private static final int[] mount = {1, 24, 50, 105, 134, 196, 254, 263, 315, 367, 430, 543, 587, 679, 725, 897, 1146, 1394, 1701, 2247, 2543, 2898, 3156, 3313, 3584, 3923, 4150, 4305, 4550};

    public static final int getExpNeededForLevel(int level) {
        return level > 200 ? 2000000000 : exp[level];
    }

    public static final int getClosenessNeededForLevel(int level) {
        return pet[level];
    }

    public static final int getMountExpNeededForLevel(int level) {
        return mount[level];
    }

    public static final int getReverseItemExpNeededForLevel(int num) {
        return 5 * num + 65;
    }

    public static final int getTimelessItemExpNeededForLevel(int i) {
        return 10 * i + 70;
    }
}
