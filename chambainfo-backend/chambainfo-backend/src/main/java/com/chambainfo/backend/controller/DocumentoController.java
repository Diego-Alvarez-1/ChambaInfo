package com.chambainfo.backend.controller;

import com.chambainfo.backend.service.DocumentoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para gestionar la carga y descarga de documentos de usuario.
 * Permite subir fotos de DNI y certificados laborales (CUL).
 */
@RestController
@RequestMapping("/documentos")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DocumentoController {

    private final DocumentoService documentoService;

    /**
     * Sube la foto del anverso del DNI del usuario autenticado.
     *
     * @param file El archivo de imagen del DNI (anverso).
     * @param authentication La información de autenticación del usuario.
     * @return Una respuesta con la URL del archivo subido.
     */
    @PostMapping("/dni/anverso")
    public ResponseEntity<Map<String, String>> subirDniAnverso(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        log.info("Solicitud para subir DNI anverso");

        String usuarioAutenticado = authentication.getName();
        String fileUrl = documentoService.guardarDniAnverso(file, usuarioAutenticado);

        Map<String, String> response = new HashMap<>();
        response.put("url", fileUrl);
        response.put("mensaje", "DNI anverso subido exitosamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Sube la foto del reverso del DNI del usuario autenticado.
     *
     * @param file El archivo de imagen del DNI (reverso).
     * @param authentication La información de autenticación del usuario.
     * @return Una respuesta con la URL del archivo subido.
     */
    @PostMapping("/dni/reverso")
    public ResponseEntity<Map<String, String>> subirDniReverso(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        log.info("Solicitud para subir DNI reverso");

        String usuarioAutenticado = authentication.getName();
        String fileUrl = documentoService.guardarDniReverso(file, usuarioAutenticado);

        Map<String, String> response = new HashMap<>();
        response.put("url", fileUrl);
        response.put("mensaje", "DNI reverso subido exitosamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Sube el Certificado Único Laboral (CUL) del usuario autenticado.
     *
     * @param file El archivo del CUL (puede ser PDF o imagen).
     * @param authentication La información de autenticación del usuario.
     * @return Una respuesta con la URL del archivo subido.
     */
    @PostMapping("/cul")
    public ResponseEntity<Map<String, String>> subirCUL(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        log.info("Solicitud para subir CUL");

        String usuarioAutenticado = authentication.getName();
        String fileUrl = documentoService.guardarCertificadoLaboral(file, usuarioAutenticado);

        Map<String, String> response = new HashMap<>();
        response.put("url", fileUrl);
        response.put("mensaje", "CUL subido exitosamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene los datos de los documentos del usuario autenticado.
     *
     * @param authentication La información de autenticación del usuario.
     * @return Una respuesta con las URLs de los documentos del usuario.
     */
    @GetMapping("/mis-documentos")
    public ResponseEntity<Map<String, String>> obtenerMisDocumentos(
            Authentication authentication) {

        log.info("Obteniendo documentos del usuario");

        String usuarioAutenticado = authentication.getName();
        Map<String, String> documentos = documentoService.obtenerDocumentosUsuario(usuarioAutenticado);

        return ResponseEntity.ok(documentos);
    }

    /**
     * Descarga un archivo de documento por su nombre.
     *
     * @param filename El nombre del archivo a descargar.
     * @param authentication La información de autenticación del usuario.
     * @return Una respuesta con el archivo solicitado.
     */
    @GetMapping("/descargar/{filename:.+}")
    public ResponseEntity<Resource> descargarArchivo(
            @PathVariable String filename,
            Authentication authentication) {

        log.info("Descargando archivo: {}", filename);

        String usuarioAutenticado = authentication.getName();
        Resource resource = documentoService.cargarArchivo(filename, usuarioAutenticado);

        String contentType = "application/octet-stream";
        if (filename.endsWith(".pdf")) {
            contentType = "application/pdf";
        } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (filename.endsWith(".png")) {
            contentType = "image/png";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * Elimina un documento específico del usuario autenticado.
     *
     * @param tipoDocumento El tipo de documento a eliminar (dni_anverso, dni_reverso, cul).
     * @param authentication La información de autenticación del usuario.
     * @return Una respuesta con un mensaje de confirmación.
     */
    @DeleteMapping("/{tipoDocumento}")
    public ResponseEntity<Map<String, String>> eliminarDocumento(
            @PathVariable String tipoDocumento,
            Authentication authentication) {

        log.info("Eliminando documento: {}", tipoDocumento);

        String usuarioAutenticado = authentication.getName();
        documentoService.eliminarDocumento(tipoDocumento, usuarioAutenticado);

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Documento eliminado exitosamente");

        return ResponseEntity.ok(response);
    }
}