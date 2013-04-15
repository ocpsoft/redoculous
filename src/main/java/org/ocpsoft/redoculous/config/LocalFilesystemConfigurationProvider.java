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

import java.io.File;

import javax.servlet.ServletContext;

import org.ocpsoft.logging.Logger.Level;
import org.ocpsoft.redoculous.config.util.CanonicalizeFileName;
import org.ocpsoft.redoculous.config.util.RemoveFilePrefixTransposition;
import org.ocpsoft.redoculous.config.util.SafeFileNameTransposition;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Direction;
import org.ocpsoft.rewrite.config.Filesystem;
import org.ocpsoft.rewrite.config.Log;
import org.ocpsoft.rewrite.config.Subset;
import org.ocpsoft.rewrite.param.Transposition;
import org.ocpsoft.rewrite.servlet.config.DispatchType;
import org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider;
import org.ocpsoft.rewrite.servlet.config.Path;
import org.ocpsoft.rewrite.servlet.config.Query;
import org.ocpsoft.rewrite.servlet.config.Response;
import org.ocpsoft.rewrite.servlet.config.SendStatus;
import org.ocpsoft.rewrite.servlet.config.Stream;
import org.ocpsoft.rewrite.transform.Transform;
import org.ocpsoft.rewrite.transform.markup.Asciidoc;

public class LocalFilesystemConfigurationProvider extends HttpConfigurationProvider
{
   Transposition<String> safeFileName = new SafeFileNameTransposition();
   Transposition<String> canonicalizeFilename = new CanonicalizeFileName();

   @Override
   public Configuration getConfiguration(ServletContext context)
   {
      return ConfigurationBuilder
               .begin()

               /*
                * Don't do anything if we don't have required values.
                */
               .addRule()
               .when(Direction.isInbound()
                        .and(DispatchType.isRequest())
                        .and(Path.matches("/serve"))
                        .and(Query.parameterExists("path"))
               )
               .perform(Log
                        .message(Level.INFO, "File requested [{path}]")
                        .and(Subset.evaluate(ConfigurationBuilder
                                 .begin()

                                 /*
                                  * Serve, render, and cache the doc, or serve directly from file.
                                  */
                                 .addRule()
                                 .when(Filesystem.fileExists(new File("/{path}.asciidoc")))
                                 .perform(Log
                                          .message(Level.INFO, "Serving file [{path}]")
                                          .and(Response.setContentType("text/html")
                                                   .and(Response.addHeader("Charset", "UTF-8")))

                                          .and(Response.addHeader("Access-Control-Allow-Origin", "*"))
                                          .and(Response.addHeader("Access-Control-Allow-Credentials", "true"))
                                          .and(Response.addHeader("Access-Control-Allow-Methods", "GET, POST"))
                                          .and(Response
                                                   .addHeader("Access-Control-Allow-Headers",
                                                            "Content-Type, User-Agent, X-Requested-With, X-Requested-By, Cache-Control"))

                                          .and(Response.setStatus(200))
                                          .and(Subset
                                                   .evaluate(
                                                            ConfigurationBuilder
                                                                     .begin()

                                                                     .addRule()
                                                                     .when(Query.parameterExists("preview"))
                                                                     .perform(Response
                                                                              .withOutputInterceptedBy(new PreviewLinkInterceptor()))
                                                   )
                                                   .and(Transform.with(Asciidoc.partialDocument())
                                                            .and(Stream.from(new File(
                                                                     "/{path}.asciidoc"))))
                                          )
                                          .and(Response.complete()))

                                 /*
                                  * Serve from directory index file.
                                  */
                                 .addRule()
                                 .when(Filesystem.fileExists(new File("/{path}/index.asciidoc")))
                                 .perform(Log
                                          .message(Level.INFO, "Serving index file [{path}/index.asciidoc]")
                                          .and(Response
                                                   .setContentType("text/html")
                                                   .and(Response.addHeader("Charset", "UTF-8")))

                                          .and(Response.addHeader("Access-Control-Allow-Origin", "*"))
                                          .and(Response.addHeader("Access-Control-Allow-Credentials", "true"))
                                          .and(Response.addHeader("Access-Control-Allow-Methods", "GET, POST"))
                                          .and(Response
                                                   .addHeader("Access-Control-Allow-Headers",
                                                            "Content-Type, User-Agent, X-Requested-With, X-Requested-By, Cache-Control"))

                                          .and(Response.setStatus(200))
                                          .and(Subset
                                                   .evaluate(
                                                            ConfigurationBuilder
                                                                     .begin()

                                                                     .addRule()
                                                                     .when(Query.parameterExists("preview"))
                                                                     .perform(Response
                                                                              .withOutputInterceptedBy(new PreviewLinkInterceptor()))
                                                   )
                                                   .and(Transform
                                                            .with(Asciidoc.partialDocument())
                                                            .and(Stream.from(new File(
                                                                     "/{path}/index.asciidoc"))))
                                          )
                                          .and(Response.complete()))

                                 .addRule()
                                 .perform(SendStatus.error(404))

                                 )))
               .where("path").matches("file:///.*")
               .transposedBy(new RemoveFilePrefixTransposition())
               .transposedBy(canonicalizeFilename);

   }

   @Override
   public int priority()
   {
      return 0;
   }
}
