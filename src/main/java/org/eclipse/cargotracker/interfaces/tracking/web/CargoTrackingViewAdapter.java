package org.eclipse.cargotracker.interfaces.tracking.web;

import org.eclipse.cargotracker.application.util.DateUtil;
import org.eclipse.cargotracker.domain.model.cargo.Cargo;
import org.eclipse.cargotracker.domain.model.cargo.Delivery;
import org.eclipse.cargotracker.domain.model.cargo.HandlingActivity;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** View adapter for displaying a cargo in a tracking context. */
public class CargoTrackingViewAdapter {

    // public static final String DT_PATTERN = "MM/dd/yyyy hh:mm a z";
    // private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DT_PATTERN);

    private final Cargo cargo;
    private final List<HandlingEventViewAdapter> events;

    public CargoTrackingViewAdapter(Cargo cargo, List<HandlingEvent> handlingEvents) {
        this.cargo = cargo;
        this.events = new ArrayList<>(handlingEvents.size());

        handlingEvents.stream().map(HandlingEventViewAdapter::new).forEach(events::add);
    }

    public String getTrackingId() {
        return cargo.getTrackingId().getIdString();
    }

    public String getOriginName() {
        return cargo.getRouteSpecification().getOrigin().getName();
    }

    public String getOriginCode() {
        return cargo.getRouteSpecification().getOrigin().getUnLocode().getIdString();
    }

    public String getDestinationName() {
        return cargo.getRouteSpecification().getDestination().getName();
    }

    public String getDestinationCode() {
        return cargo.getRouteSpecification().getDestination().getUnLocode().getIdString();
    }

    public String getLastKnownLocationName() {
        return cargo.getDelivery()
                        .getLastKnownLocation()
                        .getUnLocode()
                        .getIdString()
                        .equals("XXXXX")
                ? "Unknown"
                : cargo.getDelivery().getLastKnownLocation().getName();
    }

    public String getLastKnownLocationCode() {
        return cargo.getDelivery().getLastKnownLocation().getUnLocode().getIdString();
    }

    public String getStatusCode() {
        if (cargo.getItinerary().getLegs().isEmpty()) {
            return "NOT_ROUTED";
        }

        if (cargo.getDelivery().isUnloadedAtDestination()) {
            return "AT_DESTINATION";
        }

        if (cargo.getDelivery().isMisdirected()) {
            return "MISDIRECTED";
        }

        return cargo.getDelivery().getTransportStatus().name();
    }

    /**
     * @return A readable string describing the cargo status.
     */
    public String getStatusText() {
        Delivery delivery = cargo.getDelivery();

        switch (delivery.getTransportStatus()) {
            case IN_PORT:
                return "In port " + cargo.getRouteSpecification().getDestination().getName();
            case ONBOARD_CARRIER:
                return "Onboard voyage "
                        + delivery.getCurrentVoyage().getVoyageNumber().getIdString();
            case CLAIMED:
                return "Claimed";
            case NOT_RECEIVED:
                return "Not received";
            case UNKNOWN:
                return "Unknown";
            default:
                return "[Unknown status]"; // Should never happen.
        }
    }

    public boolean isMisdirected() {
        return cargo.getDelivery().isMisdirected();
    }

    public String getEta() {
        LocalDateTime eta = cargo.getDelivery().getEstimatedTimeOfArrival();

        if (eta == null) {
            return "?";
        } else {
            return DateUtil.toString(eta);
        }
    }

    public String getNextExpectedActivity() {
        HandlingActivity activity = cargo.getDelivery().getNextExpectedActivity();

        if ((activity == null) || (activity.isEmpty())) {
            return "";
        }

        String text = "Next expected activity is to ";
        HandlingEvent.Type type = activity.getType();

        if (type.sameValueAs(HandlingEvent.Type.LOAD)) {
            return text
                    + type.name().toLowerCase()
                    + " cargo onto voyage "
                    + activity.getVoyage().getVoyageNumber()
                    + " in "
                    + activity.getLocation().getName();
        } else if (type.sameValueAs(HandlingEvent.Type.UNLOAD)) {
            return text
                    + type.name().toLowerCase()
                    + " cargo off of "
                    + activity.getVoyage().getVoyageNumber()
                    + " in "
                    + activity.getLocation().getName();
        } else {
            return text
                    + type.name().toLowerCase()
                    + " cargo in "
                    + activity.getLocation().getName();
        }
    }

    /**
     * @return An unmodifiable list of handling event view adapters.
     */
    public List<HandlingEventViewAdapter> getEvents() {
        return Collections.unmodifiableList(events);
    }

    /** Handling event view adapter component. */
    public class HandlingEventViewAdapter {

        private final HandlingEvent handlingEvent;

        private boolean expected;

        public HandlingEventViewAdapter(HandlingEvent handlingEvent) {
            this.handlingEvent = handlingEvent;
            // move this executed before rendering the view.
            this.expected = cargo.getItinerary().isExpected(handlingEvent);
        }

        /**
         * @return the date in the format MM/dd/yyyy hh:mm a z
         */
        public String getTime() {
            // return
            // handlingEvent.getCompletionTime().format(DateTimeFormatter.ofPattern(DT_PATTERN));
            return DateUtil.toString(handlingEvent.getCompletionTime());
        }

        public boolean isExpected() {
            // This will cause Hibernate lazy initialization exception thrown in WildFly.
            // return cargo.getItinerary().isExpected(handlingEvent);
            return this.expected;
        }

        public String getDescription() {
            switch (handlingEvent.getType()) {
                case LOAD:
                    return "Loaded onto voyage "
                            + handlingEvent.getVoyage().getVoyageNumber().getIdString()
                            + " in "
                            + handlingEvent.getLocation().getName();
                case UNLOAD:
                    return "Unloaded off voyage "
                            + handlingEvent.getVoyage().getVoyageNumber().getIdString()
                            + " in "
                            + handlingEvent.getLocation().getName();
                case RECEIVE:
                    return "Received in " + handlingEvent.getLocation().getName();
                case CLAIM:
                    return "Claimed in " + handlingEvent.getLocation().getName();
                case CUSTOMS:
                    return "Cleared customs in " + handlingEvent.getLocation().getName();
                default:
                    return "[Unknown]";
            }
        }
    }
}
