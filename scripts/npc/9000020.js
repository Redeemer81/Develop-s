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
/*
 * @Name: Cody
 * @NPC ID: 9200000
 * @Author: MrXotic
 * @Author: XxOsirisxX
 * @Author: Moogra
 */

var status = -1;
var possibleJobs = new Array();
var maps = [
/*마을*/[100000000, 101000000, 102000000, 103000000, 104000000, 105000000, 120000000, 200000000, 211000000, 220000000, 250000000, 251000000, 261000000, 240000000],
/*MonsterMaps*/[100040001, 101010100, 104040000, 103000101, 103000105, 101030110, 106000002, 101030103, 101040001, 101040003, 101030001, 104010001, 105070001, 105090300, 105040306, 230020000, 230010400, 211041400, 222010000, 220080000, 220070301, 220070201, 220050300, 220010500, 250020000, 251010000, 200040000, 200010301, 240020100, 240040500, 240040000, 600020300, 801040004, 800020130, 800020400],
/*Towns*/[130000000, 300000000, 1010000, 680000000, 230000000, 101000000, 211000000, 100000000, 251000000, 103000000, 222000000, 104000000, 240000000, 220000000, 250000000, 800000000, 600000000, 221000000, 200000000, 102000000, 801000000, 105040300, 610010004, 260000000, 540010000, 120000000]];
var jobA = false;
var warper = false;
var giveMS = false;
var beauty = false;
var job;
var newJob;
var chosenMap = -1;
var chosenSection = -1;
var fourthJob = false;

function start() {
    cm.sendSimple("#fUI/UIWindow.img/QuestIcon/3/0#\r\n#b#L0#세계 여행#l\r\n#L1#직업 전직#l\r\n#L2#고성능 확성기 지급#l\r\n#L3#뷰티샵#l");
}

function action(mode, type, selection) {
    status++;
    if(mode != 1){
        cm.dispose();
        return;
    }
    if (!jobA && !warper)
        if (selection == 1)
            jobA = true;
        else if (selection == 0)
            warper = true;
		else if (selection == 2)
			giveMS = true;
		else if (selection == 3)
			beauty = true;
    if (jobA)
        jobAdv(selection);
    else if (warper)
        warp(selection);
	else if (giveMS)
		giveMessenger(selection);
	else if (beauty)
		beautyShop(selection);
}

function beautyShop(selection){
	if (status == 0) {
		cm.sendNext("준비 중 입니다.");
		cm.dispose();
		return;
	}
}

function giveMessenger(selection){
	if (status == 0) {
		cm.sendNext("지금은 받을 수 없습니다.");
		cm.dispose();
		return;
		if (cm.haveItem(5072000)) {
			cm.sendNext("이미 고성능 확성기를 가지고 계시네요.");
			cm.dispose();
			return;
		}
		cm.sendNext("지급해드렸습니다. #r도배하실경우 경고 및 제재#k를 당하실 수 있으니 주의하세요~");
		cm.gainItem(5072000, 10);
		cm.dispose();
	}
}

function warp(selection){
    if (status == 0)
        cm.sendSimple("#fUI/UIWindow.img/QuestIcon/3/0#\r\n#b#L0#마을#l");
    else if (status == 1) {
        chosenSection = selection;
        var selStr = "어느 마을로 가시겠습니까?#b";
        for (var i = 0; i < maps[selection].length; i++)
            selStr += "\r\n#L" + i + "##m" + maps[selection][i] + "#";
        cm.sendSimple(selStr);
    } else if (status == 2) {
        chosenMap = selection;
        cm.sendYesNo("정말로 #b#m" + maps[chosenSection][selection] + "##k 마을로 이동하시겠습니까?");
    } else if (status == 3) {
        cm.warp(maps[chosenSection][chosenMap]);
        cm.dispose();
    }
}

function jobAdv(selection){
    if (status == 0) {
        newJob = cm.getJobId() + 1;
        if (cm.getJobId() % 10 == 2) {
            cm.sendOk("이미 전직을 다 하셨습니다.");
            cm.dispose();
        } else if (cm.getJobId() % 10 >= 0 && cm.getJobId() % 100 != 0) {
            var secondJob = cm.getJobId() % 10 == 0;
			var thirdJob = cm.getJobId() % 10 == 1;
            if ((secondJob && cm.getLevel() < 70) || (!secondJob && cm.getLevel() < 120)) {
                cm.sendOk("아직 전직을 할 수 없으시네요.");
                cm.dispose();
            } else {
				if (thirdJob)
					fourthJob = true;
                cm.sendYesNo("레벨이 " + cm.getLevel() + " 이시네요. #b"+cm.getJobName(newJob)+"#k 직업으로 전직하시겠습니까?");
			}
        } else {
            if (cm.getJobId() % 1000 == 0) {
                if (cm.getLevel() >= 10) 
                    for (var i = 1; i < 6; i++) 
                        possibleJobs.push(cm.getJobId() + 100 * i);
                else if (cm.getLevel() >= 8)
                    possibleJobs.push(cm.getJobId() + 200);
            } else if (cm.getLevel() >= 30) {
                switch (cm.getJobId()) {
                    case 100:
                    case 200:
                        possibleJobs.push(cm.getJobId() + 30);
                    case 300:
                    case 400:
                    case 500:
                        possibleJobs.push(cm.getJobId() + 20);
                    case 1100:
                    case 1200:
                    case 1300:
                    case 1400:
                    case 1500:
                        possibleJobs.push(cm.getJobId() + 10);
                        break;
                }
            }
            if (possibleJobs.length == 0) {
                cm.sendOk("아직 전직을 할 수 없으시네요.");
                cm.dispose();
            } else {
                var text = "어떤 직업으로 전직하시겠습니까?#b";
                for (var j = 0; j < possibleJobs.length; j++)
                    text += "\r\n#L"+j+"#"+cm.getJobName(possibleJobs[j])+"#l";
                cm.sendSimple(text);
            }
        }
    } else if (status == 1 && cm.getJobId() % 100 != 0) {
        cm.changeJobById(cm.getJobId() + 1);
		if (fourthJob)
			cm.maxSkills(cm.getJobId());
        cm.dispose();
    } else if (status == 1) {
        cm.changeJobById(possibleJobs[selection]);
		if (cm.getJobId() % 10 == 0) {
			if (cm.getJobId() == 100) {
				cm.gainItem(1302077);
				cm.resetStats();
			} else if (cm.getJobId() == 200) {
				cm.gainItem(1372043);
				cm.resetStats();
			} else if (cm.getJobId() == 300) {
				cm.gainItem(1452051);
				cm.gainItem(2060000, 1000);
				cm.gainItem(2060000, 1000);
				cm.resetStats();
			} else if (cm.getJobId() == 400) {
				cm.gainItem(1472061);
				cm.gainItem(2070015, 500);
				cm.gainItem(2070015, 500);
				cm.resetStats();
			} else if (cm.getJobId() == 500) {
				cm.gainItem(1482014);
				cm.gainItem(1492014);
				cm.gainItem(2330006, 500);
				cm.gainItem(2330006, 500);
				cm.resetStats();
			}
			cm.gainItem(5072000, 10);
			cm.gainItem(5076000, 5);
		}
        cm.dispose();
    } else if (status == 2) {
        job = selection;
        cm.sendYesNo("Are you sure you want to job advance?");
    } else if (status == 3) {
        cm.changeJobById(possibleJobs[job]);
        cm.dispose();
    }
}