package org.eclipse.cargotracker.infrastructure.messaging.stub;


import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.application.CargoInspectionService;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
public class SynchronousApplicationEventsStub implements ApplicationEvents {
    
    @Inject
    private Instance<CargoInspectionService> cargoInspectionService;
    
    //no-args constructor required by CDI
    public SynchronousApplicationEventsStub() {
    }
    
    @Override
    public void cargoWasHandled(HandlingEvent event) {
        System.out.println("EVENT: cargo was handled: " + event);
        cargoInspectionService.get().inspectCargo(event.getCargo().getTrackingId());
    }
    
    @Override
    public void cargoWasMisdirected(Cargo cargo) {
        System.out.println("EVENT: cargo was misdirected");
    }
    
    @Override
    public void cargoHasArrived(Cargo cargo) {
        System.out.println("EVENT: cargo has arrived: " + cargo.getTrackingId().getIdString());
    }
    
    @Override
    public void receivedHandlingEventRegistrationAttempt(HandlingEventRegistrationAttempt attempt) {
        System.out.println("EVENT: received handling event registration attempt");
    }
}
