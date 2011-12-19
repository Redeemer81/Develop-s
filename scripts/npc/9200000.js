var status = 0;
function start() 
{
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) 
	{ 
		cm.sendYesNo("Hey and welcome to LocalMS. I'm in charge of giving out an item called Mark of the Beta! This is available for players who played LocalMS before the beta character wipe. Would you like one of these rare and exclusive items?");
	} else if (status == 1)
	{
		if((cm.getPlayer().isBeta()) && (!cm.getPlayer().hasreceivedMOTB()))
		{
			cm.sendOk("Here you go! Thank you for playing beta, and welcome back to LocalMS!");
			cm.gainItem(1002419);
			cm.getPlayer().setreceivedMOTB(true);
		}
		else if((cm.getPlayer().isBeta()) && (cm.getPlayer().hasreceivedMOTB()))
			cm.sendOk("You have already received your Mark of the Beta.");
		else
			cm.sendOk("Unfortunately, you aren't eligible for the Mark of the Beta.");
		cm.dispose();
	} else if (status == -1)
		cm.sendOk("Okey doke, come back whenever you like!");
		cm.dispose();
	}
}