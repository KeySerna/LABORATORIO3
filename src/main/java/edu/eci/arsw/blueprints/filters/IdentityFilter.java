package edu.eci.arsw.blueprints.filters;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import edu.eci.arsw.blueprints.model.Blueprint;

/**
 * Filtro por defecto.
 * Se utiliza cuando no se ha seleccionado un filtro específico.
 */
@Component
@Profile("!redundancy & !undersampling")
public class IdentityFilter implements BlueprintsFilter {

    @Override
    public Blueprint apply(Blueprint bp) {
        return bp;
    }
}
