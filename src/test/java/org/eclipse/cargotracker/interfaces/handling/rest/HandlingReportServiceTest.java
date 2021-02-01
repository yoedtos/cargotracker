package org.eclipse.cargotracker.interfaces.handling.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.cargotracker.Deployments.addApplicationBase;
import static org.eclipse.cargotracker.Deployments.addDomainModels;
import static org.eclipse.cargotracker.Deployments.addExtraJars;
import static org.eclipse.cargotracker.Deployments.addInfraBase;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.eclipse.cargotracker.IntegrationTests;
import org.eclipse.cargotracker.application.ApplicationEvents;
import org.eclipse.cargotracker.application.util.RestConfiguration;
import org.eclipse.cargotracker.domain.model.cargo.TrackingId;
import org.eclipse.cargotracker.domain.model.handling.HandlingEvent.Type;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.eclipse.cargotracker.interfaces.handling.HandlingEventRegistrationAttempt;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(IntegrationTests.class)
public class HandlingReportServiceTest {

    private static final Logger LOGGER =
            Logger.getLogger(HandlingReportServiceTest.class.getName());

    @Deployment()
    public static WebArchive createDeployment() {

        WebArchive war = ShrinkWrap.create(WebArchive.class, "test-HandlingReportServiceTest.war");

        addExtraJars(war);
        addDomainModels(war);
        addInfraBase(war);
        addApplicationBase(war);
        war.addClass(HandlingReportService.class)
                .addClass(HandlingReport.class)
                .addClass(ApplicationEventsStub.class)
                .addClass(ApplicationEvents.class)
                .addClass(HandlingEventRegistrationAttempt.class)
                // rest config.
                .addClass(RestConfiguration.class)
                // add samples.
                .addClass(SampleLocations.class)
                .addClass(SampleVoyages.class)
                .addAsResource("test-jboss-logging.properties", "jboss-logging.properties")
                // add web xml
                .addAsWebInfResource("test-web.xml", "web.xml")
                // add Wildfly specific deployment descriptor
                .addAsWebInfResource(
                        "test-jboss-deployment-structure.xml", "jboss-deployment-structure.xml");

        LOGGER.log(Level.INFO, "War deployment: {0}", war.toString(true));

        return war;
    }

    @ArquillianResource URL base;

    @Inject ApplicationEventsStub applicationEventsStub;

    private Client client;

    @Before
    public void setup() {
        this.client = ClientBuilder.newClient();
    }

    @After
    public void teardown() {
        if (this.client != null) {
            this.client.close();
        }
    }

    @Test
    public void submitReport() throws MalformedURLException {
        HandlingReport report = new HandlingReport();
        report.setCompletionTime("2021-02-01 04:26");
        report.setEventType("LOAD");
        report.setTrackingId("A001");
        report.setVoyageNumber(SampleVoyages.HONGKONG_TO_NEW_YORK.getVoyageNumber().getIdString());
        report.setUnLocode(SampleLocations.HONGKONG.getUnLocode().getIdString());

        final WebTarget postReportTarget =
                client.target(new URL(base, "rest/handling/reports").toExternalForm());

        // Response is an autocloseable resource.
        try (final Response postReportResponse =
                postReportTarget.request().post(Entity.json(report))) {
            assertThat(postReportResponse.getStatus()).isEqualTo(202);
            LOGGER.log(
                    Level.INFO,
                    "response of POST rest/handling/reports: {0}",
                    postReportResponse.getEntity().toString());

            assertThat(applicationEventsStub.getAttempt()).isNotNull();
            var attempt = applicationEventsStub.getAttempt();

            assertThat(attempt.getTrackingId()).isEqualTo(new TrackingId("A001"));
            assertThat(attempt.getType()).isEqualTo(Type.LOAD);
            assertThat(attempt.getVoyageNumber())
                    .isEqualTo(SampleVoyages.HONGKONG_TO_NEW_YORK.getVoyageNumber());
        }
    }
}
