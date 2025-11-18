package Backend.Server.Servicios.Core;

import Backend.Server.Entidades.Archivo;
import Backend.Server.Entidades.ConfiguracionLimites;
import Backend.Server.Entidades.Enums.TipoAccion;
import Backend.Server.Entidades.Enums.TipoArchivo;
import Backend.Server.Entidades.RegistroAccion;
import Backend.Server.Entidades.Usuario;
import Backend.Server.Repositorios.ArchivoRepository;
import Backend.Server.Repositorios.ConfiguracionLimitesRepository;
import Backend.Server.Servicios.Observers.SistemaObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArchivoService {

    private final ArchivoRepository archivoRepository;
    private final ConfiguracionLimitesRepository limitesRepository;
    private final List<SistemaObserver> observers;

    // ============================================================
    // SUBIDA / REGISTRO DE ARCHIVO (sin validar filesystem)
    // ============================================================
    @Transactional
    public Archivo subirArchivo(Usuario propietario,
                                String nombre,
                                String ruta,
                                long tamano,
                                TipoArchivo tipo,
                                String mimeType,
                                Integer duracion,
                                String formato) {

        // Configuración
        ConfiguracionLimites limites = limitesRepository.findByActivaTrue()
                .orElseThrow(() -> new IllegalStateException("No hay configuración"));

        // Tamaño máximo permitido
        if (tamano > limites.getMaxTamanoArchivoByte()) {
            throw new IllegalArgumentException("Archivo excede tamaño máximo permitido");
        }

        // Límite diario
        long enviadosHoy = archivoRepository.findByPropietarioId(propietario.getId()).stream()
                .filter(a -> a.getFechaSubida().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                .count();

        if (enviadosHoy >= limites.getMaxArchivosEnviadosPorDia()) {
            throw new IllegalStateException("Límite diario de envíos alcanzado");
        }

        // *** N O T A ***
        // NO verificar Files.exists() aquí.
        // ClienteHandler ya guardó físicamente el archivo.

        Archivo archivo = Archivo.builder()
                .nombreArchivo(nombre)
                .rutaAlmacenamiento(ruta) // Guardamos tal cual la ruta
                .tamanoBytes(tamano)
                .tipoArchivo(tipo)
                .mimeType(mimeType)
                .propietario(propietario)
                .duracionSegundos(duracion)
                .formatoAudio(formato)
                .build();

        archivoRepository.save(archivo);

        notificar(
                TipoAccion.ENVIO_ARCHIVO,
                propietario,
                "Archivo subido: " + nombre,
                null
        );

        return archivo;
    }

    // ============================================================
    // Obtener archivo por id
    // ============================================================
    public Archivo obtenerPorId(Long id) {
        return archivoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Archivo no encontrado: " + id));
    }

    // ============================================================
    // Notificación a observadores
    // ============================================================
    private void notificar(TipoAccion tipo, Usuario usuario, String descripcion, String ip) {
        RegistroAccion accion = RegistroAccion.builder()
                .tipo(tipo)
                .usuario(usuario)
                .descripcion(descripcion)
                .fechaHora(LocalDateTime.now())
                .direccionIP(ip)
                .build();

        observers.forEach(o -> o.notificar(accion));
    }
}
