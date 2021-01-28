package org.eclipse.cargotracker.interfaces.booking.rest;

import org.eclipse.cargotracker.IntegrationTests;
import org.eclipse.cargotracker.application.util.RestConfiguration;
import org.eclipse.cargotracker.application.util.SampleDataGenerator;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.cargotracker.Deployments.*;

@RunWith(Arquillian.class)
@Category(IntegrationTests.class)
public class CargoMonitoringServiceTest {
    private static final Logger LOGGER =
            Logger.getLogger(CargoMonitoringServiceTest.class.getName());
    @ArquillianResource private URL base;
    private Client client;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "test-CargoMonitoringServiceTest.war");

        addExtraJars(war);
        addDomainModels(war);
        addDomainRepositories(war);
        addInfraBase(war);
        addInfraPersistence(war);
        addApplicationBase(war);

        war.addClass(RestConfiguration.class).addClass(CargoMonitoringService.class);
        war.addClass(SampleDataGenerator.class)
                .addClass(SampleLocations.class)
                .addClass(SampleVoyages.class)
                // add persistence unit descriptor
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")

                // add web xml
                .addAsWebInfResource("test-web.xml", "web.xml")

                // add Wildfly specific deployment descriptor
                .addAsWebInfResource(
                        "test-jboss-deployment-structure.xml", "jboss-deployment-structure.xml");

        LOGGER.log(Level.INFO, "War deployment: {0}", war.toString(true));

        return war;
    }

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
    public void testGetAllPosts() throws Exception {
        LOGGER.log(Level.INFO, " Running test:: CargoMonitoringServiceTest#testGetAllPosts ... ");
        final WebTarget getAllPostsTarget =
                client.target(new URL(base, "rest/cargo").toExternalForm());

        // Response is an autocloseable resource.
        try (final Response getAllPostsResponse =
                getAllPostsTarget.request().accept(MediaType.APPLICATION_JSON).get()) {
            assertThat(getAllPostsResponse.getStatus()).isEqualTo(200);
            // TODO: use POJO to assert the response body.
        }
    }
}
