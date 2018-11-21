package main.java.api;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

public class Password {
	@Context
	ServletContext context;
	
	private static String salt = "dcs.gla.ac.uk";

	public boolean checkPassword(String password, String hash) {
		try {
			String hashedPass = hashPassword(password);
		    return hashedPass.equals(hash);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private static String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 128);
		SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] hash = f.generateSecret(spec).getEncoded();
		Base64.Encoder enc = Base64.getEncoder();
		return enc.encodeToString(salt.getBytes()) + enc.encodeToString(hash);
	}
}
