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
<%@ taglib prefix="jfunctions" uri="http://www.foo.org/jahia/tags/1.0" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="badgeNumber"/>
<jcr:nodeProperty node="${currentNode}" name="firstname" var="firstname"/>
<jcr:nodeProperty node="${currentNode}" name="lastname" var="lastname"/>
<c:url var="href" value="${currentNode.url}"/>
<%--- ${url.base}${currentNode.path}.html --%>
<div>
    <a href="${href}"><c:out value="${firstname} ${lastname} (#${badgeNumber})"/></a>
    <c:if test="${renderContext.previewMode || renderContext.liveMode}">
        <fmt:message key="foont_employee.fullname"/>: ${requestScope['fullname']}
    </c:if>
    <jfunctions:timeago date="${currentNode.properties['jcr:created'].date}" var="timeAgo"/>
    <c:out value="il y a ${timeAgo}"/>
</div>
<template:addResources type="javascript" resources="employee.js"/>