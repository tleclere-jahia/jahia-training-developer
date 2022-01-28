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
<c:set var="jobTitle" value="${currentNode.properties['jobTitle']}"/>
<c:set var="email" value="${currentNode.properties['email']}"/>
<c:set var="phone" value="${currentNode.properties['phone']}"/>

BEGIN:VCARD
FN:<c:out
        value="${currentNode.properties['firstname'].string} ${currentNode.properties['lastname'].string} (${currentNode.properties['jcr:title'].string})"/>
N:<c:out value="${currentNode.properties['lastname'].string};${currentNode.properties['firstname'].string}"/>
<c:if test="${not empty jobTitle}">
    TITLE:<jcr:nodePropertyRenderer node="${currentNode}" name="jobTitle" renderer="resourceBundle"/>
</c:if>
<c:if test="${not empty email}">EMAIL:${email.string}</c:if>
<c:if test="${not empty phone}">TEL;TYPE=${phone.string}</c:if>
END:VCARD
