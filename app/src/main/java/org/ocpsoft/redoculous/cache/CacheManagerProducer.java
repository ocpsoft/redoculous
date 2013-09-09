package org.ocpsoft.redoculous.cache;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.enterprise.inject.Produces;

import org.infinispan.manager.EmbeddedCacheManager;

@Singleton
public class CacheManagerProducer
{
   @Produces
   @Resource(lookup = "java:jboss/infinispan/cluster")
   private static EmbeddedCacheManager container;
}
