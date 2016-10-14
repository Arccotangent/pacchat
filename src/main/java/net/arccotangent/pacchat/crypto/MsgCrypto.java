package net.arccotangent.pacchat.crypto;

import net.arccotangent.pacchat.logging.Logger;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.PrivateKey;
import java.security.PublicKey;

public class MsgCrypto {
	
	private static Logger mc_log = new Logger("CRYPTO/MSGCRYPTO");
	
	public static String encryptAndSignMessage(String msg, PublicKey publicKey, PrivateKey privateKey) {
		mc_log.i("Encrypting and signing message.");
		SecretKey aes = AES.generateAESKey();
		
		assert aes != null;
		
		byte[] aesCryptedText = AES.encryptBytes(msg.getBytes(), aes);
		byte[] cryptedAesKey = RSA.encryptBytes(aes.getEncoded(), publicKey);
		
		byte[] signature = RSA.signBytes(cryptedAesKey, privateKey);
		
		String cryptedTextB64 = Base64.encodeBase64String(aesCryptedText);
		String cryptedKeyB64 = Base64.encodeBase64String(cryptedAesKey);
		String signatureB64 = Base64.encodeBase64String(signature);
		
		return cryptedKeyB64 + "\n" + cryptedTextB64 + "\n" + signatureB64;
	}
	
	public static String decryptAndVerifyMessage(String cryptedMsg, PrivateKey privateKey, PublicKey publicKey) {
		mc_log.i("Decrypting and verifying message.");
		String[] messageComponents = cryptedMsg.split("\n");
		String cryptedKeyB64 = messageComponents[0];
		String cryptedTextB64 = messageComponents[1];
		String signatureB64 = messageComponents[2];
		
		if (RSA.verifyBytes(Base64.decodeBase64(cryptedKeyB64), Base64.decodeBase64(signatureB64), publicKey))
			mc_log.i("Message authenticity verified!");
		else
			mc_log.w("Message authenticity NOT VERIFIED! Will continue decryption anyway.");
		
		byte[] aesKey = RSA.decryptBytes(Base64.decodeBase64(cryptedKeyB64), privateKey);
		assert aesKey != null;
		SecretKey aes = new SecretKeySpec(aesKey, "AES");
		
		byte[] msg = AES.decryptBytes(Base64.decodeBase64(cryptedTextB64), aes);
		assert msg != null;
		return new String(msg);
	}
	
}
