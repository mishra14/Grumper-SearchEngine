package test.edu.upenn.cis455;


import java.io.*;
import static org.easymock.EasyMock.replay;
import javax.servlet.http.*;
import junit.framework.TestCase;
import static org.easymock.EasyMock.expect;
import org.easymock.EasyMock;
import org.junit.Test;
import edu.upenn.cis455.servlet.XPathServlet;

/**
 * This class is used to test the servlet by mocking the calls to request and response objects
 * 
 * @author cis455
 *
 */
public class ServletTest extends TestCase
{
	/**
	 * This method tests the case when a success html is expected
	 * @throws Exception
	 */
    @Test
    public void testServletSuccess() throws Exception 
    {
        HttpServletRequest request = EasyMock.mock(HttpServletRequest.class);       
        HttpServletResponse response = EasyMock.mock(HttpServletResponse.class);    

        expect(request.getParameter("url")).andReturn("http://www.w3schools.com/xml/note.xml");
        expect(request.getParameter("xPath")).andReturn("/note");
        replay(request);
        PrintWriter writer = new PrintWriter("result.txt");
        expect(response.getWriter()).andReturn(writer);
        //expect(response.flushBuffer());
        response.flushBuffer();
        EasyMock.expectLastCall();
        replay(response);

        new XPathServlet().doPost(request, response);

        writer.flush();
        writer.close();
        
        FileReader fileReader = new FileReader("result.txt");
        BufferedReader reader = new BufferedReader(fileReader);
        StringBuilder fileString = new StringBuilder();
        String line;
        while((line = reader.readLine())!=null)
        {
        	fileString.append(line);
        }
        reader.close();
        fileReader.close();
        System.out.println(fileString.toString());
        assertTrue(fileString.toString().equals("/note:Success"));
    }
    
    /**
     * This method tests the case when a failure html message is expected
     * @throws Exception
     */
    
    @Test
    public void testServletFaliure() throws Exception 
    {
        HttpServletRequest request = EasyMock.mock(HttpServletRequest.class);       
        HttpServletResponse response = EasyMock.mock(HttpServletResponse.class);    

        expect(request.getParameter("url")).andReturn("http://www.w3schools.com/xml/note.xml");
        expect(request.getParameter("xPath")).andReturn("/noted");
        replay(request);
        PrintWriter writer = new PrintWriter("result.txt");
        expect(response.getWriter()).andReturn(writer);
        //expect(response.flushBuffer());
        response.flushBuffer();
        EasyMock.expectLastCall();
        replay(response);

        new XPathServlet().doPost(request, response);

        writer.flush();
        writer.close();
        
        FileReader fileReader = new FileReader("result.txt");
        BufferedReader reader = new BufferedReader(fileReader);
        StringBuilder fileString = new StringBuilder();
        String line;
        while((line = reader.readLine())!=null)
        {
        	fileString.append(line);
        }
        reader.close();
        fileReader.close();
        System.out.println(fileString.toString());
        assertTrue(fileString.toString().equals("/noted:Faliure"));
    }
}