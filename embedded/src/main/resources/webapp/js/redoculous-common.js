var OSName = "Unknown OS";
if (navigator.appVersion.indexOf("Win") != -1)
	OSName = "Windows";
if (navigator.appVersion.indexOf("Mac") != -1)
	OSName = "MacOS";
if (navigator.appVersion.indexOf("X11") != -1)
	OSName = "UNIX";
if (navigator.appVersion.indexOf("Linux") != -1)
	OSName = "Linux";

console.log('Detected OS: ' + OSName);

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

if (typeof String.prototype.replaceAll != 'function') {
   String.prototype.replaceAll = function (find, replace) {
      return this.replace(new RegExp(find, 'g'), replace);
   };
}


var getNearestDirectory = function(path) {
	var result = path.substring(0, path.lastIndexOf('/'));
	return result;
};

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
};

var getParameterByName = function(name) {
	var search = window.location.search;
	name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
	var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"), results = regex
			.exec(search);
	return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g,
			" "));
};

var nl2br = function(str, is_xhtml) {
	var breakTag = (is_xhtml || typeof is_xhtml === 'undefined') ? '' : '<br>';

	return (str + '').replace(/([^>\r\n]?)(\r\n|\n\r|\r|\n)/g, '$1' + breakTag
			+ '$2');
};

function getCaretPosition(ctrl) {
   var start, end;
   if (ctrl.setSelectionRange) {
       start = ctrl.selectionStart;
       end = ctrl.selectionEnd;
   } else if (document.selection && document.selection.createRange) {
       var range = document.selection.createRange();
       start = 0 - range.duplicate().moveStart('character', -100000);
       end = start + range.text.length;
   }
   return {
       start: start,
       end: end
   }
}

function getWordAtCaret(value, caret) {
   var result = /\S+$/.exec(value.slice(0, value.indexOf(' ',caret.end)));
   var lastWord = result ? result[0] : null;
   return lastWord;
}

function getCurrentLine(string, caretPos) {
   var start = caretPos.start;
   var end = caretPos.end;
   
   if(start == end && start > 0)
      start--;
   
   for (; start >= 0 && string[start] != "\n"; --start);
   for (; end < string.length && string[end] != "\n"; ++end);

   return string.substring(start + 1, end);
}

console.log('Loaded: redoculous-common.js');