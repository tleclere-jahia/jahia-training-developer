import React from "react";
import {Button, Dashboard} from "@jahia/moonstone";
import {Popover} from "@material-ui/core";
import {withStyles} from "@material-ui/core/styles";

const ColorPalette = ({classes, field, id, value, inputContext, onChange, onBlur}) => {
    const colors = ['#FF0000', '#00FF00', '#0000FF']
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
        <Popover PaperProps={classes.popover} open={open} anchorEl={anchorEl}
                 onClose={() => setAnchorEl(null)} anchorOrigin={{vertical: 'center', horizontal: 'right'}}
                 transformOrigin={{vertical: 'center', horizontal: 'left'}}>
            <div>
                <div className={classes.header} style={{backgroundColor: color}}>{color}</div>
                <div className={classes.footer}>
                    {colors.map(c => <div className={classes.selection} style={{backgroundColor: c}}
                                          onClick={() => handleColorSelected(c)}
                                          onTouchStart={() => handleColorSelected(c)}></div>)}
                </div>
            </div>
        </Popover>
    </>;
};
export default withStyles({
    popover: {
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
        margin: '5px 0 0 5px'
    }
})(ColorPalette);
