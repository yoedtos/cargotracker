package org.eclipse.cargotracker.interfaces.booking.socket;

import org.eclipse.cargotracker.IntegrationTests;
import org.eclipse.cargotracker.domain.model.location.SampleLocations;
import org.eclipse.cargotracker.domain.model.voyage.SampleVoyages;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.cargotracker.Deployments.*;

@RunWith(Arquillian.class)
@Category(IntegrationTests.class)
public class RealtimeCargoTrackingServiceTest {

    private static final Logger LOGGER = Logger.getLogger(RealtimeCargoTrackingServiceTest.class.getName());

    @Deployment(testable = false)
    public static WebArchive createDeployment() {

        WebArchive war = ShrinkWrap.create(WebArchive.class, "test-RealtimeCargoTrackingServiceTest.war");

        addExtraJars(war);
        addDomainModels(war);
        addInfraBase(war);
        addApplicationBase(war);
        war.addClass(RealtimeCargoTrackingService.class)
                //.addClass(TestClient.class)
                // EJB to raise a CDI event
                .addClass(CargoInspectedStub.class)
                //add samples.
                .addClass(SampleLocations.class).addClass(SampleVoyages.class)
                // add web xml
                .addAsWebInfResource("test-web.xml", "web.xml")
                // add Wildfly specific deployment descriptor
                .addAsWebInfResource("test-jboss-deployment-structure.xml", "jboss-deployment-structure.xml");

        LOGGER.log(Level.INFO, "War deployment: {0}", war.toString(true));

        return war;
    }

    @ArquillianResource
    URL base;

    @Test
    public void testOnCargoInspected() throws Exception {
        LOGGER.log(Level.INFO, "run test RealtimeCargoTrackingServiceTest# testOnCargoInspected");
        TestClient.latch = new CountDownLatch(1);
        var session = connectToServer();
        assertThat(session).isNotNull();
        TestClient.latch.await(5, TimeUnit.SECONDS);
        assertThat(TestClient.response).isNotNull();
        // assert json data.
    }

    public Session connectToServer() throws DeploymentException, IOException, URISyntaxException {
        var container = ContainerProvider.getWebSocketContainer();
        URI uri = new URI("ws://"
                + base.getHost()
                + ":"
                + base.getPort()
                + base.getPath()
                + "tracking"
        );

        LOGGER.log(Level.INFO, "connected to url: {0}", uri);
        return container.connectToServer(TestClient.class, uri);
    }


}