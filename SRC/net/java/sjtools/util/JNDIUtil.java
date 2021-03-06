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
package net.java.sjtools.util;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

public class JNDIUtil {
	public static Object getJNDIObject(String jndiName) throws NamingException  {
		Context ctx = null;
		Object obj = null;

		try {
			ctx = ContextUtil.getInitialContext();

			obj = ctx.lookup(jndiName);
		} finally {
			ContextUtil.close(ctx);
		}

		return obj;
	}
	
	public static Object getJNDIRemoteObject(String jndiName, Class clazz) throws NamingException {
		Object object = getJNDIObject(jndiName);
		
		return PortableRemoteObject.narrow(object, clazz);
	}
}
