package org.apache.camel.example.cdi.util;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.UserTransactionImple;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.jms.ConnectionFactory;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.camel.component.ActiveMQConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

@Slf4j
@ApplicationScoped
public class CdiProducers {

    @Produces
    @ApplicationScoped
    ConnectionFactory createJmsConnectionFactory() throws Exception {
        ActiveMQXAConnectionFactory activeMQXAConnectionFactory = new ActiveMQXAConnectionFactory();
        activeMQXAConnectionFactory.setBrokerURL("vm://localhost?broker.persistent=false&broker.useJmx=true");
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setMaximumRedeliveries(0);
        activeMQXAConnectionFactory.setRedeliveryPolicy(redeliveryPolicy);
        return activeMQXAConnectionFactory;
    }

    @Produces
    public UserTransaction userTransaction() throws Throwable {
        UserTransactionImple userTransactionImp = new UserTransactionImple();
        userTransactionImp.setTransactionTimeout(1000);
        return userTransactionImp;
    }

    @Produces
    @ApplicationScoped
    public TransactionManager userTransactionManager() throws Throwable {
        TransactionManagerImple userTransactionManager = new TransactionManagerImple();
        userTransactionManager.setTransactionTimeout(20);
        return userTransactionManager;
    }


    @Produces
    @ApplicationScoped
    PlatformTransactionManager createTransactionManager(UserTransaction userTransaction, TransactionManager userTransactionManager) {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager(userTransaction, userTransactionManager);
        jtaTransactionManager.afterPropertiesSet();
        return jtaTransactionManager;
    }

    @Produces
    @Named("activemq")
    @ApplicationScoped
    ActiveMQComponent createActiveMQComponent(PlatformTransactionManager transactionManager, ConnectionFactory jmsConnectionFactory) throws Exception {
        ActiveMQConfiguration activeMQConfiguration = new ActiveMQConfiguration();
        activeMQConfiguration.setConnectionFactory(jmsConnectionFactory);
        activeMQConfiguration.setTransactionManager(transactionManager);
        activeMQConfiguration.setTransacted(false);
        activeMQConfiguration.setCacheLevelName("CACHE_CONNECTION");
        return new ActiveMQComponent(activeMQConfiguration);
    }
}
