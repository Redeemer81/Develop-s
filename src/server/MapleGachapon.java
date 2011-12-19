/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server;
import java.util.ArrayList;
import client.IItem;
import client.Item;
/**
 *
 * @author Simon
 */
public class MapleGachapon {
    private int npcId;
    private ArrayList<MapleGachaponEntry> items = new ArrayList<MapleGachaponEntry>();

    public MapleGachapon(int npcId)
    {
        this.npcId = npcId;
    }

    public int getNPCId()
    {
        return npcId;
    }

    public IItem getRandomItem()
    {
        //lol this is the difficult bit

        return null;
    }

    public void addItem(int npcid, int itemid, int chance)
    {
        items.add(new MapleGachaponEntry(npcid, new Item(itemid, (byte)0, (short)1), chance));
    }
}
