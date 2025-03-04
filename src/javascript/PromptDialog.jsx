import React, {useState} from "react";
import {Dialog, DialogActions, DialogContent, DialogTitle} from "@material-ui/core";
import {Button, Input, Textarea, Typography} from "@jahia/moonstone";

export default ({prompt, textarea, cancelButton, okButton, onCloseDialog}) => {
    const [input, setInput] = useState('');

    return <Dialog fullWidth={true} maxWidth={"sm"} open={true} onClose={onCloseDialog}>
        <DialogTitle>
            <Typography>{prompt}</Typography>
        </DialogTitle>
        <DialogContent>
            {textarea ?
                <Input onChange={e => setInput(e.target.value)}/> :
                <Textarea id={'prompt'} onChange={e => setInput(e.target.value)}/>
            }
        </DialogContent>
        <DialogActions>
            <Button label={cancelButton.label} color="danger" onClick={async () => {
                await cancelButton.onClick();
                onCloseDialog();
            }}/>,
            <Button label={okButton.label} onClick={async () => {
                await okButton.onClick(input);
                onCloseDialog();
            }}/>
        </DialogActions>
    </Dialog>;
};
