package org.ocpsoft.redoculous.tests;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class HtmlAction
{
   private final WebClient client;
   private final HtmlPage page;

   public HtmlAction(WebClient client, HtmlPage page)
   {
      this.client = client;
      this.page = page;
   }

   public WebClient getClient()
   {
      return client;
   }

   public HtmlPage getPage()
   {
      return page;
   }
}