<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:url var="action" value="${url.base}${renderContext.mainResource.node.path}.ajaxForm.do"/>
<form id="form-${currentNode.identifier}" method="post" action="${action}" onsubmit="return false;">
    <label>
        <input type="text" name="message"/>
    </label>
    <input type="submit" value="<fmt:message key="label.clickme"/>"/>
</form>
<template:addResources type="inlinejavascript">
    <script>
        document.addEventListener("DOMContentLoaded", () => {
            document.getElementById('form-${currentNode.identifier}').addEventListener('submit', e => {
                e.preventDefault();
                fetch(e.target.action, {
                    method: e.target.method,
                    headers: {
                        "Accept": "application/json",
                    },
                    body: new FormData(e.target)
                }).then(response => {
                    if (response.ok) {
                        return response.text();
                    }
                    return Promise.reject(response);
                }).then(data => console.log(data)).catch(error => console.warn(error));
                return false;
            });
        });
    </script>
</template:addResources>
