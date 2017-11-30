/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.example.cdi;

import java.lang.management.ManagementFactory;
import javax.enterprise.event.Observes;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.cdi.CdiRouteBuilder;
import org.jboss.weld.environment.se.events.ContainerInitialized;

/**
 * Configures all our Camel routes, components, endpoints and beans
 */
public class MyRoutes extends CdiRouteBuilder {

    private static final String MQ_INBOUND_ENDPOINT = "activemq:inbound?transacted=true";
    private static final String MQ_MIDDLE_ENDPOINT = "activemq:middle?transacted=true";

    @Produce(uri = MQ_INBOUND_ENDPOINT)
    ProducerTemplate activemqInbound;

    @Override
    public void configure() {

        //@formatter:off
        errorHandler(transactionErrorHandler()
                .setTransactionPolicy("PROPAGATION_SUPPORTS")
                .logStackTrace(false)
                .maximumRedeliveries(0));

        from(MQ_INBOUND_ENDPOINT)
                .transacted()
                .routeId("test")
                .to(MQ_MIDDLE_ENDPOINT)
                .process((Exchange exchange) -> {
                    if (exchange.getIn().getBody(String.class).contains("trigger rollback")) {
                        throw new IllegalStateException("Triggering Rollback");
                    }
                })
                .to("log:out");

        //@formatter:on
    }

    void onContextStarted(@Observes ContainerInitialized event) throws Exception {
        log.info("ContainerInitialized: {}", event);

        activemqInbound.sendBody("message 3 - ok");
        activemqInbound.sendBody("message 4- trigger rollback");

        Thread.sleep(2000);

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        ObjectName inboundQueueName = ObjectName.getInstance("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=inbound");
        System.out.println("inbound QueueSize = " + mbs.getAttribute(inboundQueueName, "QueueSize"));

        ObjectName middleQueueName = ObjectName.getInstance("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=middle");
        System.out.println("middle QueueSize = " + mbs.getAttribute(middleQueueName, "QueueSize"));

    }

}
