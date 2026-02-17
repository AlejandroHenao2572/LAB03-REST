package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.ApiResponse;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * REST Controller para gestión de Blueprints
 * Todas las excepciones son manejadas por GlobalExceptionHandler
 * Endpoints base: /api/v1/blueprints
 */
@RestController
@RequestMapping("api/v1/blueprints")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { 
        this.services = services; 
    }

    /**
     * Obtiene todos los blueprints del sistema
     * @return 200 OK con lista de blueprints
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        Set<Blueprint> blueprints = services.getAllBlueprints();
        return ResponseEntity.ok(
            ApiResponse.success("Blueprints retrieved successfully", blueprints)
        );
    }

    /**
     * Obtiene todos los blueprints de un autor específico
     * @param author Nombre del autor
     * @return 200 OK con blueprints del autor
     * @throws BlueprintNotFoundException si el autor no tiene blueprints (manejado por GlobalExceptionHandler)
     */
    @GetMapping("/{author}")
    public ResponseEntity<ApiResponse<Set<Blueprint>>> byAuthor(@PathVariable String author) 
            throws BlueprintNotFoundException {
        Set<Blueprint> blueprints = services.getBlueprintsByAuthor(author);
        return ResponseEntity.ok(
            ApiResponse.success("Blueprints by author retrieved", blueprints)
        );
    }

    /**
     * Obtiene un blueprint específico por autor y nombre
     * @param author Nombre del autor
     * @param bpname Nombre del blueprint
     * @return 200 OK con el blueprint
     * @throws BlueprintNotFoundException si no existe (manejado por GlobalExceptionHandler)
     */
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<ApiResponse<Blueprint>> byAuthorAndName(
            @PathVariable String author, 
            @PathVariable String bpname) throws BlueprintNotFoundException {
        Blueprint bp = services.getBlueprint(author, bpname);
        return ResponseEntity.ok(
            ApiResponse.success("Blueprint retrieved successfully", bp)
        );
    }

    /**
     * Crea un nuevo blueprint
     * @param req Datos del blueprint (validados con @Valid)
     * @return 201 CREATED con el blueprint creado
     * @throws BlueprintPersistenceException si ya existe (manejado por GlobalExceptionHandler → 409 CONFLICT)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Blueprint>> add(
            @Valid @RequestBody NewBlueprintRequest req) 
            throws BlueprintPersistenceException {
        Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
        services.addNewBlueprint(bp);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created("Blueprint created successfully", bp));
    }

    /**
     * Agrega un punto a un blueprint existente
     * @param author Nombre del autor
     * @param bpname Nombre del blueprint
     * @param p Punto a agregar
     * @return 202 Acepted con el blueprint actualizado
     * @throws BlueprintNotFoundException si no existe (manejado por GlobalExceptionHandler)
     */
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<ApiResponse<Blueprint>> addPoint(
            @PathVariable String author, 
            @PathVariable String bpname,
            @RequestBody Point p) throws BlueprintNotFoundException {
        services.addPoint(author, bpname, p.x(), p.y());
        Blueprint updated = services.getBlueprint(author, bpname);
        return ResponseEntity.ok(
            ApiResponse.updated("Point added successfully", updated)
        );
    }

    /**
     * DTO para crear blueprints con validaciones
     */
    public record NewBlueprintRequest(
            @NotBlank(message = "Author cannot be blank") 
            String author,
            
            @NotBlank(message = "Name cannot be blank") 
            String name,
            
            @Valid 
            java.util.List<Point> points
    ) { }
}
