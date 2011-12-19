/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License zas
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
package client;

import constants.ExpTable;
import constants.skills.Bishop;
import constants.skills.BlazeWizard;
import constants.skills.Corsair;
import constants.skills.Crusader;
import constants.skills.DarkKnight;
import constants.skills.DawnWarrior;
import constants.skills.FPArchMage;
import constants.skills.GM;
import constants.skills.Hermit;
import constants.skills.ILArchMage;
import constants.skills.Magician;
import constants.skills.Marauder;
import constants.skills.NightWalker;
import constants.skills.Priest;
import constants.skills.Ranger;
import constants.skills.Sniper;
import constants.skills.Spearman;
import constants.skills.Swordsman;
import constants.skills.ThunderBreaker;
import constants.skills.WindArcher;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.Formatter;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import net.MaplePacket;
import net.channel.ChannelServer;
import net.world.MapleMessenger;
import net.world.MapleMessengerCharacter;
import net.world.MapleParty;
import net.world.MaplePartyCharacter;
import net.world.PartyOperation;
import net.world.PlayerBuffValueHolder;
import net.world.PlayerCoolDownValueHolder;
import net.world.guild.MapleGuild;
import net.world.guild.MapleGuildCharacter;
import net.world.remote.WorldChannelInterface;
import scripting.event.EventInstanceManager;
import scripting.event.EventTeam;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleMiniGame;
import server.MaplePlayerShop;
import server.MaplePortal;
import server.MapleShop;
import server.MapleStatEffect;
import server.MapleStorage;
import server.MapleTrade;
import server.TimerManager;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.maps.AbstractAnimatedMapleMapObject;
import server.maps.FieldLimit;
import server.maps.HiredMerchant;
import server.maps.MapleDoor;
import server.maps.MapleDragon;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleSummon;
import server.maps.PlayerNPCs;
import server.maps.SavedLocation;
import server.maps.SavedLocationType;
import server.quest.MapleQuest;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import client.anticheat.CheatTracker;
import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;
import tools.PrimitiveLogger;
import provider.MapleDataTool;

public class MapleCharacter extends AbstractAnimatedMapleMapObject {

    private int world;
    private int accountid;
    private int rank;
    private int rankMove;
    private int jobRank;
    private int jobRankMove;
    private int id;
    private int level;
    private int str;
    private int dex;
    private int luk;
    private int int_;
    private int hp;
    private int maxhp;
    private int mp;
    private int maxmp;
    private int hpMpApUsed;
    private int hair;
    private int face;
    private int remainingAp;
    private int remainingSp;
    private int fame;
    private int initialSpawnPoint;
    private int mapid;
    private int gender;
    private int currentPage;
    private int currentType = 0;
    private int currentTab = 1;
    private int chair;
    private int title = 0;
    private int itemEffect = 0;
    private int paypalnx;
    private int maplepoints;
    private int cardnx;
    private int guildid;
    private int guildrank;
    private int allianceRank;
    private int messengerposition = 4;
    private int energybar;
    private int gmLevel;
    private int ci = 0;
    private int familyId;
    private int bookCover;
    private String linkedName;
    private int linkedLevel;
    private int battleshipHp = 0;
    private int mesosTraded = 0;
    private int possibleReports = 10;
    private int dojoPoints;
    private int vanquisherStage;
    private int dojoStage;
    private int dojoEnergy;
    private int vanquisherKills;
    private int warpToId = -1;
    private int mesoRate = 1;
    private int dropRate = 1;
    private int omokwins;
    private int omokties;
    private int omoklosses;
    private int matchcardwins;
    private int matchcardties;
    private int matchcardlosses;
    private int married;
    private int fallcounter;
    private int givenRiceCakes;
    private int points = 0;
    private int beaconOid = -1;
    private int cp;
    private int totalCp;
    private int strikes = 0;
    private double expRate = 1;
    private long dojoFinish;
    private long lastfametime;
    private long lastUsedCashItem;
    private long lastHealed;
    private long megaLimit = 0;
    private transient int localmaxhp;
    private transient int localmaxmp;
    private transient int localstr;
    private transient int localdex;
    private transient int localluk;
    private transient int localint_;
    private transient int magic;
    private transient int watk;
    private boolean hasBeacon = false;
    private boolean hidden;
    private boolean canDoor = true;
    private boolean incs;
    private boolean inmts;
    private boolean whitechat = true;
    private boolean Berserk;
    private boolean hasMerchant;
    private boolean watchedCygnusIntro;
    private boolean finishedDojoTutorial;
    private boolean dojoParty;
    private boolean gottenRiceHat;
    private boolean allowMapChange = true;
    private boolean insertSPTable = false;
    private int chartype = 0;
    private boolean stuck = false;
    private String name;
    private String chalktext;
    private String search = null;
    private String partyquestitems = "";
    private AtomicInteger exp = new AtomicInteger();
    private AtomicInteger meso = new AtomicInteger();
    private BuddyList buddylist;
    private EventInstanceManager eventInstance = null;
    private HiredMerchant hiredMerchant = null;
    private MapleClient client;
    private MapleGuildCharacter mgc = null;
    private MapleInventory[] inventory;
    private MapleJob job = MapleJob.BEGINNER;
    private MapleMap map;
    private MapleMap dojoMap;
    private MapleMessenger messenger = null;
    private MapleMiniGame miniGame;
    private MapleMount maplemount;
    private MapleParty party;
    private MaplePet[] pets = new MaplePet[3];
    private MaplePlayerShop playerShop = null;
    private MapleShop shop = null;
    private MapleSkinColor skinColor = MapleSkinColor.NORMAL;
    private MapleStorage storage = null;
    private MapleTrade trade = null;
    private SavedLocation savedLocations[];
    private SkillMacro[] skillMacros = new SkillMacro[5];
    private List<Integer> lastmonthfameids;
    private Map<MapleQuest, MapleQuestStatus> quests;
    private Set<MapleMonster> controlled = new LinkedHashSet<MapleMonster>();
    private Map<Integer, String> entered = new LinkedHashMap<Integer, String>();
    private Set<MapleMapObject> visibleMapObjects = new LinkedHashSet<MapleMapObject>();
    private Map<ISkill, SkillEntry> skills = new LinkedHashMap<ISkill, SkillEntry>();
    private Map<MapleBuffStat, MapleBuffStatValueHolder> effects = Collections.synchronizedMap(new LinkedHashMap<MapleBuffStat, MapleBuffStatValueHolder>());
    private Map<Integer, MapleKeyBinding> keymap = new LinkedHashMap<Integer, MapleKeyBinding>();
    private Map<Integer, MapleSummon> summons = new ConcurrentHashMap<Integer, MapleSummon>();
    private Map<Integer, MapleCoolDownValueHolder> coolDowns = new LinkedHashMap<Integer, MapleCoolDownValueHolder>();
    private List<MapleDisease> diseases = new ArrayList<MapleDisease>();
    private List<MapleDoor> doors = new ArrayList<MapleDoor>();
    public List<Pair<IItem, MapleInventoryType>> tempMerchantItems;
    private ScheduledFuture<?> dragonBloodSchedule;
    private ScheduledFuture<?> mapTimeLimitTask = null;
    private ScheduledFuture<?> periodicSaveTask = null;
    private ScheduledFuture<?> hpDecreaseTask;
    private ScheduledFuture<?> beholderHealingSchedule;
    private ScheduledFuture<?> beholderBuffSchedule;
    private ScheduledFuture<?> BerserkSchedule;
    private ScheduledFuture<?>[] fullness = new ScheduledFuture<?>[3];
    private NumberFormat nf = new DecimalFormat("#,###,###,###");
    private static List<Pair<Byte, Integer>> inventorySlots = new ArrayList<Pair<Byte, Integer>>();
    private ArrayList<String> commands = new ArrayList<String>();
    private ArrayList<Integer> excluded = new ArrayList<Integer>();
    private MonsterBook monsterbook;
    private List<Integer> wishList = new ArrayList<Integer>();
    private List<MapleRing> crushRings = new ArrayList<MapleRing>();
    private List<MapleRing> friendshipRings = new ArrayList<MapleRing>();
    private List<MapleRing> marriageRings = new ArrayList<MapleRing>();
    private List<Integer> vipRockMaps = new LinkedList<Integer>();
    private List<Integer> rockMaps = new LinkedList<Integer>();
    private transient int localmaxbasedamage;
    private CheatTracker anticheat;
    private boolean Beta;
    private boolean receivedMOTB;
    private boolean canSmega;
    public long latestUse = 0;
    private int slot;
    private MapleDragon dragon;
    private ExtendedSPTable SPTable;
    public int summontype = 0;
    private EventTeam team;
    // PQs
    private static String[] ariantroomleader = new String[3];
    private static int[] ariantroomslot = new int[3];

    private MapleCharacter() {
        canSmega = true;
        setStance(0);
        inventory = new MapleInventory[MapleInventoryType.values().length];
        savedLocations = new SavedLocation[SavedLocationType.values().length];
        for (MapleInventoryType type : MapleInventoryType.values()) {
            inventory[type.ordinal()] = new MapleInventory(type);
        }
        for (int i = 0; i < SavedLocationType.values().length; i++) {
            savedLocations[i] = null;
        }
        quests = new LinkedHashMap<MapleQuest, MapleQuestStatus>();
        setPosition(new Point(0, 0));
        //this.anticheat = new CheatTracker(this);
    }

    public static MapleCharacter getDefault(MapleClient c) {
        MapleCharacter ret = new MapleCharacter();
        ret.client = c;
        ret.gmLevel = c.gmLevel();
        ret.Beta = c.isBeta();
        ret.hp = 50;
        ret.maxhp = 50;
        ret.mp = 5;
        ret.maxmp = 5;
        ret.str = 12;
        ret.dex = 5;
        ret.int_ = 4;
        ret.luk = 4;
        ret.map = null;
        ret.mapid = -1;
        ret.job = MapleJob.BEGINNER;
        ret.level = 1;
        ret.accountid = c.getAccID();
        ret.buddylist = new BuddyList(20);
        ret.chartype = 0;

        ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(96);
        ret.getInventory(MapleInventoryType.USE).setSlotLimit(96);
        ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(96);
        ret.getInventory(MapleInventoryType.ETC).setSlotLimit(96);

        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT name, paypalNX, mPoints, cardNX FROM accounts WHERE id = ?");
            ps.setInt(1, ret.accountid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ret.client.setAccountName(rs.getString("name"));
                ret.paypalnx = rs.getInt("paypalNX");
                ret.maplepoints = rs.getInt("mPoints");
                ret.cardnx = rs.getInt("cardNX");
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
        }
        ret.maplemount = null;
        int[] key = {18, 65, 2, 23, 3, 4, 5, 6, 16, 17, 19, 25, 26, 27, 31, 34, 35, 37, 38, 40, 43, 44, 45, 46, 50, 56, 59, 60, 61, 62, 63, 64, 57, 48, 29, 7, 24, 33, 41};
        int[] type = {4, 6, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 4, 4, 5, 6, 6, 6, 6, 6, 6, 5, 4, 5, 4, 4, 4, 4};
        int[] action = {0, 106, 10, 1, 12, 13, 18, 24, 8, 5, 4, 19, 14, 15, 2, 17, 11, 3, 20, 16, 9, 50, 51, 6, 7, 53, 100, 101, 102, 103, 104, 105, 54, 22, 52, 21, 25, 26, 23};
        for (int i = 0; i < key.length; i++) {
            ret.keymap.put(key[i], new MapleKeyBinding(type[i], action[i]));
        }
        return ret;
    }

    public void addCooldown(int skillId, long startTime, long length, ScheduledFuture<?> timer) {
        if (this.coolDowns.containsKey(Integer.valueOf(skillId))) {
            this.coolDowns.remove(skillId);
        }
        this.coolDowns.put(Integer.valueOf(skillId), new MapleCoolDownValueHolder(skillId, startTime, length, timer));
    }

    public void addCommandToList(String command) {
        commands.add(command);
    }

    public void addCrushRing(MapleRing r) {
        crushRings.add(r);
    }

    public int addDojoPointsByMap() {
        int pts = 0;
        if (dojoPoints < 17000) {
            pts = 1 + ((getMap().getId() - 1) / 100 % 100) / 6;
            if (!dojoParty) {
                pts++;
            }
            this.dojoPoints += pts;
        }
        return pts;
    }

    public void addDoor(MapleDoor door) {
        doors.add(door);
    }

    public void addExcluded(int x) {
        excluded.add(x);
    }

    public void addFame(int famechange) {
        this.fame += famechange;
    }

    public void addFriendshipRing(MapleRing r) {
        friendshipRings.add(r);
    }

    public void addHP(int delta) {
        int newHP = hp + delta;
        setHp(newHP);
        updateSingleStat(MapleStat.HP, newHP);
    }
        public boolean getCanSmega() {
        return canSmega;
    }

    public void setCanSmega(boolean setTo) {
        canSmega = setTo;
    }

    public void addMarriageRing(MapleRing r) {
        marriageRings.add(r);
    }

    public void addMesosTraded(int gain) {
        this.mesosTraded += gain;
    }

    public void addMP(int delta) {
        int newMP = mp + delta;
        setMp(newMP);
        updateSingleStat(MapleStat.MP, newMP);
    }

    public void addMPHP(int hpDiff, int mpDiff) {
        int newHP = hp + hpDiff;
        int newMP = mp + mpDiff;
        setHp(newHP);
        setMp(newMP);
        updateSingleStat(MapleStat.HP, newHP);
        updateSingleStat(MapleStat.MP, newMP);
    }

    public void addPet(MaplePet pet) {
        for (int i = 0; i < 3; i++) {
            if (pets[i] == null) {
                pets[i] = pet;
                return;
            }
        }
    }

    public void addStat(int type, int up) {
        if (type == 1) {
            this.str += up;
            updateSingleStat(MapleStat.STR, str);
        } else if (type == 2) {
            this.dex += up;
            updateSingleStat(MapleStat.DEX, dex);
        } else if (type == 3) {
            this.int_ += up;
            updateSingleStat(MapleStat.INT, int_);
        } else if (type == 4) {
            this.luk += up;
            updateSingleStat(MapleStat.LUK, luk);
        }
    }

    public void addSummon(final int id, final MapleSummon summon) {
        summons.put(id, summon);

    /*    TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                getMap().broadcastMessage(MaplePacketCreator.removeSpecialMapObject(summon, true), summon.getPosition());
                getMap().removeMapObject(summon);
                removeVisibleMapObject(summon);
                summons.remove(id);
            }
        }, summon.getDuration() * 1000);*/
    }

    public void addTeleportRockMap(Integer mapId, int type) {
        if (type == 0 && rockMaps.size() < 5 && !rockMaps.contains(mapId)) {
            rockMaps.add(mapId);
        } else if (vipRockMaps.size() < 10 && !vipRockMaps.contains(mapId)) {
            vipRockMaps.add(mapId);
        }
    }

    public void addToWishList(int sn) {
        wishList.add(sn);
    }

    public void addVisibleMapObject(MapleMapObject mo) {
        visibleMapObjects.add(mo);
    }

    public void ban(String reason, boolean dc) {
        try {
            client.banMacs();
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = 1, norankupdate = 1, banreason = ? WHERE id = ?");
            ps.setString(1, reason);
            ps.setInt(2, accountid);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
            ps.setString(1, client.getSession().getRemoteAddress().toString().split(":")[0]);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
        }
        client.disconnect();
    }

