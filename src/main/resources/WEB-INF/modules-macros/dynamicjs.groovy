import org.apache.commons.lang.StringUtils

currentNode.getPrimaryNodeType().getTemplatePackage().getResources("javascript").each { resource ->
    if (StringUtils.startsWith(resource.filename, "employee.")) print resource.filename
}
