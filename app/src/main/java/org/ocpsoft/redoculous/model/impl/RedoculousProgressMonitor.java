/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.model.impl;

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.eclipse.jgit.lib.BatchingProgressMonitor;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.ocpsoft.logging.Logger;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class RedoculousProgressMonitor extends TextProgressMonitor
{
   private static final Logger log = Logger.getLogger(RedoculousProgressMonitor.class.getName());

   public static void shutdown()
   {
      try {
         Field field = BatchingProgressMonitor.class.getDeclaredField("alarmQueue");
         field.setAccessible(true);
         ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) field.get(null);
         executor.shutdownNow();
      }
      catch (Exception e) {
         log.error("Could not shut down JGit alarmQueue:", e);
      }
   }
}
