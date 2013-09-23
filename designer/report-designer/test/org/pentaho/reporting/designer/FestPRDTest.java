/*
 *
 *  * This program is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 *  * Foundation.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License along with this
 *  * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *  * or from the Free Software Foundation, Inc.,
 *  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *  *
 *  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  * See the GNU Lesser General Public License for more details.
 *  *
 *  * Copyright (c) 2006 - 2009 Pentaho Corporation..  All rights reserved.
 *
 */

package org.pentaho.reporting.designer;

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import junit.framework.TestCase;
import static org.fest.assertions.Assertions.assertThat;
import org.fest.swing.annotation.GUITest;
import org.fest.swing.annotation.RunsInEDT;
import org.fest.swing.core.BasicComponentFinder;
import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.ComponentFinder;
import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.core.Robot;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JComboBoxFixture;
import org.fest.swing.fixture.JListFixture;
import org.fest.swing.fixture.JTextComponentFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.openformula.ui.FormulaEditorDialog;
import org.pentaho.openformula.ui.FormulaEditorPanel;
import org.pentaho.reporting.designer.core.DefaultReportDesignerContext;
import org.pentaho.reporting.designer.core.ReportDesignerBoot;
import org.pentaho.reporting.designer.core.ReportDesignerFrame;
import org.pentaho.reporting.designer.core.util.GUIUtils;
import org.pentaho.reporting.designer.testsupport.TestReportDesignerView;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.ui.xul.XulException;

@GUITest     // takes screenshot if test case fails
public class FestPRDTest extends TestCase
{
  private FrameFixture window;
  private ReportDesignerFrame frame;

  public FestPRDTest()
  {
  }

