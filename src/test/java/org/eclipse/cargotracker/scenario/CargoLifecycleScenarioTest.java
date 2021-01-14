package org.eclipse.cargotracker.scenario;

import org.eclipse.cargotracker.IntegrationTests;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.application.BookingService;
import org.eclipse.cargotracker.application.CargoInspectionService;
import org.eclipse.cargotracker.application.HandlingEventService;
import org.eclipse.cargotracker.application.internal.DefaultBookingService;
import org.eclipse.cargotracker.application.internal.DefaultCargoInspectionService;
import org.eclipse.cargotracker.application.internal.DefaultHandlingEventService;
import org.eclipse.cargotracker.application.util.DateUtil;
import org.eclipse.cargotracker.application.util.SampleDataGenerator;
import org.eclipse.cargotracker.domain.model.cargo.*;
import org.eclipse.cargotracker.domain.model.handling.CannotCreateHandlingEventException;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventFactory;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventRepository;
import org.eclipse.cargotracker.domain.model.location.Location;
import org.eclipse.cargotracker.domain.model.location.LocationRepository;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.domain.model.voyage.Voyage;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;
import org.eclipse.cargotracker.domain.model.voyage.VoyageRepository;
import org.eclipse.cargotracker.domain.service.RoutingService;
import org.eclipse.cargotracker.infrastructure.messaging.stub.SynchronousApplicationEventsStub;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.cargotracker.Deployments.*;

@RunWith(Arquillian.class)
@Category(IntegrationTests.class)
public class CargoLifecycleScenarioTest {
    
    private static final Logger LOGGER = Logger.getLogger(CargoLifecycleScenarioTest.class.getName());
    
    @Deployment
    public static WebArchive createDeployment() {
        
        WebArchive war = ShrinkWrap.create(WebArchive.class);
        
        addExtraJars(war);
        addDomainModels(war);
        addInfraBase(war);
        addInfraPersistence(war);
        addApplicationBase(war);
        
        // add JMS package
        //addInfraMessaging(war);
        // Now pickup application service to setup in test.
        war .addClass(ApplicationEvents.class)
                // mock the applications events
                .addClass(SynchronousApplicationEventsStub.class)
                .addClass(BookingService.class)
                .addClass(HandlingEventService.class)
                .addClass(CargoInspectionService.class)
                // Application layer components
                .addClass(DefaultBookingService.class)
                .addClass(DefaultHandlingEventService.class)
                .addClass(DefaultCargoInspectionService.class);
        
        addDomainService(war);
        // add fake routing service to isolate the external APIs.
        war.addClass(CargoLifecycleScenarioTestRoutingService.class);
        
        // Sample data.
        war.addClass(SampleDataGenerator.class)
                .addClass(SampleLocations.class)
                .addClass(SampleVoyages.class);
        
        // add persistence unit descriptor
        war.addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml");
        //war.addAsWebInfResource("test-beans.xml", "beans.xml");
        
        LOGGER.log(Level.INFO, "War deployment: {0}", war.toString(true));
        
        return war;
    }
    
    /**
     * Repository implementations are part of the infrastructure layer, which in
     * this test is stubbed out by in-memory replacements.
     */
    HandlingEventRepository handlingEventRepository;
    
    @Inject
    CargoRepository cargoRepository;
    LocationRepository locationRepository;
    VoyageRepository voyageRepository;
    /**
     * This interface is part of the application layer, and defines a number of
     * events that occur during aplication execution. It is used for message-driving
     * and is implemented using JMS.
     * <p>
     * In this test it is stubbed with synchronous calls.
     */
    ApplicationEvents applicationEvents;
    /**
     * These three components all belong to the application layer, and map against
     * use cases of the application. The "real" implementations are used in this
     * lifecycle test, but wired with stubbed infrastructure.
     */
    @Inject
    BookingService bookingService;
    
    @Inject
    HandlingEventService handlingEventService;
    CargoInspectionService cargoInspectionService;
    /**
     * This factory is part of the handling aggregate and belongs to the domain
     * layer. Similar to the application layer components, the "real" implementation
     * is used here too, wired with stubbed infrastructure.
     */
    HandlingEventFactory handlingEventFactory;
    /**
     * This is a domain service interface, whose implementation is part of the
     * infrastructure layer (remote call to external system).
     * <p>
     * It is stubbed in this test.
     */
    RoutingService routingService;
    
