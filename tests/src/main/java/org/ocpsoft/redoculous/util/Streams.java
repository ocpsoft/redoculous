/*
 * Copyright 2012 <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ocpsoft.redoculous.util;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

/**
 * Utility methods for working with {@link InputStream} and {@link OutputStream} instances.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public final class Streams
{
   public Streams()
   {}

   private static final int BUFFER_SIZE = 2048;

   /**
    * Copy all contents from the {@link InputStream} to the {@link OutputStream}, using a buffer of size 2048.
    */
   public static boolean copy(InputStream input, OutputStream output)
   {
      return copy(input, output, BUFFER_SIZE);
   }

   /**
    * Copy all contents from the {@link InputStream} to the {@link OutputStream}, using a buffer of the given size.
    */
   public static boolean copy(InputStream input, OutputStream output, int bufferSize)
   {
      try
      {
         byte[] buffer = new byte[bufferSize];

         int read = input.read(buffer);
         while (read != -1) {
            output.write(buffer, 0, read);
            read = input.read(buffer);
         }

         output.flush();
      }
      catch (IOException e) {
         return false;
      }
      return true;
   }

   /**
    * Return a {@link String} containing the contents of the given {@link InputStream}
    */
   public static String toString(final InputStream stream)
   {
      StringBuilder out = new StringBuilder();
      try
      {
         final char[] buffer = new char[0x10000];
         Reader in = new InputStreamReader(stream, "UTF-8");
         int read;
         do
         {
            read = in.read(buffer, 0, buffer.length);
            if (read > 0)
            {
               out.append(buffer, 0, read);
            }
         }
         while (read >= 0);
      }
      catch (UnsupportedEncodingException e)
      {
         throw new RuntimeException(e);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      return out.toString();
   }

   /**
    * Create an {@link InputStream} from the given {@link String}.
    */
   public static InputStream fromString(final String data)
   {
      return new ByteArrayInputStream(data.getBytes());
   }

   /**
    * Close the given {@link Closeable} without throwing exceptions on failure.
    */
   public static void closeQuietly(final Closeable source)
   {
      if (source != null)
      {
         try
         {
            source.close();
         }
         catch (IOException ignore)
         {

         }
      }
   }
}
