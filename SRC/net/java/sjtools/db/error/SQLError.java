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
package net.java.sjtools.db.error;

import java.sql.SQLException;

import net.java.sjtools.error.ApplicationError;

public class SQLError extends ApplicationError {
	private static final long serialVersionUID = -1773781004539682300L;

	public SQLError(SQLException e) {
		super(getErrorMsg(e), e);
	}

	private static String getErrorMsg(SQLException e) {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("SQLException chain:");
		
		SQLException exception = e;
		
		while (exception != null) {
			buffer.append(" (");
			buffer.append("errorCode=");
			buffer.append(e.getErrorCode());
			buffer.append(" SQLState=");
			buffer.append(e.getSQLState());
			buffer.append(" message=");
			buffer.append(e.getMessage());
			buffer.append(")");
			
			exception = exception.getNextException();
		}
		
		return buffer.toString();
	}
}
