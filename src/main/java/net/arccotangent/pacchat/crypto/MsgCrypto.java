package net.arccotangent.pacchat.crypto;

import net.arccotangent.pacchat.logging.Logger;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.PrivateKey;
import java.security.PublicKey;

public class MsgCrypto {
	
	private static Logger mc_log = new Logger("CRYPTO/MSGCRYPTO");
	
	public static String encryptMessage(String msg, PublicKey publicKey) {
		mc_log.i("Encrypting message.");
		SecretKey aes = AES.generateAESKey();
		
		assert aes != null;
		
		byte[] aesCryptedText = AES.encryptBytes(msg.getBytes(), aes);
		byte[] cryptedAesKey = RSA.encryptBytes(aes.getEncoded(), publicKey);
		
		String cryptedTextB64 = Base64.encodeBase64String(aesCryptedText);
		String cryptedKeyB64 = Base64.encodeBase64String(cryptedAesKey);
		
		return cryptedKeyB64 + "\n" + cryptedTextB64;
	}
	
	public static String decryptMessage(String cryptedMsg, PrivateKey privateKey) {
		mc_log.i("Decrypting message.");
		String cryptedKeyB64 = cryptedMsg.substring(0, cryptedMsg.indexOf("\n"));
		String cryptedTextB64 = cryptedMsg.substring(cryptedMsg.indexOf("\n") + 1);
		
		byte[] aesKey = RSA.decryptBytes(Base64.decodeBase64(cryptedKeyB64), privateKey);
		assert aesKey != null;
		SecretKey aes = new SecretKeySpec(aesKey, "AES");
		
		byte[] msg = AES.decryptBytes(Base64.decodeBase64(cryptedTextB64), aes);
		assert msg != null;
		return new String(msg);
	}
	
}
