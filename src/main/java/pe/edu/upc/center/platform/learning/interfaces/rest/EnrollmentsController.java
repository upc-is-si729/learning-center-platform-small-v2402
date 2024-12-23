package pe.edu.upc.center.platform.learning.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.center.platform.learning.domain.model.queries.GetAllEnrollmentsQuery;
import pe.edu.upc.center.platform.learning.domain.model.queries.GetEnrollmentByIdQuery;
import pe.edu.upc.center.platform.learning.domain.services.EnrollmentCommandService;
import pe.edu.upc.center.platform.learning.domain.services.EnrollmentQueryService;
import pe.edu.upc.center.platform.learning.interfaces.rest.resources.CreateEnrollmentResource;
import pe.edu.upc.center.platform.learning.interfaces.rest.resources.EnrollmentResource;
import pe.edu.upc.center.platform.learning.interfaces.rest.transform.CreateEnrollmentCommandFromResourceAssembler;
import pe.edu.upc.center.platform.learning.interfaces.rest.transform.EnrollmentResourceFromEntityAssembler;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/enrollments", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Enrollments" , description = "Enrollments Management Endpoints")
public class EnrollmentsController {

  private final EnrollmentQueryService enrollmentQueryService;
  private final EnrollmentCommandService enrollmentCommandService;

  public EnrollmentsController(EnrollmentQueryService enrollmentQueryService, EnrollmentCommandService enrollmentCommandService) {
    this.enrollmentQueryService = enrollmentQueryService;
    this.enrollmentCommandService = enrollmentCommandService;
  }

  @Operation(
      summary = "Add a new enrollment item",
      description = "Add a new enrollment item to a student",
      operationId = "createEnrollment",
      responses = {
          @ApiResponse(
              responseCode = "201",
              description = "Successful operation",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = EnrollmentResource.class)
              )
          ),
          @ApiResponse (
              responseCode = "400",
              description = "Bad Request",
              content = @Content (
                  mediaType = "application/json",
                  schema = @Schema(implementation = RuntimeException.class)
              )
          )
      }
  )
  @PostMapping
  public ResponseEntity<EnrollmentResource> createEnrollment(@RequestBody CreateEnrollmentResource resource) {

    var createEnrollmentCommand = CreateEnrollmentCommandFromResourceAssembler.toCommand(resource);
    var enrollmentId = this.enrollmentCommandService.handle(createEnrollmentCommand);

    if (enrollmentId.equals(0L)) {
      return ResponseEntity.badRequest().build();
    }

    var getEnrollmentByIdQuery = new GetEnrollmentByIdQuery(enrollmentId);
    var optionalEnrollment = this.enrollmentQueryService.handle(getEnrollmentByIdQuery);

    if (optionalEnrollment.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    var enrollmentResource = EnrollmentResourceFromEntityAssembler.toResource(optionalEnrollment.get());
    return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentResource);

  }

  @Operation(
      summary = "Fetch all enrollments",
      description = "Fetch all enrollments from database",
      operationId = "getAllEnrollments",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Successful operation",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = EnrollmentResource.class)
              )
          )
      }
  )
  @GetMapping
  public ResponseEntity<List<EnrollmentResource>> getAllEnrollments() {
    var getAllEnrollmentsQuery = new GetAllEnrollmentsQuery();
    var enrollments = this.enrollmentQueryService.handle(getAllEnrollmentsQuery);

    var enrollmentResources = enrollments.stream()
        .map(EnrollmentResourceFromEntityAssembler::toResource)
        .toList();
    return ResponseEntity.ok(enrollmentResources);
  }
}
