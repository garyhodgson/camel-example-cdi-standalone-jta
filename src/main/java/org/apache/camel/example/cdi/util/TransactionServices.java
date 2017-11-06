package org.apache.camel.example.cdi.util;

import com.atomikos.icatch.jta.UserTransactionImp;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionServices implements org.jboss.weld.transaction.spi.TransactionServices {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionServices.class);

    @Override
    public void registerSynchronization(Synchronization s) {
        LOG.info("registerSynchronization");
    }

    @Override
    public boolean isTransactionActive() {
        LOG.info("isTransactionActive");
        return true;
    }

    @Override
    public UserTransaction getUserTransaction() {
         UserTransactionImp userTransactionImp = new UserTransactionImp();
        try {
            userTransactionImp.setTransactionTimeout(10);
        } catch (SystemException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
        return userTransactionImp;
    }

    @Override
    public void cleanup() {
         LOG.info("cleanup");
        
    }

}
