/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/**
 * @author Flav
 */
function start() {
    status = 0;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    var altar = cm.getMap(280030000);
    var mc = cm.getPlayer();

    if (mode == 1) {
        status++;
    } else {
        status--;

        if (mode < 1) {
            cm.dispose();
            return;
        }
    }
    if (status == 1) {
        if (altar.getMapleSquad() == null) {
            cm.sendYesNo("No Zakum Squad has been created yet. Do you want to create a squad?");
        } else {
            if (mc.getName().equals(altar.getMapleSquad().getLeader()) || altar.getMapleSquad().getMembers().contains(mc)) {
                if (altar.getMapleSquad().isClosed()) {
                    if (mc.getName().equals(altar.getMapleSquad().getLeader()))
                        cm.sendSimple("What do you want to do?\r\n\r\n#b#L0#Enter Zakum's Altar.#l\r\n#L1#View the squad members.#l\r\n#L3#Open the squad.#l\r\n#L4#Delete the squad.#l");
                    else
                        cm.sendSimple("What do you want to do?\r\n\r\n#b#L0#Enter Zakum's Altar.#l\r\n#L1#View the squad members.#l\r\n#L2#Leave the squad.#l");
                } else {
                    if (mc.getName().equals(altar.getMapleSquad().getLeader()))
                        cm.sendSimple("What do you want to do?\r\n\r\n#b#L1#View the squad members.#l\r\n#L3#Close the squad.#l\r\n#L4#Delete the squad.#l");
                    else
                        cm.sendSimple("What do you want to do?\r\n\r\n#b#L1#View the squad members.#l\r\n#L2#Leave the squad.#l");
                }
            } else {
                if (altar.getMapleSquad().isClosed())
                    cm.sendNext("The squad is closed.");
                else
                    cm.sendYesNo("Do you want to sign up for the squad?");
            }
        }
    } else if (status == 2) {
        if (altar.getMapleSquad() == null) {
            altar.createMapleSquad(mc.getName());
            cm.sendNext("The squad has been created.");
        } else if (!mc.getName().equals(altar.getMapleSquad().getLeader()) && !altar.getMapleSquad().getMembers().contains(mc)) {
            if (!altar.getMapleSquad().isClosed()) {
                if (altar.getMapleSquad().addMember(mc))
                    cm.sendNext("You have signed up for the squad.");
                else
                    cm.sendNext("You have been banned form the squad.");
            }
        } else {
            if (selection == 0) {
                if (!mc.getName().equals(altar.getMapleSquad().getLeader()) && !altar.equals(altar.getMapleSquad().findLeader().getMap()))
                    cm.sendNext("The squad leader has to enter first.");
                else
                    cm.warp(altar.getId());
            } else if (selection == 1) {
                members = altar.getMapleSquad().getMembers();
                var s = "The following members make up your squad:\r\n\r\n#L0#" + altar.getMapleSquad().getLeader() + "#l"

                for (var i = 0; i < members.size(); i++) {
                    s += "\r\n#L" + (i + 1) + "#" + members.get(i).getName() + "#l";
                }

                cm.sendSimple(s);
            } else if (selection == 2) {
                altar.getMapleSquad().removeMember(mc);
                cm.sendNext("You have left the squad.");
            } else if (selection == 3) {
                if (altar.getMapleSquad().isClosed()) {
                    altar.getMapleSquad().close(false);
                    cm.sendNext("You have opened the squad.");
                } else {
                    altar.getMapleSquad().close(true);
                    cm.sendNext("You have closed the squad.");
                }
            } else {
                altar.deleteMapleSquad();
                cm.sendNext("You have deleted the squad.");
            }
        }

        if (selection != 1 || !mc.getName().equals(altar.getMapleSquad().getLeader()))
            cm.dispose();
    } else if (status == 3) {
        sel = selection;

        if (sel != 0) {
            sel--;
            cm.sendYesNo("Do you want to ban " + members.get(sel).getName() + " from the squad?");
        } else {
            cm.dispose();
        }
    } else {
        cm.sendNext(members.get(sel).getName() + " has been banned from the squad.");
        altar.getMapleSquad().banMember(members.get(sel));
        cm.dispose();
    }
}