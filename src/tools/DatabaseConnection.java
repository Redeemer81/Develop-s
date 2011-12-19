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
package tools;

import constants.ServerConstants;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;


public class DatabaseConnection {
private static final HashMap<Integer, ConWrapper> connections =
			new HashMap();
  //  private static String dbDriver, dbUrl, dbUser, dbPass;
	private static boolean propsInited = false;
	private static long connectionTimeOut = 2 * 60 * 1000; // 5 minutes

    private DatabaseConnection() {}

    public static Connection getConnection() {
		Thread cThread = Thread.currentThread();
		int threadID = (int) cThread.getId();
		ConWrapper ret = connections.get(threadID);

		if (ret == null) {
			Connection retCon = connectToDB();
			ret = new ConWrapper(retCon);
			ret.id = threadID;
			connections.put(threadID, ret);
		}

		return ret.getConnection();
    }

    private static long getWaitTimeout(Connection con) {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery("SHOW VARIABLES LIKE 'wait_timeout'");
			if (rs.next()) {
				return Math.max(1000, rs.getInt(2) * 1000 - 1000);
			} else {
				return -1;
			}
		} catch (SQLException ex) {
			return -1;
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ex) {
				} finally {
					if (rs != null) {
						try {
							rs.close();
						} catch (SQLException ex1) {}
					}
				}
			}
		}
	}

	private static Connection connectToDB() {
		if (!propsInited) {
			initializeProperties();
                }

		try {
			Class.forName("com.mysql.jdbc.Driver");    // touch the MySQL driver
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Connection con = DriverManager.getConnection(ServerConstants.DATABASE_URL, ServerConstants.DATABASE_USER, ServerConstants.DATABASE_PASS);
			if (!propsInited) {
				long timeout = getWaitTimeout(con);
				if (timeout == -1) {
					
				} else {
					connectionTimeOut = timeout;
				
				}
				propsInited = true;
			} 
			return con;
		} catch (SQLException e) {
			e.printStackTrace();
		}
                return null;
	}

	static class ConWrapper {
		private long lastAccessTime = 0;
		private Connection connection;
		private int id;

		public ConWrapper(Connection con) {
			this.connection = con;
		}

		public Connection getConnection() {
			if (expiredConnection()) {
				try { // Assume that the connection is stale
					connection.close();
				} catch (Throwable err) {
					// Who cares
				}
				this.connection = connectToDB();
			}

			lastAccessTime = System.currentTimeMillis(); // Record Access
			return this.connection;
		}

		/**
		 * Returns whether this connection has expired
		 * @return
		 */
		public boolean expiredConnection() {
			if (lastAccessTime == 0) {
				return false;
			}
			try {
				return System.currentTimeMillis() - lastAccessTime >= connectionTimeOut || connection.isClosed();
			} catch (Throwable ex) {
				return true;
			}
		}
	}

    public static void closeAll() throws SQLException {
        for (ConWrapper con : connections.values()) {
            con.connection.close();
        }
		connections.clear();
    }
        private static void initializeProperties()
        {
            Properties ConfigProps = new Properties();
            try {
            ConfigProps.load(new FileInputStream("config.properties"));
            } catch (FileNotFoundException fnf)
            {
                System.out.println("Unable to start server: config.properties was not found.");
                return;
            } catch (IOException e)
            {
                System.out.println("Unable to start server: Error reading from config.properties.");
                return;
            }

            ServerConstants.DATABASE_USER = ConfigProps.getProperty("db_user");
            ServerConstants.DATABASE_PASS = ConfigProps.getProperty("db_pass");
            ServerConstants.DATABASE_URL = ConfigProps.getProperty("db_database_url");
        }
            
    }