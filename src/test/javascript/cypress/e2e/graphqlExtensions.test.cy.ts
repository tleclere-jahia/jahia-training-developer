import {publishAndWaitJobEnding} from "@jahia/cypress";
import {
    addChild,
    addChildOrFail,
    expectServerRejection,
    gqlMutation,
    gqlQuery,
    nodeProperties,
    setupTrainingSite,
    teardownTrainingSite
} from "../support/training";

describe('GraphQL extensions of the training module', () => {
    const siteKey = 'trainingGraphql';
    const contents = `/sites/${siteKey}/contents`;
    const employeePath = `${contents}/ada`;

    before(() => {
        setupTrainingSite(siteKey, ['en', 'fr']);
        addChildOrFail(contents, 'ada', 'foont:employee', [
            {name: 'jcr:title', value: '42'},
            {name: 'firstname', value: 'Ada'},
            {name: 'lastname', value: 'Lovelace'},
            {name: 'jobTitle', value: 'developer'},
            {name: 'jcr:description', language: 'en', value: 'First programmer'}
        ]);
        addChildOrFail(contents, 'split-folder', 'jnt:contentFolder');
        addChildOrFail(contents, 'plain-text', 'foont:text', [{name: 'text', language: 'en', value: '<p>plain</p>'}]);
        publishAndWaitJobEnding(contents, ['en', 'fr']);
        addChildOrFail(contents, 'draft', 'foont:employee', [
            {name: 'jcr:title', value: '43'},
            {name: 'firstname', value: 'Grace'},
            {name: 'lastname', value: 'Hopper'}
        ]);
    });
    after(() => teardownTrainingSite(siteKey));

    describe('Query.employee', () => {
        const employee = (path: string) => gqlQuery(`query($path: String!) {
            employee(path: $path) { firstname lastname fullName }
        }`, {path});

        it('returns a published employee from the live workspace', () => {
            employee(employeePath).should(result => {
                expect(result.errors).to.be.undefined;
                expect(result.data.employee).to.deep.eq({
                    firstname: 'Ada',
                    lastname: 'Lovelace',
                    fullName: 'Ada Lovelace',
                    __typename: 'GqlEmployee'
                });
            });
        });

        it('returns null for an employee that is not published', () => {
            employee(`${contents}/draft`).should(result => {
                expect(result.errors).to.be.undefined;
                expect(result.data.employee).to.be.null;
            });
        });

        it('returns null for a path that does not exist', () => {
            employee(`${contents}/does-not-exist`).should(result => {
                expect(result.errors).to.be.undefined;
                expect(result.data.employee).to.be.null;
            });
        });

        it('fails on a node that is not an employee', () => {
            employee(`${contents}/plain-text`).should(result => expectServerRejection(result, 'Invalid nodetype'));
        });
    });

    describe('JCRNode.propertyValue', () => {
        it('returns the value of a property', () => {
            gqlQuery(`query($path: String!) {
                jcr { nodeByPath(path: $path) {
                    firstname: propertyValue(name: "firstname")
                    description: propertyValue(name: "jcr:description", language: "en")
                } }
            }`, {path: employeePath}).should(result => {
                expect(result.errors).to.be.undefined;
                expect(result.data.jcr.nodeByPath.firstname).to.eq('Ada');
                expect(result.data.jcr.nodeByPath.description).to.eq('First programmer');
            });
        });

        it('fails when the property is missing', () => {
            gqlQuery(`query($path: String!) {
                jcr { nodeByPath(path: $path) { propertyValue(name: "phone") } }
            }`, {path: employeePath}).should(result => expectServerRejection(result, 'Mandatory property: phone'));
        });
    });

    describe('JCRProperty.resourceBundleValue', () => {
        it('translates a choicelist value with the module resource bundle', () => {
            gqlQuery(`query($path: String!) {
                jcr { nodeByPath(path: $path) {
                    property(name: "jobTitle") { value en: resourceBundleValue(language: "en") }
                } }
            }`, {path: employeePath}).should(result => {
                expect(result.errors).to.be.undefined;
                expect(result.data.jcr.nodeByPath.property.value).to.eq('developer');
                expect(result.data.jcr.nodeByPath.property.en).to.eq('Developer');
            });
        });
    });

    describe('JCRNodeMutation.enableAutoSplit', () => {
        const enableAutoSplit = (path: string) => gqlMutation(`mutation($path: String!) {
            jcr { mutateNode(pathOrId: $path) { enableAutoSplit(splitConfiguration: "firstChars,j:nodename,1") { uuid } } }
        }`, {path});

        it('enables the auto split on a content folder', () => {
            enableAutoSplit(`${contents}/split-folder`).should(result => expect(result.errors).to.be.undefined);
            nodeProperties(`${contents}/split-folder`, ['j:splitConfig', 'j:splitNodeType']).should(result => {
                const node = result.data.jcr.nodeByPath;
                expect(node.mixinTypes.map(m => m.name)).to.include('jmix:autoSplitFolders');
                expect(node.properties.find(p => p.name === 'j:splitConfig')?.value).to.eq('firstChars,j:nodename,1');
                expect(node.properties.find(p => p.name === 'j:splitNodeType')?.value).to.eq('jnt:contentFolder');
            });
        });

        it('moves a new child into a split sub folder', () => {
            addChild(`${contents}/split-folder`, 'bob', 'foont:text', [{name: 'text', language: 'en', value: '<p>bob</p>'}])
                .should(result => expect(result.errors).to.be.undefined);
            gqlQuery(`query($path: String!) {
                jcr { nodesByQuery(query: $path, queryLanguage: SQL2) { nodes { path } } }
            }`, {path: `SELECT * FROM [foont:text] WHERE ISDESCENDANTNODE('${contents}/split-folder') AND NAME() = 'bob'`})
                .should(result => {
                    const paths = result.data.jcr.nodesByQuery.nodes.map(n => n.path);
                    expect(paths).to.deep.eq([`${contents}/split-folder/b/bob`]);
                });
        });

        it('does nothing on a node that is not a content folder', () => {
            enableAutoSplit(`${contents}/plain-text`).should(result => expect(result.errors).to.be.undefined);
            nodeProperties(`${contents}/plain-text`, []).should(result => {
                expect(result.data.jcr.nodeByPath.mixinTypes.map(m => m.name)).not.to.include('jmix:autoSplitFolders');
            });
        });
    });
});
