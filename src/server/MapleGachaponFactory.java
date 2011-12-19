/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server;
import java.util.LinkedHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import tools.DatabaseConnection;
import tools.PrimitiveLogger;
/**
 *
 * @author Simon
 */
public class MapleGachaponFactory {
    private static LinkedHashMap<Integer, MapleGachapon> gachaTable = new LinkedHashMap<Integer, MapleGachapon>();

    public static MapleGachapon getGachapon(int npcID)
    {
        synchronized(gachaTable)
        {
            if(gachaTable.containsKey(npcID))
                return gachaTable.get(npcID);
            MapleGachapon newGacha = new MapleGachapon(npcID);
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM `gachapon` WHERE `npcid` = ?");
                ps.setInt(1, npcID);
                ResultSet rs = ps.executeQuery();
                while(rs.next())
                {
                    newGacha.addItem(npcID, rs.getInt("itemid"), rs.getInt("chance"));
                }
                rs.close();
                ps.close();
            } catch (SQLException ex) {
                PrimitiveLogger.logException(ex);
            }
            return newGacha;
        }
    }
}
