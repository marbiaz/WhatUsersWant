

package wuw.ui.WebUI;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import wuw.ui.UIHandler;


/**
 * This class attends all the message received and send to WebPage
 * 
 * @author Adriana Perez-Espinosa
 * @date 2012 Feb 15
 */
public class WebUIComm {

PrintWriter out = null;
ServerSocket serverSocket = null;
BufferedReader input = null;
Socket socket = null;
WebUIHandler handler;


public WebUIComm() {
  handler = new WebUIHandler();
}


public WebUIComm(WebUIHandler h) {
  handler = h;
}


/**
 * Accept to communication for send/received message
 */
public void Connection() {

  try {

    String x;
    boolean iterate = true;
    // int init=0,end=0;
    for (;;) {

      serverSocket = new ServerSocket(5557);
      socket = serverSocket.accept();
      BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream());
      do {

        x = input.readLine();

        System.out.println("Receive: " + x);
        if (x.contains("Request") == true) {
          out.println(Request(x));
          out.flush();
        } else if (x.contains("New#") == true) {
          String idContent = obtainIdContent(x);
          if (handler.existContent(idContent) == false) {
            CreateContent(idContent);
            handler.displayPreferences(idContent);
            handler.displayContent();
            sendFeedback(handler.getFeeback(idContent));
          }
        } else if (x.contains("Del#") == true) {
          handler.removeContent(obtainIdContent(x));
          handler.displayContent();

        } else if (x.contains("Reset#") == true) {
          resetValues(obtainIdContent(x));

        } else if (x.contains("exit") == true) {
          handler.removeAll();
          handler.displayContent();

          iterate = false;

        } else {

          obtainValues(x);

        }


      } while (iterate);
      iterate = true;


      serverSocket.close();
      input.close();
      socket.close();

    }


  }
  catch (IOException e) {
    e.printStackTrace();

  }
}


/**
 * This function create a new content.
 * 
 * @param idContent
 *          is the id content
 */
public void CreateContent(String idContent) {
  Integer[] val_a = { 100, 500, 700 };
  String[] val_b = { "false", "true" };
  handler.initPref(idContent, new String("Preference1"), 1.0, 3.0, 0.5, 2.0);
  handler.initPref(idContent, new String("Preference2"), (Object[])val_a, (Object)val_a[2]);
  handler.initPref(idContent, new String("Preference3"), 3.0, 5.0, 0.1, 4.9);
  handler.initPref(idContent, new String("Preference4"), (Object[])val_b, true);
  handler.initFeedback(idContent, UIHandler.feedback_measures, UIHandler.feedback_defaults);
}


/**
 * This function is called when the user wants modify the preferences of the
 * content
 * 
 * @param x
 *          is the id content
 * @return return a String where are include all the names and values by default
 *         used in the preferences
 */
private String Request(String x) {
  String content = obtainIdContent(x);
  String Form = "Form" + handler.getPreferences(content);
  return Form;

}


/**
 * @param message
 *          Message received
 * @return The Id of the Content
 */
private String obtainIdContent(String message) {
  String[] IdContent = null;
  String content = new String(message);
  int pos = content.indexOf("#");
  if (pos != -1) {
    IdContent = message.split("#");
  }
  return IdContent[1].toString();
}


/**
 * @param message
 *          message receive for the browser
 */
private void obtainValues(String message) {
  String[] Values = null;
  int pos = message.indexOf("#", 0);
  if (pos != -1) {
    Values = message.split("#");
    setValues(Values);
  }
}


/**
 * @param idContent
 *          Id of the content
 * @param message
 *          String that include the name of the preferences and its value set up
 * @param numPref
 *          number of preferences
 */
private void setValues(String[] Values) {
  String Preferences = Values[2].toString();
  String[] Pref = null;
  String[] SetValue = null;
  if (Preferences.indexOf("&", 0) != -1) {
    Pref = Preferences.split("&");
    for (int i = 0; i < Integer.parseInt(Values[1]); i++) {
      SetValue = Pref[i].split("=");
      handler.setPref(Values[0], SetValue[0], (Object)SetValue[1]);
    }

  }
  handler.displayPreferences(Values[0]);
}


/**
 * This function sets the value of the preferences to its default value.
 * 
 * @param idContent
 *          id of the content
 */
private void resetValues(String idContent) {
  handler.reset(idContent);
  handler.displayPreferences(idContent);

}


public void sendFeedback(String message) {
  String mess = "FeedBack#" + message;
  out.println(mess);
  out.flush();
}


public static void main(String[] args) {
  WebUIComm communication = new WebUIComm();
  communication.Connection();

}

}
