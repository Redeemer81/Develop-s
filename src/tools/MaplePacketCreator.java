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
    but WITHOUT ANY WARRANTshowY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public Licharcense for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package tools;

import java.awt.Point;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import client.BuddylistEntry;
import client.IEquip;
import client.IEquip.ScrollResult;
import client.IItem;
import client.ISkill;
import client.Item;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleDisease;
import client.MapleInventory;
import client.MapleInventoryType;
import client.MapleJob;
import client.MapleKeyBinding;
import client.MapleMount;
import client.MaplePet;
import client.MapleQuestStatus;
import client.MapleRing;
import client.MapleStat;
import client.SkillMacro;
import client.ExtendedSPTable;
import client.status.MonsterStatus;
import constants.InventoryConstants;
import constants.ServerConstants;
import constants.skills.Buccaneer;
import constants.skills.Marauder;
import constants.skills.ThunderBreaker;
import constants.skills.WindArcher;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import net.LongValueHolder;
import net.MaplePacket;
import net.SendPacketOpcode;
import net.channel.handler.SummonDamageHandler.SummonAttackEntry;
import net.channel.handler.PlayerInteractionHandler;
import net.world.MapleParty;
import net.world.MaplePartyCharacter;
import net.world.PartyOperation;
import net.world.PlayerCoolDownValueHolder;
import net.world.guild.MapleAlliance;
import net.world.guild.MapleGuild;
import net.world.guild.MapleGuildCharacter;
import net.world.guild.MapleGuildSummary;
import server.CashItemInfo;
import server.DueyPackages;
import server.MTSItemInfo;
import server.MapleItemInformationProvider;
import server.MapleMiniGame;
import server.MaplePlayerShop;
import server.MaplePlayerShopItem;
import server.MapleShopItem;
import server.MapleTrade;
import server.cashshop.CashDataProvider;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.life.MobSkill;
import server.maps.HiredMerchant;
import server.maps.MapleMap;
import server.maps.MapleMist;
import server.maps.MapleReactor;
import server.maps.MapleDragon;
import server.maps.MapleSummon;
import server.maps.PlayerNPCs;
import server.movement.LifeMovementFragment;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 *
 * @author Frz
 */
public class MaplePacketCreator {
    private final static byte[] CHAR_INFO_MAGIC = new byte[]{(byte) 0xff, (byte) 0xc9, (byte) 0x9a, 0x3b};
    public static final List<Pair<MapleStat, Integer>> EMPTY_STATUPDATE = Collections.emptyList();
    private final static byte[] ITEM_MAGIC = new byte[]{(byte) 0x80, 5};
    private final static long FT_UT_OFFSET = 116444736000000000L; // 100 nsseconds from 1/1/1601 -> 1/1/1970
    private final static byte[] NON_EXPIRE = {0x00, (byte) 0x80, 0x05, (byte) 0xBB, 0x46, (byte) 0xE6, 0x17, 0x02};
    private final static byte[] WARP_PACKET_MAGIC = HexTool.getByteArrayFromHexString("02 00 01 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00");

    private static int getItemTimestamp(long realTimestamp) {
        return (int) (((int) ((realTimestamp - 946681229830l)) / 60000) * 35.762787) + -1085019342;
    }

    private static int getQuestTimestamp(long realTimestamp) {
        return (int) (((int) (realTimestamp / 1000 / 60)) * 0.1396987) + 27111908;
    }

    private static long getKoreanTimestamp(long realTimestamp) {
        return realTimestamp * 10000 + 116444592000000000L;
    }

    private static long getTime(long realTimestamp) {
        return realTimestamp * 10000 + 116444592000000000L;
    }

    	/**
	 * Converts a Unix Timestamp into File Time
	 *
	 * @param realTimestamp The actual timestamp in milliseconds.
	 * @return A 64-bit long giving a filetime timestamp
	 */
	public static long getTempBanTimestamp(long realTimestamp) {
		// long time = (realTimestamp / 1000);//seconds
		return ((realTimestamp * 10000) + FT_UT_OFFSET);
	}

    private static void addCharEntry(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        addCharStats(mplew, chr);
        addCharLook(mplew, chr, false);
        mplew.write(0);
        mplew.writeShort(0);
    }

    private static void addQuestInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        List<MapleQuestStatus> started = chr.getStartedQuests();
        mplew.writeShort(started.size());
        for (MapleQuestStatus q : started) {
            mplew.writeShort(q.getQuest().getId());
            mplew.writeMapleAsciiString(q.getQuestData());
        }
        mplew.write(1);
        List<MapleQuestStatus> completed = chr.getCompletedQuests();
        mplew.writeShort(completed.size());
        for (MapleQuestStatus q : completed) {
            mplew.writeShort(q.getQuest().getId());
            mplew.writeLong(q.getCompletionTime());
        }
    }

    private static void addItemInfo(MaplePacketLittleEndianWriter mplew, IItem item) {
        addItemInfo(mplew, item, false, false);
    }

    private static void addExpirationTime(MaplePacketLittleEndianWriter mplew, long time, boolean showexpirationtime) {
        mplew.writeInt(getItemTimestamp(time));
        mplew.write(showexpirationtime ? 1 : 2);
    }

    private static void addItemInfo(MaplePacketLittleEndianWriter mplew, IItem item, boolean zeroPosition, boolean leaveOut) {
        addItemInfo(mplew, item, zeroPosition, leaveOut, false, false);
    }


    private static void addItemInfo(MaplePacketLittleEndianWriter mplew, IItem item, boolean zeroPosition, boolean leaveOut, boolean gachapon, boolean inTrade) {
        boolean ring = false;
        IEquip equip = null;
        if (item.getType() == IItem.EQUIP) {
            equip = (IEquip) item;
            if (equip.getRingId() > -1) {
                ring = true;
            }
        }
        byte pos = item.getPosition();
        boolean masking = false;
        if (!gachapon) {
            if (zeroPosition) {
                if (!leaveOut) {
                        mplew.write(0);
                }
            } else if (pos <= (byte) -1) {
                pos *= -1;
                if (pos > 100 || ring) {
                    masking = true;
                    mplew.writeShort(pos - 100);
                } else {
                    if((item.getType() == IItem.EQUIP) && (!inTrade))
                        mplew.writeShort(pos);
                    else
                        mplew.write(pos);
                }
            } else {
                if((item.getType() == IItem.EQUIP) && (!inTrade))
                    mplew.writeShort(item.getPosition());
                else
                    mplew.write(item.getPosition());
            }
        }
        if (item.getPetId() > -1) {
            mplew.write(3);
        } else {
            mplew.write(item.getType());
        }
        mplew.writeInt(item.getItemId());
        if (ring) {
            mplew.write(1);
            mplew.writeInt(equip.getRingId());
            mplew.writeInt(0);
        }
        if (item.getPetId() > -1) {
            MaplePet pet = MaplePet.loadFromDb(item.getItemId(), item.getPosition(), item.getPetId());
            String petname = pet.getName();
            mplew.write(1);
            mplew.writeInt(item.getPetId());
            mplew.writeInt(0);
            mplew.write(0);
            mplew.write(ITEM_MAGIC);
            mplew.write(HexTool.getByteArrayFromHexString("BB 46 E6 17 02"));
            if (petname.length() > 13) {
                petname = petname.substring(0, 13);
            }
            mplew.writeAsciiString(petname);
            for (int i = petname.getBytes().length; i < 13; i++) {
                mplew.write(0);
            }
            mplew.write(pet.getLevel());
            mplew.writeShort(pet.getCloseness());
            mplew.write(pet.getFullness());
            mplew.writeLong(getKoreanTimestamp((long) (System.currentTimeMillis() * 1.2)));
            mplew.writeInt(0);
            mplew.write(HexTool.getByteArrayFromHexString("50 46 00 00 00 00")); //wonder what this is - WAS 50 46 00 00
            return;
        }
        if (masking && !ring) {
            mplew.write(1);
            mplew.writeLong(12345678); // 캐시id
            mplew.write(0);
            mplew.write(ITEM_MAGIC);
            addExpirationTime(mplew, 0, false);
            mplew.writeInt(-1);
        } else if (ring) {
            mplew.writeLong(getKoreanTimestamp((long) (System.currentTimeMillis() * 1.2)));
        } else {
            mplew.writeShort(0);
            mplew.write(ITEM_MAGIC);
            addExpirationTime(mplew, 0, false);
            mplew.writeInt(-1);
        }
        if (item.getType() == IItem.EQUIP) {
            mplew.write(equip.getUpgradeSlots());
            mplew.write(equip.getLevel());
            mplew.writeShort(equip.getStr());
            mplew.writeShort(equip.getDex());
            mplew.writeShort(equip.getInt());
            mplew.writeShort(equip.getLuk());
            mplew.writeShort(equip.getHp());
            mplew.writeShort(equip.getMp());
            mplew.writeShort(equip.getWatk());
            mplew.writeShort(equip.getMatk());
            mplew.writeShort(equip.getWdef());
            mplew.writeShort(equip.getMdef());
            mplew.writeShort(equip.getAcc());
            mplew.writeShort(equip.getAvoid());
            mplew.writeShort(equip.getHands());
            mplew.writeShort(equip.getSpeed());
            mplew.writeShort(equip.getJump());
            mplew.writeMapleAsciiString(equip.getOwner());
            mplew.writeShort(equip.getFlag());
            mplew.write(0);
            mplew.write(equip.getItemLevel());
            mplew.writeShort(0);
            mplew.writeShort(0); // 아이템 경험치
            mplew.writeInt(-1); // 내구도
            mplew.writeShort(equip.getVicious());
            mplew.writeInt(0);
            mplew.write(equip.getPotential()); // 잠재능력 등급
            mplew.write(equip.getPStars()); // 강화
            mplew.writeShort(equip.getPotential_1()); // 옵션1
            mplew.writeShort(equip.getPotential_2()); // 옵션2
            mplew.writeShort(equip.getPotential_3()); // 옵션3
            mplew.writeInt(0);
            if (!masking)
                mplew.writeLong(-1);
            mplew.write(HexTool.getByteArrayFromHexString("00 40 E0 FD 3B 37 4F 01"));
            mplew.writeInt(-1);
        } else {
            mplew.writeShort(item.getQuantity());
            mplew.writeMapleAsciiString(item.getOwner());
            mplew.writeShort(item.getFlag());
            if (InventoryConstants.isRechargable(item.getItemId())) {
                mplew.writeLong(0);
            }
        }
    }

    /*
     * 42 B1 49 00 
     00 00 00 00
     1C 1C 1C 1C 60
     00 40 E0 FD 3B 37 4F 01
     01 00 01 60 4F 0F 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 0A 00 0A 00 0A 00 0A 00 0A 00 C8 00 00 00 00 00 00 00 78 00 78 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 13 00 9A 75 56 4E 00 00 00 00 00 00 7B 26 03 00 B0 01 00 48 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 
     04 00 01 B1 BF 0F 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 05 00 03 00 03 00 03 00 03 00 50 00 50 00 00 00 00 00 1B 00 1B 00 14 00 14 00 00 00 00 00 00 00 00 00 08 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 DA 88 03 00 F4 01 00 4C 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     05 00 01 F5 0E 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 0A 00 06 00 06 00 06 00 06 00 50 00 50 00 00 00 00 00 84 00 84 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 11 00 45 27 02 00 00 00 00 00 00 00 8D 61 02 00 AA 01 00 40 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     07 00 01 E2 5D 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 05 00 04 00 04 00 04 00 04 00 00 00 00 00 00 00 00 00 32 00 32 00 00 00 00 00 00 00 03 00 01 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 11 00 1E 27 0D 00 08 00 00 00 00 00 51 B8 05 00 C5 01 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     08 00 01 92 82 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 04 00 00 00 00 00 00 00 00 00 00 00 00 08 00 00 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 11 00 45 27 07 00 00 00 00 00 00 00 07 46 00 00 CC 00 00 4C 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     09 00 01 F2 D1 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 05 00 01 00 01 00 01 00 01 00 0A 00 0A 00 00 00 00 00 23 00 23 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 11 00 17 27 05 00 00 00 00 00 00 00 C8 AD 05 00 C5 01 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     0A 00 01 FC C4 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 0C 00 0C 00 00 00 00 00 58 02 6E 00 00 00 00 00 55 00 2D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 76 F2 49 00 FF FF FF FF 00 00 00 00 00 00 11 00 01 00 07 00 0B 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     0B 00 01 7F 2C 14 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 06 01 03 00 03 00 03 00 03 00 00 00 00 00 6C 00 00 00 00 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 08 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 11 00 F2 27 02 00 04 00 00 00 00 00 29 2E 04 00 AF 01 00 18 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     0C 00 01 5C FA 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 04 00 04 00 04 00 04 00 50 00 50 00 03 00 03 00 0A 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 06 BA 05 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 A3 5E 02 00 AB 01 00 44 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     0D 00 01 60 FA 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 02 00 02 00 02 00 02 00 00 00 00 00 01 00 01 00 14 00 14 00 0F 00 00 00 00 00 05 00 00 00 00 00 08 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 F3 88 03 00 F4 01 00 4C 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     0F 00 01 61 FA 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 04 00 04 00 04 00 04 00 32 00 32 00 02 00 02 00 28 00 28 00 19 00 0F 00 00 00 05 00 00 00 00 00 08 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 A5 B0 05 00 C5 01 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     10 00 01 67 FA 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 02 00 03 00 03 00 03 00 03 00 4B 00 4B 00 01 00 01 00 3C 00 3C 00 14 00 0F 00 00 00 05 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 3C 04 00 AB 01 00 2C 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     11 00 01 6A 1F 11 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 04 00 06 00 06 00 06 00 06 00 3C 00 3C 00 01 00 01 00 28 00 28 00 00 00 00 00 00 00 00 00 00 00 00 00 08 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 11 00 3A 27 08 00 00 00 00 00 00 00 EA 88 03 00 F4 01 00 4C 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     13 00 01 E9 2C 1D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 75 AA 01 00 A6 01 00 3C 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     15 00 01 52 6E 11 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 05 00 05 00 05 00 05 00 C8 00 C8 00 01 00 01 00 78 00 78 00 32 00 32 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 0D 3C 04 00 AB 01 00 2C 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     16 00 01 4E 46 11 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 03 00 04 00 04 00 04 00 04 00 3C 00 3C 00 00 00 00 00 3C 00 3C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 11 00 17 27 0E 00 00 00 00 00 00 00 B2 AD 05 00 C5 01 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     17 00 01 44 94 11 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 05 00 05 00 05 00 05 00 00 00 00 00 02 00 02 00 19 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1F 4D 05 00 C5 01 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 
     00 00
     01 00 01 43 42 0F 00 01 37 09 9F 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 
     04 00 01 58 BF 0F 00 01 3A 09 9F 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     05 00 01 06 06 10 00 01 42 09 9F 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     07 00 01 19 5C 10 00 01 40 09 9F 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     08 00 01 77 83 10 00 01 3F 09 9F 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     09 00 01 1C D1 10 00 01 3C 09 9F 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     0B 00 01 85 F9 19 00 01 3D 09 9F 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     00 00
     01 00 01 F3 D1 10 00 01 42 BE 9A 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 
     02 00 01 64 4F 0F 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 08 00 05 00 05 00 05 00 05 00 00 00 00 00 01 00 01 00 50 00 50 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 11 00 1E 27 08 00 3E 27 00 00 00 00 BE AD 05 00 C5 01 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     03 00 01 17 84 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 05 00 04 00 04 00 04 00 04 00 28 00 28 00 00 00 00 00 23 00 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 11 00 40 27 0D 00 00 00 00 00 00 00 F7 C0 05 00 C5 01 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     04 00 01 AC 6D 11 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 02 00 02 00 02 00 02 00 64 00 64 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 E8 C8 00 00 A8 01 00 28 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     05 00 01 45 6E 11 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 32 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 79 01 00 00 AF 01 00 44 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     06 00 01 46 6E 11 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 64 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 BB 24 00 00 AF 01 00 44 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     07 00 01 47 6E 11 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 01 00 01 00 01 00 01 00 96 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 8F 55 03 00 B0 01 00 48 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     08 00 01 48 6E 11 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 02 00 02 00 02 00 02 00 C8 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 91 00 00 AA 01 00 38 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     09 00 01 4D 6E 11 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 08 00 08 00 08 00 08 00 50 00 50 00 00 00 00 00 00 00 00 00 50 00 50 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 CF 00 05 00 F7 01 00 4C 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     0A 00 01 8F 2C 14 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 07 00 00 00 00 00 00 00 00 00 00 00 00 00 6C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 91 00 00 AA 01 00 38 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     0B 00 01 9A 2C 14 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 05 01 03 00 03 00 03 00 03 00 00 00 00 00 60 00 00 00 00 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 11 00 3C 27 02 00 00 00 00 00 00 00 DC C2 05 00 C5 01 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     0C 00 01 09 01 16 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 07 00 00 00 00 00 00 00 00 00 00 00 00 00 65 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FE 52 05 00 C5 01 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     0D 00 01 40 12 17 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 E2 F5 04 00 A8 01 00 28 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     0E 00 01 F4 D1 10 00 01 4F BE 9A 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
     *
     00 00
     00 00
     00 00
     00 00
     *
     01 02 99 84 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 2C 01 00 00 00 00 
     02 02 99 84 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 32 00 00 00 00 00
     03 02 9A 84 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 96 00 00 00 00 00
     04 02 61 8A 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 96 00 00 00 00 00
     05 02 7A 8A 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 2C 01 00 00 00 00
     06 02 7A 8A 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 2C 01 00 00 00 00
     07 02 7A 8A 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 28 00 00 00 00 00
     08 02 88 8A 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 04 01 00 00 00 00
     09 02 88 8A 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 2C 01 00 00 00 00
     0A 02 88 8A 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF AA 00 00 00 00 00
     0B 02 C9 DA 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF B0 00 00 00 00 00
     0C 02 B0 F9 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 0A 00 00 00 00 00
     0D 02 59 38 1F 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00
     0E 02 B0 38 1F 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00
     0F 02 17 45 1F 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00
     10 02 39 F2 22 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00
     11 02 14 16 25 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 06 00 00 00 00 00
     12 02 1A 16 25 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 03 00 00 00 00 00
     13 02 62 89 25 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 05 00 00 00 00 00
     14 02 BC 4C 26 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00
     15 02 B5 24 1F 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00
     16 02 11 16 25 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00

     00 
     *
     01 02 9A EE 2D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00
     02 02 28 75 38 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00
     03 02 0B F3 3C 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 04 00 00 00 00 00
     04 02 11 F3 3C 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00
     06 02 2E 75 38 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00
     
     00

     01 02 15 09 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 23 00 00 00 00 00
     02 02 06 0A 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 1A 00 00 00 00 00
     03 02 88 0D 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 02 00 00 00 00 00
     04 02 71 0E 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00
     05 02 99 0E 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 07 00 00 00 00 00
     06 02 C9 0E 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00
     07 02 F1 0E 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 42 00 00 00 00 00
     08 02 FA 0E 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00
     09 02 17 5B 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 03 00 00 00 10 00
     0A 02 29 A1 3F 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 14 00 00 00 00 00
     0B 02 04 C4 41 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 7F 00 00 00 00 00
     0C 02 0B C4 41 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 16 00 00 00 00 00
     0D 02 0D C4 41 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 82 00 00 00 00 00
     0E 02 19 12 42 00 00 00 80 05 BB 46 E6 17 02 00 00 00 00 01 00 00 00 08 00
     0F 02 52 0F 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 39 00 00 00 00 00
     10 02 0C C4 41 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 19 00 00 00 00 00
     12 02 04 0A 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 04 00 00 00 00 00
     13 02 71 20 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00
     14 02 E8 09 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 0C 00 00 00 00 00
     15 02 EB 0E 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 04 00 00 00 00 00
     16 02 E9 09 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 16 00 00 00 00 00
     17 02 EA 09 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 1C 00 00 00 00 00
     18 02 54 0F 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 06 00 00 00 00 00
     00
     01 02 B0 CD 4F 00 01 4E 09 9F 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 02 00 00 00 00 00 00 19 12 42 00 00 00 00 00 02 02 34 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 2D 00 00 00 00 00 01 00 00 00 02 14 30 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 0D 00 00 00 00 00 02 00 00 00 02 21 57 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 0A 00 00 00 00 00 03 00 00 00 02 24 57 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 0F 00 00 00 00 00 FF FF FF FF FF FF FF FF
     *
     */
    private static void addInventoryInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getMeso());
        mplew.writeInt(0);
        for (byte i = 1; i <= 5; i++) {
            mplew.write(chr.getInventory(MapleInventoryType.getByType(i)).getSlotLimit());
        }
        mplew.write(HexTool.getByteArrayFromHexString("00 40 E0 FD 3B 37 4F 01")); //*
        MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
        Collection<IItem> equippedC = iv.list();
        List<Item> equipped = new ArrayList<Item>(equippedC.size());
        List<Item> equippedCash = new ArrayList<Item>(equippedC.size());
        for (IItem item : equippedC) {
            if (item.getPosition() <= -100) {
                equippedCash.add((Item) item);
            } else {
                equipped.add((Item) item);
            }
        }
        Collections.sort(equipped);
        for (Item item : equipped) { 
            addItemInfo(mplew, item);
        }
        mplew.writeShort(0);
        for (Item item : equippedCash) {
            addItemInfo(mplew, item);
        }
        mplew.writeShort(0);
        for (IItem item : chr.getInventory(MapleInventoryType.EQUIP).list()) {
            addItemInfo(mplew, item);
        }
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeShort(0);
        for (IItem item : chr.getInventory(MapleInventoryType.USE).list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);
        for (IItem item : chr.getInventory(MapleInventoryType.SETUP).list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);
        for (IItem item : chr.getInventory(MapleInventoryType.ETC).list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);
        for (IItem item : chr.getInventory(MapleInventoryType.CASH).list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0);
        mplew.writeInt(-1);
    }

    private static void addSkillInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        Map<ISkill, MapleCharacter.SkillEntry> skills = chr.getSkills();
        mplew.writeShort(skills.size());
        for (Entry<ISkill, MapleCharacter.SkillEntry> skill : skills.entrySet()) {
            mplew.writeInt(skill.getKey().getId());
            mplew.writeInt(skill.getValue().skillevel);
            mplew.write(0);
            mplew.write(ITEM_MAGIC);
            mplew.writeInt(400967355);
            mplew.write(2);
            if (skill.getKey().hasMastery()) { //love you moogra
                mplew.writeInt(skill.getValue().masterlevel);
            }
        }
        mplew.writeShort(chr.getAllCooldowns().size());
        for (PlayerCoolDownValueHolder cooling : chr.getAllCooldowns()) {
            mplew.writeInt(cooling.skillId);
            int timeLeft = (int) (cooling.length + cooling.startTime - System.currentTimeMillis());
            mplew.writeShort(timeLeft / 1000);
        }
    }

    private static void addMonsterBookInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getMonsterBookCover()); // cover
        mplew.write(0);
        Map<Integer, Integer> cards = chr.getMonsterBook().getCards();
        mplew.writeShort(cards.size());
        for (Entry<Integer, Integer> all : cards.entrySet()) {
            mplew.writeShort(all.getKey() % 10000); // Id
            mplew.write(all.getValue()); // Level
        }
    }

    public static MaplePacket sendCygnusCreateChar() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
        mplew.writeShort(SendPacketOpcode.CREATE_CYGNUS.getValue());
        return mplew.getPacket();
    }

    public static MaplePacket sendCygnusMessage(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendPacketOpcode.CYGNUS_CHAR_CREATED.getValue());
        mplew.writeInt(type);
        return mplew.getPacket();
    }

    public static MaplePacket sendGuestTOS() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SEND_LINK.getValue());
        mplew.writeShort(0x100);
        mplew.writeInt(Randomizer.getInstance().nextInt(999999));
        mplew.writeLong(0);
        mplew.write(HexTool.getByteArrayFromHexString("40 E0 FD 3B 37 4F 01"));
        mplew.writeLong(getKoreanTimestamp(System.currentTimeMillis()));
        mplew.writeInt(0);
        mplew.writeMapleAsciiString("http://nexon.net");
        return mplew.getPacket();
    }

    /**
     * Sends a hello packet.
     *
     * @param mapleVersion The maple client version.
     * @param sendIv the IV used by the server for sending
     * @param recvIv the IV used by the server for receiving
     * @param testServer
     * @return
     */
    public static MaplePacket getHello(short mapleVersion, byte subVersion, byte[] sendIv, byte[] recvIv) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        String version = String.valueOf(MapleAESOFB.getGeneratePatchLocation(mapleVersion, subVersion));
        mplew.writeShort(13 + version.length());
        mplew.writeShort(291);
        mplew.writeMapleAsciiString(version);
        mplew.write(recvIv);
        mplew.write(sendIv);
        mplew.write(1);
        return mplew.getPacket();
    }

    /**
     * Sends a ping packet.
     *
     * @return The packet.
     */
    public static MaplePacket getPing() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
        mplew.writeShort(SendPacketOpcode.PING.getValue());
        return mplew.getPacket();
    }

    /**
     * Gets a login failed packet.
     *
     * Possible values for <code>reason</code>:<br>
     * 3: ID deleted or blocked<br>
     * 4: Incorrect password<br>
     * 5: Not a registered id<br>
     * 6: System error<br>
     * 7: Already logged in<br>
     * 8: System error<br>
     * 9: System error<br>
     * 10: Cannot process so many connections<br>
     * 11: Only users older than 20 can use this channel<br>
     * 13: Unable to log on as master at this ip<br>
     * 14: Wrong gateway or personal info and weird korean button<br>
     * 15: Processing request with that korean button!<br>
     * 16: Please verify your account through email...<br>
     * 17: Wrong gateway or personal info<br>
     * 21: Please verify your account through email...<br>
     * 23: License agreement<br>
     * 25: Maple Europe notice =[<br>
     * 27: Some weird full client notice, probably for trial versions<br>
     *
     * @param reason The reason logging in failed.
     * @return The login failed packet.
     */
    public static MaplePacket getLoginFailed(int reason) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.write(reason);
        return mplew.getPacket();
    }

    public static MaplePacket getPermBan(byte reason) {
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);
		// Response.WriteHexString("00 00 02 00 01 01 01 01 01 00");
		mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
		mplew.writeShort(0x02); // Account is banned

		mplew.write(0x0);
		mplew.write(reason);
		mplew.write(HexTool.getByteArrayFromHexString("01 01 01 01 00"));
		return mplew.getPacket();
	}
