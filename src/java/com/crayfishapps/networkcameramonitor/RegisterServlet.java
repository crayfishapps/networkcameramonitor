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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author crayfishapps
 */
public class RegisterServlet extends HttpServlet {
    
    private DatastoreService datastore;

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
        datastore = DatastoreServiceFactory.getDatastoreService();
        
        String userString;
        
        if (user == null) {
            userString = "<p>Welcome!</p>";
            userString += "<p><a class=\"greenbutton\" href=\"" + userService.createLoginURL("/register") + "\">Sign in here</a></p>";
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
            out.println("<title>Camera Registration</title>");
            out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"main.css\"/>");
            out.println("</head>");
            out.println("<body>");
            out.println(userString);
            out.println("<p><a class=\"greenbutton\" href=monitor>Monitoring site</a></p>");
            
            if (user != null) {
                
                String currentUser = user.getUserId();
                
                // Add new entry to datastore or remove entry
                Enumeration<String> parameterNames = request.getParameterNames();
                if (parameterNames.hasMoreElements()) {
                    String serialNumber = request.getParameter("serial_number");
                    String cameraType = request.getParameter("camera_type");
                    String cameraLocation = request.getParameter("camera_location");
                    String action = request.getParameter("action");
                    
                    serialNumber = serialNumber.trim().toLowerCase();
                    serialNumber = serialNumber.replaceAll(":", "");
                    serialNumber = serialNumber.replaceAll("-", "");

                    if (action != null) {
                        removeItem(serialNumber);
                    }
                    else {
                        if ((serialNumber.length() != 12) || (!serialNumber.matches("^[a-f0-9]*$"))){
                            out.println("<p><div class=\"error\">The format of the serial number is incorrect!</div></p>");
                        }
                        else {
                            if(isSerialNumberRegistered(serialNumber)) {
                                out.println("<p><div class=\"error\">The serial number is already registered!</div></p>");
                            }
                            else {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date date = new Date();
                                String dateString = dateFormat.format(date);

                                Entity member = new Entity("Camera");
                                
                                // main parameters
                                member.setProperty("userID", user.getUserId());
                                member.setProperty("userMail", user.getEmail());
                                member.setProperty("serialnumber", serialNumber);
                                
                                // additional informatin added by user
                                member.setProperty("type", cameraType);
                                member.setProperty("location", cameraLocation);
                                
                                // heartbeat status
                                member.setProperty("contact", dateString);
                                member.setProperty("status", "OK");
                                
                                // status
                                member.setProperty("storage_sd", "unknown");
                                member.setProperty("storage_nas", "unknown");
                                member.setProperty("boot", "");
                                member.setProperty("tampering", "");
                                
                                // camera usage - active or inactive
                                member.setProperty("streaming", "");
                                member.setProperty("motion", "");
                                member.setProperty("recording", "");
                                
                                datastore.put(member);

                                out.println("<p><div class=\"success\">The " + cameraType + " with serial number " + serialNumber + " has been added.</div></p>");
                            }
                        }
                    }
                }
                
                // Form to add a new entry
                out.println("<form action=\"/register\" method=\"get\">");
                out.println("<div><fieldset>");
                out.println("<label for=\"serial_number\">Serial number: <em>required</em></label>");
                out.println("<input id=\"serial_number\" type=\"text\" name=\"serial_number\" class=\"input\"><br>");
                out.println("<label for=\"camera_type\">Camera type: </label>");
                out.println("<input id=\"camera_type\" type=\"text\" name=\"camera_type\" class=\"input\"><br>");
                out.println("<label for=\"camera_location\">Camera location: </label>");
                out.println("<input id=\"camera_location\" type=\"text\" name=\"camera_location\" class=\"input\"><br>");               
                out.println("<label>&nbsp;</label><input class=\"greenbutton\" type=\"submit\" value=\"Add Camera\">");
                out.println("</fieldset></div>");
                out.println("</form><br>");                
                
                // list of existing entries
                Filter userFilter = new FilterPredicate("userID", FilterOperator.EQUAL, currentUser);
                Query query = new Query("Camera").setFilter(userFilter);
                List<Entity> members = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
                try {
                    if (!members.isEmpty()) {
                        int rawCount = 0;
                        out.println("<table>");
                        out.println("<tr><th>Serial number</th><th>Camera type</th><th>Camera location</th><th>Action</th></tr>");
                        for (Entity memberEntity : members) {
                            rawCount++;

                            String serialNumber = memberEntity.getProperty("serialnumber").toString();
                            String cameraType = memberEntity.getProperty("type").toString();
                            String cameraLocation = memberEntity.getProperty("location").toString();

                            out.print("<tr");
                            if (rawCount % 2 == 0) {
                                out.println(" class=\"alt\"");
                            }
                            out.println(">");

                            out.println("<td>" + serialNumber + "</td>");
                            out.println("<td>" + cameraType + "</td>");
                            out.println("<td>" + cameraLocation + "</td>");
                            out.print("<td><a class=\"greenbutton\" href=\"/register?action=remove&serial_number=");
                            out.println(serialNumber + "\">Remove</a></td></tr>");
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
    
    private boolean isSerialNumberRegistered(String serialNumber) {
        Filter cameraFilter = new FilterPredicate("serialnumber", FilterOperator.EQUAL, serialNumber);
        Query query = new Query("Camera").setFilter(cameraFilter);
        List<Entity> members = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        if (!members.isEmpty()) {
            return true;
        }
        return false;
    }
    
    private void removeItem(String serialNumber) {
        Filter cameraFilter = new FilterPredicate("serialnumber", FilterOperator.EQUAL, serialNumber);
        Query query = new Query("Camera").setFilter(cameraFilter);
        List<Entity> members = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        if (!members.isEmpty()) {
            for (Entity memberEntity : members) {
                datastore.delete(memberEntity.getKey());
            }
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