    public static boolean ban(String id, String reason, boolean accountId) {
        PreparedStatement ps = null;
        try {
            Connection con = DatabaseConnection.getConnection();
            if (id.matches("/[0-9]{1,3}\\..*")) {
                ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                ps.setString(1, id);
                ps.executeUpdate();
                ps.close();
                return true;
            }
            if (accountId) {
                ps = con.prepareStatement("SELECT id FROM accounts WHERE name = ?");
            } else {
                ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            }
            boolean ret = false;
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PreparedStatement psb = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET banned = 1, norankupdate = 1, banreason = ? WHERE id = ?");
                psb.setString(1, reason);
                psb.setInt(2, rs.getInt(1));
                psb.executeUpdate();
                psb.close();
                ret = true;
            }
            rs.close();
            ps.close();
            return ret;
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
            } catch (SQLException e) {
            }
        }
        return false;
    }

    public void addStrike(String GMName)
    {
        Calendar banDuration = Calendar.getInstance();
        strikes++;
        switch(this.strikes)
        {
            case 0:
                banDuration.set(Calendar.HOUR_OF_DAY, banDuration.get(Calendar.HOUR_OF_DAY) + 1);
                break;
            case 1:
                banDuration.set(Calendar.DATE, banDuration.get(Calendar.DATE) + 1);
                break;
            case 2:
                banDuration.set(Calendar.DATE, banDuration.get(Calendar.DATE) + 7);
                break;
            default:
                banDuration.set(Calendar.YEAR, 2050); //shows as perm
                break;
        }
        this.tempban("Strike " + strikes + ", given by GM " + GMName, banDuration, 7, this.accountid);
        this.getClient().disconnect();
    }

    public void resetStrikes()
    {
        this.strikes = 0;
        this.saveToDB(true);
    }
    	public void tempban(String reason, Calendar duration, int greason) {
		if (lastmonthfameids == null) {
			throw new RuntimeException("Trying to ban a non-loaded character (testhack)");
		}
		tempban(reason, duration, greason, client.getAccID());
		client.getSession().close();
	}

        /*
         * 			DateFormat df = DateFormat.getInstance();
			tempB.set(tempB.get(Calendar.YEAR) + yChange, tempB.get(Calendar.MONTH) + mChange, tempB.get(Calendar.DATE) +
				(wChange * 7) + dChange, tempB.get(Calendar.HOUR_OF_DAY) + hChange, tempB.get(Calendar.MINUTE) +
				iChange);
         */

    	public static boolean tempban(String reason, Calendar duration, int greason, int accountid) {
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE accounts SET tempban = ?, banreason = ?, greason = ? WHERE id = ?");
			Timestamp TS = new Timestamp(duration.getTimeInMillis());
			ps.setTimestamp(1, TS);
			ps.setString(2, reason);
			ps.setInt(3, greason);
			ps.setInt(4, accountid);
			ps.executeUpdate();
			ps.close();
			return true;
		} catch (SQLException ex) {
                        ex.printStackTrace();
                    //log.error("Error while tempbanning", ex);
		}
		return false;
	}

    public int calculateMaxBaseDamageForHH(int watk) {
        if (watk == 0) {
            return 1;
        } else {
            IItem weapon_item = getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
            if (weapon_item != null) {
                return (int) (((MapleItemInformationProvider.getInstance().getWeaponType(weapon_item.getItemId()).getMaxDamageMultiplier() * localstr + localdex) / 100.0) * watk);
            } else {
                return 0;
            }
        }
    }

    public void cancelAllBuffs() {
        for (MapleBuffStatValueHolder mbsvh : new ArrayList<MapleBuffStatValueHolder>(effects.values())) {
            cancelEffect(mbsvh.effect, false, mbsvh.startTime);
        }
    }

    public void cancelBuffStats(MapleBuffStat stat) {
        List<MapleBuffStat> buffStatList = Arrays.asList(stat);
        deregisterBuffStats(buffStatList);
        cancelPlayerBuffs(buffStatList);
    }

    public static class CancelCooldownAction implements Runnable {

        private int skillId;
        private WeakReference<MapleCharacter> target;

        public CancelCooldownAction(MapleCharacter target, int skillId) {
            this.target = new WeakReference<MapleCharacter>(target);
            this.skillId = skillId;
        }

        @Override
        public void run() {
            MapleCharacter realTarget = target.get();
            if (realTarget != null) {
                realTarget.removeCooldown(skillId);
                realTarget.client.getSession().write(MaplePacketCreator.skillCooldown(skillId, 0));
            }
        }
    }

    public void cancelEffect(MapleStatEffect effect, boolean overwrite, long startTime) {
        List<MapleBuffStat> buffstats;
        if (!overwrite) {
            buffstats = getBuffStats(effect, startTime);
        } else {
            List<Pair<MapleBuffStat, Integer>> statups = effect.getStatups();
            buffstats = new ArrayList<MapleBuffStat>(statups.size());
            for (Pair<MapleBuffStat, Integer> statup : statups) {
                buffstats.add(statup.getLeft());
            }
        }
        deregisterBuffStats(buffstats);
        if (effect.isMagicDoor()) {
            if (!getDoors().isEmpty()) {
                MapleDoor door = getDoors().iterator().next();
                for (MapleCharacter chr : door.getTarget().getCharacters()) {
                    door.sendDestroyData(chr.client);
                }
                for (MapleCharacter chr : door.getTown().getCharacters()) {
                    door.sendDestroyData(chr.client);
                }
                for (MapleDoor destroyDoor : getDoors()) {
                    door.getTarget().removeMapObject(destroyDoor);
                    door.getTown().removeMapObject(destroyDoor);
                }
                clearDoors();
                silentPartyUpdate();
            }
        }
        if (effect.getSourceId() == Spearman.HYPER_BODY || effect.getSourceId() == GM.HYPER_BODY) {
            List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(4);
            statup.add(new Pair<MapleStat, Integer>(MapleStat.HP, Math.min(hp, maxhp)));
            statup.add(new Pair<MapleStat, Integer>(MapleStat.MP, Math.min(mp, maxhp)));
            statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, maxhp));
            statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, maxmp));
            client.getSession().write(MaplePacketCreator.updatePlayerStats(statup));
        }
        if (effect.isMonsterRiding()) {
            if (effect.getSourceId() != Corsair.BATTLE_SHIP) {
                maplemount.cancelSchedule();
                maplemount.setActive(false);
            }
        }
        final int id_ = effect.getSourceId();
        if (id_ == DawnWarrior.SOUL || id_ == BlazeWizard.FLAME || id_ == WindArcher.STORM || id_ == NightWalker.DARKNESS || id_ == ThunderBreaker.LIGHTNING) {
            message(new String[]{"Soul", "Flame", "Storm", "Darkness", "Lightning"}[id / 100000 % 10] + "'s time has run out has disappeared.");
        }
        if (!overwrite) {
            cancelPlayerBuffs(buffstats);
            if (effect.isHide() && (MapleCharacter) getMap().getMapObject(getObjectId()) != null) {
                this.hidden = false;
                this.getClient().getSession().write(MaplePacketCreator.getGMEffect(16, (byte)0));
                getMap().broadcastNONGMMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);
                for (int i = 0; pets[i] != null; i++) {
                    getMap().broadcastNONGMMessage(this, MaplePacketCreator.showPet(this, pets[i], false, false), false);
                }
            }
        }
    }

    public void cancelEffectFromBuffStat(MapleBuffStat stat) {
        cancelEffect(effects.get(stat).effect, false, -1);
    }

    public void cancelMagicDoor() {
        for (MapleBuffStatValueHolder mbsvh : new ArrayList<MapleBuffStatValueHolder>(effects.values())) {
            if (mbsvh.effect.isMagicDoor()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public void cancelMapTimeLimitTask() {
        if (mapTimeLimitTask != null) {
            mapTimeLimitTask.cancel(false);
        }
    }

    public void cancelPeriodicSaveTask() {
        if (periodicSaveTask != null) {
            periodicSaveTask.cancel(false);
        }
    }

    private void cancelPlayerBuffs(List<MapleBuffStat> buffstats) {
        if (client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
            recalcLocalStats();
            enforceMaxHpMp();
            client.getSession().write(MaplePacketCreator.cancelBuff(buffstats));
            if (buffstats.size() > 0 && !buffstats.get(0).equals(MapleBuffStat.HOMING_BEACON)) {
                getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignBuff(getId(), buffstats), false);
            }
        }
    }

    public static boolean canCreateChar(String name) {
        if (name.getBytes().length <= 3 || name.getBytes().length > 12) {
            return false;
        }
        return getIdByName(name) < 0/* && Pattern.compile("[°¡-?a-zA-Z0-9_-]{2,12}").matcher(name).matches()*/;
    }

    public boolean canDoor() {
        return canDoor;
    }

    public FameStatus canGiveFame(MapleCharacter from) {
        if (gmLevel > 0) {
            return FameStatus.OK;
        } else if (lastfametime >= System.currentTimeMillis() - 3600000 * 24) {
            return FameStatus.NOT_TODAY;
        } else if (lastmonthfameids.contains(Integer.valueOf(from.getId()))) {
            return FameStatus.NOT_THIS_MONTH;
        } else {
            return FameStatus.OK;
        }
    }

    public void changeCI(int type) {
        this.ci = type;
    }

    public void changeJob(MapleJob newJob) {
        if (newJob == null)
            return;
        this.job = newJob;
        this.remainingSp++;
        if (newJob.getId() % 10 == 2) {
            this.remainingSp += 2;
        }
        if (newJob.getId() % 10 > 1) {
            this.remainingAp += 5;
        }
        int job_ = job.getId() % 1000; // lame temp "fix"
        if (job_ == 100) {
            maxhp += rand(200, 250);
        } else if (job_ == 200) {
            if (job.getId() == 200 && level > 8) {
                remainingSp += 3 * (level - 8);
            }
            maxmp += rand(100, 150);
        } else if (job_ % 100 == 0) {
            maxhp += rand(100, 150);
            maxhp += rand(25, 50);
        } else if (job_ > 0 && job_ < 200) {
            maxhp += rand(300, 350);
        } else if (job_ < 300) {
            maxmp += rand(250, 300);
        } else if (job_ > 0 && job_ != 1000) {
            maxhp += rand(300, 350);
            maxmp += rand(150, 200);
        }
        if (maxhp >= 99999) {
            maxhp = 99999;
        }
        if (maxmp >= 99999) {
            maxmp = 99999;
        }
        if (gmLevel < 1) {
            for (int i = (job_ == 200 ? 2 : 1); i < (job_ != 100 && job_ != 200 ? 3 : 5); i++)
                getInventory(MapleInventoryType.getByType((byte) i)).increaseSlotLimit(4);
        }
        List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(5);
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, Integer.valueOf(maxhp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, Integer.valueOf(maxmp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, remainingAp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLESP, remainingSp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.JOB, Integer.valueOf(job.getId())));
        recalcLocalStats();
        client.getSession().write(MaplePacketCreator.updatePlayerStats(statup, false, MapleJob.isExtendSPJob(job), this.SPTable));
        silentPartyUpdate();
        guildUpdate();
        getMap().broadcastMessage(this, MaplePacketCreator.showForeignEffect(getId(), 8), false);
    }

    public void changeKeybinding(int key, MapleKeyBinding keybinding) {
        if (keybinding.getType() != 0) {
            keymap.put(Integer.valueOf(key), keybinding);
        } else {
            keymap.remove(Integer.valueOf(key));
        }
    }

    public void changeMap(MapleMap to) {
        changeMap(to, to.getPortal(0));
    }
    public void changeMap(MapleMap to, boolean release) {
        changeMap(to, to.getPortal(0), release);
    }

    public void changeMap(final MapleMap to, final MaplePortal pto, boolean release) {
        if (to.getId() == 100000200 || to.getId() == 211000100 || to.getId() == 220000300) {
            changeMapInternal(to, pto.getPosition(), MaplePacketCreator.getWarpToMap(to, pto.getId() - 2, this), release);
        } else {
            changeMapInternal(to, pto.getPosition(), MaplePacketCreator.getWarpToMap(to, pto.getId(), this), release);
        }
    }
    public void changeMap(final MapleMap to, final MaplePortal pto) {
        changeMap(to, pto, false);
    }

    public void changeMap(final MapleMap to, final Point pos) {
        changeMapInternal(to, pos, MaplePacketCreator.getWarpToMap(to, 0x80, this), false);
    }

    public void changeMap(final MapleMap to, final Point pos, boolean release) {
        changeMapInternal(to, pos, MaplePacketCreator.getWarpToMap(to, 0x80, this), release);
    }

    public void changeMapBanish(int mapid, String portal, String msg) {
        dropMessage(5, msg);
        MapleMap map_ = ChannelServer.getInstance(client.getChannel()).getMapFactory().getMap(mapid);
        changeMap(map_, map_.getPortal(portal));
    }

    private void changeMapInternal(final MapleMap to, final Point pos, MaplePacket warpPacket, boolean release) {
        if ((this.getEventInstance() != null) && (!this.getEventInstance().mapChanged(this))) {
            this.dropMessage(6, "Map changes are not allowed in this event. Please leave the event before changing your map.");
            return;
        }
        if (this.getMap().getProperties().getProperty("jail").equals(Boolean.TRUE) && !release && !this.isGM()) {
            this.dropMessage(6, "No such luck, buddy. You're in jail. Please wait for a GM to warp you out.");
            return;
        }
        warpPacket.setOnSend(new Runnable() {

            @Override
            public void run() {
                map.removePlayer(MapleCharacter.this);
                if (client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
                    map = to;
                    setPosition(pos);
                    map.addPlayer(MapleCharacter.this);
                    if (party != null) {
                        silentPartyUpdate();
                        client.getSession().write(MaplePacketCreator.updateParty(client.getChannel(), party, PartyOperation.SILENT_UPDATE, null));
                        updatePartyMemberHP();
                    }
                    if (getMap().getHPDec() > 0) {
                        hpDecreaseTask = TimerManager.getInstance().schedule(new Runnable() {

                            @Override
                            public void run() {
                                doHurtHp();
                            }
                        }, 10000);
                    }
                }
            }
        });
        client.getSession().write(warpPacket);
    }

    public void changePage(int page) {
        this.currentPage = page;
    }

    public void changeSkillLevel(ISkill skill, int newLevel, int newMasterlevel) {
        skills.put(skill, new SkillEntry(newLevel, newMasterlevel));
        this.client.getSession().write(MaplePacketCreator.updateSkill(skill.getId(), newLevel, newMasterlevel));
    }

    public void changeTab(int tab) {
        this.currentTab = tab;
    }

    public void changeType(int type) {
        this.currentType = type;
    }

    public void checkBerserk() {
        if (BerserkSchedule != null) {
            BerserkSchedule.cancel(false);
        }
        final MapleCharacter chr = this;
        if (job.equals(MapleJob.DARKKNIGHT)) {
            ISkill BerserkX = SkillFactory.getSkill(DarkKnight.BERSERK);
            final int skilllevel = getSkillLevel(BerserkX);
            if (skilllevel > 0) {
                Berserk = chr.getHp() * 100 / chr.getMaxHp() < BerserkX.getEffect(skilllevel).getX();
                BerserkSchedule = TimerManager.getInstance().register(new Runnable() {

                    @Override
                    public void run() {
                        client.getSession().write(MaplePacketCreator.showOwnBerserk(skilllevel, Berserk));
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBerserk(getId(), skilllevel, Berserk), false);
                    }
                }, 5000, 3000);
            }
        }
    }

    public void checkMessenger() {
        if (messenger != null && messengerposition < 4 && messengerposition > -1) {
            try {
                WorldChannelInterface wci = ChannelServer.getInstance(client.getChannel()).getWorldInterface();
                wci.silentJoinMessenger(messenger.getId(), new MapleMessengerCharacter(this, messengerposition), messengerposition);
                wci.updateMessenger(getMessenger().getId(), name, client.getChannel());
            } catch (Exception e) {
                client.getChannelServer().reconnectWorld();
            }
        }
    }

    public void checkMonsterAggro(MapleMonster monster) {
        if (!monster.isControllerHasAggro()) {
            if (monster.getController() == this) {
                monster.setControllerHasAggro(true);
            } else {
                monster.switchController(this, true);
            }
        }
    }

    public void clearDoors() {
        doors.clear();
    }

    public void clearSavedLocation(SavedLocationType type) {
        savedLocations[type.ordinal()] = null;
    }

    public void clearWishList() {
        wishList.clear();
    }

    public void controlMonster(MapleMonster monster, boolean aggro) {
        monster.setController(this);
        controlled.add(monster);
        client.getSession().write(MaplePacketCreator.controlMonster(monster, false, aggro));
    }

    public int countItem(int itemid) {
        return inventory[MapleItemInformationProvider.getInstance().getInventoryType(itemid).ordinal()].countById(itemid);
    }

    public void decreaseBattleshipHp(int decrease) {
        this.battleshipHp -= decrease;
        if (battleshipHp <= 0) {
            this.battleshipHp = 0;
            ISkill battleship = SkillFactory.getSkill(Corsair.BATTLE_SHIP);
            int cooldown = battleship.getEffect(getSkillLevel(battleship)).getCooldown();
            getClient().getSession().write(MaplePacketCreator.skillCooldown(Corsair.BATTLE_SHIP, cooldown));
            addCooldown(Corsair.BATTLE_SHIP, System.currentTimeMillis(), cooldown * 1000, TimerManager.getInstance().schedule(new CancelCooldownAction(this, Corsair.BATTLE_SHIP), cooldown * 1000));
            cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
            resetBattleshipHp();
        }
    }

    public void decreaseReports() {
        this.possibleReports--;
    }

    public void deleteGuild(int guildId) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = 0, guildrank = 5 WHERE guildid = ?");
            ps.setInt(1, guildId);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("DELETE FROM guilds WHERE guildid = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            System.out.print("Error deleting guild: " + ex);
        }
    }

    public void deleteTeleportRockMap(Integer mapId, int type) {
        if (type == 0) {
            rockMaps.remove(mapId);
        } else {
            vipRockMaps.remove(mapId);
        }
    }

    private void deleteWhereCharacterId(Connection con, String sql) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    private void deregisterBuffStats(List<MapleBuffStat> stats) {
        synchronized (stats) {
            List<MapleBuffStatValueHolder> effectsToCancel = new ArrayList<MapleBuffStatValueHolder>(stats.size());
            for (MapleBuffStat stat : stats) {
                MapleBuffStatValueHolder mbsvh = effects.get(stat);
                if (mbsvh != null) {
                    effects.remove(stat);
                    boolean addMbsvh = true;
                    for (MapleBuffStatValueHolder contained : effectsToCancel) {
                        if (mbsvh.startTime == contained.startTime && contained.effect == mbsvh.effect) {
                            addMbsvh = false;
                        }
                    }
                    if (addMbsvh) {
                        effectsToCancel.add(mbsvh);
                    }
                    if (stat == MapleBuffStat.SUMMON || stat == MapleBuffStat.PUPPET) {
                        int summonId = mbsvh.effect.getSourceId();
                        MapleSummon summon = summons.get(summonId);
                        if (summon != null) {
                            getMap().broadcastMessage(MaplePacketCreator.removeSpecialMapObject(summon, true), summon.getPosition());
                            getMap().removeMapObject(summon);
                            removeVisibleMapObject(summon);
                            summons.remove(summonId);
                        }
                        if (summon.getSkill() == DarkKnight.BEHOLDER) {
                            if (beholderHealingSchedule != null) {
                                beholderHealingSchedule.cancel(false);
                                beholderHealingSchedule = null;
                            }
                            if (beholderBuffSchedule != null) {
                                beholderBuffSchedule.cancel(false);
                                beholderBuffSchedule = null;
                            }
                        }
                    } else if (stat == MapleBuffStat.DRAGONBLOOD) {
                        dragonBloodSchedule.cancel(false);
                        dragonBloodSchedule = null;
                    }
                }
                stat = null;
            }
            for (MapleBuffStatValueHolder cancelEffectCancelTasks : effectsToCancel) {
                if (getBuffStats(cancelEffectCancelTasks.effect, cancelEffectCancelTasks.startTime).size() == 0) {
                    if (!cancelEffectCancelTasks.effect.isHomingBeacon()) {
                        cancelEffectCancelTasks.schedule.cancel(false);
                    }
                }
            }
        }
    }

    public void disableDoor() {
        canDoor = false;
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                canDoor = true;
            }
        }, 5000);
    }

    public void disbandGuild() {
        if (guildid < 1 || guildrank != 1) {
            return;
        }
        try {
            client.getChannelServer().getWorldInterface().disbandGuild(guildid);
        } catch (Exception e) {
        }
    }

    public void dispel() {
        for (MapleBuffStatValueHolder mbsvh : new ArrayList<MapleBuffStatValueHolder>(effects.values())) {
            if (mbsvh.effect.isSkill()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public void dispelDebuffs() {
        setRates(true);
        List<MapleDisease> disease_ = new ArrayList<MapleDisease>();
        for (MapleDisease disease : diseases) {
            disease_.add(disease);
            client.getSession().write(MaplePacketCreator.cancelDebuff(disease_));
            getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(this.id, disease_), false);
            disease_.clear();
        }
        this.diseases.clear();
    }

    public void dispelSeduce() {
        List<MapleDisease> disease_ = new ArrayList<MapleDisease>();
        for (MapleDisease disease : diseases) {
            if (disease == MapleDisease.SEDUCE) {
                disease_.add(disease);
                client.getSession().write(MaplePacketCreator.cancelDebuff(disease_));
                getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(this.id, disease_), false);
                disease_.clear();
            }
        }
        this.diseases.clear();
    }

    public void dispelSkill(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (skillid == 0) {
                if (mbsvh.effect.isSkill() && (mbsvh.effect.getSourceId() % 10000000 == 1004 || dispelSkills(mbsvh.effect.getSourceId()))) {
                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                }
            } else if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    private boolean dispelSkills(int skillid) {
        switch (skillid) {
            case DarkKnight.BEHOLDER:
            case FPArchMage.ELQUINES:
            case ILArchMage.IFRIT:
            case Priest.SUMMON_DRAGON:
            case Bishop.BAHAMUT:
            case Ranger.PUPPET:
            case Ranger.SILVER_HAWK:
            case Sniper.PUPPET:
            case Sniper.GOLDEN_EAGLE:
            case Hermit.SHADOW_PARTNER:
                return true;
            default:
                return false;
        }
    }

    public void dispelSeal() {
        List<MapleDisease> disease_ = new ArrayList<MapleDisease>();
        for (MapleDisease disease : diseases) {
            if (disease == MapleDisease.SEAL) {
                disease_.add(disease);
                client.getSession().write(MaplePacketCreator.cancelDebuff(disease_));
                getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(this.id, disease_), false);
                disease_.clear();
            }
        }
        this.diseases.clear();
    }

    public void doHurtHp() {
        if (this.getInventory(MapleInventoryType.EQUIPPED).findById(getMap().getHPDecProtect()) != null) {
            return;
        }
        addHP(-getMap().getHPDec());
        hpDecreaseTask = TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                doHurtHp();
            }
        }, 10000);
    }

    public void dropMessage(String message) {
        dropMessage(5, message);
    }

    public void dropMessage(int type, String message) {
        client.getSession().write(MaplePacketCreator.serverNotice(type, message));
    }

    public String emblemCost() {
        return nf.format(MapleGuild.CHANGE_EMBLEM_COST);
    }

    private void enforceMaxHpMp() {
        List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>(2);
        if (getMp() > getCurrentMaxMp()) {
            setMp(getMp());
            stats.add(new Pair<MapleStat, Integer>(MapleStat.MP, Integer.valueOf(getMp())));
        }
        if (getHp() > getCurrentMaxHp()) {
            setHp(getHp());
            stats.add(new Pair<MapleStat, Integer>(MapleStat.HP, Integer.valueOf(getHp())));
        }
        if (stats.size() > 0) {
            client.getSession().write(MaplePacketCreator.updatePlayerStats(stats));
        }
    }

    public void enteredScript(String script, int mapid) {
        if (!entered.containsKey(mapid)) {
            entered.put(mapid, script);
        }
    }

    public void equipChanged() {
        getMap().broadcastMessage(this, MaplePacketCreator.updateCharLook(this), false);
        recalcLocalStats();
        enforceMaxHpMp();
        if (getMessenger() != null) {
            WorldChannelInterface wci = client.getChannelServer().getWorldInterface();
            try {
                wci.updateMessenger(getMessenger().getId(), getName(), client.getChannel());
            } catch (Exception e) {
                client.getChannelServer().reconnectWorld();
            }
        }
    }

    public void expirationTask() {
        long expiration, currenttime = System.currentTimeMillis();
        List<IItem> toberemove = new ArrayList<IItem>(); // This is here to prevent deadlock.
        for (MapleInventory inv : inventory) {
            for (IItem item : inv.list()) {
                expiration = item.getExpiration();
                if (expiration != -1) {
                    if (currenttime < expiration) {
                        client.getSession().write(MaplePacketCreator.itemExpired(item.getItemId()));
                        toberemove.add(item);
                    }
                }
            }
            for (IItem item : toberemove) {
                MapleInventoryManipulator.removeFromSlot(client, inv.getType(), item.getPosition(), item.getQuantity(), true);
            }
            toberemove.clear();
        }
    }

    public enum FameStatus {

        OK, NOT_TODAY, NOT_THIS_MONTH
    }

    public void gainExp(int gain, boolean show, boolean inChat) {
        gainExp(gain, show, inChat, true);
    }

    public void gainExp(int gain, boolean show, boolean inChat, boolean white) {
        gainExp(gain, show, inChat, white, 0);
    }

    public void gainExp(int gain, boolean show, boolean inChat, boolean white, int party) {
        if (level < getMaxLevel()) {
            int total = gain;
            if (party > 1) {
                total += party * gain / 20;
            }
            if ((long) this.exp.get() + (long) gain > (long) Integer.MAX_VALUE) {
                int gainFirst = ExpTable.getExpNeededForLevel(level) - this.exp.get();
                gain -= gainFirst + 1;
                this.gainExp(gainFirst + 1, false, inChat, white);
            }
            updateSingleStat(MapleStat.EXP, this.exp.addAndGet(gain));
            if (show && gain != 0) {
                client.getSession().write(MaplePacketCreator.getShowExpGain(gain, inChat, white, (byte) (total != gain ? party - 1 : 0)));
            }
            if (exp.get() >= ExpTable.getExpNeededForLevel(level)) {
                levelUp(true);
                int need = ExpTable.getExpNeededForLevel(level);
                if (exp.get() >= need) {
                    setExp(need - 1);
                    updateSingleStat(MapleStat.EXP, need);
                }
            }
        }
    }

    public void gainFame(int delta) {
        this.addFame(delta);
        this.updateSingleStat(MapleStat.FAME, this.fame);
    }

    public void gainMeso(int gain, boolean show) {
        gainMeso(gain, show, false, false);
    }

    public void gainMeso(int gain, boolean show, boolean enableActions, boolean inChat) {
        boolean noOp = false;
        if (((long) (meso.get()) + gain) >= 2147483647L) { //no-op; they've reached max mesos
            noOp = true;
            client.getSession().write(MaplePacketCreator.enableActions());
            return;
     //       updateSingleStat(MapleStat.MESO, meso.addAndGet(2147483647 - meso.get()), enableActions);
        } else {
            updateSingleStat(MapleStat.MESO, meso.addAndGet(gain), enableActions);
        }
        if (show && !noOp) {
            client.getSession().write(MaplePacketCreator.getShowMesoGain(gain, inChat));
        }
    }

    public void clearSlots() {
        inventorySlots.clear();
    }

    public void genericGuildMessage(int code) {
        this.client.getSession().write(MaplePacketCreator.genericGuildMessage((byte) code));
    }

    public int getAccountID() {
        return accountid;
    }

    public List<PlayerBuffValueHolder> getAllBuffs() {
        List<PlayerBuffValueHolder> ret = new ArrayList<PlayerBuffValueHolder>();
        for (MapleBuffStatValueHolder mbsvh : effects.values()) {
            ret.add(new PlayerBuffValueHolder(mbsvh.startTime, mbsvh.effect));
        }
        return ret;
    }

    public List<PlayerCoolDownValueHolder> getAllCooldowns() {
        List<PlayerCoolDownValueHolder> ret = new ArrayList<PlayerCoolDownValueHolder>();
        for (MapleCoolDownValueHolder mcdvh : coolDowns.values()) {
            ret.add(new PlayerCoolDownValueHolder(mcdvh.skillId, mcdvh.startTime, mcdvh.length));
        }
        return ret;
    }

    public int getAllianceRank() {
        return this.allianceRank;
    }

    public int getAllowWarpToId() {
        return warpToId;
    }

    public static String getAriantRoomLeaderName(int room) {
        return ariantroomleader[room];
    }

    public static int getAriantSlotsRoom(int room) {
        return ariantroomslot[room];
    }

    public int getBattleshipHp() {
        return battleshipHp;
    }

    public BuddyList getBuddylist() {
        return buddylist;
    }

    public Long getBuffedStarttime(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return Long.valueOf(mbsvh.startTime);
    }

    public Integer getBuffedValue(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return Integer.valueOf(mbsvh.value);
    }

    public int getBuffSource(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        if (mbsvh == null) {
            return -1;
        }
        return mbsvh.effect.getSourceId();
    }

    private List<MapleBuffStat> getBuffStats(MapleStatEffect effect, long startTime) {
        List<MapleBuffStat> stats = new ArrayList<MapleBuffStat>();
        for (Entry<MapleBuffStat, MapleBuffStatValueHolder> stateffect : effects.entrySet()) {
            if (stateffect.getValue().effect.sameSource(effect) && (startTime == -1 || startTime == stateffect.getValue().startTime)) {
                stats.add(stateffect.getKey());
            }
        }
        return stats;
    }

    public int getChair() {
        return chair;
    }

    public String getChalkboard() {
        return this.chalktext;
    }

    public MapleClient getClient() {
        return client;
    }

    public final List<MapleQuestStatus> getCompletedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<MapleQuestStatus>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus().equals(MapleQuestStatus.Status.COMPLETED)) {
                ret.add(q);
            }
        }
        return Collections.unmodifiableList(ret);
    }

    public Collection<MapleMonster> getControlledMonsters() {
        return Collections.unmodifiableCollection(controlled);
    }

    public int getCp() {
        return cp;
    }

    public List<MapleRing> getCrushRings() {
        Collections.sort(crushRings);
        return crushRings;
    }

    public int getCSPoints(int type) {
        switch (type) {
            case 1:
                return paypalnx;
            case 2:
                return maplepoints;
            default:
                return cardnx;
        }
    }

    public int getCurrentCI() {
        return ci;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getCurrentMaxHp() {
        return localmaxhp;
    }

    public int getCurrentMaxMp() {
        return localmaxmp;
    }

    public int getCurrentTab() {
        return currentTab;
    }

    public int getCurrentType() {
        return currentType;
    }

    public int getDex() {
        return dex;
    }

    public List<MapleDisease> getDiseases() {
        synchronized (diseases) {
            return Collections.unmodifiableList(diseases);
        }
    }

    public int getDojoEnergy() {
        return dojoEnergy;
    }

    public boolean getDojoParty() {
        return dojoParty;
    }

    public int getDojoPoints() {
        return dojoPoints;
    }

    public int getDojoStage() {
        return dojoStage;
    }

    public List<MapleDoor> getDoors() {
        return new ArrayList<MapleDoor>(doors);
    }

    public int getDropRate() {
        return dropRate;
    }

    public int getEnergyBar() {
        return energybar;
    }

    public EventInstanceManager getEventInstance() {
        return eventInstance;
    }

    public ArrayList<Integer> getExcluded() {
        return excluded;
    }

    public int getExp() {
        return exp.get();
    }

    public double getExpRate() {
        return expRate;
    }

    public int getFace() {
        return face;
    }

    public int getFallCounter() {
        return fallcounter;
    }

    public int getFame() {
        return fame;
    }

    public MapleFamilyEntry getFamily() {
        return MapleFamily.getMapleFamily(this).getMember(getId());
    }

    public int getFamilyId() {
        return familyId;
    }

    public boolean getFinishedDojoTutorial() {
        return finishedDojoTutorial;
    }

    public List<MapleRing> getFriendshipRings() {
        Collections.sort(friendshipRings);
        return friendshipRings;
    }

    public int getGender() {
        return gender;
    }

    public int getGivenRiceCakes() {
        return givenRiceCakes;
    }

    public boolean getGMChat() {
        return whitechat;
    }

    public boolean getGottenRiceHat() {
        return gottenRiceHat;
    }

    public MapleGuild getGuild() {
        try {
            return client.getChannelServer().getWorldInterface().getGuild(getGuildId(), null);
        } catch (Exception ex) {
            return null;
        }
    }

    public int getGuildId() {
        return guildid;
    }

    public int getGuildRank() {
        return guildrank;
    }

    public int getHair() {
        return hair;
    }

    public HiredMerchant getHiredMerchant() {
        return hiredMerchant;
    }

    public int getHp() {
        return hp;
    }

    public int getHpMpApUsed() {
        return hpMpApUsed;
    }

    public int getId() {
        return id;
    }

    public static int getIdByName(String name) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT id FROM characters WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            int id = -1;
            if (rs.next()) {
                id = rs.getInt("id");
            }
            rs.close();
            ps.close();
            return id;
        } catch (Exception e) {
        }
        return -1;
    }

    public int getInitialSpawnpoint() {
        return initialSpawnPoint;
    }

    public int getInt() {
        return int_;
    }

    public MapleInventory getInventory(MapleInventoryType type) {
        return inventory[type.ordinal()];
    }

    public int getItemEffect() {
        return itemEffect;
    }
    
    public int getTitleItem() {
        return title;
    }

    public int getItemQuantity(int itemid, boolean checkEquipped) {
        int possesed = inventory[MapleItemInformationProvider.getInstance().getInventoryType(itemid).ordinal()].countById(itemid);
        if (checkEquipped) {
            possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        }
        return possesed;
    }

    public MapleJob getJob() {
        return job;
    }

    public int getJobRank() {
        return jobRank;
    }

    public int getJobRankMove() {
        return jobRankMove;
    }

    public int getJobType() {
        return job.getId() / 1000;
    }

    public Map<Integer, MapleKeyBinding> getKeymap() {
        return keymap;
    }

    public long getLastHealed() {
        return lastHealed;
    }

    public long getLastUsedCashItem() {
        return lastUsedCashItem;
    }

    public int getLevel() {
        return level;
    }

    public String getLinkedName() {
        return linkedName;
    }

    public int getLinkedLevel() {
        return linkedLevel;
    }

    public boolean isLinked() {
        return linkedName != null;
    }

    public int getLuk() {
        return luk;
    }

    public MapleMap getMap() {
        return map;
    }

    public int getMapId() {
        if (map != null) {
            return map.getId();
        }
        return mapid;
    }

    public List<MapleRing> getMarriageRings() {
        Collections.sort(marriageRings);
        return marriageRings;
    }

    public int getMarried() {
        return married;
    }

    public int getMasterLevel(ISkill skill) {
        if (skills.get(skill) == null) {
            return 0;
        }
        return skills.get(skill).masterlevel;
    }

    public int getMaxHp() {
        return maxhp;
    }

    public int getMaxLevel() {
        return isCygnus() ? 120 : 200;
    }

    public int getMaxMp() {
        return maxmp;
    }

    public int getMeso() {
        return meso.get();
    }

    public int getMesoRate() {
        return mesoRate;
    }

    public int getMesosTraded() {
        return mesosTraded;
    }

    public int getMessengerPosition() {
        return messengerposition;
    }

    public MapleGuildCharacter getMGC() {
        return mgc;
    }

    public MapleMiniGame getMiniGame() {
        return miniGame;
    }

    public int getMiniGamePoints(String type, boolean omok) {
        if (omok) {
            if (type.equals("wins")) {
                return omokwins;
            } else if (type.equals("losses")) {
                return omoklosses;
            } else {
                return omokties;
            }
        } else {
            if (type.equals("wins")) {
                return matchcardwins;
            } else if (type.equals("losses")) {
                return matchcardlosses;
            } else {
                return matchcardties;
            }
        }
    }

    public MonsterBook getMonsterBook() {
        return monsterbook;
    }

    public int getMonsterBookCover() {
        return bookCover;
    }

    public MapleMount getMount() {
        return maplemount;
    }

    public int getMp() {
        return mp;
    }

    public MapleMessenger getMessenger() {
        return messenger;
    }

    public String getName() {
        return name;
    }

    public int getNextEmptyPetIndex() {
        for (int i = 0; i < 3; i++) {
            if (pets[i] == null) {
                return i;
            }
        }
        return 3;
    }

    public int getNoPets() {
        int ret = 0;
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                ret++;
            }
        }
        return ret;
    }

    public int getNumControlledMonsters() {
        return controlled.size();
    }

    public MapleParty getParty() {
        return party;
    }

    public int getPartyId() {
        return (party != null ? party.getId() : -1);
    }

    public MaplePet getPet(int index) {
        return pets[index];
    }

    public int getPetIndex(int petId) {
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getUniqueId() == petId) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int getPetIndex(MaplePet pet) {
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getUniqueId() == pet.getUniqueId()) {
                    return i;
                }
            }
        }
        return -1;
    }

    public MaplePlayerShop getPlayerShop() {
        return playerShop;
    }

    public MaplePet[] getPets() {
        return pets;
    }

    public int getPossibleReports() {
        return possibleReports;
    }

    public MapleQuestStatus getQuest(MapleQuest quest) {
        if (!quests.containsKey(quest)) {
            return new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
        }
        return quests.get(quest);
    }

    public int getRank() {
        return rank;
    }

    public int getRankMove() {
        return rankMove;
    }

    public int getRemainingAp() {
        return remainingAp;
    }

    public int getRemainingSp() {
        return remainingSp;
    }

    public int getSavedLocation(String type) {
        int m = savedLocations[SavedLocationType.fromString(type).ordinal()].getMapId();
        if (!SavedLocationType.fromString(type).equals(SavedLocationType.WORLDTOUR)) {
            clearSavedLocation(SavedLocationType.fromString(type));
        }
        return m;
    }

    public String getSearch() {
        return search;
    }

    public MapleShop getShop() {
        return shop;
    }

    public Map<ISkill, SkillEntry> getSkills() {
        return Collections.unmodifiableMap(skills);
    }

    public int getSkillLevel(int skill) {
        SkillEntry ret = skills.get(SkillFactory.getSkill(skill));
        if (ret == null) {
            return 0;
        }
        return ret.skillevel;
    }

    public int getSkillLevel(ISkill skill) {
        if (skills.get(skill) == null) {
            return 0;
        }
        return skills.get(skill).skillevel;
    }

    public MapleSkinColor getSkinColor() {
        return skinColor;
    }

    public final List<MapleQuestStatus> getStartedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<MapleQuestStatus>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
                ret.add(q);
            }
        }
        return Collections.unmodifiableList(ret);
    }

    public MapleStatEffect getStatForBuff(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.effect;
    }

    public MapleStorage getStorage() {
        return storage;
    }

    public int getStr() {
        return str;
    }

    public Map<Integer, MapleSummon> getSummons() {
        return summons;
    }

    public List<Integer> getTeleportRockMaps(int type) {
        if (type == 0) {
            return rockMaps;
        } else {
            return vipRockMaps;
        }
    }

    public int getTotalCp() {
        return totalCp;
    }

    public int getTotalLuk() {
        return localluk;
    }

    public int getTotalMagic() {
        return magic;
    }

    public int getTotalWatk() {
        return watk;
    }

    public MapleTrade getTrade() {
        return trade;
    }

    public int getVanquisherKills() {
        return vanquisherKills;
    }

    public int getVanquisherStage() {
        return vanquisherStage;
    }

    public Collection<MapleMapObject> getVisibleMapObjects() {
        return Collections.unmodifiableCollection(visibleMapObjects);
    }

    public List<Integer> getWishList() {
        return wishList;
    }

    public int getWorld() {
        return world;
    }

    public void giveCoolDowns(final int skillid, long starttime, long length) {
        int time = (int) ((length + starttime) - System.currentTimeMillis());
        addCooldown(skillid, System.currentTimeMillis(), time, TimerManager.getInstance().schedule(new CancelCooldownAction(this, skillid), time));
    }

    public void giveDebuff(MapleDisease disease, MobSkill skill) {
        if (diseases.size() < 4) {
            List<Pair<MapleDisease, Integer>> disease_ = new ArrayList<Pair<MapleDisease, Integer>>();
            disease_.add(new Pair<MapleDisease, Integer>(disease, Integer.valueOf(skill.getX())));
            this.diseases.add(disease);
            client.getSession().write(MaplePacketCreator.giveDebuff(disease_, skill));
            getMap().broadcastMessage(this, MaplePacketCreator.giveForeignDebuff(this.id, disease_, skill), false);
        }
    }

    public int gmLevel() {
        return gmLevel;
    }

    public boolean gotPartyQuestItem(String partyquestchar) {
        return partyquestitems.contains(partyquestchar);
    }

    public String guildCost() {
        return nf.format(MapleGuild.CREATE_GUILD_COST);
    }

    private void guildUpdate() {
        if (this.guildid < 1) {
            return;
        }
        mgc.setLevel(level);
        mgc.setJobId(job.getId());
        try {
            this.client.getChannelServer().getWorldInterface().memberLevelJobUpdate(this.mgc);
            int allianceId = getGuild().getAllianceId();
            if (allianceId > 0) {
                client.getChannelServer().getWorldInterface().allianceMessage(allianceId, MaplePacketCreator.updateAllianceJobLevel(this), getId(), -1);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void handleEnergyChargeGain() { // to get here energychargelevel has to be > 0
        ISkill energycharge = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.ENERGY_CHARGE) : SkillFactory.getSkill(Marauder.ENERGY_CHARGE);
        MapleStatEffect ceffect = null;
        ceffect = energycharge.getEffect(getSkillLevel(energycharge));
        TimerManager tMan = TimerManager.getInstance();
        if (energybar < 10000) {
            energybar += 102;
            if (energybar > 10000) {
                energybar = 10000;
            }
            client.getSession().write(MaplePacketCreator.giveEnergyCharge(energybar));
            client.getSession().write(MaplePacketCreator.showOwnBuffEffect(energycharge.getId(), 2));
            getMap().broadcastMessage(this, MaplePacketCreator.showBuffeffect(this, id, energycharge.getId(), 2));
            if (energybar == 10000) {
                getMap().broadcastMessage(this, MaplePacketCreator.giveForeignEnergyCharge(id, energybar));
            }
        }
        if (energybar >= 10000) {
            energybar = 15000;
            final MapleCharacter chr = this;
            tMan.schedule(new Runnable() {

                @Override
                public void run() {
                    client.getSession().write(MaplePacketCreator.giveEnergyCharge(0));
                    getMap().broadcastMessage(chr, MaplePacketCreator.giveForeignEnergyCharge(id, energybar));
                    energybar = 0;
                }
            }, ceffect.getDuration());
        }
    }

    public void handleOrbconsume() {
        int skillid = isCygnus() ? DawnWarrior.COMBO : Crusader.COMBO;
        ISkill combo = SkillFactory.getSkill(skillid);
        List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, 1));
        setBuffedValue(MapleBuffStat.COMBO, 1);
        client.getSession().write(MaplePacketCreator.giveBuff(skillid, combo.getEffect(getSkillLevel(combo)).getDuration() + (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis())), stat, false));
        getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(getId(), skillid, stat, false), false);
    }

    public boolean hasEntered(String script) {
        for (int mapId : entered.keySet()) {
            if (entered.get(mapId).equals(script)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEntered(String script, int mapId) {
        if (entered.containsKey(mapId)) {
            if (entered.get(mapId).equals(script)) {
                return true;
            }
        }
        return false;
    }

    public void hasGivenFame(MapleCharacter to) {
        lastfametime = System.currentTimeMillis();
        lastmonthfameids.add(Integer.valueOf(to.getId()));
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO famelog (characterid, characterid_to) VALUES (?, ?)");
            ps.setInt(1, getId());
            ps.setInt(2, to.getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
        }
    }

    public boolean hasMerchant() {
        return hasMerchant;
    }

    public boolean hasWatchedCygnusIntro() {
        return watchedCygnusIntro;
    }

    public boolean haveItem(int itemid) {
        return getItemQuantity(itemid, false) > 0;
    }

    public boolean haveItemEquipped(int itemid) {
        if (getInventory(MapleInventoryType.EQUIPPED).findById(itemid) != null) {
            return true;
        }
        return false;
    }

    public void increaseCp(int amount) {
        this.cp += amount;
    }

    public void increaseTotalCp(int amount) {
        this.totalCp += amount;
    }

    public void increaseGivenRiceCakes(int amount) {
        this.givenRiceCakes += amount;
    }

    public void increaseGuildCapacity() { //hopefully nothing is null
        if (getMeso() < getGuild().getIncreaseGuildCost(getGuild().getCapacity())) {
            dropMessage(1, "You don't have enough mesos.");
            return;
        }
        try {
            client.getChannelServer().getWorldInterface().increaseGuildCapacity(guildid);
        } catch (RemoteException e) {
            client.getChannelServer().reconnectWorld();
            return;
        }
        gainMeso(-getGuild().getIncreaseGuildCost(getGuild().getCapacity()), true, false, false);
    }

    public boolean inCS() {
        return incs;
    }

    public boolean inMTS() {
        return inmts;
    }

    public boolean isActiveBuffedValue(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
                return true;
            }
        }
        return false;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public boolean isBuffFrom(MapleBuffStat stat, ISkill skill) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        if (mbsvh == null) {
            return false;
        }
        return mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skill.getId();
    }

    public boolean isCygnus() {
        return getJobType() == 1;
    }

    public boolean isAran() {
        return getJobType() == 2;
    }

    public boolean isGM() {
        return gmLevel > 0;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isMapObjectVisible(MapleMapObject mo) {
        return visibleMapObjects.contains(mo);
    }

    public boolean isPartyLeader() {
        return party.getLeader() == party.getMemberById(getId());
    }

    public void leaveMap() {
        controlled.clear();
        visibleMapObjects.clear();
        if (chair != 0) {
            chair = 0;
        }
        if (hpDecreaseTask != null) {
            hpDecreaseTask.cancel(false);
        }
    }

    public void levelUp(boolean takeexp) {
        ISkill improvingMaxHP = null;
        ISkill improvingMaxMP = null;
        int improvingMaxHPLevel = 0;
        int improvingMaxMPLevel = 0;
        if (isCygnus() && level < 70) {
            remainingAp++;
        }
        if (job.getId() / 100 % 10 > 0 || level > 10) {
            remainingAp += 5;
        } else {
            this.str += 5;
            this.updateSingleStat(MapleStat.STR, str);
        }
        int jobtype = job.getId() / 100 % 10;
        if (jobtype == 0) {
            maxhp += rand(12, 16);
            maxmp += rand(10, 12);
        } else if (jobtype == 1) {
            if (isAran()) {
                maxhp += rand(44, 48);
                maxmp += rand(9, 10);
            } else {
                improvingMaxHP = isCygnus() ? SkillFactory.getSkill(DawnWarrior.MAX_HP_INCREASE) : SkillFactory.getSkill(Swordsman.IMPROVED_MAX_HP_INCREASE);
                if (job.isA(MapleJob.CRUSADER)) {
                    improvingMaxMP = SkillFactory.getSkill(1210000);
                } else if (job.isA(MapleJob.DAWNWARRIOR2)) {
                    improvingMaxMP = SkillFactory.getSkill(11110000);
                }
                improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
                maxhp += rand(24, 28);
                maxmp += rand(4, 6);
            }
        } else if (jobtype == 2) {
            improvingMaxMP = isCygnus() ? SkillFactory.getSkill(BlazeWizard.INCREASING_MAX_MP) : SkillFactory.getSkill(Magician.IMPROVED_MAX_MP_INCREASE);
            improvingMaxMPLevel = getSkillLevel(improvingMaxMP);
            maxhp += rand(10, 14);
            maxmp += rand(22, 24);
        } else if (jobtype <= 4) {
            maxhp += rand(20, 24);
            maxmp += rand(14, 16);
        } else if (jobtype == 5) {
            improvingMaxHP = isCygnus() ? SkillFactory.getSkill(ThunderBreaker.IMPROVE_MAX_HP) : SkillFactory.getSkill(5100000);
            improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
            maxhp += rand(22, 28);
            maxmp += rand(18, 23);
        } else if (jobtype == 9) {
            maxhp = 99999;
            maxmp = 99999;
        }
        if (improvingMaxHPLevel > 0 && (jobtype == 1 || job.isA(MapleJob.PIRATE))) {
            maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getX();
        }
        if (improvingMaxMPLevel > 0 && (jobtype == 2 || job.isA(MapleJob.CRUSADER))) {
            maxmp += improvingMaxMP.getEffect(improvingMaxMPLevel).getX();
        }
        maxmp += localint_ / 10;
        if (takeexp) {
            exp.addAndGet(-ExpTable.getExpNeededForLevel(level));
            if (exp.get() < 0) {
                exp.set(0);
            }
        }
        level++;
        if (level >= getMaxLevel()) {
            exp.set(0);
        }
        maxhp = Math.min(99999, maxhp);
        maxmp = Math.min(99999, maxmp);
        if (level == 200) {
            exp.set(0);
        }
        hp = maxhp;
        mp = maxmp;
        recalcLocalStats();
        List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(8);
        statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, remainingAp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.HP, localmaxhp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MP, localmaxmp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.EXP, exp.get()));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.LEVEL, level));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, maxhp));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, maxmp));
        if (jobtype > 0) {
            if(MapleJob.isExtendSPJob(job))
            {
                SPTable.addSPFromJobID(job.getId(), 3);
            } else {
                remainingSp += 3;
            }
            statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLESP, remainingSp)); //updatePlayerStats forces SPTable update, so this is fine
        }
        client.getSession().write(MaplePacketCreator.updatePlayerStats(statup, false, MapleJob.isExtendSPJob(job), this.SPTable));
        getMap().broadcastMessage(this, MaplePacketCreator.showForeignEffect(getId(), 0), false);
        if(level % 10 == 0 && job.isAnEvan())
            evanAdvance();
        recalcLocalStats();
        silentPartyUpdate();
        guildUpdate();
        if (this.guildid > 0) { // to do, make it not show to self
            this.getGuild().broadcast(MaplePacketCreator.serverNotice(5, "[Guild] " + name + " has reached Lv. " + level + "."));
        }
        if (level == 200) {
            client.getChannelServer().broadcastPacket(MaplePacketCreator.serverNotice(6, "[Congrats] " + name + " has reached Level 200! Congratulate " + name + " on such an amazing achievement!"));
        }
    }

    public static MapleCharacter loadCharFromDB(int charid, MapleClient client, boolean channelserver) throws SQLException {
        try {
            MapleCharacter ret = new MapleCharacter();
            ret.client = client;
            ret.id = charid;
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
            ps.setInt(1, charid);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                throw new RuntimeException("Loading char failed (not found)");
            }
            ret.name = rs.getString("name");
            ret.level = rs.getInt("level");
            ret.fame = rs.getInt("fame");
            ret.str = rs.getInt("str");
            ret.dex = rs.getInt("dex");
            ret.int_ = rs.getInt("int");
            ret.luk = rs.getInt("luk");
            ret.exp.set(rs.getInt("exp"));
            ret.hp = rs.getInt("hp");
            ret.maxhp = rs.getInt("maxhp");
            ret.mp = rs.getInt("mp");
            ret.maxmp = rs.getInt("maxmp");
            ret.hpMpApUsed = rs.getInt("hpMpUsed");
            ret.chartype = rs.getInt("charType");
            ret.hasMerchant = rs.getInt("HasMerchant") == 1;
            ret.remainingSp = rs.getInt("sp");
            ret.remainingAp = rs.getInt("ap");
            ret.meso.set(rs.getInt("meso"));
            ret.gmLevel = rs.getInt("gm");
            ret.skinColor = MapleSkinColor.getById(rs.getInt("skincolor"));
            ret.gender = rs.getInt("gender");
            ret.job = MapleJob.getById(rs.getInt("job"));
            ret.finishedDojoTutorial = rs.getInt("finishedDojoTutorial") == 1;
            ret.vanquisherKills = rs.getInt("vanquisherKills");
            ret.omokwins = rs.getInt("omokwins");
            ret.omoklosses = rs.getInt("omoklosses");
            ret.omokties = rs.getInt("omokties");
            ret.matchcardwins = rs.getInt("matchcardwins");
            ret.matchcardlosses = rs.getInt("matchcardlosses");
            ret.matchcardties = rs.getInt("matchcardties");
            ret.Beta = rs.getInt("beta") == 1;
            ret.receivedMOTB = rs.getInt("receivedMOTB") == 1;
            ret.hair = rs.getInt("hair");
            ret.face = rs.getInt("face");
            ret.accountid = rs.getInt("accountid");
            ret.mapid = rs.getInt("map");
            ret.initialSpawnPoint = rs.getInt("spawnpoint");
            ret.world = rs.getInt("world");
            ret.rank = rs.getInt("rank");
            ret.rankMove = rs.getInt("rankMove");
            ret.jobRank = rs.getInt("jobRank");
            ret.jobRankMove = rs.getInt("jobRankMove");
            int mountexp = rs.getInt("mountexp");
            int mountlevel = rs.getInt("mountlevel");
            int mounttiredness = rs.getInt("mounttiredness");
            ret.guildid = rs.getInt("guildid");
            ret.guildrank = rs.getInt("guildrank");
            ret.allianceRank = rs.getInt("allianceRank");
            ret.familyId = rs.getInt("familyId");
            ret.bookCover = rs.getInt("monsterbookcover");
            ret.monsterbook = new MonsterBook();
            ret.monsterbook.loadCards(charid);
            ret.watchedCygnusIntro = rs.getInt("watchedcygnusintro") == 1;
            ret.vanquisherStage = rs.getInt("vanquisherStage");
            ret.dojoPoints = rs.getInt("dojoPoints");
            ret.dojoStage = rs.getInt("lastDojoStage");
            ret.whitechat = ret.gmLevel > 0;
            ret.givenRiceCakes = rs.getInt("givenRiceCakes");
            ret.partyquestitems = rs.getString("partyquestitems");
            ret.itemEffect = rs.getInt("effectitem");
            ret.title = rs.getInt("titleitem");
            if (ret.guildid > 0) {
                ret.mgc = new MapleGuildCharacter(ret);
            }
            int buddyCapacity = rs.getInt("buddyCapacity");
            ret.buddylist = new BuddyList(buddyCapacity);

            for (byte i = 1; i <= 4; i++) {
                MapleInventoryType type = MapleInventoryType.getByType(i);
                ret.getInventory(type).setSlotLimit(rs.getInt(type.name().toLowerCase() + "slots"));
            }

            for (Pair<IItem, MapleInventoryType> item : ItemFactory.INVENTORY.loadItems(ret.id, !channelserver))
                ret.getInventory(item.getRight()).addFromDB(item.getLeft());

            if (channelserver) {
                MapleMapFactory mapFactory = client.getChannelServer().getMapFactory();
                ret.map = mapFactory.getMap(ret.mapid);
                if (ret.map == null) {
                    ret.map = mapFactory.getMap(100000000);
                }
                MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
                if (portal == null) {
                    portal = ret.map.getPortal(0);
                    ret.initialSpawnPoint = 0;
                }
                ret.setPosition(portal.getPosition());
                int partyid = rs.getInt("party");
                try {
                    MapleParty party = client.getChannelServer().getWorldInterface().getParty(partyid);
                    if (party != null) {
                        ret.party = party;
                    }
                } catch (RemoteException ex) {
                    client.getChannelServer().reconnectWorld();
                }
                int messengerid = rs.getInt("messengerid");
                int position = rs.getInt("messengerposition");
                if (messengerid > 0 && position < 4 && position > -1) {
                    try {
                        WorldChannelInterface wci = client.getChannelServer().getWorldInterface();
                        MapleMessenger messenger = wci.getMessenger(messengerid);
                        if (messenger != null) {
                            ret.messenger = messenger;
                            ret.messengerposition = position;
                        }
                    } catch (RemoteException ez) {
                        client.getChannelServer().reconnectWorld();
                    }
                }
                if(ret.job.isAnEvan() && ret.job.getId() != 2001)
                    ret.dragon = new MapleDragon(ret);
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT `name`, `level` FROM `characters` WHERE `accountid` = ? AND `id` <> ? ORDER BY `level` DESC LIMIT 1");
            ps.setInt(1, ret.accountid);
            ps.setInt(2, ret.id);
            rs = ps.executeQuery();
            if (rs.next()) {
                ret.linkedName = rs.getString("name");
                ret.linkedLevel = rs.getInt("level");
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT name, paypalNX, mPoints, cardNX, strikes, points FROM accounts WHERE id = ?");
            ps.setInt(1, ret.accountid);
            rs = ps.executeQuery();
            if (rs.next()) {
                ret.client.setAccountName(rs.getString("name"));
                ret.paypalnx = rs.getInt("paypalNX");
                ret.maplepoints = rs.getInt("mPoints");
                ret.cardnx = rs.getInt("cardNX");
                ret.strikes = rs.getInt("strikes");
                ret.points = rs.getInt("points");
            }
            rs.close();
            ps.close();
            if (channelserver) {
                ps = con.prepareStatement("SELECT * FROM queststatus WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                PreparedStatement pse = con.prepareStatement("SELECT * FROM queststatusmobs WHERE queststatusid = ?");
                while (rs.next()) {
                    MapleQuest q = MapleQuest.getInstance(rs.getInt("quest"));
                    MapleQuestStatus status = new MapleQuestStatus(q, MapleQuestStatus.Status.getById(rs.getInt("status")));
                    long cTime = rs.getLong("time");
                    if (cTime > -1) {
                        status.setCompletionTime(cTime * 1000);
                    }
                    status.setForfeited(rs.getInt("forfeited"));
                    ret.quests.put(q, status);
                    pse.setInt(1, rs.getInt("queststatusid"));
                    ResultSet rsMobs = pse.executeQuery();
                    while (rsMobs.next()) {
                        status.setMobKills(rsMobs.getInt("mob"), rsMobs.getInt("count"));
                    }
                    rsMobs.close();
                }
                rs.close();
                ps.close();
                pse.close();
                ps = con.prepareStatement("SELECT skillid,skilllevel,masterlevel FROM skills WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret.skills.put(SkillFactory.getSkill(rs.getInt("skillid")), new SkillEntry(rs.getInt("skilllevel"), rs.getInt("masterlevel")));
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT * FROM skillmacros WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    int position = rs.getInt("position");
                    SkillMacro macro = new SkillMacro(rs.getInt("skill1"), rs.getInt("skill2"), rs.getInt("skill3"), rs.getString("name"), rs.getInt("shout"), position);
                    ret.skillMacros[position] = macro;
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT `key`,`type`,`action` FROM keymap WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    int key = rs.getInt("key");
                    int type = rs.getInt("type");
                    int action = rs.getInt("action");
                    ret.keymap.put(Integer.valueOf(key), new MapleKeyBinding(type, action));
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT `locationtype`,`map`,`portal` FROM savedlocations WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret.savedLocations[SavedLocationType.valueOf(rs.getString("locationtype")).ordinal()] = new SavedLocation(rs.getInt("map"), rs.getInt("portal"));
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT mapId, type FROM telerockmaps WHERE characterId = ? ORDER BY type");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getInt("type") == 0) {
                        ret.rockMaps.add(Integer.valueOf(rs.getInt("mapid")));
                    } else {
                        ret.vipRockMaps.add(Integer.valueOf(rs.getInt("mapid")));
                    }
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT `characterid_to`,`when` FROM famelog WHERE characterid = ? AND DATEDIFF(NOW(),`when`) < 30");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                ret.lastfametime = 0;
                ret.lastmonthfameids = new ArrayList<Integer>(31);
                while (rs.next()) {
                    ret.lastfametime = Math.max(ret.lastfametime, rs.getTimestamp("when").getTime());
                    ret.lastmonthfameids.add(Integer.valueOf(rs.getInt("characterid_to")));
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT `sn` FROM wishlist WHERE `charid` = ?");
                ps.setInt(1, ret.id);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret.wishList.add(rs.getInt("sn"));
                }
                rs.close();
                ps.close();

                if(MapleJob.isExtendSPJob(ret.job) || ret.isGM()) {
                    ps = con.prepareStatement("SELECT * FROM extendedsp WHERE `characterid` = ?");
                    ps.setInt(1, ret.id);
                    rs = ps.executeQuery();
                    rs.last();
                    if(rs.getRow() == 0) //this will generally happen for GMs, who are forced to have an SP table but have existing characters without them.
                    {
                        PreparedStatement psx = con.prepareStatement("INSERT INTO extendedsp (characterid) values (?)");//0 by default <3
                        psx.setInt(1, charid);
                        psx.executeUpdate();
                        psx.close();
                        rs.first();
                        ret.SPTable = new ExtendedSPTable(ret.job.getId());
                        ret.insertSPTable = true;//forces SP table to be inserted on next save
                    } else {
                        HashMap<Integer, Integer> SPMap = new HashMap<Integer, Integer>();
                        rs.first();
                        for(int i = 1; i < 11; i++) {
                            SPMap.put(i, rs.getInt("job" + i));
                        }
                        ret.SPTable = new ExtendedSPTable(SPMap, ret.job.getId());
                    }
                    rs.close();
                    ps.close();
                    
                }
                ret.buddylist.loadFromDb(charid);
                ret.storage = MapleStorage.loadOrCreateFromDB(ret.accountid);
                ret.recalcLocalStats();
                ret.resetBattleshipHp();
                ret.silentEnforceMaxHpMp();
            } else {
                ret.SPTable = new ExtendedSPTable(2100);//bleh doesn't matter as long as it's empty
            }
            int mountid = ret.getJobType() * 10000000 + 1004;
            if (ret.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18) != null) {
                ret.maplemount = new MapleMount(ret, ret.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18).getItemId(), mountid);
            } else {
                ret.maplemount = new MapleMount(ret, 0, mountid);
            }
            ret.maplemount.setExp(mountexp);
            ret.maplemount.setLevel(mountlevel);
            ret.maplemount.setTiredness(mounttiredness);
            ret.maplemount.setActive(false);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String makeMapleReadable(String in) {
        String i = in.replace('I', 'i');
        i = i.replace('l', 'L');
        i = i.replace("rn", "Rn");
        i = i.replace("vv", "Vv");
        i = i.replace("VV", "Vv");
        return i;
    }

    private static class MapleBuffStatValueHolder {

        public MapleStatEffect effect;
        public long startTime;
        public int value;
        public ScheduledFuture<?> schedule;

        public MapleBuffStatValueHolder(MapleStatEffect effect, long startTime, ScheduledFuture<?> schedule, int value) {
            super();
            this.effect = effect;
            this.startTime = startTime;
            this.schedule = schedule;
            this.value = value;
        }
    }

    public static class MapleCoolDownValueHolder {

        public int skillId;
        public long startTime,  length;
        public ScheduledFuture<?> timer;

        public MapleCoolDownValueHolder(int skillId, long startTime, long length, ScheduledFuture<?> timer) {
            super();
            this.skillId = skillId;
            this.startTime = startTime;
            this.length = length;
            this.timer = timer;
        }
    }

    public void message(String m) {
        dropMessage(5, m);
    }

    public void mobKilled(int id) {
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus() == MapleQuestStatus.Status.COMPLETED || q.getQuest().canComplete(this, null)) {
                continue;
            }
            if (q.mobKilled(id)) {
                client.getSession().write(MaplePacketCreator.updateQuestMobKills(q));
                if (q.getQuest().canComplete(this, null)) {
                    client.getSession().write(MaplePacketCreator.getShowQuestCompletion(q.getQuest().getId()));
                }
            }
        }
    }

    public void modifyCSPoints(int type, int dx) {
        if (type == 1) {
            this.paypalnx += dx;
        } else if (type == 2) {
            this.maplepoints += dx;
        } else if (type == 4) {
            this.cardnx += dx;
        }
    }

    public void mount(int id, int skillid) {
        maplemount = new MapleMount(this, id, skillid);
    }

    public void offBeacon(boolean bf) {
        hasBeacon = false;
        beaconOid = -1;
        if (bf) {
            cancelEffectFromBuffStat(MapleBuffStat.HOMING_BEACON);
        }
    }

    /*
    private static int playerMap(int playerjob) {
    return playerjob > 1000 ? 13000100 : (playerjob > 500 ? 120000101 : (playerjob > 400 ? 103000003 : (playerjob > 300 ? 100000201 : (playerjob > 200 ? 102000003 : (playerjob > 0 ? 101000003 : 0)))));
    }*/
    public void playerNPC(MapleCharacter v, int scriptId) {
        int npcId;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT id FROM playernpcs WHERE ScriptId = ?");
            ps.setInt(1, scriptId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps = con.prepareStatement("INSERT INTO playernpcs (name, hair, face, skin, x, cy, map, ScriptId, Foothold, rx0, rx1) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                ps.setString(1, v.getName());
                ps.setInt(2, v.getHair());
                ps.setInt(3, v.getFace());
                ps.setInt(4, v.getSkinColor().getId());
                ps.setInt(5, getPosition().x);
                ps.setInt(6, getPosition().y);
                ps.setInt(7, getMapId());
                ps.setInt(8, scriptId);
                ps.setInt(9, getMap().getFootholds().findBelow(getPosition()).getId());
                ps.setInt(10, getPosition().x + 50);
                ps.setInt(11, getPosition().x - 50);
                ps.executeUpdate();
                rs = ps.getGeneratedKeys();
                rs.next();
                npcId = rs.getInt(1);
                ps.close();
                ps = con.prepareStatement("INSERT INTO playernpcs_equip (NpcId, equipid, equippos) VALUES (?, ?, ?)");
                ps.setInt(1, npcId);
                for (IItem equip : getInventory(MapleInventoryType.EQUIPPED)) {
                    int position = Math.abs(equip.getPosition());
                    if ((position < 12 && position > 0) || (position > 100 && position < 112)) {
                        ps.setInt(2, equip.getItemId());
                        ps.setInt(3, equip.getPosition());
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
                ps.close();
                rs.close();
                ps = con.prepareStatement("SELECT * FROM playernpcs WHERE ScriptId = ?");
                ps.setInt(1, scriptId);
                rs = ps.executeQuery();
                rs.next();
                PlayerNPCs pn = new PlayerNPCs(rs);
                for (ChannelServer channel : ChannelServer.getAllInstances()) {
                    MapleMap m = channel.getMapFactory().getMap(getMapId());
                    m.broadcastMessage(MaplePacketCreator.spawnPlayerNPC(pn));
                    m.broadcastMessage(MaplePacketCreator.getPlayerNPC(pn));
                    m.addMapObject(pn);
                }
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void playerDead() {
        cancelAllBuffs();
        dispelDebuffs();
        if (getEventInstance() != null) {
            getEventInstance().playerKilled(this);
        }
        int[] charmID = {5130000, 4031283, 4140903};
        int possesed = 0;
        int i;
        for (i = 0; i < charmID.length; i++) {
            int quantity = getItemQuantity(charmID[i], false);
            if (possesed == 0 && quantity > 0) {
                possesed = quantity;
                break;
            }
        }
        if (possesed > 0) {
            message("You have used a safety charm, so your EXP points have not been decreased.");
            MapleInventoryManipulator.removeById(client, MapleItemInformationProvider.getInstance().getInventoryType(charmID[i]), charmID[i], 1, true, false);
        } else if (mapid > 925020000 && mapid < 925030000) {
            this.dojoStage = 0;
        } else if (mapid > 980000000 && mapid < 980000604) {
            if (cp > 10) {
                cp -= 10;
            } else {
                cp = 0;
            }
        } else if (FieldLimit.CANNOTREGULAREXPLOSS.check(map.getId())) {
        } else if (getJob() != MapleJob.BEGINNER) {
            int XPdummy = ExpTable.getExpNeededForLevel(getLevel());
            if (getMap().isTown()) {
                XPdummy /= 100;
            }
            if (XPdummy == ExpTable.getExpNeededForLevel(getLevel())) {
                if (getLuk() <= 100 && getLuk() > 8) {
                    XPdummy *= (200 - getLuk()) / 2000;
                } else if (getLuk() <= 8) {
                    XPdummy /= 10;
                } else {
                    XPdummy /= 20;
                }
            }
        //        gainExp(getExp() > XPdummy ? -XPdummy : -getExp(), false, false);
        }
        if (getBuffedValue(MapleBuffStat.MORPH) != null) {
            cancelEffectFromBuffStat(MapleBuffStat.MORPH);
        }
        if (getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
            cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        }
        if (getChair() == -1) {
            setChair(0);
            client.getSession().write(MaplePacketCreator.cancelChair(-1));
            getMap().broadcastMessage(this, MaplePacketCreator.showChair(getId(), 0), false);
        }
        client.getSession().write(MaplePacketCreator.enableActions());
    }

    private void prepareDragonBlood(final MapleStatEffect bloodEffect) {
        if (dragonBloodSchedule != null) {
            dragonBloodSchedule.cancel(false);
        }
        dragonBloodSchedule = TimerManager.getInstance().register(new Runnable() {
            @Override
            public void run() {
                addHP(-bloodEffect.getX());
                client.getSession().write(MaplePacketCreator.showOwnBuffEffect(bloodEffect.getSourceId(), 5));
                getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(client.getPlayer(), getId(), bloodEffect.getSourceId(), 5), false);
                checkBerserk();
            }
        }, 4000, 4000);
    }

    private static int rand(int l, int u) {
        return Randomizer.getInstance().nextInt(u - l + 1) + l;
    }

    private void recalcLocalStats() {
        int oldmaxhp = localmaxhp;
        localmaxhp = getMaxHp();
        localmaxmp = getMaxMp();
        localdex = getDex();
        localint_ = getInt();
        localstr = getStr();
        localluk = getLuk();
        magic = localint_;
        watk = 0;
        for (IItem item : getInventory(MapleInventoryType.EQUIPPED)) {
            IEquip equip = (IEquip) item;
            localmaxhp += equip.getHp();
            localmaxmp += equip.getMp();
            localdex += equip.getDex();
            localint_ += equip.getInt();
            localstr += equip.getStr();
            localluk += equip.getLuk();
            magic += equip.getMatk() + equip.getInt();
            watk += equip.getWatk();
        }
        magic = Math.min(magic, 2000);
        Integer hbhp = getBuffedValue(MapleBuffStat.HYPERBODYHP);
        if (hbhp != null) {
            localmaxhp += (hbhp.doubleValue() / 100) * localmaxhp;
        }
        Integer hbmp = getBuffedValue(MapleBuffStat.HYPERBODYMP);
        if (hbmp != null) {
            localmaxmp += (hbmp.doubleValue() / 100) * localmaxmp;
        }
        localmaxhp = Math.min(99999, localmaxhp);
        localmaxmp = Math.min(99999, localmaxmp);
        Integer watkbuff = getBuffedValue(MapleBuffStat.WATK);
        if (watkbuff != null) {
            watk += watkbuff.intValue();
        }
        if (job.isA(MapleJob.BOWMAN)) {
            ISkill expert = null;
            if (job.isA(MapleJob.MARKSMAN)) {
                expert = SkillFactory.getSkill(3220004);
            } else if (job.isA(MapleJob.BOWMASTER)) {
                expert = SkillFactory.getSkill(3120005);
            }
            if (expert != null) {
                int boostLevel = getSkillLevel(expert);
                if (boostLevel > 0) {
                    watk += expert.getEffect(boostLevel).getX();
                }
            }
        }
        Integer matkbuff = getBuffedValue(MapleBuffStat.MATK);
        if (matkbuff != null) {
            magic += matkbuff.intValue();
        }
        if (oldmaxhp != 0 && oldmaxhp != localmaxhp) {
            updatePartyMemberHP();
        }
    }

    public void receivePartyMemberHP() {
        if (party != null) {
            int channel = client.getChannel();
            for (MaplePartyCharacter partychar : party.getMembers()) {
                if (partychar.getMapid() == getMapId() && partychar.getChannel() == channel) {
                    MapleCharacter other = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(partychar.getName());
                    if (other != null) {
                        client.getSession().write(MaplePacketCreator.updatePartyMemberHP(other.getId(), other.getHp(), other.getCurrentMaxHp()));
                    }
                }
            }
        }
    }

    public void registerEffect(MapleStatEffect effect, long starttime, ScheduledFuture<?> schedule) {
        if (effect.isHide() && gmLevel > 0) {
            this.hidden = true;
            this.getClient().getSession().write(MaplePacketCreator.getGMEffect(16, (byte)1));
            getMap().broadcastNONGMMessage(this, MaplePacketCreator.removePlayerFromMap(id), false);
        /*} else if (effect.isDragonBlood()) {
            prepareDragonBlood(effect);*/
        } else if (effect.isBerserk()) {
            checkBerserk();
        } else if (effect.isBeholder()) {
            final int beholder = DarkKnight.BEHOLDER;
            if (beholderHealingSchedule != null) {
                beholderHealingSchedule.cancel(false);
            }
            if (beholderBuffSchedule != null) {
                beholderBuffSchedule.cancel(false);
            }
            ISkill bHealing = SkillFactory.getSkill(DarkKnight.AURA_OF_BEHOLDER);
            int bHealingLvl = getSkillLevel(bHealing);
            if (bHealingLvl > 0) {
                final MapleStatEffect healEffect = bHealing.getEffect(bHealingLvl);
                int healInterval = healEffect.getX() * 1000;
                beholderHealingSchedule = TimerManager.getInstance().register(new Runnable() {

                    @Override
                    public void run() {
                        addHP(healEffect.getHp());
                        client.getSession().write(MaplePacketCreator.showOwnBuffEffect(beholder, 2));
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), beholder, 5), true);
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showOwnBuffEffect(beholder, 2), false);
                    }
                }, healInterval, healInterval);
            }
            ISkill bBuff = SkillFactory.getSkill(DarkKnight.HEX_OF_BEHOLDER);
            if (getSkillLevel(bBuff) > 0) {
                final MapleStatEffect buffEffect = bBuff.getEffect(getSkillLevel(bBuff));
                int buffInterval = buffEffect.getX() * 1000;
                beholderBuffSchedule = TimerManager.getInstance().register(new Runnable() {

                    @Override
                    public void run() {
                        buffEffect.applyTo(MapleCharacter.this);
                        client.getSession().write(MaplePacketCreator.showOwnBuffEffect(beholder, 2));
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), beholder, (int) (Math.random() * 3) + 6), true);
                        getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(client.getPlayer(), getId(), beholder, 2), false);
                    }
                }, buffInterval, buffInterval);
            }
        }
        for (Pair<MapleBuffStat, Integer> statup : effect.getStatups()) {
            effects.put(statup.getLeft(), new MapleBuffStatValueHolder(effect, starttime, schedule, statup.getRight().intValue()));
        }
        recalcLocalStats();
    }

    public void removeAllCooldownsExcept(int id) {
        for (MapleCoolDownValueHolder mcvh : coolDowns.values()) {
            if (mcvh.skillId != id) {
                coolDowns.remove(mcvh.skillId);
            }
        }
    }

    public static void removeAriantRoom(int room) {
        ariantroomleader[room] = "";
        ariantroomslot[room] = 0;
    }

    public void removeBuffStat(MapleBuffStat effect) {
        effects.remove(effect);
    }

    public void removeCooldown(int skillId) {
        if (this.coolDowns.containsKey(skillId)) {
            this.coolDowns.remove(skillId);
        }
    }

    public void removeDisease(MapleDisease disease) {
        synchronized (diseases) {
            if (diseases.contains(disease)) {
                diseases.remove(disease);
            }
        }
    }

    public void removePartyQuestItem(String letter) {
        partyquestitems = partyquestitems.substring(0, partyquestitems.indexOf(letter)) + partyquestitems.substring(partyquestitems.indexOf(letter) + 1);
    }

    public void removePet(MaplePet pet, boolean shift_left) {
        int slot = -1;
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getUniqueId() == pet.getUniqueId()) {
                    pets[i] = null;
                    slot = i;
                    break;
                }
            }
        }
        if (shift_left) {
            if (slot > -1) {
                for (int i = slot; i < 3; i++) {
                    if (i != 2) {
                        pets[i] = pets[i + 1];
                    } else {
                        pets[i] = null;
                    }
                }
            }
        }
    }

    public void removeVisibleMapObject(MapleMapObject mo) {
        visibleMapObjects.remove(mo);
    }

    public void resetBattleshipHp() {
        this.battleshipHp = 4000 * getSkillLevel(SkillFactory.getSkill(Corsair.BATTLE_SHIP)) + ((getLevel() - 120) * 2000);
    }

    public void resetEnteredScript() {
        if (entered.containsKey(map.getId())) {
            entered.remove(map.getId());
        }
    }

    public void resetEnteredScript(int mapId) {
        if (entered.containsKey(mapId)) {
            entered.remove(mapId);
        }
    }

    public void resetEnteredScript(String script) {
        for (int mapId : entered.keySet()) {
            if (entered.get(mapId).equals(script)) {
                entered.remove(mapId);
            }
        }
    }

    public void resetMGC() {
        this.mgc = null;
    }

    public void saveCooldowns() {
        if (getAllCooldowns().size() > 0) {
            try {
                PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO cooldowns (charid, SkillID, StartTime, length) VALUES (?, ?, ?, ?)");
                ps.setInt(1, getId());
                for (PlayerCoolDownValueHolder cooling : getAllCooldowns()) {
                    ps.setInt(2, cooling.skillId);
                    ps.setLong(3, cooling.startTime);
                    ps.setLong(4, cooling.length);
                    ps.addBatch();
                }
                ps.executeBatch();
                ps.close();
            } catch (SQLException se) {
            }
        }
    }

    public void saveGuildStatus() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET guildid = ?, guildrank = ?, allianceRank = ? WHERE id = ?");
            ps.setInt(1, guildid);
            ps.setInt(2, guildrank);
            ps.setInt(3, allianceRank);
            ps.setInt(4, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
        }
    }

    public void saveLocation(String type) {
        MaplePortal closest = map.findClosestPortal(getPosition());
        savedLocations[SavedLocationType.fromString(type).ordinal()] = new SavedLocation(getMapId(), closest != null ? closest.getId() : 0);
    }

    public void saveToDB(boolean update) {
        if((update) && this.trade != null)
            return; //t
        Connection con = DatabaseConnection.getConnection();
        try {
            con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            con.setAutoCommit(false);
            PreparedStatement ps;
            if (update) {
                ps = con.prepareStatement("UPDATE characters SET level = ?, fame = ?, str = ?, dex = ?, luk = ?, `int` = ?, exp = ?, hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, gm = ?, skincolor = ?, gender = ?, job = ?, hair = ?, face = ?, map = ?, meso = ?, hpMpUsed = ?, spawnpoint = ?, party = ?, buddyCapacity = ?, messengerid = ?, messengerposition = ?, mountlevel = ?, mountexp = ?, mounttiredness= ?, equipslots = ?, useslots = ?, setupslots = ?, etcslots = ?,  monsterbookcover = ?, watchedcygnusintro = ?, vanquisherStage = ?, dojoPoints = ?, lastDojoStage = ?, finishedDojoTutorial = ?, vanquisherKills = ?, matchcardwins = ?, matchcardlosses = ?, matchcardties = ?, omokwins = ?, omoklosses = ?, omokties = ?, givenRiceCakes = ?, partyquestitems = ?, effectitem = ?, titleitem = ?, receivedMOTB = ? WHERE id = ?");
            } else {
                ps = con.prepareStatement("INSERT INTO characters (level, fame, str, dex, luk, `int`, exp, hp, mp, maxhp, maxmp, sp, ap, gm, skincolor, gender, job, hair, face, map, meso, hpMpUsed, spawnpoint, party, buddyCapacity, messengerid, messengerposition, mountlevel, mounttiredness, mountexp, equipslots, useslots, setupslots, etcslots, monsterbookcover, watchedcygnusintro, vanquisherStage, dojopoints, lastDojoStage, finishedDojoTutorial, vanquisherKills, matchcardwins, matchcardlosses, matchcardties, omokwins, omoklosses, omokties, givenRiceCakes, partyquestitems, effectitem, titleitem, accountid, name, world, beta) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            }
            if (gmLevel < 1 && level > 199) {
                ps.setInt(1, isCygnus() ? 120 : 200);
            } else {
                ps.setInt(1, level);
            }
            ps.setInt(2, fame);
            ps.setInt(3, str);
            ps.setInt(4, dex);
            ps.setInt(5, luk);
            ps.setInt(6, int_);
            ps.setInt(7, exp.get());
            ps.setInt(8, hp);
            ps.setInt(9, mp);
            ps.setInt(10, maxhp);
            ps.setInt(11, maxmp);
            ps.setInt(12, remainingSp);
            ps.setInt(13, remainingAp);
            ps.setInt(14, gmLevel);
            ps.setInt(15, skinColor.getId());
            ps.setInt(16, gender);
            ps.setInt(17, job.getId());
            ps.setInt(18, hair);
            ps.setInt(19, face);
            if ((map == null) && (mapid == -1)) { //ie no data for map (shouldn't happen)
                ps.setInt(20, 0);
            } else if (map == null)//ie mapid set at createchar
            {
                ps.setInt(20, mapid);
            } else if (map.getForcedReturnId() != 999999999) {
                ps.setInt(20, map.getForcedReturnId());
            } else {
                ps.setInt(20, map.getId());
            }
            ps.setInt(21, meso.get());
            ps.setInt(22, hpMpApUsed);
            if (map == null || map.getId() == 610020000 || map.getId() == 610020001) {
                ps.setInt(23, 0);
            } else {
                MaplePortal closest = map.findClosestSpawnpoint(getPosition());
                if (closest != null) {
                    ps.setInt(23, closest.getId());
                } else {
                    ps.setInt(23, 0);
                }
            }
            ps.setInt(24, party != null ? party.getId() : -1);
            ps.setInt(25, buddylist.getCapacity());
            if (messenger != null) {
                ps.setInt(26, messenger.getId());
                ps.setInt(27, messengerposition);
            } else {
                ps.setInt(26, 0);
                ps.setInt(27, 4);
            }
            if (maplemount != null) {
                ps.setInt(28, maplemount.getLevel());
                ps.setInt(29, maplemount.getExp());
                ps.setInt(30, maplemount.getTiredness());
            } else {
                ps.setInt(28, 1);
                ps.setInt(29, 0);
                ps.setInt(30, 0);
            }
            for (int i = 31; i < 35; i++) {
                ps.setInt(i, getInventory(MapleInventoryType.getByType((byte) (i - 30))).getSlotLimit());
            }
            if (update) {
                monsterbook.saveCards(getId());
                try {
                    getFamily().save();
                } catch (NullPointerException npe) {
                }
            }
            ps.setInt(35, bookCover);
            ps.setInt(36, watchedCygnusIntro ? 1 : 0);
            ps.setInt(37, vanquisherStage);
            ps.setInt(38, dojoPoints);
            ps.setInt(39, dojoStage);
            ps.setInt(40, finishedDojoTutorial ? 1 : 0);
            ps.setInt(41, vanquisherKills);
            ps.setInt(42, matchcardwins);
            ps.setInt(43, matchcardlosses);
            ps.setInt(44, matchcardties);
            ps.setInt(45, omokwins);
            ps.setInt(46, omoklosses);
            ps.setInt(47, omokties);
            ps.setInt(48, givenRiceCakes);
            ps.setString(49, partyquestitems);
            ps.setInt(50, itemEffect);
            ps.setInt(51, title);
            if (update) {
                ps.setInt(52, receivedMOTB ? 1 : 0);
                ps.setInt(53, id);
            } else {
                ps.setInt(52, accountid);
                ps.setString(53, name);
                ps.setInt(54, world);
                ps.setInt(55, Beta ? 1 : 0);
            }
            int updateRows = ps.executeUpdate();
            if (!update) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    this.id = rs.getInt(1);
                } else {
                    throw new RuntimeException("Inserting char failed.");
                }
                rs.close();
            } else if (updateRows < 1) {
                throw new RuntimeException("Character not in database (" + id + ")");
            }
            for (int i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    pets[i].saveToDb();
                }
            }
            ps.close();
            deleteWhereCharacterId(con, "DELETE FROM keymap WHERE characterid = ?");
/*            ps = con.prepaffreStatement("INSERT INTO keymap (characterid, `key`, `type`, `action`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, id);
            for (Entry<Integer, MapleKeyBinding> keybinding : keymap.entrySet()) {
                ps.setInt(2, keybinding.getKey().intValue());
                ps.setInt(3, keybinding.getValue().getType());
                ps.setInt(4, keybinding.getValue().getAction());
                ps.addBatch();
            }
            ps.executeBatch();*/
            if(!keymap.isEmpty())
            {
                ps = con.prepareStatement(prepareKeymapQuery());
                ps.executeUpdate();
                ps.close();
            }

            deleteWhereCharacterId(con, "DELETE FROM skillmacros WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO skillmacros (characterid, skill1, skill2, skill3, name, shout, position) VALUES (?, ?, ?, ?, ?, ?, ?)");
            ps.setInt(1, getId());
            for (int i = 0; i < 5; i++) {
                SkillMacro macro = skillMacros[i];
                if (macro != null) {
                    ps.setInt(2, macro.getSkill1());
                    ps.setInt(3, macro.getSkill2());
                    ps.setInt(4, macro.getSkill3());
                    ps.setString(5, macro.getName());
                    ps.setInt(6, macro.getShout());
                    ps.setInt(7, i);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            ps.close();
            deleteWhereCharacterId(con, "DELETE FROM telerockmaps WHERE characterId = ?");
            ps = con.prepareStatement("INSERT into telerockmaps (characterId, mapId, type) VALUES (?, ?, ?)");
            ps.setInt(1, id);
            for (int mapId : rockMaps) {
                ps.setInt(2, mapId);
                ps.setInt(3, 0);
                ps.addBatch();
            }
            for (int mapId : vipRockMaps) {
                ps.setInt(2, mapId);
                ps.setInt(3, 1);
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
            List<Pair<IItem, MapleInventoryType>> itemsWithType = new ArrayList<Pair<IItem, MapleInventoryType>>();

            for (MapleInventory iv : inventory) {
                for (IItem item : iv.list())
                    itemsWithType.add(new Pair<IItem, MapleInventoryType>(item, iv.getType()));
            }

            ItemFactory.INVENTORY.saveItems(itemsWithType, id);
            deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?");
            /*
            ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel) VALUES (?, ?, ?, ?)");
            ps.setInt(1, id);
            for (Entry<ISkill, SkillEntry> skill : skills.entrySet()) {
                ps.setInt(2, skill.getKey().getId());
                ps.setInt(3, skill.getValue().skillevel);
                ps.setInt(4, skill.getValue().masterlevel);
                ps.addBatch();
            }
            ps.executeBatch();*/
            if(!skills.isEmpty())
            {
                ps = con.prepareStatement(prepareSkillQuery());
                ps.executeUpdate();
                ps.close();
            }
            deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?");
            if(savedLocations.length > 0)
            {
                ps = con.prepareStatement("INSERT INTO savedlocations (characterid, `locationtype`, `map`, `portal`) VALUES (?, ?, ?, ?)");
                ps.setInt(1, id);
                for (SavedLocationType savedLocationType : SavedLocationType.values()) {
                    if (savedLocations[savedLocationType.ordinal()] != null) {
                        ps.setString(2, savedLocationType.name());
                        ps.setInt(3, savedLocations[savedLocationType.ordinal()].getMapId());
                        ps.setInt(4, savedLocations[savedLocationType.ordinal()].getPortal());
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
                ps.close();
            }
            deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ? AND pending = 0");
            ps = con.prepareStatement("INSERT INTO buddies (characterid, `buddyid`, `pending`, `group`) VALUES (?, ?, 0, ?)");
            ps.setInt(1, id);
            for (BuddylistEntry entry : buddylist.getBuddies()) {
                if (entry.isVisible()) {
                    ps.setInt(2, entry.getCharacterId());
                    ps.setString(3, entry.getGroup());
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            ps.close();
            deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `forfeited`) VALUES (DEFAULT, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            PreparedStatement pse = con.prepareStatement("INSERT INTO queststatusmobs VALUES (DEFAULT, ?, ?, ?)");
            ps.setInt(1, id);
            for (MapleQuestStatus q : quests.values()) {
                ps.setInt(2, q.getQuest().getId());
                ps.setInt(3, q.getStatus().getId());
                ps.setInt(4, (int) (q.getCompletionTime() / 1000));
                ps.setInt(5, q.getForfeited());
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                for (int mob : q.getMobKills().keySet()) {
                    pse.setInt(1, rs.getInt(1));
                    pse.setInt(2, mob);
                    pse.setInt(3, q.getMobKills(mob));
                    pse.addBatch();
                }
                pse.executeBatch();
                rs.close();
            }
            pse.close();
            ps.close();
            ps = update ? con.prepareStatement("UPDATE accounts SET `paypalNX` = ?, `mPoints` = ?, `cardNX` = ?, gm = ?, strikes = ?, points = ? WHERE id = ?")
                    : con.prepareStatement("UPDATE accounts SET `paypalNX` = ?, `mPoints` = ?, `cardNX` = ?, gm = ?, strikes = ? WHERE id = ?");
            ps.setInt(1, paypalnx);
            ps.setInt(2, maplepoints);
            ps.setInt(3, cardnx);
            ps.setInt(4, gmLevel);
            ps.setInt(5, strikes);
            if(update)
            {
                ps.setInt(6, points);
                ps.setInt(7, client.getAccID());
            } else {
                ps.setInt(6, client.getAccID());
            }
            ps.executeUpdate();
            ps.close();
            if (storage != null) {
                storage.saveToDB();
            }
            ps = con.prepareStatement("DELETE FROM wishlist WHERE `charid` = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("INSERT INTO wishlist(`sn`, `charid`) VALUES(?, ?)");
            for (int sn : wishList) {
                ps.setInt(1, sn);
                ps.setInt(2, id);
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
            
            if(MapleJob.isExtendSPJob(job) || isGM()) {
                if(!update) {
                    ps = con.prepareStatement("INSERT INTO extendedsp (characterid) VALUES (?)");
                    ps.setInt(1, id);
                } else if (insertSPTable) {
                    ps = con.prepareStatement("INSERT INTO extendedsp (characterid, job1, job2, job3, job4, job5, " + "job6, job7, job8, job9, job10) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    ps.setInt(1, id);
                    for(int j = 1; j < 11; j++)
                    {
                      //  System.out.println("Adding SP for job slot " + j);
                        ps.setInt(j + 1, SPTable.getSPFromSlotID(j));
                    }
                    insertSPTable = false;
                } else
                {
                    ps = con.prepareStatement("UPDATE extendedsp SET job1 = ?, job2 = ?, job3 = ?, job4 = ?, job5 = ?, "
                            + "job6 = ?, job7 = ?, job8 = ?, job9 = ?, job10 = ? where characterid = ?");
                    for(int j = 1; j < 11; j++) {
                    //    System.out.println("Adding SP for job slot " + j);
                        ps.setInt(j, SPTable.getSPFromSlotID(j));
                    }
                    ps.setInt(11, id);
                }
                ps.executeUpdate();
                ps.close();
            }
            if (gmLevel > 0) {
                ps = con.prepareStatement("INSERT INTO gmlog (`cid`, `command`) VALUES (?, ?)");
                ps.setInt(1, id);
                for (String com : commands) {
                    ps.setString(2, com);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            ps.close();
            con.commit();
            ps = null;
        } catch (Exception e) {
            e.printStackTrace();
            PrimitiveLogger.logException(e);
            try {
                con.rollback();
            } catch (SQLException se) {
                PrimitiveLogger.logException(se);
            }
        } finally {
            try {
                con.setAutoCommit(true);
                con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            } catch (Exception e) {
                PrimitiveLogger.logException(e);
            }
        }
    }

    public void sendKeymap() {
        client.getSession().write(MaplePacketCreator.getKeymap(keymap));
    }

    public void sendMacros() {
        boolean macros = false;
        for (int i = 0; i < 5; i++) {
            if (skillMacros[i] != null) {
                macros = true;
            }
        }
        if (macros) {
            client.getSession().write(MaplePacketCreator.getMacros(skillMacros));
        }
    }

    public void sendNote(String to, String msg) throws SQLException {
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`) VALUES (?, ?, ?, ?)");
        ps.setString(1, to);
        ps.setString(2, this.getName());
        ps.setString(3, msg);
        ps.setLong(4, System.currentTimeMillis());
        ps.executeUpdate();
        ps.close();
    }

    public void setAllianceRank(int rank) {
        allianceRank = rank;
        if (mgc != null) {
            mgc.setAllianceRank(rank);
        }
    }

    public void setAllowWarpToId(int id) {
        this.warpToId = id;
    }

    public static void setAriantRoomLeader(int room, String charname) {
        ariantroomleader[room] = charname;
    }

    public static void setAriantSlotRoom(int room, int slot) {
        ariantroomslot[room] = slot;
    }

    public void setBattleshipHp(int battleshipHp) {
        this.battleshipHp = battleshipHp;
    }

    public void setBeacon(int oid) {
        beaconOid = oid;
    }

    public void setBuddyCapacity(int capacity) {
        buddylist.setCapacity(capacity);
        client.getSession().write(MaplePacketCreator.updateBuddyCapacity(capacity));
    }

    public void setBuffedValue(MapleBuffStat effect, int value) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return;
        }
        mbsvh.value = value;
    }

    public void setChair(int chair) {
        this.chair = chair;
    }

    public void setChalkboard(String text) {
        this.chalktext = text;
    }

    public void setDex(int dex) {
        this.dex = dex;
        recalcLocalStats();
    }

    public void setDojoEnergy(int x) {
        this.dojoEnergy = x;
    }

    public void setDojoParty(boolean b) {
        this.dojoParty = b;
    }

    public void setDojoPoints(int x) {
        this.dojoPoints = x;
    }

    public void setDojoStage(int x) {
        this.dojoStage = x;
    }

    public void setDojoStart() {
        this.dojoMap = map;
        int stage = (map.getId() / 100) % 100;
        this.dojoFinish = System.currentTimeMillis() + (stage > 36 ? 15 : stage / 6 + 5) * 60000;
    }

    public void setRates(boolean dispel) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT-8"));
        int hr = cal.get(Calendar.HOUR_OF_DAY);
        if ((haveItem(5360001) && hr > 6 && hr < 12) || (haveItem(5360002) && hr > 9 && hr < 15) || (haveItem(536000) && hr > 12 && hr < 18) || (haveItem(5360004) && hr > 15 && hr < 21) || (haveItem(536000) && hr > 18) || (haveItem(5360006) && hr < 5) || (haveItem(5360007) && hr > 2 && hr < 6) || (haveItem(5360008) && hr >= 6 && hr < 11)) {
            this.dropRate = 2 * client.getChannelServer().getDropRate();
            this.mesoRate = 2 * client.getChannelServer().getMesoRate();
        } else {
            this.dropRate = client.getChannelServer().getDropRate();
            this.mesoRate = client.getChannelServer().getMesoRate();
        }
        if ((haveItem(5211000) && hr > 17 && hr < 21) || (haveItem(5211014) && hr > 6 && hr < 12) || (haveItem(5211015) && hr > 9 && hr < 15) || (haveItem(5211016) && hr > 12 && hr < 18) || (haveItem(5211017) && hr > 15 && hr < 21) || (haveItem(5211018) && hr > 14) || (haveItem(5211039) && hr < 5) || (haveItem(5211042) && hr > 2 && hr < 8) || (haveItem(5211045) && hr > 5 && hr < 11) || haveItem(5211048)) {
            this.expRate = 2 * client.getChannelServer().getEXPRate();
        } else {
            this.expRate = client.getChannelServer().getEXPRate();
        }
        if (diseases.contains(MapleDisease.CURSE) && (!dispel)) {
            this.expRate *= 0.5;
            return;
        }
        if ((dispel) && (diseases.contains(MapleDisease.CURSE))) { //only double exp when curse is active
            this.expRate *= 2;
        }

    }

    public void setEnergyBar(int set) {
        energybar = set;
    }

    public void setEventInstance(EventInstanceManager eventInstance) {
        this.eventInstance = eventInstance;
    }

    public void setExp(int amount) {
        this.exp.set(amount);
    }

    public void setFace(int face) {
        this.face = face;
    }

    public void setFallCounter(int fallcounter) {
        this.fallcounter = fallcounter;
    }

    public void setFame(int fame) {
        this.fame = fame;
    }

    public void setFamilyId(int familyId) {
        this.familyId = familyId;
    }

    public void setFinishedDojoTutorial() {
        this.finishedDojoTutorial = true;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setGM(int level) {
        this.gmLevel = level;
    }

    public void setGottenRiceHat(boolean b) {
        this.gottenRiceHat = b;
    }

    public void setGuildId(int _id) {
        guildid = _id;
        if (guildid > 0) {
            if (mgc == null) {
                mgc = new MapleGuildCharacter(this);
            } else {
                mgc.setGuildId(guildid);
            }
        } else {
            mgc = null;
        }
    }

    public void setGuildRank(int _rank) {
        guildrank = _rank;
        if (mgc != null) {
            mgc.setGuildRank(_rank);
        }
    }

    public void setHair(int hair) {
        this.hair = hair;
    }

    public void setHasMerchant(boolean set) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET HasMerchant = ? WHERE id = ?");
            ps.setInt(1, set ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            return;
        }
        hasMerchant = set;
    }

    public void setHiredMerchant(HiredMerchant merchant) {
        this.hiredMerchant = merchant;
    }

    public void setHp(int newhp) {
        setHp(newhp, false);
    }

    public void setHp(int newhp, boolean silent) {
        int oldHp = hp;
        int thp = newhp;
        if (thp < 0) {
            thp = 0;
        }
        if (thp > localmaxhp) {
            thp = localmaxhp;
        }
        this.hp = thp;
        if (!silent) {
            updatePartyMemberHP();
        }
        if (oldHp > hp && !isAlive()) {
            playerDead();
        }
    }

    public void setHpMpApUsed(int mpApUsed) {
        this.hpMpApUsed = mpApUsed;
    }

    public void setHpMp(int x) {
        setHp(x);
        setMp(x);
        updateSingleStat(MapleStat.HP, hp);
        updateSingleStat(MapleStat.MP, mp);
    }

    public void setInCS(boolean b) {
        this.incs = b;
    }

    public void setInMTS(boolean b) {
        this.inmts = b;
    }

    public void setInt(int int_) {
        this.int_ = int_;
        recalcLocalStats();
    }

    public void setInventory(MapleInventoryType type, MapleInventory inv) {
        inventory[type.ordinal()] = inv;
    }

    public void setItemEffect(int itemEffect) {
        this.itemEffect = itemEffect;
    }
    
    public void setTitleItem(int itemId) {
        this.title = itemId;
    }

    public void setJob(MapleJob job) {
        this.job = job;
    }

    public void setLastHealed(long time) {
        this.lastHealed = time;
    }

    public void setLastUsedCashItem(long time) {
        this.lastUsedCashItem = time;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setLuk(int luk) {
        this.luk = luk;
        recalcLocalStats();
    }

    public void setMap(int PmapId) {
        this.mapid = PmapId;
    }

    public void setMap(MapleMap newmap) {
        this.map = newmap;
    }

    public String getMapName(int mapId) {
        return client.getChannelServer().getMapFactory().getMap(mapId).getMapName();
    }

    public void setMaxHp(int hp) {
        this.maxhp = hp;
        recalcLocalStats();
    }

    public void setMaxMp(int mp) {
        this.maxmp = mp;
        recalcLocalStats();
    }

    public void setMessenger(MapleMessenger messenger) {
        this.messenger = messenger;
    }

    public void setMessengerPosition(int position) {
        this.messengerposition = position;
    }

    public void setMiniGame(MapleMiniGame miniGame) {
        this.miniGame = miniGame;
    }

    public void setMiniGamePoints(MapleCharacter visitor, int winnerslot, boolean omok) {
        if (omok) {
            if (winnerslot == 1) {
                this.omokwins++;
                visitor.omoklosses++;
            } else if (winnerslot == 2) {
                visitor.omokwins++;
                this.omoklosses++;
            } else {
                this.omokties++;
                visitor.omokties++;
            }
        } else {
            if (winnerslot == 1) {
                this.matchcardwins++;
                visitor.matchcardlosses++;
            } else if (winnerslot == 2) {
                visitor.matchcardwins++;
                this.matchcardlosses++;
            } else {
                this.matchcardties++;
                visitor.matchcardties++;
            }
        }
    }

    public void setMonsterBookCover(int bookCover) {
        this.bookCover = bookCover;
    }

    public void setMp(int newmp) {
        int tmp = newmp;
        if (tmp < 0) {
            tmp = 0;
        }
        if (tmp > localmaxmp) {
            tmp = localmaxmp;
        }
        this.mp = tmp;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParty(MapleParty party) {
        this.party = party;
    }

    public void setPartyQuestItemObtained(String partyquestchar) {
        this.partyquestitems += partyquestchar;
    }

    public void setPlayerShop(MaplePlayerShop playerShop) {
        this.playerShop = playerShop;
    }

    public void setRemainingAp(int remainingAp) {
        this.remainingAp = remainingAp;
    }

    public void setRemainingSp(int remainingSp) {
        this.remainingSp = remainingSp;
    }

    public void setSearch(String find) {
        search = find;
    }

    public void setSkinColor(MapleSkinColor skinColor) {
        this.skinColor = skinColor;
    }

    public void setShop(MapleShop shop) {
        this.shop = shop;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public void setStr(int str) {
        this.str = str;
        recalcLocalStats();
    }

    public void setTrade(MapleTrade trade) {
        this.trade = trade;
    }

    public void setVanquisherKills(int x) {
        this.vanquisherKills = x;
    }

    public void setVanquisherStage(int x) {
        this.vanquisherStage = x;
    }

    public void setWatchedCygnusIntro(boolean set) {
        this.watchedCygnusIntro = set;
    }

    public void setWorld(int world) {
        this.world = world;
    }

    public void shiftPetsRight() {
        if (pets[2] == null) {
            pets[2] = pets[1];
            pets[1] = pets[0];
            pets[0] = null;
        }
    }

    public void showDojoClock() {
        int stage = (map.getId() / 100) % 100;
        long time;
        if (stage % 6 == 1) {
            time = (stage > 36 ? 15 : stage / 6 + 5) * 60;
        } else {
            time = (dojoFinish - System.currentTimeMillis()) / 1000;
        }
        if (stage % 6 > 0) {
            client.getSession().write(MaplePacketCreator.getClock((int) time));
        }
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                int clockid = (dojoMap.getId() / 100) % 100;
                if (dojoMap.getId() > clockid / 6 * 6 + 6 || dojoMap.getId() < clockid / 6 * 6) {
                    return;
                }
                client.getPlayer().changeMap(client.getChannelServer().getMapFactory().getMap(925020000));
            }
        }, time * 1000 + 100); // let the TIMES UP display for .1 seconds like in GMS
    }

    public void showNote() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM notes WHERE `to`=?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            rs.last();
            rs.first();
            client.getSession().write(MaplePacketCreator.showNotes(rs, rs.getRow()));
            rs.close();
            ps.close();
        } catch (SQLException e) {
        }
    }

    private void silentEnforceMaxHpMp() {
        setMp(getMp());
        setHp(getHp(), true);
    }

    public void silentGiveBuffs(List<PlayerBuffValueHolder> buffs) {
        for (PlayerBuffValueHolder mbsvh : buffs) {
            mbsvh.effect.silentApplyBuff(this, mbsvh.startTime);
        }
    }

    public void silentPartyUpdate() {
        if (party != null) {
            try {
                client.getChannelServer().getWorldInterface().updateParty(party.getId(), PartyOperation.SILENT_UPDATE, new MaplePartyCharacter(this));
            } catch (RemoteException e) {
                e.printStackTrace();
                client.getChannelServer().reconnectWorld();
            }
        }
    }

    public static class SkillEntry {

        public int skillevel,  masterlevel;

        public SkillEntry(int skillevel, int masterlevel) {
            this.skillevel = skillevel;
            this.masterlevel = masterlevel;
        }
    }

    public boolean skillisCooling(int skillId) {
        return coolDowns.containsKey(Integer.valueOf(skillId));
    }

    public void startCygnusIntro() {
        client.getSession().write(MaplePacketCreator.cygnusIntroDisableUI(true));
        client.getSession().write(MaplePacketCreator.cygnusIntroLock(true));
        saveLocation("CYGNUSINTRO");
        MapleMap introMap = client.getChannelServer().getMapFactory().getMap(913040000);
        changeMap(introMap, introMap.getPortal(0));
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                client.getSession().write(MaplePacketCreator.cygnusIntroDisableUI(false));
                client.getSession().write(MaplePacketCreator.cygnusIntroLock(false));
            }
        }, 54 * 1000);
        savedLocations[SavedLocationType.CYGNUSINTRO.ordinal()] = null;
    }

    public void startFullnessSchedule(final int decrease, final MaplePet pet, int petSlot) {
        ScheduledFuture<?> schedule = TimerManager.getInstance().register(new Runnable() {

            @Override
            public void run() {
                int newFullness = pet.getFullness() - decrease;
                if (newFullness <= 5) {
                    pet.setFullness(15);
                    pet.saveToDb();
                    unequipPet(pet, true, true);
                } else {
                    pet.setFullness(newFullness);
                    client.getSession().write(MaplePacketCreator.updatePet(pet));
                }
            }
        }, 60000, 60000);
        fullness[petSlot] = schedule;
    }

    public void startMapTimeLimitTask(final MapleMap from, final MapleMap to) {
        if (to.getTimeLimit() > 0 && from != null) {
            mapTimeLimitTask = TimerManager.getInstance().register(new Runnable() {

                @Override
                public void run() {
                    MaplePortal pfrom = null;
                    switch (from.getId()) {
                        case 100020000: // pig
                        case 105040304: // golem
                        case 105050100: // mushroom
                        case 221023400: // rabbit
                        case 240020500: // kentasaurus
                        case 240040511: // skelegons
                        case 240040520: // newties
                        case 260020600: // sand rats
                        case 261020300: // magatia
                            pfrom = from.getPortal("MD00");
                            break;
                        default:
                            pfrom = from.getPortal(0);
                    }
                    if (pfrom != null) {
                        MapleCharacter.this.changeMap(from, pfrom);
                    }
                }
            }, from.getTimeLimit() * 1000, from.getTimeLimit() * 1000);
        }
    }

    public void stopControllingMonster(MapleMonster monster) {
        controlled.remove(monster);
    }

    public void toggleGMChat() {
        whitechat = !whitechat;
    }

    public void unequipAllPets() {
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                unequipPet(pets[i], true);
            }
        }
    }

    public void unequipPet(MaplePet pet, boolean shift_left) {
        unequipPet(pet, shift_left, false);
    }

    public void unequipPet(MaplePet pet, boolean shift_left, boolean hunger) {
        if (this.getPet(this.getPetIndex(pet)) != null) {
            this.getPet(this.getPetIndex(pet)).saveToDb();
        }
        int petSlot = getPetIndex(pet);
        if (fullness[petSlot] != null) {
            fullness[petSlot].cancel(false);
            fullness[petSlot] = null;
        }
        getMap().broadcastMessage(this, MaplePacketCreator.showPet(this, pet, true, hunger), true);
        //updateSingleStat(MapleStat.PET, 0);
        client.getSession().write(MaplePacketCreator.petStatUpdate(this));
        client.getSession().write(MaplePacketCreator.enableActions());
        removePet(pet, shift_left);
    }

    public void updateMacros(int position, SkillMacro updateMacro) {
        skillMacros[position] = updateMacro;
    }

    public void updatePartyMemberHP() {
        if (party != null) {
            int channel = client.getChannel();
            for (MaplePartyCharacter partychar : party.getMembers()) {
                if (partychar.getMapid() == getMapId() && partychar.getChannel() == channel) {
                    MapleCharacter other = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(partychar.getName());
                    if (other != null) {
                        other.client.getSession().write(MaplePacketCreator.updatePartyMemberHP(getId(), this.hp, localmaxhp));
                    }
                }
            }
        }
    }

    public void updateQuest(MapleQuestStatus quest) {
        quests.put(quest.getQuest(), quest);
        if (quest.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
            client.getSession().write(MaplePacketCreator.startQuest(this, (short) quest.getQuest().getId()));
            client.getSession().write(MaplePacketCreator.updateQuestInfo(this, (short) quest.getQuest().getId(), quest.getNpc(), (byte) 8));
        } else if (quest.getStatus().equals(MapleQuestStatus.Status.COMPLETED)) {
            client.getSession().write(MaplePacketCreator.completeQuest(this, (short) quest.getQuest().getId()));
        } else if (quest.getStatus().equals(MapleQuestStatus.Status.NOT_STARTED)) {
            client.getSession().write(MaplePacketCreator.forfeitQuest(this, (short) quest.getQuest().getId()));
        }
    }

    public void updateSingleStat(MapleStat stat, int newval) {
        updateSingleStat(stat, newval, false);
    }

    private void updateSingleStat(MapleStat stat, int newval, boolean itemReaction) {
        client.getSession().write(MaplePacketCreator.updatePlayerStats(Collections.singletonList(new Pair<MapleStat, Integer>(stat, Integer.valueOf(newval))), itemReaction, MapleJob.isExtendSPJob(this.getJob()), this.SPTable));
    }

    @Override
    public int getObjectId() {
        return getId();
    }

    public MapleMapObjectType getType() {
        return MapleMapObjectType.PLAYER;
    }

    public void sendDestroyData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.removePlayerFromMap(this.getObjectId()));
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if ((this.isHidden() && client.getPlayer().isGM()) || !this.isHidden()) {
            client.getSession().write(MaplePacketCreator.spawnPlayerMapobject(this));
            for (int i = 0; pets[i] != null; i++) {
                client.getSession().write(MaplePacketCreator.showPet(this, pets[i], false, false));
            }
        }
    }

    @Override
    public void setObjectId(int id) {
    }

    @Override
    public String toString() {
        return name;
    }

    public CheatTracker getCheatTracker() {
        return anticheat;
    }

    public void InitiateSaveEvent() {
        periodicSaveTask = TimerManager.getInstance().register(new Runnable() {

            @Override
            public void run() {
                client.getPlayer().saveToDB(true);
            }
        }, 300000); // 5 mins
    }

    public boolean isBeta() {
        return this.Beta;
    }

    public boolean hasreceivedMOTB() {
        return this.receivedMOTB;
    }

    public void setreceivedMOTB(boolean received) {
        this.receivedMOTB = received;
        this.saveToDB(true);
    }

    public boolean allowedMapChange() {
        return this.allowMapChange;
    }

    public void setallowedMapChange(boolean allowed) {
        this.allowMapChange = allowed;
    }
    public int aranCombo;

    public int getCombo() {
        return aranCombo;
    }
    public int setCombo(int _new) {
        if (aranCombo %10 == 0)
            client.getSession().write(MaplePacketCreator.addComboBuff(_new));
        return aranCombo = _new;
    }

    private String prepareKeymapQuery()
    {
            StringBuilder query = new StringBuilder("INSERT INTO keymap (characterid, `key`, `type`, `action`) VALUES ");

            for (Iterator<Entry<Integer, MapleKeyBinding>> i = this.keymap.entrySet().iterator(); i.hasNext(); )
                    {
                        String entry = "";
                        Formatter itemEntry = new Formatter();
                        Entry<Integer, MapleKeyBinding> e = i.next();
                        itemEntry.format("(%d, %d, %d, %d)",
                                id, e.getKey().intValue(), e.getValue().getType(), e.getValue().getAction());
                        if (i.hasNext())
                            entry = itemEntry.toString() + ", ";
                        else
                            entry = itemEntry.toString();

                        query.append(entry);

                    }
      //      System.out.println(query);
             return query.toString();
    }

    private String prepareSkillQuery()
    {
            StringBuilder query = new StringBuilder("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel) VALUES ");

            for (Iterator<Entry<ISkill, SkillEntry>> i = this.skills.entrySet().iterator(); i.hasNext(); )
                    {
                        String entry = "";
                        Formatter itemEntry = new Formatter();
                        Entry<ISkill, SkillEntry> e = i.next();
                        itemEntry.format("(%d, %d, %d, %d)",
                                id, e.getKey().getId(), e.getValue().skillevel, e.getValue().masterlevel);
                        if (i.hasNext())
                            entry = itemEntry.toString() + ", ";
                        else
                            entry = itemEntry.toString();

                        query.append(entry);

                    }
         //   System.out.println(query);
             return query.toString();
    }
    public void setStuck(boolean isStuck)
    {
        this.stuck = isStuck;
    }

    public boolean isStuck()
    {
        return stuck;
    }

    public void empty()//scheduled tasks need to be cancelled, otherwise strong refs remain to this, and we all know what that means
    {
        this.cancelMapTimeLimitTask();
        this.cancelPeriodicSaveTask();
        this.cancelAllBuffs();
        this.anticheat.killInvalidationTask();
        this.anticheat = null;

        if(dragonBloodSchedule != null)
           dragonBloodSchedule.cancel(false);
        if(hpDecreaseTask != null)
            hpDecreaseTask.cancel(false);
        if(beholderHealingSchedule != null)
            beholderHealingSchedule.cancel(false);
        if(beholderBuffSchedule != null)
            beholderBuffSchedule.cancel(false);
        if(BerserkSchedule != null)
            BerserkSchedule.cancel(false);

        if(fullness != null)
        {
            for(ScheduledFuture<?> f : fullness)
                if(f != null)
                    f.cancel(false);
        }
        this.maplemount = null;
        this.dragon.setOwner(null);
        this.dragon = null;
        this.client = null; //refs need to be nulled from char -> client AND client -> char
    }

    public void setMegaLimit(long limit)
    {
        this.megaLimit = limit;
    }

    public long getMegaLimit()
    {
        return this.megaLimit;
    }

    public int updateMesosGetOverflow(int gain)
    {
        int origMesos = meso.get();
        int overflow = 0;
        if (((long) (origMesos) + gain) >= 2147483647L) { //no-op; they've reached max mesos
            overflow = ((origMesos + gain) - 2147483647);
            meso.set(2147483647);
            updateSingleStat(MapleStat.MESO, meso.get(), true);
        } else {
            updateSingleStat(MapleStat.MESO, meso.addAndGet(gain), true);
        }
            client.getSession().write(MaplePacketCreator.getShowMesoGain(gain, false));
            return overflow;
    }

    public int getPoints()
    {
        return this.points;
    }

    public void setPoints(int newPoints)
    {
        this.points = newPoints;
    }

    public MapleDragon getDragon()
    {
        return this.dragon;
    }

    public void setDragon(MapleDragon toSet)
    {
        this.dragon = toSet;
    }

    public String getMedalText()
    {
        String medal = "";
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        IItem medalItem = this.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -49);
        if (medalItem != null) {
            medal = "<" + ii.getName(medalItem.getItemId()) + "> ";
        }
        return medal;
    }

    public ExtendedSPTable getSPTable()
    {
        return SPTable;
    }

    public void updateDragon(boolean login)
    {
        if(job.isAnEvan() && job.getId() != 2001 && this.level >= 10)
        {
                if(this.dragon == null)
                    this.dragon = new MapleDragon(this);
                dragon.setPosition(this.getPosition());
                getMap().spawnDragon(dragon);
        }
    }

    public void evanAdvance()
    {
        boolean prevSP = true;
        switch (level)
        {
            case 10:
                this.changeJob(MapleJob.EVAN2);
                prevSP = false;
                break;
            case 20:
                this.changeJob(MapleJob.EVAN3);
                break;
            case 30:
                this.changeJob(MapleJob.EVAN4);
                break;
            case 40:
                this.changeJob(MapleJob.EVAN5);
                break;
            case 50:
                this.changeJob(MapleJob.EVAN6);
                break;
            case 60:
                this.changeJob(MapleJob.EVAN7);
                break;
            case 80:
                this.changeJob(MapleJob.EVAN8);
                break;
            case 100:
                this.changeJob(MapleJob.EVAN9);
                break;
            case 120:
                this.changeJob(MapleJob.EVAN10);
                break;
            case 160:
                this.changeJob(MapleJob.EVAN11);
                break;
            default:
                return;
        }
        if(prevSP)
            this.SPTable.addSPFromJobID(this.job.getId() - 1, 3);
        else
        {
            resetStats();
        }
        this.SPTable.addSPFromJobID(this.job.getId(), 3);
        this.updateSingleStat(MapleStat.AVAILABLESP, -1);//dummy
    }

    public void resetStats()
    {
            int totAp = getStr() + getDex() + getLuk() + getInt() + getRemainingAp();
            setStr(4);
            setDex(4);
            setLuk(4);
            setInt(4);
            setRemainingAp(totAp - 16);
            updateSingleStat(MapleStat.STR, 4);
            updateSingleStat(MapleStat.DEX, 4);
            updateSingleStat(MapleStat.LUK, 4);
            updateSingleStat(MapleStat.INT, 4);
            updateSingleStat(MapleStat.AVAILABLEAP, totAp);
    }

    public static int getGMLevelForCharacter(String charactername)
    {
        int ret = 0;
        try
        {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `gm` from `characters` WHERE `name` = ?");
            ps.setString(1, charactername);
            ResultSet rs = ps.executeQuery();
            rs.last();
            ret = rs.getInt("gm");
            rs.close();
            ps.close();
        } catch (Exception e) {
            PrimitiveLogger.logException(e);
        }
        return ret;
    }

    public EventTeam getEventTeam()
    {
        return this.team;
    }

    public void setEventTeam(EventTeam team)
    {
        this.team = team;
    }

    public int getCharType(){
        return chartype;
    }
}


