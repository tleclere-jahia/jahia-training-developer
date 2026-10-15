import {nodeProperties, propertyValue, setupTrainingSite, teardownTrainingSite} from "../support/training";

describe('importNode provisioning operation', () => {
    const siteKey = 'trainingProvisioning';
    const contents = `/sites/${siteKey}/contents`;

    before(() => setupTrainingSite(siteKey));
    after(() => teardownTrainingSite(siteKey));

    // Like a Jahia subtree export, the root element of repository.xml is the imported node itself.
    it('imports a zip sent with the provisioning script under rootPath', () => {
        cy.runProvisioningScript({
            script: [{importNode: 'importNode.zip', rootPath: contents}],
            files: [{fileName: 'importNode.zip', type: 'application/zip'}]
        });
        nodeProperties(`${contents}/imported-by-provisioning/provisioned-text`, ['text']).should(result => {
            expect(result.errors, JSON.stringify(result.errors)).to.be.undefined;
            expect(propertyValue(result.data.jcr.nodeByPath, 'text')).to.eq('<p>provisioned</p>');
        });
    });
});
