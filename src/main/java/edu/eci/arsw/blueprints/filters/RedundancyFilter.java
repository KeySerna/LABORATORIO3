package edu.eci.arsw.blueprints.filters;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;

/**
 * Elimina puntos consecutivos duplicados.
 *
 * Perfil de Spring: redundancy
 */
@Component
@Profile("redundancy")
public class RedundancyFilter implements BlueprintsFilter {

    @Override
    public Blueprint apply(Blueprint bp) {

        List<Point> in = bp.getPoints();

        if (in.isEmpty()) {
            return bp;
        }

        List<Point> out = new ArrayList<>();

        Point previous = null;

        for (Point current : in) {

            // Agregar el primer punto.
            if (previous == null) {
                out.add(current);
                previous = current;
                continue;
            }

            // Agregar solamente si es diferente al anterior.
            if (current.x() != previous.x()
                    || current.y() != previous.y()) {

                out.add(current);
                previous = current;
            }
        }

        return new Blueprint(
                bp.getAuthor(),
                bp.getName(),
                out
        );
    }
}