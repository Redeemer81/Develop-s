/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.channel.handler;
import client.MapleClient;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.MaplePacketCreator;
import server.movement.LifeMovementFragment;
import java.util.List;
import server.maps.MapleDragon;

import java.awt.Point;
/**
 *
 * @author Simon
 */
public class DragonMoveHandler extends AbstractMovementPacketHandler {

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c)
    {
        Point startPos = new Point(slea.readShort(), slea.readShort());
        slea.readInt();
        List<LifeMovementFragment> res = parseMovement(slea);
        updatePosition(res, c.getPlayer().getDragon(), 0);
        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.moveDragon(c.getPlayer().getId(), startPos, res), false);

    }

}
