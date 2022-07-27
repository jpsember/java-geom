package testbed;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import base.*;

/**
 * Application class.
 *
 */
public abstract class Application {

  private static boolean dba = false;

  /**
   * Start a console application, or an applet simulation of one
   * 
   * @param args
   *          String[]
   */
  protected void doMain(String[] args) {
    // We must call this in case we are not running as an applet
    Streams.loadResources(this);
  }

  /**
   * Start a GUI application. Should be called by doMain() if it's supposed to
   * be a GUI.
   */
  protected void doMainGUI(String[] args) {
    Streams.loadResources(this);
    Streams.setFileAccess(new GUIAppFileAccess());

    // Schedule a job for the event-dispatching thread:
    // calling an application object's run() method.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        doInit();
      }
    });
  }

  /**
   * Determine title of application or applet. This is displayed as the title of
   * the frame, or as the label of the appletTitleLabel, if one is defined.
   *
   * @return String
   */
  protected String title() {
    String s = this.getClass().getName();
    s = s.substring(s.lastIndexOf('.') + 1);
    return s;
  }

  /**
   * Determine extended title of application/applet. This is the base title with
   * optional extra information. If none has been defined, uses default title().
   *
   * @return String
   */
  protected String extendedTitle() {
    String s = extTitle;
    if (s == null)
      s = title();
    return s;
  }

  /**
   * Set extended title of application/applet.
   * 
   * @param t
   *          String
   */
  public void setExtendedTitle(String t) {
    extTitle = t;
  }

  // extended title
  private String extTitle;

  /**
   * Override this method to change preferred size of application frame.
   * 
   * @return Dimension
   */
  public Dimension getPreferredSize() {
    return new Dimension(1024, 768);
  }

  protected void setFrameOptions(JFrame f) {
    f.addWindowListener(new WindowAdapter() {
      /**
       * Invoked when the user attempts to close the window from the window's
       * system menu.
       */
      public void windowClosing(WindowEvent e) {
        if (dba)
          Streams.out.println("windowClosing: " + e);
        exitProgram();
      }
    });
    f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
  }

  // ---------------------------------------------------

  protected void exitProgram() {

    final boolean db = dba;

    if (db) {
      System.out.println("make invis");
    }
    appFrame.setVisible(false);
    appFrame.dispose();
    if (db) {
      System.out.println("dispose");
    }

    if (db) {
      System.out.println("done");
    }
    if (db)
      Streams.out.println(" setting exited = true");

  }

  public void updateTitle() {
    if (appFrame != null)
      appFrame.setTitle(extendedTitle());
  }

  /**
   */
  protected void doInit() {
    Streams.loadResources(this);
    
    // Create and set up the window.
    appFrame = new ApplicationJFrame(this);
    setFrameOptions(appFrame);
    updateTitle();
  }

  /**
   * Show the application
   */
  protected void showApp() {
    appFrame.pack();
    appFrame.setLocationRelativeTo(null);
    appFrame.setVisible(true);
  }

  /**
   * Get content pane of application (or applet)
   * 
   * @return JComponent
   */
  protected static JComponent getAppContentPane() {
    return ((JComponent) appFrame.getContentPane());
  }

  /**
   * Get outermost application
   */
  public static Component getAppContainer() {
    return appFrame;
  }

  /**
   * Customized JFrame for application frame
   */
  private static class ApplicationJFrame extends JFrame {
    public ApplicationJFrame(Application app) {
      this.app = app;
    }

    private Application app;

    public Dimension getPreferredSize() {
      return app.getPreferredSize();
    }
  }

  /**
   * Get the JFrame containing the application or applet
   * 
   * @return JFrame, or null if applet that is not 'launched'
   */
  protected static JFrame appFrame() {
    return appFrame;
  }

  // JFrame of application (null if not open yet)
  protected static JFrame appFrame;

}
