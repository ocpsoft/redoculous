package org.ocpsoft.redoculous.rest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.lib.Ref;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.ocpsoft.redoculous.config.git.GitUtils;
import org.ocpsoft.redoculous.repositories.RepositoryUtils;

@Path("/serve")
@Produces({ "text/html" })
public class DocumentService
{
   private static final String UTF8 = "UTF8";

   @Inject
   private RepositoryUtils repositories;

   @GET
   public Response serve(
            @QueryParam("repo") String repo,
            @QueryParam("ref") String ref,
            @QueryParam("path") String path)
            throws Exception
   {
      repositories.initRef(repo, ref);
      File document = repositories.resolveRendered(repo, ref, path);
      Document parsed = Jsoup.parse(document, UTF8);
      return Response.ok(parsed.toString()).build();
   }

   @GET
   @Path("/toc")
   public Response getTableOfContents(
            @QueryParam("repo") String repo,
            @QueryParam("ref") String ref,
            @QueryParam("path") String path)
            throws Exception
   {
      repositories.initRef(repo, ref);
      File file = repositories.resolveRendered(repo, ref, path);
      Document document = Jsoup.parse(file, UTF8);
      Element toc = document.getElementById("toc");
      if (toc != null)
         return Response.ok(toc.toString()).build();
      return Response.noContent().build();
   }

   @GET
   @Path("/versions")
   @Produces({ "application/xml", "application/json" })
   public VersionResult getAvailableVersions(@QueryParam("repo") String repo,
            @QueryParam("filter") @DefaultValue(".*") String filter)
            throws Exception
   {
      List<String> result = new ArrayList<String>();

      File repoDir = repositories.getRepoDir(repo);

      Git git = null;
      try {
         git = Git.open(repoDir);
         List<Ref> branches = git.branchList().setListMode(ListMode.ALL).call();
         result.addAll(processRefs(branches, filter));
         List<Ref> tags = git.tagList().call();
         result.addAll(processRefs(tags, filter));
      }
      finally {
         if (git != null) {
            GitUtils.close(git);
         }
      }

      VersionResult versions = new VersionResult(result);
      return versions;
   }

   @XmlRootElement(name = "versions")
   public static class VersionResult
   {
      private List<String> versions;

      public VersionResult()
      {}

      public VersionResult(List<String> versions)
      {
         this.versions = versions;
      }

      @XmlElement
      public List<String> getVersions()
      {
         return versions;
      }
   }

   private List<String> processRefs(List<Ref> refs, String filter)
   {
      List<String> result = new ArrayList<String>();
      for (Ref ref : refs) {
         String name = ref.getName();
         if (filter == null || filter.isEmpty() || name.matches(filter))
            result.add(name);
      }
      return result;
   }

}
