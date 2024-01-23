package de.egi.geofence.geozone.utils;

import android.annotation.SuppressLint;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class SimpleCryptoPBKDF2 {

	// A random value that is generally not a secret, which is used to make some
	// precomputed attacks harder.
	// It is added to the string which is to be encrypted.
	private static final String SALT = "some_very_important_salt!";
	private final static String HEX = "0123456789ABCDEF";

	public static String encrypt(String seed, String cleartext) throws Exception {
		SecretKey key = generateKey(seed.toCharArray(), SALT.getBytes());
		byte[] rawKey = key.getEncoded();
		byte[] result = encrypt(rawKey, cleartext.getBytes());
		return toHex(result);
	}

	public static String decrypt(String seed, String encrypted) throws Exception {
		SecretKey key = generateKey(seed.toCharArray(), SALT.getBytes());
		byte[] rawKey = key.getEncoded();
		byte[] enc = toByte(encrypted);
		byte[] result = decrypt(rawKey, enc);
		return new String(result);
	}

	public static SecretKey generateKey(char[] passphraseOrPin, byte[] salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		// Number of PBKDF2 hardening rounds to use. Larger values increase
		// computation time. You should select a value that causes computation
		// to take >100ms.
		final int iterations = 1000;

		// Generate a 256-bit key
		final int outputKeyLength = 256;

		SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec keySpec = new PBEKeySpec(passphraseOrPin, salt, iterations, outputKeyLength);
		return secretKeyFactory.generateSecret(keySpec);
	}

	private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		@SuppressLint("GetInstance") Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		return cipher.doFinal(clear);
	}

	private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		@SuppressLint("GetInstance") Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		return cipher.doFinal(encrypted);
	}
	public static byte[] toByte(String hexString) {
		int len = hexString.length() / 2;
		byte[] result = new byte[len];

		for (int i = 0; i < len; i++)
			result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();

		return result;
	}

	public static String toHex(byte[] buf) {
		if (buf == null)
			return "";

		StringBuffer result = new StringBuffer(2 * buf.length);

		for (byte b : buf) {
			appendHex(result, b);
		}

		return result.toString();
	}

	private static void appendHex(StringBuffer sb, byte b) {
		sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
	}

	public static void main(String[] args) {
		String seed = "Huber"; // Secret key 
		String clear = "Testarossa Huber Susi Testarossa Huber Susi Testarossa Huber Susi Testarossa Huber Susi Testarossa Huber Susi ";
		String encr = null;
		String decr = null;
		try {
			encr = SimpleCryptoPBKDF2.encrypt(seed, clear);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Encrypted: " + encr);
		try {
			decr = SimpleCryptoPBKDF2.decrypt(seed, encr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Decrypted: " + decr);
	}
}