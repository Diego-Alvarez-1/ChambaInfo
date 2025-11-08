package com.chambainfo.backend.service;

import com.chambainfo.backend.entity.Usuario;
import com.chambainfo.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio para gestionar la carga, almacenamiento y recuperación de documentos de usuario.
 * Maneja fotos de DNI y certificados laborales (CUL).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentoService {

    private final UsuarioRepository usuarioRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Guarda la foto del anverso del DNI del usuario.
     *
     * @param file El archivo de imagen del DNI (anverso).
     * @param usuarioNombre El nombre de usuario del usuario autenticado.
     * @return La ruta del archivo guardado.
     * @throws RuntimeException Si el archivo no es válido o hay error al guardar.
     */
    public String guardarDniAnverso(MultipartFile file, String usuarioNombre) {
        validarArchivoImagen(file);

        Usuario usuario = usuarioRepository.findByUsuario(usuarioNombre)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getFotoDniAnverso() != null) {
            eliminarArchivoFisico(usuario.getFotoDniAnverso());
        }

        String filename = guardarArchivo(file, "dni_anverso_" + usuario.getDni());
        usuario.setFotoDniAnverso(filename);
        usuarioRepository.save(usuario);

        log.info("DNI anverso guardado para usuario: {}", usuarioNombre);
        return filename;
    }

    /**
     * Guarda la foto del reverso del DNI del usuario.
     *
     * @param file El archivo de imagen del DNI (reverso).
     * @param usuarioNombre El nombre de usuario del usuario autenticado.
     * @return La ruta del archivo guardado.
     * @throws RuntimeException Si el archivo no es válido o hay error al guardar.
     */
    public String guardarDniReverso(MultipartFile file, String usuarioNombre) {
        validarArchivoImagen(file);

        Usuario usuario = usuarioRepository.findByUsuario(usuarioNombre)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getFotoDniReverso() != null) {
            eliminarArchivoFisico(usuario.getFotoDniReverso());
        }

        String filename = guardarArchivo(file, "dni_reverso_" + usuario.getDni());
        usuario.setFotoDniReverso(filename);
        usuarioRepository.save(usuario);

        log.info("DNI reverso guardado para usuario: {}", usuarioNombre);
        return filename;
    }

    /**
     * Guarda el Certificado Único Laboral (CUL) del usuario.
     *
     * @param file El archivo del CUL (PDF o imagen).
     * @param usuarioNombre El nombre de usuario del usuario autenticado.
     * @return La ruta del archivo guardado.
     * @throws RuntimeException Si el archivo no es válido o hay error al guardar.
     */
    public String guardarCertificadoLaboral(MultipartFile file, String usuarioNombre) {
        validarArchivoCUL(file);

        Usuario usuario = usuarioRepository.findByUsuario(usuarioNombre)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getCertificadoLaboral() != null) {
            eliminarArchivoFisico(usuario.getCertificadoLaboral());
        }

        String filename = guardarArchivo(file, "cul_" + usuario.getDni());
        usuario.setCertificadoLaboral(filename);
        usuarioRepository.save(usuario);

        log.info("CUL guardado para usuario: {}", usuarioNombre);
        return filename;
    }

    /**
     * Obtiene las rutas de todos los documentos del usuario.
     *
     * @param usuarioNombre El nombre de usuario del usuario autenticado.
     * @return Un mapa con los tipos de documento y sus rutas.
     * @throws RuntimeException Si el usuario no se encuentra.
     */
    public Map<String, String> obtenerDocumentosUsuario(String usuarioNombre) {
        Usuario usuario = usuarioRepository.findByUsuario(usuarioNombre)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Map<String, String> documentos = new HashMap<>();
        documentos.put("dniAnverso", usuario.getFotoDniAnverso());
        documentos.put("dniReverso", usuario.getFotoDniReverso());
        documentos.put("cul", usuario.getCertificadoLaboral());

        return documentos;
    }

    /**
     * Carga un archivo desde el sistema de archivos.
     *
     * @param filename El nombre del archivo a cargar.
     * @param usuarioNombre El nombre de usuario del usuario autenticado.
     * @return El recurso del archivo solicitado.
     * @throws RuntimeException Si el archivo no se encuentra o no pertenece al usuario.
     */
    public Resource cargarArchivo(String filename, String usuarioNombre) {
        try {
            Usuario usuario = usuarioRepository.findByUsuario(usuarioNombre)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (!filename.equals(usuario.getFotoDniAnverso()) &&
                    !filename.equals(usuario.getFotoDniReverso()) &&
                    !filename.equals(usuario.getCertificadoLaboral())) {
                throw new RuntimeException("No tienes permiso para acceder a este archivo");
            }

            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("Archivo no encontrado: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error al cargar archivo: " + filename, e);
        }
    }

    /**
     * Elimina un documento específico del usuario.
     *
     * @param tipoDocumento El tipo de documento a eliminar (dni_anverso, dni_reverso, cul).
     * @param usuarioNombre El nombre de usuario del usuario autenticado.
     * @throws RuntimeException Si el usuario no se encuentra o el tipo es inválido.
     */
    public void eliminarDocumento(String tipoDocumento, String usuarioNombre) {
        Usuario usuario = usuarioRepository.findByUsuario(usuarioNombre)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String filename = null;

        switch (tipoDocumento.toLowerCase()) {
            case "dni_anverso":
                filename = usuario.getFotoDniAnverso();
                usuario.setFotoDniAnverso(null);
                break;
            case "dni_reverso":
                filename = usuario.getFotoDniReverso();
                usuario.setFotoDniReverso(null);
                break;
            case "cul":
                filename = usuario.getCertificadoLaboral();
                usuario.setCertificadoLaboral(null);
                break;
            default:
                throw new RuntimeException("Tipo de documento inválido");
        }

        if (filename != null) {
            eliminarArchivoFisico(filename);
        }

        usuarioRepository.save(usuario);
        log.info("Documento {} eliminado para usuario: {}", tipoDocumento, usuarioNombre);
    }

    /**
     * Guarda un archivo en el sistema de archivos.
     *
     * @param file El archivo a guardar.
     * @param prefix El prefijo para el nombre del archivo.
     * @return El nombre del archivo guardado.
     * @throws RuntimeException Si hay error al guardar el archivo.
     */
    private String guardarArchivo(MultipartFile file, String prefix) {
        try {
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = prefix + "_" + UUID.randomUUID().toString() + extension;

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar archivo", e);
        }
    }

    /**
     * Elimina un archivo del sistema de archivos.
     *
     * @param filename El nombre del archivo a eliminar.
     */
    private void eliminarArchivoFisico(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Error al eliminar archivo: {}", filename, e);
        }
    }

    /**
     * Valida que el archivo sea una imagen válida.
     *
     * @param file El archivo a validar.
     * @throws RuntimeException Si el archivo no es válido.
     */
    private void validarArchivoImagen(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("El archivo está vacío");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") &&
                !contentType.equals("image/jpg") &&
                !contentType.equals("image/png"))) {
            throw new RuntimeException("Solo se permiten imágenes JPG, JPEG o PNG");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("El archivo no debe superar los 5MB");
        }
    }

    /**
     * Valida que el archivo del CUL sea válido (PDF o imagen).
     *
     * @param file El archivo a validar.
     * @throws RuntimeException Si el archivo no es válido.
     */
    private void validarArchivoCUL(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("El archivo está vacío");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/pdf") &&
                !contentType.equals("image/jpeg") &&
                !contentType.equals("image/jpg") &&
                !contentType.equals("image/png"))) {
            throw new RuntimeException("Solo se permiten archivos PDF o imágenes JPG, JPEG, PNG");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("El archivo no debe superar los 10MB");
        }
    }
}