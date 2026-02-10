## Laboratorio #4 – REST API Blueprints (Java 21 / Spring Boot 3.3.x)
# Escuela Colombiana de Ingeniería – Arquitecturas de Software  

---

## 📋 Requisitos
- Java 21
- Maven 3.9+

## ▶️ Ejecución del proyecto
```bash
mvn clean install
mvn spring-boot:run
```
Probar con `curl`:
```bash
curl -s http://localhost:8080/blueprints | jq
curl -s http://localhost:8080/blueprints/john | jq
curl -s http://localhost:8080/blueprints/john/house | jq
curl -i -X POST http://localhost:8080/blueprints -H 'Content-Type: application/json' -d '{ "author":"john","name":"kitchen","points":[{"x":1,"y":1},{"x":2,"y":2}] }'
curl -i -X PUT  http://localhost:8080/blueprints/john/kitchen/points -H 'Content-Type: application/json' -d '{ "x":3,"y":3 }'
```

> Si deseas activar filtros de puntos (reducción de redundancia, *undersampling*, etc.), implementa nuevas clases que implementen `BlueprintsFilter` y cámbialas por `IdentityFilter` con `@Primary` o usando configuración de Spring.
---

Abrir en navegador:  
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)  
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)  

---

## 🗂️ Estructura de carpetas (arquitectura)

```
src/main/java/edu/eci/arsw/blueprints
  ├── model/         # Entidades de dominio: Blueprint, Point
  ├── persistence/   # Interfaz + repositorios (InMemory, Postgres)
  │    └── impl/     # Implementaciones concretas
  ├── services/      # Lógica de negocio y orquestación
  ├── filters/       # Filtros de procesamiento (Identity, Redundancy, Undersampling)
  ├── controllers/   # REST Controllers (BlueprintsAPIController)
  └── config/        # Configuración (Swagger/OpenAPI, etc.)
```

> Esta separación sigue el patrón **capas lógicas** (modelo, persistencia, servicios, controladores), facilitando la extensión hacia nuevas tecnologías o fuentes de datos.

---

## 📖 Actividades del laboratorio

### 1. Familiarización con el código base

### 1.1 Revisa el paquete `model` con las clases `Blueprint` y `Point`. 

**Diagrama de clases model UML**

![alt text](img/modeluml.png)

Estas son las entidades principales que representan los planos y sus puntos. Observa cómo se estructuran y qué atributos tienen.

**Clase `Point.java`:** 

Esta clase usa un Java Record que es una forma concisa de crear clases de datos inmutables. 

Automáticamente genera:

- Constructor: Point(int x, int y)
- Getters: x() y y() 
- equals(): Compara puntos por sus coordenadas
- hashCode(): Para usar en colecciones
- toString(): Representación textual "Point[x=1, y=2]"

Características:

- Inmutable: No se pueden cambiar las coordenadas después de crear el punto
- Validación: Las coordenadas son enteros (int)
- Uso: Representa una coordenada cartesiana en un plano 2D

**Clase `Blueprint.java`:**

Esta clase representa un plano que tiene un autor, un nombre y una lista de puntos.

Atributos:

```java
private String author;        // Autor del plano
private String name;          // Nombre del plano
private final List<Point> points = new ArrayList<>();  // Lista de puntos
```
- La lista points es final 
- Se inicializa vacía y se llena en el constructor

Contructor:

```java
public Blueprint(String author, String name, List<Point> pts) {
    this.author = author;
    this.name = name;
    if (pts != null) points.addAll(pts);
} 
```
- Recibe el autor, nombre y una lista de puntos
- Copia los pintos a su propia lista para mantener la inmutabilidad de la referencia

Getters:
```java
public String getAuthor() { return author; }
public String getName() { return name; }
public List<Point> getPoints() { 
    return Collections.unmodifiableList(points); 
}
```
- Devuelve el autor, nombre y una lista inmodificable de puntos

Metodo para agregar puntos:
```java
public void addPoint(Point p) {
    points.add(p);
}
```
- Permite agregar un punto al plano

