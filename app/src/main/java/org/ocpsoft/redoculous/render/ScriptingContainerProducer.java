package org.ocpsoft.redoculous.render;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Startup;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jruby.CompatVersion;
import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;
import org.ocpsoft.logging.Logger;

@Startup
@Stateless
public class ScriptingContainerProducer
{
   private static final Logger log = Logger.getLogger(ScriptingContainerProducer.class);
   public static ScriptingContainer container;

   @PostConstruct
   public void init()
   {
      log.debug("Creating new ScriptingContainer.");
      container = new ScriptingContainer(LocalContextScope.CONCURRENT, LocalVariableBehavior.TRANSIENT);
   }

   @Produces
   @ApplicationScoped
   public ScriptingContainer getContainer()
   {
      container.setRunRubyInProcess(false);
      container.setCompileMode(CompileMode.JIT);
      container.setCompatVersion(CompatVersion.RUBY2_0);
      container.setClassLoader(ScriptingContainerProducer.class.getClassLoader());

      return container;
   }

   @PreDestroy
   public void destroy()
   {
      try {
         if (container != null)
            container.terminate();
      }
      catch (Exception e) {
         log.error("Could not terminate ScriptingContainer", e);
      }
   }
}
