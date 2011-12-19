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
-- Odin JavaScript --------------------------------------------------------------------------------
	Nella - Hidden Street : 1st Accompaniment
-- By ---------------------------------------------------------------------------------------------
	Xterminator
-- Version Info -----------------------------------------------------------------------------------
	1.0 - First Version by Xterminator
---------------------------------------------------------------------------------------------------
**/
importPackage(Packages.server.life);
importPackage(Packages.server);
importPackage(Packages.server.maps);

var status = -1;
var started = false;
var finished = false;
var stage = 0;

var eqArray = new Array(1452060, 1452044, 1472053, 1472052, 1462017, 1462039,
1332051, 1332052, 1332049, 1332050, 1312030, 1312031,
1322045, 1322052, 1302107, 1302059, 1442068, 1442044,
1442045, 1432056, 1432038, 1382060, 1382036, 1412021,
1412026, 1422027, 1422028, 1402035, 1402037, 1372010,
1372032, 1482012, 1482013, 1492012, 1492013, 1092049);  //lvl 100 and 110 weapons


// v   lifted from lolcastle - thanks serp & matze <3
var scrollArray = new Array(	2040001, 2040029, 2040004, 2040025, 2044807, 2044801, 
2043112, 2043101, 2043212, 2043201, 2043017, 2043001, 
2043009, 2040504, 2040501, 2040513, 2040516, 2040532, 
2048012, 2048011, 2048004, 2048013, 2048001, 2048010, 
2044401, 2044412, 2041107, 2041104, 2041110, 2041101, 
2040023, 2040901, 2040927, 2040924, 2040919, 2040931, 
2040914, 2040759, 2040701, 2040704, 2040707, 2044312, 
2044301, 2043801, 2040401, 2040421, 2040413, 2040425, 
2040418, 2044112, 2044101, 2044212, 2044201, 2044012, 
2044001, 2043701, 2041211, 2041317, 2041315, 2041319, 
2041313, 2040605, 2040611, 2040609, 2040607, 2044505, 
2041039, 2041031, 2041037, 2041041, 2041027, 2041033, 
2041035, 2041029, 2044705, 2044605, 2043305, 2040307, 
2040305, 2040309, 2040203, 2040208, 2040108, 2040103,
2040811, 2040809, 2040813, 2040815, 2040015, 2040009, 
2040011, 2040013, 2043105, 2043205, 2043005, 2043007, 
2040511, 2040509, 2040519, 2040521, 2044405, 2041117, 
2041115, 2041119, 2041113, 2040905, 2040909, 2040907, 
2040922, 2040917, 2040713, 2040715, 2040717, 2044305, 
2043805, 2040405, 2040409, 2040411, 2040407, 2044105, 
2044205, 2044005, 2043705, 2040626, 2040327, 2040322, 
2044904, 2040030, 2044808, 2044804, 2043113, 2043213, 
2043018, 2040533, 2044413, 2040932, 2044313, 2040426, 
2044113, 2044213, 2044013, 2049106, 2044905, 2044810, 
2044508, 2044708, 2044608, 2043308, 2040315, 2043108, 
2043208, 2043013, 2044408, 2040912, 2044308, 2043808, 
2044108, 2044208, 2044008, 2043708, 2044906, 2040524, 
2040615, 2044506, 2041054, 2041052, 2041056, 2041042, 
2041046, 2041048, 2041050, 2041044, 2044706, 2044606, 
2043306, 2040313, 2040821, 2040819, 2040019, 2040021, 
2040720, 2048008, 2044813, 2044811, 2043106, 2043206, 
2043011, 2040522, 2040526, 2040528, 2044406, 2044407, 
2040910, 2040718, 2044306, 2040722, 2048006, 2043806, 
2040415, 2044106, 2044206, 2044006, 2043706, 2041316, 
2041314, 2041318, 2041312, 2040604, 2040610, 2040608, 
2040606, 2044504, 2041038, 2041030, 2041036, 2041040, 
2041026, 2041032, 2041034, 2041028, 2044704, 2044604, 
2043304, 2040306, 2040304, 2040308, 2040204, 2040209, 
2040109, 2040104, 2040810, 2040808, 2040812, 2040814, 
2040014, 2040008, 2040012, 2043104, 2043204, 2043004, 
2043006, 2040510, 2040508, 2040518, 2040520, 2044404, 
2041116, 2041114, 2041118, 2041112, 2040904, 2040908, 
2040906, 2040921, 2040916, 2040712, 2040714, 2040716, 
2044304, 2043804, 2040404, 2040408, 2040410, 2040406, 
2044104, 2044204, 2044004, 2043704, 2040624, 2040325, 
2040320, 2044903, 2040028, 2040010, 2044806, 2044803, 
2043111, 2043211, 2043016, 2040531, 2044411, 2040930, 
2044311, 2040424, 2044111, 2044211, 2044011, 2049105, 
2049110, 2049109, 2049108, 2049107
		);


function start() {
    status = -1;
    var eim = cm.getPlayer().getEventInstance();
    if (eim == null)
       cm.dispose();
    var prop = eim.getProperty("roundStarted");
    if ((prop == null) || (prop.equals("false")))
       started = false;
    else
        started = true;

    prop = eim.getProperty("pqFinished");
    if ((prop == null) || (prop.equals("false")))
       finished = false;
    else
       finished = true;
    action(1,0,0);
}

