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

        window.jahia.uiExtender.registry.add('accordionItem', 'lienservicepicker-accordionItem', window.jahia.jcontent.jcontentUtils.mergeDeep({}, window.jahia.uiExtender.registry.get('accordionItem', 'picker-content-folders'), {
            targets: [],
            rootPath: '/sites/systemsite/services-link-list',
            tableConfig: {
                viewSelector: null,
            }
        }));

        window.jahia.uiExtender.registry.add('pickerConfiguration', 'lienservicepicker', {
            showOnlyNodesWithTemplates: false,
            selectableTypesTable: ['jnt:text'],
            pickerDialog: {
                displaySiteSwitcher: true,
                displayTree: false,
                dialogTitle: 'jahia-training-developer:label.lienservicepicker.title',
                displaySearch: false
            },
            accordions: ['lienservicepicker-accordionItem'],
        });
    }
});
