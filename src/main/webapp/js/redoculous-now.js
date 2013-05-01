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

					setTimeout(reload, 1000);
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
			setTimeout(reload, 500);
		});
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
