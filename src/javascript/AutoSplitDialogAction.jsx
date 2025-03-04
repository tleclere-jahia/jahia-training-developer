import React, {useContext} from "react";
import {useTranslation} from "react-i18next";
import {ComponentRendererContext} from "@jahia/ui-extender";
import {useNodeChecks} from "@jahia/data-helper";
import {useContentEditorContext} from "@jahia/jcontent";
import PromptDialog from "./PromptDialog";
import {Loading} from "@jahia/moonstone";

export const autoSplitDialogAction = {
    component: ({render: Render, loading: Loading, label, showOnNodeTypes, hideOnExternal, ...otherProps}) => {
        const {t} = useTranslation('jahia-training-developer');
        const {path} = otherProps;
        const {render, destroy} = useContext(ComponentRendererContext);
        const {mode, nodeData, lang, language} = useContentEditorContext();
        const {loading, checksResult} = useNodeChecks(
            {path},
            {showOnNodeTypes: showOnNodeTypes, hideOnExternal: hideOnExternal}
        );

        if (loading) {
            return (Loading && <Loading buttonLabel={label} {...otherProps}/>) || <></>;
        }
        return <Render enabled={mode !== 'create'} {...otherProps} isVisible={checksResult} onClick={() => {
            render('AutoSplitDialog', PromptDialog, {
                prompt: t('label.autoSplitDialog.prompt'),
                textarea: false,
                cancelButton: {
                    label: t('label.autoSplitDialog.remove'),
                    onClick: async () => {
                        await (await fetch(`/modules/graphql`, {
                            method: 'POST',
                            body: JSON.stringify({
                                query: `mutation removeAutoSplit($path: String!) {
                                  jcr {
                                    mutateNode(pathOrId: $path) {
                                      removeMixins(mixins:["jmix:autoSplitFolders"])
                                    }
                                  }
                                }`,
                                variables: {path}
                            })
                        })).json();
                    }
                },
                okButton: {
                    label: t('label.autoSplitDialog.add'),
                    onClick: async input => {
                        console.log('input', input);
                        if (input !== undefined && input !== '') {
                            await (await fetch(`/modules/graphql`, {
                                method: 'POST',
                                body: JSON.stringify({
                                    query: `mutation addAutoSplit($path: String!, $splitConfiguration: String!) {
                                      jcr {
                                        mutateNode(pathOrId: $path) {
                                          enableAutoSplit(splitConfiguration: $splitConfiguration) {
                                              node {
                                                path
                                              }
                                          }
                                        }
                                      }
                                    }`,
                                    variables: {path, splitConfiguration: input}
                                })
                            })).json();
                        }
                    }
                },
                onCloseDialog: () => destroy('AutoSplitDialog')
            });
        }}/>;
    }
};
