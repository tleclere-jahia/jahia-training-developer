package org.foo.modules.jahia.taglibs;

import org.jahia.api.settings.SettingsBean;
import org.jahia.osgi.BundleUtils;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockPageContext;
import org.springframework.mock.web.MockServletContext;
import org.testng.Assert;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

public class JahiaPropertiesTaglibTest {
    @Test
    public void testJahiaPropertiesTaglib() {
        MockPageContext mockPageContext = new MockPageContext(new MockServletContext());
        try (MockedStatic<BundleUtils> bundleUtils = Mockito.mockStatic(BundleUtils.class)) {
            SettingsBean settingsBean = Mockito.mock(SettingsBean.class);
            bundleUtils.when(() -> BundleUtils.getOsgiService(SettingsBean.class, null)).thenReturn(settingsBean);
            Mockito.when(settingsBean.getPropertyValue(Mockito.anyString())).thenReturn("titi");

            JahiaPropertiesTaglib myCustomTag = new JahiaPropertiesTaglib();
            myCustomTag.setPageContext(mockPageContext);
            myCustomTag.setProperty("cluster.node.serverId");
            int result = myCustomTag.doStartTag();
            String output = ((MockHttpServletResponse) mockPageContext.getResponse()).getContentAsString();
            Assert.assertEquals(result, Tag.SKIP_BODY);
            Assert.assertEquals(output, "titi");
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testVarJahiaPropertiesTaglib() throws Exception {
        MockPageContext mockPageContext = new MockPageContext(new MockServletContext());
        try (MockedStatic<BundleUtils> bundleUtils = Mockito.mockStatic(BundleUtils.class)) {
            SettingsBean settingsBean = Mockito.mock(SettingsBean.class);
            bundleUtils.when(() -> BundleUtils.getOsgiService(SettingsBean.class, null)).thenReturn(settingsBean);
            Mockito.when(settingsBean.getPropertyValue(Mockito.anyString())).thenReturn("tata");

            JahiaPropertiesTaglib myCustomTag = new JahiaPropertiesTaglib();
            myCustomTag.setPageContext(mockPageContext);
            myCustomTag.setProperty("cluster.node.serverId");
            myCustomTag.setVar("nodeId");
            int result = myCustomTag.doStartTag();
            Assert.assertEquals(result, Tag.SKIP_BODY);
            Assert.assertNull(mockPageContext.getAttribute("nodeId"));
            Assert.assertEquals(mockPageContext.getAttribute("nodeId", PageContext.REQUEST_SCOPE), "tata");
            Assert.assertNotEquals(mockPageContext.getAttribute("nodeId", PageContext.REQUEST_SCOPE), "titi");
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
