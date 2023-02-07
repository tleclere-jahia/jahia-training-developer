package org.foo.modules.jahia.taglibs;

import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.i18n.Messages;
import org.orbeon.oxf.fr.embedding.servlet.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;

public final class OrbeonTaglib extends AbstractJahiaTag {
    private static final Logger logger = LoggerFactory.getLogger(OrbeonTaglib.class);
    private static final long serialVersionUID = -798515610114553269L;

    private static final String SESSION_ATTRIBUTE = "orbeon.form-runner.remote-session-id";
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
    public int doEndTag() throws JspException {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            API.embedFormJava((HttpServletRequest) pageContext.getRequest(), pageContext.getOut(), app, form, mode, documentId, query, headers);
            return super.doEndTag();
        } catch (ClassCastException e) {
            try {
                pageContext.getOut().write(Messages.get(getResourceBundle(),
                        getCurrentResource().getScript(getRenderContext()).getView().getModule(),
                        ERROR_MESSAGE, getRenderContext().getMainResourceLocale()));
            } catch (RepositoryException | IOException ex) {
                logger.error("", e);
            }
            pageContext.getSession().removeAttribute(SESSION_ATTRIBUTE);
            return SKIP_BODY;
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }
}
