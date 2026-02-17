package edu.eci.arsw.blueprints;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para el API REST de Blueprints
 * Probar todas las funcionalidades: GET, POST, PUT
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BlueprintsAPITest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BlueprintsServices services;

    private String baseUrl;

    @BeforeEach
    void setUp() throws BlueprintPersistenceException {
        baseUrl = "http://localhost:" + port + "/api/v1/blueprints";
        
        // Crear datos de prueba iniciales
        try {
            services.addNewBlueprint(new Blueprint("john", "house",
                List.of(new Point(0, 0), new Point(10, 0), new Point(10, 10), new Point(0, 10))));
            services.addNewBlueprint(new Blueprint("john", "garage",
                List.of(new Point(5, 5), new Point(15, 5), new Point(15, 15))));
            services.addNewBlueprint(new Blueprint("jane", "garden",
                List.of(new Point(2, 2), new Point(3, 4), new Point(6, 7))));
        } catch (BlueprintPersistenceException e) {
            // Ignorar si ya existen
        }
    }

    /**
     * Test 1: GET /blueprints - Obtener todos los blueprints
     * Debe retornar 200 OK con todos los blueprints del sistema
     */
    @Test
    void testGetAllBlueprints() {
        ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl, Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().get("code"));
        assertEquals("Blueprints retrieved successfully", response.getBody().get("message"));
        assertNotNull(response.getBody().get("data"));
    }

    /**
     * Test 2: GET /blueprints/{author} - Obtener blueprints por autor existente
     * Debe retornar 200 OK con los blueprints del autor
     */
    @Test
    void testGetBlueprintsByAuthor_Success() {
        String author = "john";
        ResponseEntity<Map> response = restTemplate.getForEntity(
            baseUrl + "/" + author, 
            Map.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().get("code"));
        assertNotNull(response.getBody().get("data"));
    }

    /**
     * Test 3: GET /blueprints/{author} - Obtener blueprints por autor inexistente
     * Debe retornar 404 NOT_FOUND
     */
    @Test
    void testGetBlueprintsByAuthor_NotFound() {
        String author = "noexiste123";
        ResponseEntity<Map> response = restTemplate.getForEntity(
            baseUrl + "/" + author, 
            Map.class
        );
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().get("code"));
    }

    /**
     * Test 4: GET /blueprints/{author}/{name} - Obtener blueprint específico existente
     * Debe retornar 200 OK con el blueprint
     */
    @Test
    void testGetBlueprintByAuthorAndName_Success() {
        String author = "john";
        String name = "house";
        
        ResponseEntity<Map> response = restTemplate.getForEntity(
            baseUrl + "/" + author + "/" + name, 
            Map.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().get("code"));
        
        // Verificar que el blueprint retornado es el correcto
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertEquals(author, data.get("author"));
        assertEquals(name, data.get("name"));
    }

    /**
     * Test 5: GET /blueprints/{author}/{name} - Obtener blueprint inexistente
     * Debe retornar 404 NOT_FOUND
     */
    @Test
    void testGetBlueprintByAuthorAndName_NotFound() {
        String author = "noexiste";
        String name = "noexiste";
        
        ResponseEntity<Map> response = restTemplate.getForEntity(
            baseUrl + "/" + author + "/" + name, 
            Map.class
        );
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().get("code"));
    }

    /**
     * Test 6: POST /blueprints - Crear un nuevo blueprint válido
     * Debe retornar 201 CREATED
     */
    @Test
    void testCreateBlueprint_Success() {
        Map<String, Object> newBlueprint = Map.of(
            "author", "testauthor",
            "name", "testplan" + System.currentTimeMillis(), // Nombre único
            "points", List.of(
                Map.of("x", 1, "y", 1),
                Map.of("x", 2, "y", 2)
            )
        );
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl, 
            newBlueprint, 
            Map.class
        );
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(201, response.getBody().get("code"));
        assertEquals("Blueprint created successfully", response.getBody().get("message"));
    }

    /**
     * Test 7: POST /blueprints - Crear blueprint duplicado
     * Debe retornar 409 CONFLICT
     */
    @Test
    void testCreateBlueprint_Duplicate() {
        // Crear el blueprint inicial
        String uniqueName = "duplicatetest" + System.currentTimeMillis();
        Map<String, Object> blueprint = Map.of(
            "author", "duplicate",
            "name", uniqueName,
            "points", List.of(Map.of("x", 1, "y", 1))
        );
        
        restTemplate.postForEntity(baseUrl, blueprint, Map.class);
        
        // Intentar crear el mismo blueprint nuevamente
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl, 
            blueprint, 
            Map.class
        );
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().get("code"));
    }

    /**
     * Test 8: POST /blueprints - Crear blueprint con datos inválidos (sin author)
     * Debe retornar 400 BAD_REQUEST
     */
    @Test
    void testCreateBlueprint_InvalidData() {
        Map<String, Object> invalidBlueprint = Map.of(
            "author", "",  // Author vacío - inválido
            "name", "testplan",
            "points", List.of(Map.of("x", 1, "y", 1))
        );
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl, 
            invalidBlueprint, 
            Map.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("code"));
    }

    /**
     * Test 9: PUT /blueprints/{author}/{name}/points - Agregar punto a blueprint existente
     * Debe retornar 202 ACCEPTED
     */
    @Test
    void testAddPoint_Success() {
        String author = "john";
        String name = "house";
        
        Map<String, Integer> newPoint = Map.of("x", 100, "y", 200);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/" + author + "/" + name + "/points",
            HttpMethod.PUT,
            new HttpEntity<>(newPoint),
            Map.class
        );
        
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(202, response.getBody().get("code"));
        assertEquals("Point added successfully", response.getBody().get("message"));
    }

    /**
     * Test 10: PUT /blueprints/{author}/{name}/points - Agregar punto a blueprint inexistente
     * Debe retornar 404 NOT_FOUND
     */
    @Test
    void testAddPoint_BlueprintNotFound() {
        String author = "noexiste";
        String name = "noexiste";
        
        Map<String, Integer> newPoint = Map.of("x", 100, "y", 200);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            baseUrl + "/" + author + "/" + name + "/points",
            HttpMethod.PUT,
            new HttpEntity<>(newPoint),
            Map.class
        );
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().get("code"));
    }

    /**
     * Test 11: Verificar que un blueprint creado puede ser consultado
     * Prueba el flujo completo: POST -> GET
     */
    @Test
    void testCreateAndRetrieveBlueprint() {
        // Crear blueprint
        String uniqueName = "flowtest" + System.currentTimeMillis();
        Map<String, Object> newBlueprint = Map.of(
            "author", "flowtest",
            "name", uniqueName,
            "points", List.of(Map.of("x", 5, "y", 10))
        );
        
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
            baseUrl, 
            newBlueprint, 
            Map.class
        );
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        
        // Recuperar el blueprint creado
        ResponseEntity<Map> getResponse = restTemplate.getForEntity(
            baseUrl + "/flowtest/" + uniqueName, 
            Map.class
        );
        
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        Map<String, Object> data = (Map<String, Object>) getResponse.getBody().get("data");
        assertEquals("flowtest", data.get("author"));
        assertEquals(uniqueName, data.get("name"));
    }

    /**
     * Test 12: Verificar que se puede agregar un punto y recuperarlo
     * Prueba el flujo: POST -> PUT -> GET
     */
    @Test
    void testCreateAddPointAndRetrieve() throws BlueprintPersistenceException, BlueprintNotFoundException {
        // Crear blueprint
        String uniqueName = "pointtest" + System.currentTimeMillis();
        Blueprint bp = new Blueprint("pointtest", uniqueName, List.of(new Point(1, 1)));
        services.addNewBlueprint(bp);
        
        // Agregar punto
        Map<String, Integer> newPoint = Map.of("x", 99, "y", 88);
        ResponseEntity<Map> putResponse = restTemplate.exchange(
            baseUrl + "/pointtest/" + uniqueName + "/points",
            HttpMethod.PUT,
            new HttpEntity<>(newPoint),
            Map.class
        );
        assertEquals(HttpStatus.ACCEPTED, putResponse.getStatusCode());
        
        // Verificar que el punto fue agregado
        Blueprint updated = services.getBlueprint("pointtest", uniqueName);
        assertTrue(updated.getPoints().size() >= 2);
    }
}
