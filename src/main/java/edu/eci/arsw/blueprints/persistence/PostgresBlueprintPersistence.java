package edu.eci.arsw.blueprints.persistence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;

@Repository
@Profile("postgres")
public class PostgresBlueprintPersistence implements BlueprintPersistence {

    private final JdbcTemplate jdbcTemplate;

    public PostgresBlueprintPersistence(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Guarda un Blueprint y todos sus puntos en PostgreSQL.
     */
    @Override
    @Transactional
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {

        String insertBlueprint = """
                INSERT INTO blueprints (author, name)
                VALUES (?, ?)
                """;

        String insertPoint = """
                INSERT INTO blueprint_points (author, name, x, y)
                VALUES (?, ?, ?, ?)
                """;

        try {
            // Primero guardamos el Blueprint
            jdbcTemplate.update(
                    insertBlueprint,
                    bp.getAuthor(),
                    bp.getName()
            );

            // Después guardamos todos sus puntos
            for (Point point : bp.getPoints()) {
                jdbcTemplate.update(
                        insertPoint,
                        bp.getAuthor(),
                        bp.getName(),
                        point.x(),
                        point.y()
                );
            }

        } catch (DuplicateKeyException e) {

            throw new BlueprintPersistenceException(
                    "Blueprint already exists: "
                            + bp.getAuthor()
                            + ":"
                            + bp.getName()
            );

        } catch (DataAccessException e) {

            throw new BlueprintPersistenceException(
                    "Error saving blueprint: " + e.getMessage()
            );
        }
    }

    /**
     * Obtiene un Blueprint específico por autor y nombre.
     */
    @Override
    public Blueprint getBlueprint(String author, String name)
            throws BlueprintNotFoundException {

        String blueprintSql = """
                SELECT author, name
                FROM blueprints
                WHERE author = ? AND name = ?
                """;

        try {

            List<Blueprint> blueprints = jdbcTemplate.query(
                    blueprintSql,
                    (rs, rowNum) -> new Blueprint(
                            rs.getString("author"),
                            rs.getString("name"),
                            new ArrayList<>()
                    ),
                    author,
                    name
            );

            if (blueprints.isEmpty()) {
                throw new BlueprintNotFoundException(
                        "Blueprint not found: %s/%s"
                                .formatted(author, name)
                );
            }

            Blueprint blueprint = blueprints.get(0);

            // Obtener los puntos del Blueprint
            String pointsSql = """
                    SELECT x, y
                    FROM blueprint_points
                    WHERE author = ? AND name = ?
                    ORDER BY id
                    """;

            List<Point> points = jdbcTemplate.query(
                    pointsSql,
                    (rs, rowNum) -> new Point(
                            rs.getInt("x"),
                            rs.getInt("y")
                    ),
                    author,
                    name
            );

            // Agregar los puntos al Blueprint
            for (Point point : points) {
                blueprint.addPoint(point);
            }

            return blueprint;

        } catch (BlueprintNotFoundException e) {

            throw e;

        } catch (DataAccessException e) {

            throw new BlueprintNotFoundException(
                    "Error retrieving blueprint: " + e.getMessage()
            );
        }
    }

    /**
     * Obtiene todos los Blueprints pertenecientes a un autor.
     */
    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author)
            throws BlueprintNotFoundException {

        String sql = """
                SELECT
                    b.author,
                    b.name,
                    p.x,
                    p.y
                FROM blueprints b
                LEFT JOIN blueprint_points p
                    ON b.author = p.author
                    AND b.name = p.name
                WHERE b.author = ?
                ORDER BY b.name, p.id
                """;

        try {

            Map<String, Blueprint> blueprintMap = new LinkedHashMap<>();

            jdbcTemplate.query(
                    sql,
                    rs -> {

                        String blueprintName = rs.getString("name");

                        String key = rs.getString("author")
                                + ":"
                                + blueprintName;

                        Blueprint blueprint = blueprintMap.get(key);

                        if (blueprint == null) {

                            blueprint = new Blueprint(
                                    rs.getString("author"),
                                    blueprintName,
                                    new ArrayList<>()
                            );

                            blueprintMap.put(key, blueprint);
                        }

                        // LEFT JOIN puede devolver NULL si no hay puntos
                        int x = rs.getInt("x");

                        if (!rs.wasNull()) {

                            int y = rs.getInt("y");

                            blueprint.addPoint(
                                    new Point(x, y)
                            );
                        }
                    },
                    author
            );

            if (blueprintMap.isEmpty()) {

                throw new BlueprintNotFoundException(
                        "No blueprints for author: " + author
                );
            }

            return new LinkedHashSet<>(
                    blueprintMap.values()
            );

        } catch (BlueprintNotFoundException e) {

            throw e;

        } catch (DataAccessException e) {

            throw new BlueprintNotFoundException(
                    "Error retrieving blueprints for author: "
                            + e.getMessage()
            );
        }
    }

    /**
     * Obtiene todos los Blueprints almacenados en PostgreSQL.
     */
    @Override
    public Set<Blueprint> getAllBlueprints() {

        String sql = """
                SELECT
                    b.author,
                    b.name,
                    p.x,
                    p.y
                FROM blueprints b
                LEFT JOIN blueprint_points p
                    ON b.author = p.author
                    AND b.name = p.name
                ORDER BY b.author, b.name, p.id
                """;

        Map<String, Blueprint> blueprintMap = new LinkedHashMap<>();

        jdbcTemplate.query(
                sql,
                rs -> {

                    String author = rs.getString("author");
                    String name = rs.getString("name");

                    String key = author + ":" + name;

                    Blueprint blueprint = blueprintMap.get(key);

                    if (blueprint == null) {

                        blueprint = new Blueprint(
                                author,
                                name,
                                new ArrayList<>()
                        );

                        blueprintMap.put(key, blueprint);
                    }

                    // LEFT JOIN puede no tener puntos
                    int x = rs.getInt("x");

                    if (!rs.wasNull()) {

                        int y = rs.getInt("y");

                        blueprint.addPoint(
                                new Point(x, y)
                        );
                    }
                }
        );

        return new LinkedHashSet<>(
                blueprintMap.values()
        );
    }

    /**
     * Agrega un punto a un Blueprint existente.
     */
    @Override
    public void addPoint(
            String author,
            String name,
            int x,
            int y
    ) throws BlueprintNotFoundException {

        // Primero verificamos que el Blueprint exista
        String checkSql = """
                SELECT COUNT(*)
                FROM blueprints
                WHERE author = ? AND name = ?
                """;

        try {

            Integer count = jdbcTemplate.queryForObject(
                    checkSql,
                    Integer.class,
                    author,
                    name
            );

            if (count == null || count == 0) {

                throw new BlueprintNotFoundException(
                        "Blueprint not found: %s/%s"
                                .formatted(author, name)
                );
            }

            String insertPoint = """
                    INSERT INTO blueprint_points
                        (author, name, x, y)
                    VALUES (?, ?, ?, ?)
                    """;

            jdbcTemplate.update(
                    insertPoint,
                    author,
                    name,
                    x,
                    y
            );

        } catch (BlueprintNotFoundException e) {

            throw e;

        } catch (DataAccessException e) {

            throw new BlueprintNotFoundException(
                    "Error adding point to blueprint: "
                            + e.getMessage()
            );
        }
    }
}