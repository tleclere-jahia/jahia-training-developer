import Viewer from 'bpmn-js/lib/NavigatedViewer.js';

import {layoutProcess} from 'bpmn-auto-layout';

import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css';

import diagram from './custom-workflow.bpmn2?raw';

const viewer = new Viewer({container: '#viewer'});

(async () => {
    const xmlWithLayout = await layoutProcess(diagram);
    try {
        const {warnings} = await viewer.importXML(xmlWithLayout);
        if (warnings.length) {
            console.log(warnings);
        }
        viewer.get('canvas').zoom('fit-viewport');
    } catch (err) {
        console.log(err);
    }
})();
