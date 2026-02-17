package edu.eci.arsw.blueprints;

import edu.eci.arsw.blueprints.filters.BlueprintsFilter;
import edu.eci.arsw.blueprints.filters.IdentityFilter;
import edu.eci.arsw.blueprints.filters.RedundancyFilter;
import edu.eci.arsw.blueprints.filters.UndersamplingFilter;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para los filtros de blueprints
 * Verifica que cada filtro funcione correctamente
 */
public class BlueprintFiltersTest {

    /**
     * Test 1: IdentityFilter no modifica el blueprint
     */
    @Test
    void testIdentityFilter() {
        BlueprintsFilter filter = new IdentityFilter();
        
        Blueprint original = new Blueprint("test", "plan", 
            List.of(
                new Point(0, 0),
                new Point(1, 1),
                new Point(2, 2)
            )
        );
        
        Blueprint filtered = filter.apply(original);
        
        assertEquals(3, filtered.getPoints().size());
        assertEquals(original.getPoints().size(), filtered.getPoints().size());
    }

    /**
     * Test 2: RedundancyFilter elimina puntos consecutivos duplicados
     */
    @Test
    void testRedundancyFilter_RemovesDuplicates() {
        BlueprintsFilter filter = new RedundancyFilter();
        
        Blueprint original = new Blueprint("test", "plan",
            List.of(
                new Point(0, 0),
                new Point(0, 0),  // Duplicado
                new Point(1, 1),
                new Point(1, 1),  // Duplicado
                new Point(2, 2)
            )
        );
        
        Blueprint filtered = filter.apply(original);
        
        // Debe quedar: (0,0), (1,1), (2,2) = 3 puntos
        assertEquals(3, filtered.getPoints().size());
    }

    /**
     * Test 3: RedundancyFilter no afecta si no hay duplicados consecutivos
     */
    @Test
    void testRedundancyFilter_NoDuplicates() {
        BlueprintsFilter filter = new RedundancyFilter();
        
        Blueprint original = new Blueprint("test", "plan",
            List.of(
                new Point(0, 0),
                new Point(1, 1),
                new Point(2, 2)
            )
        );
        
        Blueprint filtered = filter.apply(original);
        
        assertEquals(3, filtered.getPoints().size());
    }

    /**
     * Test 4: RedundancyFilter mantiene puntos duplicados NO consecutivos
     */
    @Test
    void testRedundancyFilter_NonConsecutiveDuplicates() {
        BlueprintsFilter filter = new RedundancyFilter();
        
        Blueprint original = new Blueprint("test", "plan",
            List.of(
                new Point(0, 0),
                new Point(1, 1),
                new Point(0, 0),  // Mismo que el primero pero no consecutivo
                new Point(2, 2)
            )
        );
        
        Blueprint filtered = filter.apply(original);
        
        // No debe eliminar el (0,0) no consecutivo
        assertEquals(4, filtered.getPoints().size());
    }

    /**
     * Test 5: UndersamplingFilter reduce puntos a la mitad (submuestreo)
     */
    @Test
    void testUndersamplingFilter() {
        BlueprintsFilter filter = new UndersamplingFilter();
        
        Blueprint original = new Blueprint("test", "plan",
            List.of(
                new Point(0, 0),
                new Point(1, 1),
                new Point(2, 2),
                new Point(3, 3),
                new Point(4, 4),
                new Point(5, 5)
            )
        );
        
        Blueprint filtered = filter.apply(original);
        
        // Debe tomar 1 de cada 2 = 3 puntos
        assertEquals(3, filtered.getPoints().size());
        
        // Verificar que son los puntos correctos (0, 2, 4)
        List<Point> points = filtered.getPoints();
        assertEquals(0, points.get(0).x());
        assertEquals(2, points.get(1).x());
        assertEquals(4, points.get(2).x());
    }

