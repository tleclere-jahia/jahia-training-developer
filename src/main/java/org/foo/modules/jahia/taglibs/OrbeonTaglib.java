package org.foo.modules.jahia.taglibs;

import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.ClassLoaderUtils;
import org.jahia.utils.i18n.Messages;
import org.orbeon.oxf.fr.embedding.servlet.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

public final class OrbeonTaglib extends AbstractJahiaTag {
    private static final Logger logger = LoggerFactory.getLogger(OrbeonTaglib.class);
    private static final long serialVersionUID = -798515610114553269L;

    public static final String ORBEON_FORM_ATTRIBUTE = "orbeon.form-runner.remote-session-id";
    private static final String ERROR_MESSAGE = "foont_orbeonForm.error";

    private String app;
    private String form;
    private String mode;
    private String documentId;
    private String query;
    private Map<String, String> headers;

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public int doStartTag() {
        /**
         * <%
         *             org.orbeon.oxf.fr.embedding.servlet.API.embedFormJava(
         *                 request,                // HttpServletRequest: incoming HttpServletRequest
         *                 out,                    // Writer: where the embedded form is written
         *                 (String) pageContext.getAttribute("app"),           // String: Form Runner app name
         *                 (String) pageContext.getAttribute("form"),          // String: Form Runner form name
         *                 "new",            // String: Form Runner action name
         *                 null,        // String: Form Runner document id (optional)
         *                 null,                   // String: query string (optional)
         *                 (Map) pageContext.getAttribute("headers")              // Map<String, String>: custom HTTP headers (optional)
         *             );
         *         %>
         */
        ClassLoaderUtils.executeWith(getClass().getClassLoader(), () -> {
            try {
                API.embedFormJava((HttpServletRequest) pageContext.getRequest(), pageContext.getOut(), app, form, mode, documentId, query, headers);
                return true;
            } catch (ClassCastException e) {
                try {
                    pageContext.getOut().write(Messages.get(getResourceBundle(),
                            getCurrentResource().getScript(getRenderContext()).getView().getModule(),
                            ERROR_MESSAGE, getRenderContext().getMainResourceLocale()));
                } catch (RepositoryException | IOException ex) {
                    logger.error("", e);
                }
                pageContext.getSession().removeAttribute(ORBEON_FORM_ATTRIBUTE);
                return false;
            }
        });
        return SKIP_BODY;
    }

    @Override
    public int doEndTag() {
        resetState();
        return EVAL_PAGE;
    }

    @Override
    protected void resetState() {
        super.resetState();
        setApp(null);
        setForm(null);
        setMode(mode);
        setDocumentId(documentId);
        setQuery(query);
        setHeaders(headers);
    }
}
