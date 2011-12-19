var text;
var pay = 4000038;
function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
    if (status == 0) {
            cm.sendSimple("Hey, I'm the LocalMS Event Item Trader. \r\nIf you have a #bEvent Trophy#k I can give you your prize! They look like this: \r\n #i4000038 \r\n\r\n What would you like?: \r\n#L0#GM Scroll for Glove Attack#l \r\n#L1#Inferno Dragon Chair#l\r\n#L2#Abyss Dragon Chair#l\r\n");
       } else if (status == 1) {
            if (selection === 0) {
                if (cm.haveItem(pay, 3)) {
	            cm.gainItem(4000038, -3); 
                    cm.gainItem(2040830, 1); // Scroll for Glove for Attack
                    cm.sendOk("Thank you !");
		    cm.dispose();
                } else {
                cm.sendOk("Sorry, it does not look like you have #b3 Event Trophies#k");
                cm.dispose();
            }

            }  else if (selection == 1) {
                if (cm.haveItem(pay, 1)) {
                    cm.gainItem(pay, -1); 
                    cm.gainItem(3010046, 1); // Inferno Dragon Chair
                    cm.sendOk("Thank you !");
		    cm.dispose();
                } else {
                cm.sendOk("Sorry, it does not look like you have #b1 Event Trophy#k");
                cm.dispose();
            }

            }  else if (selection == 2) {
                if (cm.haveItem(pay, 40)) {
                    cm.gainItem(3010047, 1); // Abyss Dragon Chair
                    cm.gainItem(pay, -1); 
                    cm.sendOk("Thank you !");
		    cm.dispose();
                } else {
                cm.sendOk("Sorry, it does not look like you have #b1 Event Trophy#k");
                cm.dispose();
            }
            } else {
                cm.sendOk("See you around!");
                cm.dispose();
}
}
}
}
