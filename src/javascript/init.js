import React from "react";
import i18next from "i18next";
import {IframeRenderer, registry} from "@jahia/ui-extender";
import MyComponent from "./mycomponent/MyComponent";
import {HelpOutline, Love} from "@jahia/moonstone";

export default function () {
    registry.add('callback', 'jahia-training-developer', {
        targets: ['jahiaApp-init:5'],
        callback: async () => {
            await i18next.loadNamespaces('jahia-training-developer');

            registry.add('adminRoute', 'myComponent', {
                targets: ['jcontent:90'],
                label: 'jahia-training-developer:label.myComponent',
                isSelectable: true,
                render: () => <MyComponent/>
            });

            registry.add('action', '3dotsSampleAction', {
                buttonLabel: 'jahia-training-developer:label.contentActions.3dotsSampleAction',
                buttonIcon: <Love/>,
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

            registry.add('route', 'toolsRoute', {
                targets: ['main:2'],
                requiredPermission: 'systemToolsAccess',
                path: '/tools',
                render: () => <IframeRenderer url={`${window.contextJsParameters.contextPath}/tools`}/>
            });

            registry.add('primary-nav-item', 'toolsEntry', {
                targets: ['nav-root-top:9999'],
                icon: <HelpOutline/>,
                requiredPermission: 'systemToolsAccess',
                label: 'jahia-training-developer:label.developerTools.toolsEntry',
                path: '/tools'
            });

            registry.add('adminRoute', 'toolsEntry', {
                targets: ['developerTools:9999'],
                icon: <HelpOutline/>,
                requiredPermission: 'systemToolsAccess',
                label: 'jahia-training-developer:label.developerTools.toolsEntry',
                isSelectable: true,
                iframeUrl: `${window.contextJsParameters.contextPath}/tools`
            });
        }
    });
};
