<%@ page import="dd.web.*" %>
<%@ page import="dd.gui.*" %>
<%@ page import="dd.engine.*" %>
<%@ page import="java.io.*" %>
<%@ page import="java.text.*" %>
<!-- %@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" % -->
<!-- %@ taglib prefix="my" uri="http://jakarta.apache.org/tomcat/jsp2-example-taglib"% -->

<% 
        Mainweb main=new Mainweb(request);
	DemoSessionData r= main.sd;
%>

<html>
  <head>	
   <title>DNDO Mixed Policies Demo (ver. <%=Main.version%>)</title>
  </head>
  <body>
    <h1>DNDO Mixed Policies Demo (ver. <%=Main.version%>)</h1>

<%
   if (main.error) {
   %>
<p class="normal">Error: <em class="errMsg"><%= main.errmsg %></em></p>
<p><%= main.e %>
<pre><%= main.exceptionTrace() %></pre></p><%
   }

    if (r.q==null) {
%>
<p>No valid sensor data supplied. Please go to the <a href="sensorForm.jsp">sensor upload form</a> to upload a sensor description.
</p>
<%
} else {
%>

<table border=1>
<tr> <td colspan=2>
Sensor loaded: <%= r.sensorFileName %>  (<a href="sensorForm.jsp">Change sensor</a>)
</td></tr>
<!-- tr> <td colspan=2>
<form  action="mainweb.jsp" method="post">
Expected proportion of "bad" objects (pi, between 0.0 and 1.0):
 <input name="pi" value="<%=r.context.pi%>">
 <input type="submit" name="updatepi" value="Update graph">
</td></tr -->

<tr> <td colspan=2><form action="mainweb.jsp" method="post">
Budget (between 0.0 and 1.1): <input type="text" name="budget"
<value="<%= (r.budget != null) ? r.budget.toString(): ""%>">
<input type="submit" value="Enter">
</form></td></tr>
<tr>
<td valign="top">
<% String bgc[] = {"","","",""};
   bgc[r.stage] = "bgcolor=\"#D09000\"";
%>
<form  action="mainweb.jsp" method="post"><table>
       <tr><td>Show ...</td?</tr>
       <tr><td <%=bgc[1]%>>	
       <button name="stage" value="1">1. Non-mixed policies only</button>
       </td></tr>
       <tr><td <%=bgc[2]%>>
       <button name="stage" value="2">2. Mixed action</button>
       </td></tr>
       <tr><td <%=bgc[3]%>>
       <button name="stage" value="3">3. Fully randomized deceptive strategy</button>
       </td></tr>
</table></form>
</td>
<td>
<% if (r.presented==null) { %> No data generated! <%
} else { %>
<img src="GraphServlet?stage=<%=r.stage%>&serial=<%=r.presented.serial%>" alt="Loading cost/detection rate plot, stage <%=r.stage%>...">
<% } %>
</td>
</tr>
<tr><td colspan=2>
<p>stage=<%=r.stage%>:</p>
<p>
<% if (r.stage==1) {%>
This plot shows average per-item costs and detection rates of
"non-mixed" (deterministic) policies, each of which always associates
a particular action (I[nspect] or R[release]) with a each output
channel of the sensor. The no-test I[nspect] and R[elease] polices are shown as well.

<%} else if (r.stage==2) {%>

This plot shows average per-item costs and detection rates of the following policies:
<ul>

<li>"non-mixed" (deterministic) policies, each of which always associates
a particular action (I[nspect] or R[elease]) with a each output
channel of the sensor 

<li>"mixed action" policies, which involve always performing a test
with the sensor, always associating I[nspect] with some output and
R[elease] with others, while R or I is chosen probabilistically on one
of the channels.

<li>no-test I[nspect] and R[elease] polices
</ul>

<%} else if (r.stage==3) {%>

The red curve shows average per-item costs and detection rates for the
"extremal frontier" policies based on the specified sensor and the
no-test I[nspect] and R[elease] polices. They include mixed policies that
may perform the test only in certain percentage of randomly selected tests.
</p>

<p>
The green curve, is shown, is from stage 2. 

<%}%>

</td></tr>
<tr><td colspan=2><%=  r.budgetMessage() %>
</td></tr>
</table> 

<%}%>

<hr>
<p><small><em>
<%= main.infomsg %>
</small></em>


</body>
</html>
