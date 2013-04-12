$.support.cors = true;
$.fn.redoculous = function() {
	var handle = $(this);
	var server = "http://localhost:8080/redoculous";
	var repo = handle.attr("data-repo");
	var ref = handle.attr("data-ref");
	var path = handle.attr("data-path");
	var root = handle.attr("data-root");

	handle.hide();

	var loadDoc = function(repo, ref, path) {
		var url = server + "?repo=" + repo + "&ref=" + ref + "&path=" + path;

		$.ajax({
			url : url,
			cache : false,
			dataType : "html",
			type : "GET"
		}).fail(function(xhr, status, error) {
			alert("failed! " + status + " " + error);
		}).done(function(html) {
			var dom = $.parseHTML(html);
			handle.html(html);
			$(handle).find("a").each(function() {
				var link = $(this);
				var href = link.attr("href");
				if(href.match("/^(!?(http|www|mailto|ftp|ssh).*).*$"))
				link.click(function(event) {
					event.preventDefault();
					loadDoc(repo, ref, href);
				});
			});
			handle.show();
		});
	};

	loadDoc(repo, ref, path);

};

$(document).ready(function() {
	$("[data-redoculous]").redoculous();
});
