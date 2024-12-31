window.jahia.i18n.loadNamespaces('jahia-training-developer');

const getRootPath = async () => {
    try {
        const json = await (await fetch(`/modules/graphql`, {
            method: 'POST',
            body: JSON.stringify({
                query: `query getSiteOption($sitePath:String!) {
                                    jcr {
                                        nodeByPath(path: $sitePath) {
                                            property(name: "listeLiensServiceAdmin") {
                                                refNode { 
                                                    path
                                                }
                                            }
                                        }
                                    }
                                }`,
                variables: {sitePath: `/sites/systemsite`}
            })
        })).json();
        return json.data?.jcr?.nodeByPath?.property?.refNode?.path || '/sites/{site}';
    } catch (e) {
        console.error(e);
    }
    return '/sites/{site}';
};

window.jahia.uiExtender.registry.add('callback', 'jahia-training-developer-ui-extensions', {
    targets: ['jahiaApp-init:6'],
    callback: async () => {
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

        const rootPath = await getRootPath();
        window.jahia.uiExtender.registry.add('accordionItem', 'lienservicepicker-accordionItem', window.jahia.jcontent.jcontentUtils.mergeDeep({}, window.jahia.uiExtender.registry.get('accordionItem', 'picker-content-folders'), {
            targets: [],
            rootPath: rootPath,
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
