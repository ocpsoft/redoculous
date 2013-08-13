package org.ocpsoft.redoculous.render;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jruby.CompatVersion;
import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;

public class ScriptingContainerProducer
{
   @Produces
   @ApplicationScoped
   public ScriptingContainer getContainer()
   {
      ScriptingContainer result = new ScriptingContainer(LocalContextScope.CONCURRENT, LocalVariableBehavior.TRANSIENT);
      result.setRunRubyInProcess(false);
      result.setCompileMode(CompileMode.JIT);
      result.setCompatVersion(CompatVersion.RUBY2_0);

      return result;
   }
}
