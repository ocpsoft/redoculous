package org.ocpsoft.redoculous;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.ocpsoft.rewrite.exception.RewriteException;
import org.ocpsoft.rewrite.servlet.RewriteFilter;
import org.ocpsoft.rewrite.servlet.impl.RewriteServletContextListener;
import org.ocpsoft.rewrite.servlet.impl.RewriteServletRequestListener;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class Redoculous
{
   private static File root = null;

   public static File getRoot()
   {
      if (root == null)
      {
         try
         {
            root = File.createTempFile("redoculous", "");
            root.delete();
            root.mkdirs();
         }
         catch (IOException e)
         {
            throw new RewriteException("Could not create temp folder for doc files or cache.", e);
         }
      }
      return root;
   }

   public static void main(String[] args) throws Exception
   {
      Server server = new Server(8080);

      WebAppContext webapp = new WebAppContext();
      webapp.addEventListener(new RewriteServletContextListener());
      webapp.addEventListener(new RewriteServletRequestListener());
      webapp.setServer(server);
      webapp.setContextPath("/");
      webapp.setClassLoader(Redoculous.class.getClassLoader());
      webapp.addFilter(RewriteFilter.class, "/*",
               EnumSet.of(DispatcherType.REQUEST,
                        DispatcherType.FORWARD,
                        DispatcherType.ERROR));

      webapp.start();
      server.start();
      server.join();
   }
}
