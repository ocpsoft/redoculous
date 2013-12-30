/* Add methods to String to get lines and count number of lines */
if (typeof String.prototype.lines != 'function') {
	String.prototype.lines = function() {
		return this.split(/\r*\n/);
	}
}
if (typeof String.prototype.lineCount != 'function') {
	String.prototype.lineCount = function() {
		return this.lines().length
				- (navigator.userAgent.indexOf("MSIE") != -1);
	}
}
if (typeof String.prototype.startsWith != 'function') {
	String.prototype.startsWith = function(str) {
		return this.slice(0, str.length) == str;
	};
}
if (typeof String.prototype.endsWith != 'function') {
	String.prototype.endsWith = function(str) {
		return this.slice(-str.length) == str;
	};
}

var getCurrentFilePath = function() {
	var search = window.location.search;
	var result = getParameterByName("path");

	if (!result.startsWith("file://")) {
		while (result.startsWith("/")) {
			result = result.substring(1);
		}
		result = "file:///" + result;
	}

	return result;
}

var getParameterByName = function(name) {
	var search = window.location.search;
	name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
	var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"), results = regex
			.exec(search);
	return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g,
			" "));
}

var nl2br = function(str, is_xhtml) {
	var breakTag = (is_xhtml || typeof is_xhtml === 'undefined') ? '' : '<br>';

	return (str + '').replace(/([^>\r\n]?)(\r\n|\n\r|\r|\n)/g, '$1' + breakTag
			+ '$2');
}

console.log('Loaded: redoculous-common.js');