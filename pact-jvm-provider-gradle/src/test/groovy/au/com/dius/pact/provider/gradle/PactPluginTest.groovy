package au.com.dius.pact.provider.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class PactPluginTest {

    private PactPlugin plugin
    private Project project

    @Before
    public void setup() {
        project = ProjectBuilder.builder().build()
        plugin = new PactPlugin()
        plugin.apply(project)
    }

    @Test
    public void 'defines a pactVerify task'() {
        assert project.tasks.pactVerify
    }

    @Test
    public void 'defines a task for each defined provider'() {
        project.pact {
            serviceProviders {
                provider1 {

                }

                provider2 {

                }
            }
        }

        project.evaluate()

        assert project.tasks.pactVerify_provider1
        assert project.tasks.pactVerify_provider2
    }

    @Test
    public void 'defines a task for each file in the pact file directory'() {
        def resource = getClass().classLoader.getResource('pacts/foo_pact.json')
        File pactFileDirectory = new File(resource.file).parentFile
        project.pact {
            serviceProviders {
                provider1 {
                    hasPactsWith("many consumers") {
                        pactFileLocation = project.file("${pactFileDirectory.absolutePath}")
                        stateChange = "http://localhost:8080/state"
                    }
                }
            }
        }
        project.evaluate()


        def consumers = project.tasks.pactVerify_provider1.providerToVerify.consumers
        assert consumers.size() == 2
        assert consumers.find { it.name == 'Foo Consumer'}
        assert consumers.find { it.name == 'Bar Consumer'}
    }

    @Test
    public void 'configures the providers and consumers correctly'() {
        def pactFileUrl = 'http://localhost:8000/pacts/provider/prividera/consumer/consumera/latest'
        def stateChangeUrl = 'http://localhost:8080/stateChange'
        project.pact {
            serviceProviders {
                ProviderA { providerInfo ->
                    startProviderTask = 'jettyEclipseRun'
                    terminateProviderTask = 'jettyEclipseStop'

                    port = 1234

                    hasPactWith('ConsumerA') {
                        pactFile = url(pactFileUrl)
                        stateChange = url(stateChangeUrl)
                    }
                }
            }
        }

        project.evaluate()

        def provider = project.tasks.pactVerify_ProviderA.providerToVerify
        assert provider.startProviderTask == 'jettyEclipseRun'
        assert provider.terminateProviderTask == 'jettyEclipseStop'
        assert provider.port == 1234

        def consumer = provider.consumers.first()
        assert consumer.name == 'ConsumerA'
        assert consumer.pactFile == new URL(pactFileUrl)
        assert consumer.stateChange == new URL(stateChangeUrl)
    }

    @Test
    public void 'do not set the state change url automatically'() {
        def pactFileUrl = 'http://localhost:8000/pacts/provider/prividera/consumer/consumera/latest'
        project.pact {
            serviceProviders {
                ProviderA { providerInfo ->
                    hasPactWith('ConsumerA') {
                        pactFile = url(pactFileUrl)
                    }
                }
            }
        }

        project.evaluate()

        def consumer = project.tasks.pactVerify_ProviderA.providerToVerify.consumers.first()
        assert consumer.pactFile == new URL(pactFileUrl)
        assert consumer.stateChange == null
    }
}
