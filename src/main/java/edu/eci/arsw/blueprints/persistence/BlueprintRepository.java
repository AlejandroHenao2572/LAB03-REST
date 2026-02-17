package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.Blueprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlueprintRepository extends JpaRepository<Blueprint, Long> {
    
    // Spring genera automaticamente estas consultas, no es necesario implementacion
    Optional<Blueprint> findByAuthorAndName(String author, String name);
    List<Blueprint> findByAuthor(String author);
    boolean existsByAuthorAndName(String author, String name);
}