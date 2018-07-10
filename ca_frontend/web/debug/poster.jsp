<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>HTTP Poster</title>
	<script type="text/javascript">
		function post(){
			storeValues();
			var request = new XMLHttpRequest();
			request.onreadystatechange = function(){
				if(request.readyState == 4){
					document.getElementById("response").value = request.status+" "+request.statusText+"\n\n"+request.responseText;
			    }				
			};
			request.open(localStorage.method, localStorage.uri, true);
			if(localStorage.headers && localStorage.headers !== ""){
				var headers = localStorage.headers.split('\n');
				for(var i=0;i<headers.length;++i){
					var header = headers[i].split("=");
					if(header.length != 2){
						console.log("Ignored invalid header: "+headers[i]);
					}else{
						request.setRequestHeader(header[0],header[1]);
					}
				}
			}
			if(localStorage.body && localStorage.body !== ""){
				request.send(localStorage.body);
			}else{
				request.send();
			}
		}
		
		function loadValues(){
			if(localStorage.uri){
				document.getElementById("uri").value = localStorage.uri;
			}
			if(localStorage.method){
				document.getElementById("method").value = localStorage.method;
			}
			if(localStorage.headers){
				document.getElementById("headers").value = localStorage.headers;
			}
			if(localStorage.body){
				document.getElementById("body").value = localStorage.body;
			}
		}
		
		function storeValues(){
			localStorage.uri = document.getElementById("uri").value;
			localStorage.method = document.getElementById("method").value;
			localStorage.headers = document.getElementById("headers").value;
			localStorage.body = document.getElementById("body").value;
		}
	</script>
</head>

<body onload="loadValues()">
<button onclick="post()">Execute</button>

<p>HTTP Method</p>
<select id="method">
  <option value="POST">POST</option>
  <option value="GET">GET</option>
  <option value="DELETE">DELETE</option>
</select>

<p>HTTP URI</p>
<input id="uri" type="text" size="100"></input>

<p>HTTP Headers (e.g. HEADER=VALUE[NEW_LINE]HEADER=VALUE)</p>
<textarea rows="3" cols="100" id="headers"></textarea>

<p>HTTP Body</p>
<textarea rows="40" cols="100" id="body"></textarea>

<p>Response</p>
<textarea rows="40" cols="100" id="response"></textarea>
</body>
</html>