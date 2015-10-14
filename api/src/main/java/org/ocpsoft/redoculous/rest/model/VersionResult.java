package org.ocpsoft.redoculous.rest.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@XmlRootElement(name = "versions")
public class VersionResult
{
   private List<String> versions;

   public VersionResult()
   {
      // required for proxying
   }

   public VersionResult(List<String> versions)
   {
      this.versions = versions;
   }

   @XmlElement(name = "version")
   public List<String> getVersions()
   {
      return versions;
   }
}