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

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.cdi.CdiRouteBuilder;
import org.apache.camel.management.event.CamelContextStartedEvent;

/**
 * Configures all our Camel routes, components, endpoints and beans
 */
public class MyRoutes extends CdiRouteBuilder {

    @Produce(uri = "activemq:inbound")
    ProducerTemplate activemqInbound;

    @Inject
    UserTransaction userTransaction;

    @Override
    public void configure() {
        
        //@formatter:off

        errorHandler(transactionErrorHandler()
                .setTransactionPolicy("PROPAGATION_SUPPORTS")
                .maximumRedeliveries(0));

        from("activemq:inbound")
                .transacted()
                .routeId("test")
                .process((Exchange exchange) -> {
                    if (exchange.getIn().getBody(String.class).contains("trigger rollback")) {
                        throw new IllegalStateException("Triggering Rollback");
                    }
                })
                .to("log:out");

        //@formatter:on
    }

    void onContextStarted(@Observes CamelContextStartedEvent event) throws Exception{
        log.info("Context started: {}", event);

        userTransaction.begin();
        log.info("Sending: message 1 - ok");
        activemqInbound.sendBody("message 1 - ok");
        log.info("Sending: message 2 - ok - trigger rollback");
        activemqInbound.sendBody("message 2 - ok - trigger rollback");
        userTransaction.commit();

        log.info("Sending: message 3 - fails");
        activemqInbound.sendBody("message 3 - fails");


        

    }

}
