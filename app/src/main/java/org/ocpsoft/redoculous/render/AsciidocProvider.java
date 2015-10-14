/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.render;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Startup;
import javax.ejb.Stateful;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.internal.JRubyAsciidoctor;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@Startup
@Stateful
@ApplicationScoped
public class AsciidocProvider
{
   private Asciidoctor asciidoctor;

   @Inject
   private Instance<JRubyLoadPathProvider> renderers;

   public Asciidoctor getAsciidoctor()
   {
      if (asciidoctor == null)
      {
         List<String> loadPaths = new ArrayList<String>();
         for (JRubyLoadPathProvider renderer : renderers) {
            loadPaths.addAll(renderer.getLoadPaths());
         }
         asciidoctor = JRubyAsciidoctor.create(loadPaths);
         asciidoctor.extensionRegistry().includeProcessor(GridIncludeProcessor.class);
      }
      return asciidoctor;
   }

}
