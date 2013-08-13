package org.ocpsoft.redoculous.exception;

public class NoSuchRepositoryException extends RuntimeException
{
   private static final long serialVersionUID = 5366553704967812669L;
   private String name;

   public NoSuchRepositoryException(String repoName)
   {
      super("No such repository [" + repoName + "]");
      this.name = repoName;
   }

   public String getRepositoryName()
   {
      return name;
   }
}