//02 00 00 00 00 00 01 00 D2 74 3B EF 05 D2 01
	public static MaplePacket getTempBan(long timestampTill, byte reason) {

		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(17);
		mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
		mplew.write(0x02);
		mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00")); // Account is banned

		mplew.write(reason);
		mplew.writeLong(timestampTill); // Tempban date is handled as a 64-bit long, number of 100NS intervals since
		// 1/1/1601. Lulz.

		return mplew.getPacket();
	}

    /**
     * Gets a successful authentication and PIN Request packet.
     *
     * @param account The account name.
     * @return The PIN request packet.
     */
    public static MaplePacket getAuthSuccessRequestPin(MapleClient c, String account) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.write(0);
        mplew.writeInt(c.getAccID());
        mplew.write(0); // gender
        mplew.write(0);
        mplew.write(0);
        mplew.writeMapleAsciiString(c.getAccountName()); // nexon id
        mplew.writeInt(750101); // dob
        mplew.write(1);
        mplew.write(3);
        mplew.write(0);
        mplew.writeShort(0);
        mplew.writeLong(0);
        return mplew.getPacket();
    }

    /**
     * Gets a packet detailing a PIN operation.
     *
     * Possible values for <code>mode</code>:<br>
     * 0 - PIN was accepted<br>
     * 1 - Register a new PIN<br>
     * 2 - Invalid pin / Reenter<br>
     * 3 - Connection failed due to system error<br>
     * 4 - Enter the pin
     *
     * @param mode The mode.
     * @return
     */
    public static MaplePacket pinOperation(int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendPacketOpcode.PIN_OPERATION.getValue());
        mplew.write(mode);
        return mplew.getPacket();
    }

    public static MaplePacket pinRegistered() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendPacketOpcode.PIN_ASSIGNED.getValue());
        mplew.write(0);
        return mplew.getPacket();
    }

    /**
     * Gets a packet detailing a server and its channels.
     *
     * @param serverIndex The index of the server to create information about.
     * @param serverName The name of the server.
     * @param channelLoad Load of the channel - 1200 seems to be max.
     * @return The server info packet.
     */


    
    public static MaplePacket getServerList(int serverId, String serverName, Map<Integer, Integer> channelLoad) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SERVERLIST.getValue());
        mplew.write(serverId);
        mplew.writeMapleAsciiString(serverName);
        mplew.write(ServerConstants.FLAG);
        mplew.writeMapleAsciiString(ServerConstants.EVENT_MESSAGE);
        mplew.writeShort(100);
        mplew.writeShort(100);
        int lastChannel = 1;
        Set<Integer> channels = channelLoad.keySet();
        for (int i = 30; i > 0; i--) {
            if (channels.contains(i)) {
                lastChannel = i;
                break;
            }
        }
        mplew.write(lastChannel);
        int load;
        for (int i = 1; i <= lastChannel; i++) {
            if (channels.contains(i)) {
                load = channelLoad.get(i) * 1200 / ServerConstants.CHANNEL_LOAD;
            } else {
                load = ServerConstants.CHANNEL_LOAD; // full
            }
            String channel = "";
            if (i == 1)
                channel = String.valueOf(i);
            else if (i == 2)
                channel = "20세이상";
            else
                channel = String.valueOf(i - 1);
            mplew.writeMapleAsciiString(serverName + "-" + channel);
            mplew.writeInt(load);
            mplew.write(serverId);
            mplew.write(i - 1);
            if(i != 2){
                mplew.write(0); // 20세이상 채널
            } else {
                mplew.write(1);
            }
        }
        mplew.writeShort(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket getLastSelectedWorld() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.LAST_WORLD.getValue());
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket sendRecommendedServers() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SEND_RECOMMENDED.getValue());
        mplew.write(1); //# of Recommended Servers
        mplew.writeInt(0);//scania
        mplew.writeMapleAsciiString(ServerConstants.RECOMMENDED_MESSAGE);
        return mplew.getPacket();
    }
    
    public static MaplePacket getSocialNumberResponse(byte status) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SOCIALNUMBER_RESPONSE.getValue());
        mplew.write(status);
        return mplew.getPacket();
    }

    /**
     * Gets a packet saying that the server list is over.
     *
     * @return The end of server list packet.
     */
    public static MaplePacket getEndOfServerList() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendPacketOpcode.SERVERLIST.getValue());
        mplew.writeShort(0xFF);
        return mplew.getPacket();
    }

    /**
     * Gets a packet detailing a server status message.
     *
     * Possible values for <code>status</code>:<br>
     * 0 - Normal<br>
     * 1 - Highly populated<br>
     * 2 - Full
     *
     * @param status The server status.
     * @return The server status packet.
     */
    public static MaplePacket getServerStatus(int status) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendPacketOpcode.SERVERSTATUS.getValue());
        mplew.writeShort(status);
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client the IP of the channel server.
     *
     * @param inetAddr The InetAddress of the requested channel server.
     * @param port The port the channel is on.
     * @param clientId The ID of the client.
     * @return The server IP packet.
     */
    public static MaplePacket getServerIP(InetAddress inetAddr, int port, int clientId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SERVER_IP.getValue());
        mplew.writeShort(0);
        byte[] addr = inetAddr.getAddress();
        mplew.write(addr);
        mplew.writeShort(port);
        mplew.writeInt(clientId); // this gets repeated to the channel server
        mplew.write(new byte[]{ 1, 0, 0, 0, 0, 0, 0, 0, 6, 0 });
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client the IP of the new channel.
     *
     * @param inetAddr The InetAddress of the requested channel server.
     * @param port The port the channel is on.
     * @return The server IP packet.
     */
    public static MaplePacket getChannelChange(InetAddress inetAddr, int port) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CHANGE_CHANNEL.getValue());
        mplew.write(1);
        byte[] addr = inetAddr.getAddress();
        mplew.write(addr);
        mplew.writeShort(port);
        mplew.write(0);
        return mplew.getPacket();
    }

    /**
     * Gets a packet with a list of characters.
     *
     * @param c The MapleClient to load characters of.
     * @param serverId The ID of the server requested.
     * @return The character list packet.
     */
    public static MaplePacket getCharList(MapleClient c, int serverId) {
        List<MapleCharacter> chars = c.loadCharacters(serverId);
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CHARLIST.getValue());
        mplew.write(0);
        mplew.writeInt(c.getSocialNumber());
        mplew.write(chars.size());
        for (MapleCharacter chr : chars){
            addCharEntry(mplew, chr);
        }
        mplew.write(2); // 1 : 2차비번 있음, 0 : 2차비번 없음, 2 : 2차비번 사용안함
        mplew.write(0); // 1 : 주민번호 인증, 0 : 주민번호 인증 X
        mplew.write(3); // 최대생성가능캐릭터갯수
        mplew.write0(7);
        return mplew.getPacket();
    }

	/**
	 * Adds character stats to an existing MaplePacketLittleEndianWriter.
	 *
	 * @param mplew The MaplePacketLittleEndianWrite instance to write the stats
	 *            to.
	 * @param chr The character to add the stats of.
	 */
	private static void addCharStats(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
            mplew.writeInt(chr.getId());
            mplew.writeAsciiString(StringUtil.getRightPaddedStr(chr.getName(), '\0', 13));
            mplew.write(chr.getGender());
            mplew.write(chr.getSkinColor().getId());
            mplew.writeInt(chr.getFace());
            mplew.writeInt(chr.getHair());
            mplew.write(chr.getLevel());
            mplew.writeShort(chr.getJob().getId());
            mplew.writeShort(chr.getStr());
            mplew.writeShort(chr.getDex());
            mplew.writeShort(chr.getInt());
            mplew.writeShort(chr.getLuk());
            mplew.writeInt(chr.getHp());
            mplew.writeInt(chr.getMaxHp());
            mplew.writeInt(chr.getMp());
            mplew.writeInt(chr.getMaxMp());
            mplew.writeShort(chr.getRemainingAp());
            if (MapleJob.isExtendSPJob(chr.getJob()))
                chr.getSPTable().addSPData(mplew);
            else
                mplew.writeShort(chr.getRemainingSp());
            mplew.writeInt(chr.getExp());
            mplew.writeInt(chr.getFame());
            mplew.writeInt(chr.getMapId());
            mplew.write(chr.getInitialSpawnpoint());
            if(chr.getCharType() == 1) { // 듀얼 블레이드
                mplew.writeShort(1);
            } else if(chr.getCharType() == 2) { // 캐논 슈터
                mplew.writeShort(2);
            } else {
                mplew.writeShort(0);
            }
            if(MapleJob.isDemonSlayer(chr.getJob().getId())) {
                mplew.writeInt(1012279); // 데몬슬레이어 얼굴장식
            }
            mplew.write(0);
            mplew.writeInt(2011010101); // 최근에 로그인 한 시간
            mplew.writeInt(0); // 카리스마
            mplew.writeInt(0); // 통찰력
            mplew.writeInt(0); // 의지
            mplew.writeInt(0); // 손재주
            mplew.writeInt(0); // 감성
            mplew.writeInt(0); // 매력
            mplew.writeShort(0); // 오늘의 카리스마
            mplew.writeShort(0); // 오늘의 통찰력
            mplew.writeShort(0); // 오늘의 의지
            mplew.writeShort(0); // 오늘의 손재주
            mplew.writeShort(0); // 오늘의 감성
            mplew.writeShort(0); // 오늘의 매력
            mplew.writeInt(0); //배틀 경험치
            mplew.write(10); //배틀 등급
            mplew.writeInt(0); //보유 배틀포인트(BP)
            mplew.write(5);
            mplew.writeInt(0);
            mplew.write(HexTool.getByteArrayFromHexString("A0 B9 CC 01 50 A4 0C BD"));
	}

	/**
	 * Adds the aesthetic aspects of a character to an existing
	 * MaplePacketLittleEndianWriter.
	 *
	 * @param mplew The MaplePacketLittleEndianWrite instance to write the stats
	 *            to.
	 * @param chr The character to add the looks of.
	 * @param mega Unknown
	 */
	private static void addCharLook(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean mega) {
            mplew.write(chr.getGender());
            mplew.write(chr.getSkinColor().getId());
            mplew.writeInt(chr.getFace());
            mplew.writeShort(chr.getJob().getId());
            mplew.writeShort(0);
            mplew.write(mega ? 0 : 1);
            mplew.writeInt(chr.getHair());
            MapleInventory equip = chr.getInventory(MapleInventoryType.EQUIPPED);
            Map<Byte, Integer> myEquip = new LinkedHashMap<Byte, Integer>();
            Map<Byte, Integer> maskedEquip = new LinkedHashMap<Byte, Integer>();
            for (IItem item : equip.list()) {
                byte pos = (byte) (item.getPosition() * -1);
                if (pos < 100 && myEquip.get(pos) == null) {
                    myEquip.put(pos, item.getItemId());
                } else if (pos > 100 && pos != 111) {
                    pos -= 100;
                    if (myEquip.get(pos) != null) {
                        maskedEquip.put(pos, myEquip.get(pos));
                    }
                    myEquip.put(pos, item.getItemId());
                } else if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, item.getItemId());
                }
            }
            for (Entry<Byte, Integer> entry : myEquip.entrySet()) {
                mplew.write(entry.getKey());
                mplew.writeInt(entry.getValue());
            }
            mplew.write(0xFF);
            for (Entry<Byte, Integer> entry : maskedEquip.entrySet()) {
                mplew.write(entry.getKey());
                mplew.writeInt(entry.getValue());
            }
            mplew.write(0xFF);
            IItem cWeapon = equip.getItem((byte) -111);
            mplew.writeInt(cWeapon != null ? cWeapon.getItemId() : 0);
            mplew.writeInt(0);//페
            mplew.writeInt(0);//ㅅ
            mplew.writeInt(0);//들
            if (MapleJob.isDemonSlayer(chr.getJob().getId())) {
                mplew.writeInt(1012279);
            }
	}

	/**
	 * Adds an entry for a character to an existing
	 * MaplePacketLittleEndianWriter.
	 *
	 * @param mplew The MaplePacketLittleEndianWrite instance to write the stats
	 *            to.
	 * @param chr The character to add.
	 */
	/*private static void addCharEntry(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
	//99 C8 07 00 31 30 32 39 31 38 32 33 00 E2 86 03 00 00 00 21 4E 00 00 44 75 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 03 00 00 16 00 05 00 04 00 04 00 4C 00 4C 00 1A 00 1A 00 00 00 00 00 05 00 00 00 00 00 00 00 00 00 40 9C 00 00 00 17 00 00 00 00 00 21 4E 00 00 00 44 75 00 00 05 82 DE 0F 00 06 A2 2C 10 00 07 A6 5B 10 00 0B 15 2C 14 00 FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 D9 F8 09 00 6E 33 36 35 31 30 32 00 E8 03 00 00 00 00 03 84 4E 00 00 4B 75 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 2B 3E 08 9E 00 45 00 04 00 04 00 D9 06 A9 07 97 01 A2 01 00 00 00 00 DF C3 01 00 1A 00 00 00 00 00 C9 44 24 06 00 35 21 00 00 00 03 84 4E 00 00 00 4B 75 00 00 01 14 4A 0F 00 05 D5 DE 0F 00 06 EA 2C 10 00 07 A8 5B 10 00 08 92 82 10 00 0B D9 00 16 00 0C 5D F9 10 00 11 D7 1E 11 00 31 77 6D 11 00 FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 A8 59 00 00 E1 FF FF FF C3 E8 00 00 25 FF FF FF 01 04 00 00 00
		addCharStats(mplew, chr);
		addCharLook(mplew, chr, false);

		mplew.writeShort(0); // No longer a byte but a short...... Unless ofc you want to add Rankings.
	}*/

    public static MaplePacket enableTV() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendPacketOpcode.ENABLE_TV.getValue());
        mplew.writeInt(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    /**
     * Removes TV
     * 
     * @return The Remove TV Packet
     */
    public static MaplePacket removeTV() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
        mplew.writeShort(SendPacketOpcode.REMOVE_TV.getValue());
        return mplew.getPacket();
    }

    /**
     * Sends MapleTV
     *
     * @param chr The character shown in TV
     * @param messages The message sent with the TV
     * @param type The type of TV
     * @param partner The partner shown with chr
     * @return the SEND_TV packet
     */
    public static MaplePacket sendTV(MapleCharacter chr, List<String> messages, int type, MapleCharacter partner) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SEND_TV.getValue());
        mplew.write(partner != null ? 3 : 1);
        mplew.write(type); //Heart = 2  Star = 1  Normal = 0
        addCharLook(mplew, chr, false);
        mplew.writeMapleAsciiString(chr.getName());
        if (partner != null) {
            mplew.writeMapleAsciiString(partner.getName());
        } else {
            mplew.writeShort(0);
        }
        for (int i = 0; i < messages.size(); i++) {
            if (i == 4 && messages.get(4).length() > 15) {
                mplew.writeMapleAsciiString(messages.get(4).substring(0, 15));
            } else {
                mplew.writeMapleAsciiString(messages.get(i));
            }
        }
        mplew.writeInt(1337); // time limit shit lol 'Your thing still start in blah blah seconds'
        if (partner != null) {
            addCharLook(mplew, partner, false);
        }
        return mplew.getPacket();
    }

    /**
     * Gets character info for a character.
     *
     * @param chr The character to get info about.
     * @return The character info packet.
     */
    public static MaplePacket getCharInfo(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());
        mplew.writeInt(chr.getClient().getChannel() - 1);
        mplew.write0(5);
        mplew.write(1);
        mplew.write0(4);
        mplew.write(1);
        mplew.write0(2);
        for (int i = 0; i < 3; i++) {
            mplew.writeInt(Randomizer.getInstance().nextInt());
        }
        mplew.writeLong(-1);
        mplew.write0(7);
        addCharStats(mplew, chr);
        mplew.write(chr.getBuddylist().getCapacity()); // buddylist capacity
        if (chr.getLinkedName() == null) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeMapleAsciiString(chr.getLinkedName());
        }
        mplew.write(0); // 여제의 축복
        mplew.write(0); // 여제의 강화
        addInventoryInfo(mplew, chr);
        mplew.write0(9);
        mplew.write(1); // v143
        addSkillInfo(mplew, chr);
        mplew.write(1);
        addQuestInfo(mplew, chr);
        mplew.writeLong(0);// 커플링
        for (int x = 0; x < 28; x++) {
            mplew.write(CHAR_INFO_MAGIC);
        }
        mplew.writeShort(0);//todo: area keys and w/e
        mplew.writeInt(0); // 파티퀘스트
        mplew.write0(52);
        mplew.writeShort(1);
        mplew.write(0);
        mplew.writeLong(getTime(System.currentTimeMillis()));
        mplew.write(100);
        mplew.writeInt(0);
        mplew.write(1);
        return mplew.getPacket();
    }

    /**
     * Gets an empty stat update.
     *
     * @return The empy stat update packet.
     */
    public static MaplePacket enableActions() {
        return updatePlayerStats(EMPTY_STATUPDATE, true, false, null);
    }

    /**
     * Gets an update for specified stats.
     *
     * @param stats The stats to update.
     * @return The stat update packet.
     */
    public static MaplePacket updatePlayerStats(List<Pair<MapleStat, Integer>> stats) {
        return updatePlayerStats(stats, false, false, null);
    }

    public static MaplePacket updatePlayerStats(List<Pair<MapleStat, Integer>> stats, boolean itemReaction) {
        return updatePlayerStats(stats, itemReaction, false, null);
    }

    /**
     * Gets an update for specified stats.
     *
     * @param stats The list of stats to update.
     * @param itemReaction Result of an item reaction(?)
     * @return The stat update packet.
     */
    public static MaplePacket updatePlayerStats(List<Pair<MapleStat, Integer>> stats, boolean itemReaction, boolean extendSPJob, ExtendedSPTable table) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        mplew.write(itemReaction ? 1 : 0);
        int updateMask = 0;
        for (Pair<MapleStat, Integer> statupdate : stats) {
            updateMask |= statupdate.getLeft().getValue();
        }
        List<Pair<MapleStat, Integer>> mystats = stats;
        if (mystats.size() > 1) {
            Collections.sort(mystats, new Comparator<Pair<MapleStat, Integer>>() {
                @Override
                public int compare(Pair<MapleStat, Integer> o1, Pair<MapleStat, Integer> o2) {
                    int val1 = o1.getLeft().getValue();
                    int val2 = o2.getLeft().getValue();
                    return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
                }
            });
        }
        mplew.writeInt(updateMask);
        for (Pair<MapleStat, Integer> statupdate : mystats) {
            if (statupdate.getLeft().getValue() >= 1) {
                if (statupdate.getLeft().getValue() == 0x1) {
                    mplew.writeShort(statupdate.getRight().shortValue());
                } else if (statupdate.getLeft() == MapleStat.AVAILABLESP) {
                    if(extendSPJob) {
                        table.addSPData(mplew);
                    } else {
                        mplew.writeShort(statupdate.getRight().shortValue());
                    }
                } else if (statupdate.getLeft().getValue() <= 0x4) {
                    mplew.writeInt(statupdate.getRight());
                } else if (statupdate.getLeft().getValue() < 0x20) {
                    mplew.write(statupdate.getRight().shortValue());
                } else if (statupdate.getLeft().getValue() >= 0x400 && statupdate.getLeft().getValue() <= 0x2000) {
                    mplew.writeInt(statupdate.getRight().intValue());
                } else if (statupdate.getLeft().getValue() < 0xFFFF) {
                    mplew.writeShort(statupdate.getRight().shortValue());
                } else {
                    mplew.writeInt(statupdate.getRight().intValue());
                }
            }
        }
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket updateExtendedSP(int newSP) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        mplew.write(0); //items don't cause you to gain SP
        mplew.write(HexTool.getByteArrayFromHexString("00 80 00 00"));//maple stat mask for SP, we're hard coding it so we might as well just echo it to save work
        if(newSP > 0) {
            mplew.write(1);
            mplew.write(1);
            mplew.write(newSP);
        } else {
            mplew.write(0);
        }
        mplew.write(0);
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to change maps.
     *
     * @param to The <code>MapleMap</code> to warp to.
     * @param spawnPoint The spawn portal number to spawn at.
     * @param chr The character warping to <code>to</code>
     * @return The map change packet.
     */
    public static MaplePacket getWarpToMap(MapleMap to, int spawnPoint, MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());
        mplew.writeLong(chr.getClient().getChannel() - 1);
        mplew.write(0); // Count
        mplew.write(2);
        mplew.writeLong(0);
        mplew.writeInt(to.getId());
        mplew.write(spawnPoint);
        mplew.writeInt(chr.getHp());
        mplew.writeLong(getTime(System.currentTimeMillis()));
        mplew.writeInt(100);
        mplew.write(0);
        mplew.write(1);
        return mplew.getPacket();
    }

    /**
     * Gets a packet to spawn a portal.
     *
     * @param townId The ID of the town the portal goes to.
     * @param targetId The ID of the target.
     * @param pos Where to put the portal.
     * @return The portal spawn packet.
     */
    public static MaplePacket spawnPortal(int townId, int targetId, Point pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(14);
        mplew.writeShort(SendPacketOpcode.SPAWN_PORTAL.getValue());
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
        if (pos != null) {
            mplew.writeShort(pos.x);
            mplew.writeShort(pos.y);
        }
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     * Gets a packet to spawn a door.
     *
     * @param oid The door's object ID.
     * @param pos The position of the door.
     * @param town
     * @return The remove door packet.
     */
    public static MaplePacket spawnDoor(int oid, Point pos, boolean town) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
        mplew.writeShort(SendPacketOpcode.SPAWN_DOOR.getValue());
        mplew.write(town ? 1 : 0);
        mplew.writeInt(oid);
        mplew.writeShort(pos.x);
        mplew.writeShort(pos.y);
        return mplew.getPacket();
    }

    /**
     * Gets a packet to remove a door.
     *
     * @param oid The door's ID.
     * @param town
     * @return The remove door packet.
     */
    public static MaplePacket removeDoor(int oid, boolean town) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);
        if (town) {
            mplew.writeShort(SendPacketOpcode.SPAWN_PORTAL.getValue());
            mplew.writeInt(999999999);
            mplew.writeInt(999999999);
        } else {
            mplew.writeShort(SendPacketOpcode.REMOVE_DOOR.getValue());
            mplew.write(0);
            mplew.writeInt(oid);
        }
        return mplew.getPacket();
    }

    /**
     * Gets a packet to spawn a special map object.
     *
     * @param summon
     * @param skillLevel The level of the skill used.
     * @param animated Animated spawn?
     * @return The spawn packet for the map object.
     */
    public static MaplePacket spawnSpecialMapObject(MapleSummon summon, int skillLevel, boolean animated) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SPAWN_SPECIAL_MAPOBJECT.getValue());
        mplew.writeInt(summon.getOwner().getId());
        mplew.writeInt(summon.getObjectId()); // Supposed to be Object ID, but this works too! <3
        mplew.writeInt(summon.getSkill());
        mplew.write(0xC8);
        mplew.write(skillLevel);
        mplew.writeShort(summon.getPosition().x);
        mplew.writeShort(summon.getPosition().y);
        mplew.write0(3);
        mplew.write(0); // test
        mplew.write(summon.getMovementType().getValue()); // 0 = don't move, 1 = follow (4th mage summons?), 2/4 = only tele follow, 3 = bird follow
        mplew.write(1); // 0 and the summon can't attack - but puppets don't attack with 1 either ^.-
        mplew.write(animated ? 0 : 1);
        mplew.write(0);
        System.out.println("Summon packet: " + mplew.getPacket().toString());
        return mplew.getPacket();
    }

    /**
     * Gets a packet to remove a special map object.
     *
     * @param summon
     * @param animated Animated removal?
     * @return The packet removing the object.
     */
    public static MaplePacket removeSpecialMapObject(MapleSummon summon, boolean animated) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
        mplew.writeShort(SendPacketOpcode.REMOVE_SPECIAL_MAPOBJECT.getValue());
        mplew.writeInt(summon.getOwner().getId());
        mplew.writeInt(summon.getObjectId());
        mplew.write(animated ? 4 : 1); // ?
        mplew.writeShort(0);//v88
        return mplew.getPacket();
    }

    /**
     * Gets the response to a relog request.
     *
     * @return The relog response packet.
     */
    public static MaplePacket getRelogResponse() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendPacketOpcode.RELOG_RESPONSE.getValue());
        mplew.write(1);
        return mplew.getPacket();
    }

    /**
     * Gets a server message packet.
     *
     * @param message The message to convey.
     * @return The server message packet.
     */
    public static MaplePacket serverMessage(String message) {
        return serverMessage(4, 0, message, true, false);
    }

    /**
     * Gets a server notice packet.
     *
     * Possible values for <code>type</code>:<br>
     * 0: [Notice]<br>
     * 1: Popup<br>
     * 2: Megaphone<br>
     * 3: Super Megaphone<br>
     * 4: Scrolling message at top<br>
     * 5: Pink Text<br>
     * 6: Lightblue Text
     *
     * @param type The type of the notice.
     * @param message The message to convey.
     * @return The server notice packet.
     */
    public static MaplePacket serverNotice(int type, String message) {
        return serverMessage(type, 0, message, false, false);
    }

    /**
     * Gets a server notice packet.
     *
     * Possible values for <code>type</code>:<br>
     * 0: [Notice]<br>
     * 1: Popup<br>
     * 2: Megaphone<br>
     * 3: Super Megaphone<br>
     * 4: Scrolling message at top<br>
     * 5: Pink Text<br>
     * 6: Lightblue Text
     *
     * @param type The type of the notice.
     * @param channel The channel this notice was sent on.
     * @param message The message to convey.
     * @return The server notice packet.
     */
    public static MaplePacket serverNotice(int type, int channel, String message) {
        return serverMessage(type, channel, message, false, false);
    }

    public static MaplePacket serverNotice(int type, int channel, String message, boolean smegaEar) {
        return serverMessage(type, channel, message, false, smegaEar);
    }

    /**
     * Gets a server message packet.
     *
     * Possible values for <code>type</code>:<br>
     * 0: [Notice]<br>
     * 1: Popup<br>
     * 2: Megaphone<br>
     * 3: Super Megaphone<br>
     * 4: Scrolling message at top<br>
     * 5: Pink Text<br>
     * 6: Lightblue Text
     *
     * @param type The type of the notice.
     * @param channel The channel this notice was sent on.
     * @param message The message to convey.
     * @param servermessage Is this a scrolling ticker?
     * @return The server notice packet.
     */
    private static MaplePacket serverMessage(int type, int channel, String message, boolean servermessage, boolean megaEar) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(type);
        if (servermessage) {
            mplew.write(1);
        }
        mplew.writeMapleAsciiString(message);
        if (type == 3) {
            mplew.write(channel - 1); // channel
            mplew.write(megaEar ? 1 : 0);
        } else if (type == 6) {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    /**
     * Sends a Avatar Super Megaphone packet.
     *
     * @param chr The character name.
     * @param medal The medal text.
     * @param channel Which channel.
     * @param itemId Which item used.
     * @param message The message sent.
     * @param ear Whether or not the ear is shown for whisper.
     * @return
     */
    public static MaplePacket getAvatarMega(MapleCharacter chr, String medal, int channel, int itemId, List<String> message, boolean ear) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.AVATAR_MEGA.getValue());
        mplew.writeInt(itemId);
        mplew.writeMapleAsciiString(medal + chr.getName());
        for (String s : message) {
            mplew.writeMapleAsciiString(s);
        }
        mplew.writeInt(channel - 1); // channel
        mplew.write(ear ? 1 : 0);
        addCharLook(mplew, chr, true);
        return mplew.getPacket();
    }

    /**
     * Sends the Gachapon green message when a user uses a gachapon ticket.
     * @param item
     * @param town
     * @param player
     * @return
     */
    public static MaplePacket gachaponMessage(IItem item, String town, MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(0x0B);
        mplew.writeMapleAsciiString(player.getName() + " : got a(n)");
        mplew.writeInt(0); //random?
        mplew.writeMapleAsciiString(town);
        addItemInfo(mplew, item, false, false, true, false);
        return mplew.getPacket();
    }

    public static MaplePacket spawnNPC(MapleNPC life) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(24);
        mplew.writeShort(SendPacketOpcode.SPAWN_NPC.getValue());
        mplew.writeInt(life.getObjectId());
        mplew.writeInt(life.getId());
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getCy());
        mplew.write(life.getF() == 1 ? 0 : 1);
        mplew.writeShort(life.getFh());
        mplew.writeShort(life.getRx0());
        mplew.writeShort(life.getRx1());
        mplew.write(1);
        return mplew.getPacket();
    }

    public static MaplePacket spawnNPCRequestController(MapleNPC life, boolean MiniMap) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(23);
        mplew.writeShort(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        mplew.write(1);
        mplew.writeInt(life.getObjectId());
        mplew.writeInt(life.getId());
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getCy());
        mplew.write(life.getF() == 1 ? 0 : 1);
        mplew.writeShort(life.getFh());
        mplew.writeShort(life.getRx0());
        mplew.writeShort(life.getRx1());
        mplew.write(MiniMap ? 1 : 0);
        return mplew.getPacket();
    }

    /**
     * Gets a spawn monster packet.
     *
     * @param life The monster to spawn.
     * @param newSpawn Is it a new spawn?
     * @return The spawn monster packet.
     */
    public static MaplePacket spawnMonster(MapleMonster life, boolean newSpawn) {
        return spawnMonsterInternal(life, false, newSpawn, false, 0, false);
    }

    /**
     * Gets a spawn monster packet.
     *
     * @param life The monster to spawn.
     * @param newSpawn Is it a new spawn?
     * @param effect The spawn effect.
     * @return The spawn monster packet.
     */
    public static MaplePacket spawnMonster(MapleMonster life, boolean newSpawn, int effect) {
        return spawnMonsterInternal(life, false, newSpawn, false, effect, false);
    }

    /**
     * Gets a control monster packet.
     *
     * @param life The monster to give control to.
     * @param newSpawn Is it a new spawn?
     * @param aggro Aggressive monster?
     * @return The monster control packet.
     */
    public static MaplePacket controlMonster(MapleMonster life, boolean newSpawn, boolean aggro) {
        return spawnMonsterInternal(life, true, newSpawn, aggro, 0, false);
    }

    /**
     * Makes a monster invisible for Ariant PQ.
     * @param life
     * @return
     */
    public static MaplePacket makeMonsterInvisible(MapleMonster life) {
        return spawnMonsterInternal(life, true, false, false, 0, true);
    }

    /**
     * Internal function to handler monster spawning and controlling.
     *
     * @param life The mob to perform operations with.
     * @param requestController Requesting control of mob?
     * @param newSpawn New spawn (fade in?)
     * @param aggro Aggressive mob?
     * @param effect The spawn effect to use.
     * @return The spawn/control packet.
     */
    private static MaplePacket spawnMonsterInternal(MapleMonster life, boolean requestController, boolean newSpawn, boolean aggro, int effect, boolean makeInvis) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (makeInvis) {
            mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
            mplew.write(0);
            mplew.writeInt(life.getObjectId());
            return mplew.getPacket();
        }
        if (requestController) {
            mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
            if (aggro) {
                mplew.write(2);
            } else {
                mplew.write(1);
            }
        } else {
            mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER.getValue());
        }
        mplew.writeInt(life.getObjectId());
        //mplew.write(life.getController() == null ? 5 : 1); // ????!? either 5 or 1?  5 if has no controller, 1 if so
        mplew.write(5);//v88 just try 5
        mplew.writeInt(life.getId());
        mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 E0 03 00 00 00 00 00 88 00 00 00 00 00 00 00 00 8C 93 00 00 00 00 00 00 00 00 8C 93 00 00 00 00 00 00 00 00 8C 93 00 00 00 00 00 00 00 00 8C 93 00 00 00 00"));
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getPosition().y);
        mplew.write(life.getStance());
        mplew.writeShort(life.getStartFh());
        mplew.writeShort(life.getFh());
        if (effect > 0) {
            mplew.write(effect);
            mplew.write(0);
            mplew.writeShort(0);
            if (effect == 15) {//15 seems to add a byte... (Dojo spawn effect)
                mplew.write(0);
            }
        }
        if (newSpawn) {
            mplew.writeShort(-2);
        } else {
            mplew.writeShort(-1);
        }
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     * Handles monsters not being targettable, such as Zakum's first body.
     * @param life The mob to spawn as non-targettable.
     * @param effect The effect to show when spawning.
     * @return The packet to spawn the mob as non-targettable.
     */
    public static MaplePacket spawnFakeMonster(MapleMonster life, int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(38);
        mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
        mplew.write(1);
        mplew.writeInt(life.getObjectId());
   /*     mplew.write(5);
        mplew.writeInt(life.getId());
        mplew.writeInt(0);*/

        mplew.write(life.getController() == null ? 5 : 1); // ????!? either 5 or 1?  5 if has no controller, 1 if so
        mplew.writeInt(life.getId());

        mplew.writeLong(0);
        mplew.writeInt(0);
        mplew.writeShort(0);

        mplew.write(0);

        mplew.write(0x88);
        mplew.writeShort(0);
        mplew.writeInt(0);

        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getPosition().y);
        mplew.write(life.getStance());
        mplew.writeShort(life.getStartFh());
        mplew.writeShort(life.getFh());
        if (effect > 0) {
            mplew.write(effect);
            mplew.write(0);
            mplew.writeShort(0);
        }
        mplew.writeShort(-2);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     * Makes a monster previously spawned as non-targettable, targettable.
     * @param life The mob to make targettable.
     * @return The packet to make the mob targettable.
     */
    public static MaplePacket makeMonsterReal(MapleMonster life) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(30);
        mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER.getValue());
        mplew.writeInt(life.getObjectId());
        mplew.write(5);
        mplew.writeInt(life.getId());
       // mplew.writeInt(0);
        mplew.writeLong(0);
        mplew.writeInt(0);
        mplew.writeShort(0);

        mplew.write(0);

        mplew.write(0x88);
        mplew.writeShort(0);
        mplew.writeInt(0);
        
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getPosition().y);
        mplew.write(life.getStance());
        mplew.writeShort(life.getStartFh());
        mplew.writeShort(life.getFh());
        mplew.writeShort(-1);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     * Gets a stop control monster packet.
     *
     * @param oid The ObjectID of the monster to stop controlling.
     * @return The stop control monster packet.
     */
    public static MaplePacket stopControllingMonster(int oid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
        mplew.write(0);
        mplew.writeInt(oid);
        return mplew.getPacket();
    }

    /**
     * Gets a response to a move monster packet.
     *
     * @param objectid The ObjectID of the monster being moved.
     * @param moveid The movement ID.
     * @param currentMp The current MP of the monster.
     * @param useSkills Can the monster use skills?
     * @return The move response packet.
     */
    public static MaplePacket moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills) {
        return moveMonsterResponse(objectid, moveid, currentMp, useSkills, 0, 0);
    }

    /**
     * Gets a response to a move monster packet.
     *
     * @param objectid The ObjectID of the monster being moved.
     * @param moveid The movement ID.
     * @param currentMp The current MP of the monster.
     * @param useSkills Can the monster use skills?
     * @param skillId The skill ID for the monster to use.
     * @param skillLevel The level of the skill to use.
     * @return The move response packet.
     */
    public static MaplePacket moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills, int skillId, int skillLevel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(17);
        mplew.writeShort(SendPacketOpcode.MOVE_MONSTER_RESPONSE.getValue());
        mplew.writeInt(objectid);
        mplew.writeShort(moveid);
        mplew.write(useSkills ? 1 : 0);
        mplew.writeShort(currentMp);
        mplew.write(skillId);
        mplew.write(skillLevel);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     * Gets a general chat packet.
     *
     * @param cidfrom The character ID who sent the chat.
     * @param text The text of the chat.
     * @param whiteBG
     * @param show
     * @return The general chat packet.
     */
    public static MaplePacket getChatText(int cidfrom, String text, boolean whiteBG, int show) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CHATTEXT.getValue());
        mplew.writeInt(cidfrom);
        mplew.write(whiteBG ? 1 : 0);
        mplew.writeMapleAsciiString(text);
        mplew.write(show);
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to show an EXP increase.
     *
     * @param gain The amount of EXP gained.
     * @param inChat In the chat box?
     * @param white White text or yellow?
     * @param party In party or not
     * @return The exp gained packet.
     */
    public static MaplePacket getShowExpGain(int gain, boolean inChat, boolean white, byte party) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(3); // 3 = exp, 4 = fame, 5 = mesos, 6 = guildpoints
        mplew.write(white ? 1 : 0);
        mplew.writeInt(gain);
        mplew.write(inChat ? 1 : 0);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to show a fame gain.
     *
     * @param gain How many fame gained.
     * @return The meso gain packet.
     */
    public static MaplePacket getShowFameGain(int gain) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(4);
        mplew.writeInt(gain);
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to show a meso gain.
     *
     * @param gain How many mesos gained.
     * @return The meso gain packet.
     */
    public static MaplePacket getShowMesoGain(int gain) {
        return getShowMesoGain(gain, false);
    }

    /**
     * Gets a packet telling the client to show a meso gain.
     *
     * @param gain How many mesos gained.
     * @param inChat Show in the chat window?
     * @return The meso gain packet.
     */
    public static MaplePacket getShowMesoGain(int gain, boolean inChat) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        if (!inChat) {
            mplew.write(0);
            mplew.write(1);
            mplew.write(0);
            mplew.writeInt(gain);
            mplew.writeShort(0);
        } else {
            mplew.write(6);
            mplew.writeInt(gain);
            mplew.writeInt(-1);
        }
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to show a item gain.
     *
     * @param itemId The ID of the item gained.
     * @param quantity How many items gained.
     * @return The item gain packet.
     */
    public static MaplePacket getShowItemGain(int itemId, short quantity) {
        return getShowItemGain(itemId, quantity, false);
    }

    /**
     * Gets a packet telling the client to show an item gain.
     *
     * @param itemId The ID of the item gained.
     * @param quantity The number of items gained.
     * @param inChat Show in the chat window?
     * @return The item gain packet.
     */
    public static MaplePacket getShowItemGain(int itemId, short quantity, boolean inChat) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(20);
        if (inChat) {
            mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            mplew.write(5);
            mplew.write(1);
            mplew.writeInt(itemId);
            mplew.writeInt(quantity);
        } else {
            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.writeShort(0);
            mplew.writeInt(itemId);
            mplew.writeInt(quantity);
        }
        return mplew.getPacket();
    }

    public static MaplePacket killMonster(int oid, boolean animation) {
        return killMonster(oid, animation ? 1 : 0);
    }

    /**
     * Gets a packet telling the client that a monster was killed.
     *
     * @param oid The objectID of the killed monster.
     * @param animation 0 = dissapear, 1 = fade out, 2+ = special
     * @return The kill monster packet.
     */
    public static MaplePacket killMonster(int oid, int animation) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
        mplew.writeShort(SendPacketOpcode.KILL_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(animation);
        return mplew.getPacket();
    }

    /**
     * Gets a packet telling the client to show mesos coming out of a map
     * object.
     *
     * @param amount The amount of mesos.
     * @param itemoid The ObjectID of the dropped mesos.
     * @param dropperoid The OID of the dropper.
     * @param ownerid The ID of the drop owner.
     * @param dropfrom Where to drop from.
     * @param dropto Where the drop lands.
     * @param mod ?
     * @return The drop mesos packet.
     */
    public static MaplePacket dropMesoFromMapObject(int amount, int itemoid, int dropperoid, int ownerid, Point dropfrom, Point dropto, byte mod, byte type) {
        return dropItemFromMapObjectInternal(amount, itemoid, dropperoid, ownerid, dropfrom, dropto, mod, true, type);
    }

    public static MaplePacket dropMesoFromMapObject(int amount, int itemoid, int dropperoid, int ownerid, Point dropfrom, Point dropto, byte mod) {
        return dropMesoFromMapObject(amount, itemoid, dropperoid, ownerid, dropfrom, dropto, mod, (byte) 0);
    }

    /**
     * Gets a packet telling the client to show an item coming out of a map
     * object.
     *
     * @param itemid The ID of the dropped item.
     * @param itemoid The ObjectID of the dropped item.
     * @param dropperoid The OID of the dropper.
     * @param ownerid The ID of the drop owner.
     * @param dropfrom Where to drop from.
     * @param dropto Where the drop lands.
     * @param mod ?
     * @return The drop mesos packet.
     */
    public static MaplePacket dropItemFromMapObject(int itemid, int itemoid, int dropperoid, int ownerid, Point dropfrom, Point dropto, byte mod, byte type) {
        return dropItemFromMapObjectInternal(itemid, itemoid, dropperoid, ownerid, dropfrom, dropto, mod, false, type);
    }

    public static MaplePacket dropItemFromMapObject(int itemid, int itemoid, int dropperoid, int ownerid, Point dropfrom, Point dropto, byte mod) {
        return dropItemFromMapObject(itemid, itemoid, dropperoid, ownerid, dropfrom, dropto, mod, (byte) 0);
    }

    /**
     * Internal function to get a packet to tell the client to drop an item onto
     * the map.
     *
     * @param itemid The ID of the item to drop.
     * @param itemoid The ObjectID of the dropped item.
     * @param dropperoid The OID of the dropper.
     * @param ownerid The ID of the drop owner.
     * @param dropfrom Where to drop from.
     * @param dropto Where the drop lands.
     * @param mod ?
     * @param mesos Is the drop mesos?
     * @return The item drop packet.
     */
    public static MaplePacket dropItemFromMapObjectInternal(int itemid, int itemoid, int dropperoid, int ownerid, Point dropfrom, Point dropto, byte mod, boolean mesos, byte type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
        mplew.write(mod);
        mplew.writeInt(itemoid);
        mplew.write(mesos ? 1 : 0); // 1 = mesos, 0 =item
        mplew.writeInt(itemid);
        mplew.writeInt(ownerid); // owner charid
        mplew.write(mesos ? 4 : type); //<-----  0 = timeout for anyone who isn't the owner (like what happens with normal drops), 1 = timeout for owner's party, 2 = FFA (Free for all), 3 = explosive/FFA
        mplew.writeShort(dropto.x);
        mplew.writeShort(dropto.y);
        mplew.writeInt(ownerid); // drop owner, 0 for bosses
        if (mod != 2) {
            mplew.writeShort(dropfrom.x);
            mplew.writeShort(dropfrom.y);
            mplew.writeShort(0);
        }
        if (!mesos) {
            mplew.write(0);
            mplew.write(ITEM_MAGIC);
            addExpirationTime(mplew, System.currentTimeMillis(), false);
        }
        mplew.write(1); //pet EQP pickup
        mplew.write(1); //pet EQP pickup enable
        return mplew.getPacket();
    }

    /**
     * Gets a packet spawning a player as a mapobject to other clients.
     *
     * @param chr The character to spawn to other clients.
     * @return The spawn player packet.
     */
    public static MaplePacket spawnPlayerMapobject(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SPAWN_PLAYER.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(chr.getLevel());
        mplew.writeMapleAsciiString(chr.getName());
        mplew.writeShort(0);
        if (chr.getGuildId() < 1) {
            mplew.writeMapleAsciiString("");
            mplew.write0(6);
        } else {
            MapleGuildSummary gs = chr.getClient().getChannelServer().getGuildSummary(chr.getGuildId());
            if (gs != null) {
                mplew.writeMapleAsciiString(gs.getName());
                mplew.writeShort(gs.getLogoBG());
                mplew.write(gs.getLogoBGColor());
                mplew.writeShort(gs.getLogo());
                mplew.write(gs.getLogoColor());
            } else {
                mplew.writeMapleAsciiString("");
                mplew.write0(6);
            }
        }
        mplew.write0(3);
        mplew.writeShort(0xFE);
        mplew.write(0x80);
        mplew.write(2);
        mplew.write0(17);
        /*if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
            mplew.writeInt(2);
        } else {
            mplew.writeInt(0);
        }*/
        long buffmask = 0;
        Integer buffvalue = null;
        if (chr.getBuffedValue(MapleBuffStat.DARKSIGHT) != null && !chr.isHidden()) {
            buffmask |= MapleBuffStat.DARKSIGHT.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.COMBO) != null) {
            buffmask |= MapleBuffStat.COMBO.getValue();
            buffvalue = Integer.valueOf(chr.getBuffedValue(MapleBuffStat.COMBO).intValue());
        }
        /*if (chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) {
            buffmask |= MapleBuffStat.SHADOWPARTNER.getValue();
            buffvalue = Integer.valueOf(chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER).intValue());
        }*/
        if (chr.getBuffedValue(MapleBuffStat.SOULARROW) != null) {
            buffmask |= MapleBuffStat.SOULARROW.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
            buffvalue = Integer.valueOf(chr.getBuffedValue(MapleBuffStat.MORPH).intValue());
        }
        if (chr.getJob() == MapleJob.DARKKNIGHT) {
            buffmask |= 0x7000000;
        }
        mplew.writeInt(0);
        mplew.writeInt((int) ((buffmask >> 32) & 0xffffffffL));
        if (buffvalue != null) {
            if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
                mplew.writeShort(buffvalue);
            } else if (chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) {
                mplew.writeShort(buffvalue);
                mplew.writeInt(4111002);
            } else {
                mplew.write(buffvalue.byteValue());
            }
        }
        mplew.writeInt(-1);
        mplew.writeInt((int) (buffmask & 0xffffffffL));
        int CHAR_MAGIC_SPAWN = Randomizer.getInstance().nextInt();
        mplew.write0(8);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.write0(11);
        mplew.writeInt(CHAR_MAGIC_SPAWN);//v74
        mplew.write0(11);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.write0(3);
        IItem mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18);
        if (chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null && mount != null) {
            mplew.writeInt(mount.getItemId());
            mplew.writeInt(1004);
        } else {
            mplew.writeLong(0);
        }
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.write0(9);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.write(HexTool.getByteArrayFromHexString("00 01 79 02 D2 01"));
        mplew.write0(10);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.write0(17);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.write0(3);
        mplew.writeShort(chr.getJob().getId());
        mplew.writeShort(2);
        addCharLook(mplew, chr, false);
        mplew.write0(21);
        mplew.writeInt(chr.getTitleItem());
        mplew.writeInt(0);
        mplew.writeInt(chr.getItemEffect());
        mplew.writeInt(chr.getChair());
        mplew.writeShort(chr.getPosition().x);
        mplew.writeShort(chr.getPosition().y);
        mplew.write(chr.getStance());
        mplew.writeShort(0); // 풋홀드
        mplew.write(0);
        mplew.writeInt(1); // 라이딩레벨
        mplew.writeInt(0); // 라이딩경험치
        mplew.writeInt(0); // 라이딩포만감
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    private static void checkRing(MaplePacketLittleEndianWriter mplew, List<MapleRing> ringlist) {
        if (ringlist.size() == 0) {
            mplew.write(0);
        } else {
            addRingPacketInfo(mplew, ringlist);
        }
    }

    private static void addRingPacketInfo(MaplePacketLittleEndianWriter mplew, List<MapleRing> ringlist) {
        for (MapleRing mr : ringlist) {
            mplew.write(1);
            mplew.writeInt(mr.getRingId());
            mplew.writeInt(0);
            mplew.writeInt(mr.getPartnerRingId());
            mplew.writeInt(0);
            mplew.writeInt(mr.getItemId());
        }
    }

    /**
     * Adds a announcement box to an existing MaplePacketLittleEndianWriter.
     *
     * @param mplew The MaplePacketLittleEndianWriter to add an announcement box to.
     * @param shop The shop to announce.
     */
    private static void addAnnounceBox(MaplePacketLittleEndianWriter mplew, MaplePlayerShop shop, int availability) {
        mplew.write(4);
        mplew.writeInt(shop.getObjectId());
        mplew.writeMapleAsciiString(shop.getDescription());
        mplew.write(0);
        mplew.write(0);
        mplew.write(1);
        mplew.write(availability);
        mplew.write(0);
    }

    private static void addAnnounceBox(MaplePacketLittleEndianWriter mplew, MapleMiniGame game, int gametype, int type, int ammount, int joinable) {
        mplew.write(gametype);
        mplew.writeInt(game.getObjectId()); // gameid/shopid
        mplew.writeMapleAsciiString(game.getDescription()); // desc
        mplew.write(0);
        mplew.write(type);
        mplew.write(ammount);
        mplew.write(2);
        mplew.write(joinable);
    }

    public static MaplePacket facialExpression(MapleCharacter from, int expression) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.FACIAL_EXPRESSION.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(expression);
        mplew.writeInt(-1);//v88
        mplew.write(0);//v88
        return mplew.getPacket();
    }

    private static void serializeMovementList(LittleEndianWriter lew, List<LifeMovementFragment> moves) {
        lew.write(moves.size());
        for (LifeMovementFragment move : moves) {
            move.serialize(lew);
        }
    }

    public static MaplePacket movePlayer(int cid, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MOVE_PLAYER.getValue());
        mplew.writeInt(cid);
        mplew.write0(8);//was 4, might have to echo junk
        serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }

    public static MaplePacket moveSummon(int cid, int oid, Point startPos, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MOVE_SUMMON.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(oid);
        mplew.writeShort(startPos.x);
        mplew.writeShort(startPos.y);
        mplew.writeInt(0);//new in v88
        serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }

    public static MaplePacket moveDragon(int cid, Point startPos, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MOVE_DRAGON.getValue());
        mplew.writeInt(cid);
        mplew.writeShort(startPos.x);
        mplew.writeShort(startPos.y);
        mplew.writeInt(0);//new in v88
        serializeMovementList(mplew, moves);
  //      System.out.println(mplew.getPacket().toString());
        return mplew.getPacket();
    }

    public static MaplePacket moveMonster(int useskill, int skill, int skill_1, int skill_2, int skill_3, int oid, Point startPos, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MOVE_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(useskill);
        mplew.write(skill);
        mplew.write(skill_1);
        mplew.write(skill_2);
        mplew.write(skill_3);
        mplew.write(0);
        mplew.writeShort(0);
        mplew.writeShort(startPos.x);
        mplew.writeShort(startPos.y);
        mplew.writeInt(100924656);
        serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }

    public static MaplePacket summonAttack(int cid, int summonSkillId, int newStance, List<SummonAttackEntry> allDamage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SUMMON_ATTACK.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);

        mplew.write(0); //guess

        mplew.write(newStance);
        mplew.write(allDamage.size());
        for (SummonAttackEntry attackEntry : allDamage) {
            mplew.writeInt(attackEntry.getMonsterOid()); // oid
            //mplew.write(6); // who knows
            mplew.write0(18);
            mplew.writeInt(attackEntry.getDamage()); // damage
        }
        return mplew.getPacket();
    }

    public static MaplePacket closeRangeAttack(int cid, int skill, int stance, int numAttackedAndDamage, List<Pair<Integer, List<Integer>>> damage, int speed, byte UNK80, int charge) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CLOSE_RANGE_ATTACK.getValue());
        if (skill == 4211006) {
            addMesoExplosion(mplew, cid, skill, stance, numAttackedAndDamage, 0, damage, speed, UNK80);
        } else {
            addAttackBody(mplew, cid, skill, stance, numAttackedAndDamage, 0, damage, speed, UNK80, charge);
        }
        return mplew.getPacket();
    }

    public static MaplePacket rangedAttack(int cid, int skill, int stance, int numAttackedAndDamage, int projectile, List<Pair<Integer, List<Integer>>> damage, int speed, byte UNK80, int charge, Point pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.RANGED_ATTACK.getValue());
        addAttackBody(mplew, cid, skill, stance, numAttackedAndDamage, projectile, damage, speed, UNK80, charge);
        mplew.writeInt(0); // 포지션
        return mplew.getPacket();
    }

    public static MaplePacket magicAttack(int cid, int skill, int stance, int numAttackedAndDamage, List<Pair<Integer, List<Integer>>> damage, int charge, int speed, byte UNK80) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MAGIC_ATTACK.getValue());
        addAttackBody(mplew, cid, skill, stance, numAttackedAndDamage, 0, damage, speed, UNK80, charge);
 
        return mplew.getPacket();
    }

    private static void addAttackBody(LittleEndianWriter lew, int cid, int skill, int stance, int numAttackedAndDamage, int projectile, List<Pair<Integer, List<Integer>>> damage, int speed, byte UNK80, int charge) {
        lew.writeInt(cid);
        lew.write(numAttackedAndDamage);
        lew.write(1); // 레벨
        if (skill > 0) {
            lew.write(0xFF); // 스킬레벨
            lew.writeInt(skill);
        } else {
            lew.write(0);
        }
        lew.write(0);
        lew.write(UNK80);//unkv80 ?
        lew.write(stance);
        lew.write(speed);
        lew.write(0);
        lew.writeInt(projectile);
        for (Pair<Integer, List<Integer>> oned : damage) {
            if (oned.getRight() != null) {
                lew.writeInt(oned.getLeft().intValue());
                lew.write(0x07);
                for (Integer eachd : oned.getRight()) {
                    lew.writeInt(eachd.intValue());
                }
            }
        }

        if (charge != -1) {
            lew.writeLong(charge);
        }
    }

    private static void addMesoExplosion(LittleEndianWriter lew, int cid, int skill, int stance, int numAttackedAndDamage, int projectile, List<Pair<Integer, List<Integer>>> damage, int speed, byte UNK80) {
        lew.writeInt(cid);
        lew.write(numAttackedAndDamage);

        lew.write(0x5B);

        lew.write(0x1E);
        lew.writeInt(skill);

        lew.write(0);
        lew.write(UNK80);//unkv80 ?
        lew.write(stance);
        lew.write(speed);
        lew.write(0x0A);
        lew.writeInt(projectile);
        for (Pair<Integer, List<Integer>> oned : damage) {
            if (oned.getRight() != null) {
                lew.writeInt(oned.getLeft().intValue());
                lew.write(0xFF);
                lew.write(oned.getRight().size());
                for (Integer eachd : oned.getRight()) {
                    lew.writeInt(eachd.intValue());
                }
            }
        }
    }

    private static int doubleToShortBits(double d) {
        return (int) (Double.doubleToLongBits(d) >> 48);
    }

    public static MaplePacket getNPCShop(MapleClient c, int sid, List<MapleShopItem> items) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.OPEN_NPC_SHOP.getValue());
        mplew.writeInt(0);
        mplew.writeInt(sid);
        mplew.writeShort(items.size()); // item count
        for (MapleShopItem item : items) {
            mplew.writeInt(item.getItemId());
            mplew.writeInt(item.getPrice());
            mplew.write0(4); // 특수아이템
            mplew.write0(4); // 특수아이템 필요개수 
            mplew.write0(4); // 구입 후 사용기간 (날짜 * 1440)
            mplew.write0(4); // 0
            mplew.write0(4); // 1 - 장비 2 - 소비 7 - 스페셜
            mplew.write0(1); // 1 일경우 미확인
            mplew.write0(4); // 구입제한개수
            if (!InventoryConstants.isRechargable(item.getItemId())) {
                mplew.writeShort(1); // stacksize o.o
                mplew.writeShort(item.getBuyable());
                mplew.write(0); // 재구매일경우 additeminfo
            } else {
                mplew.writeShort(0);
                mplew.writeInt(0);
                mplew.writeShort(doubleToShortBits(ii.getPrice(item.getItemId())));
                mplew.writeShort(ii.getSlotMax(c, item.getItemId()));
                mplew.write(0); // 재구매일경우 additeminfo
            }
        }
        return mplew.getPacket();
    }
    
    public static MaplePacket confirmShopTransaction(MapleClient c, byte code) {
        return confirmShopTransaction(c, code, 0, null);
    }

    public static MaplePacket confirmShopTransaction(MapleClient c, byte code, int sid, List<MapleShopItem> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        mplew.writeShort(SendPacketOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
        switch (code) {
            case 0: // 구입
                mplew.writeShort(code);
                break;
            case 4: // 판매
                mplew.write(code);
                mplew.writeInt(0);
                mplew.writeInt(sid);
                mplew.writeShort(items.size()); // item count
                for (MapleShopItem item : items) {
                    mplew.writeInt(item.getItemId());
                    mplew.writeInt(item.getPrice());
                    mplew.write0(4); // 특수아이템
                    mplew.write0(4); // 특수아이템 필요개수 
                    mplew.write0(4); // 구입 후 사용기간 (날짜 * 1440)
                    mplew.write0(4); // 0
                    mplew.write0(4); // 1 - 장비 2 - 소비 7 - 스페셜
                    mplew.write0(1); // 1 일경우 미확인
                    mplew.write0(4); // 구입제한개수
                    if (!InventoryConstants.isRechargable(item.getItemId())) {
                        mplew.writeShort(1); // stacksize o.o
                        mplew.writeShort(item.getBuyable());
                        mplew.write(0); // 재구매일경우 additeminfo
                    } else {
                        mplew.writeShort(0);
                        mplew.writeInt(0);
                        mplew.writeShort(doubleToShortBits(ii.getPrice(item.getItemId())));
                        mplew.writeShort(ii.getSlotMax(c, item.getItemId()));
                        mplew.write(0); // 재구매일경우 additeminfo
                    }
                }
                break;
            case 8: // 충전
                mplew.write(code);
                break;
        }
        return mplew.getPacket();
    }

    public static MaplePacket addInventorySlot(MapleInventoryType type, IItem item) {
        return addInventorySlot(type, item, false);
    }

    public static MaplePacket addInventorySlot(MapleInventoryType type, IItem item, boolean fromDrop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        if (fromDrop) {
            mplew.write(1);
        } else {
            mplew.write(0);
        }
        mplew.write(HexTool.getByteArrayFromHexString("01 00 00")); // add mode
        mplew.write(type.getType()); // iv type
        mplew.write(item.getPosition()); // slot id
        addItemInfo(mplew, item, true, false);
        return mplew.getPacket();
    }

    public static MaplePacket updateInventorySlot(MapleInventoryType type, IItem item) {
        return updateInventorySlot(type, item, false);
    }

    public static MaplePacket updateInventorySlot(MapleInventoryType type, IItem item, boolean fromDrop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        if (fromDrop) {
            mplew.write(1);
        } else {
            mplew.write(0);
        }
        mplew.write(HexTool.getByteArrayFromHexString("01 00 01")); // update
        mplew.write(type.getType()); // iv type
        mplew.write(item.getPosition()); // slot id
        mplew.write(0); // ?
        mplew.writeShort(item.getQuantity());
        return mplew.getPacket();
    }

    public static MaplePacket moveInventoryItem(MapleInventoryType type, byte src, byte dst) {
        return moveInventoryItem(type, src, dst, (byte) -1);
    }

    public static MaplePacket moveInventoryItem(MapleInventoryType type, byte src, byte dst, byte equipIndicator) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 01 00 02"));
        mplew.write(type.getType());
        mplew.writeShort(src);
        mplew.writeShort(dst);
        if (equipIndicator != -1) {
            mplew.write(equipIndicator);
        }
        return mplew.getPacket();
    }

    public static MaplePacket moveAndMergeInventoryItem(MapleInventoryType type, byte src, byte dst, short total) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 02 00 03"));
        mplew.write(type.getType());
        mplew.writeShort(src);
        mplew.write(1); // merge mode?
        mplew.write(type.getType());
        mplew.writeShort(dst);
        mplew.writeShort(total);
        return mplew.getPacket();
    }

    public static MaplePacket moveAndMergeWithRestInventoryItem(MapleInventoryType type, byte src, byte dst, short srcQ, short dstQ) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 02 00 01"));
        mplew.write(type.getType());
        mplew.writeShort(src);
        mplew.writeShort(srcQ);
        mplew.write(0x01);
        mplew.write(type.getType());
        mplew.writeShort(dst);
        mplew.writeShort(dstQ);
        return mplew.getPacket();
    }

    public static MaplePacket clearInventoryItem(MapleInventoryType type, byte slot, boolean fromDrop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(fromDrop ? 1 : 0);
        mplew.write(HexTool.getByteArrayFromHexString("01 00 03"));
        mplew.write(type.getType());
        mplew.writeShort(slot);
        return mplew.getPacket();
    }

    public static MaplePacket scrolledItem(IItem scroll, IItem item, boolean destroyed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1); // fromdrop always true
        mplew.write(destroyed ? 2 : 3);
        mplew.write(0);
        mplew.write(scroll.getQuantity() > 0 ? 1 : 3);
        mplew.write(MapleInventoryType.USE.getType());
        mplew.writeShort(scroll.getPosition());
        if (scroll.getQuantity() > 0) {
            mplew.writeShort(scroll.getQuantity());
        }
        mplew.write(3);
        if (!destroyed) {
            mplew.write(MapleInventoryType.EQUIP.getType());
            mplew.writeShort(item.getPosition());
            mplew.write(0);
        }
        mplew.write(MapleInventoryType.EQUIP.getType());
        mplew.writeShort(item.getPosition());
        if (!destroyed) {
            addItemInfo(mplew, item, true, true);
        }
        mplew.write(8);
        return mplew.getPacket();
    }

    public static MaplePacket getScrollEffect(int chr, ScrollResult scrollSuccess, boolean legendarySpirit, int scrollId, int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_SCROLL_EFFECT.getValue());
        mplew.writeInt(chr);
        switch (scrollSuccess) {
            case SUCCESS:
                mplew.writeShort(1);
                mplew.write(legendarySpirit ? 1 : 0);
                mplew.writeInt(scrollId);
                mplew.writeInt(itemId);
                break;
            case FAIL:
                mplew.writeShort(0);
                mplew.write(legendarySpirit ? 1 : 0);
                mplew.writeInt(scrollId);
                mplew.writeInt(itemId);
                break;
            case CURSE:
                mplew.write(0);
                mplew.write(1);
                mplew.write(legendarySpirit ? 1 : 0);
                mplew.writeInt(scrollId);
                mplew.writeInt(itemId);
                break;
        }
        return mplew.getPacket();
    }

    public static MaplePacket removePlayerFromMap(int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
        mplew.writeInt(cid);
        return mplew.getPacket();
    }

    /**
     * animation: 0 - expire<br/> 1 - without animation<br/> 2 - pickup<br/>
     * 4 - explode<br/> cid is ignored for 0 and 1
     *
     * @param oid
     * @param animation
     * @param cid
     * @return
     */
    public static MaplePacket removeItemFromMap(int oid, int animation, int cid) {
        return removeItemFromMap(oid, animation, cid, false, 0);
    }

    /**
     * animation: 0 - expire<br/> 1 - without animation<br/> 2 - pickup<br/>
     * 4 - explode<br/> cid is ignored for 0 and 1.<br /><br />Flagging pet
     * as true will make a pet pick up the item.
     *
     * @param oid
     * @param animation
     * @param cid
     * @param pet
     * @param slot
     * @return
     */
    public static MaplePacket removeItemFromMap(int oid, int animation, int cid, boolean pet, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        mplew.write(animation); // expire
        mplew.writeInt(oid);
        if (animation >= 2) {
            mplew.writeInt(cid);
            if (pet) {
                mplew.write(slot);
                mplew.writeLong(0); //safety pending proper cap
            }
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateCharLook(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_LOOK.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        addCharLook(mplew, chr, false);
        if (chr.getMarriageRings().size() + chr.getFriendshipRings().size() + chr.getCrushRings().size() > 0) {
            if (chr.getMarriageRings().size() > 0) {
                checkRing(mplew, chr.getCrushRings());
                checkRing(mplew, chr.getFriendshipRings());
                addRingPacketInfo(mplew, chr.getMarriageRings());
            } else if (chr.getFriendshipRings().size() > 0) {
                checkRing(mplew, chr.getCrushRings());
                addRingPacketInfo(mplew, chr.getFriendshipRings());
            } else if (chr.getCrushRings().size() > 0) {
                addRingPacketInfo(mplew, chr.getCrushRings());
            }
        } else {
            mplew.write0(8);
        }
        return mplew.getPacket();
    }

    public static MaplePacket dropInventoryItem(MapleInventoryType type, short src) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 01 00 03"));
        mplew.write(type.getType());
        mplew.writeShort(src);
        if (src < 0) {
            mplew.write(1);
        }
        return mplew.getPacket();
    }

    public static MaplePacket dropInventoryItemUpdate(MapleInventoryType type, IItem item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 01 00 01"));
        mplew.write(type.getType());
        mplew.writeShort(item.getPosition());
        mplew.writeShort(item.getQuantity());
        return mplew.getPacket();
    }

    public static MaplePacket damagePlayer(int skill, int monsteridfrom, int cid, int damage, int fake, int direction, boolean pgmr, int pgmr_1, boolean is_pg, int oid, int pos_x, int pos_y) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.DAMAGE_PLAYER.getValue());
        mplew.writeInt(cid);
        mplew.write(skill);
        mplew.writeInt(damage);
        mplew.write(direction);
        mplew.writeInt(monsteridfrom);
        if (pgmr) {
            mplew.write(pgmr_1);
            mplew.write(is_pg ? 1 : 0);
            mplew.writeInt(oid);
            mplew.write(6);
            mplew.writeShort(pos_x);
            mplew.writeShort(pos_y);
            mplew.write(0);
        } else {
            mplew.write0(11);
        }
        mplew.writeInt(damage);
        if (fake > 0) {
            mplew.writeInt(fake);
        }
        return mplew.getPacket();
    }

    public static MaplePacket charNameResponse(String charname, boolean nameUsed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CHAR_NAME_RESPONSE.getValue());
        mplew.writeMapleAsciiString(charname);
        mplew.write(nameUsed ? 1 : 0);
        return mplew.getPacket();
    }

    public static MaplePacket addNewCharEntry(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ADD_NEW_CHAR_ENTRY.getValue());
        mplew.write(0);
        addCharEntry(mplew, chr);
        return mplew.getPacket();
    }
    
    public static MaplePacket createCharFail() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ADD_NEW_CHAR_ENTRY.getValue());
        mplew.write(9);
        return mplew.getPacket();
    }

    /**
     * 
     * @param c
     * @param quest
     * @return
     */
    public static MaplePacket startQuest(MapleCharacter c, short quest) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(quest);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    /**
     * state 0 = del ok state 12 = invalid bday
     *
     * @param cid
     * @param state
     * @return
     */
    public static MaplePacket deleteCharResponse(int cid, byte state) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendPacketOpcode.DELETE_CHAR_RESPONSE.getValue());
        mplew.writeInt(cid);
        mplew.write(state);
        return mplew.getPacket();
    }

    /**
     * 
     * @param chr
     * @param isSelf
     * @return
     */
    public static MaplePacket charInfo(MapleCharacter chr, boolean isSelf) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CHAR_INFO.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(chr.getLevel());
        mplew.writeShort(chr.getJob().getId());
        mplew.write(10); // 대난투 등급
        mplew.writeInt(chr.getFame());
        mplew.write(chr.getMarried() > 0 ? 1 : 0);
        
        mplew.write(2); // 전문기술갯수?
        mplew.writeShort(9200);
        mplew.writeShort(9204);
        
        String guildName = "";
        String allianceName = "";
        MapleGuildSummary gs = chr.getClient().getChannelServer().getGuildSummary(chr.getGuildId());
        if (chr.getGuildId() > 0 && gs != null) {
            guildName = gs.getName();
            try {
                MapleAlliance alliance = chr.getClient().getChannelServer().getWorldInterface().getAlliance(gs.getAllianceId());
                if (alliance != null) {
                    allianceName = alliance.getName();
                }
            } catch (RemoteException re) {
                re.printStackTrace();
                chr.getClient().getChannelServer().reconnectWorld();
            }
        }

        if(guildName.equalsIgnoreCase(""))
        {
            mplew.write(HexTool.getByteArrayFromHexString("01 00 2D 00 00"));
        } else {
            mplew.writeMapleAsciiString(guildName);
            mplew.writeMapleAsciiString(allianceName);
        }
        mplew.write(0xFF);
        mplew.write(0);
        
        MaplePet[] pets = chr.getPets();
        IItem inv = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -114);
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                mplew.write(pets[i].getUniqueId());
                mplew.writeInt(pets[i].getItemId()); // petid
                mplew.writeMapleAsciiString(pets[i].getName());
                mplew.write(pets[i].getLevel()); // pet level
                mplew.writeShort(pets[i].getCloseness()); // pet closeness
                mplew.write(pets[i].getFullness()); // pet fullness
                mplew.writeShort(0);
                mplew.writeInt(inv != null ? inv.getItemId() : 0);
            }
        }
        mplew.write(0); //end of pets
        /*if (chr.getMount() != null && chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18) != null) {
            mplew.write(chr.getMount().getId()); //mount
            mplew.writeInt(chr.getMount().getLevel()); //level
            mplew.writeInt(chr.getMount().getExp()); //exp
            mplew.writeInt(chr.getMount().getTiredness()); //tiredness
        } else {*/
            mplew.write(0);
        //}
        mplew.write(0);
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM wishlist WHERE charid = ? ORDER BY sn DESC");
            ps.setInt(1, chr.getId());
            ResultSet rs = ps.executeQuery();
            List<Integer> sn = new ArrayList<Integer>(); // Ordered pl0x <3
            while (rs.next()) {
                sn.add(rs.getInt("sn"));
            }
            mplew.write(sn.size());
            for (int serialn : sn) {
                mplew.writeInt(serialn);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
        }
        IItem medal = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -49);
        if (medal != null)
            mplew.writeInt(medal.getItemId());
        else
            mplew.writeInt(0);
        ArrayList<Integer> medalQuests = new ArrayList<Integer>();
        List<MapleQuestStatus> completed = chr.getCompletedQuests();
        for (MapleQuestStatus q : completed) {
            if((q.getQuest().getId() >= 29900) && (q.getQuest().getId() <= 29923))
                medalQuests.add((Integer) q.getQuest().getId());
        }
        Collections.sort(medalQuests);
        mplew.writeShort(medalQuests.size());
        for (Integer i : medalQuests)
        {
            mplew.writeShort(i);
        }
        mplew.write0(6);
        return mplew.getPacket();
    }

    /**
     * It is important that statups is in the correct order (see decleration
     * order in MapleBuffStat) since this method doesn't do automagical
     * reordering.
     *
     * @param buffid
     * @param bufflength
     * @param statups
     * @param morph
     * @return
     */
    public static MaplePacket giveBuff(int buffid, int bufflength, List<Pair<MapleBuffStat, Integer>> statups, boolean mount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        writeLongMask(mplew, statups);
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            mplew.writeShort(statup.getRight().shortValue());
            mplew.writeInt(buffid);
            mplew.writeInt(bufflength);
        }
        mplew.write0(7);
        if(ServerConstants.DEBUG)
            System.out.println(mplew.getPacket().toString());
        return mplew.getPacket();
    }


    public static MaplePacket giveMount(int buffid, int skillid, List<Pair<MapleBuffStat, Integer>> statups) {
	MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        writeLongMask(mplew, statups);
        mplew.write0(3);
        mplew.writeInt(buffid); // 1902000 saddle
        mplew.writeInt(skillid); // skillid
        mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00 00 00 1A"));
	return mplew.getPacket();
    }

    private static void writeLongMask(MaplePacketLittleEndianWriter mplew, List<Pair<MapleBuffStat, Integer>> statups) {
	long firstmask = 0;
	long secondmask = 0;
        long thirdmask = 0;
        long forthmask = 0;
	for (Pair<MapleBuffStat, Integer> statup : statups) {
            switch (statup.getLeft().getPosion()) {
                case 1:
                    firstmask |= statup.getLeft().getValue();
                    break;
                case 2:
                    secondmask |= statup.getLeft().getValue();
                    break;
                case 3:
                    thirdmask |= statup.getLeft().getValue();
                    break;
                case 4:
                    forthmask |= statup.getLeft().getValue();
                    break;
            }
	}
        mplew.writeLong(firstmask);
        mplew.writeLong(secondmask);
	mplew.writeLong(thirdmask);
	mplew.writeLong(forthmask);
    }

    /**
     * 
     * @param cid
     * @param statups
     * @param mount
     * @return
     */
    public static MaplePacket showMonsterRiding(int cid, List<Pair<MapleBuffStat, Integer>> statups, MapleMount mount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        writeLongMask(mplew, statups);
        mplew.writeShort(0);
        mplew.write(18);
        mplew.writeInt(mount.getItemId());
        mplew.writeInt(mount.getSkillId());
        mplew.write0(7);
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param quest
     * @return
     */
    public static MaplePacket forfeitQuest(MapleCharacter c, short quest) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(quest);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param quest
     * @return
     */
    public static MaplePacket completeQuest(MapleCharacter c, short quest) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(quest);
        mplew.write(HexTool.getByteArrayFromHexString("02 A0 67 B9 DA 69 3A C8 01"));
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param quest
     * @param npc
     * @param progress
     * @return
     */
    public static MaplePacket updateQuestInfo(MapleCharacter c, short quest, int npc, byte progress) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(progress);
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    private static <E extends LongValueHolder> long getLongMask(List<Pair<E, Integer>> statups) {
        long mask = 0;
        for (Pair<E, Integer> statup : statups) {
            mask |= statup.getLeft().getValue();
        }
        return mask;
    }

    private static <E extends LongValueHolder> long getLongMaskFromList(List<E> statups) {
        long mask = 0;
        for (E statup : statups) {
            mask |= statup.getValue();
        }
        return mask;
    }

    private static <E extends LongValueHolder> long getLongMaskD(List<Pair<MapleDisease, Integer>> statups) {
        long mask = 0;
        for (Pair<MapleDisease, Integer> statup : statups) {
            mask |= statup.getLeft().getValue();
        }
        return mask;
    }

    private static <E extends LongValueHolder> long getLongMaskFromListD(List<MapleDisease> statups) {
        long mask = 0;
        for (MapleDisease statup : statups) {
            mask |= statup.getValue();
        }
        return mask;
    }

    public static MaplePacket giveDebuff(List<Pair<MapleDisease, Integer>> statups, MobSkill skill) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        long mask = getLongMaskD(statups);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        for (Pair<MapleDisease, Integer> statup : statups) {
            mplew.writeShort(statup.getRight().shortValue());
            mplew.writeShort(skill.getSkillId());
            mplew.writeShort(skill.getSkillLevel());
            mplew.writeInt((int) skill.getDuration());
        }
        mplew.writeShort(0); // ??? wk charges have 600 here o.o
        mplew.writeShort(900);//Delay
        mplew.write(1);
        return mplew.getPacket();
    }

    public static MaplePacket giveForeignDebuff(int cid, List<Pair<MapleDisease, Integer>> statups, MobSkill skill) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        long mask = getLongMaskD(statups);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        for (int i = 0; i < statups.size(); i++) {
            mplew.writeShort(skill.getSkillId());
            mplew.writeShort(skill.getSkillLevel());
        }
        mplew.writeShort(0); // same as give_buff
        mplew.writeShort(900);//Delay
        return mplew.getPacket();
    }

    public static MaplePacket cancelForeignDebuff(int cid, List<MapleDisease> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        long mask = getLongMaskFromListD(statups);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        return mplew.getPacket();
    }

    public static MaplePacket giveForeignBuff(int cid, int skillId, List<Pair<MapleBuffStat, Integer>> statups, boolean morph) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        long mask = getLongMask(statups);
        mplew.writeLong(0);
        mplew.writeLong(0);
        mplew.writeLong(0);
        mplew.writeLong(mask);
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            if (morph) {
                mplew.writeInt(statup.getRight().intValue());
            } else {
                mplew.writeShort(statup.getRight().shortValue());
                mplew.writeInt(skillId);
            }
        }
        mplew.write(0);
        mplew.writeShort(0);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket cancelForeignBuff(int cid, List<MapleBuffStat> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        long mask = getLongMaskFromList(statups);
        if (isFirstLong(statups)) {
            mplew.writeLong(mask);
            mplew.writeLong(0);
            mplew.writeLong(0);
            mplew.writeLong(0);
        } else if (isSecondLong(statups)) {
            mplew.writeLong(0);
            mplew.writeLong(mask);
            mplew.writeLong(0);
            mplew.writeLong(0);
        } else if (isThirdLong(statups)) {
            mplew.writeLong(0);
            mplew.writeLong(0);
            mplew.writeLong(mask);
            mplew.writeLong(0);
        } else {
            mplew.writeLong(0);
            mplew.writeLong(0);
            mplew.writeLong(0);
            mplew.writeLong(mask);
        }
        if (mask == MapleBuffStat.MONSTER_RIDING.getValue())
            mplew.write(1);
        return mplew.getPacket();
    }
    
    private static boolean isFirstLong(List<MapleBuffStat> statups) {
        for (MapleBuffStat stat : statups) {
            if (stat.getPosion() == 1) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isSecondLong(List<MapleBuffStat> statups) {
        for (MapleBuffStat stat : statups) {
            if (stat.getPosion() == 2) {
                return true;
            }
        }
        return false;
    }

    private static boolean isThirdLong(List<MapleBuffStat> statups) {
        for (MapleBuffStat stat : statups) {
            if (stat.getPosion() == 3) {
                return true;
            }
        }
        return false;
    }

    public static MaplePacket cancelBuff(List<MapleBuffStat> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
        long mask = getLongMaskFromList(statups);
        if (isFirstLong(statups)) {
            mplew.writeLong(mask);
            mplew.writeLong(0);
            mplew.writeLong(0);
            mplew.writeLong(0);
        } else if (isSecondLong(statups)) {
            mplew.writeLong(0);
            mplew.writeLong(mask);
            mplew.writeLong(0);
            mplew.writeLong(0);
        } else if (isThirdLong(statups)) {
            mplew.writeLong(0);
            mplew.writeLong(0);
            mplew.writeLong(mask);
            mplew.writeLong(0);
        } else {
            mplew.writeLong(0);
            mplew.writeLong(0);
            mplew.writeLong(0);
            mplew.writeLong(mask);
        }
        if (mask == MapleBuffStat.MONSTER_RIDING.getValue())
            mplew.write(0);
        if (mask != MapleBuffStat.BLESS.getValue())
            mplew.write(mask == MapleBuffStat.DASH.getValue() ? 4 : 6);
        return mplew.getPacket();
    }

    public static MaplePacket cancelDebuff(List<MapleDisease> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
        mplew.writeLong(0);
        mplew.writeLong(getLongMaskFromListD(statups));
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket getPlayerShopChat(MapleCharacter c, String chat, boolean owner) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("06 08"));
        mplew.write(owner ? 0 : 1);
        mplew.writeMapleAsciiString(c.getName() + " : " + chat);
        return mplew.getPacket();
    }

    public static MaplePacket getPlayerShopNewVisitor(MapleCharacter c, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x04);
        mplew.write(slot);
        addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        return mplew.getPacket();
    }

    public static MaplePacket getPlayerShopRemoveVisitor(int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x0A);
        if (slot > 0) {
            mplew.write(slot);
        }
        return mplew.getPacket();
    }

    public static MaplePacket getTradePartnerAdd(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("09 01"));// 00 04 88 4E 00"));
        addCharLook(mplew, c, false);
        mplew.write(0);
        mplew.writeMapleAsciiString(c.getName());
        mplew.write(HexTool.getByteArrayFromHexString("9C 01"));
        return mplew.getPacket();
    }

    public static MaplePacket getTradeInvite(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("0B 03"));
        mplew.writeMapleAsciiString(c.getName());
        mplew.writeInt(1919);
        return mplew.getPacket();
    }

    public static MaplePacket getTradeMesoSet(byte number, int meso) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x01);
        mplew.write(number);
        mplew.writeInt(meso);
        return mplew.getPacket();
    }

    public static MaplePacket getTradeItemAdd(byte number, IItem item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0);
        mplew.write(number);
        addItemInfo(mplew, item, false, false, false, true);
        return mplew.getPacket();
    }

    public static MaplePacket getPlayerShopItemUpdate(MaplePlayerShop shop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x19);
        mplew.write(shop.getItems().size());
        for (MaplePlayerShopItem item : shop.getItems()) {
            mplew.writeShort(item.getBundles());
            mplew.writeShort(item.getItem().getQuantity());
            mplew.writeInt(item.getPrice());
            addItemInfo(mplew, item.getItem(), true, true);
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param c
     * @param shop
     * @param owner
     * @return
     */
    public static MaplePacket getPlayerShop(MapleClient c, MaplePlayerShop shop, boolean owner) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("05 04 04"));
        mplew.write(owner ? 0 : 1);
        mplew.write(0);
        addCharLook(mplew, shop.getOwner(), false);
        mplew.writeMapleAsciiString(shop.getOwner().getName());
        mplew.write(1);
        addCharLook(mplew, shop.getOwner(), false);
        mplew.writeMapleAsciiString(shop.getOwner().getName());
        mplew.write(0xFF);
        mplew.writeMapleAsciiString(shop.getDescription());
        List<MaplePlayerShopItem> items = shop.getItems();
        mplew.write(0x10);
        mplew.write(items.size());
        for (MaplePlayerShopItem item : items) {
            mplew.writeShort(item.getBundles());
            mplew.writeShort(item.getItem().getQuantity());
            mplew.writeInt(item.getPrice());
            addItemInfo(mplew, item.getItem(), true, true);
        }
        return mplew.getPacket();
    }

    public static MaplePacket getTradeStart(MapleClient c, MapleTrade trade, byte number) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("0A 03 02"));
        mplew.write(number);
        if (number == 1) {
            mplew.write(0);
            addCharLook(mplew, trade.getPartner().getChr(), false);
            mplew.write(0);
            mplew.writeMapleAsciiString(trade.getPartner().getChr().getName());
            mplew.write(HexTool.getByteArrayFromHexString("9C 01"));
        }
        mplew.write(number);
        addCharLook(mplew, c.getPlayer(), false);
        mplew.write(0);
        mplew.writeMapleAsciiString(c.getPlayer().getName());
        mplew.write(HexTool.getByteArrayFromHexString("64 00 FF"));
        return mplew.getPacket();
    }

    public static MaplePacket getTradeConfirmation() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x02);
        return mplew.getPacket();
    }

    public static MaplePacket getTradeCompletion(byte number) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x12);
        mplew.write(number);
        mplew.write(7);
        return mplew.getPacket();
    }

    public static MaplePacket getTradeCancel(byte number) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x12);
        mplew.write(number);
        mplew.write(2);
        return mplew.getPacket();
    }

    public static MaplePacket addCharBox(MapleCharacter c, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        addAnnounceBox(mplew, c.getPlayerShop(), type);
        return mplew.getPacket();
    }

    public static MaplePacket removeCharBox(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket getNPCTalk(int npc, byte msgType, String talk, String endBytes) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4); // ?
        mplew.writeInt(npc);
        mplew.write(msgType);
        mplew.write(0); //'speaker'
        mplew.writeMapleAsciiString(talk);
        mplew.write(HexTool.getByteArrayFromHexString(endBytes));
        return mplew.getPacket();
    }

    public static MaplePacket getNPCTalkStyle(int npc, String talk, int styles[]) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4); // ?
        mplew.writeInt(npc);
        mplew.writeShort(9);
        mplew.writeMapleAsciiString(talk);
        mplew.write(styles.length);
        for (int i = 0; i < styles.length; i++) {
            mplew.writeInt(styles[i]);
        }
        return mplew.getPacket();
    }

    public static MaplePacket getNPCTalkNum(int npc, String talk, int def, int min, int max) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4); // ?
        mplew.writeInt(npc);
        mplew.write(4);
        mplew.write(0);
        mplew.writeMapleAsciiString(talk);
        mplew.writeInt(def);
        mplew.writeInt(min);
        mplew.writeInt(max);
        return mplew.getPacket();
    }

    public static MaplePacket getNPCTalkText(int npc, String talk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4); // ?
        mplew.writeInt(npc);
        mplew.write(3);
        mplew.write(0);
        mplew.writeMapleAsciiString(talk);
        mplew.write0(8);
        return mplew.getPacket();
    }

    public static MaplePacket showForeignEffect(int cid, int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(effect);
        return mplew.getPacket();
    }

    public static MaplePacket showBuffeffect(MapleCharacter player, int cid, int skillid, int effectid) {
        return showBuffeffect(player, cid, skillid, effectid, (byte) 3);
    }

