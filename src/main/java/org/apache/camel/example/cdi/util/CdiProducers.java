package org.apache.camel.example.cdi.util;

import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jms.AtomikosConnectionFactoryBean;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
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
        System.out.println(">>> createJmsConnectionFactory");
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
    public TransactionManager userTransactionManager() throws Throwable {
        System.out.println(">>> userTransactionManager");
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        userTransactionManager.setForceShutdown(false);
        userTransactionManager.setTransactionTimeout(120);
        userTransactionManager.init();
        return userTransactionManager;
    }

    @Produces
    @ApplicationScoped
    @Named("jtaTransactionManager")
    PlatformTransactionManager createTransactionManager(UserTransaction userTransaction, TransactionManager userTransactionManager) {
        System.out.println(">>> createTransactionManager");
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager(userTransaction, userTransactionManager);
        jtaTransactionManager.afterPropertiesSet();
        return jtaTransactionManager;
    }

    @Produces
    @Named("activemq")
    @ApplicationScoped
    ActiveMQComponent createActiveMQComponent(PlatformTransactionManager transactionManager, ConnectionFactory jmsConnectionFactory) {
        System.out.println(">>> createActiveMQComponent");
        ActiveMQConfiguration activeMQConfiguration = new ActiveMQConfiguration();
        activeMQConfiguration.setConnectionFactory(jmsConnectionFactory);
        activeMQConfiguration.setTransactionManager(transactionManager);
        activeMQConfiguration.setConcurrentConsumers(1);
        activeMQConfiguration.setUsePooledConnection(true);
        activeMQConfiguration.setTransacted(true);
        activeMQConfiguration.setCacheLevelName("CACHE_NONE");
        return new ActiveMQComponent(activeMQConfiguration);
    }

    public void closeUserTransactionManager(@Disposes TransactionManager transactionManager) {
        System.out.println(">>> closeUserTransactionManager");
        ((UserTransactionManager) transactionManager).close();
    }

    public void closeJmsConnectionFactory(@Disposes ConnectionFactory connectionFactory) {
        System.out.println(">>> closeJmsConnectionFactory");
        ((AtomikosConnectionFactoryBean) connectionFactory).close();
    }

}
