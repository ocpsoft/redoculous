package org.ocpsoft.redoculous.tests;

import java.util.List;

import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependency;
import org.jboss.shrinkwrap.resolver.api.maven.filter.MavenResolutionFilter;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.MavenResolutionStrategy;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.TransitiveExclusionPolicy;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class TransitiveOnlyStrategy implements MavenResolutionStrategy
{

   @Override
   public MavenResolutionFilter[] getResolutionFilters()
   {
      return new MavenResolutionFilter[] {
               new MavenResolutionFilter()
               {
                  @Override
                  public boolean accepts(MavenDependency dependency, List<MavenDependency> dependenciesForResolution,
                           List<MavenDependency> dependencyAncestors)
                  {
                     for (MavenDependency requested : dependenciesForResolution)
                     {
                        if (requested.getArtifactId().equals(dependency.getArtifactId())
                                 && requested.getGroupId().equals(dependency.getGroupId()))
                        {
                           return false;
                        }
                     }
                     return true;
                  }
               }
      };
   }

   @Override
   public TransitiveExclusionPolicy getTransitiveExclusionPolicy()
   {
      return new TransitiveExclusionPolicy()
      {
         @Override
         public ScopeType[] getFilteredScopes()
         {
            return new ScopeType[] { ScopeType.PROVIDED, ScopeType.RUNTIME, ScopeType.TEST };
         }

         @Override
         public boolean allowOptional()
         {
            return false;
         }
      };
   }

}