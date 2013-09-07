package org.ocpsoft.redoculous.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;
import java.util.Set;
import java.util.HashSet;
import org.ocpsoft.redoculous.model.Ref;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;

@Entity
public class Repository implements Serializable
{
   private static final long serialVersionUID = 7646097439293871039L;

   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   @Column(name = "id", updatable = false, nullable = false)
   private Long id = null;

   @Version
   @Column(name = "version")
   private int version = 0;

   @Column
   private String url;

   @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL)
   private Set<Ref> refs = new HashSet<Ref>();

   public Long getId()
   {
      return this.id;
   }

   public void setId(final Long id)
   {
      this.id = id;
   }

   public int getVersion()
   {
      return this.version;
   }

   public void setVersion(final int version)
   {
      this.version = version;
   }

   @Override
   public boolean equals(Object that)
   {
      if (this == that)
      {
         return true;
      }
      if (that == null)
      {
         return false;
      }
      if (getClass() != that.getClass())
      {
         return false;
      }
      if (id != null)
      {
         return id.equals(((Repository) that).id);
      }
      return super.equals(that);
   }

   @Override
   public int hashCode()
   {
      if (id != null)
      {
         return id.hashCode();
      }
      return super.hashCode();
   }

   public String getUrl()
   {
      return this.url;
   }

   public void setUrl(final String url)
   {
      this.url = url;
   }

   @Override
   public String toString()
   {
      String result = getClass().getSimpleName() + " ";
      if (url != null && !url.trim().isEmpty())
         result += "url: " + url;
      return result;
   }

   public Set<Ref> getRefs()
   {
      return this.refs;
   }

   public void setRefs(final Set<Ref> refs)
   {
      this.refs = refs;
   }
}