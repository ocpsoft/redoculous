/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;

import org.ocpsoft.redoculous.util.Files;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
@Stateless
public class FileOperations
{
   @Asynchronous
   public Future<Void> copyDirectoryAsync(File source, File destination)
   {
      copyDirectory(source, destination);
      return new AsyncResult<Void>(null);
   }

   public void copyDirectory(File source, File destination)
   {
      try
      {
         Files.copyDirectory(source, destination);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Could not replicate repository to GridFilesystem", e);
      }
   }

}
