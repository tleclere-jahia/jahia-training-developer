import {addChildOrFail, gqlQuery, setupTrainingSite, teardownTrainingSite} from "../support/training";

describe('Jahia Properties Taglib Test', () => {
    const siteKey = 'trainingTaglib';
    const contents = `/sites/${siteKey}/contents`;

    before(() => {
        setupTrainingSite(siteKey);
        addChildOrFail(contents, 'simple-list', 'foont:simpleList', [{name: 'itemType', value: 'foomix:textType'}], ['foomix:textType']);
    });
    after(() => teardownTrainingSite(siteKey));

    it('writes cluster.node.serverId from jahia.properties in the simple list view', () => {
        gqlQuery(`query($path: String!) {
            jcr { nodeByPath(path: $path) { renderedContent(contextConfiguration: "module", language: "en") { output } } }
        }`, {path: `${contents}/simple-list`}).should(result => {
            expect(result.errors).to.be.undefined;
            // The value depends on the server: "standalone" on a single node, the node id in a cluster.
            expect(result.data.jcr.nodeByPath.renderedContent.output.trim()).to.match(/^<!-- cluster\.node\.serverId: \S+ -->$/);
        });
    });
});
