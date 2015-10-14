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
import javax.ws.rs.core.MediaType;

import org.jruby.embed.ScriptingContainer;
import org.ocpsoft.common.util.Streams;

@ApplicationScoped
public class TextileRenderer implements Renderer, JRubyLoadPathProvider
{
   private static final Charset UTF8 = Charset.forName("UTF8");
   private static final String SCRIPT = "require 'redcloth'\n" +
            "RedCloth.new(input).to_html\n";

   @Inject
   private ScriptingContainer container;

   @Override
   public Iterable<String> getSupportedExtensions()
   {
      return Arrays.asList("textile");
   }

   @Override
   public String getName()
   {
      return "textile";
   }

   @Override
   public void render(RenderRequest request, InputStream inputStream, OutputStream outputStream)
   {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      Streams.copy(inputStream, bos);
      String input = new String(bos.toByteArray(), UTF8);

      container.put("input", input);

      Object output = container.runScriptlet(SCRIPT);
      // write result to the output stream
      try
      {
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
      return Arrays.asList("ruby/redcloth/lib");
   }

   @Override
   public int priority()
   {
      return Renderer.TEXT;
   }

   @Override
   public MediaType getOutputMediaType()
   {
      return MediaType.TEXT_HTML_TYPE;
   }
}
