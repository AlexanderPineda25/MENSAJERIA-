package frontend.client.utils;

import java.util.regex.Pattern;

/**
 * Clase utilitaria para validaciones comunes en el cliente
 */
public class ValidacionUtils {

    // Patrones de validación
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{4,20}$"
    );

    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    /**
     * Valida un email
     */
    public static boolean esEmailValido(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Valida un nombre de usuario
     * Debe tener 4-20 caracteres alfanuméricos y guión bajo
     */
    public static boolean esUsernameValido(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    /**
     * Valida una contraseña
     * Mínimo 6 caracteres
     */
    public static boolean esPasswordValida(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return password.length() >= 6;
    }

    /**
     * Valida una contraseña con requisitos más estrictos
     * Mínimo 8 caracteres, una mayúscula, una minúscula, un número
     */
    public static boolean esPasswordFuerte(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean tieneMayuscula = password.matches(".*[A-Z].*");
        boolean tieneMinuscula = password.matches(".*[a-z].*");
        boolean tieneNumero = password.matches(".*[0-9].*");

        return tieneMayuscula && tieneMinuscula && tieneNumero;
    }

    /**
     * Valida una dirección IP (formato IPv4)
     */
    public static boolean esIPValida(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }

        // Permitir "localhost"
        if (ip.trim().equalsIgnoreCase("localhost")) {
            return true;
        }

        return IP_PATTERN.matcher(ip.trim()).matches();
    }

    /**
     * Valida un puerto
     * Debe estar entre 1024 y 65535
     */
    public static boolean esPuertoValido(int puerto) {
        return puerto >= 1024 && puerto <= 65535;
    }

    /**
     * Valida un puerto desde String
     */
    public static boolean esPuertoValido(String puerto) {
        if (puerto == null || puerto.trim().isEmpty()) {
            return false;
        }

        try {
            int port = Integer.parseInt(puerto.trim());
            return esPuertoValido(port);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Valida que un campo no esté vacío
     */
    public static boolean noEstaVacio(String campo) {
        return campo != null && !campo.trim().isEmpty();
    }

    /**
     * Valida la longitud de un campo
     */
    public static boolean tieneLongitudValida(String campo, int min, int max) {
        if (campo == null) {
            return false;
        }

        int longitud = campo.trim().length();
        return longitud >= min && longitud <= max;
    }

    /**
     * Valida que dos contraseñas coincidan
     */
    public static boolean passwordsCoinciden(String password1, String password2) {
        if (password1 == null || password2 == null) {
            return false;
        }
        return password1.equals(password2);
    }

    /**
     * Valida un nombre de archivo
     * No debe contener caracteres especiales peligrosos
     */
    public static boolean esNombreArchivoValido(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            return false;
        }

        // Caracteres prohibidos en nombres de archivo
        String caracteresProhibidos = "[<>:\"/\\\\|?*]";
        return !nombreArchivo.matches(".*" + caracteresProhibidos + ".*");
    }

    /**
     * Valida el tamaño de un archivo
     * @param tamanoBytes Tamaño en bytes
     * @param maxMB Tamaño máximo en MB
     */
    public static boolean esTamanoArchivoValido(long tamanoBytes, int maxMB) {
        long maxBytes = maxMB * 1024L * 1024L;
        return tamanoBytes > 0 && tamanoBytes <= maxBytes;
    }

    /**
     * Valida una extensión de archivo permitida
     */
    public static boolean esExtensionPermitida(String nombreArchivo, String[] extensionesPermitidas) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return false;
        }

        String extension = nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase();

        for (String ext : extensionesPermitidas) {
            if (extension.equals(ext.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Sanitiza un string eliminando caracteres peligrosos
     */
    public static String sanitizar(String input) {
        if (input == null) {
            return "";
        }

        // Eliminar caracteres de control y caracteres especiales peligrosos
        return input.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
                .replaceAll("[<>\"']", "")
                .trim();
    }

    /**
     * Valida un número de teléfono (formato simple)
     */
    public static boolean esTelefonoValido(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            return false;
        }

        // Permitir dígitos, espacios, guiones, paréntesis y +
        String telefonoLimpio = telefono.replaceAll("[\\s\\-()\\+]", "");
        return telefonoLimpio.matches("\\d{7,15}");
    }

    /**
     * Obtiene mensaje de error para validación de username
     */
    public static String getMensajeErrorUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "El usuario no puede estar vacío";
        }

        if (username.length() < 4) {
            return "El usuario debe tener al menos 4 caracteres";
        }

        if (username.length() > 20) {
            return "El usuario no puede tener más de 20 caracteres";
        }

        if (!username.matches("[a-zA-Z0-9_]+")) {
            return "El usuario solo puede contener letras, números y guión bajo";
        }

        return null; // Válido
    }

    /**
     * Obtiene mensaje de error para validación de email
     */
    public static String getMensajeErrorEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "El email no puede estar vacío";
        }

        if (!esEmailValido(email)) {
            return "El formato del email no es válido";
        }

        return null; // Válido
    }

    /**
     * Obtiene mensaje de error para validación de password
     */
    public static String getMensajeErrorPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "La contraseña no puede estar vacía";
        }

        if (password.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres";
        }

        if (password.length() > 50) {
            return "La contraseña no puede tener más de 50 caracteres";
        }

        return null; // Válida
    }

    /**
     * Obtiene mensaje de error para validación de IP
     */
    public static String getMensajeErrorIP(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return "La IP no puede estar vacía";
        }

        if (!esIPValida(ip)) {
            return "El formato de la IP no es válido (ej: 192.168.1.100)";
        }

        return null; // Válida
    }
}