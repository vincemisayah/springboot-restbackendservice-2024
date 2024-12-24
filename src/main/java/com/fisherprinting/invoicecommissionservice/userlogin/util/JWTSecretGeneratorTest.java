package com.fisherprinting.invoicecommissionservice.userlogin.util;

import io.jsonwebtoken.Jwts;
import org.junit.Test;
import jakarta.xml.bind.DatatypeConverter;

import javax.crypto.SecretKey;

public class JWTSecretGeneratorTest {
    @Test
    public void generateSecretKey(){
        SecretKey key = Jwts.SIG.HS512.key().build();

        // Converts key to hex format
        String encodedKey = DatatypeConverter.printBase64Binary(key.getEncoded());
        System.out.printf("\nKey = [%s]\n", encodedKey);
    }
}
