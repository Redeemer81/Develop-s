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
    cm.sendNext("�ȳ��ϼ���~! ���丮�� �����ý��Դϴ�. �ٸ� ������ �����ϰ� ������ �̵��ϰ� �����Ű���? �׷��ٸ� �������� �ֿ켱���� �����ϴ� #b���丮�� �����ý�#k�� �̿��� ������. �� �������� ���Ͻô� ������ ģ���ϰ� ��Ŵ� �帮�� �ֽ��ϴ�.");
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
                selStr += "�ʺ��ڿ��Դ� Ư���� 90% ���� �������� ��ð� �ֽ��ϴ�. ";
            selStr += "�������� ������ �ּ���.#b";
            for (var i = 0; i < maps.length; i++){
                if (cm.getPlayer().getMapId() != maps[i])
                    selStr += "\r\n#L" + i + "##m" + maps[i] + "# (1000 �޼�)#l";
            }
            cm.sendSimple(selStr);
        } else if (status == 2) {
            cm.sendYesNo("�̰����� �� �̻� ������ ������ ����̷α���. ������ #b#m" + maps[selection] + "##k������ �̵��Ͻðڽ��ϱ�? ������ #b1000 �޼� �Դϴ�#k.");
            selectedMap = selection;
        } else if (status == 3) {
            if (cm.getMeso() < 1000) {
                cm.sendNext("�޼Ұ� �����Ͻñ���. �˼������� ����� �������� ������ ���� �ýø� �̿��Ͻ� �� �����ϴ�.");
                cm.dispose();
                return;
            }
			cm.gainMeso(-1000);
            cm.warp(maps[selectedMap], 0);
            cm.dispose();
        }
    }
}