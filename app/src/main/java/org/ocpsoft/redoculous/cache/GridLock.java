/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.cache;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.infinispan.AdvancedCache;
import org.ocpsoft.logging.Logger;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class GridLock
{
   private static final Logger log = Logger.getLogger(GridLock.class);

   private AdvancedCache<String, Object> cache;

   public GridLock(AdvancedCache<String, Object> cache)
   {
      this.cache = cache;
   }

   /**
    * Get a {@link Lock} instance for the given keys. All keys will be locked and unlocked with the same operation.
    */
   public Lock getLock(final UserTransaction transaction, final String... keys)
   {
      return new Lock()
      {
         @Override
         public void unlock()
         {
            try
            {
               transaction.commit();
               log.info("Lock: " + Arrays.asList(keys) + " Released");
            }
            catch (Exception e)
            {
               try
               {
                  transaction.rollback();
                  log.info("Lock: " + Arrays.asList(keys) + " - Released.");
               }
               catch (Exception e1)
               {
                  e1.printStackTrace();
                  throw new RuntimeException(e);
               }
               throw new RuntimeException(e);
            }
         }

         @Override
         public boolean tryLock(long time, TimeUnit unit) throws InterruptedException
         {
            boolean result = false;

            long start = System.currentTimeMillis();
            while (!(result = cache.lock(keys)))
            {
               if (System.currentTimeMillis() > (start + TimeUnit.MILLISECONDS.convert(time, unit)))
               {
                  break;
               }

               try
               {
                  Thread.sleep(10);
               }
               catch (InterruptedException e)
               {
                  throw new RuntimeException("Interrupted waiting for Lock on [" + keys + "].", e);
               }
            }

            if (result)
               log.info("Lock: " + Arrays.asList(keys) + " - Obtained.");

            return result;
         }

         @Override
         public boolean tryLock()
         {
            boolean result = cache.lock(keys);
            if (result)
               log.info("Lock: " + Arrays.asList(keys) + " - Obtained.");
            return result;
         }

         @Override
         public Condition newCondition()
         {
            throw new UnsupportedOperationException();
         }

         @Override
         public void lockInterruptibly() throws InterruptedException
         {
            throw new UnsupportedOperationException();
         }

         @Override
         public void lock()
         {
            try
            {
               if (Status.STATUS_NO_TRANSACTION == transaction.getStatus())
                  transaction.begin();
            }
            catch (Exception e)
            {
               throw new RuntimeException(e);
            }

            while (!cache.lock(keys))
            {
               try
               {
                  Thread.sleep(10);
               }
               catch (InterruptedException e)
               {
                  throw new RuntimeException("Interrupted waiting for Lock on [" + keys + "].", e);
               }
            }

            log.info("Lock: " + Arrays.asList(keys) + " - Obtained.");
         }
      };
   }

}
