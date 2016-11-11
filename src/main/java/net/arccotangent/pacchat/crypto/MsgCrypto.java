/*
This file is part of PacChat.

PacChat is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

PacChat is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with PacChat.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.arccotangent.pacchat.crypto;

import net.arccotangent.pacchat.logging.Logger;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.PrivateKey;
import java.security.PublicKey;

public class MsgCrypto {
	
	private static final Logger mc_log = new Logger("CRYPTO/MSGCRYPTO");
	
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
	
	public static PacchatMessage decryptAndVerifyMessage(String cryptedMsg, PrivateKey privateKey, PublicKey publicKey) {
		mc_log.i("Decrypting and verifying message.");
		String[] messageComponents = cryptedMsg.split("\n");
		String cryptedKeyB64 = messageComponents[0];
		String cryptedTextB64 = messageComponents[1];
		String signatureB64 = messageComponents[2];
		
		boolean verified = RSA.verifyBytes(Base64.decodeBase64(cryptedKeyB64), Base64.decodeBase64(signatureB64), publicKey);
		if (verified)
			mc_log.i("Message authenticity verified!");
		else {
			mc_log.w("**********************************************");
			mc_log.w("Message authenticity NOT VERIFIED! Will continue decryption anyway.");
			mc_log.w("Someone may be tampering with your connection! This is an unlikely, but not impossible scenario!");
			mc_log.w("If you are sure the connection was not tampered with, consider updating the sender's key with the 'getkey' command.");
			mc_log.w("**********************************************");
		}
		
		DecryptStatus rsa = RSA.decryptBytes(Base64.decodeBase64(cryptedKeyB64), privateKey);
		byte[] aesKey = rsa.getMessage();
		SecretKey aes = new SecretKeySpec(aesKey, "AES");
		
		DecryptStatus message = AES.decryptBytes(Base64.decodeBase64(cryptedTextB64), aes);
		byte[] msg = message.getMessage();
		boolean decrypted = message.isDecryptedSuccessfully();
		
		return new PacchatMessage(new String(msg), verified, decrypted);
	}
	
}
