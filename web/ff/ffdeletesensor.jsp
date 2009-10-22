<%@ page import="dd.web.*" %>
<%@ page import="dd.gui.*" %>
<%@ page import="dd.engine.*" %>
<%@ page import="java.io.*" %>
<%@ page import="java.text.*" %>
<!-- %@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" % -->
<!-- %@ taglib prefix="my" uri="http://jakarta.apache.org/tomcat/jsp2-example-taglib"% -->

<% 
	FFResultsBase main=new FFResultsBase(request);
	FFSessionData r= main.sd;
%>

<html>
  <head>	
   <link rel="icon" type="image/vnd.microsoft.icon" href="../favicon16.ico">
   <title>Delete sensor? (FF Lite ver. <%=Main.version%>)</title>
  </head>
  <body>
    <h1>Delete sensor? (FF Lite ver. <%=Main.version%>)</h1>

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
<p>
Are you sure you want to delete sensor No. <%= main.id+1%>?
</p>

<form  action="ffmain.jsp" method="post">
<input type="hidden" name="id" value="<%=main.id%>">
<input type="submit" name="cancel" value="Cancel">
<input type="submit" name="deletesensor" value="Delete">
</form>

<%
   }
%>

<hr>
<p><small><em>
<%= main.infomsg %>
</small></em>


</body>
</html>