Metodos de identidad e igualdad:
```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Blueprint bp)) return false;
    return Objects.equals(author, bp.author) && 
           Objects.equals(name, bp.name);
}

@Override
public int hashCode() {
    return Objects.hash(author, name);
}
```
- Dos planos son iguales si tienen el mismo autor y nombre, sin importar los puntos
- hashCode se basa en autor y nombre para uso en colecciones

### 1.2 Entiende la capa `persistence` con `InMemoryBlueprintPersistence`.

**Intefaz 'BlueprintPersistence'**

Define el contrato para la persistencia de planos.

```java
public interface BlueprintPersistence {
    void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException;
    Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException;
    Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException;
    Set<Blueprint> getAllBlueprints();
    void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException;
}
```
Operaciones CRUD:

- Create: saveBlueprint() - Guarda un nuevo blueprint
- Read: getBlueprint(), getBlueprintsByAuthor(), getAllBlueprints()
- Update: addPoint() - Actualiza agregando un punto

**Implementacion `InMemoryBlueprintPersistence`**

Estrcuctura de datos:
```java
private final Map<String, Blueprint> blueprints = new ConcurrentHashMap<>();
```
- ConcurrentHashMap: Thread-safe para acceso concurrente 
- Clave: String compuesta "author:name" (ej: "john:house")
- Valor: El objeto Blueprint completo

Sistema de claves:
```java
private String keyOf(Blueprint bp) { 
    return bp.getAuthor() + ":" + bp.getName(); 
}

private String keyOf(String author, String name) { 
    return author + ":" + name; 
}
```
- Genera claves compuestas como string para cada plano basado en autor y nombre 

Contructor:
```java
public InMemoryBlueprintPersistence() {
    Blueprint bp1 = new Blueprint("john", "house",
            List.of(new Point(0,0), new Point(10,0), new Point(10,10), new Point(0,10)));
    Blueprint bp2 = new Blueprint("john", "garage",
            List.of(new Point(5,5), new Point(15,5), new Point(15,15)));
    Blueprint bp3 = new Blueprint("jane", "garden",
            List.of(new Point(2,2), new Point(3,4), new Point(6,7)));
    
    blueprints.put(keyOf(bp1), bp1);
    blueprints.put(keyOf(bp2), bp2);
    blueprints.put(keyOf(bp3), bp3);
}
```
- Inicializa con algunos planos de ejemplo

Metodo saveBlueprint():
```java
@Override
public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
    String k = keyOf(bp);
    if (blueprints.containsKey(k)) 
        throw new BlueprintPersistenceException("Blueprint already exists: " + k);
    blueprints.put(k, bp);
}
```
- Genera la clave del blueprint
- Validación: Si ya existe, lanza excepción 
- Si no existe, lo guarda en el mapa
- No se permite duplicados 

Metodo getBlueprint():
```java
@Override
public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
    Blueprint bp = blueprints.get(keyOf(author, name));
    if (bp == null) 
        throw new BlueprintNotFoundException("Blueprint not found: %s/%s".formatted(author, name));
    return bp;
}
```
- Busca en el mapa usando la clave compuesta
- Si no existe (null), lanza excepción
- Si existe, retorna el blueprint

Metodo getBlueprintsByAuthor():
```java
@Override
public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
    Set<Blueprint> set = blueprints.values().stream()
            .filter(bp -> bp.getAuthor().equals(author))
            .collect(Collectors.toSet());
    if (set.isEmpty()) 
        throw new BlueprintNotFoundException("No blueprints for author: " + author);
    return set;
}
```
- Leer por autor 
- Stream: Itera sobre todos los blueprints del mapa
- Filter: Filtra por autor usando equals()
- Collect: Recopila en un Set
- Si encuentra al menos uno, retorna el Set
- Si no encuentra ninguno, lanza excepción

Metodo getAllBlueprints():
```java
@Override
public Set<Blueprint> getAllBlueprints() {
    return new HashSet<>(blueprints.values());
}
```
- Retorna un nuevo HashSet con todos los blueprints del mapa

Metodo addPoint():
```java
@Override
public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
    Blueprint bp = getBlueprint(author, name);
    bp.addPoint(new Point(x, y));
}
```
- Busca el blueprint por autor y nombre
- Si no existe, getBlueprint() lanzará excepción
- Si existe, agrega un nuevo punto al blueprint usando su método addPoint()

