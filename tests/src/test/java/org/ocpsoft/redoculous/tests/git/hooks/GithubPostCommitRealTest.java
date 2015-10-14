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

import java.net.URL;
import java.net.URLDecoder;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.redoculous.tests.HttpAction;
import org.ocpsoft.redoculous.tests.RedoculousTestBase;
import org.ocpsoft.redoculous.tests.WebTest;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
// @Ignore
@RunWith(Arquillian.class)
public class GithubPostCommitRealTest extends RedoculousTestBase
{
   @Deployment(testable = false)
   public static WebArchive getDeployment()
   {
      return RedoculousTestBase.getDeployment();
   }

   @ArquillianResource
   private URL baseUrl;

   @Test
   public void testPostUpdateRealHook() throws Exception
   {
      WebTest test = new WebTest(baseUrl);
      String repositoryURL = "https://github.com/ocpsoft/rewrite.git";
      // String repositoryURL = "file:///Users/lb3/projects/ocpsoft/rewrite";
      HttpAction<HttpPost> post = test.post("/api/v1/manage?repo=" + repositoryURL);
      Assert.assertEquals(201, post.getResponse().getStatusLine().getStatusCode());
      String location = URLDecoder.decode(post.getResponseHeaderValue("location"), "UTF8");
      Assert.assertEquals(test.getBaseURL() + test.getContextPath() +
               "/api/v1/serve?repo=" + repositoryURL, location);

      HttpAction<HttpGet> document = test.get("/api/v1/serve?repo=" + repositoryURL
               + "&ref=master&path=/documentation/src/main/asciidoc/");
      Assert.assertEquals(200, document.getStatusCode());
      Assert.assertTrue(document.getResponseContent().contains("Learn Rewrite"));

      HttpAction<HttpPost> postCommit = test.post("/api/v1/hooks/github?repo=" + repositoryURL);
      Assert.assertEquals(200, postCommit.getStatusCode());

      document = test.get("/api/v1/serve?repo=" + repositoryURL +
               "&ref=master&path=/documentation/src/main/asciidoc/");
      Assert.assertEquals(200, document.getStatusCode());
      Assert.assertTrue(document.getResponseContent().contains("Learn Rewrite"));

   }
}
