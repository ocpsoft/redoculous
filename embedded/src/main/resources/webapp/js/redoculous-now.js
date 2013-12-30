$.support.cors = false;

var connected = false;

$.fn.redoculousNow = function() {

	var handle = $(this);
	var editor = window.opener;
	var titlePattern = "%TITLE%";

	var connectToParent = function() {
		try {
			if (editor) {
				if (!editor.isViewerOpen()) {
					if (!connected) {
						// if editor has just opened this viewer
						if (editor.getCurrentFilePath() != getCurrentFilePath()
								&& getCurrentFilePath() != "") {
							window.location.search = "?path="
									+ editor.getCurrentFilePath();
						}
					} else {
						// if editor has just changed URLs
						if (editor.getCurrentFilePath() != getCurrentFilePath()) {
							window.location.search = "?path="
									+ editor.getCurrentFilePath();
						}
					}
				} else if (editor.isViewerOpen()) {
					if (!connected) {
						// if viewer has changed URL

						if (editor.getCurrentFilePath() != getCurrentFilePath()
								&& editor.getCurrentFilePath() != "") {
							editor.location.search = "?path="
									+ getCurrentFilePath();
						}
					}
				}

				editor.setViewer(window);
				connected = true;

				window.setTimeout(connectToParent, 1000);
			}
		} catch (err) {
			window.setTimeout(connectToParent, 200);
		}
	};

	connectToParent();

	/* Begin SyncCursor */
	var syncCursorHighlighter = null;
	var syncCursorTimeoutId;
	var syncCursor = function() {
		window.clearTimeout(syncCursorTimeoutId);

		if (editor) {
			if (editor.follow && editor.syncCursorUpdateRequired) {

				if (syncCursorHighlighter == null) {
					$("html")
							.append(
									"<div id='redoculousCursorHighlighter' style='width: 100%; height: 100px; background-color: yellow; position: absolute; left: 0; opacity: 0.2;'></div>")
					syncCursorHighlighter = $('#redoculousCursorHighlighter');
				}

				syncCursorHighlighter.stop();
				syncCursorHighlighter.animate({
					top : ($("body").height()) * editor.syncCursorPercent + 25
				}, 250);
				editor.syncCursorUpdateRequired = false;
			}
			if (!editor.follow && syncCursorHighlighter) {
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

		if (editor) {
			if (editor.follow && editor.syncScrollUpdateRequired) {

				var newScrollTop = ($(document).height() - $(window).height())
						* editor.syncScrollPercent;

				$('html, body').stop();
				$('html, body').animate({
					scrollTop : newScrollTop
				}, 250);
				editor.syncScrollUpdateRequired = false;
			}
		}

		window.setTimeout(syncScroll, 100);
	}

	syncScroll();
	/* End SyncScroll */

	/* Unload Sync */
	$(window).unload(function() {
		if (editor) {
			editor.onViewerClosed();
		}
	});

	var ajaxCall = function() {

		if (!editor || editor.viewerUpdateRequired) {

			if (editor)
				editor.viewerUpdateRequired = false;

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
