/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server.movement;
import java.awt.Point;
import tools.data.output.LittleEndianWriter;

/**
 *
 * @author Simon
 */
public class FlashJumpMovement extends AbstractLifeMovement {
    private Point pixelsPerSecond;

    public FlashJumpMovement(int type, Point position, int duration, int newstate) {
        super(type, position, duration, newstate);
    }

    public Point getPixelsPerSecond() {
        return pixelsPerSecond;
    }

    public void setPixelsPerSecond(Point wobble) {
        this.pixelsPerSecond = wobble;
    }

    @Override
    public void serialize(LittleEndianWriter lew) {
        lew.write(getType());
        lew.writeShort(getPosition().x);
        lew.writeShort(getPosition().y);
        lew.writeShort(pixelsPerSecond.x);
        lew.writeShort(pixelsPerSecond.y);
        lew.write(getNewstate());
        lew.writeShort(getDuration());
    }
}
