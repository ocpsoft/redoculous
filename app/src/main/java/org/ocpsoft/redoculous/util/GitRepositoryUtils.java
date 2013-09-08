package org.ocpsoft.redoculous.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.TagOpt;
import org.ocpsoft.redoculous.Redoculous;
import org.ocpsoft.redoculous.config.git.GitUtils;
import org.ocpsoft.redoculous.config.util.DocumentFilter;
import org.ocpsoft.redoculous.config.util.SafeFileNameTransposition;
import org.ocpsoft.redoculous.render.Renderer;
import org.ocpsoft.rewrite.exception.RewriteException;

public class GitRepositoryUtils
{
   @Inject
   @Any
   private Instance<Renderer> renderers;

   public File getRepoDir(String repo)
   {
      final File root = Redoculous.getRoot();
      String safeRepoName = SafeFileNameTransposition.toSafeFilename(repo);
      File result = new File(root, safeRepoName + "/repo");
      return result;
   }

   public File getRefsDir(String repo)
   {
      final File root = Redoculous.getRoot();
      String safeRepoName = SafeFileNameTransposition.toSafeFilename(repo);
      File result = new File(root, safeRepoName + "/refs");
      return result;
   }

   public File getCacheDir(String repo)
   {
      final File root = Redoculous.getRoot();
      String safeRepoName = SafeFileNameTransposition.toSafeFilename(repo);
      File result = new File(root, safeRepoName + "/cache");
      return result;
   }

   public void invalidate(String repo)
   {
      File refsDir = getRefsDir(repo);
      File cacheDir = getCacheDir(repo);

      Files.delete(refsDir, true);
      Files.delete(cacheDir, true);
      cacheDir.mkdirs();
   }

   public void initRef(String repo, String ref)
   {
      File repoDir = getRepoDir(repo);

      ref = resolveRef(ref);

      File refDir = getRefDir(repo, ref);
      File refCacheDir = getRefCacheDir(repo, ref);
      if (!refDir.exists())
      {
         System.out.println("Creating ref copy [" + repo + "] [" + ref + "]");
         refDir.mkdirs();
         refCacheDir.mkdirs();
         Git git = null;
         try
         {
            git = Git.open(repoDir);

            git.checkout().setName(ref).call();

            System.out.println("Deleting cache for [" + repo + "] [" + ref + "]");
            Files.delete(refDir, true);
            Files.delete(refCacheDir, true);
            refCacheDir.mkdirs();
            Files.copyDirectory(repoDir, refDir, new DocumentFilter(renderers));
         }
         catch (Exception e)
         {
            if (git != null)
            {
               GitUtils.close(git);
               git = null;
            }
            Files.delete(refDir, true);
            Files.delete(refCacheDir, true);
            throw new RewriteException("Could checkout ref [" + ref
                     + "] from repository [" + repo + "].", e);
         }
         finally
         {
            if (git != null)
            {
               GitUtils.close(git);
            }
         }
      }
   }

   public File getRefDir(String repo, String ref)
   {
      return new File(getRefsDir(repo), SafeFileNameTransposition.toSafeFilename(resolveRef(ref)));
   }

   public File getRefCacheDir(String repo, String ref)
   {
      return new File(getCacheDir(repo), SafeFileNameTransposition.toSafeFilename(resolveRef(ref)));
   }

   public File resolvePath(String repo, String ref, String path)
   {
      File refDir = getRefDir(repo, ref);
      File pathFile = resolvePath(path, refDir);
      return pathFile;
   }

   public File resolveCachePath(String repo, String ref, String path)
   {
      File refCacheDir = getRefCacheDir(repo, ref);
      File pathFile = resolvePath(path, refCacheDir);
      return pathFile;
   }

   public File resolveRendered(String repo, String ref, String path)
   {
      File result = resolveCachePath(repo, ref, path);
      if (!result.exists())
      {
         File source = resolvePath(repo, ref, path);
         if (source.exists())
         {
            result = new File(getRefCacheDir(repo, ref), source.getName());
            LOOP: for (Renderer renderer : renderers)
            {
               for (String extension : renderer.getSupportedExtensions())
               {
                  if (extension.matches(source.getName().replaceAll("^.*\\.([^.]+)", "$1")))
                  {
                     render(renderer, source, result);
                     break LOOP;
                  }
               }
            }
         }
      }
      return result;
   }

   private void render(Renderer renderer, File source, File result)
   {
      InputStream input = null;
      OutputStream output = null;
      try
      {
         result.createNewFile();
         input = new BufferedInputStream(new FileInputStream(source));
         output = new BufferedOutputStream(new FileOutputStream(result));
         renderer.render(input, output);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         if (input != null)
            try
            {
               input.close();
            }
            catch (IOException e)
            {
               throw new RuntimeException(e);
            }
         if (output != null)
            try
            {
               output.close();
            }
            catch (Exception e)
            {
               throw new RuntimeException(e);
            }
      }
   }

   private String resolveRef(String ref)
   {
      if (!ref.startsWith("refs/"))
      {
         ref = "refs/remotes/origin/" + ref;
      }
      return ref;
   }

   private File resolvePath(String path, File refDir)
   {
      File pathFile = new File(refDir, path);
      if (path.endsWith("/") && pathFile.isDirectory())
      {
         LOOP: for (Renderer renderer : renderers)
         {
            for (String extension : renderer.getSupportedExtensions())
            {
               pathFile = new File(refDir, path + "/index." + extension);
               if (pathFile.isFile())
                  break LOOP;
            }
         }
      }

      if (!pathFile.exists())
      {
         LOOP: for (Renderer renderer : renderers)
         {
            for (String extension : renderer.getSupportedExtensions())
            {
               pathFile = new File(refDir, path + "." + extension);
               if (pathFile.isFile())
                  break LOOP;
            }
         }
      }
      return pathFile;
   }

   public void clone(String repo)
   {
      File repoDir = getRepoDir(repo);
      File refsDir = getRefsDir(repo);
      File cacheDir = getCacheDir(repo);

      if (!repoDir.exists())
      {
         try
         {
            repoDir.mkdirs();
            refsDir.mkdirs();
            cacheDir.mkdirs();

            Git git = Git.cloneRepository().setURI(repo)
                     .setRemote("origin").setCloneAllBranches(true)
                     .setDirectory(repoDir)
                     .setProgressMonitor(new TextProgressMonitor()).call();

            git.fetch().setRemote("origin").setTagOpt(TagOpt.FETCH_TAGS)
                     .setThin(false).setTimeout(10)
                     .setProgressMonitor(new TextProgressMonitor()).call();

            GitUtils.close(git);
         }
         catch (Exception e)
         {
            throw new RuntimeException("Could not clone repository [" + repo + "]", e);
         }
      }
   }

}
