/*
 * SJTools - SysVision Java Tools
 * 
 * Copyright (C) 2006 SysVision - Consultadoria e Desenvolvimento em Sistemas de Informática, Lda.  
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package net.java.sjtools.db.pk;

import java.sql.Connection;
import java.sql.SQLException;

import net.java.sjtools.db.DBMS;
import net.java.sjtools.db.DBMSUtil;

public class NativePrimaryKeyUtil {
	public static final long KEY_NOT_FOUND = -1;
	
	private static NativePrimaryKeyReader postgresql = new PostgreSQLPrimaryKeyReader();
	private static NativePrimaryKeyReader informix = new InformixPrimaryKeyReader();
	private static NativePrimaryKeyReader mysql = new MySQLPrimaryKeyReader();
	private static NativePrimaryKeyReader derby = new DerbyPrimaryKeyReader();
	private static NativePrimaryKeyReader hsql = new HSQLPrimaryKeyReader();
	private static NativePrimaryKeyReader h2 = new H2PrimaryKeyReader();

	public static long getPrimaryKey(Connection con) throws SQLException {
		return getPrimaryKey(con, DBMSUtil.getDBMS(con), null);
	}
	
	public static long getPrimaryKey(Connection con, String tableName) throws SQLException {
		return getPrimaryKey(con, DBMSUtil.getDBMS(con), tableName);
	}

	public static long getPrimaryKey(Connection con, DBMS dbms, String tableName) throws SQLException {
		long ret = KEY_NOT_FOUND;

		NativePrimaryKeyReader reader = null;

		if (dbms.equals(DBMS.DBMS_POSTGRESQL)) {
			reader = postgresql;
		} else if (dbms.equals(DBMS.DBMS_INFORMIX)) {
			reader = informix;
		} else if (dbms.equals(DBMS.DBMS_MYSQL)) { 
			reader = mysql;
		} else if (dbms.equals(DBMS.DBMS_DERBY)) { 
			reader = derby;
		} else if (dbms.equals(DBMS.DBMS_HSQL)) { 
			reader = hsql;
		} else if (dbms.equals(DBMS.DBMS_H2)) { 
			reader = h2;			
		}

		if (reader != null) {
			ret = reader.getKey(con, tableName);
		}

		return ret;
	}
}
