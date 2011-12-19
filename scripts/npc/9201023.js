/* Simple Reborn NPC */

//function start() {
	cm.sendSimple("Hello, #h #. I am god. Are you interested in being reborn? \r\n\r\n#b#L0#Yes, Oh mighty lord please allow me to be reborn!#l\r\n#L1#GOD DOESN'T EXIST!!#l#k");
}

function action(mode, type, selection) {
	cm.dispose();
	if (selection == 0) {
		if (cm.getLevel() >= 200) {
			cm.doReborn();
			cm.dispose();
		} else {
			cm.sendNext("You're not ready to be reborn yet!");
			cm.dispose();
		}
	} else if (selection == 1) {
		cm.dispose();
	}
}
