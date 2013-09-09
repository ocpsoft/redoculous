/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.cache;

import org.ocpsoft.redoculous.util.OperatingSystemUtils;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public final class Keys
{

   private Keys()
   {
   }

   public static String repository(String repository)
   {
      return OperatingSystemUtils.getSafeFilename(repository);
   }

}
