package org.apache.camel.example.cdi.util;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jms.AtomikosConnectionFactoryBean;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
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
        System.out.println(">>>>>>>>>>>>>>>> createJmsConnectionFactory");
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

        atomikosConnectionFactoryBean.init();

        System.out.println("<<<<<<<<<<<<<< createJmsConnectionFactory");
        return atomikosConnectionFactoryBean;
    }

    public void closeJmsConnectionFactory(@Disposes ConnectionFactory connectionFactory) {
        System.out.println(">>>>>>>>>>>>>>>>>>>> closeJmsConnectionFactory");
        ((AtomikosConnectionFactoryBean) connectionFactory).close();
    }

    @Produces
    @ApplicationScoped
    public UserTransaction userTransaction() throws Throwable {
        System.out.println(">>>>>>>>>>>>>>>> userTransaction");
        UserTransactionImp userTransactionImp = new UserTransactionImp();
        userTransactionImp.setTransactionTimeout(1000);
        System.out.println("<<<<<<<<<<<< userTransaction");
        return userTransactionImp;
    }

    @Produces
    @ApplicationScoped
    public TransactionManager userTransactionManager() throws Throwable {
        System.out.println(">>>>>>>>>>>>>>>> userTransactionManager");
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        userTransactionManager.setForceShutdown(false);
        userTransactionManager.setTransactionTimeout(20);
        userTransactionManager.init();
        System.out.println("<<<<<<<<<<<<<< userTransactionManager");
        return userTransactionManager;
    }

    public void closeUserTransactionManager(@Disposes TransactionManager transactionManager) {
        System.out.println(">>>>>>>>>>>>>>>>>>>> closeUserTransactionManager");
        ((UserTransactionManager) transactionManager).close();
    }

    @Produces
    @Named("jtaTransactionManager")
    @ApplicationScoped
    PlatformTransactionManager createTransactionManager(UserTransaction userTransaction, TransactionManager userTransactionManager) {
        System.out.println(">>>>>>>>>>>>>>>> createTransactionManager");
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager(userTransaction, userTransactionManager);
        jtaTransactionManager.afterPropertiesSet();
        System.out.println("<<<<<<<<<<<<< createTransactionManager");
        return jtaTransactionManager;
    }

    @Produces
    @Named("activemq")
    @ApplicationScoped
    ActiveMQComponent createActiveMQComponent(PlatformTransactionManager transactionManager, ConnectionFactory jmsConnectionFactory) throws Exception {
        System.out.println(">>>>>>>>>>>>>>>> createActiveMQComponent");
        System.out.println(">>>>>>>>>>>>>>>> jmsConnectionFactory " + jmsConnectionFactory);
        System.out.println(">>>>>>>>>>>>>>>> transactionManager = " + transactionManager);
        ActiveMQConfiguration activeMQConfiguration = new ActiveMQConfiguration();
        activeMQConfiguration.setConnectionFactory(jmsConnectionFactory);
        activeMQConfiguration.setTransactionManager(transactionManager);
        activeMQConfiguration.setTransacted(false);
        System.out.println("<<<<<<<<<<<<<<<< createActiveMQComponent");
        return new ActiveMQComponent(activeMQConfiguration);
    }}
