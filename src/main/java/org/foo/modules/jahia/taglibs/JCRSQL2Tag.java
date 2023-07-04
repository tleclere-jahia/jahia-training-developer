package org.foo.modules.jahia.taglibs;

import org.apache.commons.lang3.StringUtils;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.taglibs.jcr.query.JCRSQLTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

public final class JCRSQL2Tag extends JCRSQLTag {
    private static final Logger logger = LoggerFactory.getLogger(JCRSQL2Tag.class);
    private static final long serialVersionUID = -5842291516923346680L;

    private boolean useRootUser = false;
    private int scope = PageContext.PAGE_SCOPE;
    private String var;
    private String statement;
    private long limit;
    private long offset;
    private String workspace;

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    public void setVar(String var) {
        this.var = var;
    }

    protected void setStatement(String statement) {
        this.statement = statement;
    }

    public void setSql(String sql) {
        setStatement(sql);
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    /**
     * Returns the type of the query language.
     *
     * @return the type of the query language
     */
    protected String getQueryLanguage() {
        return Query.JCR_SQL2;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public boolean isUseRootUser() {
        return useRootUser;
    }

    public void setUseRootUser(boolean useSystemUserSession) {
        this.useRootUser = useSystemUserSession;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    @Override
    protected String getWorkspace() throws RepositoryException {
        if (StringUtils.isNotEmpty(workspace)) {
            if (Constants.EDIT_WORKSPACE.equals(workspace) || Constants.LIVE_WORKSPACE.equals(workspace)) {
                return workspace;
            }
        }
        return super.getWorkspace();
    }

    @Override
    public int doEndTag() throws JspException {
        QueryResult result = null;
        JahiaUser userToReset = null;
        try {
            if (isUseRootUser()) {
                userToReset = JCRSessionFactory.getInstance().getCurrentUser();
                JCRSessionFactory.getInstance().setCurrentUser(JahiaUserManagerService.getInstance().lookupRootUser().getJahiaUser());
            }
            result = executeQuery(getJCRSession());

        } catch (RepositoryException e) {
            throw new JspTagException(e);
        } finally {
            if (userToReset != null) {
                JCRSessionFactory.getInstance().setCurrentUser(userToReset);
            }
        }
        pageContext.setAttribute(var, result, scope);
        resetState();
        return EVAL_PAGE;
    }

    /**
     * Executes the query.
     *
     * @return the QueryResult instance with the results of the query
     * @throws RepositoryException   in case of JCR errors
     * @throws InvalidQueryException in case of bad query statement
     */
    private QueryResult executeQuery(JCRSessionWrapper session) throws InvalidQueryException, RepositoryException {
        long startTime = System.currentTimeMillis();
        QueryResult queryResult = null;
        if (logger.isDebugEnabled()) {
            logger.debug("Executing " + getQueryLanguage() + " query: " + statement);
        }

        Query q = session.getWorkspace().getQueryManager().createQuery(statement, getQueryLanguage());
        if (limit > 0) {
            q.setLimit(limit);
        }
        if (offset > 0) {
            q.setOffset(offset);
        }
        // execute query
        queryResult = q.execute();
        if (logger.isDebugEnabled()) {
            logger.debug(getQueryLanguage() + " [" + statement + "] executed in " + (System.currentTimeMillis() - startTime) + " ms --> found [" + queryResult.getRows().getSize() + "] values.");
        }

        return queryResult;
    }

    @Override
    protected void resetState() {
        workspace = null;
        super.resetState();
    }
}
