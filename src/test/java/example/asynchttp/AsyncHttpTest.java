package example.asynchttp;

import example.monads.Try;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.asynchttpclient.Dsl.asyncHttpClient;

//https://jfarcand.wordpress.com/2010/12/21/going-asynchronous-using-asynchttpclient-the-basic/
public class AsyncHttpTest {

    @Test
    public void testAsyncHttp() throws IOException {
        try (AsyncHttpClient asyncHttpClient = asyncHttpClient()) {
            CompletableFuture<String> future = asyncHttpClient
                    .prepareGet("http://bipif-test.4tek.de/flexout/2018.10/2552/html/unaktienglob_fondsportrait.html1")
                    .execute()
                    .toCompletableFuture()
                    .thenApply(r -> {
                        if (r.getStatusCode() != HttpResponseStatus.OK.code()) {
                            throw new RuntimeException("Response code is " + r.getStatusCode());
                        }
                        return r;
                    })
                    .thenApply(Response::getResponseBody)
                    .thenApply(DigestUtils::md5Hex)
                    .thenApply(String::toUpperCase);

            String result = Try.of(() -> future.join())
                    .recoverWith(t -> Try.of(() -> t.getMessage()))
                    .getUnchecked();
            System.out.println(result);

        }
    }

    @Test
    public void testPromiseRequest() throws IOException {
        try (AsyncHttpClient asyncHttpClient = asyncHttpClient()) {
            CompletableFuture<String> reply = loadTag(asyncHttpClient, "http://bipif-test.4tek.de/flexout/2018.10/2552/html/unaktienglob_fondsportrait.html1")
                    .thenApply(DigestUtils::md5Hex)
                    .thenApply(String::toUpperCase);

            String result = Try.of(() -> reply.join())
                    .recoverWith(t -> Try.of(() -> ExceptionUtils.getRootCause(t).getMessage()))
                    .getUnchecked();
            System.out.println(result);
        }
    }

    public CompletableFuture<String> loadTag(AsyncHttpClient asyncHttpClient, String urlString) throws IOException {
        final CompletableFuture<String> promise = new CompletableFuture<>();
        asyncHttpClient.prepareGet(urlString).execute(
                new AsyncCompletionHandler<Void>() {

                    @Override
                    public Void onCompleted(Response response) throws Exception {
                        if (response.getStatusCode() != HttpResponseStatus.OK.code()) {
                            promise.completeExceptionally(new IOException("Response code is " + response.getStatusCode()));
                        } else {
                            promise.complete(response.getResponseBody());
                        }
                        return null;
                    }

                    @Override
                    public void onThrowable(Throwable t) {
                        promise.completeExceptionally(t);
                    }
                }
        );
        return promise;
    }
}