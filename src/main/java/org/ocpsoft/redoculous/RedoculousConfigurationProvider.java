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
package org.ocpsoft.redoculous;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;

import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Direction;
import org.ocpsoft.rewrite.config.Filesystem;
import org.ocpsoft.rewrite.config.Not;
import org.ocpsoft.rewrite.config.Subset;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;
import org.ocpsoft.rewrite.exception.RewriteException;
import org.ocpsoft.rewrite.param.Transposition;
import org.ocpsoft.rewrite.servlet.config.DispatchType;
import org.ocpsoft.rewrite.servlet.config.Header;
import org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider;
import org.ocpsoft.rewrite.servlet.config.Method;
import org.ocpsoft.rewrite.servlet.config.Path;
import org.ocpsoft.rewrite.servlet.config.Query;
import org.ocpsoft.rewrite.servlet.config.Response;
import org.ocpsoft.rewrite.servlet.config.Stream;
import org.ocpsoft.rewrite.servlet.config.rule.Join;
import org.ocpsoft.rewrite.transform.Transform;
import org.ocpsoft.rewrite.transform.markup.Asciidoc;

public class RedoculousConfigurationProvider extends HttpConfigurationProvider
{
   Transposition<String> safeFileName = new SafeFileNameTransposition();

   @Override
   public Configuration getConfiguration(ServletContext context)
   {
      final File root;
      try {
         root = File.createTempFile("redoculous", "");
         root.delete();
         root.mkdirs();
      }
      catch (IOException e) {
         throw new RewriteException("Could not create temp folder for doc files or cache.", e);
      }

      System.out.println("Redoculous starting with storage directory [" + root.getAbsolutePath() + "]");

      return ConfigurationBuilder
               .begin()

               .addRule(Join.path("/docs").to("/demo.html"))
               .addRule(Join.path("/docs/{*}").to("/demo.html"))

               /*
                * Clear the cache and or re-clone when github says so:
                */
               .addRule()
               .when(Direction.isInbound()
                        .and(DispatchType.isRequest())
                        .and(Method.isPost())
                        .and(Path.matches("/update")))
               .perform(new UpdateRepositoryOperation(root)
                        .and(Response.setStatus(200))
                        .and(Response.complete()))

               /*
                * Set up compression.
                */
               .addRule()
               .when(Header.matches("{Accept-Encoding}", "{gzip}"))
               .perform(Response.gzipStreamCompression())
               .where("Accept-Encoding")
               .matches("(?i)Accept-Encoding")
               .where("gzip")
               .matches("(?i).*\\bgzip\\b.*")

               /*
                * Don't do anything if we don't have required values.
                */
               .addRule()
               .when(Direction.isInbound()
                        .and(DispatchType.isRequest())
                        .and(Query.parameterExists("repo"))
                        .and(Query.parameterExists("ref"))
                        .and(Query.parameterExists("path"))
               )
               .perform(Subset.evaluate(ConfigurationBuilder
                        .begin()

                        /*
                         * Clone the repository and set up the cache dir.
                         */
                        .addRule()
                        .perform(new CloneRepositoryOperation(root, "repo", "ref"))

                        /*
                         * Check out the ref if it does not exist.
                         */
                        .addRule()
                        .when(Not.any(Filesystem.directoryExists(new File(root, "{repo}/refs/{ref}"))))
                        .perform(new CheckoutRefOperation(root, "repo", "ref"))

                        /*
                         * Serve, render, and cache the doc, or serve directly from cache.
                         */
                        .addRule()
                        .when(Filesystem.fileExists(new File(root, "{repo}/refs/{ref}/{path}.asciidoc")))
                        .perform(Response
                                 .setContentType("text/html")
                                 .and(Response.addHeader("Charset", "UTF-8"))
                                 .and(Response.addHeader("Access-Control-Allow-Origin", "*"))
                                 .and(Response.addHeader("Access-Control-Allow-Credentials", "true"))
                                 .and(Response.addHeader("Access-Control-Allow-Methods", "GET, POST"))
                                 .and(Response.addHeader("Access-Control-Allow-Headers",
                                          "Content-Type, User-Agent, X-Requested-With, X-Requested-By, Cache-Control"))
                                 .and(Response.setStatus(200))
                                 .and(Subset.evaluate(ConfigurationBuilder
                                          .begin()
                                          .addRule()
                                          .when(Filesystem
                                                   .fileExists(new File(root, "{repo}/caches/{ref}/{path}.html")))
                                          .perform(Stream.from(new File(root, "{repo}/caches/{ref}/{path}.html")))
                                          .otherwise(
                                                   Transform.with(Asciidoc.partialDocument())
                                                            .and(Stream.to(new File(root,
                                                                     "{repo}/caches/{ref}/{path}.html")))
                                                            .and(Stream.from(new File(root,
                                                                     "{repo}/refs/{ref}/{path}.asciidoc")))
                                          )))
                                 .and(Response.complete()))

                        ))
               .where("path").matches(".*").transposedBy(new Transposition<String>() {

                  @Override
                  public String transpose(Rewrite event, EvaluationContext context, String value)
                  {
                     return value.replaceAll("(.*)\\.asciidoc$", "$1");
                  }
               })
               .where("repo").transposedBy(safeFileName);

   }

   @Override
   public int priority()
   {
      return 0;
   }
}
