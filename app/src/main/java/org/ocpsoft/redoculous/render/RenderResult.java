/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.render;

import java.io.InputStream;

import javax.ws.rs.core.MediaType;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class RenderResult
{

   private MediaType mediaType;
   private InputStream stream;

   public InputStream getStream()
   {
      return stream;
   }

   public void setStream(InputStream stream)
   {
      this.stream = stream;
   }

   public MediaType getMediaType()
   {
      return mediaType;
   }

   public void setMediaType(MediaType mediaType)
   {
      this.mediaType = mediaType;
   }

}
