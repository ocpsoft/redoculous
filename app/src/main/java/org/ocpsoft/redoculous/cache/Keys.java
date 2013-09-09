/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.cache;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public final class Keys
{

   private Keys()
   {
   }

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

   public static String repository(String repository)
   {
      return hash(repository);
   }

   private static String hash(String value)
   {
      return new String(digest.digest(value.getBytes()));
   }

}
