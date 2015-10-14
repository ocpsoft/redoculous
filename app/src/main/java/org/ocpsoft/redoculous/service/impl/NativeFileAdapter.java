/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.service.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

   @Override
   public InputStream getInputStream(File file) throws IOException
   {
      return new BufferedInputStream(new FileInputStream(file));
   }

   @Override
   public OutputStream getOutputStream(File file) throws IOException
   {
      return new BufferedOutputStream(new FileOutputStream(file));
   }
}
