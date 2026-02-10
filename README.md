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
