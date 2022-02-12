package com.itrustmachines.bnsautofolderattest.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class AESUtil {
  
  public static final String ALGORITHM = "AES";
  
  public String encrypt(@NonNull final String plainText, @NonNull final String keySeed) throws NoSuchPaddingException,
      NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    Key secretKey = getKey(keySeed);
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    byte[] p = plainText.getBytes(StandardCharsets.UTF_8);
    byte[] result = cipher.doFinal(p);
    final Base64.Encoder encoder = Base64.getEncoder();
    return encoder.encodeToString(result);
  }
  
  public String decrypt(@NonNull final String cipherText, @NonNull final String keySeed) throws InvalidKeyException,
      NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException {
    Key secretKey = getKey(keySeed);
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.DECRYPT_MODE, secretKey);
    final Base64.Decoder decoder = Base64.getDecoder();
    byte[] c = decoder.decode(cipherText);
    byte[] result = cipher.doFinal(c);
    return new String(result, StandardCharsets.UTF_8);
  }
  
  public Key getKey(@NonNull final String keySeed) throws NoSuchAlgorithmException {
    if (keySeed.isBlank()) {
      return null;
    }
    SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
    secureRandom.setSeed(keySeed.getBytes(StandardCharsets.UTF_8));
    KeyGenerator generator = KeyGenerator.getInstance(ALGORITHM);
    generator.init(secureRandom);
    return generator.generateKey();
  }
}
