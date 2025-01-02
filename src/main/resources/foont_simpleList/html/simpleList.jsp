<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jfunctions" uri="http://www.foo.org/jahia/tags/1.0" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:set var="resourceReadOnly" value="${currentResource.moduleParams.readOnly}"/>
<template:include view="hidden.header"/>
<c:set var="isEmpty" value="true"/>
<c:forEach items="${moduleMap.currentList}" var="subchild" begin="${moduleMap.begin}" end="${moduleMap.end}">
    <template:module node="${subchild}" view="${moduleMap.subNodesView}"
                     editable="${moduleMap.editable && !resourceReadOnly}">
        <c:if test="${jcr:isNodeType(currentNode, 'foomix:dateType,foomix:datetimeType')}">
            <template:param name="pattern" value="${currentNode.properties['pattern'].string}"/>
        </c:if>
    </template:module>
    <c:set var="isEmpty" value="false"/>
</c:forEach>
<c:if test="${fn:toLowerCase(omitFormatting) eq 'false'}">
    <div class="clear"></div>
</c:if>
<c:if test="${not empty moduleMap.emptyListMessage and (renderContext.editMode or moduleMap.forceEmptyListMessageDisplay) and isEmpty}">
    ${moduleMap.emptyListMessage}
</c:if>
<c:if test="${moduleMap.editable and renderContext.editMode && !resourceReadOnly}">
    <template:module path="*"/>
</c:if>

<jfunctions:osgiconfiguration factoryPid="org.jahia.modules.jahiacsrfguard" property="whitelist"
                              varIsMultivalued="isMultiValued" var="whitelist"/>
<utility:logger level="info" value="Whitelist: ${whitelist}"/>
<jfunctions:osgiconfiguration pid="org.foo.modules.jahiatrainingdeveloper" property="training.description" var="description"/>
<utility:logger level="info" value="training.description=${description}"/>
<jfunctions:properties property="cluster.node.serverId" var="nodeId"/>
<utility:logger level="info" value="NodeID: ${nodeId}"/>
<!-- cluster.node.serverId: ${nodeId} -->
