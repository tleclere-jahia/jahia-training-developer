package org.foo.modules.jahia.valve;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.api.usermanager.JahiaUserManagerService;
import org.jahia.bin.Login;
import org.jahia.osgi.FrameworkService;
import org.jahia.params.valves.*;
import org.jahia.pipelines.Pipeline;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component(service = Valve.class, immediate = true)
public class CustomValveForJExperience extends BaseAuthValve {
    private static final Logger logger = LoggerFactory.getLogger(CustomValveForJExperience.class);

    private Pipeline authPipeline;
    private JahiaUserManagerService jahiaUserManagerService;
    private JExperienceService jExperienceService;

    @Reference(service = Pipeline.class, target = "(type=authentication)")
    public void setAuthPipeline(Pipeline authPipeline) {
        this.authPipeline = authPipeline;
    }

    @Reference
    private void setJahiaUserManagerService(JahiaUserManagerService jahiaUserManagerService) {
        this.jahiaUserManagerService = jahiaUserManagerService;
    }

    @Reference
    private void setjExperienceService(JExperienceService jExperienceService) {
        this.jExperienceService = jExperienceService;
    }

    @Activate
    private void onActivate(Map<String, ?> properties) {
        setId(CustomValveForJExperience.class.getSimpleName());
        removeValve(authPipeline);
        addValve(authPipeline, -1, "LoginEngineAuthValve", null);
        setEnabled(properties != null && properties.containsKey("valve.enabled")
                && Boolean.parseBoolean((String) properties.get("valve.enabled")));
    }

    @Deactivate
    private void onDeactivate() {
        removeValve(authPipeline);
    }

    @Override
    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        final AuthValveContext authValveContext = (AuthValveContext) context;
        final HttpServletRequest httpServletRequest = authValveContext.getRequest();
        final String siteKey = httpServletRequest.getParameter("site");

        if (!isEnabled() || authValveContext.getSessionFactory().getCurrentUser() != null || StringUtils.isBlank(siteKey)) {
            valveContext.invokeNext(context);
        } else {
            JCRUserNode jcrUserNode = getJcrUserNode(httpServletRequest, siteKey);
            if (jcrUserNode == null) {
                valveContext.invokeNext(context);
            } else {
                logger.info("User {} logged in.", jcrUserNode);

                // if there are any attributes to conserve between session, let's copy them into a map first
                Map<String, Object> savedSessionAttributes = preserveSessionAttributes(httpServletRequest);

                JahiaUser jahiaUser = jcrUserNode.getJahiaUser();

                if (httpServletRequest.getSession(false) != null) {
                    httpServletRequest.getSession().invalidate();
                }

                // if there were saved session attributes, we restore them here.
                restoreSessionAttributes(httpServletRequest, savedSessionAttributes);

                httpServletRequest.setAttribute(LoginEngineAuthValveImpl.VALVE_RESULT, LoginEngineAuthValveImpl.OK);
                authValveContext.getSessionFactory().setCurrentUser(jahiaUser);

                // do a switch to the user's preferred language
                if (SettingsBean.getInstance().isConsiderPreferredLanguageAfterLogin()) {
                    Locale preferredUserLocale = UserPreferencesHelper.getPreferredLocale(jcrUserNode, LanguageCodeConverters.resolveLocaleForGuest(httpServletRequest));
                    httpServletRequest.getSession().setAttribute(Constants.SESSION_LOCALE, preferredUserLocale);
                }

                try {
                    final String identifier = httpServletRequest.getParameter("identifier");
                    jExperienceService.sendLoginEvent(jcrUserNode, identifier, siteKey, httpServletRequest);
                } catch (RepositoryException | JsonProcessingException e) {
                    logger.error("", e);
                }

                SpringContextSingleton.getInstance().publishEvent(new LoginEvent(this, jahiaUser, authValveContext));
                Map<String, Object> m = new HashMap<>();
                m.put("user", jahiaUser);
                m.put("authContext", authValveContext);
                m.put("source", this);
                FrameworkService.sendEvent("org/jahia/usersgroups/login/LOGIN", m, false);
            }
        }
    }

    private JCRUserNode getJcrUserNode(HttpServletRequest httpServletRequest, String siteKey) {
        if (isLoginRequested(httpServletRequest)) {
            final String username = httpServletRequest.getParameter("username");
            if (StringUtils.isNotBlank(username)) {
                return jahiaUserManagerService.lookupUser(username, siteKey);
            }
        }
        return null;
    }

    private boolean isLoginRequested(HttpServletRequest request) {
        String doLogin = request.getParameter("login");
        if (doLogin != null) {
            return Boolean.parseBoolean(doLogin) || "1".equals(doLogin);
        } else if ("/cms".equals(request.getServletPath())) {
            return Login.getMapping().equals(request.getPathInfo());
        }
        return false;
    }

    private static Map<String, Object> preserveSessionAttributes(HttpServletRequest httpServletRequest) {
        Map<String, Object> savedSessionAttributes = new HashMap<>();
        HttpSession session = httpServletRequest.getSession(false);
        if (session != null) {
            String sessionAttributeName = "wemSessionId";
            Object attributeValue = session.getAttribute(sessionAttributeName);
            if (attributeValue != null) {
                savedSessionAttributes.put(sessionAttributeName, attributeValue);
            }
        }
        return savedSessionAttributes;
    }

    private static void restoreSessionAttributes(HttpServletRequest httpServletRequest, Map<String, Object> savedSessionAttributes) {
        if (savedSessionAttributes.size() > 0) {
            HttpSession session = httpServletRequest.getSession();
            for (Map.Entry<String, Object> savedSessionAttribute : savedSessionAttributes.entrySet()) {
                session.setAttribute(savedSessionAttribute.getKey(), savedSessionAttribute.getValue());
            }
        }
    }

    private static class LoginEvent extends BaseLoginEvent {
        private static final long serialVersionUID = 8750248847271757719L;

        public LoginEvent(Object source, JahiaUser jahiaUser, AuthValveContext authValveContext) {
            super(source, jahiaUser, authValveContext);
        }
    }
}
