/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server.maps;
import client.MapleClient;
import client.MapleCharacter;
import java.awt.Point;
import tools.MaplePacketCreator;
/**
 *
 * @author Simon
 */
public class MapleDragon extends AbstractAnimatedMapleMapObject {
    private MapleCharacter owner;

    public MapleDragon(MapleCharacter owner, Point pos)
    {
        this.owner = owner;
        this.setPosition(pos);
    }

    public MapleDragon(MapleCharacter owner)
    {
        this.owner = owner;
    }

    @Override
    public MapleMapObjectType getType()
    {
        return MapleMapObjectType.DRAGON;
    }

    @Override
    public void sendSpawnData(MapleClient c)
    {
        c.getSession().write(MaplePacketCreator.spawnEvanDragon(this, owner.getId() == c.getPlayer().getId()));
    }

    @Override
    public void sendDestroyData(MapleClient c)
    {
        //TODO: destroy packets for dragon
    }

    public MapleCharacter getOwner()
    {
        return owner;
    }

    public void setOwner(MapleCharacter owner)
    {
        this.owner = owner;
    }

}