    @Test
    public void testCargoFromHongkongToStockholm() throws Exception {
        /*
         * Test setup: A cargo should be shipped from Hongkong to
         * SampleLocations.STOCKHOLM, and it should arrive in no more than two weeks.
         */
        Location origin = SampleLocations.HONGKONG;
        Location destination = SampleLocations.STOCKHOLM;
        LocalDate arrivalDeadline = DateUtil.toDate("2009-03-18");
        
        /*
         * Use case 1: booking
         *
         * A new cargo is booked, and the unique tracking id is assigned to the cargo.
         */
        TrackingId trackingId = bookingService.bookNewCargo(origin.getUnLocode(), destination.getUnLocode(),
                arrivalDeadline);
        
        /*
         * The tracking id can be used to lookup the cargo in the repository.
         *
         * Important: The cargo, and thus the domain model, is responsible for
         * determining the status of the cargo, whether it is on the right track or not
         * and so on. This is core domain logic.
         *
         * Tracking the cargo basically amounts to presenting information extracted from
         * the cargo aggregate in a suitable way.
         */
        Cargo cargo = cargoRepository.find(trackingId);
        assertThat(cargo).isNotNull();
        assertThat(cargo.getDelivery().getTransportStatus()).isEqualTo(TransportStatus.NOT_RECEIVED);
        assertThat(cargo.getDelivery().getRoutingStatus()).isEqualTo(RoutingStatus.NOT_ROUTED);
        assertThat(cargo.getDelivery().isMisdirected()).isFalse();
        assertThat(cargo.getDelivery().getEstimatedTimeOfArrival()).isNull();
        assertThat(cargo.getDelivery().getNextExpectedActivity()).isNull();
        
        /*
         * Use case 2: routing
         *
         * A number of possible routes for this cargo is requested and may be presented
         * to the customer in some way for him/her to choose from. Selection could be
         * affected by things like price and time of delivery, but this test simply uses
         * an arbitrary selection to mimic that process.
         *
         * The cargo is then assigned to the selected route, described by an itinerary.
         */
        List<Itinerary> itineraries = bookingService.requestPossibleRoutesForCargo(trackingId);
        Itinerary itinerary = selectPreferredItinerary(itineraries);
        cargo.assignToRoute(itinerary);
        
        assertThat(cargo.getDelivery().getTransportStatus()).isEqualTo(TransportStatus.NOT_RECEIVED);
        assertThat(cargo.getDelivery().getRoutingStatus()).isEqualTo(RoutingStatus.ROUTED);
        assertThat(cargo.getDelivery().getEstimatedTimeOfArrival()).isNotNull();
        assertThat(cargo.getDelivery().getNextExpectedActivity())
                .isEqualTo(new HandlingActivity(HandlingEvent.Type.RECEIVE, SampleLocations.HONGKONG));
        
        /*
         * Use case 3: handling
         *
         * A handling event registration attempt will be formed from parsing the data
         * coming in as a handling report either via the web service interface or as an
         * uploaded CSV file.
         *
         * The handling event factory tries to create a HandlingEvent from the attempt,
         * and if the factory decides that this is a plausible handling event, it is
         * stored. If the attempt is invalid, for example if no cargo exists for the
         * specfied tracking id, the attempt is rejected.
         *
         * Handling begins: cargo is received in Hongkong.
         */
        handlingEventService.registerHandlingEvent(DateUtil.toDateTime("2009-03-01", "00:00"), trackingId, null,
                SampleLocations.HONGKONG.getUnLocode(), HandlingEvent.Type.RECEIVE);
        
        assertThat(cargo.getDelivery().getTransportStatus()).isEqualTo(TransportStatus.IN_PORT);
        assertThat(cargo.getDelivery().getLastKnownLocation()).isEqualTo(SampleLocations.HONGKONG);
        
        // Next event: Load onto voyage SampleVoyages.CM003 in Hongkong
        handlingEventService.registerHandlingEvent(DateUtil.toDateTime("2009-03-03", "00:00"), trackingId,
                SampleVoyages.v100.getVoyageNumber(), SampleLocations.HONGKONG.getUnLocode(), HandlingEvent.Type.LOAD);
        
        // Check current state - should be ok
        assertThat(cargo.getDelivery().getCurrentVoyage()).isEqualTo(SampleVoyages.v100);
        assertThat(cargo.getDelivery().getLastKnownLocation()).isEqualTo(SampleLocations.HONGKONG);
        assertThat(cargo.getDelivery().getTransportStatus()).isEqualTo(TransportStatus.ONBOARD_CARRIER);
        assertThat(cargo.getDelivery().isMisdirected()).isFalse();
        assertThat(cargo.getDelivery().getNextExpectedActivity())
                .isEqualTo(new HandlingActivity(HandlingEvent.Type.UNLOAD, SampleLocations.NEWYORK, SampleVoyages.v100));
        
        /*
         * Here's an attempt to register a handling event that's not valid because there
         * is no voyage with the specified voyage number, and there's no location with
         * the specified UN Locode either.
         *
         * This attempt will be rejected and will not affect the cargo delivery in any
         * way.
         */
        VoyageNumber noSuchVoyageNumber = new VoyageNumber("XX000");
        UnLocode noSuchUnLocode = new UnLocode("ZZZZZ");
        
        assertThatThrownBy(() -> handlingEventService.registerHandlingEvent(DateUtil.toDateTime("2009-03-05", "00:00"),
                trackingId,
                noSuchVoyageNumber,
                noSuchUnLocode,
                HandlingEvent.Type.LOAD),
                "Should not be able to register a handling event with invalid location and voyage"
        ).isInstanceOf(CannotCreateHandlingEventException.class);
//        try {
//            handlingEventService.registerHandlingEvent(DateUtil.toDateTime("2009-03-05", "00:00"), trackingId, noSuchVoyageNumber,
//                    noSuchUnLocode, HandlingEvent.Type.LOAD);
//            org.junit.Assert.fail("Should not be able to register a handling event with invalid location and voyage");
//        } catch (CannotCreateHandlingEventException expected) {
//        }
        
        // Cargo is now (incorrectly) unloaded in Tokyo
        handlingEventService.registerHandlingEvent(DateUtil.toDateTime("2009-03-05", "00:00"), trackingId,
                SampleVoyages.v100.getVoyageNumber(), SampleLocations.TOKYO.getUnLocode(), HandlingEvent.Type.UNLOAD);
        
        // Check current state - cargo is misdirected!
        assertThat(cargo.getDelivery().getCurrentVoyage()).isEqualTo(Voyage.NONE);
        assertThat(cargo.getDelivery().getLastKnownLocation()).isEqualTo(SampleLocations.TOKYO);
        assertThat(cargo.getDelivery().getTransportStatus()).isEqualTo(TransportStatus.IN_PORT);
        assertThat(cargo.getDelivery().isMisdirected()).isTrue();
        assertThat(cargo.getDelivery().getNextExpectedActivity()).isNotNull();
        
        // -- Cargo needs to be rerouted --
        // TODO [TDD] cleaner reroute from "earliest location from where the new route
        // originates"
        // Specify a new route, this time from Tokyo (where it was incorrectly unloaded)
        // to SampleLocations.STOCKHOLM
        RouteSpecification fromTokyo = new RouteSpecification(SampleLocations.TOKYO, SampleLocations.STOCKHOLM,
                arrivalDeadline);
        cargo.specifyNewRoute(fromTokyo);
        
        // The old itinerary does not satisfy the new specification
        assertThat(cargo.getDelivery().getRoutingStatus()).isEqualTo(RoutingStatus.MISROUTED);
        assertThat(cargo.getDelivery().getNextExpectedActivity()).isNull();
        
        // Repeat procedure of selecting one out of a number of possible routes
        // satisfying the route spec
        List<Itinerary> newItineraries = bookingService.requestPossibleRoutesForCargo(cargo.getTrackingId());
        Itinerary newItinerary = selectPreferredItinerary(newItineraries);
        cargo.assignToRoute(newItinerary);
        
        // New itinerary should satisfy new route
        assertThat(cargo.getDelivery().getRoutingStatus()).isEqualTo(RoutingStatus.ROUTED);
        
        // TODO [TDD] we can't handle the face that after a reroute, the cargo isn't
        // misdirected anymore
        // org.junit.Assert.assertFalse(cargo.isMisdirected());
        // org.junit.Assert.assertEquals(new HandlingActivity(HandlingEvent.Type.LOAD,
        // SampleLocations.TOKYO), cargo.getNextExpectedActivity());
        
        
        // -- Cargo has been rerouted, shipping continues --
        // Load in Tokyo
        handlingEventService.registerHandlingEvent(DateUtil.toDateTime("2009-03-08", "00:00"), trackingId,
                SampleVoyages.v300.getVoyageNumber(), SampleLocations.TOKYO.getUnLocode(), HandlingEvent.Type.LOAD);
        
        // Check current state - should be ok
        assertThat(cargo.getDelivery().getCurrentVoyage()).isEqualTo(SampleVoyages.v300);
        assertThat(cargo.getDelivery().getLastKnownLocation()).isEqualTo(SampleLocations.TOKYO);
        assertThat(cargo.getDelivery().getTransportStatus()).isEqualTo(TransportStatus.ONBOARD_CARRIER);
        assertThat(cargo.getDelivery().isMisdirected()).isFalse();
        assertThat(cargo.getDelivery().getNextExpectedActivity())
                .isEqualTo(new HandlingActivity(HandlingEvent.Type.UNLOAD, SampleLocations.HAMBURG, SampleVoyages.v300));
        
        // Unload in Hamburg
        handlingEventService.registerHandlingEvent(DateUtil.toDateTime("2009-03-12", "00:00"), trackingId,
                SampleVoyages.v300.getVoyageNumber(), SampleLocations.HAMBURG.getUnLocode(), HandlingEvent.Type.UNLOAD);
        
        // Check current state - should be ok
        assertThat(cargo.getDelivery().getCurrentVoyage()).isEqualTo(Voyage.NONE);
        assertThat(cargo.getDelivery().getLastKnownLocation()).isEqualTo(SampleLocations.HAMBURG);
        assertThat(cargo.getDelivery().getTransportStatus()).isEqualTo(TransportStatus.IN_PORT);
        assertThat(cargo.getDelivery().isMisdirected()).isFalse();
        assertThat(cargo.getDelivery().getNextExpectedActivity())
                .isEqualTo(new HandlingActivity(HandlingEvent.Type.LOAD, SampleLocations.HAMBURG, SampleVoyages.v400));
        
        // Load in Hamburg
        handlingEventService.registerHandlingEvent(DateUtil.toDateTime("2009-03-14", "00:00"), trackingId,
                SampleVoyages.v400.getVoyageNumber(), SampleLocations.HAMBURG.getUnLocode(), HandlingEvent.Type.LOAD);
        
        // Check current state - should be ok
        assertThat(cargo.getDelivery().getCurrentVoyage()).isEqualTo(SampleVoyages.v400);
        assertThat(cargo.getDelivery().getLastKnownLocation()).isEqualTo(SampleLocations.HAMBURG);
        assertThat(cargo.getDelivery().getTransportStatus()).isEqualTo(TransportStatus.ONBOARD_CARRIER);
        assertThat(cargo.getDelivery().isMisdirected()).isFalse();
        assertThat(cargo.getDelivery().getNextExpectedActivity())
                .isEqualTo(new HandlingActivity(HandlingEvent.Type.UNLOAD, SampleLocations.STOCKHOLM, SampleVoyages.v400));
        
        // Unload in SampleLocations.STOCKHOLM
        handlingEventService.registerHandlingEvent(DateUtil.toDateTime("2009-03-15", "00:00"), trackingId,
                SampleVoyages.v400.getVoyageNumber(), SampleLocations.STOCKHOLM.getUnLocode(),
                HandlingEvent.Type.UNLOAD);
        
        // Check current state - should be ok
        assertThat(cargo.getDelivery().getCurrentVoyage()).isEqualTo(Voyage.NONE);
        assertThat(cargo.getDelivery().getLastKnownLocation()).isEqualTo(SampleLocations.STOCKHOLM);
        assertThat(cargo.getDelivery().getTransportStatus()).isEqualTo(TransportStatus.IN_PORT);
        assertThat(cargo.getDelivery().isMisdirected()).isFalse();
        assertThat(cargo.getDelivery().getNextExpectedActivity())
                .isEqualTo(new HandlingActivity(HandlingEvent.Type.CLAIM, SampleLocations.STOCKHOLM));
        
        // Finally, cargo is claimed in SampleLocations.STOCKHOLM. This ends the cargo
        // lifecycle from our perspective.
        handlingEventService.registerHandlingEvent(DateUtil.toDateTime("2009-03-16", "00:00"), trackingId, null,
                SampleLocations.STOCKHOLM.getUnLocode(), HandlingEvent.Type.CLAIM);
        
        // Check current state - should be ok
        assertThat(cargo.getDelivery().getCurrentVoyage()).isEqualTo(Voyage.NONE);
        assertThat(cargo.getDelivery().getLastKnownLocation()).isEqualTo(SampleLocations.STOCKHOLM);
        assertThat(cargo.getDelivery().getTransportStatus()).isEqualTo(TransportStatus.CLAIMED);
        assertThat(cargo.getDelivery().isMisdirected()).isFalse();
        assertThat(cargo.getDelivery().getNextExpectedActivity()).isNull();
    }
    
