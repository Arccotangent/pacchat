package net.arccotangent.pacchat.crypto;

import net.arccotangent.pacchat.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;

public class RSA {
	
	private static Logger rsa_log = new Logger("CRYPTO/RSA");
	
	public static KeyPair generateRSAKeypair(int bitsize) {
		try {
			rsa_log.i("Initializing key pair generator, generating " + bitsize + " bit RSA key");
			KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
			gen.initialize(bitsize);
			
			rsa_log.i("Generating RSA keypair now.");
			KeyPair keyPair = gen.generateKeyPair();
			rsa_log.i("Generation complete.");
			
			return keyPair;
		} catch (NoSuchAlgorithmException e) {
			rsa_log.e("Error while generating RSA keys!");
			e.printStackTrace();
		}
		return null;
	}
	
	static byte[] encryptBytes(byte[] toEncrypt, PublicKey publicKey) {
		try {
			Cipher c = Cipher.getInstance("RSA");
			c.init(Cipher.ENCRYPT_MODE, publicKey);
			return c.doFinal(toEncrypt);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			rsa_log.e("Error while encrypting text with RSA pubkey!");
			e.printStackTrace();
		}
		return null;
	}
	
	static byte[] decryptBytes(byte[] toDecrypt, PrivateKey privateKey) {
		try {
			Cipher c = Cipher.getInstance("RSA");
			c.init(Cipher.DECRYPT_MODE, privateKey);
			return c.doFinal(toDecrypt);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			rsa_log.e("Error while decrypting text with RSA privkey!");
			e.printStackTrace();
		}
		return null;
	}
	
	static byte[] signBytes(byte[] toSign, PrivateKey privateKey) {
		try {
			Signature sig = Signature.getInstance("SHA512withRSA");
			sig.initSign(privateKey);
			sig.update(toSign);
			return sig.sign();
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			rsa_log.e("Error while signing text with RSA privkey!");
			e.printStackTrace();
		}
		return null;
	}
	
	static boolean verifyBytes(byte[] signedBytes, byte[] signature, PublicKey publicKey) {
		try {
			Signature sig = Signature.getInstance("SHA512withRSA");
			sig.initVerify(publicKey);
			sig.update(signedBytes);
			return sig.verify(signature);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			rsa_log.e("Error while signing text with RSA privkey!");
			e.printStackTrace();
		}
		return false;
	}
	
}
