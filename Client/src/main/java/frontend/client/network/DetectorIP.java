package frontend.client.network;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Utilidad para detectar IP local del cliente.
 */
public class DetectorIP {

    /**
     * Obtiene la IP local preferiblemente de la red LAN.
     */
    public static String obtenerIPLocal() {
        try {

            // 1️⃣ Preferir IP válida no localhost de interfaces activas
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();

                if (!iface.isUp() || iface.isLoopback() || iface.isVirtual())
                    continue;

                for (Enumeration<InetAddress> en = iface.getInetAddresses(); en.hasMoreElements(); ) {
                    InetAddress addr = en.nextElement();

                    if (addr instanceof Inet4Address &&
                            !addr.isLoopbackAddress() &&
                            !addr.isLinkLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }

            // 2️⃣ Fallback: obtener IP desde localHost
            InetAddress localhost = InetAddress.getLocalHost();
            if (!localhost.getHostAddress().startsWith("127."))
                return localhost.getHostAddress();

        } catch (Exception ignored) {}

        // 3️⃣ Última opción
        return "127.0.0.1";
    }

    /**
     * Devuelve todas las IPs IPv4 disponibles del equipo.
     */
    public static String[] obtenerTodasLasIPs() {
        List<String> lista = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();

                if (!iface.isUp() || iface.isLoopback() || iface.isVirtual())
                    continue;

                for (Enumeration<InetAddress> en = iface.getInetAddresses(); en.hasMoreElements(); ) {
                    InetAddress addr = en.nextElement();

                    if (addr instanceof Inet4Address)
                        lista.add(addr.getHostAddress());
                }
            }

        } catch (Exception ignored) {}

        if (lista.isEmpty())
            lista.add("127.0.0.1");

        return lista.toArray(new String[0]);
    }

    /**
     * Valida formato IPv4.
     */
    public static boolean esIPValida(String ip) {
        if (ip == null) return false;

        return ip.matches(
                "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}"
                        + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
        );
    }

    /**
     * Verifica si corresponde a localhost.
     */
    public static boolean esLocalhost(String ip) {
        return "127.0.0.1".equals(ip) || "localhost".equalsIgnoreCase(ip);
    }

    /**
     * Nombre del equipo.
     */
    public static String obtenerNombreHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    /**
     * Información completa del host.
     */
    public static String obtenerInfoCompleta() {
        StringBuilder sb = new StringBuilder();
        sb.append("Host: ").append(obtenerNombreHost()).append("\n");
        sb.append("IP Principal: ").append(obtenerIPLocal()).append("\n");
        sb.append("Todas las IPs:\n");

        for (String ip : obtenerTodasLasIPs())
            sb.append("  - ").append(ip).append("\n");

        return sb.toString();
    }

    /**
     * Verifica si hay internet (rápido).
     */
    public static boolean hayConexionInternet() {
        try {
            InetAddress address = InetAddress.getByName("8.8.8.8");
            return address.isReachable(1500);
        } catch (Exception e) {
            return false;
        }
    }
}
