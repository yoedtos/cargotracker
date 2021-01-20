package org.eclipse.cargotracker.scenario;

import org.eclipse.cargotracker.application.util.DateUtil;
import org.eclipse.cargotracker.domain.model.cargo.Itinerary;
import org.eclipse.cargotracker.domain.model.cargo.Leg;
import org.eclipse.cargotracker.domain.model.cargo.RouteSpecification;
import org.eclipse.cargotracker.domain.model.location.LocationRepository;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.domain.model.voyage.VoyageRepository;
import org.eclipse.cargotracker.domain.service.RoutingService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class CargoLifecycleScenarioTestRoutingServiceStub implements RoutingService {

    private static final Logger LOGGER = Logger.getLogger(CargoLifecycleScenarioTestRoutingServiceStub.class.getName());

    @Inject
    private LocationRepository locationRepository;

    @Inject
    private VoyageRepository voyageRepository;

    @Override
    public List<Itinerary> fetchRoutesForSpecification(RouteSpecification routeSpecification) {
        LOGGER.log(Level.INFO, "fetchRoutesForSpecification:: {0}", routeSpecification);
        if (routeSpecification.getOrigin().equals(SampleLocations.HONGKONG)) {
            // Hongkong - NYC - Chicago - SampleLocations.STOCKHOLM, initial routing
            return Arrays.asList(new Itinerary(Arrays.asList(
                    new Leg(
                            voyageRepository.find(SampleVoyages.v100.getVoyageNumber()),
                            locationRepository.find(SampleLocations.HONGKONG.getUnLocode()),
                            locationRepository.find(SampleLocations.NEWYORK.getUnLocode()),
                            DateUtil.toDateTime("2014-03-03", "00:00"), DateUtil.toDateTime("2014-03-09", "00:00")),
                    new Leg(
                            voyageRepository.find(SampleVoyages.v200.getVoyageNumber()),
                            locationRepository.find(SampleLocations.NEWYORK.getUnLocode()),
                            locationRepository.find(SampleLocations.CHICAGO.getUnLocode()),
                            DateUtil.toDateTime("2014-03-10", "00:00"), DateUtil.toDateTime("2014-03-14", "00:00")),
                    new Leg(
                            voyageRepository.find(SampleVoyages.v200.getVoyageNumber()),
                            locationRepository.find(SampleLocations.CHICAGO.getUnLocode()),
                            locationRepository.find(SampleLocations.STOCKHOLM.getUnLocode()),
                            DateUtil.toDateTime("2014-03-07", "00:00"), DateUtil.toDateTime("2014-03-11", "00:00"))))
            );
        } else {
            // Tokyo - Hamburg - SampleLocations.STOCKHOLM, rerouting misdirected cargo from
            // Tokyo
            return Arrays.asList(new Itinerary(Arrays.asList(
                    new Leg(
                            voyageRepository.find(SampleVoyages.v300.getVoyageNumber()),
                            locationRepository.find(SampleLocations.TOKYO.getUnLocode()),
                            locationRepository.find(SampleLocations.HAMBURG.getUnLocode()),
                            DateUtil.toDateTime("2014-03-08", "00:00"), DateUtil.toDateTime("2014-03-12", "00:00")),
                    new Leg(
                            voyageRepository.find(SampleVoyages.v400.getVoyageNumber()),
                            locationRepository.find(SampleLocations.HAMBURG.getUnLocode()),
                            locationRepository.find(SampleLocations.STOCKHOLM.getUnLocode()),
                            DateUtil.toDateTime("2014-03-14", "00:00"), DateUtil.toDateTime("2014-03-15", "00:00"))))
            );
        }
    }
}
