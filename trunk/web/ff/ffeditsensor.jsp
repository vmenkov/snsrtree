<%@ page import="dd.web.*" %>
<%@ page import="dd.gui.*" %>
<%@ page import="dd.engine.*" %>
<%@ page import="java.io.*" %>
<% 
        FFResultsBase main=new FFResultsBase(request);
	FFSessionData r= main.sd;
	Test q = (main.id > 0 || main.id < r.maxNS) ?  r.sensors[main.id]:null;
%>

<html>
  <head>
   <link rel="icon" type="image/vnd.microsoft.icon" href="../favicon16.ico">
    <title>FF Lite: edit/replace sensor description (ver. <%=Main.version%>)</title>
  </head>
  <body>

<%
   if (main.error) {
   %>
<p class="normal">Error: <em class="errMsg"><%= main.errmsg %></em></p>
<% if (main.e != null) { %>
<p><%= main.e %></p>
<hr>
<p>Details of the exception, if you care for them:</p>

<p><small>
<pre><%= main.exceptionTrace() %></pre></small></p><%
      }
   } else if (main.id < 0) {
%>
<p>Error: sensor id not specified. How did you even get to this page?!</p>
<%
   } else {
%>
    <h1>Editing sensor No. <%=(main.id+1)%></h1>

<p>Here you can do one of the two things:
<ul>

<li> You can create a sensor description file in a text editor, and
upload it. The format is illustrated in the text box below.

<li>Or you can edit the sensor description in the text box, and upload <em>that</em>
</p>


<table border=1>
<tr> <td>
Option 1 - upload a description file:  
<form  enctype="multipart/form-data" action="ffmain.jsp" method="post">
<input type="hidden" name="id" value="<%=main.id%>">
Upload a description file: <input type="file" size="80" name="sensor">
<input type="submit" value="Upload file" />
</form>

</td></tr>
<tr><td>
<p>Option 2 - edit the sensor description:
<br>
<form  enctype="multipart/form-data" action="ffmain.jsp" method="post">
<input type="hidden" name="id" value="<%=main.id%>">
Sensor name: 
<input type="text" name="sensorname" 
       value="<%=(q==null?"Sample":q.getName())%>"> (optional; it's used for plot titles)
<br>
<textarea name="sensordata" cols="20" rows="10"><% 
if (q!=null) { %> <%= q.toString1() %> <%
} else {%>
cost: .05
0 0
.2 .6
.4 .8
1 1  <% } %>
</textarea>
<br>
<!--
<button name="sendsensor" value="1">Upload data from the box above</button>
<button name="reset" type="reset">Reset sample text</button>
-->
<input type="submit" name="sendsensor" value="Upload data from the box above">
<input name="reset" type="reset" value="Reset sensor description">
</form>

</td></tr>
<tr><td>
<div align=center><strong>Format explanation</strong></div>

<p>
The first line of the file contains the cost of the test; the remaining lines describe the ROC curve (detection rate versus false alarm rate), which runs from (0,0) to (1,1).
</p>

<p>This should be a convex curve, i.e. the segments must appear in the
order of decreasing slope.</p>

</td></tr>
</table> 

<% } %>

</body>
</html>
