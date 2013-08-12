package org.ocpsoft.redoculous.config;

import java.io.File;

import javax.servlet.ServletContextEvent;

import org.ocpsoft.redoculous.Redoculous;
import org.ocpsoft.redoculous.util.Files;
import org.ocpsoft.rewrite.servlet.spi.ContextListener;

public class TempStorageContextListener implements ContextListener
{
   @Override
   public int priority()
   {
      return 0;
   }

   @Override
   public void contextInitialized(ServletContextEvent event)
   {
      File root = Redoculous.getRoot();
      System.out.println("Redoculous starting with storage directory [" + root.getAbsolutePath() + "]");
   }

   @Override
   public void contextDestroyed(ServletContextEvent event)
   {
      File root = Redoculous.getRoot();
      System.out.println("Redoculous removing storage directory [" + root.getAbsolutePath() + "]");
      Files.delete(root, true);
   }
}
