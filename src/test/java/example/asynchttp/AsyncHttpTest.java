package example.asynchttp;

import example.monads.Try;
import org.apache.commons.codec.digest.DigestUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.asynchttpclient.Dsl.asyncHttpClient;

public class AsyncHttpTest {

    @Test
    public void testAsyncHttp() throws IOException {
        try (AsyncHttpClient asyncHttpClient = asyncHttpClient()) {
            CompletableFuture<String> future = asyncHttpClient
                    .prepareGet("http://bipif-test.4tek.de/flexout/2018.10/2552/html/unaktienglob_fondsportrait.html1")
                    .execute()
                    .toCompletableFuture()
                    .thenApply(r -> {
                        if (r.getStatusCode() > 300) {
                            throw new RuntimeException("Response code is " + r.getStatusCode());
                        }
                        return r;
                    })
                    .thenApply(Response::getResponseBody)
                    .thenApply(DigestUtils::md5Hex)
                    .thenApply(String::toUpperCase);

            String result = Try.ofFailable(() -> future.join())
                    .recoverWith(t -> Try.ofFailable(() -> t.getMessage()))
                    .getUnchecked();
            System.out.println(result);
        }
    }
}