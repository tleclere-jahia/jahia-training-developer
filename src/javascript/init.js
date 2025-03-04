import React from "react";
import i18next from "i18next";
import {IframeRenderer, registry} from "@jahia/ui-extender";
import {MyComponent} from "./MyComponent";
import {ToggleBox} from "./ToggleBox";
import {HelpOutline} from "@jahia/moonstone";
import {nodeInfoDialogAction} from "./NodeInfoDialog";
import ColorPalette from "./ColorPalette";
import {autoSplitDialogAction} from "./AutoSplitDialogAction";

export default () => {
    registry.add('callback', 'jahia-training-developer', {
        targets: ['jahiaApp-init:5'],
        callback: async () => {
            await i18next.loadNamespaces('jahia-training-developer');

            registry.remove('adminRoute', 'files');

            registry.add('adminRoute', 'myComponent', {
                targets: ['jcontent:90'],
                label: 'jahia-training-developer:label.myComponent',
                isSelectable: true,
                render: () => <MyComponent/>
            });

            registry.add('action', '3dotsNodeInfoDialog', nodeInfoDialogAction, {
                buttonLabel: 'jahia-training-developer:label.nodeInfoDialog.title',
                targets: ['content-editor/header/3dots:-99.9'],
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

            const contentFoldersAccordionItem = registry.get('accordionItem', 'content-folders');
            registry.addOrReplace('accordionItem', 'content-folders', contentFoldersAccordionItem, {
                treeConfig: Object.assign({}, contentFoldersAccordionItem.treeConfig, {sortBy: null})
            });

            registry.add('selectorType', 'ToggleBox', {
                dataType: ['Boolean'],
                cmp: ToggleBox,
                supportMultiple: false,
                initValue: field => field.mandatory && !field.multiple ? false : undefined,
                adaptValue: (field, property) => field.multiple ? property.values.map(value => value === 'true') : property.value === 'true'
            });

            registry.add('selectorType', 'ColorPalette', {
                dataType: ['String'],
                cmp: ColorPalette,
                supportMultiple: true
            });

            registry.add('action', '3dotsAutoSplitDialog', autoSplitDialogAction, {
                buttonLabel: 'jahia-training-developer:label.autoSplitDialog.title',
                label: 'jahia-training-developer:label.autoSplitDialog.title',
                showOnNodeTypes: ['jnt:contentFolder'],
                hideOnExternal: true,
                targets: ['contentActions:9.9'],
            });
        }
    });
};
