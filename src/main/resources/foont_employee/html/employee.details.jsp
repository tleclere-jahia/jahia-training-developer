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
<%@ taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
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
<jcr:nodeProperty node="${currentNode}" name="nationality" var="nationality"/>
<jcr:nodeProperty node="${currentNode}" name="birthdate" var="birthdate"/>
<jcr:nodeProperty node="${currentNode}" name="biography" var="biography"/>
<jcr:nodeProperty node="${currentNode}" name="mainPhoto" var="mainPhoto"/>
<jcr:nodeProperty node="${currentNode}" name="supervisor" var="supervisor"/>

<dl>
    <dt><fmt:message key="foont_employee.jcr_title"/></dt>
    <dd>${badgeNumber.string}</dd>
    <dt><fmt:message key="foont_employee.firstname"/></dt>
    <dd>${firstname.string}</dd>
    <dt><fmt:message key="foont_employee.lastname"/></dt>
    <dd>${lastname.string}</dd>
    <c:if test="${not empty nationality}">
        <dt><fmt:message key="foont_employee.nationality"/></dt>
        <dd><jcr:nodePropertyRenderer node="${currentNode}" name="nationality" renderer="country"/></dd>
    </c:if>

    <c:if test="${not empty birthdate}">
        <dt><fmt:message key="foont_employee.birthdate"/></dt>
        <dd><fmt:formatDate value="${birthdate.date.time}" dateStyle="short" pattern="dd/MM/yyyy"/></dd>
    </c:if>

    <c:if test="${not empty biography}">
        <dt><fmt:message key="foont_employee.biography"/></dt>
        <dd>${biography.string}</dd>
    </c:if>

    <c:if test="${not empty mainPhoto && not empty mainPhoto.node}">
        <dt><fmt:message key="foont_employee.mainPhoto"/></dt>
        <dd><template:module node="${mainPhoto.node}" editable="false"/></dd>
    </c:if>

    <c:if test="${not empty supervisor && not empty supervisor.node}">
        <dt><fmt:message key="foont_employee.supervisor"/></dt>
        <dd><template:module node="${supervisor.node}" view="hidden.supervisor" editable="false">
            <template:param name="badgeEmployee" value="${currentNode.properties['jcr:title'].string}"/>
        </template:module></dd>
    </c:if>
</dl>

<template:addResources type="javascript" resources="employee.js"/>
<ul>
    <li>
        <c:set var="interests" value="null"/>
        <c:if test="${not empty currentNode.properties['wem:interests']}">
            <c:set var="interests">
                <json:object>
                    <c:forEach items="${currentNode.properties['wem:interests']}" var="interest">
                        <c:choose>
                            <c:when test="${fn:contains(interest, ':')}">
                                <c:set var="i" value="${fn:split(interest, ':')}"/>
                                <fmt:parseNumber value="${i[1]}" var="weight"/>
                                <json:property name="${fn:toLowerCase(fn:trim(i[0]))}" value="${weight}"/>
                            </c:when>
                            <c:when test="${not fn:contains(interest, ':')}">
                                <json:property name="${fn:toLowerCase(fn:trim(interest))}" value="${1}"/>
                            </c:when>
                        </c:choose>
                    </c:forEach>
                </json:object>
            </c:set>
        </c:if>
        <a href="#"
           onclick='return clickByInterest(event,
                   "${currentNode.identifier}",
                   "${currentNode.primaryNodeTypeName}",
                   ${interests},
                   "<fmt:message key="foont_employee.jExperienceMe.error"/>")'>
            <fmt:message key="foont_employee.jExperienceMe"/>
        </a>
    </li>
    <li>
        <c:url var="downloadUrl" value="${url.base}${currentNode.path}.vcf"/>
        <a href="${downloadUrl}"><fmt:message key="foont_employee.downloadVcf"/></a>
    </li>
    <c:if test="${jcr:hasPermission(currentNode, 'displayHi')}">
        <li>
            <c:url var="sayHiUrl" value="${url.base}${currentNode.path}.hi.do"/>
            <form action="${sayHiUrl}" method="post">
                <input type="submit" value="<fmt:message key="foont_employee.sayHi"/>"/>
            </form>
        </li>
        <c:if test="${jcr:hasPermission(currentNode, 'sayHi')}">
            <li>
                <c:url var="sayHiUrl" value="${url.base}${currentNode.path}.hi.do">
                    <c:param name="ajax" value="${true}"/>
                </c:url>
                <a href="#" onclick="return sayHi(event,
                        '${sayHiUrl}',
                        '<fmt:message key="foont_employee.sayHiInAjax.success"/>',
                        '<fmt:message key="foont_employee.sayHiInAjax.error"/>')">
                    <fmt:message key="foont_employee.sayHiInAjax"/>
                </a>
            </li>
        </c:if>
    </c:if>
</ul>
