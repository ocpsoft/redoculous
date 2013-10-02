package org.ocpsoft.redoculous.config;

import java.util.Calendar;

import org.ocpsoft.rewrite.servlet.config.response.ResponseContent;
import org.ocpsoft.rewrite.servlet.config.response.ResponseContentInterceptor;
import org.ocpsoft.rewrite.servlet.config.response.ResponseContentInterceptorChain;
import org.ocpsoft.rewrite.servlet.http.event.HttpServletRewrite;

public class WatermarkInterceptor implements ResponseContentInterceptor
{
   @Override
   public void intercept(HttpServletRewrite event, ResponseContent buffer,
            ResponseContentInterceptorChain chain)
   {
      String content = new String(buffer.getContents(), buffer.getCharset());
      content = content + "<center style='margin: 25px !important; " +
               "font-size: 12px !important; " +
               "display: block !important; " +
               "visibility: visible !important;'>" +
               "Rendered with " +
               "<a style='opacity: 0.8;' href='http://redoculous.io'>redoculous</a> - by " +
               "<a style='opacity: 0.8;' href='http://ocpsoft.org/'>ocpsoft.org</a> &copy " + getYear() + "</center>";
      buffer.setContents(content.getBytes());
      chain.proceed();
   }

   private String getYear()
   {
      return "" + Calendar.getInstance().get(Calendar.YEAR);
   }

}
