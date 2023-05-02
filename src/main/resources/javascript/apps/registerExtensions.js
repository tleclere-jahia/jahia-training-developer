window.jahia.i18n.loadNamespaces('jahia-training-developer');

window.jahia.uiExtender.registry.add('callback', 'training', {
    targets: ['jahiaApp-init:90'],
    callback: () => {
        window.jahia.uiExtender.registry.add('action', '3dotsSampleAction', {
            buttonLabel: 'jahia-training-developer:label.contentActions.3dotsSampleAction',
            buttonIcon: window.jahia.moonstone.toIconComponent('Love'),
            targets: ['headerPrimaryActions:9999', 'content-editor/header/3dots:99'],
            requireModuleInstalledOnSite: 'jahia-training-developer',
            onClick: () => {
                fetch('/modules/graphql', {
                    method: 'POST',
                    body: JSON.stringify({
                        query: `mutation {
                          admin {
                            jahia {
                              configuration(pid: "org.jahia.modules.jcontent") {
                                value(name: "showPageComposer", value: "true")
                              }
                            }
                          }
                        }`
                    })
                }).then(response => response.json()).then(() => alert(window.jahia.i18n.t('jahia-training-developer:label.contentActions.success')))
                    .catch(e => console.error(window.jahia.i18n.t('jahia-training-developer:label.contentActions.error'), e));
            }
        });

        window.jahia.uiExtender.registry.add('route', 'toolsRoute', {
            targets: ['main:2'],
            requiredPermission: 'systemToolsAccess',
            path: '/tools',
            render: () => window.jahia.uiExtender.getIframeRenderer(`${window.contextJsParameters.contextPath}/tools`)
        });

        window.jahia.uiExtender.registry.add('primary-nav-item', 'toolsEntry', {
            targets: ['nav-root-top:9999'],
            icon: window.jahia.moonstone.toIconComponent('HelpOutline'),
            requiredPermission: 'systemToolsAccess',
            label: 'jahia-training-developer:label.developerTools.toolsEntry',
            path: '/tools'
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
