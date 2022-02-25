window.jahia.i18n.loadNamespaces('jahia-training-developer');

window.jahia.uiExtender.registry.add('callback', 'training', {
    targets: ['jahiaApp-init:90'],
    callback: () => {
        window.jahia.uiExtender.registry.add('action', '3dotsSampleAction', {
            buttonLabel: 'jahia-training-developer:label.contentActions.3dotsSampleAction',
            buttonIcon: window.jahia.moonstone.toIconComponent('Love'),
            targets: ['contentActions:9999'],
            onClick: () => {
                localStorage.setItem('jcontent_view_mode', 'view');
                location.reload();
            }
        });

        window.jahia.uiExtender.registry.add('adminRoute', 'toolsEntry', {
            targets: ['developerTools:9999'],
            icon: window.jahia.moonstone.toIconComponent('HelpOutline'),
            requiredPermission: 'systemToolsAccess',
            label: 'jahia-training-developer:label.developerTools.toolsEntry',
            isSelectable: true,
            iframeUrl: `${window.contextJsParameters.contextPath}/tools`
        });
    }
});
