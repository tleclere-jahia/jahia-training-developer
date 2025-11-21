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

        window.jahia.uiExtender.registry.add('accordionItem', 'picker-pages-jmixlink', window.jahia.jcontent.jcontentUtils.mergeDeep({}, window.jahia.uiExtender.registry.get('accordionItem', 'picker-pages'), {
            targets: [],
            label: 'Custom Page Picker',
            rootPath: '/sites/{site}',
            treeConfig: {
                hideRoot: false,
                openableTypes: ['jnt:page', 'jnt:navMenuText', 'jnt:virtualsite'],
                selectableTypes: ['jnt:page', 'jnt:navMenuText', 'jnt:virtualsite']
            },
            tableConfig: {
                defaultViewType: 'pages',
                /*viewSelector: false,*/
                openableTypes: ['jnt:page', 'jnt:navMenuText', 'jnt:virtualsite'],
                listTypes: ['jnt:page', 'jmix:link', 'jnt:file']
            },
            getPathForItem: node => {
                // Override getPathForItem to avoid crashing when selecting /sites/THE_SITE/home due to ancestors of home being a virtualsite.
                const pages = node.ancestors
                    .filter(n => n.primaryNodeType.name === 'jnt:page' || n.primaryNodeType.name === 'jnt:virtualsite');
                return pages[pages.length - 1].path;
            }
        }));

        window.jahia.uiExtender.registry.add('accordionItem', 'picker-media1', window.jahia.jcontent.jcontentUtils.mergeDeep({}, window.jahia.uiExtender.registry.get('accordionItem', 'picker-media'), {
            targets: [],
            label: 'Custom File Picker',
            rootPath: '/sites/{site}/files',
            treeConfig: {
                hideRoot: false,
                openableTypes: ['jnt:page', 'jnt:navMenuText', 'jnt:virtualsite', 'jnt:contentFolder', 'jnt:folder'],
                selectableTypes: ['jnt:page', 'jnt:navMenuText', 'jnt:virtualsite', 'jnt:contentFolder', 'jnt:folder']
            },
            tableConfig: {
                defaultViewType: 'pages',
                viewSelector: false,
                openableTypes: ['jnt:page', 'jnt:navMenuText', 'jnt:virtualsite', 'jnt:contentFolder', 'jnt:folder'],
                listTypes: ['jnt:page', 'jmix:link', 'jnt:file']
            }

        }));
        window.jahia.uiExtender.registry.add('pickerConfiguration', 'jmixlink', {
            showOnlyNodesWithTemplates: false,
            searchContentType: 'jmix:editorialContent',
            selectableTypesTable: ['jmix:link', 'jnt:page', 'jnt:file'],
            accordions: ['picker-pages-jmixlink', 'picker-media1']
        });

        window.jahia.uiExtender.registry.get('pickerConfiguration', 'editoriallink').showOnlyNodesWithTemplates = false;
        window.jahia.uiExtender.registry.get('pickerConfiguration', 'editoriallink').selectableTypesTable = ['jnt:page', 'jnt:navMenuText', 'jnt:virtualsite', 'jmix:link'];
        window.jahia.uiExtender.registry.get('accordionItem', 'picker-editoriallink').treeConfig.selectableTypes = ['jnt:page', 'jnt:navMenuText', 'jnt:virtualsite', 'jmix:link'];
        window.jahia.uiExtender.registry.get('accordionItem', 'picker-editoriallink').tableConfig.queryHandler.getTreeParams = options => {
            const {path, openPaths, openableTypes, selectableTypes, sort, hideRoot} = options;
            const treeParams = {
                rootPaths: [path],
                openPaths: [...new Set([path, ...openPaths])],
                selectedPaths: [],
                openableTypes,
                selectableTypes,
                hideRoot: hideRoot !== false,
                sortBy: sort.orderBy === '' ? null : {
                    sortType: sort.order === '' ? null : (sort.order === 'DESC' ? 'DESC' : 'ASC'),
                    fieldName: sort.orderBy === '' ? null : sort.orderBy,
                    ignoreCase: true
                }
            };

            if (options.tableView.viewType === 'pages') {
                treeParams.openableTypes = ['jmix:mainResource', 'jnt:page', 'jnt:navMenuText'];
                treeParams.selectableTypes = ['jnt:page', 'jmix:mainResource', 'jmix:link'];
            } else { // Content
                treeParams.openableTypes = ['jnt:contentFolder'];
                treeParams.selectableTypes = ['jmix:mainResource'];
            }

            treeParams.recursionTypesFilter = {multi: 'NONE', types: ['jmix:mainResource', 'jnt:contentFolder', 'jnt:page', 'jnt:folder', 'jnt:navMenuText']};

            return treeParams;
        };

        const baseAccordion = window.jahia.uiExtender.registry.get('accordionItem', 'pages');
        window.jahia.uiExtender.registry.add('accordionItem', 'customAccordionItem', // Custom accordion
            baseAccordion, // Extend pages accordion
            {
                // Custom properties specified here
                targets: ['jcontent:2'],
                label: 'Menu entries',
                icon: window.jahia.moonstone.toIconComponent('Section'),
                rootPath: '/sites/{site}',
                treeConfig: Object.assign({},
                    baseAccordion.treeConfig,
                    {
                        hideRoot: false,
                        selectableTypes: ['jnt:navMenuText'],
                        openableTypes: ['jnt:navMenuText'],
                    }),
                tableConfig: Object.assign({},
                    baseAccordion.tableConfig,
                    {
                        typeFilter:['jnt:navMenuText']
                    })
            });
    }
});
