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
package scripting.reactor;

import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import client.Equip;
import client.IItem;
import client.Item;
import client.MapleClient;
import client.MapleInventoryType;
import constants.ServerConstants;
import scripting.AbstractPlayerInteraction;
import server.MapleItemInformationProvider;
import server.life.MapleLifeFactory;
import server.life.MapleNPC;
import server.life.MapleMonsterInformationProvider.DropEntry;
import server.maps.MapleReactor;
import tools.MaplePacketCreator;
import server.maps.MapMonitor;

/**
 * @author Lerk
 */
public class ReactorActionManager extends AbstractPlayerInteraction {
    private MapleReactor reactor;
    private MapleClient c;

    public ReactorActionManager(MapleClient c, MapleReactor reactor) {
        super(c);
        this.reactor = reactor;
        this.c = c;
    }

    public void dropItems() {
        dropItems(false, 0, 0, 0, 0);
    }

    public void dropItems(boolean meso, int mesoChance, int minMeso, int maxMeso) {
        dropItems(meso, mesoChance, minMeso, maxMeso, 0);
    }

    public void dropItems(boolean meso, int mesoChance, int minMeso, int maxMeso, int minItems) {
        List<DropEntry> chances = getDropChances();
        List<DropEntry> items = new LinkedList<DropEntry>();
        int numItems = 0;
        if (meso && Math.random() < (1 / (double) mesoChance)) {
            items.add(new DropEntry(0, mesoChance));
        }
        Iterator<DropEntry> iter = chances.iterator();
        while (iter.hasNext()) {
            DropEntry d = iter.next();
            if (Math.random() < (1 / (double) d.chance)) {
                numItems++;
                items.add(d);
            }
        }
        while (items.size() < minItems) {
            items.add(new DropEntry(0, mesoChance));
            numItems++;
        }
        java.util.Collections.shuffle(items);
        final Point dropPos = reactor.getPosition();
        dropPos.x -= (12 * numItems);
        for (DropEntry d : items) {
            if (d.itemId == 0) {
                int range = maxMeso - minMeso;
                int displayDrop = (int) (Math.random() * range) + minMeso;
                int mesoDrop = (displayDrop * ServerConstants.MESO_RATE);
                final int dropperId = reactor.getId();
                final Point dropperPos = reactor.getPosition();
                reactor.getMap().spawnMesoDrop(mesoDrop, displayDrop, dropPos, dropperId, dropperPos, getPlayer(), meso);
            } else {
                IItem drop;
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                if (ii.getInventoryType(d.itemId) != MapleInventoryType.EQUIP) {
                    drop = new Item(d.itemId, (byte) 0, (short) 1);
                } else {
                    drop = ii.randomizeStats((Equip) ii.getEquipById(d.itemId));
                }
                reactor.getMap().spawnItemDrop(reactor.getObjectId(), reactor.getPosition(), getPlayer(), drop, dropPos, false, true);
            }
            dropPos.x += 25;

        }
    }

    private List<DropEntry> getDropChances() {
        return ReactorScriptManager.getInstance().getDrops(reactor.getId());
    }

    public void spawnMonster(int id) {
        spawnMonster(id, 1, getPosition());
    }

    public void spawnMonster(int id, int x, int y) {
        spawnMonster(id, 1, new Point(x, y));
    }

    public void createMapMonitor(String portalName, int portalMapId, String portalReactorName) {
        new MapMonitor(c.getChannelServer().getMapFactory(), reactor.getMap(), portalName, portalMapId, portalReactorName);
    }

    public void activateMapListener()
    {
        reactor.getMap().getListener().disableEntry();
    }

    public void spawnMonster(int id, int qty) {
        spawnMonster(id, qty, getPosition());
    }

    public void spawnMonster(int id, int qty, int x, int y) {
        spawnMonster(id, qty, new Point(x, y));
    }

    private void spawnMonster(int id, int qty, Point pos) {
        for (int i = 0; i < qty; i++) {
            reactor.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(id), pos);
        }
    }

    public Point getPosition() {
        Point pos = reactor.getPosition();
        pos.y -= 10;
        return pos;
    }

    public void spawnNpc(int npcId) {
        spawnNpc(npcId, getPosition());
    }

    public void spawnNpc(int npcId, Point pos) {
        MapleNPC npc = MapleLifeFactory.getNPC(npcId);
        if (npc != null) {
            npc.setPosition(pos);
            npc.setCy(pos.y);
            npc.setRx0(pos.x + 50);
            npc.setRx1(pos.x - 50);
            npc.setFh(reactor.getMap().getFootholds().findBelow(pos).getId());
            reactor.getMap().addMapObject(npc);
            reactor.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
        }
    }

    public MapleReactor getReactor() {
        return reactor;
    }

    public void spawnFakeMonster(int id) {
        reactor.getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), getPosition());
    }
}