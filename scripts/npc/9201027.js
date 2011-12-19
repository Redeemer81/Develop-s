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
/**
 *9201027 - Nana(P)
 *@author BubblesDev v75 (Moogra)
 */
 
function start() {
    cm.sendNext("Nice to meet you! I am Nana the Fairy from Amoria. I am waiting for you to prove your devotion to your loved one by obtaining a Proof of Love! To start, you'll have to venture to Amoria to find my good friend, Moony the Ringmaker. Even if you are not interested in marriage yet, Amoria is open for everyone! Go visit Thomas Swift at Henesys to head to Amoria If you are interested in weddings, be sure to speak with Ames the Wise once you get there!");
    cm.dispose();
}

//function action(mode, type, selection) {
//    if (mode == -1) {
//        cm.dispose();
//    } else {
//        if (mode == 0 && status == 0) {
//            cm.dispose();
//            return;
//        }
//        if (mode == 1)
//            status++;
//        else
//            status--;
//        if (cm.getPlayer().getMarriageQuestLevel() == 1 || cm.getPlayer().getMarriageQuestLevel() == 52) {
//            if (!cm.haveItem(4000018, 40)) {
//                if (status == 0) {
//                    cm.sendNext("Hey, you look like you need proofs of love? I can get them for you.");
//                } else if (status == 1) {
//                    cm.sendOk("All you have to do is bring me 40 #bFirewood#k.");
//                    cm.dispose();
//                }
//            } else {
//                if (status == 0) {
//                    cm.sendNext("Wow, you were quick! Heres the proof of love...");
//                    cm.gainItem(4000018, -40)
//                    cm.gainItem(4031371, 1);
//                    cm.dispose();
//                }
//            }
//        }
//        else {
//            cm.sendOk("Hi, I'm Nana the love fairy... Hows it going?");
//            cm.dispose();
//        }
//    }
//}