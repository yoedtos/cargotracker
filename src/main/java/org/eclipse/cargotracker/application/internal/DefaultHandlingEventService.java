package org.eclipse.cargotracker.application.internal;

import java.time.LocalDateTime;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.application.HandlingEventService;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.CannotCreateHandlingEventException;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventFactory;
import org.eclipse.cargotracker.domain.model.handling.HandlingEventRepository;
import org.eclipse.cargotracker.domain.model.location.UnLocode;
import org.eclipse.cargotracker.domain.model.voyage.VoyageNumber;

// TODO [Jakarta EE 8] Adopt the Date-Time API.
@Stateless
public class DefaultHandlingEventService implements HandlingEventService {
	
	private static final Logger LOGGER = Logger.getLogger(DefaultHandlingEventService.class.getName());
	
	private final ApplicationEvents applicationEvents;
	
	private final HandlingEventRepository handlingEventRepository;
	
	private final HandlingEventFactory handlingEventFactory;
	
	@Inject
	public DefaultHandlingEventService(ApplicationEvents applicationEvents, HandlingEventRepository handlingEventRepository, HandlingEventFactory handlingEventFactory) {
		this.applicationEvents = applicationEvents;
		this.handlingEventRepository = handlingEventRepository;
		this.handlingEventFactory = handlingEventFactory;
	}
	
	
	@Override
	public void registerHandlingEvent(LocalDateTime completionTime, TrackingId trackingId, VoyageNumber voyageNumber,
									  UnLocode unLocode, HandlingEvent.Type type) throws CannotCreateHandlingEventException {
		LocalDateTime registrationTime = LocalDateTime.now();

		/*
		 * Using a factory to create a HandlingEvent (aggregate). This is where it is
		 * determined wether the incoming data, the attempt, actually is capable of
		 * representing a real handling event.
		 */
		HandlingEvent event = handlingEventFactory.createHandlingEvent(registrationTime, completionTime, trackingId,
				voyageNumber, unLocode, type);

		/*
		 * Store the new handling event, which updates the persistent state of the
		 * handling event aggregate (but not the cargo aggregate - that happens
		 * asynchronously!)
		 */
		handlingEventRepository.store(event);

		/* Publish an event stating that a cargo has been handled. */
		applicationEvents.cargoWasHandled(event);
		
		LOGGER.info("Registered handling event");
	}

}
