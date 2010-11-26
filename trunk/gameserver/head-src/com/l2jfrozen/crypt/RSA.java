/*
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
package com.l2jfrozen.crypt;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.apache.commons.codec.binary.Base64; 
//import sun.misc.BASE64Decoder;
//import sun.misc.BASE64Encoder;

import com.l2jfrozen.Config;

/**
 * @author Aviran Mordo http://aviran.mordos.com
 * @version 1.0
 */
public class RSA
{
	private static Logger _log;
	protected static final String ALGORITHM = "RSA";

	private RSA()
	{}

	/**
	 * Init java security to add BouncyCastle as an RSA provider
	 */
	public static void init()
	{
		Security.addProvider(new BouncyCastleProvider());
		_log = Logger.getLogger(RSA.class.getName());
	}

	/**
	 * Generate key which contains a pair of privae and public key using 1024 bytes
	 * 
	 * @return key pair
	 * @throws NoSuchAlgorithmException
	 */
	public static KeyPair generateKey() throws NoSuchAlgorithmException
	{
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
		keyGen.initialize(1024);
		KeyPair key = keyGen.generateKeyPair();
		return key;
	}

	/**
	 * Encrypt a text using public key.
	 * 
	 * @param text The original unencrypted text
	 * @param key The public key
	 * @return Encrypted text
	 * @throws java.lang.Exception
	 */
	public static byte[] encrypt(byte[] text, PublicKey key) throws Exception
	{
		byte[] cipherText = null;
		try
		{
			//
			// get an RSA cipher object and print the provider
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			if(Config.DEBUG)
			{
				_log.info("\nProvider is: " + cipher.getProvider().getInfo());
				_log.info("\nStart encryption with public key");
			}

			// encrypt the plaintext using the public key
			cipher.init(Cipher.ENCRYPT_MODE, key);
			cipherText = cipher.doFinal(text);
		}
		catch(Exception e)
		{
			_log.warning(e.getMessage());
			throw e;
		}
		return cipherText;
	}

	/**
	 * Encrypt a text using public key. The result is enctypted BASE64 encoded text
	 * 
	 * @param text The original unencrypted text
	 * @param key The public key
	 * @return Encrypted text encoded as BASE64
	 * @throws java.lang.Exception
	 */
	public static String encrypt(String text, PublicKey key) throws Exception
	{
		String encryptedText;
		try
		{
			byte[] cipherText = encrypt(text.getBytes("UTF8"), key);
			encryptedText = encodeBASE64(cipherText);

			if(Config.DEBUG)
			{
				_log.info("Enctypted text is: " + encryptedText);
			}
		}
		catch(Exception e)
		{
			_log.warning(e.getMessage());
			throw e;
		}
		return encryptedText;
	}

	/**
	 * Decrypt text using private key
	 * 
	 * @param text The encrypted text
	 * @param key The private key
	 * @return The unencrypted text
	 * @throws java.lang.Exception
	 */
	public static byte[] decrypt(byte[] text, PrivateKey key) throws Exception
	{
		byte[] dectyptedText = null;
		try
		{
			// decrypt the text using the private key
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			if(Config.DEBUG)
			{
				_log.info("Start decryption");
			}
			cipher.init(Cipher.DECRYPT_MODE, key);
			dectyptedText = cipher.doFinal(text);
		}
		catch(Exception e)
		{
			_log.warning(e.getMessage());
			throw e;
		}
		return dectyptedText;

	}

	/**
	 * Decrypt BASE64 encoded text using private key
	 * 
	 * @param text The encrypted text, encoded as BASE64
	 * @param key The private key
	 * @return The unencrypted text encoded as UTF8
	 * @throws java.lang.Exception
	 */
	public static String decrypt(String text, PrivateKey key) throws Exception
	{
		String result;
		try
		{
			// decrypt the text using the private key
			byte[] dectyptedText = decrypt(decodeBASE64(text), key);
			result = new String(dectyptedText, "UTF8");

			if(Config.DEBUG)
			{
				_log.info("Decrypted text is: " + result);
			}
		}
		catch(Exception e)
		{
			_log.warning(e.getMessage());
			throw e;
		}
		return result;

	}

	/**
	 * Convert a Key to string encoded as BASE64
	 * 
	 * @param key The key (private or public)
	 * @return A string representation of the key
	 */
	public static String getKeyAsString(Key key)
	{
		// Get the bytes of the key
		byte[] keyBytes = key.getEncoded();
		// Convert key to BASE64 encoded string
		
		//BASE64Encoder b64 = new BASE64Encoder();
		//return b64.encode(keyBytes);
		Base64 encoder = new Base64();
		String result ="";
		result += encoder.encode (keyBytes); 
		
		return result;
	}

	/**
	 * Generates Private Key from BASE64 encoded string
	 * 
	 * @param key BASE64 encoded string which represents the key
	 * @return The PrivateKey
	 * @throws java.lang.Exception
	 */
	public static PrivateKey getPrivateKeyFromString(String key) throws Exception
	{
		/*
		BASE64Decoder b64 = new BASE64Decoder();
		EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(b64.decodeBuffer(key));
		*/
		byte[] result = null; 
		Base64 decoder = new Base64(); 
	 	result = decoder.decode (key.getBytes());
	 	EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(result);
	 	
	 	KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
		PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
		return privateKey;
	}

