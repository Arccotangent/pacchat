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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class RSA {
	
	private static final Logger rsa_log = new Logger("CRYPTO/RSA");
	
	private static final String RSA_signature_algorithm = "SHA512withRSA";
	
	public static KeyPair generateRSAKeypair(int bitsize) {
		try {
			rsa_log.d("Initializing key pair generator, generating " + bitsize + " bit RSA key");
			KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA", "BC");
			gen.initialize(bitsize);
			
			rsa_log.i("Generating RSA keypair now.");
			KeyPair keyPair = gen.generateKeyPair();
			rsa_log.i("Generation complete.");
			
			return keyPair;
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			rsa_log.e("Error while generating RSA keys!");
			e.printStackTrace();
		}
		return null;
	}
	
	static byte[] encryptBytes(byte[] toEncrypt, RSAPublicKey publicKey) {
		try {
			Cipher c = Cipher.getInstance("RSA", "BC");
			c.init(Cipher.ENCRYPT_MODE, publicKey);
			return c.doFinal(toEncrypt);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchProviderException e) {
			rsa_log.e("Error while encrypting text with RSA pubkey!");
			e.printStackTrace();
		}
		return null;
	}
	
	static DecryptStatus decryptBytes(byte[] toDecrypt, RSAPrivateKey privateKey) {
		try {
			Cipher c = Cipher.getInstance("RSA", "BC");
			c.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] msg = c.doFinal(toDecrypt);
			return new DecryptStatus(msg, true);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchProviderException e) {
			rsa_log.e("Error while decrypting text with RSA privkey!");
			e.printStackTrace();
		}
		return new DecryptStatus(null, false);
	}
	
	static byte[] signBytes(byte[] toSign, RSAPrivateKey privateKey) {
		try {
			Signature sig = Signature.getInstance(RSA_signature_algorithm, "BC");
			sig.initSign(privateKey);
			sig.update(toSign);
			return sig.sign();
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchProviderException e) {
			rsa_log.e("Error while signing text with RSA privkey!");
			e.printStackTrace();
		}
		return null;
	}
	
	static boolean verifyBytes(byte[] signedBytes, byte[] signature, RSAPublicKey publicKey) {
		try {
			Signature sig = Signature.getInstance(RSA_signature_algorithm, "BC");
			sig.initVerify(publicKey);
			sig.update(signedBytes);
			return sig.verify(signature);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchProviderException e) {
			rsa_log.e("Error while signing text with RSA privkey!");
			e.printStackTrace();
		}
		return false;
	}
	
}
