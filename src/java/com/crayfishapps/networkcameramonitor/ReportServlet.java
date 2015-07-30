package com.crayfishapps.networkcameramonitor;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
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
public class ReportServlet extends HttpServlet {

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
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        try {
            Enumeration<String> parameterNames = request.getParameterNames();
            if (parameterNames.hasMoreElements()) {
                String serialNumber = request.getParameter("camera");
                serialNumber = serialNumber.trim().toLowerCase();
                serialNumber = serialNumber.replaceAll(":", "");
                String action = request.getParameter("action");
                action = action.trim().toLowerCase();
                
                DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
                Query.Filter cameraFilter = new Query.FilterPredicate("serialnumber", Query.FilterOperator.EQUAL, serialNumber);
                Query query = new Query("Camera").setFilter(cameraFilter);
                List<Entity> members = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
                if (!members.isEmpty()) {
                    for (Entity memberEntity : members) {
                        
                        // update database
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = new Date();
                        String dateString = dateFormat.format(date);

                        if (action.equalsIgnoreCase("update")) {
                            memberEntity.setProperty("contact", dateString);
                            memberEntity.setProperty("status", "OK");
                        }

                        if (action.equalsIgnoreCase("notify")) {
                            String resource = request.getParameter("resource");
                            String value = request.getParameter("value");
                            if (resource != null) {
                                if (value == null) {
                                    value = dateString;
                                }
                                memberEntity.setProperty(resource, value);
                                if (resource.equalsIgnoreCase("boot")) {
                                    memberEntity.setProperty("storage_sd", "unknown");
                                    memberEntity.setProperty("storage_nas", "unknown");
                                    memberEntity.setProperty("tampering", "");
                                    memberEntity.setProperty("streaming", "");
                                    memberEntity.setProperty("motion", "");
                                    memberEntity.setProperty("recording", "");                                    
                                }
                            }
                        }

                        datastore.put(memberEntity);
                    }
                }
            }
            out.println("<OK>");
        }
        catch (Exception e) {
        }
        finally {
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
