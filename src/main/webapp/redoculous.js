$.support.cors = true;
$.fn.redoculous = function()
{
	var handle = $(this);
	var server = handle.attr("data-redoculous");
	var repo = handle.attr("data-repo");
	var repoRoot = handle.attr("data-repo-root");
	var ref = handle.attr("data-ref");
	var root = handle.attr("data-root");
	var history = handle.attr("data-history") === "false" ? false : true;

	if (history) History.enabled = true;

	var setupHistory = function(root)
	{
		console.log("Configuring History with Redoculous root [" + root + "]");
		var History = window.History;
		if (!History.enabled)
		{
			console.log("History not enabled. Cancelling...");
			return false;
		}

		History.Adapter.bind(window, 'statechange', function()
		{
			var State = History.getState();
			History.log(State.data, State.title, State.url);
		});
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
		.done(
				function(html)
				{
					var dom = $.parseHTML(html);
					handle.html(html);

					if (window.location.pathname != browserPath)
					{
						History.pushState({
							state : History.getState().state
						}, "State 1", browserPath);
					}

					$(handle).find("a").each(
							function()
							{
								var link = $(this);
								var href = link.attr("href");
								if (href.match(/.*/g) != null)
								{
									// console.log("Processing link [" + href + "]");

									var documentLink = getDocumentLink(root, href);
									if (documentLink != null)
									{
										var documentPath = calculateDocPathFromBrowserPath(documentLink);

										console.log("Mapped link [" + href + "] to document [" + documentPath + "] at address ["
												+ documentLink + "]");
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
				});
	};

	var getDocumentLink = function(root, href)
	{
		if (href)
		{
			if (href.match(/^(http|www|ftp|mailto|ssh|scp):.*/g) != null) { return null; }

			var currentPath = window.location.pathname;

			if (root.indexOf("/", 0) == 0) root = root.substring(1);
			var rootChunks = root.split("/");

			if (currentPath.indexOf("/", 0) == 0) currentPath = currentPath.substring(1);
			var locationChunks = currentPath.split("/");

			if (href.indexOf("/", 0) == 0) href = href.substring(1);
			var linkChunks = href.split("/");

			while (linkChunks.length > 0 && locationChunks.length > 0)
			{
				if (linkChunks[0] === ".")
					linkChunks.shift();
				else if (linkChunks[0] === "..")
				{
					linkChunks.shift();
					locationChunks.pop();
				}
				else
					break;

			}

			var result = "/" + rootChunks.join("/") + "/" + linkChunks.join("/");

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

		var result = browserPath;
		if (pathChunks.length > 0)
		{
			result = repoRoot + "/" + pathChunks.join("/");
		}
		;

		console.log("Calculated document path [" + result + "] repository root [" + repoRoot + "], site root [" + root
				+ "], and location [" + browserPath + "].");

		return result;
	};

	if (history) setupHistory(root);

	var initialDocPath = calculateDocPathFromBrowserPath(window.location.pathname);
	loadDoc(repo, ref, root, window.location.pathname, initialDocPath);

};

$(document).ready(function()
{
	$("[data-redoculous]").redoculous();
});
