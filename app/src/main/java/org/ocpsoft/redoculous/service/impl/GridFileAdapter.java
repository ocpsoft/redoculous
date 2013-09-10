/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.service.impl;

import java.io.File;

import org.infinispan.io.GridFilesystem;
import org.ocpsoft.redoculous.model.impl.FileAdapter;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class GridFileAdapter implements FileAdapter
{
   private GridFilesystem gfs;

   public GridFileAdapter(GridFilesystem gfs)
   {
      this.gfs = gfs;
   }

   @Override
   public File newFile(File parent, String child)
   {
      return gfs.getFile(parent, child);
   }

}
