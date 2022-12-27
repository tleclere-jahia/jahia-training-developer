package org.foo.modules.jahia.validators;

import org.foo.modules.jahia.framework.DeployAllResourcesTestExecutionListener;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.framework.AbstractJUnitTest;
import org.jahia.test.framework.SetDefaultsTestExecutionListener;
import org.jahia.test.utils.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;

import javax.jcr.RepositoryException;
import java.util.Locale;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@TestExecutionListeners(inheritListeners = false, listeners = {SetDefaultsTestExecutionListener.class, ServletTestExecutionListener.class, DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, DeployAllResourcesTestExecutionListener.class})
public class FoomixTextValidatorTest extends AbstractJUnitTest {
    private static final String TESTSITE_NAME = "mysite";

    private JahiaSite site;
    private JCRSessionWrapper editSession;
    private JCRSessionWrapper liveSession;

    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();
        JCRStoreService.getInstance().addValidator("foomix:richText", FoomixTextValidator.class);
    }

    @Before
    public void setUp() throws Exception {
        JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        sessionFactory.closeAllSessions();
        editSession = sessionFactory.getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.FRENCH);
        liveSession = sessionFactory.getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.FRENCH);

        site = TestHelper.createSite(TESTSITE_NAME);
        assertNotNull(site);
        assertTrue(site instanceof JCRSiteNode);
    }

    @After
    public void tearDown() throws Exception {
        JCRSessionFactory.getInstance().closeAllSessions();
        TestHelper.deleteSite(TESTSITE_NAME);
        site = null;
    }

    @Test
    public void testHtmlValid() {
        try {
            JCRNodeWrapper text = editSession.getNode("/sites/" + TESTSITE_NAME + "/contents").addNode("text", "foont:text");
            text.setProperty("text", "<span>blabla</span>");
            text.saveSession();
            assertTrue(true);
        } catch (RepositoryException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testHtmlNotValid() {
        assertThrows(RepositoryException.class, () -> {
            JCRNodeWrapper text = editSession.getNode("/sites/" + TESTSITE_NAME + "/contents").addNode("text", "foont:text");
            text.setProperty("text", "<span>blabla");
            text.saveSession();
        });
    }

    private void initEmployees() throws RepositoryException {
        JCRNodeWrapper contents = editSession.getNode("/sites/" + TESTSITE_NAME + "/contents").addNode("training", "jnt:contentFolder");
        contents.addNode("jsg", "foont:company");
        contents.addNode("trainees", "foont:company");
        editSession.save();
    }
}
