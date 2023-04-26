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
<form id="formCategory" onsubmit="return false;">
    <div>
        <label>Email <input name="email" type="email"/></label>
        <input id="next" type="button" value="Next"/>
    </div>

    <div style="display: none">
        <jcr:node var="categories" path="/sites/systemsite/categories"/>
        <ul>
            <c:forEach items="${jcr:getChildrenOfType(categories, 'jnt:category')}" var="category">
                <template:addCacheDependency node="${category}"/>
                <li><label>
                    <input name="category" type="checkbox" value="${category.identifier}"/>
                    <jcr:nodeProperty node="${category}" name="jcr:title"/>
                </label></li>
            </c:forEach>
        </ul>
        <input id="subscribe" type="button" value="Subscribe"/>
    </div>
</form>
<template:addResources type="javascript" resources="categorySubscription.js"/>
