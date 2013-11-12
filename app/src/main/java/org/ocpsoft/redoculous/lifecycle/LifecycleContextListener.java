package org.ocpsoft.redoculous.lifecycle;

import java.io.File;

import javax.servlet.ServletContextEvent;

import org.ocpsoft.logging.Logger;
import org.ocpsoft.redoculous.Redoculous;
import org.ocpsoft.redoculous.model.impl.RedoculousProgressMonitor;
import org.ocpsoft.redoculous.render.ScriptingContainerProducer;
import org.ocpsoft.redoculous.util.Files;
import org.ocpsoft.rewrite.servlet.spi.ContextListener;

public class LifecycleContextListener implements ContextListener
{
   private static final Logger log = Logger.getLogger(LifecycleContextListener.class);

   @Override
   public void contextInitialized(ServletContextEvent event)
   {
      File root = Redoculous.getRoot();
      log.info("Redoculous starting with storage directory [" + root.getAbsolutePath() + "]");
   }

   @Override
   public void contextDestroyed(ServletContextEvent event)
   {
      // TODO replace this with command pattern so that components can register shutdown hooks.

      log.info("Redoculous shutting down JGit");
      RedoculousProgressMonitor.shutdown();

      File root = Redoculous.getRoot();
      log.info("Redoculous removing storage directory [" + root.getAbsolutePath() + "]");
      Files.delete(root, true);

      log.info("Redoculous shutting down JRuby");
      if (ScriptingContainerProducer.container != null)
      {
         ScriptingContainerProducer.container.terminate();
         ScriptingContainerProducer.container = null;
      }
   }

   @Override
   public int priority()
   {
      return 0;
   }

}
