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
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Direction;
import org.ocpsoft.rewrite.config.Filesystem;
import org.ocpsoft.rewrite.config.Subset;
import org.ocpsoft.rewrite.exception.RewriteException;
import org.ocpsoft.rewrite.servlet.config.DispatchType;
import org.ocpsoft.rewrite.servlet.config.Header;
import org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider;
import org.ocpsoft.rewrite.servlet.config.Path;
import org.ocpsoft.rewrite.servlet.config.Response;
import org.ocpsoft.rewrite.servlet.config.Stream;
import org.ocpsoft.rewrite.transform.Transform;
import org.ocpsoft.rewrite.transform.markup.Asciidoc;

public class RedoculousConfigurationProvider extends HttpConfigurationProvider
{
   @Override
   public Configuration getConfiguration(ServletContext context)
   {
      final File root;
      final File cache;
      try {
         root = File.createTempFile("redoculous", "x");
         root.delete();
         root.mkdirs();

         try {
            GitUtils.clone(root, "https://github.com/lincolnthree/cdi.git");
         }
         catch (GitAPIException e) {
            throw new RewriteException("Could not clone git repository.", e);
         }

         cache = new File(root, "redoculous-cache");
         cache.delete();
         cache.mkdirs();
      }
      catch (IOException e) {
         throw new RewriteException("Could not create temp folder for doc files or cache.", e);
      }

      return ConfigurationBuilder.begin()

               .addRule()
               .when(Header.matches("{Accept-Encoding}", "{gzip}"))
               .perform(Response.gzipStreamCompression())
               .where("Accept-Encoding").matches("(?i)Accept-Encoding")
               .where("gzip").matches("(?i).*\\bgzip\\b.*")

               /**
                * Figure out if we want a doc.
                */
               .addRule()
               .when(Direction.isInbound().and(DispatchType.isRequest())
                        .and(Path.matches("/{page}").withRequestBinding())
                        .and(Filesystem.fileExists(new File(root, "{page}.asciidoc"))))
               .perform(Response.setContentType("text/html")
                        .and(Response.addHeader("Charset", "UTF-8"))
                        .and(Response.setStatus(200))
                        .and(Subset.evaluate(ConfigurationBuilder.begin()
                                 .addRule()
                                 .when(Filesystem.fileExists(new File(cache, "{page}.html")))
                                 .perform(Stream.from(new File(cache, "{page}.html")))
                                 .otherwise(Transform.with(Asciidoc.fullDocument()
                                          .withTitle("Redoculous")
                                          .addStylesheet(context.getContextPath() + "/common/bootstrap.css")
                                          .addStylesheet(context.getContextPath() + "/common/common.css"))
                                          .and(Stream.to(new File(cache, "{page}.html")))
                                          .and(Stream.from(new File(root, "{page}.asciidoc")))
                                 )))
                        .and(Response.complete()))
               .where("page").matches(".*");

   }

   @Override
   public int priority()
   {
      return 0;
   }
}
