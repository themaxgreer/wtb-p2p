function wtbConnect(server) {
	var createDiv = function (className, textContent) {
		var div = document.createElement('div');
		div.className = className;
		div.textContent = textContent;
		return div;
	}

	var createMeter = function (file, slug) {
		var div = document.createElement('div');
		div.className = 'meter';

		var span = document.createElement('span');
		div.appendChild(span);
		var size = file.filesize;

		var intervalTimer = window.setInterval(function () {
			var xhr = new XMLHttpRequest();
			xhr.onreadystatechange = function (evt) {
				if (xhr.readyState === 4 && xhr.status === 200) {
					var progress = JSON.parse(xhr.responseText);
					progress = 100 * progress / size;
					updateMeter(div, Math.floor(progress) + '%');
					if (progress >= 100) {
						window.clearInterval(intervalTimer);
					}
				}
			}
			xhr.open("GET", server + "/transfer/" + slug);
			xhr.send(null);
		}, 100);

		return div;
	}

	var updateMeter = function (meter, value) {
		meter.firstChild.style.width = value;
		meter.firstChild.textContent = value;
		return meter;
	}

	var makeTransfer = function(file, slug) {
		var li = document.createElement('li');
		li.className = 'new-transfer';

		li.appendChild(createDiv('title', file.title));
		li.appendChild(document.createTextNode(' '));

		var time = document.createElement('div'); time.className = 'time';
		time.appendChild(createDiv('eta', file.eta));
		time.appendChild(document.createTextNode(' '));
		time.appendChild(createMeter(file, slug));

		li.appendChild(time);

		return li;
	}
	
	var formatSize = function (size) {
		var units = ["B", "kB", "MB", "GB", "TB"];
		var power = 0;
		while (size > 1024) {
			size = Math.floor(size / 1024);
			power = power + 1;
		}
		return size + " " + units[power];
	}

	var makeSearchResult = function (result) {
		var li = document.createElement('li');
		li.className = 'new-result';

		li.appendChild(createDiv('title', result.title));
		li.appendChild(document.createTextNode(' '));

		var info = document.createElement('div'); info.className = 'info';
		info.appendChild(createDiv('filesize', formatSize(result.filesize)));

		var seeds = result.sources.length;
		seeds = seeds == 1 ? seeds + " seed" : seeds + " seeds";
		info.appendChild(createDiv('seeds', seeds));
		info.appendChild(createDiv('eta', result.eta));
		li.appendChild(info);
		li.appendChild(document.createTextNode(' '));
		
		var spacer = document.createElement('span'); spacer.className = 'spacer';
		li.appendChild(spacer);

		li.onclick = function () {
			download(result, function (slug) {
				var transfers = document.getElementById('transfers');
				transfers.appendChild(makeTransfer(result, slug));
			});
		};
		
		return li;
	}

	var search = function (query, callback) {
		var xhr = new XMLHttpRequest();
		xhr.onreadystatechange = function (evt) {
			if (xhr.readyState === 4 && xhr.status === 200) {
				callback(JSON.parse(xhr.responseText));
			}
		}
		query = encodeURIComponent(query);
		xhr.open("GET", server + "/search?q=" + query + "&h=6");
		xhr.send(null);
	}
	
	var download = function (file, callback) {
		var xhr = new XMLHttpRequest();
		xhr.onreadystatechange = function (evt) {
			if (xhr.readyState === 4 && xhr.status === 200) {
				callback(JSON.parse(xhr.responseText));
			}
		}
		xhr.open("POST", server + "/transfer/");
		xhr.setRequestHeader("content-type", "application/x-www-form-urlencoded");	
		xhr.send("title=" + file.title + "&source=" +  file.sources[0] + "&hash=" + file.SHA512Hash);
	}

	var getResults = function(uuid, callback) {
		var xhr = new XMLHttpRequest();
		xhr.onreadystatechange = function (evt) {
			if (xhr.readyState === 4 && xhr.status === 200) {
				callback(JSON.parse(xhr.responseText));
			}
		}
		xhr.open("GET", server + "/results/" + uuid);
		xhr.send(null);
	}
	
	return {
		"makeSearchResult": makeSearchResult,
		"getResults": getResults,
		"search": search
	}
}
