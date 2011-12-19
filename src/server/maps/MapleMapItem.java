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
package server.maps;

import java.awt.Point;
import client.IItem;
import client.MapleCharacter;
import client.MapleClient;
import tools.MaplePacketCreator;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Matze
 */
public class MapleMapItem extends AbstractMapleMapObject {
    protected IItem item;
    protected MapleMapObject dropper;
    protected MapleCharacter owner;
    protected int meso;
    protected int displayMeso;
    protected int dropperId;
    protected Point dropperPos;
    protected boolean pickedUp = false;
    public ReentrantLock itemLock = new ReentrantLock();
    
    public MapleMapItem(IItem item, Point position, int _dropperId, Point _dropperPos, MapleCharacter owner) {
        setPosition(position);
        this.item = item;
        this.dropperId = _dropperId;
        this.dropperPos = _dropperPos;
        this.owner = owner;
        this.meso = 0;
    }

    public MapleMapItem(int meso, int displayMeso, Point position, int _dropperId, Point _dropperPos, MapleCharacter owner) {
        setPosition(position);
        this.item = null;
        this.meso = meso;
        this.displayMeso = displayMeso;
        this.dropperId = _dropperId;
        this.dropperPos = _dropperPos;
        this.owner = owner;
    }

    public IItem getItem() {
        return item;
    }

    public int getDropperId()
    {
        return this.dropperId;
    }

    public Point getDropperPos()
    {
        return this.dropperPos;
    }

    public MapleCharacter getOwner() {
        return owner;
    }

    public int getMeso() {
        return meso;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    public void setPickedUp(boolean pickedUp) {
        this.pickedUp = pickedUp;
        if (pickedUp)
        try {
            this.owner = null;
            super.finalize();
        } catch (Throwable t){}
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.removeItemFromMap(getObjectId(), 1, 0));
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.ITEM;
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (getMeso() > 0) {
            client.getSession().write(MaplePacketCreator.dropMesoFromMapObject(displayMeso, getObjectId(), this.dropperId, getOwner().getId(), null, getPosition(), (byte) 2));
        } else {
            client.getSession().write(MaplePacketCreator.dropItemFromMapObject(getItem().getItemId(), getObjectId(), 0, getOwner().getId(), null, getPosition(), (byte) 2));
        }
    }
}
