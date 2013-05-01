$.support.cors = false;
$.fn.redoculousNow = function() {
	var handle = $(this);

	var titlePattern = "%TITLE%";

	var ajaxCall = function() {
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
				})
		// Success
		.done(function(html) {
			var parsed = $('<div/>').html(html);
			var title = parsed.find("h1, h2").filter(":first");
			var content = parsed.find("[data-redoculous-now]").html();
			if (content) {
				handle.html(content);
			}
			if (title) {
				title = title.text();
				document.title = titlePattern.replace("%TITLE%", title);
			}

		});
	};

	$(function() {
		reload();
	});

	function reload() {
		setTimeout(reload, 500);
		ajaxCall();
	}

};

$(document).ready(function() {
	$("[data-redoculous-now]").redoculousNow();
});
