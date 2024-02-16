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
<h2>Current session</h2>
<ul>
    <li>jcr:title: <jcr:nodeProperty node="${currentNode}" name="jcr:title"/></li>
    <li>protectedLink: <c:url value="${url.base}${currentNode.properties['protectedLink'].node.path}.html"/></li>
    <li>publicLink: <c:url value="${url.base}${currentNode.properties['publicLink'].node.path}.html"/></li>
</ul>

<h2>Elevated session</h2>
<jcr:xpath var="elevatedNodes" xpath="/jcr:root/${currentNode.path}" useRootUser="true"/>
<c:forEach items="${elevatedNodes.nodes}" var="item" begin="0" end="0">
    <c:set var="elevatedNode" value="${item}"/>
</c:forEach>
<ul>
    <li>jcr:title: <jcr:nodeProperty node="${elevatedNode}" name="jcr:title"/></li>
    <li>protectedLink: <c:url value="${url.base}${elevatedNode.properties['protectedLink'].node.path}.html"/></li>
    <li>publicLink: <c:url value="${url.base}${elevatedNode.properties['publicLink'].node.path}.html"/></li>
</ul>
