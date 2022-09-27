package testbed;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import js.guiapp.UserEvent;

import static js.base.Tools.*;
import static testbed.TestBedTools.*;

import java.awt.*;

class CtSpinner extends Gadget implements ChangeListener {

  private static final boolean db = false && alert("debug printing in effect");

  // ChangeListener implementation
  @Override
  public final void stateChanged(ChangeEvent changeEvent) {
    testBed().processAction(UserEvent.widgetEvent(getId()));
  }

  public void writeValue(Object v) {
    if (db) {
      pr("writing value", v, "to CtSpinner", this);
    }
    if (mIsSlider) {
      ((mySlider) mComponent).writeValue(v);
    } else {
      ((mySpinner) mComponent).writeValue(v);
    }
  }

  public Object readValue() {
    Object result;
    if (mIsSlider) {
      result = ((mySlider) mComponent).readValue();
    } else {
      result = ((mySpinner) mComponent).readValue();
    }
    if (db)
      pr("reading value from CtSpinner", this, result);
    return result;
  }

  public void setEnabled(GadgetList gl, boolean state) {
    mComponent.setEnabled(state);
  }

  public CtSpinner(String label, double dmin, double dmax, double dvalue, double step, boolean sliderFlag,
      boolean withTicks, boolean dbl) {
    mIsSlider = sliderFlag;
    if (db)
      pr("constructing CtSpinner", this);

    JPanel panel = new JPanel();
    panel.setOpaque(true);

    if (!sliderFlag) {
      JSpinner c1 = null;
      if (!dbl) {
        c1 = new mySpinner((int) dmin, (int) dmax, (int) dvalue, (int) step);
      } else {
        c1 = new mySpinner(dmin, dmax, dvalue, step);
      }

      c1.addChangeListener(this);
      if (label != null) {
        panel.add(new JLabel(label));
      }
      mComponent = c1;
      panel.add(c1);
    } else {
      BoxLayout layout = new BoxLayout(panel, BoxLayout.X_AXIS);

      panel.setLayout(layout);
      mySlider cs = null;
      cs = new mySlider(dbl, dmin, dmax, dvalue, step);

      cs.addChangeListener(this);
      if (label != null) {
        if (!label.endsWith(":"))
          label = label + ":";
        JLabel lbl = new JLabel(label);
        lbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
        panel.add(lbl);
      }

      if (withTicks) {
        final boolean db = false;
        if (db) {
          System.out.println("slider ticks, dmax=" + dmax + ", dmin=" + dmin);
        }
        {
          int steps = 0;
          double r = dmax - dmin;
          if (dbl) {
            int nsteps = (int) (r / step);
            int stepV = (int) (r / nsteps);

            if (nsteps <= 12) {
              cs.setMinorTickSpacing(stepV);
              cs.setMajorTickSpacing(2 * stepV);
              cs.createStandardLabels(2 * stepV);
              cs.setSnapToTicks(true);
            } else {
              nsteps = Math.min(nsteps, 50);
              double major = (r / 4 / step);
              double minor = major / 2;

              cs.setMinorTickSpacing((int) minor);
              cs.setMajorTickSpacing((int) major);
              cs.createStandardLabels((int) (r / 4));
            }
          } else {
            steps = (int) (dmax + 1 - dmin);

            if (steps <= 12) {
              cs.setMinorTickSpacing(1);
              cs.setMajorTickSpacing(2);
              cs.createStandardLabels(2);
              cs.setSnapToTicks(true);
            } else {
              cs.setMinorTickSpacing(steps / 20);
              cs.setMajorTickSpacing(steps / 4);
              cs.createStandardLabels(steps / 4);
            }

          }
          if (db) {
            System.out.println(" steps=" + steps);
          }
          cs.setPaintTicks(true);
          cs.setPaintLabels(true);

        }
      }
      mComponent = cs;
      panel.add(cs);
    }
    setComponent(panel);
  }

  private class mySpinner extends JSpinner implements GadgetComponent {

    public Gadget getGadget() {
      return CtSpinner.this;
    }

    public mySpinner(int min, int max, int value, int step) {
      super(new SpinnerNumberModel(value, min, max, step));

      disableKeybd();

    }

    private void disableKeybd() {
    }

    private boolean dblFlag;

    public mySpinner(double dmin, double dmax, double dvalue, double step) {
      dblFlag = true;
      //Tools.warn("add special model to edit value in popup");
      super.setModel(new SpinnerNumberModel(dvalue, dmin, dmax, step));
      ((JSpinner.DefaultEditor) getEditor()).getTextField().setColumns(5);
      disableKeybd();

    }

    public void writeValue(Object v) {
      SpinnerNumberModel m = (SpinnerNumberModel) getModel();
      if (!dblFlag) {
        m.setValue(v);
      } else {
        double vd = ((Double) v).doubleValue();
        m.setValue(vd);
      }

    }

    public Object readValue() {
      Number n = ((SpinnerNumberModel) getModel()).getNumber();
      if (dblFlag)
        return n.doubleValue();
      else
        return n.intValue();
    }
  }

  private class mySlider extends JSlider implements GadgetComponent {

    public Gadget getGadget() {
      return CtSpinner.this;
    }

    public mySlider(boolean dbls, double dmin, double dmax, double dvalue, double step) {
      dblFlag = dbls;
      double range = dmax - dmin;
      if (dbls)
        scale = 1 / step;
      else
        scale = 1;
      offset = dmin;
      this.setMaximum((int) (range * scale));
      this.setValue((int) ((dvalue - dmin) * scale));
      // note: sv works out to be exactly 1
      // but this seems to be ignored by the underlying BoundedRangeModel
      int sv = (int) (step * scale);
      setExtent(sv);
    }

    private boolean dblFlag;
    private double scale;
    private double offset;

    public void writeValue(Object v) {
      if (dblFlag) {
        double vd = ((Double) v).doubleValue();
        getModel().setValue((int) ((vd - offset) * scale));
      } else {
        getModel().setValue(((Number) v).intValue());
      }
    }

    public Object readValue() {
      if (dblFlag) {
        return getModel().getValue() / scale + offset;
      } else {
        return (int) (getModel().getValue() + offset);
      }
    }

  }

  private boolean mIsSlider;
  private Component mComponent;

}
