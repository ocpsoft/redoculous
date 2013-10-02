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

import org.ocpsoft.rewrite.config.And;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Direction;
import org.ocpsoft.rewrite.config.Subset;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;
import org.ocpsoft.rewrite.param.Transposition;
import org.ocpsoft.rewrite.servlet.config.DispatchType;
import org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider;
import org.ocpsoft.rewrite.servlet.config.Path;
import org.ocpsoft.rewrite.servlet.config.Query;
import org.ocpsoft.rewrite.servlet.config.Response;
import org.ocpsoft.rewrite.servlet.config.URL;

public class PreviewModeConfigurationProvider extends HttpConfigurationProvider
{
   @Override
   public Configuration getConfiguration(ServletContext context)
   {
      return ConfigurationBuilder
               .begin()

               /*
                * Live preview mode.
                */
               .addRule()
               .when(Direction.isInbound()
                        .and(DispatchType.isRequest())
                        .and(Path.matches("/api/{version}/preview"))
                        .and(Query.parameterExists("path"))
                        .and(URL.captureIn("url"))
               )
               .perform(Response
                        .setContentType("text/html")
                        .and(Response.addHeader("Charset", "UTF-8"))
                        .and(Subset.evaluate(ConfigurationBuilder
                                 .begin()
                                 .addRule()
                                 .when(And.all(Query.parameterExists("repo"),
                                          Query.parameterExists("ref")))
                                 .perform(Response.withOutputInterceptedBy(new PreviewLinkInterceptor()))
                                 .otherwise(Response.withOutputInterceptedBy(new PreviewLocalLinkInterceptor())
                                 ))
                        )
                        .and(Proxy.to("{url}&nogzip"))
                        .and(Response.complete()))
               .where("url")
               .transposedBy(new Transposition<String>()
               {
                  @Override
                  public String transpose(Rewrite event, EvaluationContext context, String value)
                  {
                     return value.replaceFirst("/preview", "/serve");
                  }
               });
   }

   @Override
   public int priority()
   {
      /*
       * Very high priority.
       */
      return -10000;
   }
}
