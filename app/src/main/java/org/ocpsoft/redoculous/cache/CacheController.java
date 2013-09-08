package org.ocpsoft.redoculous.cache;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.TransactionManager;

import org.infinispan.AdvancedCache;
import org.infinispan.configuration.cache.CacheMode;

@Named
@SessionScoped
public class CacheController implements Serializable
{
   private static final long serialVersionUID = -992421203824556032L;

   @Inject
   transient AdvancedCache<Object, Object> cache;

   @SuppressWarnings("unchecked")
   public <T> T query(Object key)
   {
      return (T) cache.get(key);
   }

   public void add(Object key, Object value)
   {
      cache.putIfAbsentAsync(key, value);
   }

   public List<?> locate(Object key)
   {
      if (cache.getCacheConfiguration().clustering().cacheMode() != CacheMode.LOCAL)
      {
         return cache.getDistributionManager().locate(key);
      }
      else
      {
         return Collections.singletonList("local");
      }
   }

   public String getSelf()
   {
      if (cache.getCacheConfiguration().clustering().cacheMode() != CacheMode.LOCAL)
         return cache.getCacheManager().getAddress().toString();
      else
         return "local cache";
   }

   public TransactionManager getTransactionManager()
   {
      return cache.getTransactionManager();
   }

}