//    public static MaplePacket showBuffeffect(MapleCharacter player, int cid, int skillid, int effectid, byte direction) {
  //      return showBuffeffect(player, cid, skillid, effectid, direction);
    //}
/*
    public static MaplePacket showBuffeffect(MapleCharacter player, int cid, int skillid, int effectid, byte direction) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid); // ?
        if (skillid == Buccaneer.SUPER_TRANSFORMATION || skillid == Marauder.TRANSFORMATION || skillid == WindArcher.EAGLE_EYE || skillid == ThunderBreaker.TRANSFORMATION) {
            mplew.write(1);
            mplew.writeInt(skillid);
            mplew.write(direction);
            mplew.write(1);
        } else {
            mplew.write(effectid); //buff level
            mplew.writeInt(skillid);
            //mplew.write(3);
           // if (direction != (byte) 3) {
                mplew.write(direction);
         //   }
            mplew.write(1); // although this might have to be put in an "else" or some shit
        }
        mplew.write0(4);
        return mplew.getPacket();
    }
 *
 */

      public static MaplePacket showBuffeffect(MapleCharacter player, int cid, int skillid, int effectid, byte direction) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(13);
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid); // ?
        mplew.write(effectid); //buff level
        mplew.writeInt(skillid);
        mplew.write(0x9A); // don't know
        mplew.write(player.getSkillLevel(skillid));
        return mplew.getPacket();
    }


    public static MaplePacket showOwnBuffEffect(int skillid, int effectid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(effectid);
        mplew.writeInt(skillid);
        mplew.write(0xA9);
        mplew.write(1); // probably buff level but we don't know it and it doesn't really matter
        return mplew.getPacket();
    }

    public static MaplePacket showOwnBerserk(int skilllevel, boolean berserk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(9);
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(1);
        mplew.writeInt(1320006);
        mplew.write(0xA9);
        mplew.write(skilllevel);
        mplew.write(berserk ? 1 : 0);
        return mplew.getPacket();
    }

    public static MaplePacket showBerserk(int cid, int skilllevel, boolean berserk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(13);
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(1);
        mplew.writeInt(1320006);
        mplew.write(0x9A);
        mplew.write(skilllevel);
        mplew.write(berserk ? 1 : 0);
        return mplew.getPacket();
    }

    public static MaplePacket updateSkill(int skillid, int level, int masterlevel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_SKILLS.getValue());
        mplew.writeShort(1);
        mplew.writeShort(1);
        mplew.writeInt(skillid);
        mplew.writeInt(level);
        mplew.writeInt(masterlevel);
        mplew.write(0);//*
        mplew.write(ITEM_MAGIC);//*
        addExpirationTime(mplew, 0, false);//*
        mplew.write(3);
        return mplew.getPacket();
    }

    public static MaplePacket updateQuestMobKills(MapleQuestStatus status) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(status.getQuest().getId());
        mplew.write(1);
        String killStr = "";
        for (int kills : status.getMobKills().values()) {
            killStr += StringUtil.getLeftPaddedStr(String.valueOf(kills), '0', 3); // possibly wrong
        }
        mplew.writeMapleAsciiString(killStr);
    /*    mplew.writeInt(0);
        mplew.writeInt(0);*/
        return mplew.getPacket();
    }

    public static MaplePacket getShowQuestCompletion(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendPacketOpcode.SHOW_QUEST_COMPLETION.getValue());
        mplew.writeShort(id);
        return mplew.getPacket();
    }

    public static MaplePacket getKeymap(Map<Integer, MapleKeyBinding> keybindings) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KEYMAP.getValue());
        mplew.write(0);
        for (int x = 0; x < 90; x++) {
            MapleKeyBinding binding = keybindings.get(Integer.valueOf(x));
            if (binding != null) {
                mplew.write(binding.getType());
                mplew.writeInt(binding.getAction());
            } else {
                mplew.write(0);
                mplew.writeInt(0);
            }
        }
        return mplew.getPacket();
    }

    public static MaplePacket getWhisper(String sender, int channel, String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(0x12);
        mplew.writeMapleAsciiString(sender);
        mplew.writeShort(channel - 1); // I guess this is the channel
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }

    /**
     *
     * @param target name of the target character
     * @param reply error code: 0x0 = cannot find char, 0x1 = success
     * @return the MaplePacket
     */
    public static MaplePacket getWhisperReply(String target, byte reply) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(0x0A); // whisper?
        mplew.writeMapleAsciiString(target);
        mplew.write(reply);
        return mplew.getPacket();
    }

    public static MaplePacket getInventoryFull() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket getShowInventoryFull() {
        return getShowInventoryStatus(0xff);
    }

    public static MaplePacket showItemUnavailable() {
        return getShowInventoryStatus(0xfe);
    }

    public static MaplePacket getShowInventoryStatus(int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(0);
        mplew.write(mode);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket getStorage(int npcId, byte slots, Collection<IItem> items, int meso) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x16);
        mplew.writeInt(npcId);
        mplew.write(slots);
        mplew.writeShort(0x7E);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(meso);
        mplew.writeShort(0);
        mplew.write((byte) items.size());
        for (IItem item : items) {
            addItemInfo(mplew, item, true, true);
        }
        mplew.writeShort(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket getStorageFull() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x11);
        return mplew.getPacket();
    }

    public static MaplePacket mesoStorage(byte slots, int meso) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x13);
        mplew.write(slots);
        mplew.writeShort(2);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(meso);
        return mplew.getPacket();
    }

    public static MaplePacket storeStorage(byte slots, MapleInventoryType type, Collection<IItem> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(0xD);
        mplew.write(slots);
        mplew.writeShort(type.getBitfieldEncoding());
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(items.size());
        for (IItem item : items) {
            addItemInfo(mplew, item, true, true);
        }
        return mplew.getPacket();
    }

    public static MaplePacket takeOutStorage(byte slots, MapleInventoryType type, Collection<IItem> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(0x9);
        mplew.write(slots);
        mplew.writeShort(type.getBitfieldEncoding());
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(items.size());
        for (IItem item : items) {
            addItemInfo(mplew, item, true, true);
        }
        return mplew.getPacket();
    }

    /**
     *
     * @param oid
     * @param remhp in %
     * @return
     */
    public static MaplePacket showMonsterHP(int oid, int remhppercentage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_MONSTER_HP.getValue());
        mplew.writeInt(oid);
        mplew.write(remhppercentage);
        return mplew.getPacket();
    }

    public static MaplePacket showBossHP(int oid, int currHP, int maxHP, byte tagColor, byte tagBgColor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
        mplew.write(5);
        mplew.writeInt(oid);
        mplew.writeInt(currHP);
        mplew.writeInt(maxHP);
        mplew.write(tagColor);
        mplew.write(tagBgColor);
        return mplew.getPacket();
    }

    public static MaplePacket giveFameResponse(int mode, String charname, int newfame) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.write(0);
        mplew.writeMapleAsciiString(charname);
        mplew.write(mode);
        mplew.writeShort(newfame);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    /**
     * status can be: <br>
     * 0: ok, use giveFameResponse<br>
     * 1: the username is incorrectly entered<br>
     * 2: users under level 15 are unable to toggle with fame.<br>
     * 3: can't raise or drop fame anymore today.<br>
     * 4: can't raise or drop fame for this character for this month anymore.<br>
     * 5: received fame, use receiveFame()<br>
     * 6: level of fame neither has been raised nor dropped due to an unexpected
     * error
     *
     * @param status
     * @return
     */
    public static MaplePacket giveFameErrorResponse(int status) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.write(status);
        return mplew.getPacket();
    }

    public static MaplePacket receiveFame(int mode, String charnameFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.write(5);
        mplew.writeMapleAsciiString(charnameFrom);
        mplew.write(mode);
        return mplew.getPacket();
    }

    public static MaplePacket partyCreated() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(0x0C);
        mplew.writeInt(104160); // 파티id
        mplew.write(CHAR_INFO_MAGIC);
        mplew.write(CHAR_INFO_MAGIC);
        mplew.writeInt(0);
        mplew.write(HexTool.getByteArrayFromHexString("36 2E 2C 20 00 01"));
        return mplew.getPacket();
    }

    public static MaplePacket partyInvite(MapleCharacter from) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(4);
        mplew.writeInt(from.getId());
        mplew.writeMapleAsciiString(from.getName());
        mplew.writeInt(from.getLevel());
        mplew.writeInt(from.getJob().getId());
        mplew.write(0);
        return mplew.getPacket();
    }
    
    public static MaplePacket partyStatusMessage(int message, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(message);
        mplew.writeMapleAsciiString(name);
        return mplew.getPacket();
    }

    /**
     * 10: A beginner can't create a party.
     * 1/11/14/19: Your request for a party didn't work due to an unexpected error.
     * 13: You have yet to join a party.
     * 16: Already have joined a party.
     * 17: The party you're trying to join is already in full capacity.
     * 19: Unable to find the requested character in this channel.
     *
     * @param message
     * @return
     */
    public static MaplePacket partyStatusMessage(int message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(message);
        mplew.write0(5);
        return mplew.getPacket();
    }
    
    public static MaplePacket partyMessage(String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PARTY_MESSAGE.getValue());
        mplew.writeShort(0x0B);
        mplew.writeMapleAsciiString(msg);
        return mplew.getPacket();
    }
    
    public static MaplePacket TestMessage(int type, String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PARTY_MESSAGE.getValue());
        mplew.writeShort(type);
        mplew.writeMapleAsciiString(msg);
        return mplew.getPacket();
    }

    private static void addPartyStatus(int forchannel, MapleParty party, LittleEndianWriter lew, boolean leaving) {
        List<MaplePartyCharacter> partymembers = new ArrayList<MaplePartyCharacter>(party.getMembers());
        while (partymembers.size() < 6) {
            partymembers.add(new MaplePartyCharacter());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getId());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeAsciiString(getRightPaddedStr(partychar.getName(), '\0', 13));
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getJobId());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getLevel());
        }
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.isOnline()) {
                lew.writeInt(partychar.getChannel() - 1);
            } else {
                lew.writeInt(-2);
            }
        }
        for (MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(0); // ?
        }
        lew.writeInt(party.getLeader().getId());
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel) {
                lew.writeInt(partychar.getMapid());
            } else {
                lew.writeInt(999999999);
            }
        }
        for (MaplePartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel && !leaving) {
                lew.writeInt(partychar.getDoorTown());
                lew.writeInt(partychar.getDoorTarget());
                lew.writeInt(2311002);
                lew.writeInt(partychar.getDoorPosition().x); //short in v88 lolwat
                lew.writeInt(partychar.getDoorPosition().y);
            } else {
                lew.writeInt(leaving ? 999999999 : 0);
                lew.writeLong(leaving ? 999999999 : 0);
                lew.writeLong(leaving ? -1 : 0);
            }
        }
        lew.write(1);
    }

    public static MaplePacket updateParty(int forChannel, MapleParty party, PartyOperation op, MaplePartyCharacter target) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        switch (op) {
            case DISBAND:
            case EXPEL:
            case LEAVE:
                mplew.write(0x10);
                mplew.writeInt(party.getId());
                mplew.writeInt(target.getId());
                if (op == PartyOperation.DISBAND) {
                    mplew.write(0);
                    mplew.writeInt(party.getId());
                } else {
                    mplew.write(1);
                    mplew.write(op == PartyOperation.EXPEL ? 1 : 0);
                    mplew.writeMapleAsciiString(target.getName());
                    addPartyStatus(forChannel, party, mplew, op == PartyOperation.LEAVE);
                }
                break;
            case JOIN:
                mplew.write(0x13);
                mplew.writeInt(party.getId());
                mplew.writeMapleAsciiString(target.getName());
                addPartyStatus(forChannel, party, mplew, false);
                break;
            case SILENT_UPDATE:
            case LOG_ONOFF:
                mplew.write(0x0B);
                mplew.writeInt(party.getId());
                addPartyStatus(forChannel, party, mplew, op == PartyOperation.LOG_ONOFF);
                break;
            case CHANGE_LEADER:
                mplew.write(0x23);
                mplew.writeInt(target.getId());
                mplew.write(0);
                break;
        }
        return mplew.getPacket();
    }

    public static MaplePacket partyPortal(int townId, int targetId, Point position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.writeShort(0x23);
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
        mplew.writeShort(position.x);
        mplew.writeShort(position.y);
        return mplew.getPacket();
    }

    public static MaplePacket updatePartyMemberHP(int cid, int curhp, int maxhp) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_PARTYMEMBER_HP.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(curhp);
        mplew.writeInt(maxhp);
        return mplew.getPacket();
    }

    /**
     * mode: 0 buddychat; 1 partychat; 2 guildchat
     *
     * @param name
     * @param chattext
     * @param mode
     * @return
     */
    public static MaplePacket multiChat(String name, String chattext, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MULTICHAT.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(name);
        mplew.writeMapleAsciiString(chattext);
        return mplew.getPacket();
    }

    public static MaplePacket applyMonsterStatus(int oid, Map<MonsterStatus, Integer> stats, int skill, boolean monsterSkill, int delay) {
        return applyMonsterStatus(oid, stats, skill, monsterSkill, delay, null);
    }

    public static MaplePacket applyMonsterStatusTest(int oid, int mask, int delay, MobSkill mobskill, int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(mask);
        mplew.writeShort(1);
        mplew.writeShort(mobskill.getSkillId());
        mplew.writeShort(mobskill.getSkillLevel());
        mplew.writeShort(0); // as this looks similar to giveBuff this might actually be the buffTime but it's not displayed anywhere
        mplew.writeShort(delay); // delay in ms
        mplew.write(1); // ?
        return mplew.getPacket();
    }

    public static MaplePacket applyMonsterStatusTest2(int oid, int mask, int skill, int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(mask);
        mplew.writeShort(value);
        mplew.writeInt(skill);
        mplew.writeShort(0); // as this looks similar to giveBuff this might actually be the buffTime but it's not displayed anywhere
        mplew.writeShort(0); // delay in ms
        mplew.write(1); // ?
        return mplew.getPacket();
    }

    public static MaplePacket applyMonsterStatus(int oid, Map<MonsterStatus, Integer> stats, int skill, boolean monsterSkill, int delay, MobSkill mobskill) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
        mplew.writeInt(oid);
        mplew.writeLong(0);
        mplew.writeLong(0);
        mplew.writeLong(0);
        mplew.writeInt(0);
        int mask = 0;
        for (MonsterStatus stat : stats.keySet()) {
            mask |= stat.getValue();
        }
        mplew.writeInt(mask);
        for (Integer val : stats.values()) {
            mplew.writeInt(val);
            if (monsterSkill) {
                mplew.writeShort(mobskill.getSkillId());
                mplew.writeShort(mobskill.getSkillLevel());
            } else {
                mplew.writeInt(skill);
            }
        }
        mplew.writeShort(delay); // delay in ms
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket cancelMonsterStatus(int oid, Map<MonsterStatus, Integer> stats) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CANCEL_MONSTER_STATUS.getValue());
        mplew.writeInt(oid);
        mplew.writeLong(0);
        mplew.writeLong(0);
        mplew.writeLong(0);
        mplew.writeInt(0);
        int mask = 0;
        for (MonsterStatus stat : stats.keySet()) {
            mask |= stat.getValue();
        }
        mplew.writeInt(mask);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket getClock(int time) { // time in seconds
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
        mplew.write(2); // clock type. if you send 3 here you have to send another byte (which does not matter at all) before the timestamp
        mplew.writeInt(time);
        return mplew.getPacket();
    }

    public static MaplePacket getClockTime(int hour, int min, int sec) { // Current Time
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
        mplew.write(1); //Clock-Type
        mplew.write(hour);
        mplew.write(min);
        mplew.write(sec);
        return mplew.getPacket();
    }

    public static MaplePacket spawnMist(int oid, int ownerCid, int skill, int level, MapleMist mist) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SPAWN_MIST.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(mist.getMistType().ordinal());
        mplew.writeInt(ownerCid);
        mplew.writeInt(skill);
        mplew.write(level);
        mplew.writeShort(mist.getSkillDelay()); // Skill delay
        mplew.writeInt(mist.getBox().x);
        mplew.writeInt(mist.getBox().y);
        mplew.writeInt(mist.getBox().x + mist.getBox().width);
        mplew.writeInt(mist.getBox().y + mist.getBox().height);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket removeMist(int oid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.REMOVE_MIST.getValue());
        mplew.writeInt(oid);
        return mplew.getPacket();
    }

    public static MaplePacket damageSummon(int cid, int summonSkillId, int damage, int unkByte, int monsterIdFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.DAMAGE_SUMMON.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        mplew.write(unkByte);
        mplew.writeInt(damage);
        mplew.writeInt(monsterIdFrom);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket damageMonster(int oid, int damage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(0);
        mplew.writeInt(damage);
        return mplew.getPacket();
    }

    public static MaplePacket healMonster(int oid, int heal) {
        return damageMonster(oid, -heal);
    }

    public static MaplePacket updateBuddylist(Collection<BuddylistEntry> buddylist) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(7);
        mplew.write(buddylist.size());
        for (BuddylistEntry buddy : buddylist) {
            if (buddy.isVisible()) {
                mplew.writeInt(buddy.getCharacterId()); // cid
                mplew.writeAsciiString(getRightPaddedStr(buddy.getName(), '\0', 13));
                mplew.write(0); // opposite status
                mplew.writeInt(buddy.getChannel() - 1);
                mplew.writeNullTerminatedAsciiString("그룹 미지정");
                mplew.write0(5);
            }
        }
        for (int x = 0; x < buddylist.size(); x++) {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static MaplePacket buddylistMessage(byte message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(message);
        return mplew.getPacket();
    }

    public static MaplePacket requestBuddylistAdd(int cidFrom, int cid, String nameFrom, int levelFrom, int jobFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(9);
        mplew.writeInt(cidFrom);
        mplew.writeMapleAsciiString(nameFrom);
        mplew.writeInt(levelFrom);
        mplew.writeInt(jobFrom);
        mplew.writeInt(cidFrom);
        mplew.writeAsciiString(getRightPaddedStr(nameFrom, '\0', 13));
        mplew.write(HexTool.getByteArrayFromHexString("01 0F 00 00 00"));
        mplew.writeNullTerminatedAsciiString("그룹 미지정");
        mplew.write(HexTool.getByteArrayFromHexString("BD C3 B0 A3 B5 00"));
        return mplew.getPacket();
    }

    public static MaplePacket updateBuddyChannel(int characterid, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(0x14);
        mplew.writeInt(characterid);
        mplew.write(0);
        mplew.writeInt(channel);
        return mplew.getPacket();
    }

    public static MaplePacket itemEffect(int characterid, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_EFFECT.getValue());
        mplew.writeInt(characterid);
        mplew.writeInt(itemid);
        return mplew.getPacket();
    }

    public static MaplePacket updateBuddyCapacity(int capacity) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(0x15);
        mplew.write(capacity);
        return mplew.getPacket();
    }

    public static MaplePacket showChair(int characterid, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_CHAIR.getValue());
        mplew.writeInt(characterid);
        mplew.writeInt(itemid);
        return mplew.getPacket();
    }

    public static MaplePacket cancelChair(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CANCEL_CHAIR.getValue());
        if (id == -1) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeShort(id);
        }
        return mplew.getPacket();
    }
    
    public static MaplePacket showTitleItem(int cid, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_TITLE_ITEM.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(itemid);
        return mplew.getPacket();
    }

    // is there a way to spawn reactors non-animated?
    public static MaplePacket spawnReactor(MapleReactor reactor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        Point pos = reactor.getPosition();
        mplew.writeShort(SendPacketOpcode.REACTOR_SPAWN.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.writeInt(reactor.getId());
        mplew.write(reactor.getState());
        mplew.writeShort(pos.x);
        mplew.writeShort(pos.y);
        mplew.write(0);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket triggerReactor(MapleReactor reactor, int stance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        Point pos = reactor.getPosition();
        mplew.writeShort(SendPacketOpcode.REACTOR_HIT.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.writeShort(pos.x);
        mplew.writeShort(pos.y);
        mplew.writeShort(stance);
        mplew.write(0);
        mplew.write(5); // frame delay, set to 5 since there doesn't appear to be a fixed formula for it
        return mplew.getPacket();
    }

    public static MaplePacket destroyReactor(MapleReactor reactor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        Point pos = reactor.getPosition();
        mplew.writeShort(SendPacketOpcode.REACTOR_DESTROY.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.writeShort(pos.x);
        mplew.writeShort(pos.y);
        return mplew.getPacket();
    }

    public static MaplePacket musicChange(String song) {
        return environmentChange(song, 6);
    }

    public static MaplePacket showEffect(String effect) {
        return environmentChange(effect, 3);
    }

    public static MaplePacket playSound(String sound) {
        return environmentChange(sound, 4);
    }

    public static MaplePacket environmentChange(String env, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(env);
        return mplew.getPacket();
    }

    public static MaplePacket startMapEffect(String msg, int itemid, boolean active) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MAP_EFFECT.getValue());
        mplew.write(active ? 0 : 1);
        mplew.writeInt(itemid);
        if (active) {
            mplew.writeMapleAsciiString(msg);
        }
        return mplew.getPacket();
    }

    public static MaplePacket removeMapEffect() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MAP_EFFECT.getValue());
        mplew.write(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket showGuildInfo(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x1A); //signature for showing guild info
        if (c == null) { //show empty guild (used for leaving, expelled)
            mplew.write(0);
            return mplew.getPacket();
        }
        MapleGuild g = c.getClient().getChannelServer().getGuild(c.getMGC());
        if (g == null) { //failed to read from DB - don't show a guild
            mplew.write(0);
            return mplew.getPacket();
        } else {
            c.setGuildRank(c.getGuildRank());
        }
        mplew.write(1); //bInGuild
        mplew.writeInt(g.getId());
        mplew.writeMapleAsciiString(g.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(g.getRankTitle(i));
        }
        Collection<MapleGuildCharacter> members = g.getMembers();
        mplew.write(members.size()); //then it is the size of all the members
        for (MapleGuildCharacter mgc : members) {//and each of their character ids o_O
            mplew.writeInt(mgc.getId());
        }
        for (MapleGuildCharacter mgc : members) {
            mplew.writeAsciiString(getRightPaddedStr(mgc.getName(), '\0', 13));
            mplew.writeInt(mgc.getJobId());
            mplew.writeInt(mgc.getLevel());
            mplew.writeInt(mgc.getGuildRank());
            mplew.writeInt(mgc.isOnline() ? 1 : 0);
            mplew.writeInt(g.getSignature());
            mplew.writeInt(mgc.getAllianceRank());
        }
        mplew.writeInt(g.getCapacity());
        mplew.writeShort(g.getLogoBG());
        mplew.write(g.getLogoBGColor());
        mplew.writeShort(g.getLogo());
        mplew.write(g.getLogoColor());
        mplew.writeMapleAsciiString(g.getNotice());
        mplew.writeInt(g.getGP());
        mplew.writeInt(g.getAllianceId());
        return mplew.getPacket();
    }

    public static MaplePacket guildMemberOnline(int gid, int cid, boolean bOnline) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(12);
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x3d);
        mplew.writeInt(gid);
        mplew.writeInt(cid);
        mplew.write(bOnline ? 1 : 0);
        return mplew.getPacket();
    }

    public static MaplePacket guildInvite(int gid, String charName, int level, int job) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x05);
        mplew.writeInt(gid);
        mplew.writeMapleAsciiString(charName);
        mplew.writeInt(level);
        mplew.writeInt(job);
        mplew.write(0);
        return mplew.getPacket();
    }

    /**
     * 'Char' has denied your guild invitation.
     *
     * @param charname
     * @return
     */
    public static MaplePacket denyGuildInvitation(String charname) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x37);
        mplew.writeMapleAsciiString(charname);
        return mplew.getPacket();
    }

    public static MaplePacket genericGuildMessage(byte code) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(code);
        return mplew.getPacket();
    }

    public static MaplePacket newGuildMember(MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x27);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeAsciiString(getRightPaddedStr(mgc.getName(), '\0', 13));
        mplew.writeInt(mgc.getJobId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getGuildRank()); //should be always 5 but whatevs
        mplew.writeInt(mgc.isOnline() ? 1 : 0); //should always be 1 too
        mplew.writeInt(1); //? could be guild signature, but doesn't seem to matter
        mplew.writeInt(3);
        return mplew.getPacket();
    }

    //someone leaving, mode == 0x2c for leaving, 0x2f for expelled
    public static MaplePacket memberLeft(MapleGuildCharacter mgc, boolean bExpelled) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(bExpelled ? 0x2f : 0x2c);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeMapleAsciiString(mgc.getName());
        return mplew.getPacket();
    }

    //rank change
    public static MaplePacket changeRank(MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(12);
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x40);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.write(mgc.getGuildRank());
        return mplew.getPacket();
    }

    public static MaplePacket guildNotice(int gid, String notice) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x44);
        mplew.writeInt(gid);
        mplew.writeMapleAsciiString(notice);
        return mplew.getPacket();
    }

    public static MaplePacket guildMemberLevelJobUpdate(MapleGuildCharacter mgc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x3C);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getJobId());
        return mplew.getPacket();
    }

    public static MaplePacket rankTitleChange(int gid, String[] ranks) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x3E);
        mplew.writeInt(gid);
        for (int i = 0; i < 5; i++) {
            mplew.writeMapleAsciiString(ranks[i]);
        }
        return mplew.getPacket();
    }

    public static MaplePacket guildDisband(int gid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x32);
        mplew.writeInt(gid);
        mplew.write(1);
        return mplew.getPacket();
    }

    public static MaplePacket guildEmblemChange(int gid, short bg, byte bgcolor, short logo, byte logocolor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x42);
        mplew.writeInt(gid);
        mplew.writeShort(bg);
        mplew.write(bgcolor);
        mplew.writeShort(logo);
        mplew.write(logocolor);
        return mplew.getPacket();
    }

    public static MaplePacket guildCapacityChange(int gid, int capacity) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x3A);
        mplew.writeInt(gid);
        mplew.write(capacity);
        return mplew.getPacket();
    }

    public static void addThread(MaplePacketLittleEndianWriter mplew, ResultSet rs) throws SQLException {
        mplew.writeInt(rs.getInt("localthreadid"));
        mplew.writeInt(rs.getInt("postercid"));
        mplew.writeMapleAsciiString(rs.getString("name"));
        mplew.writeLong(getKoreanTimestamp(rs.getLong("timestamp")));
        mplew.writeInt(rs.getInt("icon"));
        mplew.writeInt(rs.getInt("replycount"));
    }

    public static MaplePacket BBSThreadList(ResultSet rs, int start) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.BBS_OPERATION.getValue());
        mplew.write(0x06);
        if (!rs.last()) {
            mplew.write(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        int threadCount = rs.getRow();
        if (rs.getInt("localthreadid") == 0) { //has a notice
            mplew.write(1);
            addThread(mplew, rs);
            threadCount--; //one thread didn't count (because it's a notice)
        } else {
            mplew.write(0);
        }
        if (!rs.absolute(start + 1)) { //seek to the thread before where we start
            rs.first(); //uh, we're trying to start at a place past possible
            start = 0;
        }
        mplew.writeInt(threadCount);
        mplew.writeInt(Math.min(10, threadCount - start));
        for (int i = 0; i < Math.min(10, threadCount - start); i++) {
            addThread(mplew, rs);
            rs.next();
        }
        return mplew.getPacket();
    }

    public static MaplePacket showThread(int localthreadid, ResultSet threadRS, ResultSet repliesRS) throws SQLException, RuntimeException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.BBS_OPERATION.getValue());
        mplew.write(0x07);
        mplew.writeInt(localthreadid);
        mplew.writeInt(threadRS.getInt("postercid"));
        mplew.writeLong(getKoreanTimestamp(threadRS.getLong("timestamp")));
        mplew.writeMapleAsciiString(threadRS.getString("name"));
        mplew.writeMapleAsciiString(threadRS.getString("startpost"));
        mplew.writeInt(threadRS.getInt("icon"));
        if (repliesRS != null) {
            int replyCount = threadRS.getInt("replycount");
            mplew.writeInt(replyCount);
            int i;
            for (i = 0; i < replyCount && repliesRS.next(); i++) {
                mplew.writeInt(repliesRS.getInt("replyid"));
                mplew.writeInt(repliesRS.getInt("postercid"));
                mplew.writeLong(getKoreanTimestamp(repliesRS.getLong("timestamp")));
                mplew.writeMapleAsciiString(repliesRS.getString("content"));
            }
            if (i != replyCount || repliesRS.next()) {
                throw new RuntimeException(String.valueOf(threadRS.getInt("threadid")));
            }
        } else {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static MaplePacket showGuildRanks(int npcid, ResultSet rs) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x49);
        mplew.writeInt(npcid);
        if (!rs.last()) { //no guilds o.o
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow()); //number of entries
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("GP"));
            mplew.writeInt(rs.getInt("logo"));
            mplew.writeInt(rs.getInt("logoColor"));
            mplew.writeInt(rs.getInt("logoBG"));
            mplew.writeInt(rs.getInt("logoBGColor"));
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateGP(int gid, int GP) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(0x48);
        mplew.writeInt(gid);
        mplew.writeInt(GP);
        return mplew.getPacket();
    }

    public static MaplePacket skillEffect(MapleCharacter from, int skillId, int level, byte flags, int speed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SKILL_EFFECT.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(skillId);
        mplew.write(level);
        mplew.write(flags);
        //mplew.write(0);
        mplew.write(speed);
        mplew.write0(8);
        return mplew.getPacket();
    }

    public static MaplePacket skillCancel(MapleCharacter from, int skillId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CANCEL_SKILL_EFFECT.getValue());
        mplew.writeInt(from.getId());
        mplew.write(1);
        mplew.writeInt(skillId);
        mplew.write0(8);
        return mplew.getPacket();
    }

    public static MaplePacket showMagnet(int mobid, byte success) { // Monster Magnet
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_MAGNET.getValue());
        mplew.writeInt(mobid);
        mplew.write(success);
        mplew.write0(10);
        return mplew.getPacket();
    }

    /**
     * Sends a player hint.
     *
     * @param hint The hint it's going to send.
     * @param width How tall the box is going to be.
     * @param height How long the box is going to be.
     * @return The player hint packet.
     */
    public static MaplePacket sendHint(String hint, int width, int height) {
        if (width < 1) {
            width = hint.length() * 10;
            if (width < 40) {
                width = 40;
            }
        }
        if (height < 5) {
            height = 5;
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_HINT.getValue());
        mplew.writeMapleAsciiString(hint);
        mplew.writeShort(width);
        mplew.writeShort(height);
        mplew.write(1);
        return mplew.getPacket();
    }

    public static MaplePacket messengerInvite(String from, int messengerid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x03);
        mplew.writeMapleAsciiString(from);
        mplew.write(0);
        mplew.writeInt(messengerid);
        mplew.writeShort(0);
        return mplew.getPacket();
    }


    /*
     * 캐시샵 패킷
     * 84 00
     FF FF FF FF FF FF FF FF
     00 00 00 00 00 00 00
     23 8D AC 00 
     53 6C 61 79 65 72 73 42 6C 61 63 6B 00
     00 
     0D
     18 4F 00 00
     29 78 00 00
     7C
     28 0C
     *
     79 02
     09 00
     04 00
     04 00
     *
     E8 27 00 00
     21 1B 00 00
     78 00 00 00
     0A 00 00 00
     00 00 00 27 C4 68 00 13 00 00 00 C0 9A 93 0C 00 00 00 36 72 0F 00 00 94 48 DF 77 10 18 00 00 7D 02 00 00 59 15 00 00 23 00 00 00 31 00 00 00 4B 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 58 00 00 00 0A 58 00 00 00 05 00 00 00 00 A0 B9 CC 01 50 A4 0C BD
     14 
     01
     0B 00 44 75 61 6C 75 41 74 74 61 63 6B
     00
     00
     * 인벤토리
     42 B1 49 00 00 00 00 00 1C 1C 1C 1C 60 00 40 E0 FD 3B 37 4F 01 01 00 01 60 4F 0F 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 0A 00 0A 00 0A 00 0A 00 0A 00 C8 00 00 00 00 00 00 00 78 00 78 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 13 00 9A 75 56 4E 00 00 00 00 00 00 7B 26 03 00 B0 01 00 48 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 04 00 01 B1 BF 0F 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 05 00 03 00 03 00 03 00 03 00 50 00 50 00 00 00 00 00 1B 00 1B 00 14 00 14 00 00 00 00 00 00 00 00 00 08 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 DA 88 03 00 F4 01 00 4C 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 05 00 01 F5 0E 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 0A 00 06 00 06 00 06 00 06 00 50 00 50 00 00 00 00 00 84 00 84 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 11 00 45 27 02 00 00 00 00 00 00 00 8D 61 02 00 AA 01 00 40 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 07 00 01 E2 5D 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 05 00 04 00 04 00 04 00 04 00 00 00 00 00 00 00 00 00 32 00 32 00 00 00 00 00 00 00 03 00 01 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 11 00 1E 27 0D 00 08 00 00 00 00 00 51 B8 05 00 C5 01 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 08 00 01 92 82 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 04 00 00 00 00 00 00 00 00 00 00 00 00 08 00 00 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 11 00 45 27 07 00 00 00 00 00 00 00 07 46 00 00 CC 00 00 4C 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 09 00 01 F2 D1 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 05 00 01 00 01 00 01 00 01 00 0A 00 0A 00 00 00 00 00 23 00 23 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 11 00 17 27 05 00 00 00 00 00 00 00 C8 AD 05 00 C5 01 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 0A 00 01 FC C4 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 0C 00 0C 00 00 00 00 00 58 02 6E 00 00 00 00 00 55 00 2D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 76 F2 49 00 FF FF FF FF 00 00 00 00 00 00 11 00 01 00 07 00 0B 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 0B 00 01 7F 2C 14 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 06 01 03 00 03 00 03 00 03 00 00 00 00 00 6C 00 00 00 00 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 08 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 11 00 F2 27 02 00 04 00 00 00 00 00 29 2E 04 00 AF 01 00 18 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 0C 00 01 5C FA 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 04 00 04 00 04 00 04 00 50 00 50 00 03 00 03 00 0A 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 06 BA 05 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 A3 5E 02 00 AB 01 00 44 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 0D 00 01 60 FA 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 02 00 02 00 02 00 02 00 00 00 00 00 01 00 01 00 14 00 14 00 0F 00 00 00 00 00 05 00 00 00 00 00 08 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 F3 88 03 00 F4 01 00 4C 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 0F 00 01 61 FA 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 04 00 04 00 04 00 04 00 32 00 32 00 02 00 02 00 28 00 28 00 19 00 0F 00 00 00 05 00 00 00 00 00 08 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 A5 B0 05 00 C5 01 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 10 00 01 67 FA 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 02 00 03 00 03 00 03 00 03 00 4B 00 4B 00 01 00 01 00 3C 00 3C 00 14 00 0F 00 00 00 05 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 3C 04 00 AB 01 00 2C 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 11 00 01 6A 1F 11 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 04 00 06 00 06 00 06 00 06 00 3C 00 3C 00 01 00 01 00 28 00 28 00 00 00 00 00 00 00 00 00 00 00 00 00 08 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 11 00 3A 27 08 00 00 00 00 00 00 00 EA 88 03 00 F4 01 00 4C 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 13 00 01 E9 2C 1D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 75 AA 01 00 A6 01 00 3C 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 15 00 01 52 6E 11 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 05 00 05 00 05 00 05 00 C8 00 C8 00 01 00 01 00 78 00 78 00 32 00 32 00 00 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 0D 3C 04 00 AB 01 00 2C 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 16 00 01 4E 46 11 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 03 00 04 00 04 00 04 00 04 00 3C 00 3C 00 00 00 00 00 3C 00 3C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 11 00 17 27 0E 00 00 00 00 00 00 00 B2 AD 05 00 C5 01 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 17 00 01 44 94 11 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 05 00 05 00 05 00 05 00 00 00 00 00 02 00 02 00 19 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1F 4D 05 00 C5 01 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 00 00 01 00 01 43 42 0F 00 01 37 09 9F 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 04 00 01 58 BF 0F 00 01 3A 09 9F 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 05 00 01 06 06 10 00 01 42 09 9F 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 07 00 01 19 5C 10 00 01 40 09 9F 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 08 00 01 77 83 10 00 01 3F 09 9F 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 09 00 01 1C D1 10 00 01 3C 09 9F 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 0B 00 01 85 F9 19 00 01 3D 09 9F 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 00 00 01 00 01 F3 D1 10 00 01 42 BE 9A 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 02 00 01 64 4F 0F 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 08 00 05 00 05 00 05 00 05 00 00 00 00 00 01 00 01 00 50 00 50 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 11 00 1E 27 08 00 3E 27 00 00 00 00 BE AD 05 00 C5 01 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 03 00 01 17 84 10 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 05 00 04 00 04 00 04 00 04 00 28 00 28 00 00 00 00 00 23 00 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 11 00 40 27 0D 00 00 00 00 00 00 00 F7 C0 05 00 C5 01 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 04 00 01 AC 6D 11 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 02 00 02 00 02 00 02 00 64 00 64 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 E8 C8 00 00 A8 01 00 28 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 05 00 01 45 6E 11 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 32 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 79 01 00 00 AF 01 00 44 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 06 00 01 46 6E 11 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 64 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 BB 24 00 00 AF 01 00 44 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 07 00 01 47 6E 11 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 01 00 01 00 01 00 01 00 96 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 8F 55 03 00 B0 01 00 48 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 08 00 01 48 6E 11 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 02 00 02 00 02 00 02 00 C8 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 91 00 00 AA 01 00 38 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 09 00 01 4D 6E 11 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 08 00 08 00 08 00 08 00 50 00 50 00 00 00 00 00 00 00 00 00 50 00 50 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 CF 00 05 00 F7 01 00 4C 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 0A 00 01 8F 2C 14 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 07 00 00 00 00 00 00 00 00 00 00 00 00 00 6C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 91 00 00 AA 01 00 38 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 0B 00 01 9A 2C 14 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 05 01 03 00 03 00 03 00 03 00 00 00 00 00 60 00 00 00 00 00 00 00 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 11 00 3C 27 02 00 00 00 00 00 00 00 DC C2 05 00 C5 01 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 0C 00 01 09 01 16 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 07 00 00 00 00 00 00 00 00 00 00 00 00 00 65 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FE 52 05 00 C5 01 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 0D 00 01 40 12 17 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 E2 F5 04 00 A8 01 00 28 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 0E 00 01 F4 D1 10 00 01 4F BE 9A 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 00 00 00 00 00 00 00 00 01 02 99 84 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 2C 01 00 00 00 00 02 02 99 84 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 32 00 00 00 00 00 03 02 9A 84 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 96 00 00 00 00 00 04 02 61 8A 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 96 00 00 00 00 00 05 02 7A 8A 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 2C 01 00 00 00 00 06 02 7A 8A 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 2C 01 00 00 00 00 07 02 7A 8A 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 28 00 00 00 00 00 08 02 88 8A 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 04 01 00 00 00 00 09 02 88 8A 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 2C 01 00 00 00 00 0A 02 88 8A 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF AA 00 00 00 00 00 0B 02 C9 DA 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF B0 00 00 00 00 00 0C 02 B0 F9 1E 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 0A 00 00 00 00 00 0D 02 59 38 1F 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00 0E 02 B0 38 1F 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00 0F 02 17 45 1F 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00 10 02 39 F2 22 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00 11 02 14 16 25 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 06 00 00 00 00 00 12 02 1A 16 25 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 03 00 00 00 00 00 13 02 62 89 25 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 05 00 00 00 00 00 14 02 BC 4C 26 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00 15 02 B5 24 1F 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00 16 02 11 16 25 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00 00 01 02 9A EE 2D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00 02 02 28 75 38 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00 03 02 0B F3 3C 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 04 00 00 00 00 00 04 02 11 F3 3C 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00 06 02 2E 75 38 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00 00 01 02 15 09 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 23 00 00 00 00 00 02 02 06 0A 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 1A 00 00 00 00 00 03 02 88 0D 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 02 00 00 00 00 00 04 02 71 0E 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00 05 02 99 0E 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 07 00 00 00 00 00 06 02 C9 0E 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00 07 02 F1 0E 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 42 00 00 00 00 00 08 02 FA 0E 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00 09 02 17 5B 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 03 00 00 00 10 00 0A 02 29 A1 3F 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 14 00 00 00 00 00 0B 02 04 C4 41 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 7F 00 00 00 00 00 0C 02 0B C4 41 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 16 00 00 00 00 00 0D 02 0D C4 41 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 82 00 00 00 00 00 0E 02 19 12 42 00 00 00 80 05 BB 46 E6 17 02 00 00 00 00 01 00 00 00 08 00 0F 02 52 0F 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 39 00 00 00 00 00 10 02 0C C4 41 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 19 00 00 00 00 00 12 02 04 0A 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 04 00 00 00 00 00 13 02 71 20 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 00 00 14 02 E8 09 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 0C 00 00 00 00 00 15 02 EB 0E 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 04 00 00 00 00 00 16 02 E9 09 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 16 00 00 00 00 00 17 02 EA 09 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 1C 00 00 00 00 00 18 02 54 0F 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 06 00 00 00 00 00 00 01 02 B0 CD 4F 00 01 4E 09 9F 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 01 00 00 00 02 00 00 00 00 00 00 19 12 42 00 00 00 00 00 02 02 34 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 2D 00 00 00 00 00 01 00 00 00 02 14 30 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 0D 00 00 00 00 00 02 00 00 00 02 21 57 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 0A 00 00 00 00 00 03 00 00 00 02 24 57 3D 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF

     0F 00 00 00 00 00 FF FF FF FF FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B 00 00 00 00 01 00 01 00 75 7B 89 00 75 7B 89 00 03 78 62 25 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0A 00 74 6A 71 6E 74 6B 73 6B 64 6C 34 00 02 98 98 00 04 00 00 00 0C 17 00 00 E7 99 98 00 04 00 00 00 CC 01 00 00 E8 99 98 00 04 00 00 00 06 09 00 00 61 9A 98 00 04 00 00 00 C8 14 00 00 62 9A 98 00 04 00 00 00 C8 14 00 00 63 9A 98 00 04 00 00 00 C0 17 00 00 65 9A 98 00 04 00 00 00 44 16 00 00 67 9B 98 00 04 00 00 00 20 08 00 00 68 9B 98 00 04 00 00 00 5E 01 00 00 13 A0 98 00 00 00 08 00 C8 47 DF 77 14 A0 98 00 00 00 18 00 00 00 00 00 00 00 00 00 0C 2F 31 01 00 00 18 00 00 00 00 00 00 00 00 00 6A F6 41 01 00 00 18 00 00 00 00 00 00 00 00 00 84 C3 C9 01 04 00 00 00 0C 17 00 00 F3 C3 C9 01 20 E0 20 00 00 00 1E 00 1E 00 1E 00 01 2B 77 FC 02 04 00 00 00 CC 01 00 00 2C 77 FC 02 04 00 00 00 06 09 00 00 CF FD FD 02 04 00 00 00 C8 14 00 00 D0 FD FD 02 04 00 00 00 C8 14 00 00 D1 FD FD 02 04 00 00 00 C0 17 00 00 D3 FD FD 02 04 00 00 00 44 16 00 00 D7 FD FD 02 04 00 00 00 20 08 00 00 D8 FD FD 02 04 00 00 00 5E 01 00 00 61 84 FF 02 04 00 00 00 96 00 00 00 62 84 FF 02 04 00 00 00 08 07 00 00 63 84 FF 02 04 00 00 00 96 00 00 00 64 84 FF 02 04 00 00 00 08 07 00 00 65 84 FF 02 04 00 00 00 96 00 00 00 66 84 FF 02 04 00 00 00 08 07 00 00 67 84 FF 02 04 00 00 00 96 00 00 00 68 84 FF 02 04 00 00 00 08 07 00 00 69 84 FF 02 04 00 00 00 96 00 00 00 6A 84 FF 02 04 00 00 00 08 07 00 00 6B 84 FF 02 04 00 00 00 96 00 00 00 6C 84 FF 02 04 00 00 00 08 07 00 00 6D 84 FF 02 04 00 00 00 96 00 00 00 6F 84 FF 02 04 00 00 00 08 07 00 00 70 84 FF 02 04 00 00 00 96 00 00 00 71 84 FF 02 04 00 00 00 08 07 00 00 72 84 FF 02 04 00 00 00 96 00 00 00 73 84 FF 02 04 00 00 00 08 07 00 00 74 84 FF 02 04 00 00 00 96 00 00 00 75 84 FF 02 04 00 00 00 08 07 00 00 76 84 FF 02 04 00 00 00 96 00 00 00 77 84 FF 02 04 00 00 00 08 07 00 00 78 84 FF 02 04 00 00 00 96 00 00 00 79 84 FF 02 04 00 00 00 08 07 00 00 7A 84 FF 02 04 00 00 00 96 00 00 00 7B 84 FF 02 04 00 00 00 08 07 00 00 7C 84 FF 02 04 00 00 00 96 00 00 00 8D 84 FF 02 04 00 00 00 08 07 00 00 8E 84 FF 02 04 00 00 00 96 00 00 00 00 00 00 06 00 00 00 4B 6D 54 00 07 00 00 00 E5 2E 31 01 E6 2E 31 01 E7 2E 31 01 E8 2E 31 01 E9 2E 31 01 EA 2E 31 01 EB 2E 31 01 4C 6D 54 00 06 00 00 00 19 63 3D 01 02 63 3D 01 05 63 3D 01 06 63 3D 01 0A 63 3D 01 0C 63 3D 01 4E 6D 54 00 05 00 00 00 06 2F 31 01 07 2F 31 01 09 2F 31 01 0A 2F 31 01 A7 2E 31 01 48 6D 54 00 08 00 00 00 CE 2E 31 01 CF 2E 31 01 D0 2E 31 01 D1 2E 31 01 D2 2E 31 01 D3 2E 31 01 D4 2E 31 01 D5 2E 31 01 49 6D 54 00 06 00 00 00 03 63 3D 01 04 63 3D 01 07 63 3D 01 08 63 3D 01 09 63 3D 01 0B 63 3D 01 4A 6D 54 00 05 00 00 00 0E 63 3D 01 0F 63 3D 01 10 63 3D 01 11 63 3D 01 12 63 3D 01 38 00 30 00 30 00 30 00 31 00 31 00 30 00 38 00 2F 00 73 00 70 00 65 00 63 00 69 00 61 00 6C 00 2F 00 30 00 00 00 00 00 02 00 14 00 93 01 08 07 02 00 00 00 31 00 00 00 05 00 16 00 9D 00 08 07 A0 01 15 00 E8 54 49 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 1B 00 9A 01 08 07 02 00 00 00 31 00 00 00 08 00 1D 00 64 00 0A 07 01 00 00 00 00 00 00 00 1B FE FD 02 01 00 00 00 00 00 00 00 02 FE FD 02 01 00 00 00 00 00 00 00 2B 77 FC 02 01 00 00 00 00 00 00 00 EA FD FD 02 01 00 00 00 00 00 00 00 CE FD FD 02 01 00 00 00 01 00 00 00 1B FE FD 02 01 00 00 00 01 00 00 00 02 FE FD 02 01 00 00 00 01 00 00 00 3F 77 FC 02 01 00 00 00 01 00 00 00 EA FD FD 02 01 00 00 00 01 00 00 00 CE FD FD 02 02 00 00 00 00 00 00 00 00 2D 31 01 02 00 00 00 00 00 00 00 E0 2E 31 01 02 00 00 00 00 00 00 00 18 63 3D 01 02 00 00 00 00 00 00 00 5E F6 41 01 02 00 00 00 00 00 00 00 7E 55 3A 01 02 00 00 00 01 00 00 00 00 2D 31 01 02 00 00 00 01 00 00 00 E0 2E 31 01 02 00 00 00 01 00 00 00 9A 62 3D 01 02 00 00 00 01 00 00 00 4A F6 41 01 02 00 00 00 01 00 00 00 E8 B3 32 01 03 00 00 00 00 00 00 00 81 C3 C9 01 03 00 00 00 00 00 00 00 2E 4A CB 01 03 00 00 00 00 00 00 00 4E 4A CB 01 03 00 00 00 00 00 00 00 D5 C3 C9 01 03 00 00 00 00 00 00 00 05 D1 CC 01 03 00 00 00 01 00 00 00 81 C3 C9 01 03 00 00 00 01 00 00 00 2E 4A CB 01 03 00 00 00 01 00 00 00 4E 4A CB 01 03 00 00 00 01 00 00 00 D5 C3 C9 01 03 00 00 00 01 00 00 00 05 D1 CC 01 04 00 00 00 00 00 00 00 1B FE FD 02 04 00 00 00 00 00 00 00 02 FE FD 02 04 00 00 00 00 00 00 00 2B 77 FC 02 04 00 00 00 00 00 00 00 EA FD FD 02 04 00 00 00 00 00 00 00 CE FD FD 02 04 00 00 00 01 00 00 00 1B FE FD 02 04 00 00 00 01 00 00 00 02 FE FD 02 04 00 00 00 01 00 00 00 2B 77 FC 02 04 00 00 00 01 00 00 00 EA FD FD 02 04 00 00 00 01 00 00 00 CE FD FD 02 05 00 00 00 00 00 00 00 2B 77 FC 02 05 00 00 00 00 00 00 00 E2 F0 FA 02 05 00 00 00 00 00 00 00 3E 77 FC 02 05 00 00 00 00 00 00 00 EB FD FD 02 05 00 00 00 00 00 00 00 CE 91 02 03 05 00 00 00 01 00 00 00 2B 77 FC 02 05 00 00 00 01 00 00 00 E2 F0 FA 02 05 00 00 00 01 00 00 00 3E 77 FC 02 05 00 00 00 01 00 00 00 EB FD FD 02 05 00 00 00 01 00 00 00 E0 91 02 03 06 00 00 00 00 00 00 00 29 87 93 03 06 00 00 00 00 00 00 00 11 87 93 03 06 00 00 00 00 00 00 00 25 87 93 03 06 00 00 00 00 00 00 00 16 87 93 03 06 00 00 00 00 00 00 00 40 94 96 03 06 00 00 00 01 00 00 00 29 87 93 03 06 00 00 00 01 00 00 00 11 87 93 03 06 00 00 00 01 00 00 00 25 87 93 03 06 00 00 00 01 00 00 00 16 87 93 03 06 00 00 00 01 00 00 00 40 94 96 03 07 00 00 00 00 00 00 00 1B FE FD 02 07 00 00 00 00 00 00 00 02 FE FD 02 07 00 00 00 00 00 00 00 2B 77 FC 02 07 00 00 00 00 00 00 00 EA FD FD 02 07 00 00 00 00 00 00 00 CE FD FD 02 07 00 00 00 01 00 00 00 1B FE FD 02 07 00 00 00 01 00 00 00 02 FE FD 02 07 00 00 00 01 00 00 00 3F 77 FC 02 07 00 00 00 01 00 00 00 EA FD FD 02 07 00 00 00 01 00 00 00 CE FD FD 02 08 00 00 00 00 00 00 00 1B FE FD 02 08 00 00 00 00 00 00 00 02 FE FD 02 08 00 00 00 00 00 00 00 2B 77 FC 02 08 00 00 00 00 00 00 00 EA FD FD 02 08 00 00 00 00 00 00 00 CE FD FD 02 08 00 00 00 01 00 00 00 1B FE FD 02 08 00 00 00 01 00 00 00 02 FE FD 02 08 00 00 00 01 00 00 00 3F 77 FC 02 08 00 00 00 01 00 00 00 EA FD FD 02 08 00 00 00 01 00 00 00 CE FD FD 02 00 00 00 00 00 01
     */
    public static MaplePacket openCashShop(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        MapleCharacter chr = c.getPlayer();
        mplew.writeShort(SendPacketOpcode.CS_OPEN.getValue());
        mplew.writeLong(-1);
        mplew.write0(7);
        addCharStats(mplew, chr);
        mplew.write(chr.getBuddylist().getCapacity()); // buddylist capacity
        if (chr.getLinkedName() == null) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeMapleAsciiString(chr.getLinkedName());
        }
        mplew.write(0); // 여제의 축복
        mplew.write(0); // 여제의 강화
        addInventoryInfo(mplew, chr);
        mplew.write(HexTool.getByteArrayFromHexString("0F 00 00 00 00 00 FF FF FF FF FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B FF C9 9A 3B 00 00 00 00 01 00 01 00 75 7B 89 00 75 7B 89 00 03 78 62 25 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0A 00 74 6A 71 6E 74 6B 73 6B 64 6C 34 00 02 98 98 00 04 00 00 00 0C 17 00 00 E7 99 98 00 04 00 00 00 CC 01 00 00 E8 99 98 00 04 00 00 00 06 09 00 00 61 9A 98 00 04 00 00 00 C8 14 00 00 62 9A 98 00 04 00 00 00 C8 14 00 00 63 9A 98 00 04 00 00 00 C0 17 00 00 65 9A 98 00 04 00 00 00 44 16 00 00 67 9B 98 00 04 00 00 00 20 08 00 00 68 9B 98 00 04 00 00 00 5E 01 00 00 13 A0 98 00 00 00 08 00 C8 47 DF 77 14 A0 98 00 00 00 18 00 00 00 00 00 00 00 00 00 0C 2F 31 01 00 00 18 00 00 00 00 00 00 00 00 00 6A F6 41 01 00 00 18 00 00 00 00 00 00 00 00 00 84 C3 C9 01 04 00 00 00 0C 17 00 00 F3 C3 C9 01 20 E0 20 00 00 00 1E 00 1E 00 1E 00 01 2B 77 FC 02 04 00 00 00 CC 01 00 00 2C 77 FC 02 04 00 00 00 06 09 00 00 CF FD FD 02 04 00 00 00 C8 14 00 00 D0 FD FD 02 04 00 00 00 C8 14 00 00 D1 FD FD 02 04 00 00 00 C0 17 00 00 D3 FD FD 02 04 00 00 00 44 16 00 00 D7 FD FD 02 04 00 00 00 20 08 00 00 D8 FD FD 02 04 00 00 00 5E 01 00 00 61 84 FF 02 04 00 00 00 96 00 00 00 62 84 FF 02 04 00 00 00 08 07 00 00 63 84 FF 02 04 00 00 00 96 00 00 00 64 84 FF 02 04 00 00 00 08 07 00 00 65 84 FF 02 04 00 00 00 96 00 00 00 66 84 FF 02 04 00 00 00 08 07 00 00 67 84 FF 02 04 00 00 00 96 00 00 00 68 84 FF 02 04 00 00 00 08 07 00 00 69 84 FF 02 04 00 00 00 96 00 00 00 6A 84 FF 02 04 00 00 00 08 07 00 00 6B 84 FF 02 04 00 00 00 96 00 00 00 6C 84 FF 02 04 00 00 00 08 07 00 00 6D 84 FF 02 04 00 00 00 96 00 00 00 6F 84 FF 02 04 00 00 00 08 07 00 00 70 84 FF 02 04 00 00 00 96 00 00 00 71 84 FF 02 04 00 00 00 08 07 00 00 72 84 FF 02 04 00 00 00 96 00 00 00 73 84 FF 02 04 00 00 00 08 07 00 00 74 84 FF 02 04 00 00 00 96 00 00 00 75 84 FF 02 04 00 00 00 08 07 00 00 76 84 FF 02 04 00 00 00 96 00 00 00 77 84 FF 02 04 00 00 00 08 07 00 00 78 84 FF 02 04 00 00 00 96 00 00 00 79 84 FF 02 04 00 00 00 08 07 00 00 7A 84 FF 02 04 00 00 00 96 00 00 00 7B 84 FF 02 04 00 00 00 08 07 00 00 7C 84 FF 02 04 00 00 00 96 00 00 00 8D 84 FF 02 04 00 00 00 08 07 00 00 8E 84 FF 02 04 00 00 00 96 00 00 00 00 00 00 06 00 00 00 4B 6D 54 00 07 00 00 00 E5 2E 31 01 E6 2E 31 01 E7 2E 31 01 E8 2E 31 01 E9 2E 31 01 EA 2E 31 01 EB 2E 31 01 4C 6D 54 00 06 00 00 00 19 63 3D 01 02 63 3D 01 05 63 3D 01 06 63 3D 01 0A 63 3D 01 0C 63 3D 01 4E 6D 54 00 05 00 00 00 06 2F 31 01 07 2F 31 01 09 2F 31 01 0A 2F 31 01 A7 2E 31 01 48 6D 54 00 08 00 00 00 CE 2E 31 01 CF 2E 31 01 D0 2E 31 01 D1 2E 31 01 D2 2E 31 01 D3 2E 31 01 D4 2E 31 01 D5 2E 31 01 49 6D 54 00 06 00 00 00 03 63 3D 01 04 63 3D 01 07 63 3D 01 08 63 3D 01 09 63 3D 01 0B 63 3D 01 4A 6D 54 00 05 00 00 00 0E 63 3D 01 0F 63 3D 01 10 63 3D 01 11 63 3D 01 12 63 3D 01 38 00 30 00 30 00 30 00 31 00 31 00 30 00 38 00 2F 00 73 00 70 00 65 00 63 00 69 00 61 00 6C 00 2F 00 30 00 00 00 00 00 02 00 14 00 93 01 08 07 02 00 00 00 31 00 00 00 05 00 16 00 9D 00 08 07 A0 01 15 00 E8 54 49 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 1B 00 9A 01 08 07 02 00 00 00 31 00 00 00 08 00 1D 00 64 00 0A 07 01 00 00 00 00 00 00 00 1B FE FD 02 01 00 00 00 00 00 00 00 02 FE FD 02 01 00 00 00 00 00 00 00 2B 77 FC 02 01 00 00 00 00 00 00 00 EA FD FD 02 01 00 00 00 00 00 00 00 CE FD FD 02 01 00 00 00 01 00 00 00 1B FE FD 02 01 00 00 00 01 00 00 00 02 FE FD 02 01 00 00 00 01 00 00 00 3F 77 FC 02 01 00 00 00 01 00 00 00 EA FD FD 02 01 00 00 00 01 00 00 00 CE FD FD 02 02 00 00 00 00 00 00 00 00 2D 31 01 02 00 00 00 00 00 00 00 E0 2E 31 01 02 00 00 00 00 00 00 00 18 63 3D 01 02 00 00 00 00 00 00 00 5E F6 41 01 02 00 00 00 00 00 00 00 7E 55 3A 01 02 00 00 00 01 00 00 00 00 2D 31 01 02 00 00 00 01 00 00 00 E0 2E 31 01 02 00 00 00 01 00 00 00 9A 62 3D 01 02 00 00 00 01 00 00 00 4A F6 41 01 02 00 00 00 01 00 00 00 E8 B3 32 01 03 00 00 00 00 00 00 00 81 C3 C9 01 03 00 00 00 00 00 00 00 2E 4A CB 01 03 00 00 00 00 00 00 00 4E 4A CB 01 03 00 00 00 00 00 00 00 D5 C3 C9 01 03 00 00 00 00 00 00 00 05 D1 CC 01 03 00 00 00 01 00 00 00 81 C3 C9 01 03 00 00 00 01 00 00 00 2E 4A CB 01 03 00 00 00 01 00 00 00 4E 4A CB 01 03 00 00 00 01 00 00 00 D5 C3 C9 01 03 00 00 00 01 00 00 00 05 D1 CC 01 04 00 00 00 00 00 00 00 1B FE FD 02 04 00 00 00 00 00 00 00 02 FE FD 02 04 00 00 00 00 00 00 00 2B 77 FC 02 04 00 00 00 00 00 00 00 EA FD FD 02 04 00 00 00 00 00 00 00 CE FD FD 02 04 00 00 00 01 00 00 00 1B FE FD 02 04 00 00 00 01 00 00 00 02 FE FD 02 04 00 00 00 01 00 00 00 2B 77 FC 02 04 00 00 00 01 00 00 00 EA FD FD 02 04 00 00 00 01 00 00 00 CE FD FD 02 05 00 00 00 00 00 00 00 2B 77 FC 02 05 00 00 00 00 00 00 00 E2 F0 FA 02 05 00 00 00 00 00 00 00 3E 77 FC 02 05 00 00 00 00 00 00 00 EB FD FD 02 05 00 00 00 00 00 00 00 CE 91 02 03 05 00 00 00 01 00 00 00 2B 77 FC 02 05 00 00 00 01 00 00 00 E2 F0 FA 02 05 00 00 00 01 00 00 00 3E 77 FC 02 05 00 00 00 01 00 00 00 EB FD FD 02 05 00 00 00 01 00 00 00 E0 91 02 03 06 00 00 00 00 00 00 00 29 87 93 03 06 00 00 00 00 00 00 00 11 87 93 03 06 00 00 00 00 00 00 00 25 87 93 03 06 00 00 00 00 00 00 00 16 87 93 03 06 00 00 00 00 00 00 00 40 94 96 03 06 00 00 00 01 00 00 00 29 87 93 03 06 00 00 00 01 00 00 00 11 87 93 03 06 00 00 00 01 00 00 00 25 87 93 03 06 00 00 00 01 00 00 00 16 87 93 03 06 00 00 00 01 00 00 00 40 94 96 03 07 00 00 00 00 00 00 00 1B FE FD 02 07 00 00 00 00 00 00 00 02 FE FD 02 07 00 00 00 00 00 00 00 2B 77 FC 02 07 00 00 00 00 00 00 00 EA FD FD 02 07 00 00 00 00 00 00 00 CE FD FD 02 07 00 00 00 01 00 00 00 1B FE FD 02 07 00 00 00 01 00 00 00 02 FE FD 02 07 00 00 00 01 00 00 00 3F 77 FC 02 07 00 00 00 01 00 00 00 EA FD FD 02 07 00 00 00 01 00 00 00 CE FD FD 02 08 00 00 00 00 00 00 00 1B FE FD 02 08 00 00 00 00 00 00 00 02 FE FD 02 08 00 00 00 00 00 00 00 2B 77 FC 02 08 00 00 00 00 00 00 00 EA FD FD 02 08 00 00 00 00 00 00 00 CE FD FD 02 08 00 00 00 01 00 00 00 1B FE FD 02 08 00 00 00 01 00 00 00 02 FE FD 02 08 00 00 00 01 00 00 00 3F 77 FC 02 08 00 00 00 01 00 00 00 EA FD FD 02 08 00 00 00 01 00 00 00 CE FD FD 02 00 00 00 00 00 01"));
        //mplew.write0(46);
        //for (int x = 0; x < 28; x++) {
        //    mplew.write(CHAR_INFO_MAGIC);
        //}
        //mplew.write0(6);
        //mplew.writeMapleAsciiString(chr.getClient().getAccountName());
        //mplew.write(HexTool.getByteArrayFromHexString("32 00 02 98 98 00 04 00 00 00 0C 17 00 00 E7 99 98 00 04 00 00 00 CC 01 00 00 E8 99 98 00 04 00 00 00 06 09 00 00 61 9A 98 00 04 00 00 00 C8 14 00 00 62 9A 98 00 04 00 00 00 C8 14 00 00 63 9A 98 00 04 00 00 00 C0 17 00 00 65 9A 98 00 04 00 00 00 44 16 00 00 67 9B 98 00 04 00 00 00 20 08 00 00 68 9B 98 00 04 00 00 00 5E 01 00 00 84 C3 C9 01 04 00 00 00 0C 17 00 00 2B 77 FC 02 04 00 00 00 CC 01 00 00 2C 77 FC 02 04 00 00 00 06 09 00 00 CF FD FD 02 04 00 00 00 C8 14 00 00 D0 FD FD 02 04 00 00 00 C8 14 00 00 D1 FD FD 02 04 00 00 00 C0 17 00 00 D3 FD FD 02 04 00 00 00 44 16 00 00 D7 FD FD 02 04 00 00 00 20 08 00 00 D8 FD FD 02 04 00 00 00 5E 01 00 00 61 84 FF 02 04 00 00 00 96 00 00 00 62 84 FF 02 04 00 00 00 08 07 00 00 63 84 FF 02 04 00 00 00 96 00 00 00 64 84 FF 02 04 00 00 00 08 07 00 00 65 84 FF 02 04 00 00 00 96 00 00 00 66 84 FF 02 04 00 00 00 08 07 00 00 67 84 FF 02 04 00 00 00 96 00 00 00 68 84 FF 02 04 00 00 00 08 07 00 00 69 84 FF 02 04 00 00 00 96 00 00 00 6A 84 FF 02 04 00 00 00 08 07 00 00 6B 84 FF 02 04 00 00 00 96 00 00 00 6C 84 FF 02 04 00 00 00 08 07 00 00 6D 84 FF 02 04 00 00 00 96 00 00 00 6F 84 FF 02 04 00 00 00 08 07 00 00 70 84 FF 02 04 00 00 00 96 00 00 00 71 84 FF 02 04 00 00 00 08 07 00 00 72 84 FF 02 04 00 00 00 96 00 00 00 73 84 FF 02 04 00 00 00 08 07 00 00 74 84 FF 02 04 00 00 00 96 00 00 00 75 84 FF 02 04 00 00 00 08 07 00 00 76 84 FF 02 04 00 00 00 96 00 00 00 77 84 FF 02 04 00 00 00 08 07 00 00 78 84 FF 02 04 00 00 00 96 00 00 00 79 84 FF 02 04 00 00 00 08 07 00 00 7A 84 FF 02 04 00 00 00 96 00 00 00 7B 84 FF 02 04 00 00 00 08 07 00 00 7C 84 FF 02 04 00 00 00 96 00 00 00 8D 84 FF 02 04 00 00 00 08 07 00 00 8E 84 FF 02 04 00 00 00 96 00 00 00 DD 0D 95 03 00 04 00 00 00 8A 94 96 03 00 04 00 00 01 8B 94 96 03 00 04 00 00 01 00 00 00 05 00 00 00 4B 6D 54 00 07 00 00 00 E5 2E 31 01 E6 2E 31 01 E7 2E 31 01 E8 2E 31 01 E9 2E 31 01 EA 2E 31 01 EB 2E 31 01 4C 6D 54 00 06 00 00 00 19 63 3D 01 02 63 3D 01 05 63 3D 01 06 63 3D 01 0A 63 3D 01 0C 63 3D 01 48 6D 54 00 08 00 00 00 CE 2E 31 01 CF 2E 31 01 D0 2E 31 01 D1 2E 31 01 D2 2E 31 01 D3 2E 31 01 D4 2E 31 01 D5 2E 31 01 49 6D 54 00 06 00 00 00 03 63 3D 01 04 63 3D 01 07 63 3D 01 08 63 3D 01 09 63 3D 01 0B 63 3D 01 4A 6D 54 00 05 00 00 00 0E 63 3D 01 0F 63 3D 01 10 63 3D 01 11 63 3D 01 12 63 3D 01 7C B9 20 00 C0 D0 E0 AC 20 00 74 C7 D9 B3 60 D5 20 00 18 C2 20 00 88 C7 E4 B2 2E 00 00 00 00 00 05 00 13 00 FC 01 08 07 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 07 00 18 00 FB 01 0C 07 26 00 00 00 73 00 6B 00 69 00 6C 00 6C 00 2F 00 38 00 30 00 30 00 30 00 31 00 30 00 31 00 36 00 2F 00 69 00 63 00 6F 00 01 00 00 00 00 00 00 00 E7 F0 FA 02 01 00 00 00 00 00 00 00 81 C3 C9 01 01 00 00 00 00 00 00 00 97 C3 C9 01 01 00 00 00 00 00 00 00 02 FE FD 02 01 00 00 00 00 00 00 00 2E 4A CB 01 01 00 00 00 01 00 00 00 E7 F0 FA 02 01 00 00 00 01 00 00 00 81 C3 C9 01 01 00 00 00 01 00 00 00 97 C3 C9 01 01 00 00 00 01 00 00 00 02 FE FD 02 01 00 00 00 01 00 00 00 2E 4A CB 01 02 00 00 00 00 00 00 00 00 2D 31 01 02 00 00 00 00 00 00 00 9A 62 3D 01 02 00 00 00 00 00 00 00 1D F6 41 01 02 00 00 00 00 00 00 00 A5 62 3D 01 02 00 00 00 00 00 00 00 1F F6 41 01 02 00 00 00 01 00 00 00 00 2D 31 01 02 00 00 00 01 00 00 00 9A 62 3D 01 02 00 00 00 01 00 00 00 1D F6 41 01 02 00 00 00 01 00 00 00 A5 62 3D 01 02 00 00 00 01 00 00 00 1F F6 41 01 03 00 00 00 00 00 00 00 81 C3 C9 01 03 00 00 00 00 00 00 00 97 C3 C9 01 03 00 00 00 00 00 00 00 2E 4A CB 01 03 00 00 00 00 00 00 00 00 D1 CC 01 03 00 00 00 00 00 00 00 82 C3 C9 01 03 00 00 00 01 00 00 00 81 C3 C9 01 03 00 00 00 01 00 00 00 97 C3 C9 01 03 00 00 00 01 00 00 00 2E 4A CB 01 03 00 00 00 01 00 00 00 00 D1 CC 01 03 00 00 00 01 00 00 00 82 C3 C9 01 04 00 00 00 00 00 00 00 E7 F0 FA 02 04 00 00 00 00 00 00 00 81 C3 C9 01 04 00 00 00 00 00 00 00 97 C3 C9 01 04 00 00 00 00 00 00 00 02 FE FD 02 04 00 00 00 00 00 00 00 2E 4A CB 01 04 00 00 00 01 00 00 00 E7 F0 FA 02 04 00 00 00 01 00 00 00 81 C3 C9 01 04 00 00 00 01 00 00 00 97 C3 C9 01 04 00 00 00 01 00 00 00 02 FE FD 02 04 00 00 00 01 00 00 00 2E 4A CB 01 05 00 00 00 00 00 00 00 E7 F0 FA 02 05 00 00 00 00 00 00 00 EA F0 FA 02 05 00 00 00 00 00 00 00 2B 77 FC 02 05 00 00 00 00 00 00 00 C4 FD FD 02 05 00 00 00 00 00 00 00 36 77 FC 02 05 00 00 00 01 00 00 00 E7 F0 FA 02 05 00 00 00 01 00 00 00 EA F0 FA 02 05 00 00 00 01 00 00 00 2B 77 FC 02 05 00 00 00 01 00 00 00 C4 FD FD 02 05 00 00 00 01 00 00 00 36 77 FC 02 06 00 00 00 00 00 00 00 21 87 93 03 06 00 00 00 00 00 00 00 11 87 93 03 06 00 00 00 00 00 00 00 25 87 93 03 06 00 00 00 00 00 00 00 1D 87 93 03 06 00 00 00 00 00 00 00 40 94 96 03 06 00 00 00 01 00 00 00 21 87 93 03 06 00 00 00 01 00 00 00 11 87 93 03 06 00 00 00 01 00 00 00 25 87 93 03 06 00 00 00 01 00 00 00 1D 87 93 03 06 00 00 00 01 00 00 00 40 94 96 03 07 00 00 00 00 00 00 00 E7 F0 FA 02 07 00 00 00 00 00 00 00 81 C3 C9 01 07 00 00 00 00 00 00 00 97 C3 C9 01 07 00 00 00 00 00 00 00 02 FE FD 02 07 00 00 00 00 00 00 00 2E 4A CB 01 07 00 00 00 01 00 00 00 E7 F0 FA 02 07 00 00 00 01 00 00 00 81 C3 C9 01 07 00 00 00 01 00 00 00 97 C3 C9 01 07 00 00 00 01 00 00 00 02 FE FD 02 07 00 00 00 01 00 00 00 2E 4A CB 01 08 00 00 00 00 00 00 00 E7 F0 FA 02 08 00 00 00 00 00 00 00 81 C3 C9 01 08 00 00 00 00 00 00 00 97 C3 C9 01 08 00 00 00 00 00 00 00 02 FE FD 02 08 00 00 00 00 00 00 00 2E 4A CB 01 08 00 00 00 01 00 00 00 E7 F0 FA 02 08 00 00 00 01 00 00 00 81 C3 C9 01 08 00 00 00 01 00 00 00 97 C3 C9 01 08 00 00 00 01 00 00 00 02 FE FD 02 08 00 00 00 01 00 00 00 2E 4A CB 01 00 00 00 00 00 01"));
        return mplew.getPacket();
    }

