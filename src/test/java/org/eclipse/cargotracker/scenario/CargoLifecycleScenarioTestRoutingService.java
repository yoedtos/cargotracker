package org.eclipse.cargotracker.scenario;

import org.eclipse.cargotracker.application.util.DateUtil;
import org.eclipse.cargotracker.domain.model.cargo.Itinerary;
import org.eclipse.cargotracker.domain.model.cargo.Leg;
import org.eclipse.cargotracker.domain.model.cargo.RouteSpecification;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.domain.service.RoutingService;

import javax.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class CargoLifecycleScenarioTestRoutingService  implements RoutingService {
    @Override
    public List<Itinerary> fetchRoutesForSpecification(RouteSpecification routeSpecification) {
        if (routeSpecification.getOrigin().equals(SampleLocations.HONGKONG)) {
            // Hongkong - NYC - Chicago - SampleLocations.STOCKHOLM, initial routing
            return Arrays.asList(new Itinerary(Arrays.asList(
                    new Leg(SampleVoyages.v100, SampleLocations.HONGKONG, SampleLocations.NEWYORK,
                            DateUtil.toDateTime("2009-03-03", "00:00"), DateUtil.toDateTime("2009-03-09", "00:00")),
                    new Leg(SampleVoyages.v200, SampleLocations.NEWYORK, SampleLocations.CHICAGO,
                            DateUtil.toDateTime("2009-03-10", "00:00"), DateUtil.toDateTime("2009-03-14", "00:00")),
                    new Leg(SampleVoyages.v200, SampleLocations.CHICAGO, SampleLocations.STOCKHOLM,
                            DateUtil.toDateTime("2009-03-07", "00:00"), DateUtil.toDateTime("2009-03-11", "00:00")))));
        } else {
            // Tokyo - Hamburg - SampleLocations.STOCKHOLM, rerouting misdirected cargo from
            // Tokyo
            return Arrays.asList(new Itinerary(Arrays.asList(
                    new Leg(SampleVoyages.v300, SampleLocations.TOKYO, SampleLocations.HAMBURG,
                            DateUtil.toDateTime("2009-03-08", "00:00"), DateUtil.toDateTime("2009-03-12", "00:00")),
                    new Leg(SampleVoyages.v400, SampleLocations.HAMBURG, SampleLocations.STOCKHOLM,
                            DateUtil.toDateTime("2009-03-14", "00:00"), DateUtil.toDateTime("2009-03-15", "00:00")))));
        }
    }
}
