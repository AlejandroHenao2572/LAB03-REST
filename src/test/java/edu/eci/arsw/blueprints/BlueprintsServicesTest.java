package edu.eci.arsw.blueprints;

import edu.eci.arsw.blueprints.filters.IdentityFilter;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistence;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.persistence.InMemoryBlueprintPersistence;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la capa de servicios BlueprintsServices
 * Verifica la lógica de negocio sin depender de Spring
 */
public class BlueprintsServicesTest {

    private BlueprintsServices services;
    private BlueprintPersistence persistence;

    @BeforeEach
    void setUp() {
        persistence = new InMemoryBlueprintPersistence();
        services = new BlueprintsServices(persistence, new IdentityFilter());
    }

    /**
     * Test 1: Agregar un nuevo blueprint exitosamente
     */
    @Test
    void testAddNewBlueprint_Success() throws BlueprintPersistenceException {
        Blueprint bp = new Blueprint("testauthor", "testplan", 
            List.of(new Point(1, 1), new Point(2, 2)));
        
        assertDoesNotThrow(() -> services.addNewBlueprint(bp));
        
        // Verificar que se puede recuperar
        assertDoesNotThrow(() -> {
            Blueprint retrieved = services.getBlueprint("testauthor", "testplan");
            assertEquals("testauthor", retrieved.getAuthor());
            assertEquals("testplan", retrieved.getName());
        });
    }

    /**
     * Test 2: Intentar agregar blueprint duplicado lanza excepción
     */
    @Test
    void testAddNewBlueprint_Duplicate() throws BlueprintPersistenceException {
        Blueprint bp = new Blueprint("duplicate", "test", List.of(new Point(1, 1)));
        
        services.addNewBlueprint(bp);
        
        assertThrows(BlueprintPersistenceException.class, () -> {
            services.addNewBlueprint(bp);
        });
    }

    /**
     * Test 3: Obtener todos los blueprints del sistema
     */
    @Test
    void testGetAllBlueprints() {
        Set<Blueprint> all = services.getAllBlueprints();
        
        assertNotNull(all);
        assertTrue(all.size() >= 3); // john:house, john:garage, jane:garden (inicial)
    }

    /**
     * Test 4: Obtener blueprints por autor existente
     */
    @Test
    void testGetBlueprintsByAuthor_Success() throws BlueprintNotFoundException {
        Set<Blueprint> johnBlueprints = services.getBlueprintsByAuthor("john");
        
        assertNotNull(johnBlueprints);
        assertTrue(johnBlueprints.size() >= 2); // house y garage
        
        // Verificar que todos son de john
        for (Blueprint bp : johnBlueprints) {
            assertEquals("john", bp.getAuthor());
        }
    }

    /**
     * Test 5: Obtener blueprints por autor inexistente lanza excepción
     */
    @Test
    void testGetBlueprintsByAuthor_NotFound() {
        assertThrows(BlueprintNotFoundException.class, () -> {
            services.getBlueprintsByAuthor("autornoexiste");
        });
    }

    /**
     * Test 6: Obtener blueprint específico por autor y nombre
     */
    @Test
    void testGetBlueprint_Success() throws BlueprintNotFoundException {
        Blueprint bp = services.getBlueprint("john", "house");
        
        assertNotNull(bp);
        assertEquals("john", bp.getAuthor());
        assertEquals("house", bp.getName());
        assertNotNull(bp.getPoints());
    }

    /**
     * Test 7: Obtener blueprint inexistente lanza excepción
     */
    @Test
    void testGetBlueprint_NotFound() {
        assertThrows(BlueprintNotFoundException.class, () -> {
            services.getBlueprint("noexiste", "noexiste");
        });
    }

    /**
     * Test 8: Agregar punto a blueprint existente
     */
    @Test
    void testAddPoint_Success() throws BlueprintPersistenceException, BlueprintNotFoundException {
        // Crear blueprint para prueba
        Blueprint bp = new Blueprint("pointtest", "plan", List.of(new Point(1, 1)));
        services.addNewBlueprint(bp);
        
        int initialSize = services.getBlueprint("pointtest", "plan").getPoints().size();
        
        // Agregar punto
        services.addPoint("pointtest", "plan", 10, 20);
        
        // Verificar que se agregó
        Blueprint updated = services.getBlueprint("pointtest", "plan");
        assertEquals(initialSize + 1, updated.getPoints().size());
    }

    /**
     * Test 9: Agregar punto a blueprint inexistente lanza excepción
     */
    @Test
    void testAddPoint_BlueprintNotFound() {
        assertThrows(BlueprintNotFoundException.class, () -> {
            services.addPoint("noexiste", "noexiste", 1, 1);
        });
    }

    /**
     * Test 10: Verificar que los puntos agregados tienen las coordenadas correctas
     */
    @Test
    void testAddPoint_VerifyCoordinates() throws BlueprintPersistenceException, BlueprintNotFoundException {
        Blueprint bp = new Blueprint("coordtest", "plan", List.of(new Point(0, 0)));
        services.addNewBlueprint(bp);
        
        services.addPoint("coordtest", "plan", 50, 75);
        
        Blueprint updated = services.getBlueprint("coordtest", "plan");
        List<Point> points = updated.getPoints();
        
        // Verificar que el último punto tiene las coordenadas correctas
        Point lastPoint = points.get(points.size() - 1);
        assertEquals(50, lastPoint.x());
        assertEquals(75, lastPoint.y());
    }
}
