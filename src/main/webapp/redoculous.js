$.support.cors = true;
$.fn.redoculous = function()
{
	var handle = $(this);
	var server = "http://localhost:8080/redoculous";
	var repo = handle.attr("data-repo");
	var ref = handle.attr("data-ref");
	var path = handle.attr("data-path");
	var root = handle.attr("data-root");
	var history = handle.attr("data-history") === "false" ? false : true;

	if (history) History.enabled = true;

	var setupHistory = function(root)
	{
		console.log("Configuring History with root [" + root + "]");
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

		History.pushState({
			state : 1
		}, "Root State", root);
	};

	var loadDoc = function(repo, ref, root, path)
	{
		var url = server + "?repo=" + repo + "&ref=" + ref + "&path=" + path;

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
			var dom = $.parseHTML(html);
			handle.html(html);
			$(handle).find("a").each(function()
			{
				var link = $(this);
				var href = link.attr("href");
				if (href.match(/.*/g) != null)
				{
					// console.log("Processing link [" + href + "]");

					var documentLink = getDocumentLink(root, href);
					if (documentLink != null)
					{
						console.log("Matched link [" + href + "] to document [" + documentLink + "]");

						link.click(function(event)
						{
							History.pushState({
								state : History.getState().state
							}, "State 1", "?state=1");

							event.preventDefault();
							loadDoc(repo, ref, root, href);
						});
					}
					// else
					// console.log("Ignored link [" + href + "]");
				}
			});
			handle.show();
		});
	};

	var getDocumentLink = function(root, href)
	{
		if (href)
		{
			if (href.match(/^(http|www|ftp|mailto|ssh|scp):.*/g) != null) { return null; }

			console.log("Beginning [" + href + "]");

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

			console.log(rootChunks);
			console.log(linkChunks);
			console.log(locationChunks);

			if (href.match(new RegExp("///")) != null) { return true; }
			if (href.match(/ /) != null) { return true; }
			if (href.match(/ /) != null)
			{
			}
			if (href.match(/ /) != null)
			{
			}
			if (href.match(/ /) != null)
			{
			}
			if (href.match(/ /) != null)
			{
			}
		}
		return null;
	};

	if (history) setupHistory(root);
	loadDoc(repo, ref, root, path);

};

$(document).ready(function()
{
	$("[data-redoculous]").redoculous();
});
