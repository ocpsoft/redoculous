/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.cache;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.ocpsoft.rewrite.servlet.config.encodequery.Base64EncodingStrategy;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public final class Keys
{
   private Keys()
   {
   }

   private static final Base64EncodingStrategy encoder = new Base64EncodingStrategy();
   private static MessageDigest digest;

   static
   {
      if (digest == null)
      {
         try
         {
            digest = MessageDigest.getInstance("MD5");
         }
         catch (NoSuchAlgorithmException e)
         {
            throw new IllegalArgumentException(e);
         }
      }
   }

   private static String hash(String value)
   {
      return encoder.encode(new String(digest.digest(value.getBytes()))).replaceAll("/", "_").replaceAll("==$", "");
   }

   /*
    * Key operations
    */

   public static String from(String value)
   {
      return hash(value);
   }

}
