package org.ocpsoft.redoculous.tests;

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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public final class WebTest
{
   private URL baseUrl;

   public WebTest(URL baseUrl)
   {
      super();
      this.baseUrl = baseUrl;
   }

   public String getBaseURL()
   {
      return baseUrl.getProtocol() + "://" + baseUrl.getHost()
               + (baseUrl.getPort() == -1 ? "" : ":" + baseUrl.getPort());
   }

   public String getContextPath()
   {
      String contextPath = baseUrl.getPath();
      if (!"/".equals(contextPath))
         contextPath = contextPath.replaceAll("^(.*)/$", "$1").replaceAll("ROOT$", "");
      return contextPath;
   }

   /**
    * Request a resource from the deployed test-application. The {@link HttpServletRequest#getContextPath()} will be
    * automatically prepended to the given path.
    * <p>
    * E.g: A path of '/example' will be sent as '/rewrite-test/example'
    * 
    * @throws Exception
    */
   public HttpAction<HttpPost> post(final String path) throws Exception
   {
      DefaultHttpClient client = new DefaultHttpClient();
      return post(client, path, new HashMap<String, String>());
   }

   /**
    * Request a resource from the deployed test-application. The {@link HttpServletRequest#getContextPath()} will be
    * automatically prepended to the given path.
    * <p>
    * E.g: A path of '/example' will be sent as '/rewrite-test/example'
    * 
    * @throws Exception
    */
   public HttpAction<HttpPost> post(HttpClient client, String path, Map<String, String> parameters) throws Exception
   {
      return post(client, path, parameters, new Header[0]);
   }

   /**
    * Request a resource from the deployed test-application. The context path will be automatically prepended to the
    * given path.
    * <p>
    * E.g: A path of '/example' will be sent as '/rewrite-test/example'
    * 
    * @throws Exception
    */
   public HttpAction<HttpPost> post(HttpClient client, String path, Map<String, String> parameters,
            Header... headers)
            throws Exception
   {
      HttpPost request = new HttpPost(getBaseURL() + getContextPath() + path);
      if (headers != null && headers.length > 0)
      {
         request.setHeaders(headers);
      }
      HttpContext context = new BasicHttpContext();
      HttpResponse response = client.execute(request, context);

      return new HttpAction<HttpPost>(client, context, request, response, getBaseURL(), getContextPath());
   }

   /**
    * Request a resource from the deployed test-application. The {@link HttpServletRequest#getContextPath()} will be
    * automatically prepended to the given path.
    * <p>
    * E.g: A path of '/example' will be sent as '/rewrite-test/example'
    * 
    * @throws Exception
    */
   public HttpAction<HttpGet> get(final String path) throws Exception
   {
      DefaultHttpClient client = new DefaultHttpClient();
      return get(client, path);
   }

   /**
    * Request a resource from the deployed test-application. The {@link HttpServletRequest#getContextPath()} will be
    * automatically prepended to the given path.
    * <p>
    * E.g: A path of '/example' will be sent as '/rewrite-test/example'
    * 
    * @throws Exception
    */
   public HttpAction<HttpGet> get(HttpClient client, String path) throws Exception
   {
      return get(client, path, new Header[0]);
   }

   /**
    * Request a resource from the deployed test-application. The {@link HttpServletRequest#getContextPath()} will be
    * automatically prepended to the given path.
    * <p>
    * E.g: A path of '/example' will be sent as '/rewrite-test/example'
    * 
    * @throws Exception
    */
   public HttpAction<HttpGet> get(HttpClient client, String path, Header... headers) throws Exception
   {
      String targetURL = path;
      if (!targetURL.startsWith("\\w+://"))
      {
         targetURL = getBaseURL() + getContextPath() + path;
      }
      HttpGet request = new HttpGet(targetURL);
      if (headers != null && headers.length > 0)
      {
         request.setHeaders(headers);
      }
      HttpContext context = new BasicHttpContext();
      HttpResponse response = client.execute(request, context);

      return new HttpAction<HttpGet>(client, context, request, response, getBaseURL(), getContextPath());
   }

   /**
    * Request a resource from the deployed test-application. The {@link HttpServletRequest#getContextPath()} will be
    * automatically prepended to the given path.
    * <p>
    * E.g: A path of '/example' will be sent as '/rewrite-test/example'
    */
   public HttpAction<HttpHead> head(final String path)
   {
      DefaultHttpClient client = new DefaultHttpClient();
      try
      {
         HttpHead request = new HttpHead(getBaseURL() + getContextPath() + path);
         HttpContext context = new BasicHttpContext();
         HttpResponse response = client.execute(request, context);

         return new HttpAction<HttpHead>(client, context, request, response, getBaseURL(), getContextPath());
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public HtmlAction getWebClient(String path) throws FailingHttpStatusCodeException, IOException
   {
      try
      {
         WebClient client = new WebClient();
         return new HtmlAction(client, (HtmlPage) client.getPage(getBaseURL() + getContextPath() + path));
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Verifies that the given text contains the given string.
    */
   public static void assertContains(String text, String s)
   {
      if (text == null || s == null || !text.contains(s))
      {
         throw new RuntimeException("Could not find [" + s + "] in text: " + text);
      }
   }
}
