package frontend.client.services;

import frontend.client.Modelos.Usuario;
import frontend.client.Modelos.EstadoUsuario;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;


public class AutenticacionService {

    private static volatile AutenticacionService instancia;

    private Usuario usuarioActual;
    private String tokenSesion;

    private AutenticacionService() {
    }

    public static AutenticacionService getInstancia() {
        if (instancia == null) {
            synchronized (AutenticacionService.class) {
                if (instancia == null) {
                    instancia = new AutenticacionService();
                }
            }
        }
        return instancia;
    }

    // =========================================================
    //  FORTALEZA DE CONTRASEÑA (solo para UI)
    // =========================================================

    public static boolean validarFortalezaPassword(String password) {
        if (password == null || password.length() < 6) return false;

        int criterios = 0;
        if (password.matches(".*[A-Z].*")) criterios++;
        if (password.matches(".*[a-z].*")) criterios++;
        if (password.matches(".*[0-9].*")) criterios++;

        return criterios >= 2;
    }

    public static int obtenerNivelFortaleza(String password) {
        if (password == null) return 0;

        int puntos = 0;

        if (password.length() >= 6) puntos++;
        if (password.length() >= 10) puntos++;
        if (password.length() >= 14) puntos++;

        if (password.matches(".*[A-Z].*")) puntos++;
        if (password.matches(".*[a-z].*")) puntos++;
        if (password.matches(".*[0-9].*")) puntos++;
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) puntos++;

        return Math.min(puntos, 4);
    }

    public static String obtenerDescripcionFortaleza(String password) {
        return switch (obtenerNivelFortaleza(password)) {
            case 0, 1 -> "Débil";
            case 2 -> "Media";
            case 3 -> "Fuerte";
            case 4 -> "Muy Fuerte";
            default -> "Desconocida";
        };
    }

    // =========================================================
    //  SESIÓN
    // =========================================================

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
        this.tokenSesion = generarTokenSesion();
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public boolean estaAutenticado() {
        return usuarioActual != null && tokenSesion != null;
    }

    public boolean puedeConectar() {
        return estaAutenticado() && usuarioActual.getEstado() == EstadoUsuario.ACEPTADO;
    }

    public void cerrarSesion() {
        usuarioActual = null;
        tokenSesion = null;
    }

    public String getTokenSesion() {
        return tokenSesion;
    }

    /**
     * Genera un token de sesión seguro
     */
    private String generarTokenSesion() {
        // Token seguro de 32 bytes base64
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // =========================================================
    //  VALIDACIONES DE CREDENCIALES
    // =========================================================

    public static boolean validarCredenciales(String username, String password) {
        return username != null && username.matches("[a-zA-Z0-9_]{4,}")
                && password != null && password.length() >= 6;
    }

    public static String generarUsernameSugerido(String email) {
        if (email == null || !email.contains("@")) {
            return "user" + System.currentTimeMillis();
        }

        String base = email.substring(0, email.indexOf("@"))
                .replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase();

        return base.isEmpty() ? "user" + System.currentTimeMillis() : base;
    }

    // =========================================================
    //  INFO SESIÓN
    // =========================================================

    public String getInfoSesion() {
        if (!estaAutenticado()) return "No hay sesión activa";

        return "Usuario: " + usuarioActual.getUsername() + "\n" +
                "Estado: " + usuarioActual.getEstadoCuenta() + "\n" +
                "Conectado: " + (usuarioActual.isConectado() ? "Sí" : "No") + "\n" +
                "IP: " + usuarioActual.getDireccionIP() + "\n" +
                "Token: " + tokenSesion;
    }
}
