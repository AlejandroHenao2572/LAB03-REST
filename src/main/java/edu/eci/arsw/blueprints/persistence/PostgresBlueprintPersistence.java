package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@Primary //Indicamos que esta implementacion tiene pririodad sobre la otra
public class PostgresBlueprintPersistence implements BlueprintPersistence {

    private final BlueprintRepository repository;

    //Inyectar la interfas del repositorio
    public PostgresBlueprintPersistence(BlueprintRepository repository) {
        this.repository = repository;
    }


    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        // Verificar si ya existe
        if (repository.existsByAuthorAndName(bp.getAuthor(), bp.getName())) {
            throw new BlueprintPersistenceException(
                "Blueprint already exists: " + bp.getAuthor() + ":" + bp.getName()
            );
        }
        repository.save(bp);
    }

    @Override
    public Blueprint getBlueprint(String author, String name) 
            throws BlueprintNotFoundException {
        return repository.findByAuthorAndName(author, name)
            .orElseThrow(() -> new BlueprintNotFoundException(
                "Blueprint not found: %s/%s".formatted(author, name)
            ));
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) 
            throws BlueprintNotFoundException {
        List<Blueprint> blueprints = repository.findByAuthor(author);
        
        if (blueprints.isEmpty()) {
            throw new BlueprintNotFoundException(
                "No blueprints for author: " + author
            );
        }
        
        return new HashSet<>(blueprints);
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        return new HashSet<>(repository.findAll());
    }

    @Override
    public void addPoint(String author, String name, int x, int y) 
            throws BlueprintNotFoundException {
        Blueprint bp = getBlueprint(author, name);
        bp.addPoint(new Point(x, y));
        repository.save(bp);  // Guarda los cambios
    }
}
