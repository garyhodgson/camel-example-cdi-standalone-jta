package org.apache.camel.example.cdi.util;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jms.AtomikosConnectionFactoryBean;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.jms.ConnectionFactory;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.camel.component.ActiveMQConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

public class CdiProducers {

    @Produces
    @ApplicationScoped
    ConnectionFactory createJmsConnectionFactory() {
        ActiveMQXAConnectionFactory activeMQXAConnectionFactory = new ActiveMQXAConnectionFactory();
        activeMQXAConnectionFactory.setBrokerURL("vm://localhost?broker.persistent=false");
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setMaximumRedeliveries(0);
        activeMQXAConnectionFactory.setRedeliveryPolicy(redeliveryPolicy);
        AtomikosConnectionFactoryBean atomikosConnectionFactoryBean = new AtomikosConnectionFactoryBean();
        atomikosConnectionFactoryBean.setMinPoolSize(1);
        atomikosConnectionFactoryBean.setMaxPoolSize(1);
        atomikosConnectionFactoryBean.setUniqueResourceName("xamq");
        atomikosConnectionFactoryBean.setLocalTransactionMode(false);
        atomikosConnectionFactoryBean.setXaConnectionFactory(activeMQXAConnectionFactory);
        return atomikosConnectionFactoryBean;
    }

    @Produces
    @ApplicationScoped
    public UserTransaction userTransaction() throws Throwable {
        UserTransactionImp userTransactionImp = new UserTransactionImp();
        userTransactionImp.setTransactionTimeout(1000);
        return userTransactionImp;
    }

    @Produces
    @ApplicationScoped
    public TransactionManager userTransactionManager() throws Throwable {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        userTransactionManager.setForceShutdown(false);
        userTransactionManager.setTransactionTimeout(120);
        userTransactionManager.init();
        return userTransactionManager;
    }

    @Produces
    @Named("jtaTransactionManager")
    @ApplicationScoped
    PlatformTransactionManager createTransactionManager(UserTransaction userTransaction, TransactionManager userTransactionManager) {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager(userTransaction, userTransactionManager);
        jtaTransactionManager.afterPropertiesSet();
        return jtaTransactionManager;
    }

    @Produces
    @Named("activemq")
    @ApplicationScoped
    ActiveMQComponent createActiveMQComponent(PlatformTransactionManager transactionManager, ConnectionFactory jmsConnectionFactory) {
        ActiveMQConfiguration activeMQConfiguration = new ActiveMQConfiguration();
        activeMQConfiguration.setConnectionFactory(jmsConnectionFactory);
        activeMQConfiguration.setTransactionManager(transactionManager);
        activeMQConfiguration.setConcurrentConsumers(5);
        activeMQConfiguration.setUsePooledConnection(true);
        activeMQConfiguration.setTransacted(true);
        activeMQConfiguration.setCacheLevelName("CACHE_NONE");
        return new ActiveMQComponent(activeMQConfiguration);
    }

}
