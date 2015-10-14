package org.ocpsoft.redoculous.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import javax.servlet.ServletOutputStream;

import org.ocpsoft.common.util.Streams;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.exception.RewriteException;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.Parameterized;
import org.ocpsoft.rewrite.param.ParameterizedPatternBuilder;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternBuilder;
import org.ocpsoft.rewrite.servlet.config.HttpOperation;
import org.ocpsoft.rewrite.servlet.http.event.HttpServletRewrite;

public class Proxy extends HttpOperation implements Parameterized
{
   private ParameterizedPatternBuilder urlBuilder;

   public Proxy(String url)
   {
      this.urlBuilder = new RegexParameterizedPatternBuilder(url);
   }

   public static Operation to(String url)
   {
      return new Proxy(url);
   }

   @Override
   public void performHttp(HttpServletRewrite event, EvaluationContext context)
   {
      String compiledUrl = urlBuilder.build(event, context);
      InputStream stream = null;
      try {
         URL url = new URL(compiledUrl);
         stream = url.openStream();
         ServletOutputStream responseStream = event.getResponse().getOutputStream();
         Streams.copy(stream, responseStream);
      }
      catch (MalformedURLException e) {
         throw new RewriteException("Could not parse URL [" + compiledUrl + "]", e);
      }
      catch (IOException e) {
         throw new RewriteException("Could read from URL [" + compiledUrl + "]", e);
      }
      finally
      {
         if (stream != null)
         {
            try {
               stream.close();
            }
            catch (IOException e) {
               throw new RewriteException("Error closing stream from URL [" + compiledUrl + "]", e);
            }
         }
      }
   }

   @Override
   public Set<String> getRequiredParameterNames()
   {
      return urlBuilder.getRequiredParameterNames();
   }

   @Override
   public void setParameterStore(ParameterStore store)
   {
      urlBuilder.setParameterStore(store);
   }

}
