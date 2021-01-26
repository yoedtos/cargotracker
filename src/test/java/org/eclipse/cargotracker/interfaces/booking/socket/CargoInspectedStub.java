package org.eclipse.cargotracker.interfaces.booking.socket;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.RouteSpecification;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.infrastructure.events.cdi.CargoInspected;

@Startup
@Singleton
public class CargoInspectedStub {
  private static final Logger LOGGER = Logger.getLogger(CargoInspectedStub.class.getName());

  @Inject @CargoInspected Event<Cargo> cargoEvent;

  @Resource TimerService timerService;

  @PostConstruct
  public void initialize() {
    LOGGER.log(Level.INFO, "starting timer service...");
    timerService.createTimer(TimeUnit.SECONDS.toMillis(5), "delayed 5 seconds to execute");
  }

  @Timeout
  public void raiseEvent(Timer timer) {
    LOGGER.log(Level.INFO, "raising event: {0}", timer.getInfo());
    cargoEvent.fire(
        new Cargo(
            new TrackingId("AAA"),
            new RouteSpecification(
                SampleLocations.HONGKONG, SampleLocations.NEWYORK, LocalDate.now().plusMonths(6))));
  }
}
