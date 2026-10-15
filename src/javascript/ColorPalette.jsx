import React from "react";
import {Button, Dashboard} from "@jahia/moonstone";
import {Popover} from "@material-ui/core";

// Inline styles, not withStyles: this module bundles its own Material-UI, whose generated class names
// (jss1, jss2...) collide with the ones of the host application and lose their rules.
const styles = {
    paper: {
        backgroundColor: 'transparent',
        boxShadow: 'none'
    },
    header: {
        padding: '1.15em',
        color: 'white',
        textAlign: 'center'
    },
    footer: {
        display: 'flex',
        paddingBottom: '1.15em',
        width: '200px'
    },
    selection: {
        width: '25px',
        height: '25px',
        borderRadius: '5px',
        margin: '5px 0 0 5px',
        cursor: 'pointer'
    }
};

const ColorPalette = ({field, id, value, inputContext, onChange, onBlur}) => {
    const colors = field.valueConstraints?.map(v => v.displayValue) || [];
    const [color, setColor] = React.useState(value);
    const [anchorEl, setAnchorEl] = React.useState(null);
    const open = Boolean(anchorEl);

    const handleColorSelected = c => {
        setColor(c);
        onChange(c);
        setAnchorEl(null);
    };
    return <>
        <Button label={color} icon={<Dashboard style={{backgroundColor: color}}/>}
                onClick={e => setAnchorEl(e.currentTarget)}/>
        <Popover PaperProps={{style: styles.paper}} open={open} anchorEl={anchorEl}
                 onClose={() => setAnchorEl(null)} anchorOrigin={{vertical: 'center', horizontal: 'right'}}
                 transformOrigin={{vertical: 'center', horizontal: 'left'}}>
            <div>
                <div style={{...styles.header, backgroundColor: color}}>{color}</div>
                <div style={styles.footer}>
                    {colors.map(c => <div key={c} style={{...styles.selection, backgroundColor: c}}
                                          onClick={() => handleColorSelected(c)}
                                          onTouchStart={() => handleColorSelected(c)}></div>)}
                </div>
            </div>
        </Popover>
    </>;
};

export default ColorPalette;
