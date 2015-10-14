package org.ocpsoft.redoculous.tests.git.hooks;

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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.redoculous.tests.HttpAction;
import org.ocpsoft.redoculous.tests.RedoculousTestBase;
import org.ocpsoft.redoculous.tests.WebTest;
import org.ocpsoft.redoculous.util.Files;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RunWith(Arquillian.class)
public class GithubPostCommitTest extends RedoculousTestBase
{
   @Deployment(testable = false)
   public static WebArchive getDeployment()
   {
      return RedoculousTestBase.getDeployment();
   }

   @ArquillianResource
   private URL baseUrl;
   private File repository;

   private Git repo;
   private File document;

   @Before
   public void before() throws IOException, GitAPIException
   {
      repository = File.createTempFile("redoc", "ulous-test");
      repository.delete();
      repository.mkdirs();
      document = new File(repository, "document.asciidoc");
      document.createNewFile();

      Files.write(document, getClass().getClassLoader().getResourceAsStream("asciidoc/toc.asciidoc"));

      repo = Git.init().setDirectory(repository).call();
      repo.add().addFilepattern("document.asciidoc").call();
      repo.commit().setMessage("Initial commit.").call();
   }

   @After
   public void after()
   {
      Files.delete(repository, true);
   }

   @Test
   public void testPostUpdateHook() throws Exception
   {
      WebTest test = new WebTest(baseUrl);
      String repositoryURL = "file://" + repository.getAbsolutePath();
      HttpAction<HttpPost> post = test.post("/api/v1/manage?repo=" + repositoryURL);
      Assert.assertEquals(201, post.getResponse().getStatusLine().getStatusCode());
      String location = URLDecoder.decode(post.getResponseHeaderValue("location"), "UTF8");
      Assert.assertEquals(test.getBaseURL() + test.getContextPath() + "/api/v1/serve?repo=" + repositoryURL,
               location);

      HttpAction<HttpGet> document = test.get("/api/v1/serve?repo=" + repositoryURL + "&ref=master&path=document");
      Assert.assertTrue(document.getResponseContent().contains("The following sections will describe both ways in detail"));

      Files.write(this.document, getClass().getClassLoader().getResourceAsStream("asciidoc/notoc.asciidoc"));
      repo.add().addFilepattern("document.asciidoc").call();
      repo.commit().setMessage("No table of contents.").call();

      HttpAction<HttpPost> postCommit = test.post("/api/v1/hooks/github?repo=" + repositoryURL);
      Assert.assertEquals(200, postCommit.getStatusCode());

      document = test.get("/api/v1/serve?repo=" + repositoryURL + "&ref=master&path=document");
      Assert.assertTrue(document.getResponseContent().contains("This is a simple document."));
   }
}
