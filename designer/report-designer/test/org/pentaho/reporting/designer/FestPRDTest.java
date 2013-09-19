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
import java.awt.Dialog;
import java.awt.Point;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import junit.framework.TestCase;
import org.fest.swing.annotation.GUITest;
import org.fest.swing.annotation.RunsInEDT;
import org.fest.swing.core.BasicComponentFinder;
import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.ComponentFinder;
import org.fest.swing.core.ComponentMatcher;
import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.core.MouseClickInfo;
import org.fest.swing.core.Robot;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.edt.GuiTask;
import org.fest.swing.exception.ComponentLookupException;
import org.fest.swing.fixture.CommonComponentFixture;
import org.fest.swing.fixture.ComponentFixture;
import org.fest.swing.fixture.ContainerFixture;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JComboBoxFixture;
import org.fest.swing.fixture.JComponentFixture;
import org.fest.swing.fixture.JListFixture;
import org.fest.swing.fixture.JPanelFixture;
import org.fest.swing.fixture.JPopupMenuFixture;
import org.fest.swing.fixture.MouseInputSimulationFixture;
import org.fest.swing.fixture.WindowFixture;
import org.fest.swing.timing.Timeout;
import org.jfree.chart.JFreeChart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.openformula.ui.DefaultFunctionParameterEditor;
import org.pentaho.openformula.ui.FieldDefinition;
import org.pentaho.openformula.ui.FormulaEditorDialog;
import org.pentaho.openformula.ui.FormulaEditorModel;
import org.pentaho.openformula.ui.FormulaEditorPanel;
import org.pentaho.openformula.ui.MultiplexFunctionParameterEditor;
import org.pentaho.reporting.designer.core.DefaultReportDesignerContext;
import org.pentaho.reporting.designer.core.ReportDesignerBoot;
import org.pentaho.reporting.designer.core.ReportDesignerFrame;
import org.pentaho.reporting.designer.core.editor.report.ResizeRootBandComponent;
import org.pentaho.reporting.designer.core.editor.report.RootBandRenderComponent;
import org.pentaho.reporting.designer.core.util.DesignerFormulaEditorDialog;
import org.pentaho.reporting.designer.core.util.GUIUtils;
import org.pentaho.reporting.designer.testsupport.TestReportDesignerView;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.ui.xul.XulException;

public class FestPRDTest extends TestCase
{
  private FrameFixture window;
  private ReportDesignerFrame frame;

  public FestPRDTest()
  {
  }

  private void block()
  {
    try
    {
      Thread.sleep(10000000);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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

    window.show(); // shows the frame to test
    window.requireVisible();

    window.maximize();
  }

  @After
  public void tearDown() {
    window.cleanUp();
  }

  @Test
  public void testInvokeFormulaEditorDialog()
  {
    ComponentFinder finder = BasicComponentFinder.finderWithCurrentAwtHierarchy();

    // Create a new report
    window.menuItemWithPath("File", "New").click();

    // Select the details band row
    Component detailsBand = finder.findByName("renderComponent_2");
    window.robot.click(detailsBand);

//    ComponentFinder newfinder = BasicComponentFinder.finderWithNewAwtHierarchy();

    // Insert a chart element
    window.menuItemWithPath("Insert", "chart").click();

    // Find the chart element and double-click it
//    Object element = (Object)finder.findByType(JFreeChart.class);


    block();
  }

//  @GUITest     // takes screenshot if test case fails
  @RunsInEDT
  @Test
  public void testFormulaEditorDialog() throws XulException, InterruptedException
  {
    ComponentFinder finder = BasicComponentFinder.finderWithCurrentAwtHierarchy();

    final FormulaEditorDialog dialog =
        GUIUtils.createFormulaEditorDialog(new DefaultReportDesignerContext(frame, new TestReportDesignerView()), frame);

    final FormulaEditorDialog formulaEditorDialog = (FormulaEditorDialog)finder.findByName("FormulaEditorDialog");
    FormulaEditorPanel panel = (FormulaEditorPanel)formulaEditorDialog.createContentPane();
    JTextArea formulaTextArea = panel.getFunctionTextArea();
    FormulaEditorModel model = panel.getEditorModel();


    // Retrieve formula category drop-down and select the 2nd item in list
    final JComboBox categoryComboBox = (JComboBox)finder.findByName(dialog, "categoryComboBox");
    final JList formulaList = (JList)finder.findByName(dialog, "formulaList");
    System.out.println("******* formulaList: " + formulaList.getName() + " with element at  " + formulaList.getModel().getElementAt(2));
    System.out.println("\t size = " + formulaList.getModel().getSize());

    final JComboBoxFixture JComboBoxFixture = new JComboBoxFixture(window.robot, categoryComboBox);
    final JListFixture jListFixture = new JListFixture(window.robot, formulaList);


    GenericTypeMatcher genericTypeMatcher = new GenericTypeMatcher(FormulaEditorDialog.class, false)
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

    final DialogFixture fixture = window.dialog(genericTypeMatcher);
    fixture.show();
    fixture.requireVisible();
    fixture.requireModal();




////    dialog.show();
//
    GuiActionRunner.execute(new GuiTask() {
      public void executeInEDT() {
        System.out.println("** selecting item ");

//        categoryComboBox.setSelectedIndex(2);
//        formulaList.setSelectedIndex(2);
//
////        dialog.setVisible(true);
//        jListFixture.selectItem(2).doubleClick();
      }
    });

    Thread.sleep(5000);
    fixture.button("OK").click();

    block();
//
//    GuiActionRunner.execute(new GuiTask() {
//      public void executeInEDT()
//      {
//        System.out.println("Double click");
//        jListFixture.doubleClick();
//      }
//    });
//
//
//    dialogFixture.button("OK").click();

//    categoryComboBox.setSelectedIndex(1);
//    System.out.println("Item count: " + categoryComboBox.getItemCount());
//    System.out.println("first item: " + categoryComboBox.getItemAt(1));
//    System.out.println("******** categroy combo: " + categoryComboBox.toString());



//    ComponentFinder finder = window.robot.finder();
//    DesignerFormulaEditorDialog editorDialog = new DesignerFormulaEditorDialog(window.component());

//    DialogFixture fixture = new DialogFixture(dialog);
//    fixture.rightClick();
//
//    dialog.show();


//    dialog.editFormula("=COUNT()", new FieldDefinition[0]);

//    fixture.show();

//    FormulaEditorPanel formulaEditorPanel = (FormulaEditorPanel)dialog.createContentPane();
//    formulaEditorPanel.setFormulaText("=COUNT(1;2;3)");
  }

  @Test
  public void validateFormulaEditor()
  {
    FormulaEditorPanel panel = new FormulaEditorPanel();
    final Robot robot = BasicRobot.robotWithCurrentAwtHierarchy();

//    ComponentFixture<FormulaEditorPanel> fixture = new Comp onentFixture<FormulaEditorPanel>(robot, panel);

    panel.getFunctionTextArea().getDocument().removeDocumentListener(panel.getDocSyncHandler());

    panel.setFormulaText("=COUNT()");

    MultiplexFunctionParameterEditor functionParameterEditor = panel.getFunctionParameterEditor();
    DefaultFunctionParameterEditor activeEditor = functionParameterEditor.getDefaultEditor();

    activeEditor.fireParameterUpdate(0, "SUM(1)");
    activeEditor.fireParameterUpdate(1, "2");
    activeEditor.fireParameterUpdate(2, "3");

    assertEquals("=COUNT(SUM(1);2;3)", panel.getFormulaText());
  }

  @Test
  public void testSplashScreen()
  {
    window.focus();
  }
}
