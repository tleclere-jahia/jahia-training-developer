import {addChild, addChildOrFail, expectServerRejection, serverName, setupTrainingSite, teardownTrainingSite} from "../support/training";

describe('JCR node validators', () => {
    const siteKey = 'trainingValidators';
    const contents = `/sites/${siteKey}/contents`;

    before(() => setupTrainingSite(siteKey));
    after(() => teardownTrainingSite(siteKey));

    describe('UniqueTitle on foont:uniqueTitle', () => {
        before(() => addChildOrFail(contents, 'unique-ref', 'foont:uniqueTitle', [{name: 'jcr:title', value: 'same-key'}]));

        it('accepts a sibling with a different title', () => {
            addChild(contents, 'unique-other', 'foont:uniqueTitle', [{name: 'jcr:title', value: 'other-key'}])
                .should(result => {
                    expect(result?.errors).to.be.undefined;
                    expect(result?.data?.jcr?.addNode?.uuid).to.be.a('string');
                });
        });

        it('rejects a sibling with the same title', () => {
            addChild(contents, 'unique-dup', 'foont:uniqueTitle', [{name: 'jcr:title', value: 'same-key'}])
                .should(result => expectServerRejection(result, 'jcr:title'));
        });

        it('accepts the same title under another parent', () => {
            addChildOrFail(contents, 'unique-folder', 'jnt:contentFolder');
            addChild(`${contents}/unique-folder`, 'unique-elsewhere', 'foont:uniqueTitle', [{name: 'jcr:title', value: 'same-key'}])
                .should(result => expect(result?.errors).to.be.undefined);
        });
    });

    describe('InternalHost on foomix:richText', () => {
        it('accepts a rich text without any link', () => {
            addChild(contents, 'text-no-link', 'foont:text', [{name: 'text', language: 'en', value: '<p>no link</p>'}])
                .should(result => expect(result?.errors).to.be.undefined);
        });

        it('accepts a link to an external host', () => {
            addChild(contents, 'text-external-link', 'foont:text', [{
                name: 'text',
                language: 'en',
                value: '<p><a href="https://www.example.org/page">external</a></p>'
            }]).should(result => expect(result?.errors).to.be.undefined);
        });

        it('accepts a relative link to a page of the site, stored as an internal reference', () => {
            addChild(contents, 'text-relative-link', 'foont:text', [{
                name: 'text',
                value: `<p><a href="/cms/render/default/en/sites/${siteKey}/home.html">home</a></p>`
            }]).should(result => expect(result?.errors).to.be.undefined);
        });

        it('rejects a link to the server name of a site', () => {
            addChild(contents, 'text-internal-link', 'foont:text', [{
                name: 'text',
                language: 'en',
                value: `<p><a href="http://${serverName(siteKey)}/sites/${siteKey}/home.html">internal</a></p>`
            }]).should(result => expectServerRejection(result, 'text'));
        });

        it('rejects an image served from the server name of a site', () => {
            addChild(contents, 'text-internal-img', 'foont:text', [{
                name: 'text',
                language: 'en',
                value: `<p><img src="http://${serverName(siteKey)}/files/live/image.png"/></p>`
            }]).should(result => expectServerRejection(result));
        });
    });

    describe('Property constraints on foont:employee', () => {
        const employee = (name: string, firstname: string, email?: string) => addChild(contents, name, 'foont:employee', [
            {name: 'jcr:title', value: name},
            {name: 'firstname', value: firstname},
            {name: 'lastname', value: 'Doe'},
            ...(email === undefined ? [] : [{name: 'email', value: email}])
        ]);

        it('accepts a valid employee', () => {
            employee('valid-employee', 'John', 'john.doe@example.org')
                .should(result => expect(result?.errors).to.be.undefined);
        });

        it('rejects a firstname containing a digit', () => {
            employee('digit-employee', 'J0hn').should(result => expectServerRejection(result));
        });

        it('rejects a malformed email', () => {
            employee('bad-email-employee', 'John', 'not-an-email')
                .should(result => expectServerRejection(result));
        });

        it('rejects an employee without the mandatory lastname', () => {
            addChild(contents, 'no-lastname', 'foont:employee', [
                {name: 'jcr:title', value: 'no-lastname'},
                {name: 'firstname', value: 'John'}
            ]).should(result => expectServerRejection(result));
        });
    });
});
