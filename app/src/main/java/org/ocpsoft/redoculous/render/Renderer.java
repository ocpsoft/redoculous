package org.ocpsoft.redoculous.render;

import java.io.InputStream;
import java.io.OutputStream;

public interface Renderer
{
   Iterable<String> getSupportedExtensions();

   String getName();

   void render(InputStream source, OutputStream output);
}
