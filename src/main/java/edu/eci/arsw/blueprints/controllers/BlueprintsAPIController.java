package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/blueprints")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) {
        this.services = services;
    }

    // GET /api/v1/blueprints
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {

        Set<Blueprint> blueprints = services.getAllBlueprints();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "execute ok",
                        blueprints
                )
        );
    }

    // GET /api/v1/blueprints/{author}
    @GetMapping("/{author}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    public ResponseEntity<ApiResponse<Set<Blueprint>>> byAuthor(
            @PathVariable String author) {

        try {
            Set<Blueprint> blueprints =
                    services.getBlueprintsByAuthor(author);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            HttpStatus.OK.value(),
                            "execute ok",
                            blueprints
                    )
            );

        } catch (BlueprintNotFoundException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                            new ApiResponse<>(
                                    HttpStatus.NOT_FOUND.value(),
                                    e.getMessage(),
                                    null
                            )
                    );
        }
    }

    // GET /api/v1/blueprints/{author}/{bpname}
    @GetMapping("/{author}/{bpname}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    public ResponseEntity<ApiResponse<Blueprint>> byAuthorAndName(
            @PathVariable String author,
            @PathVariable String bpname) {

        try {
            Blueprint blueprint =
                    services.getBlueprint(author, bpname);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            HttpStatus.OK.value(),
                            "execute ok",
                            blueprint
                    )
            );

        } catch (BlueprintNotFoundException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                            new ApiResponse<>(
                                    HttpStatus.NOT_FOUND.value(),
                                    e.getMessage(),
                                    null
                            )
                    );
        }
    }

    // POST /api/v1/blueprints
    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    public ResponseEntity<ApiResponse<Void>> add(
            @Valid @RequestBody NewBlueprintRequest req) {

        try {

            Blueprint bp =
                    new Blueprint(
                            req.author(),
                            req.name(),
                            req.points()
                    );

            services.addNewBlueprint(bp);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(
                            new ApiResponse<>(
                                    HttpStatus.CREATED.value(),
                                    "Blueprint created successfully",
                                    null
                            )
                    );

        } catch (BlueprintPersistenceException e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(
                            new ApiResponse<>(
                                    HttpStatus.BAD_REQUEST.value(),
                                    e.getMessage(),
                                    null
                            )
                    );
        }
    }

    // PUT /api/v1/blueprints/{author}/{bpname}/points
    @PutMapping("/{author}/{bpname}/points")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    public ResponseEntity<ApiResponse<Void>> addPoint(
            @PathVariable String author,
            @PathVariable String bpname,
            @RequestBody Point p) {

        try {

            services.addPoint(
                    author,
                    bpname,
                    p.x(),
                    p.y()
            );

            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(
                            new ApiResponse<>(
                                    HttpStatus.ACCEPTED.value(),
                                    "Point added successfully",
                                    null
                            )
                    );

        } catch (BlueprintNotFoundException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                            new ApiResponse<>(
                                    HttpStatus.NOT_FOUND.value(),
                                    e.getMessage(),
                                    null
                            )
                    );
        }
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid java.util.List<Point> points
    ) {
    }
}