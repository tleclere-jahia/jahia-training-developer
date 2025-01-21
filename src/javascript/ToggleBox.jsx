import React from 'react';
import {Toggle} from '@jahia/design-system-kit';
import {useTranslation} from 'react-i18next';

export const ToggleBox = ({field, value, id, onChange, onBlur}) => {
    const {t} = useTranslation('jahia-training-developer');

    return <>
        <Toggle
            id={id}
            checked={value === true}
            readOnly={field.readOnly}
            disabled={field.readOnly}
            onChange={() => onChange(!value)}
            onBlur={onBlur}
        />{value ? t('label.togglebox.enabled') : t('label.togglebox.disabled')}
    </>;
};
