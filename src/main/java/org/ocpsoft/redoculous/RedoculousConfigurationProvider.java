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

import org.eclipse.jgit.api.errors.GitAPIException;
import org.ocpsoft.rewrite.bind.Evaluation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Direction;
import org.ocpsoft.rewrite.config.Filesystem;
import org.ocpsoft.rewrite.config.Not;
import org.ocpsoft.rewrite.config.Subset;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;
import org.ocpsoft.rewrite.exception.RewriteException;
import org.ocpsoft.rewrite.servlet.config.DispatchType;
import org.ocpsoft.rewrite.servlet.config.Header;
import org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider;
import org.ocpsoft.rewrite.servlet.config.HttpOperation;
import org.ocpsoft.rewrite.servlet.config.Lifecycle;
import org.ocpsoft.rewrite.servlet.config.Query;
import org.ocpsoft.rewrite.servlet.config.Response;
import org.ocpsoft.rewrite.servlet.config.Stream;
import org.ocpsoft.rewrite.servlet.http.event.HttpServletRewrite;
import org.ocpsoft.rewrite.transform.Transform;
import org.ocpsoft.rewrite.transform.markup.Asciidoc;

public class RedoculousConfigurationProvider extends HttpConfigurationProvider
{
   org.ocpsoft.rewrite.param.Transform<String> safeFileName = new org.ocpsoft.rewrite.param.Transform<String>() {

      @Override
      public String transform(Rewrite event, EvaluationContext context, String value)
      {
         return value.replaceAll("[/?<>\\\\:*|\"]", "");
      }
   };

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

      return ConfigurationBuilder.begin()

               /*
                * Don't do anything if we don't have required values.
                */
               .addRule()
               .when(Direction.isInbound()
                        .and(DispatchType.isRequest())
                        .and(Not.any(Query.parameterExists("repo"))
                                 .or(Not.any(Query.parameterExists("path")))
                        )
               )
               .perform(Lifecycle.handled())

               /*
                * Set up compression.
                */
               .addRule()
               .when(Header.matches("{Accept-Encoding}", "{gzip}"))
               .perform(Response.gzipStreamCompression())
               .where("Accept-Encoding").matches("(?i)Accept-Encoding")
               .where("gzip").matches("(?i).*\\bgzip\\b.*")

               /*
                * Clone the repository and set up the cache dir.
                */
               .addRule()
               .when(Direction.isInbound()
                        .and(DispatchType.isRequest())
                        .and(Query.parameterExists("repo")))
               .perform(new HttpOperation() {
                  @Override
                  public void performHttp(HttpServletRewrite event, EvaluationContext context)
                  {
                     String repo = (String) Evaluation.property("repo").retrieve(event, context);
                     File repoDir = new File(root, safeFileName.transform(event, context, repo));
                     File cacheDir = new File(root, safeFileName.transform(event, context, repo) + "-cache");
                     if (!repoDir.exists())
                     {
                        try {
                           repoDir.mkdirs();
                           cacheDir.mkdirs();
                           GitUtils.clone(repoDir, repo);
                        }
                        catch (GitAPIException e) {
                           throw new RewriteException("Could not clone git repository.", e);
                        }
                     }
                  }
               })

               /*
                * Serve, render, and cache the doc, or serve directly from cache.
                */
               .addRule()
               .when(Direction.isInbound().and(DispatchType.isRequest())
                        .and(Query.parameterExists("repo"))
                        .and(Query.parameterExists("path"))
                        .and(Filesystem.fileExists(new File(root, "{repo}/{path}.asciidoc"))))
               .perform(Response.setContentType("text/html")
                        .and(Response.addHeader("Charset", "UTF-8"))
                        .and(Response.setStatus(200))
                        .and(Subset.evaluate(ConfigurationBuilder.begin()
                                 .addRule()
                                 .when(Filesystem.fileExists(new File(root, "{repo}-cache/{path}.html")))
                                 .perform(Stream.from(new File(root, "{repo}-cache/{path}.html")))
                                 .otherwise(Transform.with(Asciidoc.fullDocument()
                                          .withTitle("Redoculous")
                                          .addStylesheet(context.getContextPath() + "/common/bootstrap.css")
                                          .addStylesheet(context.getContextPath() + "/common/common.css"))
                                          .and(Stream.to(new File(root, "{repo}-cache/{path}.html")))
                                          .and(Stream.from(new File(root, "{repo}/{path}.asciidoc")))
                                 )))
                        .and(Response.complete()))
               .where("path").matches(".*")
               .where("repo").transformedBy(safeFileName);

   }

   @Override
   public int priority()
   {
      return 0;
   }
}
