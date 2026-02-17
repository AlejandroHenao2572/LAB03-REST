package edu.eci.arsw.blueprints;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistence;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.persistence.InMemoryBlueprintPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la capa de persistencia en memoria
 * Verifica operaciones CRUD básicas
 */
public class BlueprintPersistenceTest {

    private BlueprintPersistence persistence;

    @BeforeEach
    void setUp() {
        persistence = new InMemoryBlueprintPersistence();
    }

    /**
     * Test 1: Guardar un nuevo blueprint exitosamente
     */
    @Test
    void testSaveBlueprint_Success() throws BlueprintPersistenceException {
        Blueprint bp = new Blueprint("newauthor", "newplan", 
            List.of(new Point(1, 1)));
        
        assertDoesNotThrow(() -> persistence.saveBlueprint(bp));
    }

    /**
     * Test 2: Intentar guardar blueprint duplicado lanza excepción
     */
    @Test
    void testSaveBlueprint_Duplicate() throws BlueprintPersistenceException {
        Blueprint bp = new Blueprint("dup", "test", List.of(new Point(1, 1)));
        
        persistence.saveBlueprint(bp);
        
        BlueprintPersistenceException exception = assertThrows(
            BlueprintPersistenceException.class, 
            () -> persistence.saveBlueprint(bp)
        );
        
        assertTrue(exception.getMessage().contains("already exists"));
    }

    /**
     * Test 3: Obtener blueprint por autor y nombre existente
     */
    @Test
    void testGetBlueprint_Success() throws BlueprintNotFoundException {
        Blueprint bp = persistence.getBlueprint("john", "house");
        
        assertNotNull(bp);
        assertEquals("john", bp.getAuthor());
        assertEquals("house", bp.getName());
        assertFalse(bp.getPoints().isEmpty());
    }

    /**
     * Test 4: Obtener blueprint inexistente lanza excepción
     */
    @Test
    void testGetBlueprint_NotFound() {
        BlueprintNotFoundException exception = assertThrows(
            BlueprintNotFoundException.class,
            () -> persistence.getBlueprint("noexiste", "noexiste")
        );
        
        assertTrue(exception.getMessage().contains("not found"));
    }

    /**
     * Test 5: Obtener blueprints por autor existente
     */
    @Test
    void testGetBlueprintsByAuthor_Success() throws BlueprintNotFoundException {
        Set<Blueprint> blueprints = persistence.getBlueprintsByAuthor("john");
        
        assertNotNull(blueprints);
        assertFalse(blueprints.isEmpty());
        assertTrue(blueprints.size() >= 2); // house y garage
        
        // Verificar que todos pertenecen al autor correcto
        for (Blueprint bp : blueprints) {
            assertEquals("john", bp.getAuthor());
        }
    }

    /**
     * Test 6: Obtener blueprints por autor inexistente lanza excepción
     */
    @Test
    void testGetBlueprintsByAuthor_NotFound() {
        BlueprintNotFoundException exception = assertThrows(
            BlueprintNotFoundException.class,
            () -> persistence.getBlueprintsByAuthor("autornoexiste")
        );
        
        assertTrue(exception.getMessage().contains("No blueprints"));
    }

    /**
     * Test 7: Obtener todos los blueprints
     */
    @Test
    void testGetAllBlueprints() {
        Set<Blueprint> all = persistence.getAllBlueprints();
        
        assertNotNull(all);
        assertTrue(all.size() >= 3); // Datos iniciales: john:house, john:garage, jane:garden
    }

    /**
     * Test 8: Agregar punto a blueprint existente
     */
    @Test
    void testAddPoint_Success() throws BlueprintPersistenceException, BlueprintNotFoundException {
        // Crear blueprint
        Blueprint bp = new Blueprint("addpoint", "test", List.of(new Point(1, 1)));
        persistence.saveBlueprint(bp);
        
        int initialSize = persistence.getBlueprint("addpoint", "test").getPoints().size();
        
        // Agregar punto
        persistence.addPoint("addpoint", "test", 10, 20);
        
        // Verificar
        Blueprint updated = persistence.getBlueprint("addpoint", "test");
        assertEquals(initialSize + 1, updated.getPoints().size());
        
        // Verificar coordenadas del último punto
        List<Point> points = updated.getPoints();
        Point lastPoint = points.get(points.size() - 1);
        assertEquals(10, lastPoint.x());
        assertEquals(20, lastPoint.y());
    }

    /**
     * Test 9: Agregar punto a blueprint inexistente lanza excepción
     */
    @Test
    void testAddPoint_NotFound() {
        assertThrows(BlueprintNotFoundException.class, () -> {
            persistence.addPoint("noexiste", "noexiste", 1, 1);
        });
    }

    /**
     * Test 10: Verificar que los datos iniciales están presentes
     */
    @Test
    void testInitialData() throws BlueprintNotFoundException {
        // Verificar john:house
        Blueprint house = persistence.getBlueprint("john", "house");
        assertNotNull(house);
        assertEquals(4, house.getPoints().size()); // 4 puntos para formar una casa
        
        // Verificar john:garage
        Blueprint garage = persistence.getBlueprint("john", "garage");
        assertNotNull(garage);
        assertEquals(3, garage.getPoints().size());
        
        // Verificar jane:garden
        Blueprint garden = persistence.getBlueprint("jane", "garden");
        assertNotNull(garden);
        assertEquals(3, garden.getPoints().size());
    }

    /**
     * Test 11: Verificar que blueprints con mismo nombre pero diferente autor son distintos
     */
    @Test
    void testDifferentAuthors_SameName() throws BlueprintPersistenceException, BlueprintNotFoundException {
        Blueprint bp1 = new Blueprint("author1", "samename", List.of(new Point(1, 1)));
        Blueprint bp2 = new Blueprint("author2", "samename", List.of(new Point(2, 2)));
        
        persistence.saveBlueprint(bp1);
        persistence.saveBlueprint(bp2);
        
        Blueprint retrieved1 = persistence.getBlueprint("author1", "samename");
        Blueprint retrieved2 = persistence.getBlueprint("author2", "samename");
        
        assertNotEquals(retrieved1, retrieved2);
        assertEquals("author1", retrieved1.getAuthor());
        assertEquals("author2", retrieved2.getAuthor());
    }

    /**
     * Test 12: Verificar que el mismo autor puede tener múltiples blueprints
     */
    @Test
    void testSameAuthor_DifferentNames() throws BlueprintPersistenceException {
        Blueprint bp1 = new Blueprint("multiauthor", "plan1", List.of(new Point(1, 1)));
        Blueprint bp2 = new Blueprint("multiauthor", "plan2", List.of(new Point(2, 2)));
        Blueprint bp3 = new Blueprint("multiauthor", "plan3", List.of(new Point(3, 3)));
        
        persistence.saveBlueprint(bp1);
        persistence.saveBlueprint(bp2);
        persistence.saveBlueprint(bp3);
        
        Set<Blueprint> blueprints = assertDoesNotThrow(() -> 
            persistence.getBlueprintsByAuthor("multiauthor")
        );
        
        assertTrue(blueprints.size() >= 3);
    }
}
