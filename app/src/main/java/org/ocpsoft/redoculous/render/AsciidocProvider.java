/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.render;

import java.util.Arrays;

import javax.ejb.Startup;
import javax.ejb.Stateful;
import javax.enterprise.context.ApplicationScoped;

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
   private final Asciidoctor asciidoctor;

   public AsciidocProvider()
   {
      asciidoctor = JRubyAsciidoctor.create(Arrays.asList("gems/asciidoctor-0.1.4/lib"));
      asciidoctor.extensionRegistry().includeProcessor(GridIncludeProcessor.class);
   }

   public Asciidoctor getAsciidoctor()
   {
      return asciidoctor;
   }

}
