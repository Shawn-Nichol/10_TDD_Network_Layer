package raywenderlich.android.punchline

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.raywenderlich.android.punchline.Joke
import com.raywenderlich.android.punchline.JokeService
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

    private val jokService by lazy {
        retrofit.create(JokeService::class.java)
    }

    // Triple quotes creates a raw string, by using Raw string you don't need to worry about escaping
    // characters such as the quotes around the JSON properties
    private val testJson = """{"id": 1, "joke": "joke"}"""

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
        val testObserver = jokService.geteRandomJoke().test()
        // Verify that the value that returns is a joke object with the same values you placed in the
        // testJson and enqueued with MockWebServer.
        testObserver.assertValue(Joke("1", "joke"))
    }

}

class JokeServiceTestUsingMockWebServer {

}