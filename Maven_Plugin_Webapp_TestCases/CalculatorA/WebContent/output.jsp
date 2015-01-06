<%@ page import="com.calculatora.*" %>
<%@ page import="com.calculatord.CalculatorImpl" %>

<HTML>
    <HEAD>
    </HEAD>
    <BODY>
    	<h1>Output File</h1>
    	The sum is:
                <% 
                /*out.println(Integer.parseInt(request.getParameter("option1")) + 
                    Integer.parseInt(request.getParameter("option2")));*/
    			int value1=Integer.parseInt(request.getParameter("option1"));
    			int value2=Integer.parseInt(request.getParameter("option2"));
    			out.println("Executed till here");
    			CalculatorImpl cal=new CalculatorImpl();
    			cal.addition(value1, value2);
                %>
    </BODY>
</HTML>