  private void block(final long secs)
  {
    try
    {
      Thread.sleep(secs * 1000);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  @Before
  public void setUp() throws XulException
  {
    System.setProperty("java.awt.headless", "false");
    ClassicEngineBoot.getInstance().start();
    ReportDesignerBoot.getInstance().start();

    frame = GuiActionRunner.execute(new GuiQuery<ReportDesignerFrame>()
    {
      protected ReportDesignerFrame executeInEDT()
      {
        try
        {
          ReportDesignerFrame frame = new ReportDesignerFrame();
          frame.pack();
          frame.initWindowLocations(null);
          frame.setVisible(true);

          return frame;
        }
        catch (XulException ex)
        {
        }

        return null;
      }
    });

    window = new FrameFixture(frame);

    // Slow down user gestures (default is 60 ms).
    // Set between 100-200 to simulate actual user input.
    window.robot.settings().delayBetweenEvents(60);
//    window.robot.settings().delayBetweenEvents(1500);

    window.show(); // shows the frame to test
    window.requireVisible();

    window.maximize();
  }

  @After
  public void tearDown() {
    window.cleanUp();
  }

  @Test
  /**
   * This test case creates a new report, selects the details band and then
   * inserts a chart element into the details band.
   */
  public void testInvokeFormulaEditorDialog()
  {
    ComponentFinder finder = BasicComponentFinder.finderWithCurrentAwtHierarchy();

    // Create a new report
    window.menuItemWithPath("File", "New").click();

    // Select the details band row
    Component detailsBand = finder.findByName("renderComponent_2");
    window.robot.click(detailsBand);

    // Insert a chart element
    window.menuItemWithPath("Insert", "chart").click();

    // TODO: Find the chart element and double-click it

    block(5);
  }

  @RunsInEDT
  @Test
  /**
   * Instantiates a Formula Editor Dialog and selects the Logical cateogry
   * of formulas.  Then we select the 'IF' formula that has three parameters.
   * The first parameter is the if part.
   * The second parameter is the else part
   * The third parameter is when the if part is false
   */
  public void testFormulaEditorDialog() throws XulException, InterruptedException
  {
    final ComponentFinder finder = BasicComponentFinder.finderWithCurrentAwtHierarchy();

    final FormulaEditorDialog dialog =
        GUIUtils.createFormulaEditorDialog(new DefaultReportDesignerContext(frame, new TestReportDesignerView()), frame);

    final FormulaEditorDialog formulaEditorDialog = (FormulaEditorDialog)finder.findByName("FormulaEditorDialog");
    assertNotNull(formulaEditorDialog);

    final GenericTypeMatcher genericTypeMatcher = new GenericTypeMatcher(FormulaEditorDialog.class, false)
    {
      protected boolean isMatching(final Component component)
      {
        if (component.getName().compareTo("FormulaEditorDialog") == 0)
        {
          return true;
        }

        return false;
      }
    };

    // Create the Formula Editor Dialog fixture
    final DialogFixture fixture = window.dialog(genericTypeMatcher);
    fixture.show();
    fixture.requireVisible();
    fixture.requireModal();


    JComboBox categoryComboBox = (JComboBox)finder.findByName(dialog, "categoryComboBox");
    JComboBoxFixture comboBoxFixture = new JComboBoxFixture(window.robot, categoryComboBox);
    comboBoxFixture.selectItem("Logical");

    // Since we selected a new item, get a refreshed list
    JList filteredFormulaList = (JList)finder.findByName(dialog, "formulaList");
    final JListFixture filteredListFixture = new JListFixture(window.robot, filteredFormulaList);

    // Select IF formula
    filteredListFixture.doubleClickItem("IF");

    // Get all the parameter fields and set values in them.
    final JTextField param0 = (JTextField)finder.findByName("parameterValue0", JTextField.class);
    final JTextField param1 = (JTextField)finder.findByName("parameterValue1", JTextField.class);
    final JTextField param2 = (JTextField)finder.findByName("parameterValue2", JTextField.class);

    assertEquals("Logical", param0.getText());
    assertEquals("Any", param1.getText());
    assertEquals("Any", param2.getText());

    final JLabel label0 = (JLabel)finder.findByName("paramNameLabel0", JLabel.class);
    final JLabel label1 = (JLabel)finder.findByName("paramNameLabel1", JLabel.class);
    final JLabel label2 = (JLabel)finder.findByName("paramNameLabel2", JLabel.class);

    assertEquals("Test", label0.getText());
    assertEquals("Then_value", label1.getText());
    assertEquals("Otherwise_value", label2.getText());

    JTextComponentFixture paramTF = new JTextComponentFixture(window.robot, param0);
    paramTF.setText("1");

    paramTF = new JTextComponentFixture(window.robot, param1);
    paramTF.setText("2");

    paramTF = new JTextComponentFixture(window.robot, param2);
    paramTF.setText("3");

    // Click in the text-area
    final FormulaEditorPanel panel = (FormulaEditorPanel)formulaEditorDialog.createContentPane();
    assertNotNull(panel);

    final JTextArea formulaTextArea = panel.getFunctionTextArea();
    final JTextComponentFixture formulaTextAreaFixture = new JTextComponentFixture(window.robot, (JTextArea)formulaTextArea);
    formulaTextAreaFixture.click();

    JLabel errorTextHolder = (JLabel)finder.findByName("errorTextHolder");
    assertEquals("2", errorTextHolder.getText());

    // Validate function text area
    JTextArea functionDescLabel = finder.findByName("functionDescription", JTextArea.class);
    assertEquals("Specifies a logical test to be performed.", functionDescLabel.getText());

    JLabel functionReturnType = finder.findByName("functionReturnType", JLabel.class);
    assertEquals("Any", functionReturnType.getText());

    fixture.button("OK").click();

    // Display formula editor dialog again to display previous values
    fixture.show();

    // Lets check to make sure the category comboBox is has 'Logical' selected
    categoryComboBox = (JComboBox)finder.findByName(dialog, "categoryComboBox");
    comboBoxFixture = new JComboBoxFixture(window.robot, categoryComboBox);
    assertThat(comboBoxFixture.valueAt(0)).as("Logical");

    // Validate that the formula expression evaluates to '2' since the
    // IF part is true.
    errorTextHolder = (JLabel)finder.findByName("errorTextHolder");
    assertEquals("2", errorTextHolder.getText());

    block(10);
    fixture.button("Cancel").click();
  }
}
