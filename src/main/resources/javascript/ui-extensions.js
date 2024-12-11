window.jahia.i18n.loadNamespaces('jahia-training-developer');

window.jahia.uiExtender.registry.add('callback', 'jahia-training-developer-ui-extensions', {
    targets: ['jahiaApp-init:6'],
    callback: () => {
        window.jahia.uiExtender.registry.add('adminRoute', 'portletsite', {
            targets: ['jcontent:40'],
            label: 'jahia-training-developer:label.portletmanager.title',
            isSelectable: true,
            requireModuleInstalledOnSite: 'jahia-portlet',
            render: ({match}) => window.jahia.ui.getIframeRenderer(`${window.contextJsParameters.contextPath}/engines/manager.jsp?conf=portletmanager-anthracite&lang=${match.params.lang}&site=${window.contextJsParameters.siteUuid}`)
        });

window.jahia.uiExtender.registry.add('pickerConfiguration', 'portlet', {
    selectableTypesTable: ['jnt:portlet'],
    accordions: ['picker-content-folders'],
    accordionItem: {
        "picker-content-folders": {
            rootPath: '/sites/{site}/portlets',
            treeConfig: {
                hideRoot: false
            },
            label: 'jahia-training-developer:label.portlets.title'
        }
    }
});
