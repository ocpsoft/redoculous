$.support.cors = true;
$.fn.redoculous = function()
{
   var handle = $(this);
   var server = handle.attr("data-redoculous") + "/api/v1/serve";
   var repo = handle.attr("data-repo");
   var repoRoot = handle.attr("data-repo-root");
   var ref = handle.attr("data-ref");
   var root = handle.attr("data-root");
   var titlePattern = handle.attr("data-title");
   var onload = handle.attr("data-onload");
   var history = handle.attr("data-history") === "true" ? true : false;

   if (history && (History == null))
   {
      console.log("pushstate was activated, but Jquery History.js was not found");
      history = false;
   }
   else if (history) console.log("pushstate will be used for document transitions");

   if (!titlePattern) titlePattern = "%TITLE%";

   var displayContent = function()
   {
      if (history)
      {
         var State = History.getState();
         History.log(State.data, State.title, State.url);
         handle.html(State.data.html);
      }

      handle.find("a").each(
            function()
            {
               var link = $(this);
               var href = link.attr("href");
               if (href.match(/.*/g) != null)
               {
                  var documentLink = getDocumentLink(root, href);
                  if (documentLink != null)
                  {
                     var documentPath = calculateDocPathFromBrowserPath(documentLink);

                     console.log("Mapped link [" + href + "] to document [" + documentPath + "] at address ["
                           + documentLink + "]");
                     if (history)
                     {
                        link.click(function(event)
                        {
                           event.preventDefault();
                           loadDoc(repo, ref, root, documentLink, documentPath);
                        });
                     }
                     else
                     {
                        link.attr("href", documentPath);
                     }
                  }
               }
            });

      if (onload)
      {
         eval(onload);
      }
   };

   var setupHistory = function(root)
   {
      console.log("Configuring History with Redoculous root [" + root + "]");
      var History = window.History;
      if (!History.enabled)
      {
         console.log("History not enabled. Cancelling...");
         return false;
      }

      History.Adapter.bind(window, 'statechange', displayContent);
   };

   var loadDoc = function(repo, ref, root, browserPath, docPath)
   {
      var url = server + "?repo=" + repo + "&ref=" + ref + "&path=" + docPath;

      console.log("Requesting document [" + url + "]");

      $.ajax({
         url : url,
         cache : false,
         dataType : "html",
         type : "GET"
      })
      // Failure
      .fail(function(xhr, status, error)
      {
         console.log("Error fetching document [" + url + "] - [" + status + "-" + error + "]");
         handle.html("Content temporarily unavailable. System may be down for maintenance.");
      })
      // Success
      .done(function(html)
      {
         if (history)
         {
            var title = $('<div/>').html(html).find("h1").filter(":first");
            if (title)
            {
               title = title.text();
            }
            History.pushState({
               "html" : html
            }, titlePattern.replace("%TITLE%", title), browserPath);
         }
         else
         {
            handle.html(html);
         }
      });
   };

   var getDocumentLink = function(root, href)
   {
      String.prototype.endsWith = function(suffix)
      {
         return this.indexOf(suffix, this.length - suffix.length) !== -1;
      };

      if (href)
      {
         if (href.match(/^(https?|www|ftps?|mailto|ssh|scp):.*/g) != null) { return null; }
         if (href.match(/.*#.*/g) != null) { return null; }

         var currentPath = window.location.pathname;
         var directory = false;

         if (currentPath.endsWith("/")) directory = true;

         if (root.indexOf("/", 0) == 0) root = root.substring(1);
         var rootChunks = root.split("/");

         if (currentPath.indexOf("/", 0) == 0) currentPath = currentPath.substring(1);
         var locationChunks = currentPath.split("/");
         while (locationChunks.length > 0 && locationChunks[locationChunks.length - 1] == "")
         {
            locationChunks.pop();
         }

         if (href.indexOf("/", 0) == 0) href = href.substring(1);
         var linkChunks = href.split("/");

         while (linkChunks.length > 0 && locationChunks.length > 0)
         {
            if (linkChunks[0] === ".")
            {
               linkChunks.shift();
               if (!directory)
               {
                  locationChunks.pop();
                  directory = true;
               }
            }
            else if (linkChunks[0] === "..")
            {
               linkChunks.shift();
               locationChunks.pop();
            }
            else
               break;

         }

         var result = "/" + locationChunks.join("/") + "/" + linkChunks.join("/");

         if (locationChunks.length >= rootChunks.length) { return result; }
      }
      return null;
   };

   var calculateDocPathFromBrowserPath = function(browserPath, initial)
   {
      if (root.indexOf("/", 0) == 0) root = root.substring(1);
      var rootChunks = root.split("/");

      if (browserPath.indexOf("/", 0) == 0) browserPath = browserPath.substring(1);
      var pathChunks = browserPath.split("/");

      while (rootChunks.length > 0 && pathChunks.length > 0)
      {
         if (!initial || !(rootChunks[0] === ""))
         {
            pathChunks.shift();
         }
         rootChunks.shift();
      }

      var result = repoRoot;
      if (pathChunks.length > 0) result = repoRoot + "/" + pathChunks.join("/");
      while (result.indexOf("//", 0) == 0)
         result = result.substring(1);

      console.log("Calculated document path [" + result + "] repository root [" + repoRoot + "], site root [" + root
            + "], and location [" + browserPath + "].");

      return result;
   };

   if (history) setupHistory(root);

   var initialDocPath = calculateDocPathFromBrowserPath(window.location.pathname, true);
   loadDoc(repo, ref, root, window.location.pathname, initialDocPath);
   displayContent();

};

$(document).ready(function()
{
   $("[data-redoculous]").redoculous();
});