public static MaplePacket warpCS(MapleClient c, boolean mts) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        MapleCharacter chr = c.getPlayer();
        mplew.writeShort(mts ? SendPacketOpcode.MTS_OPEN.getValue() : SendPacketOpcode.CS_OPEN.getValue());
        mplew.writeLong(-1);
        mplew.write0(7);
        addCharStats(mplew, chr);
        mplew.write(chr.getBuddylist().getCapacity()); // buddylist capacity
        if (chr.getLinkedName() == null) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeMapleAsciiString(chr.getLinkedName());
        }
        mplew.write(0); // 여제의 축복
        mplew.write(0); // 여제의 강화
        addInventoryInfo(mplew, chr);
        mplew.write0(46);
        for (int x = 0; x < 28; x++) {
            mplew.write(CHAR_INFO_MAGIC);
        }
        mplew.write0(6);
        mplew.writeMapleAsciiString(chr.getClient().getAccountName());
        if (mts) {
            mplew.write(HexTool.getByteArrayFromHexString("88 13 00 00 07 00 00 00 F4 01 00 00 18 00 00 00 A8 00 00 00 70 AA A7 C5 4E C1 CA 01"));
        } else {
            mplew.write(HexTool.getByteArrayFromHexString("32 00 02 98 98 00 04 00 00 00 0C 17 00 00 E7 99 98 00 04 00 00 00 CC 01 00 00 E8 99 98 00 04 00 00 00 06 09 00 00 61 9A 98 00 04 00 00 00 C8 14 00 00 62 9A 98 00 04 00 00 00 C8 14 00 00 63 9A 98 00 04 00 00 00 C0 17 00 00 65 9A 98 00 04 00 00 00 44 16 00 00 67 9B 98 00 04 00 00 00 20 08 00 00 68 9B 98 00 04 00 00 00 5E 01 00 00 84 C3 C9 01 04 00 00 00 0C 17 00 00 2B 77 FC 02 04 00 00 00 CC 01 00 00 2C 77 FC 02 04 00 00 00 06 09 00 00 CF FD FD 02 04 00 00 00 C8 14 00 00 D0 FD FD 02 04 00 00 00 C8 14 00 00 D1 FD FD 02 04 00 00 00 C0 17 00 00 D3 FD FD 02 04 00 00 00 44 16 00 00 D7 FD FD 02 04 00 00 00 20 08 00 00 D8 FD FD 02 04 00 00 00 5E 01 00 00 61 84 FF 02 04 00 00 00 96 00 00 00 62 84 FF 02 04 00 00 00 08 07 00 00 63 84 FF 02 04 00 00 00 96 00 00 00 64 84 FF 02 04 00 00 00 08 07 00 00 65 84 FF 02 04 00 00 00 96 00 00 00 66 84 FF 02 04 00 00 00 08 07 00 00 67 84 FF 02 04 00 00 00 96 00 00 00 68 84 FF 02 04 00 00 00 08 07 00 00 69 84 FF 02 04 00 00 00 96 00 00 00 6A 84 FF 02 04 00 00 00 08 07 00 00 6B 84 FF 02 04 00 00 00 96 00 00 00 6C 84 FF 02 04 00 00 00 08 07 00 00 6D 84 FF 02 04 00 00 00 96 00 00 00 6F 84 FF 02 04 00 00 00 08 07 00 00 70 84 FF 02 04 00 00 00 96 00 00 00 71 84 FF 02 04 00 00 00 08 07 00 00 72 84 FF 02 04 00 00 00 96 00 00 00 73 84 FF 02 04 00 00 00 08 07 00 00 74 84 FF 02 04 00 00 00 96 00 00 00 75 84 FF 02 04 00 00 00 08 07 00 00 76 84 FF 02 04 00 00 00 96 00 00 00 77 84 FF 02 04 00 00 00 08 07 00 00 78 84 FF 02 04 00 00 00 96 00 00 00 79 84 FF 02 04 00 00 00 08 07 00 00 7A 84 FF 02 04 00 00 00 96 00 00 00 7B 84 FF 02 04 00 00 00 08 07 00 00 7C 84 FF 02 04 00 00 00 96 00 00 00 8D 84 FF 02 04 00 00 00 08 07 00 00 8E 84 FF 02 04 00 00 00 96 00 00 00 DD 0D 95 03 00 04 00 00 00 8A 94 96 03 00 04 00 00 01 8B 94 96 03 00 04 00 00 01 00 00 00 05 00 00 00 4B 6D 54 00 07 00 00 00 E5 2E 31 01 E6 2E 31 01 E7 2E 31 01 E8 2E 31 01 E9 2E 31 01 EA 2E 31 01 EB 2E 31 01 4C 6D 54 00 06 00 00 00 19 63 3D 01 02 63 3D 01 05 63 3D 01 06 63 3D 01 0A 63 3D 01 0C 63 3D 01 48 6D 54 00 08 00 00 00 CE 2E 31 01 CF 2E 31 01 D0 2E 31 01 D1 2E 31 01 D2 2E 31 01 D3 2E 31 01 D4 2E 31 01 D5 2E 31 01 49 6D 54 00 06 00 00 00 03 63 3D 01 04 63 3D 01 07 63 3D 01 08 63 3D 01 09 63 3D 01 0B 63 3D 01 4A 6D 54 00 05 00 00 00 0E 63 3D 01 0F 63 3D 01 10 63 3D 01 11 63 3D 01 12 63 3D 01 7C B9 20 00 C0 D0 E0 AC 20 00 74 C7 D9 B3 60 D5 20 00 18 C2 20 00 88 C7 E4 B2 2E 00 00 00 00 00 05 00 13 00 FC 01 08 07 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 07 00 18 00 FB 01 0C 07 26 00 00 00 73 00 6B 00 69 00 6C 00 6C 00 2F 00 38 00 30 00 30 00 30 00 31 00 30 00 31 00 36 00 2F 00 69 00 63 00 6F 00 01 00 00 00 00 00 00 00 E7 F0 FA 02 01 00 00 00 00 00 00 00 81 C3 C9 01 01 00 00 00 00 00 00 00 97 C3 C9 01 01 00 00 00 00 00 00 00 02 FE FD 02 01 00 00 00 00 00 00 00 2E 4A CB 01 01 00 00 00 01 00 00 00 E7 F0 FA 02 01 00 00 00 01 00 00 00 81 C3 C9 01 01 00 00 00 01 00 00 00 97 C3 C9 01 01 00 00 00 01 00 00 00 02 FE FD 02 01 00 00 00 01 00 00 00 2E 4A CB 01 02 00 00 00 00 00 00 00 00 2D 31 01 02 00 00 00 00 00 00 00 9A 62 3D 01 02 00 00 00 00 00 00 00 1D F6 41 01 02 00 00 00 00 00 00 00 A5 62 3D 01 02 00 00 00 00 00 00 00 1F F6 41 01 02 00 00 00 01 00 00 00 00 2D 31 01 02 00 00 00 01 00 00 00 9A 62 3D 01 02 00 00 00 01 00 00 00 1D F6 41 01 02 00 00 00 01 00 00 00 A5 62 3D 01 02 00 00 00 01 00 00 00 1F F6 41 01 03 00 00 00 00 00 00 00 81 C3 C9 01 03 00 00 00 00 00 00 00 97 C3 C9 01 03 00 00 00 00 00 00 00 2E 4A CB 01 03 00 00 00 00 00 00 00 00 D1 CC 01 03 00 00 00 00 00 00 00 82 C3 C9 01 03 00 00 00 01 00 00 00 81 C3 C9 01 03 00 00 00 01 00 00 00 97 C3 C9 01 03 00 00 00 01 00 00 00 2E 4A CB 01 03 00 00 00 01 00 00 00 00 D1 CC 01 03 00 00 00 01 00 00 00 82 C3 C9 01 04 00 00 00 00 00 00 00 E7 F0 FA 02 04 00 00 00 00 00 00 00 81 C3 C9 01 04 00 00 00 00 00 00 00 97 C3 C9 01 04 00 00 00 00 00 00 00 02 FE FD 02 04 00 00 00 00 00 00 00 2E 4A CB 01 04 00 00 00 01 00 00 00 E7 F0 FA 02 04 00 00 00 01 00 00 00 81 C3 C9 01 04 00 00 00 01 00 00 00 97 C3 C9 01 04 00 00 00 01 00 00 00 02 FE FD 02 04 00 00 00 01 00 00 00 2E 4A CB 01 05 00 00 00 00 00 00 00 E7 F0 FA 02 05 00 00 00 00 00 00 00 EA F0 FA 02 05 00 00 00 00 00 00 00 2B 77 FC 02 05 00 00 00 00 00 00 00 C4 FD FD 02 05 00 00 00 00 00 00 00 36 77 FC 02 05 00 00 00 01 00 00 00 E7 F0 FA 02 05 00 00 00 01 00 00 00 EA F0 FA 02 05 00 00 00 01 00 00 00 2B 77 FC 02 05 00 00 00 01 00 00 00 C4 FD FD 02 05 00 00 00 01 00 00 00 36 77 FC 02 06 00 00 00 00 00 00 00 21 87 93 03 06 00 00 00 00 00 00 00 11 87 93 03 06 00 00 00 00 00 00 00 25 87 93 03 06 00 00 00 00 00 00 00 1D 87 93 03 06 00 00 00 00 00 00 00 40 94 96 03 06 00 00 00 01 00 00 00 21 87 93 03 06 00 00 00 01 00 00 00 11 87 93 03 06 00 00 00 01 00 00 00 25 87 93 03 06 00 00 00 01 00 00 00 1D 87 93 03 06 00 00 00 01 00 00 00 40 94 96 03 07 00 00 00 00 00 00 00 E7 F0 FA 02 07 00 00 00 00 00 00 00 81 C3 C9 01 07 00 00 00 00 00 00 00 97 C3 C9 01 07 00 00 00 00 00 00 00 02 FE FD 02 07 00 00 00 00 00 00 00 2E 4A CB 01 07 00 00 00 01 00 00 00 E7 F0 FA 02 07 00 00 00 01 00 00 00 81 C3 C9 01 07 00 00 00 01 00 00 00 97 C3 C9 01 07 00 00 00 01 00 00 00 02 FE FD 02 07 00 00 00 01 00 00 00 2E 4A CB 01 08 00 00 00 00 00 00 00 E7 F0 FA 02 08 00 00 00 00 00 00 00 81 C3 C9 01 08 00 00 00 00 00 00 00 97 C3 C9 01 08 00 00 00 00 00 00 00 02 FE FD 02 08 00 00 00 00 00 00 00 2E 4A CB 01 08 00 00 00 01 00 00 00 E7 F0 FA 02 08 00 00 00 01 00 00 00 81 C3 C9 01 08 00 00 00 01 00 00 00 97 C3 C9 01 08 00 00 00 01 00 00 00 02 FE FD 02 08 00 00 00 01 00 00 00 2E 4A CB 01 00 00 00 00 00 01"));
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendSpouseChat(MapleCharacter wife, String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SPOUSE_CHAT.getValue());
        mplew.writeMapleAsciiString(wife.getName());
        mplew.writeMapleAsciiString(msg);
        return mplew.getPacket();
    }

    public static MaplePacket addMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x00);
        mplew.write(position);
        addCharLook(mplew, chr, true);
        mplew.writeMapleAsciiString(from);
        mplew.write(channel);
        mplew.write(0x00);
        return mplew.getPacket();
    }

    public static MaplePacket removeMessengerPlayer(int position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x02);
        mplew.write(position);
        return mplew.getPacket();
    }

    public static MaplePacket updateMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x07);
        mplew.write(position);
        addCharLook(mplew, chr, true);
        mplew.writeMapleAsciiString(from);
        mplew.write(channel);
        mplew.write(0x00);
        return mplew.getPacket();
    }

    public static MaplePacket joinMessenger(int position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x01);
        mplew.write(position);
        return mplew.getPacket();
    }

    public static MaplePacket messengerChat(String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0x06);
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }

    public static MaplePacket messengerNote(String text, int mode, int mode2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(text);
        mplew.write(mode2);
        return mplew.getPacket();
    }

  /*  public static MaplePacket updatePet(MaplePet pet) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("00 02 03 05"));
        mplew.write(pet.getPosition());
        mplew.writeShort(0);
        mplew.write(5);
        mplew.write(pet.getPosition());
        mplew.write(0);
        mplew.write(3);
        mplew.writeInt(pet.getItemId());
        mplew.write(1);
        mplew.writeInt(pet.getUniqueId());
        mplew.writeInt(0);
        mplew.write(HexTool.getByteArrayFromHexString("00 40 6F E5 0F E7 17 02"));
        String petname = pet.getName();
        if (petname.length() > 13) {
            petname = petname.substring(0, 13);
        }
        mplew.writeAsciiString(petname);
        for (int i = petname.length(); i < 13; ++i) {
            mplew.write(0);
        }
        mplew.write(pet.getLevel());
        mplew.writeShort(pet.getCloseness());
        mplew.write(pet.getFullness());
        mplew.write(0);
        mplew.write(ITEM_MAGIC);
        mplew.write(HexTool.getByteArrayFromHexString("BB 46 E6 17 02 00 00 00 00 50 46 00 00 00 00"));
        return mplew.getPacket();
    }*/

    public static MaplePacket updatePet(MaplePet pet) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("00 02 03 05"));
        mplew.write(pet.getPosition());
        mplew.writeShort(0);
        mplew.write(5);
        mplew.write(pet.getPosition());
        mplew.write(0);
        addPetInfo(mplew, pet);
        return mplew.getPacket();
    }

     public static void addPetInfo(MaplePacketLittleEndianWriter mplew, MaplePet pet) {
        mplew.write(3);
        mplew.writeInt(pet.getItemId());
        mplew.write(1);
        mplew.writeInt(pet.getUniqueId());
        mplew.writeInt(0);
        mplew.write(NON_EXPIRE);
        String petname = pet.getName();
        if (petname.length() > 13) {
            petname = petname.substring(0, 13);
        }
        mplew.writeAsciiString(petname);
        for (int i = petname.length(); i < 13; ++i) {
            mplew.write(0);
        }
        mplew.write(pet.getLevel());
        mplew.writeShort(pet.getCloseness());
        mplew.write(pet.getFullness());
        mplew.writeLong(getKoreanTimestamp((long) (System.currentTimeMillis() * 1.2)));
        mplew.write0(10);
    }

    public static MaplePacket showPet(MapleCharacter chr, MaplePet pet, boolean remove, boolean hunger) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SPAWN_PET.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(chr.getPetIndex(pet));
        if (remove) {
            mplew.write(0);
            mplew.write(hunger ? 1 : 0);
        } else {
            mplew.write(1);
            mplew.write(1);//0x00 -> 0x01 v88
            mplew.writeInt(pet.getItemId());
            mplew.writeMapleAsciiString(pet.getName());
            mplew.writeInt(pet.getUniqueId());
            mplew.writeInt(0);
            mplew.writeShort(pet.getPos().x);
            mplew.writeShort(pet.getPos().y);
            mplew.write(pet.getStance());
            mplew.writeInt(pet.getFh());
        }
        return mplew.getPacket();
    }

    public static MaplePacket movePet(int cid, int pid, int initX, int initY, int slot, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MOVE_PET.getValue());
        mplew.writeInt(cid);
        mplew.write(slot);
        mplew.writeShort(initX);
        mplew.writeShort(initY);
        mplew.writeInt(10);
        serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }

    public static MaplePacket petChat(int cid, int index, int act, String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PET_CHAT.getValue());
        mplew.writeInt(cid);
        mplew.write(index);
        mplew.write(0);
        mplew.write(act);
        mplew.writeMapleAsciiString(text);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket commandResponse(int cid, int index, int animation, boolean success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PET_COMMAND.getValue());
        mplew.writeInt(cid);
        mplew.write(index);
        mplew.write((animation == 1 && success) ? 1 : 0);
        mplew.write(animation);
        if (animation == 1) {
            mplew.write(0);
        } else {
            mplew.writeShort(success ? 1 : 0);
        }
        mplew.write(HexTool.getByteArrayFromHexString("11 03 00 3F 3F 3F"));
        return mplew.getPacket();
    }

    public static MaplePacket showOwnPetLevelUp(int index) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(4);
        mplew.write(0);
        mplew.write(index); // Pet Index
        return mplew.getPacket();
    }

    public static MaplePacket showPetLevelUp(MapleCharacter chr, int index) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(4);
        mplew.write(0);
        mplew.write(index);
        return mplew.getPacket();
    }

    public static MaplePacket changePetName(MapleCharacter chr, String newname, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PET_NAMECHANGE.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(0);
        mplew.writeMapleAsciiString(newname);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket petStatUpdate(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        int mask = 0;
        mask |= MapleStat.PET.getValue();
        mplew.write(0);
        mplew.writeInt(mask);
        MaplePet[] pets = chr.getPets();
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                mplew.writeInt(pets[i].getUniqueId());
                mplew.writeInt(0);
            } else {
                mplew.writeLong(0);
            }
        }
        mplew.write(0);
        mplew.write(1);
        mplew.write(2);
        return mplew.getPacket();
    }

    public static MaplePacket showForcedEquip() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.FORCED_MAP_EQUIP.getValue());
        return mplew.getPacket();
    }

    public static MaplePacket summonSkill(int cid, int summonSkillId, int newStance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SUMMON_SKILL.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        mplew.write(newStance);
        return mplew.getPacket();
    }

    public static MaplePacket skillCooldown(int sid, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.COOLDOWN.getValue());
        mplew.writeInt(sid);
        mplew.writeInt(time);
        return mplew.getPacket();
    }

    public static MaplePacket skillBookSuccess(MapleCharacter chr, int skillid, int maxlevel, boolean canuse, boolean success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.USE_SKILL_BOOK.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        mplew.writeInt(skillid);
        mplew.writeInt(maxlevel);
        mplew.write(canuse ? 1 : 0);
        mplew.write(success ? 1 : 0);
        return mplew.getPacket();
    }

    public static MaplePacket getMacros(SkillMacro[] macros) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SKILL_MACRO.getValue());
        int count = 0;
        for (int i = 0; i < 5; i++) {
            if (macros[i] != null) {
                count++;
            }
        }
        mplew.write(count);
        for (int i = 0; i < 5; i++) {
            SkillMacro macro = macros[i];
            if (macro != null) {
                mplew.writeMapleAsciiString(macro.getName());
                mplew.write(macro.getShout());
                mplew.writeInt(macro.getSkill1());
                mplew.writeInt(macro.getSkill2());
                mplew.writeInt(macro.getSkill3());
            }
        }
        return mplew.getPacket();
    }

    public static MaplePacket getPlayerNPC(PlayerNPCs npc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_NPC.getValue());
        mplew.write(0x01);
        mplew.writeInt(npc.getId());
        mplew.writeMapleAsciiString(npc.getName());
        mplew.write(0); // direction
        mplew.write(npc.getSkin());
        mplew.writeInt(npc.getFace());
        mplew.write(0);
        mplew.writeInt(npc.getHair());
        Map<Byte, Integer> equip = npc.getEquips();
        Map<Byte, Integer> myEquip = new LinkedHashMap<Byte, Integer>();
        for (byte position : equip.keySet()) {
            byte pos = (byte) (position * -1);
            if (pos > 100) {
                pos -= 100;
                myEquip.put(pos, equip.get(position));
            } else {
                if (myEquip.get(pos) == null) {
                    myEquip.put(pos, equip.get(position));
                }
            }
        }
        for (Entry<Byte, Integer> entry : myEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.writeShort(-1);
        Integer cWeapon = equip.get((byte) -111);
        if (cWeapon != null) {
            mplew.writeInt(cWeapon);
        } else {
            mplew.writeInt(0);
        }
        for (int i = 0; i < 12; i++) {
            mplew.write(0);
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateAriantPQRanking(String name, int score, boolean empty) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ARIANT_SCORE.getValue());
        mplew.write(empty ? 0 : 1);
        if (!empty) {
            mplew.writeMapleAsciiString(name);
            mplew.writeInt(score);
        }
        return mplew.getPacket();
    }

    public static MaplePacket catchMonster(int monsobid, int itemid, byte success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (itemid == 2270002) {
            mplew.writeShort(SendPacketOpcode.CATCH_ARIANT.getValue());
        } else {
            mplew.writeShort(SendPacketOpcode.CATCH_MOUNT.getValue());
        }
        mplew.writeInt(monsobid);
        mplew.writeInt(itemid);
        mplew.write(success);
        return mplew.getPacket();
    }

    public static MaplePacket showAllCharacter(int chars, int unk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ALL_CHARLIST.getValue());
        mplew.write(1);
        mplew.writeInt(chars);
        mplew.writeInt(unk);
        return mplew.getPacket();
    }

    public static MaplePacket showAllCharacterInfo(int worldid, List<MapleCharacter> chars) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ALL_CHARLIST.getValue());
        mplew.write(0);
        mplew.write(worldid);
        mplew.write(chars.size());
        for (MapleCharacter chr : chars) {
            addCharEntry(mplew, chr);
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateMount(int charid, MapleMount mount, boolean levelup) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_MOUNT.getValue());
        mplew.writeInt(charid);
        mplew.writeInt(mount.getLevel());
        mplew.writeInt(mount.getExp());
        mplew.writeInt(mount.getTiredness());
        mplew.write(levelup ? (byte) 1 : (byte) 0);
        return mplew.getPacket();
    }

    public static MaplePacket boatPacket(boolean type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.BOAT_EFFECT.getValue());
        mplew.writeShort(type ? 1 : 2);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGame(MapleClient c, MapleMiniGame minigame, boolean owner, int piece) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("05 01 02"));
        mplew.write(owner ? 0 : 1);
        mplew.write(0);
        addCharLook(mplew, minigame.getOwner(), false);
        mplew.writeMapleAsciiString(minigame.getOwner().getName());
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            mplew.write(1);
            addCharLook(mplew, visitor, false);
            mplew.writeMapleAsciiString(visitor.getName());
        }
        mplew.write(0xFF);
        mplew.write(0);
        mplew.writeInt(1);
        mplew.writeInt(minigame.getOwner().getMiniGamePoints("wins", true));
        mplew.writeInt(minigame.getOwner().getMiniGamePoints("ties", true));
        mplew.writeInt(minigame.getOwner().getMiniGamePoints("losses", true));
        mplew.writeInt(2000);
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            mplew.write(1);
            mplew.writeInt(1);
            mplew.writeInt(visitor.getMiniGamePoints("wins", true));
            mplew.writeInt(visitor.getMiniGamePoints("ties", true));
            mplew.writeInt(visitor.getMiniGamePoints("losses", true));
            mplew.writeInt(2000);
        }
        mplew.write(0xFF);
        mplew.writeMapleAsciiString(minigame.getDescription());
        mplew.write(piece);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameReady(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x34);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameUnReady(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x35);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameStart(MapleMiniGame game, int loser) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("37 0" + loser));
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameSkipOwner(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x39);
        mplew.write(0x01);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameRequestTie(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x2C);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameDenyTie(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x2D);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameFull() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("05 00"));
        mplew.write(2);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameSkipVisitor(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("39 00"));
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameMoveOmok(MapleMiniGame game, int move1, int move2, int move3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(12);
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("3A"));
        mplew.writeInt(move1);
        mplew.writeInt(move2);
        mplew.write(move3);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameNewVisitor(MapleCharacter c, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("04 0" + slot));
        addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        mplew.writeInt(1);
        mplew.writeInt(c.getMiniGamePoints("wins", true));
        mplew.writeInt(c.getMiniGamePoints("ties", true));
        mplew.writeInt(c.getMiniGamePoints("losses", true));
        mplew.writeInt(2000);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameRemoveVisitor() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("0A 01"));
        return mplew.getPacket();
    }

    private static MaplePacket getMiniGameResult(MapleMiniGame game, int win, int lose, int tie, int result, int forfeit, boolean omok) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x38);
        if (tie == 0 && forfeit != 1) {
            mplew.write(0);
        } else if (tie == 1) {
            mplew.write(1);
        } else if (forfeit == 1) {
            mplew.write(2);
        }
        mplew.write(0); // owner
        mplew.writeInt(1); // unknown
        mplew.writeInt(game.getOwner().getMiniGamePoints("wins", omok) + win); // wins
        mplew.writeInt(game.getOwner().getMiniGamePoints("ties", omok) + tie); // ties
        mplew.writeInt(game.getOwner().getMiniGamePoints("losses", omok) + lose); // losses
        mplew.writeInt(2000); // points
        mplew.writeInt(1); // start of visitor; unknown
        mplew.writeInt(game.getVisitor().getMiniGamePoints("wins", omok) + lose); // wins
        mplew.writeInt(game.getVisitor().getMiniGamePoints("ties", omok) + tie); // ties
        mplew.writeInt(game.getVisitor().getMiniGamePoints("losses", omok) + win); // losses
        mplew.writeInt(2000); // points
        game.getOwner().setMiniGamePoints(game.getVisitor(), result, omok);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameOwnerWin(MapleMiniGame game) {
        return getMiniGameResult(game, 0, 1, 0, 1, 0, true);
    }

    public static MaplePacket getMiniGameVisitorWin(MapleMiniGame game) {
        return getMiniGameResult(game, 1, 0, 0, 2, 0, true);
    }

    public static MaplePacket getMiniGameTie(MapleMiniGame game) {
        return getMiniGameResult(game, 0, 0, 1, 3, 0, true);
    }

    public static MaplePacket getMiniGameOwnerForfeit(MapleMiniGame game) {
        return getMiniGameResult(game, 0, 1, 0, 2, 1, true);
    }

    public static MaplePacket getMiniGameVisitorForfeit(MapleMiniGame game) {
        return getMiniGameResult(game, 1, 0, 0, 1, 1, true);
    }

    public static MaplePacket getMiniGameClose(byte number) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0xA);
        mplew.write(1);
        mplew.write(3);
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCard(MapleClient c, MapleMiniGame minigame, boolean owner, int piece) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("05 02 02"));
        mplew.write(owner ? 0 : 1);
        mplew.write(0);
        addCharLook(mplew, minigame.getOwner(), false);
        mplew.writeMapleAsciiString(minigame.getOwner().getName());
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            mplew.write(1);
            addCharLook(mplew, visitor, false);
            mplew.writeMapleAsciiString(visitor.getName());
        }
        mplew.write(0xFF);
        mplew.write(0);
        mplew.writeInt(2);
        mplew.writeInt(minigame.getOwner().getMiniGamePoints("wins", false));
        mplew.writeInt(minigame.getOwner().getMiniGamePoints("ties", false));
        mplew.writeInt(minigame.getOwner().getMiniGamePoints("losses", false));
        mplew.writeInt(2000);
        if (minigame.getVisitor() != null) {
            MapleCharacter visitor = minigame.getVisitor();
            mplew.write(1);
            mplew.writeInt(2);
            mplew.writeInt(visitor.getMiniGamePoints("wins", false));
            mplew.writeInt(visitor.getMiniGamePoints("ties", false));
            mplew.writeInt(visitor.getMiniGamePoints("losses", false));
            mplew.writeInt(2000);
        }
        mplew.write(0xFF);
        mplew.writeMapleAsciiString(minigame.getDescription());
        mplew.write(piece);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCardStart(MapleMiniGame game, int loser) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("37 0" + loser));
        mplew.write(HexTool.getByteArrayFromHexString("0C"));
        int last = 13;
        if (game.getMatchesToWin() > 10) {
            last = 31;
        } else if (game.getMatchesToWin() > 6) {
            last = 21;
        }
        for (int i = 1; i < last; i++) {
            mplew.writeInt(game.getCardId(i));
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCardNewVisitor(MapleCharacter c, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("04 0" + slot));
        addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        mplew.writeInt(1);
        mplew.writeInt(c.getMiniGamePoints("wins", false));
        mplew.writeInt(c.getMiniGamePoints("ties", false));
        mplew.writeInt(c.getMiniGamePoints("losses", false));
        mplew.writeInt(2000);
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCardSelect(MapleMiniGame game, int turn, int slot, int firstslot, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("3E 0" + turn));
        if (turn == 1) {
            mplew.write(slot);
        } else if (turn == 0) {
            mplew.write(slot);
            mplew.write(firstslot);
            mplew.write(type);
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCardOwnerWin(MapleMiniGame game) {
        return getMiniGameResult(game, 1, 0, 0, 1, 0, false);
    }

    public static MaplePacket getMatchCardVisitorWin(MapleMiniGame game) {
        return getMiniGameResult(game, 0, 1, 0, 2, 0, false);
    }

    public static MaplePacket getMatchCardTie(MapleMiniGame game) {
        return getMiniGameResult(game, 0, 0, 1, 3, 0, false);
    }

    public static MaplePacket getHiredMerchant(MapleClient c, MapleMiniGame minigame, boolean owner, int piece) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("05 05 04 00 00 71 C0 4C 00"));
        mplew.writeMapleAsciiString("Hired Merchant");
        mplew.write(0xFF);
        mplew.write(0);
        mplew.write(0);
        mplew.writeMapleAsciiString(c.getPlayer().getName());
        mplew.write(HexTool.getByteArrayFromHexString("F2 C6 D1 93 01 00 00 00 00 00 00 00 00 00"));
        mplew.writeMapleAsciiString("Hired Merchant");
        mplew.write(HexTool.getByteArrayFromHexString("10 00 00 00 00 00"));
        return mplew.getPacket();
    }

    public static MaplePacket addOmokBox(MapleCharacter c, int ammount, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        addAnnounceBox(mplew, c.getMiniGame(), 1, 0, ammount, type);
        return mplew.getPacket();
    }

    public static MaplePacket removeOmokBox(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket addMatchCardBox(MapleCharacter c, int ammount, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        addAnnounceBox(mplew, c.getMiniGame(), 2, 0, ammount, type);
        return mplew.getPacket();
    }

    public static MaplePacket removeMatchcardBox(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket getPlayerShopChat(MapleCharacter c, String chat, byte slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("06 08"));
        mplew.write(slot);
        mplew.writeMapleAsciiString(c.getName() + " : " + chat);
        return mplew.getPacket();
    }

    public static MaplePacket getTradeChat(MapleCharacter c, String chat, boolean owner) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("0E 0F"));
        mplew.write(owner ? 0 : 1);
        mplew.writeMapleAsciiString(c.getName() + " : " + chat);
        return mplew.getPacket();
    }

    public static MaplePacket hiredMerchantBox() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.HIRED_MERCHANT_BOX.getValue());
        mplew.write(0x07);
        return mplew.getPacket();
    }

    public static MaplePacket getHiredMerchant(MapleCharacter chr, HiredMerchant hm, boolean firstTime) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue()); // header.
        mplew.write(HexTool.getByteArrayFromHexString("05 05 04"));
        mplew.write(hm.isOwner(chr) ? 0 : 1);
        mplew.write(0);
        mplew.writeInt(hm.getItemId());
        mplew.writeMapleAsciiString("Hired Merchant");
        for (int i = 0; i < 3; i++) {
            if (hm.getVisitors()[i] != null) {
                mplew.write(i + 1);
                addCharLook(mplew, hm.getVisitors()[i], false);
                mplew.writeMapleAsciiString(hm.getVisitors()[i].getName());
                mplew.writeShort(0x6E);
            }
        }
        mplew.write(0xFF);
        mplew.writeShort(0); // number of chats there structure: for (int i = 0; i < num; i++) asciistring, byte slot
        mplew.writeMapleAsciiString(hm.getOwner());
        if (hm.isOwner(chr)) {
            mplew.writeInt(chr.getId());
            mplew.write(firstTime ? 1 : 0);
            mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00 00 00 00 00"));
        }
        
        mplew.writeMapleAsciiString(hm.getDescription());
        mplew.write(0x10);
        mplew.writeInt(0);
        mplew.write(hm.getItems().size());
        if (hm.getItems().size() == 0) {
            mplew.write(0);
        } else {
            for (MaplePlayerShopItem item : hm.getItems()) {
                mplew.writeShort(item.getBundles());
                mplew.writeShort(item.getPerBundles());
                mplew.writeInt(item.getPrice());
                addItemInfo(mplew, item.getItem(), true, true);
            }
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateHiredMerchant(HiredMerchant hm) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x19);
        mplew.writeInt(0);
        mplew.write(hm.getItems().size());
        for (MaplePlayerShopItem item : hm.getItems()) {
            mplew.writeShort(item.getBundles());
            mplew.writeShort(item.getPerBundles());
            mplew.writeInt(item.getPrice());
            addItemInfo(mplew, item.getItem(), true, true, false, true);
        }
        return mplew.getPacket();
    }

    public static MaplePacket hiredMerchantChat(String message, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
        mplew.write(8);
        mplew.write(slot); // slot
        mplew.writeMapleAsciiString(message);
        return mplew.getPacket();
    }

    public static MaplePacket hiredMerchantVisitorLeave(int slot, boolean owner) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        if (!owner) {
            mplew.write(slot);
        }
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket hiredMerchantOwnerLeave() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x2A);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket hiredMerchantMaintenanceMessage() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
        mplew.write(0x00);
        mplew.write(0x12);
        return mplew.getPacket();
    }

    public static MaplePacket hiredMerchantVisitorAdd(MapleCharacter chr, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
        mplew.write(slot);
        addCharLook(mplew, chr, false);
        mplew.writeMapleAsciiString(chr.getName());
        mplew.writeShort(0x6E);
        return mplew.getPacket();
    }

    public static MaplePacket spawnHiredMerchant(HiredMerchant hm) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SPAWN_HIRED_MERCHANT.getValue());
        mplew.writeInt(hm.getOwnerId());
        mplew.writeInt(hm.getItemId());
        mplew.writeShort((short) hm.getPosition().getX());
        mplew.writeShort((short) hm.getPosition().getY());
        mplew.writeShort(0);
        mplew.writeMapleAsciiString(hm.getOwner());
        mplew.write(0x05);
        mplew.writeInt(hm.getObjectId());
        mplew.writeMapleAsciiString(hm.getDescription());
        mplew.write(hm.getItemId() % 10);
        mplew.write(HexTool.getByteArrayFromHexString("00 04"));
        return mplew.getPacket();
    }

    public static MaplePacket destroyHiredMerchant(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.DESTROY_HIRED_MERCHANT.getValue());
        mplew.writeInt(id);
        return mplew.getPacket();
    }

    public static MaplePacket leaveHiredMerchant(int slot, int status2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
        mplew.write(slot);
        mplew.write(status2);
        return mplew.getPacket();
    }

        public static MaplePacket hiredMerchantForceLeave2() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x0C);
        mplew.write(0);
        mplew.write(0x10);
        return mplew.getPacket();
    }

    public static MaplePacket hiredMerchantForceLeave1() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x0B);
        mplew.write(0x01);
        mplew.write(0x0D);
        return mplew.getPacket();
    }

    public static MaplePacket spawnPlayerNPC(PlayerNPCs npc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        mplew.write(1);
        mplew.writeInt(npc.getObjectId());
        mplew.writeInt(npc.getId());
        mplew.writeShort(npc.getPosition().x);
        mplew.writeShort(npc.getCY());
        mplew.write(1);
        mplew.writeShort(npc.getFH());
        mplew.writeShort(npc.getRX0());
        mplew.writeShort(npc.getRX1());
        mplew.write(1);
        return mplew.getPacket();
    }

    public static MaplePacket sendYellowTip(String tip) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.YELLOW_TIP.getValue());
        mplew.write(0xFF);
        mplew.writeMapleAsciiString(tip);
        mplew.writeMapleAsciiString("");
        return mplew.getPacket();
    }

    public static MaplePacket giveSpeedInfusion(int buffid, int bufflength, List<Pair<MapleBuffStat, Integer>> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        writeLongMask(mplew, statups);
        mplew.writeShort(0);
        /*mplew.write(HexTool.getByteArrayFromHexString("00 00 FE FF FF FF"));
        mplew.writeInt(buffid);
        mplew.write0(10);
        mplew.writeShort(bufflength);*/
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            mplew.writeShort(statup.getRight().shortValue());
            mplew.writeShort(-1);
            mplew.writeInt(buffid);
            mplew.write0(10);
            mplew.writeShort(bufflength);
        }
        mplew.write(0x58);
        mplew.write(0x02);
        return mplew.getPacket();
    }

    public static MaplePacket givePirateBuff(int buffid, int bufflength, List<Pair<MapleBuffStat, Integer>> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        writeLongMask(mplew, statups);
        mplew.writeShort(0);
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            mplew.writeShort(statup.getRight().shortValue());
            mplew.writeShort(0);
            mplew.writeInt(buffid);
            mplew.write0(5);
            mplew.writeShort(bufflength);
        }
        mplew.write0(3);
        return mplew.getPacket();
    }

    public static MaplePacket showPirateBuff(int cid, int skillid, int time, List<Pair<MapleBuffStat, Integer>> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        long mask = getLongMask(statups);
        mplew.writeLong(mask);
        mplew.writeLong(0);
        mplew.writeShort(0);
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            mplew.writeShort(statup.getRight());
            mplew.writeShort(0);
            mplew.writeInt(skillid);
            mplew.writeInt(0);
            mplew.write(0);
            mplew.writeShort(time);
        }
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket showSpeedInfusion(int cid, int skillid, int time, List<Pair<MapleBuffStat, Integer>> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        long mask = getLongMask(statups);
        mplew.writeLong(mask);
        mplew.writeLong(0);
        mplew.writeShort(0);
        mplew.writeInt(statups.get(0).getRight());
        mplew.writeInt(skillid);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.writeShort(time);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket sendMTS(List<MTSItemInfo> items, int tab, int type, int page, int pages) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(0x15); //operation
        mplew.writeInt(pages * 16); //testing, change to 10 if fails
        mplew.writeInt(items.size()); //number of items
        mplew.writeInt(tab);
        mplew.writeInt(type);
        mplew.writeInt(page);
        mplew.write(1);
        mplew.write(1);
        for (int i = 0; i < items.size(); i++) {
            MTSItemInfo item = items.get(i);
            addItemInfo(mplew, item.getItem(), true, true);
            mplew.writeInt(item.getID()); //id
            mplew.writeInt(item.getTaxes()); //this + below = price
            mplew.writeInt(item.getPrice()); //price
            mplew.writeLong(0);
            mplew.writeInt(getQuestTimestamp(item.getEndingDate()));
            mplew.writeMapleAsciiString(item.getSeller()); //account name (what was nexon thinking?)
            mplew.writeMapleAsciiString(item.getSeller()); //char name
            for (int j = 0; j < 28; j++) {
                mplew.write(0);
            }
        }
        mplew.write(1);
        return mplew.getPacket();
    }

    public static MaplePacket showNotes(ResultSet notes, int count) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_NOTES.getValue());
        mplew.write(2);
        mplew.write(count);
        for (int i = 0; i < count; i++) {
            mplew.writeInt(notes.getInt("id"));
            mplew.writeMapleAsciiString(notes.getString("from"));
            mplew.writeMapleAsciiString(notes.getString("message"));
            mplew.writeLong(getKoreanTimestamp(notes.getLong("timestamp")));
            mplew.write(0);
            notes.next();
        }
        return mplew.getPacket();
    }

    public static MaplePacket useChalkboard(MapleCharacter chr, boolean close) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CHALKBOARD.getValue());
        mplew.writeInt(chr.getId());
        if (close) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeMapleAsciiString(chr.getChalkboard());
        }
        return mplew.getPacket();
    }

    public static MaplePacket refreshTeleportRockMapList(MapleCharacter chr, byte type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(44);
        mplew.writeShort(SendPacketOpcode.TROCK_LOCATIONS.getValue());
        mplew.write(3);
        mplew.write(type);
        List<Integer> maps = chr.getTeleportRockMaps(type);
        int limit = 5;
        if (type == 1) {
            limit = 10;
        }
        for (int map : maps) {
            mplew.writeInt(map);
        }
        for (int i = maps.size(); i < limit; i++) {
            mplew.writeInt(999999999);
        }
        return mplew.getPacket();
    }

    /*
     * AE 01 
     43
     7C 9F 98 00
     00 00 00 00
     00 00 00 00
     00 00 00 00
     00 00 00 00
     00 00 00 00
     00 00 00 00
     00 00 00 00
     00 00 00 00
     00 00 00 00
     */
    public static MaplePacket sendWishList(MapleCharacter mc, boolean update) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(43);
        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        if (update) {
            mplew.write(0x45);
        } else {
            mplew.write(0x43);
        }
        byte i = 10;
        for (int sn : mc.getWishList()) {
            mplew.writeInt(sn);
            i--;
        }
        for (byte j = 0; j < i; j++) {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static MaplePacket showMTSCash(MapleCharacter p) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION2.getValue());
        mplew.writeInt(p.getCSPoints(4));
        mplew.writeInt(p.getCSPoints(2));
        return mplew.getPacket();
    }

    public static MaplePacket MTSWantedListingOver(int nx, int items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(0x3D);
        mplew.writeInt(nx);
        mplew.writeInt(items);
        return mplew.getPacket();
    }

    public static MaplePacket MTSConfirmSell() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(0x1D);
        return mplew.getPacket();
    }

    public static MaplePacket MTSConfirmBuy() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(0x33);
        return mplew.getPacket();
    }

    public static MaplePacket MTSFailBuy() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(0x34);
        mplew.write(0x42);
        return mplew.getPacket();
    }

    public static MaplePacket MTSConfirmTransfer(int quantity, int pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(9);
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(0x27);
        mplew.writeInt(quantity);
        mplew.writeInt(pos);
        return mplew.getPacket();
    }

    public static MaplePacket notYetSoldInv(List<MTSItemInfo> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(0x23);
        mplew.writeInt(items.size());
        if (items.size() != 0) {
            for (MTSItemInfo item : items) {
                addItemInfo(mplew, item.getItem(), true, true);
                mplew.writeInt(item.getID()); //id
                mplew.writeInt(item.getTaxes()); //this + below = price
                mplew.writeInt(item.getPrice()); //price
                mplew.writeLong(0);
                mplew.writeInt(getQuestTimestamp(item.getEndingDate()));
                mplew.writeMapleAsciiString(item.getSeller()); //account name (what was nexon thinking?)
                mplew.writeMapleAsciiString(item.getSeller()); //char name
                for (int i = 0; i < 28; i++) {
                    mplew.write(0);
                }
            }
        } else {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static MaplePacket transferInventory(List<MTSItemInfo> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MTS_OPERATION.getValue());
        mplew.write(0x21);
        mplew.writeInt(items.size());
        if (items.size() != 0) {
            for (MTSItemInfo item : items) {
                addItemInfo(mplew, item.getItem(), true, true);
                mplew.writeInt(item.getID()); //id
                mplew.writeInt(item.getTaxes()); //taxes
                mplew.writeInt(item.getPrice()); //price
                mplew.writeLong(0);
                mplew.writeInt(getQuestTimestamp(item.getEndingDate()));
                mplew.writeMapleAsciiString(item.getSeller()); //account name (what was nexon thinking?)
                mplew.writeMapleAsciiString(item.getSeller()); //char name
                for (int i = 0; i < 28; i++) {
                    mplew.write(0);
                }
            }
        }
        mplew.write(0xD0 + items.size());
        mplew.write(HexTool.getByteArrayFromHexString("FF FF FF 00"));
        return mplew.getPacket();
    }

    //AD 01 B4 00 00 00 00 00 00 00
    public static MaplePacket showNXMapleTokens(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CS_UPDATE.getValue());
        mplew.writeInt(chr.getCSPoints(1));
        mplew.writeInt(chr.getCSPoints(2));
        return mplew.getPacket();
    }

    public static MaplePacket showBoughtCSItem(MapleClient c, CashItemInfo item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x63);
        addCashItemInformation(mplew, item, c.getAccID());
        return mplew.getPacket();
    }

    public static MaplePacket showBoughtCashPackage(List<CashItemInfo> cashPackage, int accountId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x89);
        mplew.write(cashPackage.size());
        for (CashItemInfo item : cashPackage) {
            addCashItemInformation(mplew, item, accountId);
        }
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    /*
        public static MaplePacket showCashInventory(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());

        mplew.write(0x4B);
        mplew.writeShort(c.getPlayer().getCashShop().getInventory().size());

        for (IItem item : c.getPlayer().getCashShop().getInventory())
            addCashItemInformation(mplew, item, c.getAccID());

        mplew.writeShort(c.getPlayer().getStorage().getSlots());
        mplew.writeShort(c.getCharacterSlots());

        return mplew.getPacket();
    }
        */

            public static MaplePacket showCashInventoryDummy(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());

        mplew.write(0x4B);
        mplew.writeShort(0); //todo: cash inv here

        mplew.writeShort(c.getPlayer().getStorage().getSlots());
        mplew.writeShort(c.getCharacterSlots());

        return mplew.getPacket();
    }

    public static void addCashItemInformation(MaplePacketLittleEndianWriter mplew, CashItemInfo item, int accountId) {
        mplew.writeLong(1337);//unique id
        mplew.writeInt(accountId);
        mplew.writeInt(0);
        mplew.writeInt(item.getId());
        mplew.writeInt(item.getSn());
        mplew.writeShort(item.getCount());
        mplew.write0(14);//unintelligible bullshit here in the real cap so if this fails we might have to echo it, who knows
        mplew.write(ITEM_MAGIC); //new in v88
        addExpirationTime(mplew, 0, false); //new in v88
        mplew.writeLong(0x1E); //write0(8) would probably work but best to be safe eh
        //mplew.write(HexTool.getByteArrayFromHexString("40 F0 81 DF 08 2E 0A CB 01"));
        //mplew.write0(8);
    }

    public static MaplePacket wrongCouponCode() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x5C);
        mplew.write(0x9F);
        return mplew.getPacket();
    }

   public static MaplePacket sendCashShopMessage(int error) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x5C);
        mplew.write(error);
        return mplew.getPacket();
    }

    public static MaplePacket showCouponRedeemedItem(int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.writeShort(0x59); //guess, 49 in v72
        mplew.writeInt(0);
        mplew.writeInt(1);
        mplew.writeShort(1);
        mplew.writeShort(0x1A);
        mplew.writeInt(itemid);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket enableCSUse0() {
        /*
         * 0A 00 01 00 00 00 00
         */
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(0x0A);
        mplew.write(1);
        mplew.write0(4);
        return mplew.getPacket();
    }

    /*
     * AE 01
     3E
     01
     00 00 00 00
     */
    public static MaplePacket enableCSUse1() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(9);
        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x3E);
        mplew.write(1);
        mplew.write0(4);
        return mplew.getPacket();
    }

    /*
     * AE 01
     3F
     04 00
     36 1E AD 01 00 00 00 00 4D D7 71 01 00 00 00 00 68 EB 4C 00 AB 11 8B 05 2D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 80 05 BB 46 E6 17 02 00 00 00 00 00 00 00 00 94 C6 40 0F 76 86 04 00 00 02 37 1E AD 01 00 00 00 00 4D D7 71 01 00 00 00 00 89 39 4D 00 AE 11 8B 05 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 80 05 BB 46 E6 17 02 00 00 00 00 00 00 00 00 56 C6 40 0F 79 86 04 00 00 02 38 1E AD 01 00 00 00 00 4D D7 71 01 00 00 00 00 63 80 1B 00 AD 11 8B 05 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 80 05 BB 46 E6 17 02 00 00 00 00 00 00 00 00 CF C5 40 0F 78 86 04 00 00 02 39 1E AD 01 00 00 00 00 4D D7 71 01 00 00 00 00 0F 4C 4C 00 AC 11 8B 05 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 80 05 BB 46 E6 17 02 00 00 00 00 00 00 00 00 2A C5 40 0F 77 86 04 00 00 02 02 00 00 00 01 63 80 1B 00 01 38 1E AD 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF 07 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF 03 0F 4C 4C 00 01 39 1E AD 01 00 00 00 00 00 80 05 BB 46 E6 17 02 FF FF FF FF C0 A3 BD C3 C4 DA B1 E2 00 00 00 00 00 01 00 00 64 00 3A 92 32 B8 C5 CC 01 00 00 81 00 00 00 00 00 00 00 00 00 00 00 00 04 00 04 00 00 00 00 00
     */
    public static MaplePacket enableCSUse2() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.writeShort(0x3F); //v88
        mplew.writeShort(4);
        mplew.writeInt(3);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket enableCSUse3() {
        //AE 01 41 00 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(43);
        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x41); //v88
        mplew.writeShort(0);
        return mplew.getPacket();
    }
    
    public static MaplePacket enableCSUse4() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(43);
        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.write(0x90); //v88
        mplew.write0(4);
        mplew.write(1);
        return mplew.getPacket();
    }

    /**
     * 
     * @param target
     * @param mapid: Map id of player, -1 if mts or cs, channel - 1 if channel
     * @param MTSmapCSchannel
     * 0: MTS
     * 1: Map
     * 2: CS
     * 3: Different Channel
     * @return
     */
    public static MaplePacket getFindReply(String target, int mapid, int MTSmapCSchannel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(9);
        mplew.writeMapleAsciiString(target);
        mplew.write(MTSmapCSchannel); // 0: mts 1: map 2: cs
        mplew.writeInt(mapid); // -1 if mts, cs
        if (MTSmapCSchannel == 1) {
            mplew.write0(8);
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendAutoHpPot(int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendPacketOpcode.AUTO_HP_POT.getValue());
        mplew.writeInt(itemId);
        return mplew.getPacket();
    }

    public static MaplePacket sendAutoMpPot(int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendPacketOpcode.AUTO_MP_POT.getValue());
        mplew.writeInt(itemId);
        return mplew.getPacket();
    }

    public static MaplePacket showOXQuiz(int questionSet, int questionId, boolean askQuestion) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendPacketOpcode.OX_QUIZ.getValue());
        mplew.write(askQuestion ? 1 : 0);
        mplew.write(questionSet);
        mplew.writeShort(questionId);
        return mplew.getPacket();
    }

    public static MaplePacket updateGender(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendPacketOpcode.GENDER.getValue());
        mplew.write(chr.getGender());
        return mplew.getPacket();
    }

    public static MaplePacket enableReport() { // by snow
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendPacketOpcode.ENABLE_REPORT.getValue());
        mplew.write(1);
        return mplew.getPacket();
    }

    public static MaplePacket giveFinalAttack(int skillid, int time) {//packets found by lailainoob
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeLong(0);
        mplew.writeShort(0);
        mplew.write(0);//some 80 and 0 bs
        mplew.write(0x80);//let's just do 80, then 0
        mplew.writeInt(0);
        mplew.writeShort(1);
        mplew.writeInt(skillid);
        mplew.writeInt(time);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket loadFamily(MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.LOAD_FAMILY.getValue());
        mplew.writeInt(7); // Number of events
        mplew.write(0);
        mplew.writeInt(300); // REP needed
        mplew.writeInt(1); // Number of times allowed per day
        mplew.writeMapleAsciiString("패밀리원에게 바로 이동");
        mplew.writeMapleAsciiString("[대상] 자기 자신\n[효과] 원하는 패밀리원이 있는 장소로 바로 이동한다.");
        mplew.write(1);
        mplew.writeInt(500); // REP needed
        mplew.writeInt(1); // Number of times allowed per day
        mplew.writeMapleAsciiString("패밀리원 바로 소환");
        mplew.writeMapleAsciiString("[대상] 패밀리원 1명\n[효과] 원하는 패밀리원을 자신이 있는 맵으로 바로 소환한다.");
        mplew.write(2);
        mplew.writeInt(700); // REP needed
        mplew.writeInt(1); // Number of times allowed per day
        mplew.writeMapleAsciiString("나만의 드롭률 1.2배(15분)");
        mplew.writeMapleAsciiString("[대상] 자기 자신\n[지속시간] 15분\n[효과] 몬스터 사냥 드롭률이 #c1.2배#로 향상.\n※ 나만의 경험치 효과나 드롭률 이벤트와 겹칠 경우 효력이 무시된다.");
        mplew.write(3);
        mplew.writeInt(900); // REP needed
        mplew.writeInt(1); // Number of times allowed per day
        mplew.writeMapleAsciiString("나만의 경험치 1.2배(15분)");
        mplew.writeMapleAsciiString("[대상] 자기 자신\n[지속시간] 15분\n[효과] 몬스터 사냥 시 얻는 경험치가 #c1.2배#로 향상.\n※ 나만의 드롭률 효과나 경험치 이벤트와 겹칠 경우 효력이 무시된다.");
        mplew.write(2);
        mplew.writeInt(1500); // REP needed
        mplew.writeInt(1); // Number of times allowed per day
        mplew.writeMapleAsciiString("나만의 드롭률 1.2배(30분)");
        mplew.writeMapleAsciiString("[대상] 자기 자신\n[지속시간] 30분\n[효과] 몬스터 사냥 드롭률이 #c1.2배#로 향상.\n※ 나만의 경험치 효과나 드롭률 이벤트와 겹칠 경우 효력이 무시된다.");
        mplew.write(3);
        mplew.writeInt(2000); // REP needed
        mplew.writeInt(1); // Number of times allowed per day
        mplew.writeMapleAsciiString("나만의 경험치 1.2배(30분)");
        mplew.writeMapleAsciiString("[대상] 자기 자신\n[지속시간] 30분\n[효과] 몬스터 사냥 시 얻는 경험치가 #c1.2배#로 향상.\n※ 나만의 드롭률 효과나 경험치 이벤트와 겹칠 경우 효력이 무시된다.");
        mplew.write(4);
        mplew.writeInt(3000); // REP needed
        mplew.writeInt(1); // Number of times allowed per day
        mplew.writeMapleAsciiString("패밀리원의 단결(30분)");
        mplew.writeMapleAsciiString("[발동조건] 가계도에 보이는 하위 패밀리원이 6명 이상 로그인\n[지속시간] 30분\n[효과] 드롭률과 경험치를 #c1.5배#로 향상. ※ 나만의 드롭률, 나만의 경험치 효과나 다른 경험치, 드롭률 이벤트와 겹칠 경우 효력이 무시된다.");
        return mplew.getPacket();
    }

    public static MaplePacket sendFamilyMessage() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendPacketOpcode.FAMILY_MESSAGE.getValue());
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket getFamilyInfo(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.OPEN_FAMILY.getValue()); // who cares what header is
        mplew.writeInt(chr.getFamily().getReputation()); // cur rep left
        mplew.writeInt(chr.getFamily().getTotalReputation()); // tot rep left
        mplew.writeInt(chr.getFamily().getTodaysRep()); // todays rep
        mplew.writeShort(chr.getFamily().getJuniors().length); // juniors added
        mplew.writeShort(chr.getFamily().getTotalJuniors()); // juniors allowed
        mplew.writeShort(0);
        mplew.writeInt(chr.getFamilyId()); // id?
        mplew.writeMapleAsciiString(chr.getFamily().getFamilyName());
        mplew.writeInt(0);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket getStatusMsg(int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(7);
        mplew.writeInt(itemid);
        return mplew.getPacket();
    }

    public static MaplePacket giveEnergyCharge(int barammount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeInt(0);
        mplew.writeLong(MapleBuffStat.ENERGY_CHARGE.getValue()); //energy charge buffstat
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeShort(barammount); // 0=no bar, 10000=full bar
        mplew.writeShort(0);
        mplew.writeLong(0);
        mplew.write(0);
        mplew.writeInt(50);
        return mplew.getPacket();
    }

    public static MaplePacket giveForeignEnergyCharge(int cid, int barammount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(0);
        mplew.writeLong(MapleBuffStat.ENERGY_CHARGE.getValue()); //energy charge buffstat
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeShort(barammount); // 0=no bar, 10000=full bar
        mplew.writeShort(0);
        mplew.writeLong(0);
        mplew.write(0);
        mplew.writeInt(50);
        return mplew.getPacket();
    }

    public static MaplePacket addCard(boolean full, int cardid, int level) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
        mplew.writeShort(SendPacketOpcode.MONSTERBOOK_ADD.getValue());
        mplew.write(full ? 0 : 1);
        mplew.writeInt(cardid);
        mplew.writeInt(level);
        return mplew.getPacket();
    }

    public static MaplePacket showGainCard() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(0x0D);
        return mplew.getPacket();
    }

    public static MaplePacket showForeginCardEffect(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(id);
        mplew.write(0x0D);
        return mplew.getPacket();
    }

    public static MaplePacket changeCover(int cardid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendPacketOpcode.MONSTER_BOOK_CHANGE_COVER.getValue());
        mplew.writeInt(cardid);
        return mplew.getPacket();
    }

    public static MaplePacket showCygnusIntro(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(0x12);
        mplew.writeMapleAsciiString("Effect/Direction.img/cygnus/Scene" + id);
        return mplew.getPacket();
    }

    public static MaplePacket cygnusIntroLock(boolean enable) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendPacketOpcode.CYGNUS_INTRO_LOCK.getValue());
        mplew.write(enable ? 1 : 0);
        return mplew.getPacket();
    }

    public static MaplePacket cygnusIntroDisableUI(boolean enable) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendPacketOpcode.CYGNUS_INTRO_DISABLE_UI.getValue());
        mplew.write(enable ? 1 : 0);
        return mplew.getPacket();
    }

    public static MaplePacket cygnusCharCreate() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
        mplew.writeShort(SendPacketOpcode.CREATE_CYGNUS.getValue());
        return mplew.getPacket();
    }

    public static MaplePacket cygnusCharacterCreated(int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendPacketOpcode.CYGNUS_CHAR_CREATED.getValue());
        mplew.writeInt(mode);
        return mplew.getPacket();
    }

    public static MaplePacket itemMegaphone(String msg, boolean whisper, int channel, IItem item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(8);
        mplew.writeMapleAsciiString(msg);
        mplew.write(channel - 1);
        mplew.write(whisper ? 1 : 0);
        if (item == null) {
            mplew.write(0);
        } else {
            addItemInfo(mplew, item, false, false, false, true);
        }
        return mplew.getPacket();
    }
    
    public static MaplePacket removeNPC(int objid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.REMOVE_NPC.getValue());
        mplew.writeInt(objid);
        return mplew.getPacket();
    }

    public static MaplePacket reportResponse(byte mode, int remainingReports) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.REPORT_RESPONSE.getValue());
        mplew.write(mode);
        if (mode == 2) {
            mplew.write(1); //does this change?
            mplew.writeInt(remainingReports);
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendHammerData(int hammerUsed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.VICIOUS_HAMMER.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("34 00 00 00 00"));
        mplew.writeInt(hammerUsed);
        return mplew.getPacket();
    }

    public static MaplePacket sendHammerMessage() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.VICIOUS_HAMMER.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("38 00 00 00 00"));
        return mplew.getPacket();
    }

    public static MaplePacket hammerItem(IItem item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(0); // could be from drop
        mplew.write(2); // always 2
        mplew.write(3); // quantity > 0 (?)
        mplew.write(1); // Inventory type
        mplew.write(item.getPosition()); // item slot
        mplew.writeShort(0);
        mplew.write(1);
        mplew.write(item.getPosition()); // wtf repeat
        addItemInfo(mplew, item, true, false);
        return mplew.getPacket();
    }

    public static MaplePacket playPortalSound() {
        return showSpecialEffect(7);
    }

    public static MaplePacket showMonsterBookPickup() {
        return showSpecialEffect(14);
    }

    public static MaplePacket showEquipmentLevelUp() {
        return showSpecialEffect(17);
    }

    public static MaplePacket showItemLevelup() {
        return showSpecialEffect(17);
    }

    /**
     * 7 = Enter portal sound
     * 8 = Job change
     * 9 = Quest complete
     * 14 = Monster book pickup
     * 16 = ??
     * 17 = Equipment levelup
     * 19 = Exp card [500, 200, 50]
     * 21++ = no effect*/
    public static MaplePacket showSpecialEffect(int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(effect);
        return mplew.getPacket();
    }

    public static MaplePacket startQuest(short quest) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(quest);
        mplew.write(1);
        mplew.writeShort(0);
        mplew.writeLong(0);
        return mplew.getPacket();
    }

    public static MaplePacket updateQuestFinish(short quest, int npc, short nextquest) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(8);
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeShort(nextquest);
        return mplew.getPacket();
    }

    public static MaplePacket showQuestExpired(String expire) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(9);
        mplew.writeMapleAsciiString(expire);
        return mplew.getPacket();
    }

    public static MaplePacket questError(short quest) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(10);
        mplew.writeShort(quest);
        return mplew.getPacket();
    }

    public static MaplePacket getMultiMegaphone(String[] messages, int channel, boolean showEar) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(0x0A);
        if (messages[0] != null) {
            mplew.writeMapleAsciiString(messages[0]);
        }
        mplew.write(messages.length);
        for (int i = 1; i < messages.length; i++) {
            if (messages[i] != null) {
                mplew.writeMapleAsciiString(messages[i]);
            }
        }
        for (int i = 0; i < 10; i++) {
            mplew.write(channel - 1);
        }
        mplew.write(showEar ? 1 : 0);
        mplew.write(1);
        return mplew.getPacket();
    }

    /**
     * Gets a gm effect packet (ie. hide, banned, etc.)
     *
     * Possible values for <code>type</code>:<br>
     * 4: You have successfully blocked access.<br>
     * 5: The unblocking has been successful.<br>
     * 6 with Mode 0: You have successfully removed the name from the ranks.<br>
     * 6 with Mode 1: You have entered an invalid character name.<br>
     * 16: GM Hide, mode determines whether or not it is on.<br>
     * 29: Mode 0: Failed to send warning Mode 1: Sent warning<br>
     *
     * @param type The type
     * @param mode The mode
     * @return The gm effect packet
     */
    public static MaplePacket getGMEffect(int type, byte mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
        mplew.writeShort(SendPacketOpcode.GM_PACKET.getValue());
        mplew.write(type);
        mplew.write(mode);
        return mplew.getPacket();
    }

    public static MaplePacket sendFamilyInvite(int playerId, String inviter) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.FAMILY_INVITE.getValue());
        mplew.writeInt(playerId);
        mplew.writeMapleAsciiString(inviter);
        return mplew.getPacket();
    }

    public static MaplePacket showBoughtCSQuestItem(int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CS_OPERATION.getValue());
        mplew.writeInt(0x81);
        mplew.writeInt(1);
        mplew.writeShort(1);
        mplew.writeShort(1);
        mplew.writeInt(itemid);//D8 82 3D 00
        return mplew.getPacket();
    }

    public static MaplePacket updateEquipSlot(IItem item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(0);
        mplew.write(HexTool.getByteArrayFromHexString("02 03 01"));
        mplew.writeShort(item.getPosition());
        mplew.write(0);
        mplew.write(item.getType());
        mplew.writeShort(item.getPosition());
        addItemInfo(mplew, item, true, true);
        mplew.writeMapleAsciiString("");
        return mplew.getPacket();
    }

    private static void getGuildInfo(MaplePacketLittleEndianWriter mplew, MapleGuild guild) {
        mplew.writeInt(guild.getId());
        mplew.writeMapleAsciiString(guild.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(guild.getRankTitle(i));
        }
        Collection<MapleGuildCharacter> members = guild.getMembers();
        mplew.write(members.size());
        for (MapleGuildCharacter mgc : members) {
            mplew.writeInt(mgc.getId());
        }
        for (MapleGuildCharacter mgc : members) {
            mplew.writeAsciiString(getRightPaddedStr(mgc.getName(), '\0', 13));
            mplew.writeInt(mgc.getJobId());
            mplew.writeInt(mgc.getLevel());
            mplew.writeInt(mgc.getGuildRank());
            mplew.writeInt(mgc.isOnline() ? 1 : 0);
            mplew.writeInt(guild.getSignature());
            mplew.writeInt(mgc.getAllianceRank());
        }
        mplew.writeInt(guild.getCapacity());
        mplew.writeShort(guild.getLogoBG());
        mplew.write(guild.getLogoBGColor());
        mplew.writeShort(guild.getLogo());
        mplew.write(guild.getLogoColor());
        mplew.writeMapleAsciiString(guild.getNotice());
        mplew.writeInt(guild.getGP());
        mplew.writeInt(guild.getAllianceId());
    }

    public static MaplePacket getAllianceInfo(MapleAlliance alliance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x0C);
        mplew.write(1);
        mplew.writeInt(alliance.getId());
        mplew.writeMapleAsciiString(alliance.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(alliance.getRankTitle(i));
        }
        mplew.write(alliance.getGuilds().size());
        mplew.writeInt(2); // probably capacity
        for (Integer guild : alliance.getGuilds()) {
            mplew.writeInt(guild);
        }
        mplew.writeMapleAsciiString(alliance.getNotice());
        return mplew.getPacket();
    }

    public static MaplePacket makeNewAlliance(MapleAlliance alliance, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x0F);
        mplew.writeInt(alliance.getId());
        mplew.writeMapleAsciiString(alliance.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(alliance.getRankTitle(i));
        }
        mplew.write(alliance.getGuilds().size());
        for (Integer guild : alliance.getGuilds()) {
            mplew.writeInt(guild);
        }
        mplew.writeInt(2); // probably capacity
        mplew.writeShort(0);
        for (Integer guildd : alliance.getGuilds()) {
            try {
                getGuildInfo(mplew, c.getChannelServer().getWorldInterface().getGuild(guildd, c.getPlayer().getMGC()));
            } catch (RemoteException re) {
                c.getChannelServer().reconnectWorld();
            }
        }
        return mplew.getPacket();
    }

    public static MaplePacket getGuildAlliances(MapleAlliance alliance, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x0D);
        mplew.writeInt(alliance.getGuilds().size());
        for (Integer guild : alliance.getGuilds()) {
            try {
                getGuildInfo(mplew, c.getChannelServer().getWorldInterface().getGuild(guild, null));
            } catch (RemoteException re) {
                c.getChannelServer().reconnectWorld();
            }
        }
        return mplew.getPacket();
    }

    public static MaplePacket addGuildToAlliance(MapleAlliance alliance, int newGuild, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x12);
        mplew.writeInt(alliance.getId());
        mplew.writeMapleAsciiString(alliance.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(alliance.getRankTitle(i));
        }
        mplew.write(alliance.getGuilds().size());
        for (Integer guild : alliance.getGuilds()) {
            mplew.writeInt(guild);
        }
        mplew.writeInt(2);
        mplew.writeMapleAsciiString(alliance.getNotice());
        mplew.writeInt(newGuild);
        try {
            getGuildInfo(mplew, c.getChannelServer().getWorldInterface().getGuild(newGuild, null));
        } catch (RemoteException re) {
            c.getChannelServer().reconnectWorld();
        }
        return mplew.getPacket();
    }

    public static MaplePacket allianceMemberOnline(MapleCharacter mc, boolean online) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x0E);
        mplew.writeInt(mc.getGuild().getAllianceId());
        mplew.writeInt(mc.getGuildId());
        mplew.writeInt(mc.getId());
        mplew.write(online ? 1 : 0);
        return mplew.getPacket();
    }

    public static MaplePacket allianceNotice(int id, String notice) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x1C);
        mplew.writeInt(id);
        mplew.writeMapleAsciiString(notice);
        return mplew.getPacket();
    }

    public static MaplePacket changeAllianceRankTitle(int alliance, String[] ranks) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x1A);
        mplew.writeInt(alliance);
        for (int i = 0; i < 5; i++) {
            mplew.writeMapleAsciiString(ranks[i]);
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateAllianceJobLevel(MapleCharacter mc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x18);
        mplew.writeInt(mc.getGuild().getAllianceId());
        mplew.writeInt(mc.getGuildId());
        mplew.writeInt(mc.getId());
        mplew.writeInt(mc.getLevel());
        mplew.writeInt(mc.getJob().getId());
        return mplew.getPacket();
    }

    public static MaplePacket removeGuildFromAlliance(MapleAlliance alliance, int expelledGuild, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x10);
        mplew.writeInt(alliance.getId());
        mplew.writeMapleAsciiString(alliance.getName());
        for (int i = 1; i <= 5; i++) {
            mplew.writeMapleAsciiString(alliance.getRankTitle(i));
        }
        mplew.write(alliance.getGuilds().size());
        for (Integer guild : alliance.getGuilds()) {
            mplew.writeInt(guild);
        }
        mplew.writeInt(2);
        mplew.writeMapleAsciiString(alliance.getNotice());
        mplew.writeInt(expelledGuild);
        try {
            getGuildInfo(mplew, c.getChannelServer().getWorldInterface().getGuild(expelledGuild, null));
        } catch (RemoteException re) {
            c.getChannelServer().reconnectWorld();
        }
        mplew.write(0x01);
        return mplew.getPacket();
    }

    public static MaplePacket disbandAlliance(int alliance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(0x1D);
        mplew.writeInt(alliance);
        return mplew.getPacket();
    }

        public static MaplePacket addComboBuff(int combo) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue()); // actually it's the same as giveBuff
        mplew.writeLong(MapleBuffStat.ARAN_COMBO.getValue());
        mplew.writeLong(0);
        mplew.writeShort(combo);
        mplew.writeInt(21000000);
        mplew.writeInt(0x100);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket sendMesoLimit() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(0x36); //Players under level 15 can only trade 1m per day
        return mplew.getPacket();
    }

    public static MaplePacket sendEngagementRequest(String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.FAMILY_ACTION.getValue()); //<name> has requested engagement. Will you accept this proposal?
        mplew.write(0);
        mplew.writeMapleAsciiString(name); // name
        mplew.writeInt(10); // playerid
        return mplew.getPacket();
    }

    public static MaplePacket sendGroomWishlist() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.FAMILY_ACTION.getValue()); //<name> has requested engagement. Will you accept this proposal?
        mplew.write(9);
        return mplew.getPacket();
    }

    public static MaplePacket sendFamilyJoinResponse(boolean accepted, String added) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.FAMILY_MESSAGE2.getValue());
        mplew.write(accepted ? 1 : 0);
        mplew.writeMapleAsciiString(added);
        return mplew.getPacket();
    }

    public static MaplePacket getSeniorMessage(String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.FAMILY_SENIOR_MESSAGE.getValue());
        mplew.writeMapleAsciiString(name);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static MaplePacket sendGainRep(int gain, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.FAMILY_GAIN_REP.getValue());
        mplew.writeInt(gain);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket removeItemFromDuey(boolean remove, int Package) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.DUEY.getValue());
        mplew.write(0x17);
        mplew.writeInt(Package);
        mplew.write(remove ? 3 : 4);
        return mplew.getPacket();
    }

    public static MaplePacket sendDueyMSG(byte operation) {
        return sendDuey(operation, null);
    }

    public static MaplePacket sendDuey(byte operation, List<DueyPackages> packages) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.DUEY.getValue());
        mplew.write(operation);
        if (operation == 8) {
            mplew.write(0);
            mplew.write(packages.size());
            for (DueyPackages dp : packages) {
                mplew.writeInt(dp.getPackageId());
                mplew.writeAsciiString(dp.getSender());
                for (int i = dp.getSender().length(); i < 13; i++) {
                    mplew.write(0);
                }
                mplew.writeInt(dp.getMesos());
                mplew.writeLong(getQuestTimestamp(dp.sentTimeInMilliseconds()));
                mplew.writeLong(0); // Contains message o____o.
                for (int i = 0; i < 48; i++) {
                    mplew.writeInt(Randomizer.getInstance().nextInt(Integer.MAX_VALUE));
                }
                mplew.writeInt(0);
                mplew.write(0);
                if (dp.getItem() != null) {
                    mplew.write(1);
                    addItemInfo(mplew, dp.getItem(), true, true);
                } else {
                    mplew.write(0);
                }
            }
            mplew.write(0);
        }
        return mplew.getPacket();
    }

    public static MaplePacket giveHomingBeacon(int buffid, int moid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeLong(MapleBuffStat.HOMING_BEACON.getValue());
        mplew.writeLong(0);
        mplew.writeShort(0);
        mplew.writeInt(1);
        mplew.writeInt(buffid);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(moid);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket sendDojoAnimation(byte firstByte, String animation) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
        mplew.write(firstByte);
        mplew.writeMapleAsciiString(animation);
        return mplew.getPacket();
    }

    public static MaplePacket getDojoInfo(String info) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(10);
        mplew.write(HexTool.getByteArrayFromHexString("B7 04"));
        mplew.writeMapleAsciiString(info);
        return mplew.getPacket();
    }

    public static MaplePacket getDojoInfoMessage(String message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(9);
        mplew.writeMapleAsciiString(message);
        return mplew.getPacket();
    }

    /**
     * Gets a "block" packet (ie. the cash shop is unavailable, etc)
     *
     * Possible values for <code>type</code>:<br>
     * 1: You cannot move that channel. Please try again later.<br>
     * 2: You cannot go into the cash shop. Please try again later.<br>
     * 3: The Item-Trading shop is currently unavailable, please try again later.<br>
     * 4: You cannot go into the trade shop, due to the limitation of user count.<br>
     * 5: You do not meet the minimum level requirement to access the Trade Shop.<br>
     *
     * @param type The type
     * @return The "block" packet.
     */
    public static MaplePacket blockedMessage(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CS_BLOCKED.getValue());
        mplew.write(type);
        return mplew.getPacket();
    }

    /**
     *
     * @param type - (0:Light&Long 1:Heavy&Short)
     * @param delay - seconds
     * @return
     */
    public static MaplePacket trembleEffect(int type, int delay) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
        mplew.write(1);
        mplew.write(type);
        mplew.writeInt(delay);
        return mplew.getPacket();
    }

    public static MaplePacket getEnergy(int level) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ENERGY.getValue());
        mplew.writeMapleAsciiString("energy");
        mplew.writeMapleAsciiString(Integer.toString(level));
        return mplew.getPacket();
    }

    public static MaplePacket dojoWarpUp() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.DOJO_WARP_UP.getValue());
        mplew.write(0);
        mplew.write(6);
        return mplew.getPacket();
    }

    public static MaplePacket updateItemInSlot(IItem item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(0); // could be from drop
        mplew.write(2); // always 2
        mplew.write(3); // quantity > 0 (?)
        mplew.write(item.getType()); // inventory type
        mplew.write(item.getPosition()); // item slot
        mplew.writeShort(0);
        mplew.write(1);
        mplew.write(item.getPosition()); // wtf repeat
        addItemInfo(mplew, item, true, false);
        return mplew.getPacket();
    }

    public static MaplePacket itemExpired(int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(2);
        mplew.writeInt(itemid);
        return mplew.getPacket();
    }

    private static String getRightPaddedStr(String in, char padchar, int length) {
        StringBuilder builder = new StringBuilder(in);
        for (int x = in.getBytes().length; x < length; x++) {
            builder.append(padchar);
        }
        return builder.toString();
    }

    public static MaplePacket MobDamageMobFriendly(MapleMonster mob, int damage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
        mplew.writeInt(mob.getObjectId());
        mplew.write(1); // direction ?
        mplew.writeInt(damage);
        int remainingHp = mob.getHp() - damage;
        if (remainingHp <= 0) {
            remainingHp = 0;
            mob.getMap().removeMapObject(mob);
        }
        mob.setHp(remainingHp);
        mplew.writeInt(remainingHp);
        mplew.writeInt(mob.getMaxHp());
        return mplew.getPacket();
    }

    public static MaplePacket shopErrorMessage(int error, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x0A);
        mplew.write(type);
        mplew.write(error);
        return mplew.getPacket();
    }

    private static void addRingInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeShort(chr.getCrushRings().size());
        for (MapleRing ring : chr.getCrushRings()) {
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeAsciiString(getRightPaddedStr(ring.getPartnerName(), '\0', 13));
            mplew.writeInt(ring.getRingId());
            mplew.writeInt(0);
            mplew.writeInt(ring.getPartnerRingId());
            mplew.writeInt(0);
        }
        mplew.writeShort(chr.getFriendshipRings().size());
        for (MapleRing ring : chr.getFriendshipRings()) {
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeAsciiString(getRightPaddedStr(ring.getPartnerName(), '\0', 13));
            mplew.writeInt(ring.getRingId());
            mplew.writeInt(0);
            mplew.writeInt(ring.getPartnerRingId());
            mplew.writeInt(0);
            mplew.writeInt(ring.getItemId());
        }
        mplew.writeShort(chr.getMarriageRings().size());
        int marriageId = 30000;
        for (MapleRing ring : chr.getMarriageRings()) {
            mplew.writeInt(marriageId);
            mplew.writeInt(chr.getId());
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeShort(3);
            mplew.writeInt(ring.getRingId());
            mplew.writeInt(ring.getPartnerRingId());
            mplew.writeAsciiString(getRightPaddedStr(chr.getName(), '\0', 13));
            mplew.writeAsciiString(getRightPaddedStr(ring.getPartnerName(), '\0', 13));
            marriageId++;
        }
    }

    public static MaplePacket finishedSort(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.FINISH_SORT.getValue());
        mplew.write(0);
        mplew.write(type);
        return mplew.getPacket();
    }

    public static MaplePacket finishedSort2(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.FINISH_SORT2.getValue());
        mplew.write(0);
        mplew.write(type);
        return mplew.getPacket();
    }

    public static MaplePacket showHPQMoon() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.HPQ_MOON.getValue());
        mplew.writeInt(-1);
        return mplew.getPacket();
    }

    public static MaplePacket triggerMoon(int oid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.REACTOR_HIT.getValue());
        mplew.writeInt(oid);
        mplew.write(6);//state
        mplew.writeShort(-183);
        mplew.writeShort(-433);
        mplew.writeShort(0);
        mplew.write(-1);
        mplew.write(78);
        return mplew.getPacket();
    }

    private static void addTeleportRockRecord(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        List<Integer> maps = chr.getTeleportRockMaps(0);
        for (int map : maps) {
            mplew.writeInt(map);
        }
        for (int i = maps.size(); i < 5; i++) {
            mplew.writeInt(999999999);
        }
        maps = chr.getTeleportRockMaps(1);
        for (int map : maps) {
            mplew.writeInt(map);
        }
        for (int i = maps.size(); i < 10; i++) {
            mplew.writeInt(999999999);
        }
    }

    public static MaplePacket getRelayPacket(byte[] data)
    {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(data);
        return mplew.getPacket();
    }

    public static MaplePacket spawnEvanDragon(MapleDragon dragon, boolean owner)
    {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(19);
        mplew.writeShort(SendPacketOpcode.SPAWN_DRAGON.getValue());
        mplew.writeInt(dragon.getOwner().getId());
        mplew.writeShort(dragon.getPosition().x);
        mplew.writeShort(owner ? 0 : -1);
        mplew.writeShort(dragon.getPosition().y);
        mplew.writeShort(owner ? 0 : -1);
        mplew.write(owner ? 4 : 5);
        mplew.writeShort(0);
        mplew.writeShort(dragon.getOwner().getJob().getId());
        return mplew.getPacket();
    }

    //All Aran Handling should be below this comment
    public static MaplePacket displayCombo(int combo) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ARAN_COMBO.getValue());
        mplew.writeInt(combo);
        return mplew.getPacket();
    }
}