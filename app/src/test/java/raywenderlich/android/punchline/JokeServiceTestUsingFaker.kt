package raywenderlich.android.punchline

import com.github.javafaker.Faker
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.raywenderlich.android.punchline.Joke
import com.raywenderlich.android.punchline.JokeService
import com.raywenderlich.android.punchline.RepositoryImpl
import io.reactivex.Single
import org.junit.Test

class JokeServiceTestUsingFaker {

    var faker = Faker()
    private val jokeService: JokeService = mock()
    private val repository = RepositoryImpl(jokeService)

    @Test
    fun getRandomJokeEmitsJoke() {
        val joke = Joke(
                faker.idNumber().valid(),
                faker.lorem().sentence()
        )

        whenever(jokeService.geteRandomJoke()).thenReturn(Single.just(joke))
        val testObserver = repository.getJoke().test()

        testObserver.assertValue(joke)


    }
}