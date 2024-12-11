import React from 'react';
import {Switch} from '@jahia/moonstone';

export const ToggleBox = ({field, value, id, onChange, onBlur}) => <Switch
    id={id}
    value={value}
    checked={value === true}
    isReadOnly={field.readOnly}
    isDisabled={field.readOnly}
    onChange={() => onChange(!value)}
    onBlur={onBlur}
/>;
