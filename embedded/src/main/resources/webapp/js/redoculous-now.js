$.support.cors = false;

$.fn.redoculousNow = function() {

	var handle = $(this);
	var titlePattern = "%TITLE%";

	var connectToParent = function() {
		if (window.opener) {
			if (window.opener.getParameterByName("path") != getParameterByName("path")) {
				window.opener.location.search = "?path=" + getCurrentFilePath();
			}
			window.opener.setViewerOpen(window);
			window.setTimeout(connectToParent, 500);
		}
	};

	connectToParent();

	/* Begin SyncCursor */
	var syncCursorHighlighter = null;
	var syncCursorTimeoutId;
	var syncCursor = function() {
		window.clearTimeout(syncCursorTimeoutId);

		if (window.opener) {
			if (window.opener.follow && window.opener.syncCursorUpdateRequired) {

				if (syncCursorHighlighter == null) {
					$("html")
							.append(
									"<div id='redoculousCursorHighlighter' style='width: 100%; height: 100px; background-color: yellow; position: absolute; left: 0; opacity: 0.2;'></div>")
					syncCursorHighlighter = $('#redoculousCursorHighlighter');
				}

				syncCursorHighlighter.stop();
				syncCursorHighlighter.animate({
					top : ($("body").height())
							* window.opener.syncCursorPercent + 25
				}, 250);
				window.opener.syncCursorUpdateRequired = false;
			}
			if (!window.opener.follow && syncCursorHighlighter) {
				syncCursorHighlighter.remove();
				syncCursorHighlighter = null;
			}
		}

		window.setTimeout(syncCursor, 100);
	}

	syncCursor();
	/* End SyncCursor */

	/* Begin SyncScroll */
	var syncScrollTimeoutId;
	var syncScroll = function() {
		window.clearTimeout(syncScrollTimeoutId);

		if (window.opener) {
			if (window.opener.follow && window.opener.syncScrollUpdateRequired) {

				var newScrollTop = ($(document).height() - $(window).height())
						* window.opener.syncScrollPercent;

				$('html, body').stop();
				$('html, body').animate({
					scrollTop : newScrollTop
				}, 250);
				window.opener.syncScrollUpdateRequired = false;
			}
		}

		window.setTimeout(syncScroll, 100);
	}

	syncScroll();
	/* End SyncScroll */

	/* Unload Sync */
	$(window).unload(function() {
		if (window.opener) {
			window.opener.onViewerClosed();
		}
	});

	var ajaxCall = function() {

		if (!window.opener || window.opener.viewerUpdateRequired) {

			if (window.opener)
				window.opener.viewerUpdateRequired = false;

			var url = window.location;
			console.log("Requesting document [" + url + "]");

			$.ajax({
				url : url,
				cache : false,
				dataType : "html",
				type : "GET"
			})
			// Failure
			.fail(
					function(xhr, status, error) {
						console.log("Error fetching document [" + url + "] - ["
								+ status + "-" + error + "]");

						setTimeout(reload, 100);
					})
			// Success
			.done(function(html) {
				var parsed = $('<div/>').html(html);
				var title = parsed.find("h1, h2").filter(":first");
				var content = parsed.find("[data-redoculous-now]").html();
				if (content && !(content === handle.html())) {
					handle.html(content);
				}
				if (title) {
					title = title.text();
					document.title = titlePattern.replace("%TITLE%", title);
				}
				setTimeout(reload, 100);
			});
		} else {
			setTimeout(reload, 100);
		}
	};

	$(function() {
		reload();
	});

	function reload() {
		ajaxCall();
	}

};

$(document).ready(function() {
	$("[data-redoculous-now]").redoculousNow();
});

console.log('Loaded: redoculous-now.js');
