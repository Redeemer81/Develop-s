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

/* Regular Cab */

var status = 0;
var maps = [100000000, 101000000, 102000000, 103000000, 104000000, 105000000, 120000000];
var selectedMap = -1;

function start() {
    cm.sendNext("안녕하세요~! 빅토리아 중형택시입니다. 다른 마을로 안전하고 빠르게 이동하고 싶으신가요? 그렇다면 고객만족을 최우선으로 생각하는 #b빅토리아 중형택시#k를 이용해 보세요. 싼 가격으로 원하시는 곳까지 친절하게 모셔다 드리고 있습니다.");
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status == 1 && mode == 0) {
            cm.dispose();
            return;
        } else if (status >= 2 && mode == 0) {
            cm.sendNext("There's a lot to see in this town, too. Come back and find us when you need to go to a different town.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1) {
            var selStr = "";
            if (cm.getJobId() == 0)
                selStr += "초보자에게는 특별히 90% 세일 가격으로 모시고 있습니다. ";
            selStr += "목적지를 선택해 주세요.#b";
            for (var i = 0; i < maps.length; i++){
                if (cm.getPlayer().getMapId() != maps[i])
                    selStr += "\r\n#L" + i + "##m" + maps[i] + "# (1000 메소)#l";
            }
            cm.sendSimple(selStr);
        } else if (status == 2) {
            cm.sendYesNo("이곳에서 더 이상 볼일이 없으신 모양이로군요. 정말로 #b#m" + maps[selection] + "##k마을로 이동하시겠습니까? 가격은 #b1000 메소 입니다#k.");
            selectedMap = selection;
        } else if (status == 3) {
            if (cm.getMeso() < 1000) {
                cm.sendNext("메소가 부족하시군요. 죄송하지만 요금을 지불하지 않으면 저희 택시를 이용하실 수 없습니다.");
                cm.dispose();
                return;
            }
			cm.gainMeso(-1000);
            cm.warp(maps[selectedMap], 0);
            cm.dispose();
        }
    }
}