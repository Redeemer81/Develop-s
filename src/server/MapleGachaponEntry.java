/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server;
import client.IItem;

/**
 *
 * @author Simon
 */
public class MapleGachaponEntry {
    private int npcid;
    private IItem item; //yeah we'll just clone this to give to people
    private int chance;

    public MapleGachaponEntry(int npcid, IItem item, int chance)
    {
        this.npcid = npcid;
        this.item = item;
        this.chance = chance;
    }

    public int getNPCId()
    {
        return npcid;
    }

    public IItem getItem()
    {
        return item;
    }

    public int getChance()
    {
        return chance;
    }
}
