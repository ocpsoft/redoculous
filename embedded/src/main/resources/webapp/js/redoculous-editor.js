$.support.cors = true;

/* Prepare the editor data */
var editor = $("#editor");
var value = editor.val();

var follow = true;
var initialized = false;
var syncTimeoutId;
var viewerUpdateRequired = true;
var viewer;

var syncCursorPercent;
var syncCursorUpdateRequired = false;
var syncScrollUpdateRequired = false;
var syncScrollPercent;
var syncScrollTimeoutId;

$.fn.redoculousEditor = function() {

	var handle = $(this);

	var getAppURL = function() {
		var url = window.location.href;
		var arr = url.split("/");
		var result = arr[0] + "//" + arr[2];
		return result + "/p";
	}

	var getDataURL = function() {
		return getAppURL() + "/edit";
	}

	/* Editor methods */
	editor.keyup(function() {
		window.clearTimeout(syncTimeoutId);
		syncTimeoutId = window.setTimeout(sync, 250);
	});

	var sync = function() {
		window.clearTimeout(syncTimeoutId);

		if (value != editor.val()) {
			if (!load()) {
				value = editor.val();
				save();
			}
		}
	}

	var load = function() {
		var url = getDataURL() + "?path=" + getCurrentFilePath();
		var loaded = false;

		console.log("Loading: " + url);

		$
				.ajax({
					url : url,
					async : false,
					cache : false,
					dataType : "text",
					type : "GET",
					timeout : 5000
				})
				// Failure
				.fail(
						function(xhr, status, error) {
							console.log("Failed loading: " + url + " - ["
									+ status + "-" + error + "]");
						})
				// Success
				.done(
						function(data) {
							if (value != data) {
								if (value != "") {
									var confirm = window
											.confirm("The document has changed on"
													+ " the filesystem. Load changes?");
									if (confirm) {
										editor.val(data);
										loaded = true;
									}
								} else {
									editor.val(data);
									loaded = true;
								}

								if (loaded) {
									value = data;
									viewerUpdateRequired = true;
								}
							}
							console.log("Loaded: " + url);
						});

		initialized = true;
		return loaded;
	};

	var save = function() {
		var url = getDataURL() + "?path=" + getCurrentFilePath();
		viewerUpdateRequired = true;

		console.log("Saving: " + url);

		$.ajax({
			url : url,
			cache : false,
			data : "content=" + encodeURIComponent(editor.val()),
			dataType : "text",
			type : "POST"
		})
		// Failure
		.fail(
				function(xhr, status, error) {
					console.log("Failed saving: " + url + " - [" + status + "-"
							+ error + "]");
				})
		// Success
		.done(function(html) {
			console.log("Saved: " + url);
		});
	};

	var syncScroll = function() {
		window.clearTimeout(syncScrollTimeoutId);
		if (follow) {

			var totalLines = editor.val().split("\n").length;
			var currentLine = editor.val().substr(0, editor[0].selectionStart)
					.split("\n").length;

			if (currentLine > 0)
				currentLine = currentLine - 1;

			if (totalLines == 0)
				totalLines = 1;

			var newCursorPercent = (currentLine * 100) / (totalLines * 100);
			if (newCursorPercent != syncCursorPercent) {
				syncCursorPercent = newCursorPercent;
				syncCursorUpdateRequired = true;
			}

			// visible height + scroll top = content height (when scrolled down)
			// visible height + scroll top = visible height (when scrolled up)
			var visibleHeight = editor.height();
			var contentHeight = editor[0].scrollHeight;
			var scrollTop = editor.scrollTop();
			var newScrollPercent = scrollTop / (contentHeight - visibleHeight)

			/*
			 * $("#stats").html( "visible height: " + visibleHeight + "<br/>" +
			 * "content height: " + contentHeight + "<br/>" + "scroll top: " +
			 * scrollTop + "<br/>" + "scroll percent: " + newScrollPercent + "<br/>" );
			 */

			if (newScrollPercent != syncScrollPercent) {
				syncScrollPercent = newScrollPercent;
				syncScrollUpdateRequired = true;
			}
		}
		syncScrollTimeoutId = window.setTimeout(syncScroll, 100);
	}

	$("#follow").click(function() {
		if (follow) {
			$("#follow").removeClass("btn-primary");
			$("#follow").text("Link with viewer");
			follow = false;
		} else {
			$("#follow").addClass("btn-primary");
			$("#follow").text("Unlink from viewer");
			follow = true;
			syncCursorUpdateRequired = true;
			syncScrollUpdateRequired = true;
		}
	});

	$("#openViewer").click(
			function() {
				if (!isViewerOpen()) {
					setViewerOpen(window.open(getAppURL() + "?path="
							+ getCurrentFilePath()));
				}
			});

	var isViewerOpen = function() {
		return viewer && !viewer.closed;
	}

	load();
	syncScroll();

};

var onViewerClosed = function() {
	$("#openViewer").removeClass("hidden");
	$("#follow").addClass("hidden");
}

var setViewerOpen = function(obj) {
	viewer = obj;
	$("#openViewer").addClass("hidden");
	$("#follow").removeClass("hidden");
}

$(document).ready(function() {
	$("[data-redoculous-editor]").redoculousEditor();
});

console.log('Loaded: redoculous-editor.js');
