$.support.cors = false;

$.fn.redoculousNow = function() {

	var handle = $(this);
	var titlePattern = "%TITLE%";

	if (window.opener) {
		if (getParameterByName(window.opener.location.search, "path") != getParameterByName(
				window.location.search, "path")) {
			window.opener.location.href = window.location.href.replace('/p/',
					'/p/edit')
		}
	}

	/* Begin SyncCursor */
	var syncCursorHighlighter = null;
	var syncCursorTimeoutId;
	var syncCursor = function() {
		window.clearTimeout(syncCursorTimeoutId);

		if (window.opener) {
			if (window.opener.followCursor
					&& window.opener.syncCursorUpdateRequired) {

				if (syncCursorHighlighter == null) {
					$("body")
							.append(
									"<div id='redoculousCursorHighlighter' style='width: 100%; height: 60px; background-color: yellow; position: absolute; opacity: 0.2;'></div>")
					syncCursorHighlighter = $('#redoculousCursorHighlighter');
				}

				syncCursorHighlighter.stop();
				syncCursorHighlighter.animate({
					top : $(document).height()
							* window.opener.syncCursorPercent
				}, 250);
				window.opener.syncCursorUpdateRequired = false;
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
			if (window.opener.followCursor
					&& window.opener.syncScrollUpdateRequired) {
				$('html, body').stop();
				$('html, body').animate(
						{
							scrollTop : $(window).height()
									* window.opener.syncScrollPercent
						}, 250);
				window.opener.syncScrollUpdateRequired = false;
			}
		}

		window.setTimeout(syncScroll, 100);
	}

	syncScroll();
	/* End SyncScroll */

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

function getParameterByName(search, name) {
	name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
	var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"), results = regex
			.exec(search);
	return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g,
			" "));
}

$(document).ready(function() {
	$("[data-redoculous-now]").redoculousNow();
});
