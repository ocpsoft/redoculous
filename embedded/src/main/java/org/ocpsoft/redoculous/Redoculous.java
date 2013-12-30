package org.ocpsoft.redoculous;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.ocpsoft.redoculous.config.util.NullServlet;
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
      Server server = new Server(Integer.getInteger("port", 8080));

      HandlerList handlers = new HandlerList();
      handlers.addHandler(getServletContextHandler());
      handlers.addHandler(getResourceHandler());

      server.setHandler(handlers);
      server.start();
      server.join();
   }

   private static ResourceHandler getResourceHandler()
   {
      ResourceHandler handler = new ResourceHandler();
      handler.setDirectoriesListed(true);
      handler.setBaseResource(Resource.newClassPathResource("webapp"));
      handler.setWelcomeFiles(new String[] { "index.html" });
      return handler;
   }

   private static ServletContextHandler getServletContextHandler()
   {
      ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
      context.setClassLoader(Redoculous.class.getClassLoader());
      context.setContextPath("/p");
      context.addFilter(RewriteFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD,
               DispatcherType.INCLUDE, DispatcherType.ASYNC, DispatcherType.ERROR));
      context.addEventListener(new RewriteServletContextListener());
      context.addEventListener(new RewriteServletRequestListener());
      context.addServlet(NullServlet.class, "/*");
      context.setResourceBase("webapp");
      return context;
   }
}
