<!-- %@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" % -->
<!-- %@ taglib prefix="my" uri="http://jakarta.apache.org/tomcat/jsp2-example-taglib"% -->
<%@ page import="dd.engine.Main" %>

<html>
  <head>
    <title>DNDO Demo: upload sensor file (ver. <%=Main.version%>)</title>
  </head>
  <body>
    <h1>DNDO Demo: Upload sensor file (ver. <%=Main.version%>)</h1>
<table border=1>
<tr> <td>
<form  enctype="multipart/form-data" action="mainweb.jsp" method="post">
Sensor description file: <input type="file" size="80" name="sensor">
<input type="submit" value="Upload" />
</form>

</td></tr>
<tr><td>
<p>Sample sensor description file format:
<pre>
cost: .05
0 0
.2 .6
.4 .8
1 1
</pre>
The first line of the file contains the cost of the test; the remaining line describe the (B,G) curve, which runs from (0,0) to (1,1).
<p>

</td></tr>
</table> 

</body>
</html>
