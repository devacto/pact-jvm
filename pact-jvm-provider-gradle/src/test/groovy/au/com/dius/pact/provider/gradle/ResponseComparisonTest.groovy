package au.com.dius.pact.provider.gradle

import au.com.dius.pact.model.Response
import au.com.dius.pact.model.Response$
import au.com.dius.pact.provider.groovysupport.ResponseComparison
import org.apache.http.entity.ContentType
import org.codehaus.groovy.runtime.powerassert.PowerAssertionError
import org.junit.Before
import org.junit.Test

class ResponseComparisonTest {

  Closure<Map> testSubject
  Response response
  def actualResponse
  int actualStatus
  Map actualHeaders = ['A': 'B', 'C': 'D', 'Content-Type': 'application/json']
  def actualBody

  @Before
  void setup() {
    response = Response$.MODULE$.apply(200, ['Content-Type': 'application/json'], '{"stuff": "is good"}', [:])
    actualStatus = 200
    actualBody = '{"stuff": "is good"}'
    actualResponse = [contentType: ContentType.APPLICATION_JSON]
    testSubject = { ResponseComparison.compareResponse(response, actualResponse, actualStatus, actualHeaders, actualBody) }
  }

  @Test
  void 'compare the status should, well, compare the status'() {
    assert testSubject().method == true
    actualStatus = 400
    assert testSubject().method instanceof PowerAssertionError
  }

  @Test
  void 'should not compare headers if there are no expected headers'() {
    response = Response$.MODULE$.apply(200, [:], "", [:])
    assert testSubject().headers == [:]
  }

  @Test
  void 'should only compare the expected headers'() {
    actualHeaders = ['A': 'B', 'C': 'D']
    response = Response$.MODULE$.apply(200, ['A': 'B'], "", [:])
    assert testSubject().headers == ['A': true]
    response = Response$.MODULE$.apply(200, ['A': 'D'], "", [:])
    assert testSubject().headers.A instanceof PowerAssertionError
  }

  @Test
  void 'ignores case in header comparisons'() {
    actualHeaders = ['A': 'B', 'C': 'D']
    response = Response$.MODULE$.apply(200, ['a': 'B'], "", [:])
    assert testSubject().headers == ['a': true]
  }

  @Test
  void 'comparing bodies should fail with different content types'() {
    actualHeaders['Content-Type'] = 'text/plain'
    assert testSubject().body == [comparison: "Expected a response type of 'application/json' but the actual type was 'text/plain'"]
  }

  @Test
  void 'comparing bodies should pass with the same content types and body contents'() {
    assert testSubject().body == [:]
  }

  @Test
  void 'comparing bodies should show all the differences'() {
    actualBody = '{"stuff": "should make the test fail"}'
    def result = testSubject().body
    assert result.comparison == ['$.body.stuff': "Expected 'is good' but received 'should make the test fail'"]
    assert result.diff[0] == '@1'
    assert result.diff[1] == '-    "stuff": "is good"'
    assert result.diff[2] == '+    "stuff": "should make the test fail"'
  }
}
