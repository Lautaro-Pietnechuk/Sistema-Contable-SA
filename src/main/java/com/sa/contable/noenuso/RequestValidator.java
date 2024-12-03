package com.sa.contable.noenuso;
/* package com.sa.contable.configuracion;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestValidator {

    private static final Logger logger = Logger.getLogger(RequestValidator.class.getName());

    public static boolean validateUri(String uri) {
        // Loggeamos la URI que se recibe
        logger.log(Level.INFO, "URI recibida: {0}", uri);

        // Intentamos decodificar la URI
        try {
            String decodedUri = URLDecoder.decode(uri, "UTF-8");

            // Comprobamos si la URI contiene caracteres no imprimibles
            for (char c : decodedUri.toCharArray()) {
                if (!Character.isISOControl(c) && !Character.isWhitespace(c)) {
                    continue;
                }
                logger.log(Level.WARNING, "Caracter no v\u00e1lido encontrado en la URI: {0}", c);
                return false;
            }

            return true;
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.SEVERE, "Error al decodificar la URI: {0}", e.getMessage());
            return false;
        }
    }
}
 */