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

//modify by shaun166 to work on new command system pls don delete 

package client.messages.commands;

import client.MapleCharacter;
import client.messages.CommandDefinition;
import client.messages.Command;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import client.MapleClient;
import client.MapleJob;
import client.MapleStat;
import client.SkillFactory;
import net.channel.ChannelServer;
import net.MaplePacket;
import tools.MaplePacketCreator;
import tools.StringUtil;
import server.MapleShopFactory;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.MaplePortal;
import server.maps.HiredMerchant;
import net.channel.ChannelServer;
import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;
import static client.messages.CommandProcessor.getOptionalIntArg;




public class customCommands implements Command {


	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,
                IllegalCommandSyntaxException {
            MapleCharacter player = c.getPlayer();
            ChannelServer cserv = c.getChannelServer();
            if (splitted[0].equals("!spy")) {
                            double var;double var2;int str; int dex;int intel; int luk; int meso; int maxhp; int maxmp;
                               MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                                 str = victim.getStr();    dex = victim.getDex();  intel = victim.getInt();   luk = victim.getLuk();   meso = victim.getMeso(); maxhp = victim.getCurrentMaxHp();maxmp = victim.getCurrentMaxMp();                                                            
                                 mc.dropMessage("Players stats are:");
                                 mc.dropMessage(" Str: "+ str+", Dex: "+ dex+ ", Int: " + intel + ", Luk: "+ luk +" .");
                                 mc.dropMessage("Player has "+ meso + "mesos.");
                                 mc.dropMessage("Max hp is" + maxhp + " Max mp is" + maxmp + ".");
              } else if (splitted[0].equals("!setall")) {
                            int max = Integer.parseInt(splitted[1]);
                            player.setStr(max);
                            player.setDex(max);
                            player.setInt(max);
                            player.setLuk(max);                         
			    player.setMaxMp(max);
                            player.setMaxHp(max);			    
                            player.updateSingleStat(MapleStat.STR, player.getStr());
                            player.updateSingleStat(MapleStat.DEX, player.getStr());
                            player.updateSingleStat(MapleStat.INT, player.getStr());
                            player.updateSingleStat(MapleStat.LUK, player.getStr());
                            player.updateSingleStat(MapleStat.MAXHP, player.getStr());
			    player.updateSingleStat(MapleStat.MAXMP, player.getStr());
                            
              
           }   else if (splitted[0].equals("!giftnx")) {
                             MapleCharacter victim1 = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                             int points = Integer.parseInt(splitted[2]);
                            	victim1.modifyCSPoints(0, points);
                                mc.dropMessage("NX cash has been given successfully to " + splitted[1]);
            } else if (splitted[0].equals("!hide")) {
                      MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                        if (victim != null) {
                            SkillFactory.getSkill(5101004).getEffect(1).applyTo(victim);
                        }
             } else if (splitted[0].equals("!heal")) {              
      			player.setHp(player.getMaxHp());
       			player.updateSingleStat(MapleStat.HP, player.getMaxHp());
       			player.setMp(player.getMaxMp());
       			player.updateSingleStat(MapleStat.MP, player.getMaxMp()); 
             } else if (splitted[0].equals("!kill")) {
                        MapleCharacter victim1 = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                        victim1.setHp(0);
                        victim1.setMp(0);
                        victim1.updateSingleStat(MapleStat.HP, 0);
                        victim1.updateSingleStat(MapleStat.MP, 0);
       //for (MapleCharacter chr : c.getPlayer().getMap().getCharacters()) {
             } else if (splitted[0].equals("!killhere")) {
               for (MapleCharacter mch : c.getPlayer().getMap().getCharacters()) {
                   if (mch != null) {
                        mch.setHp(0);
                        mch.setMp(0);
                        mch.updateSingleStat(MapleStat.HP, 0);
                        mch.updateSingleStat(MapleStat.MP, 0);
                   }
                }
            } else if (splitted[0].equals("!dcall")) {
    			for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()){
				mch.getClient().getSession().close();
					mch.getClient().disconnect();
                                }
            } else if (splitted[0].equals("!jobperson")) {
                              MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);  
                             victim.changeJob(MapleJob.getById(getOptionalIntArg(splitted, 2, 2)));
   
       		}	else if (splitted[0].equals("!mesos")){
                            c.getPlayer().gainMeso(Integer.parseInt(splitted[1]), true); 
                } else if (splitted[0].equals("!gmshop")) {
                 MapleShopFactory.getInstance().getShop(1337).sendShop(c);
         }
         else if (splitted[0].equalsIgnoreCase("!setRates"))
        {
            double[] newRates = new double[4];
            if(splitted.length != 5)
            {
                c.getPlayer().dropMessage("!setrates syntax: <EXP> <DROP> <BOSSDROP> <MESO>. If field is unneeded, put -1 so for example for just an EXP rate change: !setrates 50 -1 -1 -1. Negative numbers multiply base EXP rate so for 2x EXP do !setrates -2 -1 -1 -1.");
                return;
            } else
            {
                for(int i = 1; i < 5; i++)
                {
                    try
                    {
                        int rate = Integer.parseInt(splitted[i]);
                        newRates[i - 1] = rate;
                    } catch (NumberFormatException nfe)
                    {
                        c.getPlayer().dropMessage("There was an error with one of the arguments provided. Please only use numeric values.");
                        return;
                    }
                }
            }
            try
            {
                c.getChannelServer().getWorldInterface().changeRates(newRates[0], newRates[1], newRates[2], newRates[3]);
            } catch(Exception e){
            c.getChannelServer().reconnectWorld();
            }
        } else if (splitted[0].equals("!giftpoints")) {
             int delta = 0;
             if(splitted.length != 3)
             {
                 player.dropMessage("Syntax helper: !giftpoints <name> <amount>");
             }
             try
             {
                 delta = Integer.parseInt(splitted[2]);
             } catch (NumberFormatException nfe)
             {
                 player.dropMessage("Incorrect parameter - please ensure you abide to the syntax !giftpoints <name> <amount>");
             }
             MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if(victim != null)
                victim.setPoints(victim.getPoints() + delta);
            else
                player.dropMessage("Player " + splitted[1] + " not found.");
         } else if (splitted[0].equals("!chattype")) {
            player.toggleGMChat();
            player.message("You now chat in " + (player.getGMChat() ? "white." : "black."));
        } else if (splitted[0].equals("!cleardrops")) {
            player.getMap().clearDrops(player, true);
        } else if (splitted[0].equals("!pwn")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                MapleMap map = c.getPlayer().getMap();
                MaplePortal tp = map.getPortal(0);
                for(int i = 0; i < 20; i++) {
                    victim.changeMap(map, tp);
                }
            } else if (splitted[0].equals("!speak")) {
                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim == null) {
                    mc.dropMessage("unable to find '" + splitted[1] + "'");
                } else {
                    victim.getMap().broadcastMessage(MaplePacketCreator.getChatText(victim.getId(), StringUtil.joinStringFrom(splitted, 2), victim.isGM(), 0));
                }


            }  else if (splitted[0].equals("!smega")) {
                if (splitted.length == 1) {
                    mc.dropMessage("Usage: !smega [name] [type] [message], where [type] is love, cloud, or diablo.");
                }

                MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);

                String type = splitted[2];
                int channel = victim.getClient().getChannel();
                String text = StringUtil.joinStringFrom(splitted, 3);

                int itemID = 0;

                if (type.equals("love")) {
                    itemID = 5390002;


                } else if (type.equals("cloud")) {
                    itemID = 5390001;


                } else if (type.equals("diablo")) {
                    itemID = 5390000;


                } else {
                    mc.dropMessage("Invalid type (use love, cloud, or diablo)");
                    return;
                }

                String[] lines = {"", "", "", ""};

                if (text.length() > 30) {
                    lines[0] = text.substring(0, 10);
                    lines[1] = text.substring(10, 20);
                    lines[2] = text.substring(20, 30);
                    lines[3] = text.substring(30);
                } else if (text.length() > 20) {
                    lines[0] = text.substring(0, 10);
                    lines[1] = text.substring(10, 20);
                    lines[2] = text.substring(20);
                } else if (text.length() > 10) {
                    lines[0] = text.substring(0, 10);
                    lines[1] = text.substring(10);
                } else if (text.length() <= 10) {
                    lines[0] = text;
                }

                LinkedList list = new LinkedList();
                list.add(lines[0]);
                list.add(lines[1]);
                list.add(lines[2]);
                list.add(lines[3]);
                try {
                    MaplePacket mp = MaplePacketCreator.getAvatarMega(victim, victim.getMedalText(), channel, itemID, list, true);
                    victim.getClient().getChannelServer().getWorldInterface().broadcastMessage(null, mp.getBytes());
                } catch (Exception e) {
                }
        } else if (splitted[0].equalsIgnoreCase("!killmonster")) {
                if (splitted.length == 2) {
                    MapleMap map = c.getPlayer().getMap();
                    double range = Double.POSITIVE_INFINITY;
                    int targetId = Integer.parseInt(splitted[1]);

                    List<MapleMapObject> monsters = map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER));

                    for (MapleMapObject monstermo : monsters) {
                        MapleMonster monster = (MapleMonster) monstermo;
                        if (monster.getId() == targetId) {
                            map.killMonster(monster, player, false);
                            break;
                        }
                    }
                }

            } else if (splitted[0].equals("!music")) { //todo: there's gotta be a better way of doing this
                MapleMap target = c.getPlayer().getMap();
                if (splitted[1].equals("names") && splitted.length == 2) {
                    mc.dropMessage("Names are case sensitive!");
                    mc.dropMessage("Use !song names [Folder Name] to get list of songs.");
                    mc.dropMessage("Folder Names:");
                    mc.dropMessage("Bgm00 | Bgm01 | Bgm02 | Bgm03 | Bgm04");
                    mc.dropMessage("Bgm05 | Bgm06 | Bgm07 | Bgm08 | Bgm09");
                    mc.dropMessage("Bgm10 | Bgm11 | Bgm12 | Bgm13 | Bgm14");
                    mc.dropMessage("    Bgm15 | BgmEvent | BgmGL | BgmJp");
                } else if (splitted[1].equals("names") && splitted.length == 3) {
                    if (splitted[2].equals("Bgm00")) {
                        mc.dropMessage("Bgm00/SleepyWood");
                        mc.dropMessage("Bgm00/FloralLife");
                        mc.dropMessage("Bgm00/GoPicnic");
                        mc.dropMessage("Bgm00/Nightmare");
                        mc.dropMessage("Bgm00/RestNPeace");
                    } else if (splitted[2].equals("Bgm01")) {
                        mc.dropMessage("Bgm01/AncientMove");
                        mc.dropMessage("Bgm01/MoonlightShadow");
                        mc.dropMessage("Bgm01/WhereTheBarlogFrom");
                        mc.dropMessage("Bgm01/CavaBien");
                        mc.dropMessage("Bgm01/HighlandStar");
                        mc.dropMessage("Bgm01/BadGuys");
                    } else if (splitted[2].equals("Bgm02")) {
                        mc.dropMessage("Bgm02/MissingYou");
                        mc.dropMessage("Bgm02/WhenTheMorningComes");
                        mc.dropMessage("Bgm02/EvilEyes");
                        mc.dropMessage("Bgm02/JungleBook");
                        mc.dropMessage("Bgm02/AboveTheTreetops");
                    } else if (splitted[2].equals("Bgm03")) {
                        mc.dropMessage("Bgm03/Subway");
                        mc.dropMessage("Bgm03/Elfwood");
                        mc.dropMessage("Bgm03/BlueSky");
                        mc.dropMessage("Bgm03/Beachway");
                        mc.dropMessage("Bgm03/SnowyVillage");
                    } else if (splitted[2].equals("Bgm04")) {
                        mc.dropMessage("Bgm04/PlayWithMe");
                        mc.dropMessage("Bgm04/WhiteChristmas");
                        mc.dropMessage("Bgm04/UponTheSky");
                        mc.dropMessage("Bgm04/ArabPirate");
                        mc.dropMessage("Bgm04/Shinin'Harbor");
                        mc.dropMessage("Bgm04/WarmRegard");
                    } else if (splitted[2].equals("Bgm05")) {
                        mc.dropMessage("Bgm05/WolfWood");
                        mc.dropMessage("Bgm05/DownToTheCave");
                        mc.dropMessage("Bgm05/AbandonedMine");
                        mc.dropMessage("Bgm05/MineQuest");
                        mc.dropMessage("Bgm05/HellGate");
                    } else if (splitted[2].equals("Bgm06")) {
                        mc.dropMessage("Bgm06/FinalFight");
                        mc.dropMessage("Bgm06/WelcomeToTheHell");
                        mc.dropMessage("Bgm06/ComeWithMe");
                        mc.dropMessage("Bgm06/FlyingInABlueDream");
                        mc.dropMessage("Bgm06/FantasticThinking");
                    } else if (splitted[2].equals("Bgm07")) {
                        mc.dropMessage("Bgm07/WaltzForWork");
                        mc.dropMessage("Bgm07/WhereverYouAre");
                        mc.dropMessage("Bgm07/FunnyTimeMaker");
                        mc.dropMessage("Bgm07/HighEnough");
                        mc.dropMessage("Bgm07/Fantasia");
                    } else if (splitted[2].equals("Bgm08")) {
                        mc.dropMessage("Bgm08/LetsMarch");
                        mc.dropMessage("Bgm08/ForTheGlory");
                        mc.dropMessage("Bgm08/FindingForest");
                        mc.dropMessage("Bgm08/LetsHuntAliens");
                        mc.dropMessage("Bgm08/PlotOfPixie");
                    } else if (splitted[2].equals("Bgm09")) {
                        mc.dropMessage("Bgm09/DarkShadow");
                        mc.dropMessage("Bgm09/TheyMenacingYou");
                        mc.dropMessage("Bgm09/FairyTale");
                        mc.dropMessage("Bgm09/FairyTalediffvers");
                        mc.dropMessage("Bgm09/TimeAttack");
                    } else if (splitted[2].equals("Bgm10")) {
                        mc.dropMessage("Bgm10/Timeless");
                        mc.dropMessage("Bgm10/TimelessB");
                        mc.dropMessage("Bgm10/BizarreTales");
                        mc.dropMessage("Bgm10/TheWayGrotesque");
                        mc.dropMessage("Bgm10/Eregos");
                    } else if (splitted[2].equals("Bgm11")) {
                        mc.dropMessage("Bgm11/BlueWorld");
                        mc.dropMessage("Bgm11/Aquarium");
                        mc.dropMessage("Bgm11/ShiningSea");
                        mc.dropMessage("Bgm11/DownTown");
                        mc.dropMessage("Bgm11/DarkMountain");
                    } else if (splitted[2].equals("Bgm12")) {
                        mc.dropMessage("Bgm12/AquaCave");
                        mc.dropMessage("Bgm12/DeepSee");
                        mc.dropMessage("Bgm12/WaterWay");
                        mc.dropMessage("Bgm12/AcientRemain");
                        mc.dropMessage("Bgm12/RuinCastle");
                        mc.dropMessage("Bgm12/Dispute");
                    } else if (splitted[2].equals("Bgm13")) {
                        mc.dropMessage("Bgm13/CokeTown");
                        mc.dropMessage("Bgm13/Leafre");
                        mc.dropMessage("Bgm13/Minar'sDream");
                        mc.dropMessage("Bgm13/AcientForest");
                        mc.dropMessage("Bgm13/TowerOfGoddess");
                    } else if (splitted[2].equals("Bgm14")) {
                        mc.dropMessage("Bgm14/DragonLoad");
                        mc.dropMessage("Bgm14/HonTale");
                        mc.dropMessage("Bgm14/CaveOfHontale");
                        mc.dropMessage("Bgm14/DragonNest");
                        mc.dropMessage("Bgm14/Ariant");
                        mc.dropMessage("Bgm14/HotDesert");
                    } else if (splitted[2].equals("Bgm15")) {
                        mc.dropMessage("Bgm15/MureungHill");
                        mc.dropMessage("Bgm15/MureungForest");
                        mc.dropMessage("Bgm15/WhiteHerb");
                        mc.dropMessage("Bgm15/Pirate");
                        mc.dropMessage("Bgm15/SunsetDesert");
                    } else if (splitted[2].equals("BgmEvent")) {
                        mc.dropMessage("BgmEvent/FunnyRabbit");
                        mc.dropMessage("BgmEvent/FunnyRabbitFaster");
                    } else if (splitted[2].equals("BgmGL")) {
                        mc.dropMessage("BgmGL/amoria");
                        mc.dropMessage("BgmGL/chapel");
                        mc.dropMessage("BgmGL/cathedral");
                        mc.dropMessage("BgmGL/Amorianchallenge");
                    } else if (splitted[2].equals("BgmJp")) {
                        mc.dropMessage("BgmJp/Feeling");
                        mc.dropMessage("BgmJp/BizarreForest");
                        mc.dropMessage("BgmJp/Hana");
                        mc.dropMessage("BgmJp/Yume");
                        mc.dropMessage("BgmJp/Bathroom");
                        mc.dropMessage("BgmJp/BattleField");
                        mc.dropMessage("BgmJp/FirstStepMaster");
                    }
                } else if(splitted[1].equalsIgnoreCase("reset")){
                    target.broadcastMessage(MaplePacketCreator.musicChange(target.getDefaultBGM()));
                    target.getProperties().setProperty("bgm", target.getDefaultBGM());
                } else {
                    String songName = splitted[1];
                    target.broadcastMessage(MaplePacketCreator.musicChange(songName));
                    target.getProperties().setProperty("bgm", splitted[1]);
                }
            } else if (splitted[0].equals("!clock")) {
                player.getMap().broadcastMessage(MaplePacketCreator.getClock(getOptionalIntArg(splitted, 1, 60)));
            } else if (splitted[0].equals("!killright")) {
                for (MapleCharacter mch : c.getPlayer().getMap().getCharacters()) {
                    if (!mch.isFacingLeft()) {
                        if (mch != null) {
                            mch.setHp(0);
                            mch.setMp(0);
                            mch.updateSingleStat(MapleStat.HP, 0);
                            mch.updateSingleStat(MapleStat.MP, 0);
                        }
                    }
                }
            } else if (splitted[0].equals("!killleft")) {
                //  int n = (int)(2.0 * Math.round(Math.random())) + 1;
                //mc.dropMessage("no"+n+"");
                for (MapleCharacter mch : c.getPlayer().getMap().getCharacters()) {
                    if (mch.isFacingLeft()) {
                        if (mch != null) {
                            mch.setHp(0);
                            mch.setMp(0);
                            mch.updateSingleStat(MapleStat.HP, 0);
                            mch.updateSingleStat(MapleStat.MP, 0);
                        }
                    }
                }
            } else if (splitted[0].equals("!killrandom")) {
                int n = (int) (2.0 * Math.round(Math.random())) + 1;
                mc.dropMessage("Was!!! " + n + ">.>");
                for (MapleCharacter mch : c.getPlayer().getMap().getCharacters()) {
                    if (n == 1) {

                        if (!mch.isFacingLeft()) {
                            mch.setHp(0);
                            mch.setMp(0);
                            mch.updateSingleStat(MapleStat.HP, 0);
                            mch.updateSingleStat(MapleStat.MP, 0);

                        }
                    }
                    if (n == 3) {
                        if (mch.isFacingLeft()) {
                            mch.setHp(0);
                            mch.setMp(0);
                            mch.updateSingleStat(MapleStat.HP, 0);
                            mch.updateSingleStat(MapleStat.MP, 0);
                        }
                    }
                }
            } else if (splitted[0].equals("!killtoleft") || splitted[0].equalsIgnoreCase("!killtoright")) {
                boolean left = splitted[0].equalsIgnoreCase("!killtoleft");
                boolean kill = false;
                for (MapleCharacter mch : c.getPlayer().getMap().getCharacters()) {
                    if ((mch.getPosition().getX() < c.getPlayer().getPosition().getX()) && left)
                        kill = true;
                    else if((mch.getPosition().getX() > c.getPlayer().getPosition().getX()) && !left)
                        kill = true;

                    if(kill)
                    {
                            mch.setHp(0);
                            mch.setMp(0);
                            mch.updateSingleStat(MapleStat.HP, 0);
                            mch.updateSingleStat(MapleStat.MP, 0);
                    }
                }
            } else if (splitted[0].equals("!closeallmerchants")) {
               for(ChannelServer cserver : ChannelServer.getAllInstances())//TODO: implement into world interfaces
               {
                   cserver.getHMRegistry().closeAndDeregisterAll();
               }
             } else if (splitted[0].equalsIgnoreCase("!closemerchant"))
            {
                 if(splitted.length != 2)
                     player.dropMessage("Syntax helper: !closemerchant <name>");
                 HiredMerchant victimMerch = c.getChannelServer().getHMRegistry().getMerchantForPlayer(splitted[1]);
                 if(victimMerch != null)
                     victimMerch.closeShop();
                 else
                     player.dropMessage("The specified player is either not online or does not have a merchant.");
            }
    }
            @Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("spy", "", "", 1),
                        new CommandDefinition("speak", "", "", 1),
                        new CommandDefinition("setall", "", "", 1),
                        new CommandDefinition("fame", "", "", 1),
                        new CommandDefinition("hide", "", "", 1),
                        new CommandDefinition("heal", "", "", 1),
                        new CommandDefinition("kill", "", "", 1),
                        new CommandDefinition("killhere", "", "", 1),
                        new CommandDefinition("dcall", "", "", 3),
                        new CommandDefinition("jobperson", "", "", 2),
                        new CommandDefinition("mesos", "", "", 1),
                        new CommandDefinition("gmshop", "", "", 1),
                        new CommandDefinition("setrates", "!setrates <EXP> <DROP> <BOSSDROP> <MESO>", "", 1),
                        new CommandDefinition("giftpoints", "!giftpoints <name> <amount>", "", 2),
                        new CommandDefinition("giftnx", "!giftnx <name> <amount>", "", 2),
                        new CommandDefinition("cleardrops", "!cleardrops", "Clears the drops on the map.", 1),
                        new CommandDefinition("chattype", "!chattype", "Toggles whether you chat in white or black.", 1),
                        new CommandDefinition("pwn", "<playername> <mapid> <number of times>", "Warps someone to a specified map a specified number of times.", 1),
                        new CommandDefinition("smega", "<name> <love / cloud / diablo> <message>", "Spoofs an avatar super megaphone.", 1),
                        new CommandDefinition("killmonster", "<monsterid>", "Kills the specified monster on the map.", 1),
                        new CommandDefinition("clock", "<time in seconds>", "Broadcasts a clock packet to all users.", 2),
                        new CommandDefinition("music", "[[names] [folder]] or [music name] or [reset]", "Changes the map's music.", 1),
                        new CommandDefinition("killright", "", "Kills players facing right on the map.", 1),
                        new CommandDefinition("killleft", "", "Kills players facing left on the map.", 1),
                        new CommandDefinition("killrandom", "", "Kills random players on the map.", 1),
                        new CommandDefinition("killtoleft", "", "Kills players to the left of you.", 1),
                        new CommandDefinition("killtoright", "", "Kills players to the right of you.", 1),
                        new CommandDefinition("closeallmerchants", "", "Closes all merchants.", 1),
                        new CommandDefinition("closemerchant", "playername", "Closes the merchant for a specified player.", 1),

		};
	}

        }