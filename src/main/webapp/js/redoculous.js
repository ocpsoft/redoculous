$.support.cors = true;
$.fn.redoculous = function()
{
	var handle = $(this);
	var server = handle.attr("data-redoculous") + "/serve";
	var repo = handle.attr("data-repo");
	var repoRoot = handle.attr("data-repo-root");
	var ref = handle.attr("data-ref");
	var root = handle.attr("data-root");
	var titlePattern = handle.attr("data-title");
	var onload = handle.attr("data-onload");
	var history = handle.attr("data-history") === "false" ? false : true;

	if (!titlePattern) titlePattern = "%TITLE%";
	if (history) History.enabled = true;

	var displayContent = function()
	{
		var State = History.getState();
		History.log(State.data, State.title, State.url);

		handle.html(State.data.html);

		handle.find("a").each(
				function()
				{
					var link = $(this);
					var href = link.attr("href");
					if (href.match(/.*/g) != null)
					{
						// console.log("Processing link [" + href +
						// "]");

						var documentLink = getDocumentLink(root, href);
						if (documentLink != null)
						{
							var documentPath = calculateDocPathFromBrowserPath(documentLink);

							console.log("Mapped link [" + href + "] to document [" + documentPath + "] at address [" + documentLink
									+ "]");
							// link.attr("href", documentLink);
							link.click(function(event)
							{
								event.preventDefault();
								loadDoc(repo, ref, root, documentLink, documentPath);
							});
						}
						// else
						// console.log("Ignored link [" + href + "]");
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
		})
		// Success
		.done(function(html)
		{
			var title = $('<div/>').html(html).find("h1").filter(":first");
			if (title)
			{
				title = title.text();
			}
			History.pushState({
				"html" : html
			}, titlePattern.replace("%TITLE%", title), browserPath);
		});
	};

	var getDocumentLink = function(root, href)
	{
	    String.prototype.endsWith = function(suffix) {
            return this.indexOf(suffix, this.length - suffix.length) !== -1;
        };

		if (href)
		{
			if (href.match(/^(http|www|ftp|mailto|ssh|scp):.*/g) != null) { return null; }
			if (href.match(/.*#.*/g) != null) { return null; }

			var currentPath = window.location.pathname;
			var directory = false;
			
			if(currentPath.endsWith("/"))
			    directory = true;

			if (root.indexOf("/", 0) == 0) root = root.substring(1);
			var rootChunks = root.split("/");

			if (currentPath.indexOf("/", 0) == 0) currentPath = currentPath.substring(1);
			var locationChunks = currentPath.split("/");
			while( locationChunks.length > 0 && locationChunks[locationChunks.length - 1] == "")
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
					if(!directory)
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

	var calculateDocPathFromBrowserPath = function(browserPath)
	{
		if (root.indexOf("/", 0) == 0) root = root.substring(1);
		var rootChunks = root.split("/");

		if (browserPath.indexOf("/", 0) == 0) browserPath = browserPath.substring(1);
		var pathChunks = browserPath.split("/");

		while (rootChunks.length > 0 && pathChunks.length > 0)
		{
			rootChunks.shift();
			pathChunks.shift();
		}

		var result = repoRoot;
		if (pathChunks.length > 0)
		{
			result = repoRoot + "/" + pathChunks.join("/");
		}

		console.log("Calculated document path [" + result + "] repository root [" + repoRoot + "], site root [" + root
				+ "], and location [" + browserPath + "].");

		return result;
	};

	if (history) setupHistory(root);

	var initialDocPath = calculateDocPathFromBrowserPath(window.location.pathname);
	loadDoc(repo, ref, root, window.location.pathname, initialDocPath);
	displayContent();

};

$(document).ready(function()
{
	$("[data-redoculous]").redoculous();
});
