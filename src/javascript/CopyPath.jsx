import React from 'react';
import * as PropTypes from 'prop-types';
import {useContentEditorContext} from '@jahia/jcontent';

const CopyPath = ({path, render: Render, ...otherProps}) => {
    const {nodeData} = useContentEditorContext();
    return <Render {...otherProps} onClick={() => navigator.clipboard.writeText(nodeData.path)}/>;
};

CopyPath.propTypes = {
    path: PropTypes.string.isRequired,
    render: PropTypes.func.isRequired
};

export const copyPathAction = {
    component: CopyPath
};
