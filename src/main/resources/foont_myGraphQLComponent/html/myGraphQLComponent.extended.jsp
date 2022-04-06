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
<jcr:nodeProperty node="${currentNode}" name="employee" var="employee"/>

<h2><fmt:message key="foont_myGraphQLComponent"/></h2>
<div id="${currentNode.identifier}"></div>

<c:if test="${!renderContext.editMode && not empty employee && not empty employee.node}">
    <template:addCacheDependency node="${employee.node}"/>
    <template:addResources type="inlinejavascript">
        <script type="text/javascript">
            document.addEventListener("DOMContentLoaded", () => {
                fetch('/modules/graphql', {
                    method: 'POST',
                    body: JSON.stringify({
                        query: `query($path: String!) {
                            employee(path: $path) {
                                firstname,
                                lastname
                            }
                        }`,
                        variables: {path: '${employee.node.path}'}
                    })
                }).then(response => response.json()).then(data => {
                    alert('<fmt:message key="foont_employee.sayHiInAjax.success"/>'.replace('$firstname', data.data.employee.firstname).replace('$lastname', data.data.employee.lastname));
                    document.getElementById('${currentNode.identifier}').innerHTML = '<pre>' + JSON.stringify(data, null, 2) + '</pre>';
                }).catch(() => {
                    alert('<fmt:message key="foont_employee.sayHiInAjax.error"/>');
                });
            });
        </script>
    </template:addResources>
</c:if>