    /**
     * Test 6: UndersamplingFilter con número impar de puntos
     */
    @Test
    void testUndersamplingFilter_OddNumberOfPoints() {
        BlueprintsFilter filter = new UndersamplingFilter();
        
        Blueprint original = new Blueprint("test", "plan",
            List.of(
                new Point(0, 0),
                new Point(1, 1),
                new Point(2, 2),
                new Point(3, 3),
                new Point(4, 4)
            )
        );
        
        Blueprint filtered = filter.apply(original);
        
        // 5 puntos -> toma índices 0, 2, 4 = 3 puntos
        assertEquals(3, filtered.getPoints().size());
    }

    /**
     * Test 7: UndersamplingFilter con un solo punto
     */
    @Test
    void testUndersamplingFilter_SinglePoint() {
        BlueprintsFilter filter = new UndersamplingFilter();
        
        Blueprint original = new Blueprint("test", "plan",
            List.of(new Point(0, 0))
        );
        
        Blueprint filtered = filter.apply(original);
        
        assertEquals(1, filtered.getPoints().size());
    }

    /**
     * Test 8: RedundancyFilter con muchos duplicados consecutivos
     */
    @Test
    void testRedundancyFilter_ManyDuplicates() {
        BlueprintsFilter filter = new RedundancyFilter();
        
        Blueprint original = new Blueprint("test", "plan",
            List.of(
                new Point(0, 0),
                new Point(0, 0),
                new Point(0, 0),
                new Point(0, 0),
                new Point(1, 1),
                new Point(1, 1),
                new Point(2, 2)
            )
        );
        
        Blueprint filtered = filter.apply(original);
        
        // Debe quedar: (0,0), (1,1), (2,2) = 3 puntos
        assertEquals(3, filtered.getPoints().size());
        
        List<Point> points = filtered.getPoints();
        assertEquals(new Point(0, 0), points.get(0));
        assertEquals(new Point(1, 1), points.get(1));
        assertEquals(new Point(2, 2), points.get(2));
    }

    /**
     * Test 9: Filtros no modifican el blueprint original, crean uno nuevo
     */
    @Test
    void testFilters_DoNotModifyOriginal() {
        BlueprintsFilter filter = new RedundancyFilter();
        
        Blueprint original = new Blueprint("test", "plan",
            List.of(
                new Point(0, 0),
                new Point(0, 0),
                new Point(1, 1)
            )
        );
        
        int originalSize = original.getPoints().size();
        
        Blueprint filtered = filter.apply(original);
        
        // El blueprint original no debe cambiar
        assertEquals(originalSize, original.getPoints().size());
        // El filtrado debe tener menos puntos
        assertTrue(filtered.getPoints().size() < originalSize);
    }

    /**
     * Test 10: RedundancyFilter con blueprint vacío
     */
    @Test
    void testRedundancyFilter_EmptyBlueprint() {
        BlueprintsFilter filter = new RedundancyFilter();
        
        Blueprint original = new Blueprint("test", "plan", List.of());
        
        Blueprint filtered = filter.apply(original);
        
        assertEquals(0, filtered.getPoints().size());
    }

    /**
     * Test 11: UndersamplingFilter con blueprint vacío
     */
    @Test
    void testUndersamplingFilter_EmptyBlueprint() {
        BlueprintsFilter filter = new UndersamplingFilter();
        
        Blueprint original = new Blueprint("test", "plan", List.of());
        
        Blueprint filtered = filter.apply(original);
        
        assertEquals(0, filtered.getPoints().size());
    }

    /**
     * Test 12: IdentityFilter con blueprint vacío
     */
    @Test
    void testIdentityFilter_EmptyBlueprint() {
        BlueprintsFilter filter = new IdentityFilter();
        
        Blueprint original = new Blueprint("test", "plan", List.of());
        
        Blueprint filtered = filter.apply(original);
        
        assertEquals(0, filtered.getPoints().size());
    }
}
