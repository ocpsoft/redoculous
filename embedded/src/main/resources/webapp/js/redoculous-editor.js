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

var syncWord;
var syncWords;

$.fn.redoculousEditor = function() {

	var handle = $(this);
	
	editor.keyup(function () {
	   syncWord = getWordAtCaret(this.value, getCaretPosition(this));
	   syncWords = getCurrentLine(this.value, getCaretPosition(this));
	   console.log(syncWords);
	});
   
   editor.click(function () {
      syncWord = getWordAtCaret(this.value, getCaretPosition(this));
      syncWords = getCurrentLine(this.value, getCaretPosition(this));
      console.log(syncWords);
   });

	var getServerURL = function() {
		var url = window.location.href;
		var arr = url.split("/");
		var result = arr[0] + "//" + arr[2];
		return result;
	}

	var getAppURL = function() {
		return getServerURL() + "/p";
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
			var documentHeight = $(window).height();
			var scrollTop = editor.scrollTop();
			var newScrollPercent = scrollTop / (contentHeight - visibleHeight)

			if (false)
				$("#stats").html(
						"visible height: " + visibleHeight + "<br/>"
								+ "document height: " + documentHeight
								+ "<br/>" + "content height: " + contentHeight
								+ "<br/>" + "scroll top: " + scrollTop
								+ "<br/>" + "scroll percent: "
								+ newScrollPercent + "<br/>");

			if (newCursorPercent > newScrollPercent
					&& contentHeight == documentHeight)
				newScrollPercent = newCursorPercent;

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
			$("#follow").text("Viewer unlinked");
			follow = false;
		} else {
			$("#follow").addClass("btn-primary");
			$("#follow").text("Viewer linked");
			follow = true;
			syncCursorUpdateRequired = true;
			syncScrollUpdateRequired = true;
		}
	});

	$("#openViewer").click(function() {
		if (!isViewerOpen()) {
			window.open(getAppURL() + "?path=" + getCurrentFilePath());
		}
	});

	$("#open").click(function() {
		var path = getCurrentFilePath();
		if (path.startsWith("file://"))
			path = path.substring(7);

		var frame = $('<iframe />', {
			src : getServerURL() + "/fs" + getNearestDirectory(path),
			id : 'openFileFrame',
			scrolling : 'vertical',
			frameborder : 0
		});
		coverEditor(true);
		frame.appendTo('body');

		var configureFrameTimeoutId;
		var escape = function(e) {
			if (e.keyCode == 27) {
				frame.remove();
				coverEditor(false);
				$(document).unbind("keyup", escape);
				window.clearTimeout(configureFrameTimeoutId);
			}
		}

		$(document).keyup(escape);

		var configureFrame = function() {
			window.clearTimeout(configureFrameTimeoutId);
			frame.contents().keyup(escape);
			frame.contents().find("a").each(function() {
				var href = $(this).attr("href");
				if (!href.endsWith("/")) {
					$(this).click(function() {
						window.location.search = "path=" + href.substring(3);
					});
				}
			});
			configureFrameTimeoutId = window.setTimeout(configureFrame, 50);
		};
		configureFrameTimeoutId = window.setTimeout(configureFrame, 50);
	});

	var coverEditor = function(covered) {
		if (covered)
			$("#editorCover").css("z-index", "2");
		else
			$("#editorCover").css("z-index", "-1");
	};

	load();
	syncScroll();

};

var isViewerOpen = function() {
	return viewer && !viewer.closed;
}

var onViewerClosed = function() {
	$("#openViewer").removeClass("hidden");
	$("#follow").addClass("hidden");
}

var getViewer = function() {
	return viewer;
}

var setViewer = function(obj) {
	viewer = obj;
	$("#openViewer").addClass("hidden");
	$("#follow").removeClass("hidden");
}

$('#bookmarklet').tooltip();

$(document).ready(function() {
	$("[data-redoculous-editor]").redoculousEditor();
});

console.log('Loaded: redoculous-editor.js');
