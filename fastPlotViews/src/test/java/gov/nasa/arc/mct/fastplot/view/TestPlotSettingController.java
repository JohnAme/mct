/*******************************************************************************
 * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
 * as represented by the Administrator of the National Aeronautics and Space 
 * Administration. All rights reserved.
 *
 * The MCT platform is licensed under the Apache License, Version 2.0 (the 
 * "License"); you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 *
 * MCT includes source code licensed under additional open source licenses. See 
 * the MCT Open Source Licenses file included with this distribution or the About 
 * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
 * information. 
 *******************************************************************************/
package gov.nasa.arc.mct.fastplot.view;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisOrientationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.NonTimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.TimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestPlotSettingController {
	
	private AbstractComponent mockComponent;
	@Mock private PersistenceProvider mockProvider;
	@Mock private Platform mockPlatform;
	
	@BeforeMethod
	public void setup() {
		mockComponent = new DummyComponent();
		MockitoAnnotations.initMocks(this);
		new PlatformAccess().setPlatform(mockPlatform);
		Mockito.when(mockPlatform.getPersistenceProvider()).thenReturn(mockProvider);
	}
	
	@AfterMethod
	public void teardown() {
		new PlatformAccess().setPlatform(null);
	}
	
	@Test (expectedExceptions = IllegalArgumentException.class)
	public void  plotSettingControllerWithNullPanel() {
		@SuppressWarnings("unused") // testing exception. 
		PlotSettingController controller = new PlotSettingController(null);
 	}

	@Test
	public void testIsValidMethod() throws Exception {
		PlotViewManifestation panel = new PlotViewManifestation(mockComponent, new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT));
		PlotSettingsControlPanel plotSettingPanel = new PlotSettingsControlPanel(panel);
		PlotSettingController controller = new PlotSettingController(plotSettingPanel);
		Method invalidator = PlotSettingController.class.getDeclaredMethod("getInvalidMessage");
		invalidator.setAccessible(true);

		/* 
		 * Plot setting controller performs some additional validation on settings.
		 * The private method "getInvalidMessage" handles this by returning an appropriate 
		 * message for the invalid states, or null if the state is valid.
		 * This test invokes this method to ensure it does flag various impossible states 
		 * as invalid.  
		 */
		
		// Should not cause an error
		controller.createPlot();
		Assert.assertNull(invalidator.invoke(controller));
		
		// Setup good states for enum properties.
	    controller.setAxisOrientationSetting(AxisOrientationSetting.X_AXIS_AS_TIME);
	    controller.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT);
	    controller.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM);
	    controller.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.JUMP);
	    controller.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.AUTO);
	    controller.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.AUTO);
	    
	    // Set time Min and Max and padding to good values.  
	    controller.setNonTimeMinMaxValues(0.0, 1.0);
	    
	    GregorianCalendar now = new GregorianCalendar();
        GregorianCalendar nowPlus = new GregorianCalendar();
        nowPlus.add(Calendar.HOUR, 1);
	    controller.setTimeMinMaxValues(now, nowPlus);

	    // Good padding size
	    controller.setTimePadding(1.0);
		// Should not cause an error
	    Assert.assertNull(invalidator.invoke(controller));

		// Bad padding size
		controller.setTimePadding(-1.0);
		// Should cause an error
		Assert.assertNotNull(invalidator.invoke(controller));

		controller.setTimePadding(1.1);
		// Should cause an error
		Assert.assertNotNull(invalidator.invoke(controller));
		
		// Put padding back to a good value.
		 controller.setTimePadding(0.5);
		 Assert.assertNull(invalidator.invoke(controller));
		 
		// Test non time min and max.
		 // valid
		 controller.setNonTimeMinMaxValues(1.0, 1.1);
		 Assert.assertNull(invalidator.invoke(controller));
		 
		// invalid.
		 controller.setNonTimeMinMaxValues(1.1, 1.0);
		 Assert.assertNotNull(invalidator.invoke(controller));
		 
		 // put back to a good value.
		 controller.setNonTimeMinMaxValues(0.0, 1.0);
		 Assert.assertNull(invalidator.invoke(controller));
		 
         // Test Times
		 GregorianCalendar smallTime = new GregorianCalendar();
		 GregorianCalendar largeTime = new GregorianCalendar();
		 largeTime.add(Calendar.MINUTE,10);
		 
		 controller.setTimeMinMaxValues(largeTime, smallTime);
		 Assert.assertNotNull(invalidator.invoke(controller));
	}
	
	@Test
	public void testPlotSettingController() {
		PlotViewManifestation panel = new PlotViewManifestation(mockComponent, new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT));

		PlotSettingsControlPanel plotSettingPanel = new PlotSettingsControlPanel(panel);
		PlotSettingController controller = new PlotSettingController(plotSettingPanel);
		
		// for coverage
		controller.setAxisOrientationSetting(AxisOrientationSetting.X_AXIS_AS_TIME);
		controller.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT);
        controller.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM);
	    controller.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.JUMP);
        controller.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.AUTO);
        controller.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.AUTO);
        controller.setNonTimeMinMaxValues(0, 100);
        controller.setPlotLineDraw(PlotConstants.DEFAULT_PLOT_LINE_DRAW);
        
        GregorianCalendar now = new GregorianCalendar();
        GregorianCalendar nowPlus = new GregorianCalendar();
        nowPlus.add(Calendar.HOUR, 1);
        
        controller.setTimeMinMaxValues(now, nowPlus);
        controller.setTimePadding(0.1);
        controller.createPlot();
	}
	
	private static class DummyComponent extends AbstractComponent {
		public DummyComponent() {
		}
	}
	
}
