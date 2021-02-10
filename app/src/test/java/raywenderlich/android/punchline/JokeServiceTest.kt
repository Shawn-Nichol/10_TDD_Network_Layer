package raywenderlich.android.punchline

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.raywenderlich.android.punchline.Joke
import com.raywenderlich.android.punchline.JokeService
import com.raywenderlich.android.punchline.RepositoryImpl
import io.reactivex.Single
import junit.framework.Assert.assertEquals
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.coroutineContext


private const val id = "6"
private const val joke = "how does a train eat? It goes chew, chew"
class JokeServiceTest {

    @get:Rule
    val mockWebServer = MockWebServer()

    private val retrofit by lazy {
        Retrofit.Builder()
                // Set the baseUrl on the builder using the mockWebServer, this required when using retrofit
                // because we are not hitting the network.
                .baseUrl(mockWebServer.url("/"))
                // Add a call adapter. Using an RxJava call adapter allows you to return RxJava streams in your JokeService,
                // helping you handle the asynchronous nature of the network
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                // Add a converter factory. This is so you can use Gson to automatically convert the JSON to a nice kotlin object.
                .addConverterFactory(GsonConverterFactory.create())
                // Build it.
                .build()
    }

    private val jokeService by lazy {
        retrofit.create(JokeService::class.java)
    }

    // Triple quotes creates a raw string, by using Raw string you don't need to worry about escaping
    // characters such as the quotes around the JSON properties
    private val testJson = """{"id": $id, "joke": "$joke"}"""

    @Test
    fun getRandomJokeEmitsJoke() {
        // Use the mockWebServer that you created before to enqueue a response
        mockWebServer.enqueue(
                // You enqueue a response by building and passing in a MockResponse Object
                MockResponse()
                        // Use the testJson that you created as the Body response.
                        .setBody(testJson)
                        // Set the response code to 200 - Success.
                        .setResponseCode(200)
        )

        //By chaining test() you geta TestObserver that you can use to verify the value of the single
        // that getRandomJoke() returns
        val testObserver = jokeService.geteRandomJoke().test()
        // Verify that the value that returns is a joke object with the same values you placed in the
        // testJson and enqueued with MockWebServer.
        testObserver.assertValue(Joke(id, joke))
    }

    /**
     * Testing the endpoint
     */
    @Test
    fun getRandomJokeGetsRandomJokeJson() {
        // Use mockWebServer to Enqueue the response
        mockWebServer.enqueue(
                MockResponse()
                        .setBody(testJson)
                        .setResponseCode(200)
        )

        // Call getRandomJoke() getting a reference to a TestObserver.
        val testObserver = jokeService.geteRandomJoke().test()
        // You can also use the tests observer to make sure there were no errors emitted.
        testObserver.assertNoErrors()
        // Use mockWebServer to get the path that was requested to compare it to what you expect.
        assertEquals("/random_joke.json", mockWebServer.takeRequest().path)
    }

}



class JokeServiceTestUsingMockWebServer {

}

class JokeServiceTestMockingService {
    private val jokeService: JokeService = mock()
    private val repository = RepositoryImpl(jokeService)

    @Test
    fun getRandomJokeEmitsJoke() {
        // Create the Joke object
        val joke = Joke(id, joke)
        // Set up JokeService mock to return a Single with that Joke when getRandomJoke() is called.
        whenever(jokeService.geteRandomJoke()).thenReturn(Single.just(joke))
        // The signature of getJoke() on the repository also returns a single.
        val testObserver = repository.getJoke().test()
        // Asser that the repository's getJoke() emits the same joke that comes from JokeService().
        testObserver.assertValue(joke)
    }
}