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
package org.ocpsoft.redoculous.config.util;

import org.ocpsoft.rewrite.servlet.config.response.ResponseContent;
import org.ocpsoft.rewrite.servlet.config.response.ResponseContentInterceptor;
import org.ocpsoft.rewrite.servlet.config.response.ResponseContentInterceptorChain;
import org.ocpsoft.rewrite.servlet.http.event.HttpServletRewrite;

public class LiveReloadScriptAppender implements ResponseContentInterceptor
{

   @Override
   public void intercept(HttpServletRewrite event, ResponseContent content, ResponseContentInterceptorChain chain)
   {
      chain.proceed();
      String buffer = new String(content.getContents(), content.getCharset());

      StringBuilder builder = new StringBuilder();
      builder.append("<html>").append("\n");
      builder.append("  <head>").append("\n");
      builder.append("    <link rel='stylesheet' href='" + event.getContextPath() + "/css/asciidoctor.css'").append("\n");
      builder.append("    <link rel='shortcut icon' href='" +event.getContextPath() + "/favicon.ico' />").append("\n");
      builder.append("    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=US-ASCII\">").append("\n");
      builder.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">").append("\n");
      builder.append("  </head>").append("\n");
      builder.append("  </head>").append("\n");
      builder.append("  <body>").append("\n");
      builder.append("    <div data-redoculous-now>").append("\n");
      builder.append("      ").append(buffer).append("\n");
      builder.append("    </div>").append("\n");
      builder.append("    <script type='text/javascript' src='" + event.getContextPath() + "/js/jquery.min.js'></script>").append("\n");
      builder.append("    <script type='text/javascript' src='" + event.getContextPath() + "/js/redoculous-now.js'></script>").append("\n");
      builder.append("  </body>").append("\n");
      builder.append("</html>").append("\n");

      content.setContents(builder.toString().getBytes());
   }

}
