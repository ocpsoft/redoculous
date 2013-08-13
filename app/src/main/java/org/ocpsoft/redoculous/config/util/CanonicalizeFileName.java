package org.ocpsoft.redoculous.config.util;

import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;
import org.ocpsoft.rewrite.param.Transposition;

public final class CanonicalizeFileName implements Transposition<String>
{
   @Override
   public String transpose(Rewrite event, EvaluationContext context, String value)
   {
      value = value.replaceAll("(.*)\\.(asciidoc|ad|adoc)$", "$1");
      return value;
   }
}