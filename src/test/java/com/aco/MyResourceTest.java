package com.aco;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Ignore;
import static org.junit.Assert.assertEquals;

import com.aco.ACOResource;

public class MyResourceTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(ACOResource.class);
    }

    /**
     * Test to see that the message "Got it!" is sent in the response.
     */
    @Ignore
    public void testGetIt() {
        final String responseMsg = target().path("/api/v1.0").request().get(String.class);

        assertEquals("Got it!", responseMsg);
    }
}
