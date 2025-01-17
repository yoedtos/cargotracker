package org.eclipse.cargotracker.interfaces.booking.facade.internal.assembler;

import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.LocationDto;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LocationDtoAssemblerTest {

    @Test
    public void toDto() {
        final LocationDtoAssembler assembler = new LocationDtoAssembler();
        var dto = assembler.toDto(SampleLocations.STOCKHOLM);
        assertThat(dto.getUnLocode()).isEqualTo("SESTO");
        assertThat(dto.getName()).contains("Stockholm");
    }

    @Test
    public void toDtoList() {

        final LocationDtoAssembler assembler = new LocationDtoAssembler();
        final List<Location> locationList =
                Arrays.asList(SampleLocations.STOCKHOLM, SampleLocations.HAMBURG);

        final List<LocationDto> dtos = assembler.toDtoList(locationList);

        assertThat(dtos).hasSize(2);

        var dto = dtos.get(0);
        assertThat(dto.getUnLocode()).isEqualTo("DEHAM");
        assertThat(dto.getName()).contains("Hamburg");

        // There is different from original version.
        // It is ordered by name.
        dto = dtos.get(1);
        assertThat(dto.getUnLocode()).isEqualTo("SESTO");
        assertThat(dto.getName()).contains("Stockholm");
    }
}
