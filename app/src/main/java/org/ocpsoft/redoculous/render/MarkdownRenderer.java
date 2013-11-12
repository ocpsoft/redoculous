package org.ocpsoft.redoculous.render;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jruby.embed.ScriptingContainer;
import org.ocpsoft.common.util.Streams;

@ApplicationScoped
public class MarkdownRenderer implements Renderer, JRubyLoadPathProvider
{
   private static final Charset UTF8 = Charset.forName("UTF8");
   private final static String SCRIPT = "require 'maruku'\n" +
            "Maruku.new(input).to_html";

   @Inject
   private ScriptingContainer container;

   @Override
   public Iterable<String> getSupportedExtensions()
   {
      return Arrays.asList("md", "mkd", "mkdn", "mdwn", "mdown", "mdtxt", "mdtext", "markdown");
   }

   @Override
   public String getName()
   {
      return "markdown";
   }

   @Override
   public void render(RenderRequest request, InputStream inputStream, OutputStream outputStream)
   {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      Streams.copy(inputStream, bos);
      String input = new String(bos.toByteArray(), UTF8);

      container.put("input", input);

      Object output;
      try {
         output = container.runScriptlet(SCRIPT);
      }
      catch (RuntimeException e) {
         throw e;
      }

      try
      {
         // write result to the output stream
         outputStream.write(output.toString().getBytes(UTF8));
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   @Override
   public List<String> getLoadPaths()
   {
      return Arrays.asList("ruby/maruku/lib");
   }

}
