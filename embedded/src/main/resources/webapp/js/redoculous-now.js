$.support.cors = false;

$.fn.redoculousNow = function() {

	if (window.opener) {
		if (getParameterByName(window.opener.location.search, "path") != getParameterByName(
				window.location.search, "path")) {
			window.opener.location.href = window.location.href.replace('/p/',
					'/p/edit')
		}
	}

	var handle = $(this);

	var titlePattern = "%TITLE%";

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

						setTimeout(reload, 1000);
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
				setTimeout(reload, 500);
			});
		} else {
			setTimeout(reload, 1000);
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
