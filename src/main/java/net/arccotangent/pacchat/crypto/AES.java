package net.arccotangent.pacchat.crypto;

import net.arccotangent.pacchat.logging.Logger;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

class AES {
	
	private static Logger aes_log = new Logger("CRYPTO/AES");
	
	static SecretKey generateAESKey() {
		try {
			aes_log.i("Generating 128 bit AES key.");
			
			KeyGenerator gen = KeyGenerator.getInstance("AES");
			gen.init(128);
			return gen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			aes_log.e("Error while generating AES key!");
			e.printStackTrace();
		}
		return null;
	}
	
	static byte[] encryptBytes(byte[] toEncrypt, SecretKey aes) {
		try {
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE, aes);
			return c.doFinal(toEncrypt);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			aes_log.e("Error while encrypting text with AES key!");
			e.printStackTrace();
		}
		return null;
	}
	
	static byte[] decryptBytes(byte[] toDecrypt, SecretKey aes) {
		try {
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.DECRYPT_MODE, aes);
			return c.doFinal(toDecrypt);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			aes_log.e("Error while decrypting text with AES key!");
			e.printStackTrace();
		}
		return null;
	}
	
}
