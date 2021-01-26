package org.eclipse.cargotracker.interfaces.booking.facade.internal.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.Itinerary;
import org.eclipse.cargotracker.domain.model.cargo.Leg;
import org.eclipse.cargotracker.domain.model.cargo.RouteSpecification;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoRoute;
import org.junit.Test;

public class CargoRouteDtoAssemblerTest {

  @Test
  public void testToDTO() {
    final CargoRouteDtoAssembler assembler = new CargoRouteDtoAssembler();

    final Location origin = SampleLocations.STOCKHOLM;
    final Location destination = SampleLocations.MELBOURNE;
    final Cargo cargo =
        new Cargo(
            new TrackingId("XYZ"), new RouteSpecification(origin, destination, LocalDate.now()));

    final Itinerary itinerary =
        new Itinerary(
            Arrays.asList(
                new Leg(
                    SampleVoyages.CM001,
                    origin,
                    SampleLocations.SHANGHAI,
                    LocalDateTime.now(),
                    LocalDateTime.now()),
                new Leg(
                    SampleVoyages.CM001,
                    SampleLocations.ROTTERDAM,
                    destination,
                    LocalDateTime.now(),
                    LocalDateTime.now())));

    cargo.assignToRoute(itinerary);

    final CargoRoute dto = assembler.toDto(cargo);

    assertThat(dto.getLegs()).hasSize(2);

    org.eclipse.cargotracker.interfaces.booking.facade.dto.Leg legDTO = dto.getLegs().get(0);
    assertThat(legDTO.getVoyageNumber()).isEqualTo("CM001");
    assertThat(legDTO.getFrom())
        .contains("SESTO"); // this is a little different from original codes.
    assertThat(legDTO.getTo()).contains("CNSHA");

    legDTO = dto.getLegs().get(1);
    assertThat(legDTO.getVoyageNumber()).isEqualTo("CM001");
    assertThat(legDTO.getFrom()).contains("NLRTM");
    assertThat(legDTO.getTo()).contains("AUMEL");
  }

  @Test
  public void testToDTO_NoItinerary() {
    final CargoRouteDtoAssembler assembler = new CargoRouteDtoAssembler();

    final Cargo cargo =
        new Cargo(
            new TrackingId("XYZ"),
            new RouteSpecification(
                SampleLocations.STOCKHOLM, SampleLocations.MELBOURNE, LocalDate.now()));
    final CargoRoute dto = assembler.toDto(cargo);

    assertThat(dto.getTrackingId()).isEqualTo("XYZ");
    assertThat(dto.getOrigin()).contains("SESTO");
    assertThat(dto.getFinalDestination()).contains("AUMEL");
    assertThat(dto.getLegs().isEmpty()).isTrue();
  }
}
