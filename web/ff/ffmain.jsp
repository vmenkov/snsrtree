<%@ page import="dd.web.*" %>
<%@ page import="dd.gui.*" %>
<%@ page import="dd.engine.*" %>
<%@ page import="java.io.*" %>
<%@ page import="java.text.*" %>
<!-- %@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" % -->
<!-- %@ taglib prefix="my" uri="http://jakarta.apache.org/tomcat/jsp2-example-taglib"% -->

<% 
	FFMain main=new FFMain(request);
	FFSessionData r= main.sd;
%>

<html>
  <head>	
   <title>DNDO Frontier Finder Lite (ver. <%=Main.version%>)</title>
  </head>
  <body>
    <h1>DNDO Frontier Finder Lite (ver. <%=Main.version%>)</h1>

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
   } else {
%>
<table border=1>
<tr>
<%
int i=0;
for(; i<r.maxNS && r.sensors[i]!=null; i++) { %>
<td width="<%=r.w[i]%>%" valign="top">
<div align="center">
Sensor No. <%=(i+1)%> (<%=r.sensors[i].getName()%>) <br>
ROC curve (shifted by test cost=<%= r.sensors[i].getCost()%>)<br>
</div>
<img src="../GraphServlet?caller=ff&stage=2&serial=<%=r.presentedSensors[i].serial%>" alt="Loading shifted ROC curve no. <%=i+1%>...">
<br>
</td>
<%   
}
if (i < r.maxNS) { %>
<td width="<%=r.w[i]%>%" valign="top">
<form action="ffmain.jsp" method="post">
<input type="submit" name="addsensor" value="Add another sensor">
</form>
</td>
<%
}%>
</tr>
<form action="ffmain.jsp" method="post">
<!-- input hidden name="cmd" value="compute" -->
<tr>
<%
for(i=0;i<r.maxNS &&  r.sensors[i]!=null; i++) { %>
<td width="<%=r.w[i]%>%" valign="top" align=right>
<input align=right size=3 name="multi<%=i%>" value="<%=r.sensors[i].getNCopies()%>"> copies
</td>
<%   
}%>
</tr>
<tr><td>
<input type="submit" name="compute" value="[RE]COMPUTE EXTREMAL FRONTIER">
</td>
</tr>
</form>
</table>
<%
   }
%>

<hr>
<p><small><em>
<%= main.infomsg %>
</small></em>


</body>
</html>

