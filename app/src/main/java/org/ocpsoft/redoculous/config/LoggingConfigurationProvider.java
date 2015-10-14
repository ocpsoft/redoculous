/*
 * Copyright 2011 <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ocpsoft.redoculous.config;

import javax.servlet.ServletContext;

import org.ocpsoft.logging.Logger;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.servlet.config.DispatchType;
import org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider;
import org.ocpsoft.rewrite.servlet.config.HttpOperation;
import org.ocpsoft.rewrite.servlet.http.event.HttpServletRewrite;

public class LoggingConfigurationProvider extends HttpConfigurationProvider
{
   private Logger log = Logger.getLogger(LoggingConfigurationProvider.class);

   @Override
   public Configuration getConfiguration(ServletContext context)
   {
      return ConfigurationBuilder.begin()
               .addRule()
               .when(DispatchType.isRequest())
               .perform(new HttpOperation()
               {
                  @Override
                  public void performHttp(HttpServletRewrite event, EvaluationContext context)
                  {
                     log.info("A [" + event.getRequest().getRemoteAddr() + "] " + event.getRequest().getMethod()
                              + " " + event.getAddress().toString());
                  }
               });
   }

   @Override
   public int priority()
   {
      return Integer.MIN_VALUE + 1;
   }
}
