package org.ocpsoft.redoculous.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ocpsoft.redoculous.config.util.SafeFileNameTransposition;
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
   private File root;

   public PreviewLinkInterceptor(File root)
   {
      this.root = root;
   }

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
         String requestedRef = event.getRequest().getParameter("ref");
         String requestedRepo = event.getRequest().getParameter("repo");

         File refDir = new File(root, SafeFileNameTransposition.toSafeFilename(requestedRepo) + "/refs/" + requestedRef);

         File requestedFile = new File(refDir, requestedPath);
         if (requestedFile.isDirectory())
         {
            requestedFile = new File(requestedFile, "index");
            if (!requestedPath.endsWith("/"))
               requestedPath = requestedPath + "/";

            requestedPath = requestedPath + "index";
         }

         if (!url.matches("^(\\w+://|www\\.|/).*"))
         {
            URLBuilder urlBuilder = URLBuilder.createFrom(requestedPath);
            List<String> segments = new ArrayList<String>(urlBuilder.getSegments());

            if (url.startsWith("."))
            {
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

            if (!requestedFile.isDirectory() && !segments.isEmpty())
            {
               segments.remove(segments.size() - 1);
            }

            String result = URLBuilder.createFrom(segments, urlBuilder.getMetadata()).toPath();

            if (!url.startsWith("/") && !result.endsWith("/"))
            {
               url = "/" + url;
            }

            result = result + url;

            QueryStringBuilder query = QueryStringBuilder.createNew();
            query.addParameters(address.getQuery());
            query.removeParameter("path");
            query.addParameter("path", result);
            matcher.appendReplacement(temp, linkPrefix + query.toQueryString());
         }
      }
      matcher.appendTail(temp);
      buffer.setContents(temp.toString().getBytes());
   }

}
