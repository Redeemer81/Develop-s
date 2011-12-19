/*
	Claw Machine
	Designed as Zeds Roulette Box for prizes
	NPC ID: 9201067
	Location: Wherever Zed Spawns It
*/

/* Takes Item:
	4031325 - Secret Coca-Cola Liquid
*/

/* Current Item List: 
	Stormcaster Gloves, Sirius Cape, Pink Adventuerer Cape, Purple Adventurer Cape, Black Strap Shoes, Violet Snowshoes, Facestompers, Crystal Illbi,
	Onyx Apple, Bosshunter Greaves, Bosshunter Boots, Bosshunter Helm, Bosshunter Faceguard, Bosshunter Gi,
	Bosshunter Armor, Infinity Circlet, Crystal Leaf Earrings, Gachapon Ticket. Lord Pirate's Hat, Spiegelmann Necklace, Mark of the Beta, Astral Blade,
	Cosmic Scepter, Crescent Moon, Heavenly Messenger, Celestial Staff, Andromeda Bow, Void Hunter, Black Hole, Nebula Dagger, Black Phoenix Shield, Dark Shard Earrings,
	Zeta Cape, Antellion Miter, Neva, Tiger's Fang, Winkel, Sword Earrings, Shield Earrings, Admin's Congrats, Cokeplay Shield, Versalmas Hat, Maplemas Hat
*/

var allitems = Array(1082223, 1102145, 1102041, 1102042, 1072262, 1072238, 1072344, 2070016, 2022179,
						1072342, 1072345, 1072343, 1072346, 1002739, 1002750, 1052149, 1052152, 1052148, 1052151, 1002676,
						1032048, 5220000, 1002571, 1002572, 1002573, 1002574, 1122007, 1002419, 1302079, 1322059, 1442060,
						1382053, 1452052, 1462046, 1472062, 1332064, 1332065, 1092052, 1032049, 1102146, 1002675, 1472064,
						1402045, 1452053, 1032030, 1032031, 2022118, 1092035, 1002716, 1002717);

var selitems = Array();

// The ItemID of the item needed to get a prize 
var prizeItem = 4031325; 

var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
	getItems();
	//listItems();
}


function action(mode, type, selection) {
	
	if(mode <= 0) {
		cm.dispose();
	} else {
	
		if(mode == 1)
			status++;
		else
			status--;
		
		
		if(status == 0) {				// Introduction
			//cm.sendNext("Welcome to the roulette wheel. Here you will be given a random selection of prizes with the chance at winning one of them. If you do not like the selection you can always click #rEnd Chat#k and click me again for a new selection of prizes.\r\n\r\nTo use me you must first obtain a\r\n#v " + prizeItem + "# #d#t" + prizeItem + "##k from a GM or event."); 
			cm.sendNext("#e[#n#dAs you are progressing down your current path you catch a glimpse of what would appear to be an odd machine. The machine consists of many levers and gears and you wonder what its purpose could be. To the left of the machine you notice a sign.#k#e]#n");
		
		} else if(status == 1) {		// Rules of the machine
			status = 2;
			cm.sendNext("#e[#n#dThe sign is old and worn by the weather, however you are able to make out what it says quite clearly.#k#e]#n.\r\n\r\n#eThe Sign Reads#n\r\n#e#bClaw Machine#k#n\r\n#rUsage#k - To use this machine you must be in the posession of a #r#t" + prizeItem + "##k. They can be obtained through events or as prizes from GMs.\r\n\r\n#rRules#k - This machine will show you six items that are randomly selected from its item compartment. These items are potential items that you can win as a prize.  You will only win one item from the machine at a time. Once you place the #r#t" + prizeItem + "##k into the machine, the machine will select your prize. If you do not like the potential prize choices you can always press \"#eEnd Chat#n\" and get a new random selection of items.\r\n\r\n#eRequired:#n #v" + prizeItem + "##r#t" + prizeItem + "##k (x1)");
		
		} else if(status == 3) {		// Item Display
			var output = "";
			
			for(i = 0; i < selitems.length; i++) {
				if(i % 2 == 0) 
					output += "\t#v" + selitems[i] + "#\t#k#z" + selitems[i] + "##k\r\n";
				else
					output += "\t#v" + selitems[i] + "#\t#b#z" + selitems[i] + "##k\r\n";
			}
		
			cm.sendYesNo("#e[#n#dYou press your face up to the glass and peer inside the machine. Six items are displayed in front of you, tantalizing you with their prescense.#k#e]#n\r\n\r\n#eItem Selection#n\r\n" + output + "\r\nWould you like to give it a shot to win one of the displayed items?");
		
		} else if(status == 4) {		// Item Check (prizeItem)
			
			if(!cm.haveItem(prizeItem)) {
				status = 20;
				cm.sendOk("#e[#n#dYou reach into your pocket but alas realize that you do not have a #t" + prizeItem + "# to spare. Your face slumps off the machine as you turn in despair. The items have eluded you this time, but perhaps a brighter future awaits you.#k#e]#n");
			} else {
				cm.sendNext("#e[#n#dYou reach into your pocket and emerge your hand with a #t" + prizeItem + "# for the machine. You place it into the machine and in an instant a flash of light blinds you. You face jerks away from the machine only to be left with the sounds of gears clanking and whirring inside the machine...#k#e]#n");
			}
		
		} else if(status == 5) {
			var reward = selitems[Math.floor(Math.random() * selitems.length)];
			cm.gainItem(prizeItem, -1);
			cm.gainItem(reward, 1);
			cm.sendNext("#e[#n#dThe noise of the machine has stopped as your vision starts to come back into a soft focus. You notice a small door on the machine has opened up with a small object glimmering inside of it. You reach your hand inside the small door...#k#e]#n\r\n\r\n#eYou have gained a #b#t" + reward + "##k.");
			
		} else {
			cm.dispose();
		}
	
	}
}



/* Populates the selected items array with six unique items from the allitems array */
function getItems() {
		
	while(selitems.length < 6) {
		var item = allitems[Math.floor(Math.random() * allitems.length)];
		
		if(!hasItem(item)) {
			selitems.push(item);
		}
	}
}


/* Determines if an item already exists in the selected items array */
function hasItem(id) {

	for(i = 0; i < selitems.length; i++) {
		if(selitems[i] == id)
			return true;
	}
	
	return false;
}