	/**
	 * Generates Public Key from BASE64 encoded string
	 * 
	 * @param key BASE64 encoded string which represents the key
	 * @return The PublicKey
	 * @throws java.lang.Exception
	 */
	public static PublicKey getPublicKeyFromString(String key) throws Exception
	{
		//BASE64Decoder b64 = new BASE64Decoder();
		//EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(b64.decodeBuffer(key));
		
		byte[] result = null; 
		Base64 decoder = new Base64(); 
	 	result = decoder.decode (key.getBytes());
	 	EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(result);
	 	
	 	KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
		return publicKey;
	}

	/**
	 * Encode bytes array to BASE64 string
	 * 
	 * @param bytes
	 * @return Encoded string
	 */
	private static String encodeBASE64(byte[] bytes)
	{
		Base64 encoder = new Base64();
		String result ="";
		result += encoder.encode (bytes); 
		return result; 
		
		//return Base64.encodeBytes(bytes);
	}

	/**
	 * Decode BASE64 encoded string to bytes array
	 * 
	 * @param text The string
	 * @return Bytes array
	 * @throws IOException
	 */
	private static byte[] decodeBASE64(String text) throws IOException
	{
		byte[] result = null; 
		Base64 decoder = new Base64(); 
	 	result = decoder.decode (text.getBytes());
	 	return result;
	 	
		//return Base64.decode(text);
	}

	/**
	 * Encrypt file using 1024 RSA encryption
	 * 
	 * @param srcFileName Source file name
	 * @param destFileName Destination file name
	 * @param key The key. For encryption this is the Private Key and for decryption this is the public key
	 * @param cipherMode Cipher Mode
	 * @throws Exception
	 */
	public static void encryptFile(String srcFileName, String destFileName, PublicKey key) throws Exception
	{
		encryptDecryptFile(srcFileName, destFileName, key, Cipher.ENCRYPT_MODE);
	}

	/**
	 * Decrypt file using 1024 RSA encryption
	 * 
	 * @param srcFileName Source file name
	 * @param destFileName Destination file name
	 * @param key The key. For encryption this is the Private Key and for decryption this is the public key
	 * @param cipherMode Cipher Mode
	 * @throws Exception
	 */
	public static void decryptFile(String srcFileName, String destFileName, PrivateKey key) throws Exception
	{
		encryptDecryptFile(srcFileName, destFileName, key, Cipher.DECRYPT_MODE);
	}

	/**
	 * Encrypt and Decrypt files using 1024 RSA encryption
	 * 
	 * @param srcFileName Source file name
	 * @param destFileName Destination file name
	 * @param key The key. For encryption this is the Private Key and for decryption this is the public key
	 * @param cipherMode Cipher Mode
	 * @throws Exception
	 */
	public static void encryptDecryptFile(String srcFileName, String destFileName, Key key, int cipherMode) throws Exception
	{
		OutputStream outputWriter = null;
		InputStream inputReader = null;
		try
		{
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			//String textLine = null;

			//RSA encryption data size limitations are slightly less than the key modulus size,
			//depending on the actual padding scheme used (e.g. with 1024 bit (128 byte) RSA key,
			//the size limit is 117 bytes for PKCS#1 v 1.5 padding. (http://www.jensign.com/JavaScience/dotnet/RSAEncrypt/)
			byte[] buf = cipherMode == Cipher.ENCRYPT_MODE ? new byte[100] : new byte[128];
			int bufl;
			// init the Cipher object for Encryption...
			cipher.init(cipherMode, key);

			// start FileIO
			outputWriter = new FileOutputStream(destFileName);
			inputReader = new FileInputStream(srcFileName);
			while((bufl = inputReader.read(buf)) != -1)
			{
				byte[] encText = null;
				if(cipherMode == Cipher.ENCRYPT_MODE)
				{
					encText = encrypt(copyBytes(buf, bufl), (PublicKey) key);
				}
				else
				{
					if(Config.DEBUG)
					{
						_log.info("buf = " + new String(buf));
					}
					encText = decrypt(copyBytes(buf, bufl), (PrivateKey) key);
				}
				outputWriter.write(encText);
				if(Config.DEBUG)
				{
					_log.info("encText = " + new String(encText));
				}
			}
			outputWriter.flush();

		}
		catch(Exception e)
		{
			_log.warning(e.getMessage());
			throw e;
		}
		finally
		{
			try
			{
				if(outputWriter != null)
				{
					outputWriter.close();
				}
				if(inputReader != null)
				{
					inputReader.close();
				}
			}
			catch(Exception e)
			{
				// do nothing...
			} // end of inner try, catch (Exception)...
		}
	}

	public static byte[] copyBytes(byte[] arr, int length)
	{
		byte[] newArr = null;
		if(arr.length == length)
		{
			newArr = arr;
		}
		else
		{
			newArr = new byte[length];
			for(int i = 0; i < length; i++)
			{
				newArr[i] = arr[i];
			}
		}
		return newArr;
	}

}
