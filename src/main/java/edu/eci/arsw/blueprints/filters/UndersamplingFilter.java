package edu.eci.arsw.blueprints.filters;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;

/**
 * Conserva 1 de cada 2 puntos.
 *
 * Por ejemplo:
 *
 * P0 P1 P2 P3 P4 P5
 *
 * se convierte en:
 *
 * P0 P2 P4
 *
 * Perfil de Spring: undersampling
 */
@Component
@Profile("undersampling")
public class UndersamplingFilter implements BlueprintsFilter {

    @Override
    public Blueprint apply(Blueprint bp) {

        List<Point> in = bp.getPoints();

        if (in.size() <= 2) {
            return bp;
        }

        List<Point> out = new ArrayList<>();

        for (int i = 0; i < in.size(); i++) {

            // Conservamos los índices pares:
            // 0, 2, 4, 6, ...
            if (i % 2 == 0) {
                out.add(in.get(i));
            }
        }

        return new Blueprint(
                bp.getAuthor(),
                bp.getName(),
                out
        );
    }
}