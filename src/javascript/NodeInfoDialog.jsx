import React, {useContext} from "react";
import {useTranslation} from "react-i18next";
import {Dialog, DialogActions, DialogContent, DialogTitle, List, ListItem, ListItemText} from "@material-ui/core";
import {ComponentRendererContext} from "@jahia/ui-extender";
import {Button, Typography} from "@jahia/moonstone";
import {useContentEditorContext} from "@jahia/jcontent";

const NodeInfoDialog = ({nodeData, onCloseDialog}) => {
    const {t} = useTranslation('jahia-training-developer');
    navigator.clipboard.writeText('');
    return <Dialog fullWidth={true} maxWidth={"sm"} open={true} onClose={onCloseDialog}>
        <DialogTitle>
            <Typography>{t('jahia-training-developer:label.nodeInfoDialog.title')}</Typography>
        </DialogTitle>
        <DialogContent>
            <List>
                <ListItem>
                    <ListItemText primary={t('jahia-training-developer:label.nodeInfoDialog.identifier')}
                                  secondary={nodeData.uuid}/>
                </ListItem>
                <ListItem>
                    <ListItemText primary={t('jahia-training-developer:label.nodeInfoDialog.path')}
                                  secondary={nodeData.path}/>
                </ListItem>
                <ListItem>
                    <ListItemText primary={t('jahia-training-developer:label.nodeInfoDialog.type')}
                                  secondary={`${nodeData.primaryNodeType.displayName} (${nodeData.primaryNodeType.name})`}/>
                </ListItem>
                <ListItem>
                    <ListItemText primary={t('jahia-training-developer:label.nodeInfoDialog.supertypes')}
                                  secondary={nodeData.primaryNodeType.supertypes?.map(type => type.name).join(', ')}/>
                </ListItem>
                <ListItem>
                    <ListItemText primary={t('jahia-training-developer:label.nodeInfoDialog.mixins')}
                                  secondary={nodeData.mixinTypes?.map(type => type.name).join(', ')}/>
                </ListItem>
            </List>
        </DialogContent>
        <DialogActions>
            <Button
                label={t('jahia-training-developer:label.nodeInfoDialog.pasteAndClose')}
                onClick={() => {
                    navigator.clipboard.writeText(nodeData.path);
                    onCloseDialog();
                }}/>
            <Button
                label={t('site-settings-seo:label.close')}
                onClick={() => onCloseDialog()}/>
        </DialogActions>
    </Dialog>;
}

export const nodeInfoDialogAction = {
    component: ({render: Render, ...otherProps}) => {
        const {render, destroy} = useContext(ComponentRendererContext);
        const {mode, nodeData} = useContentEditorContext();
        return <Render enabled={mode !== 'create'} {...otherProps} onClick={() => {
            render('NodeInfoDialog', NodeInfoDialog, {
                nodeData,
                onCloseDialog: () => destroy('NodeInfoDialog')
            });
        }}/>;
    }
};
