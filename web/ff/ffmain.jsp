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
	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
	NumberFormat msfmt = new DecimalFormat("0.###");
%>

<html>
  <head>	
   <link rel="icon" type="image/vnd.microsoft.icon" href="../favicon16.ico">
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
<tr><th colspan="<%=r.colspan%>" align=center>
Sensors
</th></tr>
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
<table><tr>
<td><form  action="ffdeletesensor.jsp" method="post">
<input type="hidden" name="id" value="<%=i%>">
<input type="submit" name="confirm_deletesensor" value="Delete">
</form></td>
<td><form  action="ffeditsensor.jsp" method="post">
<input type="hidden" name="id" value="<%=i%>">
<input type="submit" name="editsensor" value="Edit">
</form>
</td></tr></table>
</td>
<%   
}
if (i < r.maxNS) { %>
<td width="<%=r.w[i]%>%" valign="top">
<form  action="ffeditsensor.jsp" method="post">
<input type="hidden" name="id" value="<%=i%>">
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
<tr><td colspan="<%=r.colspan%>">
<input type="submit" name="compute" value="[RE]COMPUTE EXTREMAL FRONTIER">
or
<!-- input type="submit" name="scratch" value="DROP THESE SENSORS AND RESTART FROM SCRATCH" -->
<a href="index.html">DROP THESE SENSORS AND RESTART FROM SCRATCH</a>
</td></tr>
<tr><td colspan="<%=r.colspan%>">
<%
if (r.presentedFrontier==null) {
%>Please click on the "Compute" button above to compute the Extremal Frontier for your set of sensors<%
} else { 
 double msec = r.presentedFrontier.frontier.runtimeMsec();
 String cost = (msec > 10000) ? "" + (int)(0.001 * msec) + " sec" :
     msfmt.format(0.001 * msec) + " sec";

%>
<div align=center>
<strong><%=r.presentedFrontier.makeGraphTitle()%></strong><br>
Computed at <%=dateFormat.format(r.presentedFrontier.getEndTime().getTime())%>, cost =  <%= cost %>
<br>
<img src="../GraphServlet?caller=ff&stage=3&serial=<%=r.presentedFrontier.serial%>" alt="Computing extremal frontier. Please wait...">
</div>
<%
}
%>
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

