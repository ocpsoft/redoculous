package org.ocpsoft.redoculous;

import java.io.File;
import java.io.IOException;

import org.ocpsoft.rewrite.exception.RewriteException;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class Redoculous
{
   private static File root = null;

   public static File getRoot()
   {
      if (root == null)
      {
         try {
            root = File.createTempFile("redoculous", "");
            root.delete();
            root.mkdirs();
         }
         catch (IOException e) {
            throw new RewriteException("Could not create temp folder for doc files or cache.", e);
         }
      }
      return root;
   }
}
