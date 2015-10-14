/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.render;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.enterprise.inject.spi.BeanManager;

import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.internal.DocumentRuby;
import org.asciidoctor.internal.PreprocessorReader;
import org.jboss.solder.beanManager.BeanManagerLocator;
import org.jboss.solder.beanManager.BeanManagerUtils;
import org.ocpsoft.redoculous.service.impl.RenderService;
import org.ocpsoft.redoculous.util.FilenameUtils;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class GridIncludeProcessor extends IncludeProcessor
{
   private BeanManager beanManager;
   private RenderService renderService;
   private AsciidocRenderer renderer;

   public GridIncludeProcessor(DocumentRuby documentRuby)
   {
      super(documentRuby);
      documentRuby.getAttributes();
   }

   @Override
   public boolean handles(String target)
   {
      return !target.startsWith("http://") && !target.startsWith("https://");
   }

   @Override
   public void process(PreprocessorReader reader, String target, Map<String, Object> attributes)
   {
      RenderRequest request = getAsciidocRenderer().getRequests().peek();
      String path = FilenameUtils.getPath(request.getPath());

      path = FilenameUtils.normalize(path + target);

      String unrenderedDocument = getRenderService().resolveUnRendered(
               new RenderRequest(request.getRepository(), request.getRef(), path));
      if (unrenderedDocument == null)
         unrenderedDocument = "";
      reader.push_include(unrenderedDocument, target, target, 1, attributes);
   }

   private AsciidocRenderer getAsciidocRenderer()
   {
      if (renderer == null)
         renderer = BeanManagerUtils.getContextualInstance(getBeanManager(), AsciidocRenderer.class,
                  new Annotation[] {});
      return renderer;
   }

   private BeanManager getBeanManager()
   {
      if (beanManager == null)
      {
         beanManager = new BeanManagerLocator().getBeanManager();
      }
      return beanManager;
   }

   private RenderService getRenderService()
   {
      if (renderService == null)
         renderService = BeanManagerUtils.getContextualInstance(getBeanManager(), RenderService.class,
                  new Annotation[] {});
      return renderService;
   }
}
