<!-- %@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" % -->
<!-- %@ taglib prefix="my" uri="http://jakarta.apache.org/tomcat/jsp2-example-taglib"% -->

<html>
  <head>
    <title>DD test</title>
  </head>
  <body>
    <h1>DD test</h1>
<table border=1>
<tr> <td colspan=2>
<% if (request.getParameter("sensor")!=null) {
} else {
%>
<form>
Sensor description file: <input type="file" name="sensor">
<input type="submit" value="Upload" />
</form>
<% } %>
</td></tr>
<tr> <td colspan=2><form>
Budget (between 0.0 and 1.1): <input type="text" name="budget"/>
<input type="submit" value="Upload" />
</form></td></tr>
<tr>
<td>buttons</td>
<td>graph<br>
<a href="c1.svg">
<embed src="c1.svg"></a><br>
<a href="/examples/jsp/jsp2/jspx/textRotate.jspx?name=Li">
<embed src="/examples/jsp/jsp2/jspx/textRotate.jspx?name=Li"></a>
</td>
</tr>
<tr>
<td colspan=2>
Explanation goes here
</td></tr>
</table> 

</body>
</html>