function action(mode, type, selection){
    if (mode == 1)
        status++;
    else {
        cm.dispose();
        return;
    }
    var eim = cm.getPlayer().getEventInstance();
    stage = eim.getProperty("stage");
    var mapId = cm.getPlayer().getMapId();

        if ((status == 0) && (finished)) {
                var outText = "Nice one! Now you MUST have your equip and use inventories with at one free space in order two receive your two prizes. Once you've done this, go ahead and hit 'yes' and claim your prize!";
                cm.sendYesNo(outText);
        } else if ((status == 1) && (finished))
        {
            
            if (eim != null)
            {
                eim.removePlayer(cm.getPlayer());             //this will warp them out, so we don't have to worry about it
                givePrize();
            }  else {
                cm.warpMap(100000000); // Warp player, in case something has gone DRASTICALLY wrong somewhere
            }

                             //deregisterparty here
            cm.dispose();
        }

        if ((status == 0) && (started)) {
            var outText = "Once you leave the map, you'll have to restart the whole quest if you want to try it again.  Do you still want to leave this map?";
            cm.sendYesNo(outText);
        } else if ((mode == 1) && (started)) { //get me out of here!

            if (eim == null)
            {
                cm.warp(221000000); // Warp player, in case something has gone DRASTICALLY wrong somewhere
                cm.dispose();
            }
            else if (cm.isLeader()) {
                cm.getEventManager("TTPQ").setProperty("TTPQOpen" , "true");
                eim.disbandParty();
            }
            else
                eim.leftParty(cm.getPlayer());

            cm.dispose();
        }

         if(stage.equals("0"))
              {
                  if ((status == 0) && (!started) && (cm.isLeader())) {
                    var outText = "Hello and welcome to Time Temple PQ. The PQ takes place over 4 stages - two are monster invasion rounds, where you will face 5 waves of really tough monsters! Afterwards, you'll face a selection of pretty nasty bosses, before a final showdown! Dare you take on the challenge?";
                    cm.sendYesNo(outText);
                } else if ((status == 0) && (!started) && (!cm.isLeader())) {
                    var outText = "Welcome, daring adventurers! Please get your leader to speak with me to begin the challenge!";
                    cm.sendOk(outText);
                    cm.dispose();
                } else if ((mode == 1) && (!started) && (cm.isLeader())) {
                  
                  cm.sendOk("Good luck! You'll need it!");
                  eim.setProperty("roundStarted", "true");
                  eim.schedule("spawnWave", 1000);
                  cm.dispose();
                }
        }
        else if(stage.equals("1"))
              {
                if ((status == 0) && (!started) && (cm.isLeader())) {
                    var outText = "Thought that was tough? There's a long way to go yet! The next stage involves another 5 waves of monsters. You ready?";
                    cm.sendYesNo(outText);
                } else if ((status == 0) && (!started) && (!cm.isLeader())) {
                    var outText = "Good job on beating the first round! Get your leader to speak to me and we'll begin the second!";
                    cm.sendOk(outText);
                    cm.dispose();
                } else if ((mode == 1) && (!started) && (cm.isLeader())) {
                  
                  cm.sendOk("Here we go!");
                  eim.setProperty("roundStarted", "true");
                  eim.schedule("spawnWave", 1000);
                  cm.dispose();
                }
              }
        else if(stage.equals("2"))
              {
                if ((status == 0) && (!started) && (cm.isLeader())) {
                    var outText = "That was just the easy part! The next round involves loads of really tough bosses - are you ready?";
                    cm.sendYesNo(outText);
                } else if ((status == 0) && (!started) && (!cm.isLeader())) {
                    var outText = "Tired yet? The battles are only just beginning! Get your leader to talk to me and we'll start the boss round!";
                    cm.sendOk(outText);
                    cm.dispose();
                } else if ((mode == 1) && (!started) && (cm.isLeader())) {
                  
                  cm.sendOk("Here we go!");
                  eim.setProperty("roundStarted", "true");
                  eim.schedule("spawnNextBoss", 1000);
                  cm.dispose();
                }
              }
        else if(stage.equals("3"))
              {
                if ((status == 0) && (!started) && (cm.isLeader())) {
                    var outText = "Wow - I'm impressed! Now the very last stage is tough. Ready to face Zakum?";
                    cm.sendYesNo(outText);
                } else if ((status == 0) && (!started) && (!cm.isLeader())) {
                    var outText = "And now for the final showdown! Get your leader to speak to me and we'll continue!";
                    cm.sendOk(outText);
                    cm.dispose();
                } else if ((mode == 1) && (!started) && (cm.isLeader())) {

                  cm.sendOk("Here we go!");
                  eim.setProperty("roundStarted", "true");
                  eim.schedule("spawnFinalBoss", 1000);
                  cm.dispose();
                }
              }
         else
         {
              var outText = "ERROR: STAGE NOT RECOGNISED";
                    cm.sendOk(outText);
                    cm.dispose();
         }
}

function givePrize()
{
  var EQal = eqArray.length;
  var equip = Math.round(Math.random()*(EQal-1));
  cm.gainItem(eqArray[equip]);
  
  for(var i = 0; i < 4; i++)
  {
   var SCal = scrollArray.length;
   var Scroll = Math.round(Math.random()*(SCal-1));
   cm.gainItem(scrollArray[Scroll]);
  }
}


