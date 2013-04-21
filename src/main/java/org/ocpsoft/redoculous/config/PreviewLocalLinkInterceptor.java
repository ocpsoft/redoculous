package org.ocpsoft.redoculous.config;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ocpsoft.rewrite.servlet.config.response.ResponseContent;
import org.ocpsoft.rewrite.servlet.config.response.ResponseContentInterceptor;
import org.ocpsoft.rewrite.servlet.config.response.ResponseContentInterceptorChain;
import org.ocpsoft.rewrite.servlet.http.event.HttpServletRewrite;
import org.ocpsoft.rewrite.servlet.util.QueryStringBuilder;
import org.ocpsoft.rewrite.servlet.util.URLBuilder;
import org.ocpsoft.urlbuilder.Address;

public class PreviewLocalLinkInterceptor implements ResponseContentInterceptor
{

   @SuppressWarnings("deprecation")
   @Override
   public void intercept(HttpServletRewrite event, ResponseContent buffer, ResponseContentInterceptorChain chain)
   {
      chain.proceed();
      Address address = event.getAddress();

      String contents = new String(buffer.getContents());
      Pattern pattern = Pattern.compile("(<a[^>]+href\\s*=\\s*(?:[\"']))([^\"']+)");
      Matcher matcher = pattern.matcher(contents);
      StringBuffer temp = new StringBuffer();
      while (matcher.find())
      {
         String linkPrefix = matcher.group(1);
         String url = matcher.group(2);
         String requestedPath = event.getRequest().getParameter("path");

         File requestedFile;
         try {
            requestedFile = new File(new URI(requestedPath));
         }
         catch (URISyntaxException e) {
            throw new RuntimeException(e);
         }

         if (!url.matches("^(\\w+://|www\\.|/).*"))
         {
            URLBuilder urlBuilder = URLBuilder.createFrom(requestedPath);
            List<String> segments = new ArrayList<String>(urlBuilder.getSegments());

            boolean directoryProcessed = false;
            if (url.startsWith("."))
            {
               while (!segments.isEmpty())
               {
                  if (url.startsWith("../"))
                  {
                     url = url.substring(3);
                     if (!requestedFile.isDirectory() || directoryProcessed)
                        segments.remove(segments.size() - 1);
                     if (requestedFile.isDirectory() && !directoryProcessed)
                        directoryProcessed = true;
                  }
                  else if (url.startsWith("./"))
                  {
                     url = url.substring(2);
                  }
                  else
                     break;
               }
            }

            if (!requestedFile.isDirectory())
            {
               segments.remove(segments.size() - 1);
            }

            String result = URLBuilder.createFrom(segments, urlBuilder.getMetadata()).toPath();

            if (!url.startsWith("/") && !result.endsWith("/"))
            {
               url = "/" + url;
            }

            QueryStringBuilder query = QueryStringBuilder.createNew();
            query.addParameters(address.getQuery());
            query.removeParameter("path");
            query.addParameter("path", result + url);
            matcher.appendReplacement(temp, linkPrefix + query.toQueryString());
         }
      }
      matcher.appendTail(temp);
      buffer.setContents(temp.toString().getBytes());
   }

}
