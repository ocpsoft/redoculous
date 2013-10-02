package org.ocpsoft.redoculous.config;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.ocpsoft.rewrite.servlet.config.response.ResponseContent;
import org.ocpsoft.rewrite.servlet.config.response.ResponseContentInterceptor;
import org.ocpsoft.rewrite.servlet.config.response.ResponseContentInterceptorChain;
import org.ocpsoft.rewrite.servlet.http.event.HttpServletRewrite;
import org.ocpsoft.rewrite.servlet.util.QueryStringBuilder;
import org.ocpsoft.rewrite.servlet.util.URLBuilder;
import org.ocpsoft.urlbuilder.Address;

@SuppressWarnings("deprecation")
public class PreviewLinkInterceptor implements ResponseContentInterceptor
{
   @Override
   public void intercept(HttpServletRewrite event, ResponseContent buffer, ResponseContentInterceptorChain chain)
   {
      chain.proceed();
      Address address = event.getAddress();

      String contents = new String(buffer.getContents());
      Document document = Jsoup.parse(contents, "UTF-8");
      Elements links = document.getElementsByAttribute("href");
      String requestedPath = event.getRequest().getParameter("path");

      for (Element link : links)
      {
         String url = link.attr("href");

         if (!url.matches("^(\\w+://|www\\.|/).*") && !url.startsWith("#"))
         {
            URLBuilder urlBuilder = URLBuilder.createFrom(requestedPath);
            List<String> segments = new ArrayList<String>(urlBuilder.getSegments());

            if (url.startsWith("."))
            {
               if (!urlBuilder.hasTrailingSlash())
               {
                  segments.remove(segments.size() - 1);
                  urlBuilder.getMetadata().setTrailingSlash(true);
               }

               while (!segments.isEmpty())
               {
                  if (url.startsWith("../"))
                  {
                     url = url.substring(3);
                     segments.remove(segments.size() - 1);
                  }
                  else if (url.startsWith("./"))
                  {
                     url = url.substring(2);
                  }
                  else
                     break;
               }
            }

            String path = URLBuilder.createFrom(segments, urlBuilder.getMetadata()).toPath();

            if (!url.startsWith("/") && !path.endsWith("/"))
            {
               url = "/" + url;
            }

            path = path + url;

            QueryStringBuilder query = QueryStringBuilder.createNew();
            query.addParameters(address.getQuery());
            query.removeParameter("path");
            query.addParameter("path", path);

            String result = address.getPath() + query.toQueryString();
            link.attr("href", result);
         }
      }
      buffer.setContents(document.toString().getBytes());
   }

}
