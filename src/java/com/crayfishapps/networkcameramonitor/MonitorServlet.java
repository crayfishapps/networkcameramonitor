package com.crayfishapps.networkcameramonitor;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author crayfishapps
 */
public class MonitorServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        
        String userString;
        
        if (user == null) {
            userString = "<p>Welcome!</p>";
            userString += "<p><a class=\"greenbutton\" href=\"" + userService.createLoginURL("/monitor") + "\">Sign in here</a></p>";
        }
        else {
            userString = "<p>Welcome, " + user.getNickname() + "</p>";
            userString += "<p><a class=\"greenbutton\" href=\"" + userService.createLogoutURL("/") + "\">Sign out here</a></p>";
        }
        
        PrintWriter out = response.getWriter();
        try {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Camera Monitoring</title>");
            out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"main.css\"/>");
            out.println("</head>");
            out.println("<body>");
            out.println(userString);
            out.println("<p><a class=\"greenbutton\" href=register>Registration site</a></p>");
            
            if (user != null) {
                
                String currentUser = user.getUserId();               
                
                // list of existing entries
                DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
                Filter userFilter = new FilterPredicate("userID", FilterOperator.EQUAL, currentUser);
                Query query = new Query("Camera");
                query.setFilter(userFilter);
                query.addSort("status");
                List<Entity> members = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
                try {
                    if (!members.isEmpty()) {
                        int rawCount = 0;
                        
                        out.println("<table>");
                        out.println("<tr><th colspan=\"2\">Information</th><th colspan=\"2\">Status</th><th colspan=\"2\">Statistics</th></tr>");
                        for (Entity memberEntity : members) {
                            rawCount++;
                            
                            String serialNumber = memberEntity.getProperty("serialnumber").toString();
                            String cameraType = memberEntity.getProperty("type").toString();
                            String cameraLocation = memberEntity.getProperty("location").toString();
                            
                            String cameraStatus = memberEntity.getProperty("status").toString();
                            
                            String storageSD = memberEntity.getProperty("storage_sd").toString();
                            String storageNAS = memberEntity.getProperty("storage_nas").toString();
                            String boot = memberEntity.getProperty("boot").toString();
                            String tampering = memberEntity.getProperty("tampering").toString();
                            
                            String streaming = memberEntity.getProperty("streaming").toString();
                            String motion = memberEntity.getProperty("motion").toString();
                            String recording = memberEntity.getProperty("recording").toString();
                            
                            out.print("<tr");
                            if (rawCount % 2 == 0) {
                                out.print(" class=\"alt\"");
                            }
                            out.println(">");
                            
                            out.println("<td align=\"right\">");
                            out.println("Serial number:<br>");
                            out.println("Camera type:<br>");
                            out.println("Camera location:<br>");
                            out.println("</td>");
                            
                            out.println("<td>");
                            out.println(serialNumber + "<br>");
                            out.println(cameraType + "<br>");
                            out.println(cameraLocation + "<br>");
                            out.println("</td>");
                            
                            out.println("<td align=\"right\">");
                            out.println("Status:<br>");
                            out.println("SD card:<br>");
                            out.println("NAS:<br>");
                            out.println("Last boot:<br>");
                            out.println("Last tampering:<br>");
                            out.println("</td>");
                            
                            out.println("<td>");
                            out.println(cameraStatus + "<br>");
                            out.println(storageSD + "<br>");
                            out.println(storageNAS + "<br>");
                            out.println(boot + "<br>");
                            out.println(tampering + "<br>");
                            out.println("</td>");
                            
                            out.println("<td align=\"right\">");
                            out.println("Streaming:<br>");
                            out.println("Motion detection:<br>");
                            out.println("Recording:<br>");
                            out.println("</td>");
                            
                            out.println("<td>");
                            out.println(streaming + "<br>");
                            out.println(motion + "<br>");
                            out.println(recording + "<br>");
                            out.println("</td>");
                            
                            out.println("</tr>");
                            
                        }
                        out.println("</table>");
                    }
                }
                catch (Exception e) {
                    out.println("<p>Error: " + e.getMessage() + "</p><p></p>");
                }
            }            
            
            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
  
}
