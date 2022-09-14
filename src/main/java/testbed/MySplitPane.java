package testbed;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

import geom.EditorPanel;

import static js.base.Tools.*;

public class MySplitPane extends JSplitPane {

  public MySplitPane(int newOrientation, Component newLeftComponent, Component newRightComponent) {
    super(newOrientation, newLeftComponent, newRightComponent);
  }
//
//  @Override
//  protected void processEvent(AWTEvent e) {
//    pr("processEvent:",e);
//    super.processEvent(e);
//  }
//  
  @Override
  protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
    return false;
//   pr("processKeyBinding:",ks,"event:",e);
//   if (true) return false;
   // return super.processKeyBinding(ks, e, condition, pressed);
  }
}
