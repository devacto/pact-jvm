package au.com.dius.pact.consumer;

import static org.junit.Assert.assertEquals;

import au.com.dius.pact.model.MockProviderConfig;
import org.junit.Test;

import au.com.dius.pact.model.PactFragment;

public abstract class ConsumerPactTest {
    public static VerificationResult PACT_VERIFIED = PactVerified$.MODULE$;

    protected abstract PactFragment createFragment(ConsumerPactBuilder.PactDslWithProvider builder);
    protected abstract String providerName();
    protected abstract String consumerName();

    protected abstract void runTest(String url);

    @Test
    public void testPact() {
        PactFragment fragment = createFragment(ConsumerPactBuilder.consumer(consumerName()).hasPactWith(providerName()));
        final MockProviderConfig config = MockProviderConfig.createDefault();

        VerificationResult result = fragment.runConsumer(config, new TestRun() {
            public void run(MockProviderConfig config) {
                runTest(config.url());
            }
        });

        if (result instanceof PactError) {
            throw new RuntimeException(((PactError)result).error());
        }

        assertEquals(PACT_VERIFIED, result);
    }
}
