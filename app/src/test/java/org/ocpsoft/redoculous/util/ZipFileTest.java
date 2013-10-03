/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.util;

import java.io.File;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

import org.junit.Test;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class ZipFileTest
{

   @Test
   public void test() throws ZipException
   {
      ZipFile zipFile = new ZipFile("/Users/lb3/Desktop/rewrite/output.zip");
      ZipParameters parameters = new ZipParameters();
      parameters.setIncludeRootFolder(false);
      parameters.setReadHiddenFiles(true);
      zipFile.createZipFileFromFolder(new File("/Users/lb3/projects/ocpsoft/rewrite/"), parameters, false, 0);
   }

}