    /*
     * Utility stubs below.
     */
    private Itinerary selectPreferredItinerary(List<Itinerary> itineraries) {
        return itineraries.get(0);
    }
    
    @Before
    public void setUp() throws Exception {
//        routingService = routeSpecification -> {
//            if (routeSpecification.getOrigin().equals(SampleLocations.HONGKONG)) {
//                // Hongkong - NYC - Chicago - SampleLocations.STOCKHOLM, initial routing
//                return Arrays.asList(new Itinerary(Arrays.asList(
//                        new Leg(SampleVoyages.v100, SampleLocations.HONGKONG, SampleLocations.NEWYORK,
//                                DateUtil.toDateTime("2009-03-03", "00:00"), DateUtil.toDateTime("2009-03-09", "00:00")),
//                        new Leg(SampleVoyages.v200, SampleLocations.NEWYORK, SampleLocations.CHICAGO,
//                                DateUtil.toDateTime("2009-03-10", "00:00"), DateUtil.toDateTime("2009-03-14", "00:00")),
//                        new Leg(SampleVoyages.v200, SampleLocations.CHICAGO, SampleLocations.STOCKHOLM,
//                                DateUtil.toDateTime("2009-03-07", "00:00"), DateUtil.toDateTime("2009-03-11", "00:00")))));
//            } else {
//                // Tokyo - Hamburg - SampleLocations.STOCKHOLM, rerouting misdirected cargo from
//                // Tokyo
//                return Arrays.asList(new Itinerary(Arrays.asList(
//                        new Leg(SampleVoyages.v300, SampleLocations.TOKYO, SampleLocations.HAMBURG,
//                                DateUtil.toDateTime("2009-03-08", "00:00"), DateUtil.toDateTime("2009-03-12", "00:00")),
//                        new Leg(SampleVoyages.v400, SampleLocations.HAMBURG, SampleLocations.STOCKHOLM,
//                                DateUtil.toDateTime("2009-03-14", "00:00"), DateUtil.toDateTime("2009-03-15", "00:00")))));
//            }
//        };

//        applicationEvents = new SynchronousApplicationEventsStub();
        // In-memory implementations of the repositories
//        handlingEventRepository = new HandlingEventRepositoryInMem();
//        cargoRepository = new CargoRepositoryInMem();
//        locationRepository = new LocationRepositoryInMem();
//        voyageRepository = new VoyageRepositoryInMem();
        // Actual factories and application services, wired with stubbed or in-memory
        // infrastructure
//        handlingEventFactory = new HandlingEventFactory(cargoRepository, voyageRepository, locationRepository);
//        cargoInspectionService = new CargoInspectionServiceImpl(applicationEvents, cargoRepository, handlingEventRepository);
//        handlingEventService = new DefaultHandlingEventService(handlingEventRepository, applicationEvents, handlingEventFactory);
//        bookingService = new BookingServiceImpl(cargoRepository, locationRepository, routingService);
        // Circular dependency when doing synchrounous calls
//        ((SynchronousApplicationEventsStub) applicationEvents).setCargoInspectionService(cargoInspectionService);
    }
}
