/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.service.impl;

import java.io.File;

import org.ocpsoft.redoculous.model.impl.FileAdapter;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class NativeFileAdapter implements FileAdapter
{
   private static final long serialVersionUID = -6825986937607933580L;

   @Override
   public File newFile(File parent, String child)
   {
      return new File(parent, child);
   }
}
