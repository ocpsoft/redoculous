/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.config.editor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.ocpsoft.common.util.Streams;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.servlet.config.HttpOperation;
import org.ocpsoft.rewrite.servlet.config.Response;
import org.ocpsoft.rewrite.servlet.http.event.HttpServletRewrite;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class ServeEditorOperation extends HttpOperation
{

   @Override
   public void performHttp(HttpServletRewrite event, EvaluationContext context)
   {
      try {
         if (event.getRequest().getParameterMap().containsKey("serveContent"))
         {
            String content = getFile(event, context);
            Response.write(content).perform(event, context);
            Response.complete().perform(event, context);
         }
         else
         {
            InputStream editor = ServeEditorOperation.class.getResourceAsStream("/webapp/editor.html");
            String content = Streams.toString(editor);
            Response.write(content).perform(event, context);
            Response.complete().perform(event, context);
         }
      }
      catch (FileNotFoundException e) {
         throw new RuntimeException(e);
      }
   }

   private String getFile(HttpServletRewrite event, EvaluationContext context) throws FileNotFoundException
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

      return Streams.toString(new BufferedInputStream(new FileInputStream(requestedFile)));
   }

}
