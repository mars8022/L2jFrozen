/*
 * L2jFrozen Project - www.l2jfrozen.com 
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2jfrozen.loginserver.network.gameserverpackets;

import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPrivateKey;

import javax.crypto.Cipher;

import org.apache.log4j.Logger;

import com.l2jfrozen.loginserver.network.clientpackets.ClientBasePacket;

/**
 * @author -Wooden-
 */
public class BlowFishKey extends ClientBasePacket
{
	byte[] _key;
	protected static final Logger LOGGER = Logger.getLogger(BlowFishKey.class);
	
	/**
	 * @param decrypt
	 * @param privateKey
	 */
	public BlowFishKey(final byte[] decrypt, final RSAPrivateKey privateKey)
	{
		super(decrypt);
		final int size = readD();
		
		byte[] tempKey = readB(size);
		
		try
		{
			byte[] tempDecryptKey;
			
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
			tempDecryptKey = rsaCipher.doFinal(tempKey);
			
			// there are nulls before the key we must remove them
			int i = 0;
			final int len = tempDecryptKey.length;
			
			for (; i < len; i++)
			{
				if (tempDecryptKey[i] != 0)
				{
					break;
				}
			}
			
			_key = new byte[len - i];
			System.arraycopy(tempDecryptKey, i, _key, 0, len - i);
			
			rsaCipher = null;
		}
		catch (final GeneralSecurityException e)
		{
			LOGGER.error("Error While decrypting blowfish key (RSA)", e);
			e.printStackTrace();
		}
		/*
		 * catch(IOException ioe) { //TODO: manage }
		 */
		
		tempKey = null;
	}
	
	public byte[] getKey()
	{
		return _key;
	}
	
}
