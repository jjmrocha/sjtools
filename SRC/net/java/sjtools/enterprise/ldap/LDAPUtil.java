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
package net.java.sjtools.enterprise.ldap;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.InvalidAttributeValueException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import net.java.sjtools.enterprise.ldap.error.LDAPException;
import net.java.sjtools.enterprise.ldap.error.LDAPInvalidUserError;
import net.java.sjtools.enterprise.ldap.error.LDAPUserLockError;
import net.java.sjtools.enterprise.ldap.error.LDAPUserNotExistsError;
import net.java.sjtools.logging.Log;
import net.java.sjtools.logging.LogFactory;
import net.java.sjtools.util.ContextUtil;
import net.java.sjtools.util.TextUtil;

public class LDAPUtil {

	private static Log log = LogFactory.getLog(LDAPUtil.class);

	private static final String NAME = "displayName";
	private static final String MAIL = "mail";
	private static final String GROUPS = "memberOf";
	private static final String[] returnedAtts = { NAME, MAIL, GROUPS };

	public static void validate(String login, String password, LDAPValidationConfig config) throws LDAPException {
		if (log.isDebugEnabled()) {
			log.debug("validate(" + login + ", ..., ...)");
		}

		String userName = TextUtil.replace(config.getGenericUserDN(), "{user}", login);

		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, config.getUrl());
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, userName);
		env.put(Context.SECURITY_CREDENTIALS, password);
		env.put(Context.REFERRAL, "follow");
		env.put("com.sun.jndi.ldap.connect.pool", "true");
		env.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(config.getTimeout()));
		env.put("com.sun.jndi.ldap.read.timeout", String.valueOf(config.getTimeout()));

		DirContext ctx = null;

		try {
			ctx = new InitialDirContext(env);

			ctx.getAttributes(userName);
		} catch (InvalidAttributeValueException e) {
			if (e.getMessage() != null && e.getMessage().indexOf("Exceed password retry limit") >= 0) {
				log.error("User " + login + " is locked", e);
				throw new LDAPUserLockError();
			} else {
				log.error("Error reading data for user " + login, e);
				throw new LDAPException("Error reading data for user " + login, e);
			}
		} catch (AuthenticationException e) {
			if (e.getResolvedObj() == null) {
				log.error("User " + login + " not exists", e);
				throw new LDAPUserNotExistsError();
			} else {
				log.error("Password is wrong for user " + login, e);
				throw new LDAPInvalidUserError();
			}
		} catch (Exception e) {
			log.error("Error reading data for user " + login, e);
			throw new LDAPException("Error reading data for user " + login, e);
		} finally {
			ContextUtil.close(ctx);
		}
	}

	public static LDAPData getUserData(String userName, LDAPConfig config) throws LDAPException {
		if (log.isDebugEnabled()) {
			log.debug("getUserData(" + userName + ", ...)");
		}

		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, config.getUrl());
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, config.getLogin());
		env.put(Context.SECURITY_CREDENTIALS, config.getPassword());
		env.put(Context.REFERRAL, "follow");
		env.put("com.sun.jndi.ldap.connect.pool", "true");
		env.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(config.getTimeout()));
		env.put("com.sun.jndi.ldap.read.timeout", String.valueOf(config.getTimeout()));

		DirContext ctx = null;
		LDAPData data = null;

		try {
			ctx = new InitialDirContext(env);

			SearchControls searchCtls = new SearchControls();
			searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			searchCtls.setReturningAttributes(getReturningAttributes(config));
			searchCtls.setTimeLimit((int) config.getTimeout());

			String user = getUserName(userName);
			String userFilter = getUserFilter(user);

			for (Iterator i = config.getDomainContextList().iterator(); i.hasNext();) {
				NamingEnumeration answer = ctx.search((String) i.next(), userFilter, searchCtls);

				data = getData(config.getRequestAttributeList(), answer);

				if (data != null) {
					data.setUserName(user);
					break;
				}
			}

			if (data == null) {
				log.error("User " + user + " not exists");
				throw new LDAPUserNotExistsError();
			}
		} catch (InvalidAttributeValueException e) {
			if (e.getMessage() != null && e.getMessage().indexOf("Exceed password retry limit") >= 0) {
				log.error("User " + config.getLogin() + " is locked", e);
				throw new LDAPUserLockError();
			} else {
				log.error("Error reading data for user " + config.getLogin(), e);
				throw new LDAPException("Error reading data for user " + config.getLogin(), e);
			}
		} catch (LDAPUserNotExistsError e) {
			throw e;
		} catch (AuthenticationException e) {
			if (e.getResolvedObj() == null) {
				log.error("User " + config.getLogin() + " not exists", e);
				throw new LDAPUserNotExistsError();
			} else {
				log.error("Password is wrong for user " + config.getLogin(), e);
				throw new LDAPInvalidUserError();
			}
		} catch (Exception e) {
			log.error("Error reading data for user " + config.getLogin(), e);
			throw new LDAPException("Error reading data for user " + config.getLogin(), e);
		} finally {
			ContextUtil.close(ctx);
		}

		return data;
	}

	private static LDAPData getData(List requestAttributeList, NamingEnumeration answer) throws NamingException {
		LDAPData data = null;

		while (answer.hasMoreElements()) {
			SearchResult sr = (SearchResult) answer.next();

			Attributes attrs = sr.getAttributes();

			NamingEnumeration enumeration = attrs.getIDs();

			while (enumeration.hasMoreElements()) {
				String attrId = (String) enumeration.nextElement();

				if (attrId.equals(NAME)) {
					if (data == null) {
						data = new LDAPData();
					}

					data.setName((String) attrs.get(attrId).get());
				}

				if (attrId.equals(MAIL)) {
					if (data == null) {
						data = new LDAPData();
					}

					data.setMail((String) attrs.get(attrId).get());
				}

				if (attrId.equals(GROUPS)) {
					NamingEnumeration groupEnumeration = attrs.get(attrId).getAll();

					while (groupEnumeration.hasMoreElements()) {
						String groupAttribute = (String) groupEnumeration.nextElement();

						List groupPropertyList = TextUtil.split(groupAttribute, ",");

						for (Iterator i = groupPropertyList.iterator(); i.hasNext();) {
							String groupProperty = (String) i.next();

							if (groupProperty.startsWith("CN=")) {
								if (data == null) {
									data = new LDAPData();
								}

								data.addGroup(groupProperty.substring(3));
							}
						}
					}
				}

				if (!requestAttributeList.isEmpty()) {
					String attributeName = null;

					for (Iterator i = requestAttributeList.iterator(); i.hasNext();) {
						attributeName = (String) i.next();

						if (attrId.equals(attributeName)) {
							List valueList = new ArrayList();

							NamingEnumeration attrEnumeration = attrs.get(attrId).getAll();

							while (attrEnumeration.hasMoreElements()) {
								valueList.add(attrEnumeration.nextElement());
							}

							if (data == null) {
								data = new LDAPData();
							}

							data.setAttributeValue(attributeName, valueList);
						}
					}
				}
			}
		}

		return data;
	}

	private static String[] getReturningAttributes(LDAPConfig config) {
		if (config.getRequestAttributeList().isEmpty()) {
			return returnedAtts;
		}

		String[] ret = new String[returnedAtts.length + config.getRequestAttributeList().size()];

		System.arraycopy(returnedAtts, 0, ret, 0, returnedAtts.length);
		System.arraycopy(config.getRequestAttributeList().toArray(new String[config.getRequestAttributeList().size()]), 0, ret, returnedAtts.length, config.getRequestAttributeList().size());

		return ret;
	}

	private static String getUserFilter(String userName) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("(&(userPrincipalName=");
		buffer.append(userName);
		buffer.append("@*))");

		return buffer.toString();
	}

	public static String getUserName(String name) {
		int pos = name.indexOf("\\");

		if (pos != -1) {
			return name.substring(pos + 1);
		}

		return name;
	}
}
