package org.apache.camel.example.cdi.util;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jms.AtomikosConnectionFactoryBean;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

@Slf4j
@ApplicationScoped
public class CdiProducers {


    @Produces
    @ApplicationScoped
    ConnectionFactory createJmsConnectionFactory() {
        log.debug(">>>>>> createJmsConnectionFactory");
        ActiveMQXAConnectionFactory activeMQXAConnectionFactory = new ActiveMQXAConnectionFactory();
        activeMQXAConnectionFactory.setBrokerURL("vm://localhost?broker.persistent=false");
        //activeMQXAConnectionFactory.setBrokerURL("tcp://localhost:61616");
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setMaximumRedeliveries(0);
        activeMQXAConnectionFactory.setRedeliveryPolicy(redeliveryPolicy);
        AtomikosConnectionFactoryBean atomikosConnectionFactoryBean = new AtomikosConnectionFactoryBean();
        atomikosConnectionFactoryBean.setMinPoolSize(1);
        atomikosConnectionFactoryBean.setMaxPoolSize(1);
        atomikosConnectionFactoryBean.setUniqueResourceName("xamq");
        atomikosConnectionFactoryBean.setLocalTransactionMode(false);
        atomikosConnectionFactoryBean.setXaConnectionFactory(activeMQXAConnectionFactory);
        try {
            atomikosConnectionFactoryBean.init();
        } catch (JMSException ex) {
            log.error(ex.getLocalizedMessage());
        }
        return atomikosConnectionFactoryBean;
    }

    @Produces
    public UserTransaction userTransaction() throws Throwable {
        log.debug(">>>>>> userTransaction");
        UserTransactionImp userTransactionImp = new UserTransactionImp();
        userTransactionImp.setTransactionTimeout(1000);
        return userTransactionImp;
    }

    @Produces
    @ApplicationScoped
    public TransactionManager userTransactionManager() throws Throwable {
        log.debug(">>>>>> userTransactionManager");
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
        log.debug(">>>>>> createTransactionManager");
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager(userTransaction, userTransactionManager);
        jtaTransactionManager.setUserTransaction(userTransaction);
        jtaTransactionManager.setTransactionManager(userTransactionManager);
        
        jtaTransactionManager.afterPropertiesSet();
        return jtaTransactionManager;
    }

}
