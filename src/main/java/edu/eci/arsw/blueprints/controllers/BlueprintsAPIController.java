package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.ApiResponse;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Blueprints", description = "API para gestión de blueprints (planos)")
@RestController
@RequestMapping("api/v1/blueprints")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { 
        this.services = services; 
    }

    @Operation(
        summary = "Obtener todos los blueprints",
        description = "Retorna la lista completa de blueprints del sistema"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Blueprints obtenidos exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Object.class)
            )
        )
    })
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


    @Operation(
        summary = "Obtener blueprints por autor",
        description = "Retorna todos los blueprints creados por un autor específico"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Blueprints obtenidos exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Object.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Autor no encontrado o sin blueprints",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Object.class)
            )
        )
    })
    /**
     * Obtiene todos los blueprints de un autor específico
     * @param author Nombre del autor
     * @return 200 OK con blueprints del autor
     * @throws BlueprintNotFoundException si el autor no tiene blueprints (manejado por GlobalExceptionHandler)
     */
    @GetMapping("/{author}")
    public ResponseEntity<ApiResponse<Set<Blueprint>>> byAuthor(
            @Parameter(description = "Nombre del autor del blueprint", required = true, example = "john_doe")
            @PathVariable String author) 
            throws BlueprintNotFoundException {
        Set<Blueprint> blueprints = services.getBlueprintsByAuthor(author);
        return ResponseEntity.ok(
            ApiResponse.success("Blueprints by author retrieved", blueprints)
        );
    }

    @Operation(
        summary = "Obtener blueprint específico",
        description = "Retorna un blueprint específico identificado por autor y nombre"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Blueprint encontrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Object.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Blueprint no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Object.class)
            )
        )
    })
    /**
     * Obtiene un blueprint específico por autor y nombre
     * @param author Nombre del autor
     * @param bpname Nombre del blueprint
     * @return 200 OK con el blueprint
     * @throws BlueprintNotFoundException si no existe (manejado por GlobalExceptionHandler)
     */
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<ApiResponse<Blueprint>> byAuthorAndName(
            @Parameter(description = "Nombre del autor del blueprint", required = true, example = "john_doe")
            @PathVariable String author,
            @Parameter(description = "Nombre del blueprint", required = true, example = "house_plan") 
            @PathVariable String bpname) throws BlueprintNotFoundException {
        Blueprint bp = services.getBlueprint(author, bpname);
        return ResponseEntity.ok(
            ApiResponse.success("Blueprint retrieved successfully", bp)
        );
    }

    @Operation(
        summary = "Crear un nuevo blueprint",
        description = "Crea un nuevo blueprint con autor, nombre y lista de puntos. El autor y nombre deben ser únicos."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Blueprint creado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Object.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Datos inválidos - autor o nombre vacíos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Object.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Blueprint ya existente - combinación autor/nombre duplicada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Object.class)
            )
        )
    })
    /**
     * Crea un nuevo blueprint
     * @param req Datos del blueprint (validados con @Valid)
     * @return 201 CREATED con el blueprint creado
     * @throws BlueprintPersistenceException si ya existe (manejado por GlobalExceptionHandler → 409 CONFLICT)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Blueprint>> add(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos del blueprint a crear",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = NewBlueprintRequest.class)
                )
            )
            @Valid @RequestBody NewBlueprintRequest req) 
            throws BlueprintPersistenceException {
        Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
        services.addNewBlueprint(bp);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created("Blueprint created successfully", bp));
    }

    @Operation(
        summary = "Agregar punto a blueprint",
        description = "Agrega un nuevo punto (coordenada x,y) a un blueprint existente"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "202",
            description = "Punto agregado exitosamente - Blueprint actualizado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Blueprint no encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Object.class)
            )
        )
    })
    /**
     * Agrega un punto a un blueprint existente
     * @param author Nombre del autor
     * @param bpname Nombre del blueprint
     * @param p Punto a agregar
     * @return 202 Accepted con el blueprint actualizado
     * @throws BlueprintNotFoundException si no existe (manejado por GlobalExceptionHandler)
     */
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<ApiResponse<Blueprint>> addPoint(
            @Parameter(description = "Nombre del autor del blueprint", required = true, example = "john_doe")
            @PathVariable String author,
            @Parameter(description = "Nombre del blueprint", required = true, example = "house_plan")
            @PathVariable String bpname,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Punto a agregar con coordenadas x e y",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Point.class)
                )
            )
            @RequestBody Point p) throws BlueprintNotFoundException {
        services.addPoint(author, bpname, p.x(), p.y());
        Blueprint updated = services.getBlueprint(author, bpname);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse.updated("Point added successfully", updated));
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
