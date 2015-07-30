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
import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author crayfishapps
 */
public class StatusUpdateServlet extends HttpServlet {

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
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date now = new Date();
            
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Query query = new Query("Camera");
            List<Entity> members = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
            if (!members.isEmpty()) {
                for (Entity memberEntity : members) {
                    String userMail =  memberEntity.getProperty("userMail").toString();
                    String serialnumber =  memberEntity.getProperty("serialnumber").toString();
                    String lastContact = memberEntity.getProperty("contact").toString();
                    String previousStatus = memberEntity.getProperty("status").toString();
                    Date cameraLastContact = dateFormat.parse(lastContact);
                    long difference = now.getTime() - cameraLastContact.getTime();
                            String cameraStatus = "OK";
                            if (difference > 3600000) {
                                cameraStatus = "Error";
                            }                     
                            memberEntity.setProperty("status", cameraStatus);
                            if ((cameraStatus.equalsIgnoreCase("Error")) && (previousStatus.equalsIgnoreCase("OK"))) {
                                sendMessage(userMail, serialnumber);
                            }

                    datastore.put(memberEntity);
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

    public void sendMessage(String userMail, String serialnumber) {
        Properties properties = new Properties();
        Session session = Session.getDefaultInstance(properties, null);

        String msgBody = "The network camera monitoring service has discovered a non-responding camera.\n";
        msgBody += "The serial number is " + serialnumber;

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("networkcameramonitor@appspot.gserviceaccount.com", "networkcameramonitor.appspot.com Admin"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(userMail, "networkcameramonitor.appspot.com User"));
            msg.setSubject("Network Camera Monitor Notification");
            msg.setText(msgBody);
            Transport.send(msg);

        } catch (Exception e) {
        }

    }
}
