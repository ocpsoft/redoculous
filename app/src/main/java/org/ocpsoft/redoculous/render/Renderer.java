package org.ocpsoft.redoculous.render;

import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.core.MediaType;

import org.ocpsoft.common.pattern.Weighted;

/**
 * Responsible for rendering a source file into its resultant output type.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 *
 */
public interface Renderer extends Weighted
{
   /**
    * Priority category for rendering binary files.
    */
   int BINARY = 1000000;

   /**
    * Priority category for rendering text files and documents.
    */
   int TEXT = 0;

   /**
    * Return the supported file extensions or file-name regular expressions which for this {@link Renderer} is
    * responsible.
    */
   Iterable<String> getSupportedExtensions();

   /**
    * Get the name of this {@link Renderer}.
    */
   String getName();

   /**
    * Render the requested {@link InputStream} and pipe to {@link OutputStream}.
    */
   void render(RenderRequest request, InputStream source, OutputStream output);

   /**
    * Return the output media type of this renderer.
    */
   MediaType getOutputMediaType();
}
