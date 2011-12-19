/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.channel.achievements;

import client.MapleCharacter;
import client.MapleClient;
import java.util.HashMap;
import java.util.Map;
import tools.MaplePacketCreator;

/**
 *
 * @author iGoofy
 */
public class Achievements {

    private int[] levelAchievements = {10, 20, 30, 35, 40, 50, 55, 60, 70, 75, 80, 85, 90, 120, 150, 200};
    private int[] levelMesos = {5000, 10000, 15000, 17500, 20000, 25000, 27000, 30000, 35000, 37000, 40000, 42500, 45000, 60000, 75000, 100000};
    private int[] levelNX = {1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 5000, 1000, 1000, 1000, 1000, 5000, 1000, 10000};

    public String getAchievementDesc(String which) {
        Map<String, String> achievement = new HashMap<String, String>();
        for (int i : levelAchievements) {
            achievement.put("Level " + i, "Reached level " + i);
        }
        achievement.put("1st job", "Gained the 1st job");
        achievement.put("2nd job", "Gained the 2nd job");
        achievement.put("3rd job", "Gained the 3rd job");
        return achievement.get(which);
    }

    public Integer getAchievementMesos(String which) {
        Map<String, Integer> achievement = new HashMap<String, Integer>();
        for (int i : levelAchievements) {
            achievement.put("Level " + i, levelMesos[i]);
        }
        achievement.put("1st Job", 10000);
        achievement.put("2nd Job", 50000);
        achievement.put("3rd Job", 100000);
        achievement.put("4th job", 1000000);

        return achievement.get(which);
    }

    public Integer getAchievementNX(String which) {
        Map<String, Integer> achievements = new HashMap<String, Integer>();
        for (int i : levelAchievements) {
            achievements.put("Level " + i, levelNX[i]);
        }
        achievements.put("1st job", 1000);
        achievements.put("2nd job", 5000);
        achievements.put("3rd job", 10000);
        achievements.put("4th job", 25000);

        return achievements.get(which);
    }

    public void gainAchievementMesos(MapleClient c, String achievement, boolean global) {
        MapleCharacter player = c.getPlayer();
        player.gainMeso(getAchievementMesos(achievement), false, true, true);
        if (!global) {
            player.dropMessage(5, "[Achievement!]" + "Description: " + getAchievementDesc(achievement) + " Reward: " + formatNumber(getAchievementMesos(achievement)) + " mesos.");
        } else {
            player.getClient().getChannelServer().broadcastPacket(MaplePacketCreator.serverNotice(0, "[Achievement!] Congratulations to " + player.getName() + "! Achievement: " + getAchievementDesc(achievement) + " Reward: " + formatNumber(getAchievementMesos(achievement)) + " mesos."));
        }
    }

    public void gainAchievementNX(MapleClient c, String achievement, boolean global) {
        MapleCharacter player = c.getPlayer();
        player.modifyCSPoints(1, getAchievementNX(achievement));
        if (!global) {
            player.dropMessage(5, "[Achievement!]" + "Description: " + getAchievementDesc(achievement) + " Reward: " + formatNumber(getAchievementNX(achievement)) + " NX Cash.");
        } else {
            player.getClient().getChannelServer().broadcastPacket(MaplePacketCreator.serverNotice(0, "[Achievement!] Congratulations to " + player.getName() + "! Achievement: " + getAchievementDesc(achievement) + " Reward: " + formatNumber(getAchievementNX(achievement)) + " NX cash."));
        }
    }

    //Credits: Howei from RaGEZONE
    private String formatNumber(int x) {
        //Format Number by Howei...took me awhile with my limited Java knowledge :( (I wrote in C++ first then translated)
        String before = Integer.toString(x);
        String result = "";
        int n = 0;
        for (int i = 0; i < before.length(); i++) {
            result = before.charAt((before.length() - 1) - i) + result;
            if (n == 2 && (i + 1) != before.length()) {
                result = "," + result;
                n = -1;
            }
            n++;
        }
        return result;
    }
}
