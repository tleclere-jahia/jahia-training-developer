import {gqlQuery} from "../support/training";

describe('Platform integrations of the training module', () => {
    const rootAuth = () => ({Authorization: `Basic ${btoa(`root:${Cypress.env('SUPER_USER_PASSWORD')}`)}`});

    describe('JAX-RS application', () => {
        it('answers GET /modules/training/other', () => {
            cy.request('/modules/training/other').its('status').should('eq', 200);
        });

        it('proxies a GraphQL query on GET /modules/training/test', () => {
            cy.request({url: '/modules/training/test', headers: rootAuth()}).then(response => {
                expect(response.status).to.eq(200);
                expect(response.body).to.have.nested.property('data.jcr.nodesByCriteria.nodes');
            });
        });

        it('accepts a JSON body on POST /modules/training/test', () => {
            cy.request({
                method: 'POST',
                url: '/modules/training/test',
                headers: rootAuth(),
                body: {pathjahia: '/sites/digitall/contents', langue: 'fr', offset: 0, limit: 2}
            }).then(response => {
                expect(response.status).to.eq(200);
                expect(response.body).to.have.nested.property('data.jcr');
            });
        });
    });

    describe('GoneResourceErrorHandler', () => {
        it('answers 410 Gone on a configured path', () => {
            cy.request({url: '/cms/render/live/en/sites/digitall/test.html', failOnStatusCode: false})
                .its('status').should('eq', 410);
        });

        it('keeps 404 on a path that is not configured', () => {
            cy.request({url: '/cms/render/live/en/sites/digitall/not-gone-at-all.html', failOnStatusCode: false})
                .its('status').should('eq', 404);
        });
    });

    describe('Health check probes', () => {
        it('registers the module probes in server-availability-manager', () => {
            cy.request({url: '/modules/healthcheck?severity=LOW', headers: rootAuth(), failOnStatusCode: false}).then(response => {
                const probes = response.body.probes.filter(p => p.severity === 'LOW');
                const byName = Object.fromEntries(probes.map(p => [p.name, p]));
                expect(byName).to.have.all.keys('Jahia log size', 'Modules update');
                expect(byName['Jahia log size'].status.message).to.match(/^jahia\.log length: /);
                expect(byName['Jahia log size'].status.health).to.be.oneOf(['GREEN', 'YELLOW', 'RED']);
            });
        });
    });

    describe('Roles and permissions', () => {
        it('declares the sayHi and displayHi permissions', () => {
            gqlQuery(`{ jcr { nodesByQuery(query: "SELECT * FROM [jnt:permission] WHERE localname() = 'sayHi' OR localname() = 'displayHi'", queryLanguage: SQL2) { nodes { name path } } } }`)
                .then(result => {
                    const permissions = result.data.jcr.nodesByQuery.nodes;
                    expect(permissions.map(n => n.name).sort()).to.deep.eq(['displayHi', 'sayHi']);
                    permissions.forEach(p => expect(p.path).to.match(/^\/modules\/jahia-training-developer\/[^/]+\/permissions\/actions\//));
                });
        });

        it('grants the module permissions to the greetings and editor roles', () => {
            gqlQuery(`{ jcr {
                greetings: nodeByPath(path: "/roles/greetings") { privileged: property(name: "j:privilegedAccess") { value } permissions: property(name: "j:permissionNames") { values } }
                editor: nodeByPath(path: "/roles/editor") { permissions: property(name: "j:permissionNames") { values } }
            } }`).then(result => {
                const {greetings, editor} = result.data.jcr;
                expect(greetings.permissions.values).to.include.members(['jcr:all_default', 'sayHi', 'displayHi']);
                expect(greetings.privileged.value).to.eq('true');
                expect(editor.permissions.values).to.include('sayHi');
            });
        });
    });

    describe('Imported content', () => {
        it('imports the training categories into the system site', () => {
            gqlQuery(`{ jcr { nodeByPath(path: "/sites/systemsite/categories/training") {
                title: property(name: "jcr:title", language: "en") { value }
                children { nodes { name } }
            } } }`).then(result => {
                expect(result.data.jcr.nodeByPath.title.value).to.eq('Training');
                expect(result.data.jcr.nodeByPath.children.nodes.map(n => n.name)).to.include('subcategory');
            });
        });
    });
});
