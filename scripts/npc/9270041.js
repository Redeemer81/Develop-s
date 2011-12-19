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
 * @author  BubblesDev v75 (Moogra)
 * @NPC     Irene
 * @purpose Warps to Singapore
 * @note    ~ is in GMS, so is lower case c in "city", celebrate is spelled celerbrate, grammar is off in the 3rd sentence in GMS as well, the space is before "the ticket"
 */
status = 0;
chosen = -1;
canEnter = true;

function start() {
    cm.sendNext("Hello there~ I am Irene from Singapore Airport. I was transfered to Kerning city to celerbrate new opening of our service! How can I help you?\r\n#L1#I would like to buy a plane ticket to Singapore#l#\r\n#L2#Let me go into the departure point#l#");
}

function action(mode, type, selection) {
    if (mode < 0) {
        cm.dispose();
    } else if (mode == 0) {
        cm.sendOk("I am here for a long time. Please talk to me again when you change your mind.");
        cm.dispose();
    } else {
        status++;
        if (status == 1) {
            chosen = selection;
            if (selection == 1) {
                cm.sendYesNo(" The ticket will cost you 5,000 mesos. Will you purchase the ticket?");
            } else if (selection == 2) {
                cm.sendYesNo(" Would you like to go in now? You will lose your ticket once you go in~ Thank you for choosing Wizet Airline.");
            }
        } else if (status == 2) {
            if (chosen == 1) {
                if (cm.getMeso() > 5000) {
                    cm.gainMeso(-5000);
                    cm.gainItem(4031731, 1);
                } else {
                    cm.sendOk("You need 5,000 mesos. Sorry, but I can't sell you the ticket."); // NOT GMS TEXT. I'm not going to drop my mesos just to get this text.
                }
            } else if (chosen == 2) {
                if (canEnter) {
                    if (cm.haveItem(4031731)) {
                        cm.warp(540010100);
                        cm.gainItem(4031731, -1);
                        cm.dispose();
                    } else {
                        cm.sendOk("Please do purchase the ticket first. Thank you~");
                        cm.dispose();
                    }
                } else {
                    cm.sendNext("We are sorry, but the gate is closed 1 minute before the departure.");
                    cm.dispose();
                }
            }
        }
    }
}