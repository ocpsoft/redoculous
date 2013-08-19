/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.tests;

import java.io.File;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.ocpsoft.redoculous.util.Files;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class RedoculousTestBase
{
   private static final File CURRENT_DIR = Files.getWorkingDirectory();

   public static WebArchive getDeployment()
   {
      return ShrinkWrap.create(WebArchive.class)
               .addAsWebInfResource(new File(CURRENT_DIR.getParent(), "/app/src/main/webapp/WEB-INF/beans.xml"))
               .addAsWebInfResource(new File(CURRENT_DIR.getParent(), "/app/src/main/webapp/WEB-INF/web.xml"))
               .addAsResource(new File(CURRENT_DIR.getParent(), "/app/target/classes/org"))
               .addAsResource(new File(CURRENT_DIR.getParent(), "/app/target/classes/META-INF"))
               .addAsLibraries(Maven.resolver()
                        .loadPomFromFile("pom.xml")
                        .resolve("org.ocpsoft.redoculous:redoculous-server:war:1.0.0-SNAPSHOT")
                        .using(new TransitiveOnlyStrategy())
                        .asFile());
   }

}
