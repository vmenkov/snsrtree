<%@ page import="dd.web.*" %>
<%@ page import="dd.gui.*" %>
<%@ page import="dd.engine.*" %>
<%@ page import="java.io.*" %>
<% 
        DemoResultsBase main=new DemoResultsBase(request);
	DemoSessionData r= main.sd;
%>

<html>
  <head>
    <title>DNDO Demo: upload sensor description (ver. <%=Main.version%>)</title>
  </head>
  <body>
    <h1>DNDO Demo: Upload sensor description (ver. <%=Main.version%>)</h1>

<p>There are two ways to upload a sensor description:
<ul>

<li> You can create a sensor description file in a text editor, and
upload it. The format is illustrated in the text box below.

<li>Or you can edit the sample sensor description in the text box, and upload <em>that</em>
</p>


<table border=1>
<tr> <td>
Option 1 - upload a description file:  
<form  enctype="multipart/form-data" action="demomain.jsp" method="post">
Upload a description file: <input type="file" size="80" name="sensor">
<input type="submit" value="Upload file" />
</form>

</td></tr>
<tr><td>
<p>Option 2 - edit the sample sensor description (or just upload it as it is):
<br>
<form  enctype="multipart/form-data" action="demomain.jsp" method="post">
<textarea name="sensordata" cols="20" rows="10"><% 
if (r.q!=null && r.sensorFromTextarea) { %> <%= r.q.toString1() %> <%
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
<input name="reset" type="reset" value="Reset sample text">
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

</body>
</html>
