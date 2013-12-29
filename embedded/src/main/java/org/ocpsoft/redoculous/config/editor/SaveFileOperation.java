/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.config.editor;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.ocpsoft.common.util.Streams;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.servlet.config.HttpOperation;
import org.ocpsoft.rewrite.servlet.config.Response;
import org.ocpsoft.rewrite.servlet.http.event.HttpServletRewrite;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class SaveFileOperation extends HttpOperation
{

   @Override
   public void performHttp(HttpServletRewrite event, EvaluationContext context)
   {
      String requestedPath = event.getRequest().getParameter("path");
      requestedPath = requestedPath.startsWith("file://") ? requestedPath.replaceFirst("file://", "")
               : requestedPath;

      File requestedFile = new File(requestedPath);

      if (requestedFile.isDirectory())
      {
         if (!requestedPath.endsWith("/"))
            requestedPath = requestedPath + "/";
         requestedPath = requestedPath + "index.asciidoc";
      }
      else if (!requestedFile.isFile())
      {
         if (!requestedPath.endsWith(".asciidoc"))
            requestedPath = requestedPath + ".asciidoc";
      }

      requestedFile = new File(requestedPath);

      try {
         String content = event.getRequest().getParameter("content");
         Streams.copy(new ByteArrayInputStream(content.getBytes()),
                  new BufferedOutputStream(new FileOutputStream(requestedFile)));
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
      Response.setStatus(200);
      Response.complete();
   }
}
