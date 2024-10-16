package com.sa.contable.configuracion;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Base64;

public class GenerarClaveJwt {
    public static void main(String[] args) {
        // Genera una clave secreta segura
        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);

        // Convierte la clave en Base64 para poder usarla y almacenarla
        String claveBase64 = Base64.getEncoder().encodeToString(secretKey.getEncoded());

        System.out.println("Clave secreta en Base64: " + claveBase64);
    }
}
