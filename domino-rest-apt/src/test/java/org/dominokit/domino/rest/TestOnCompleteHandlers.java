package org.dominokit.domino.rest;

import org.dominokit.domino.rest.shared.request.service.annotations.RequestFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

public class TestOnCompleteHandlers {

    public static void main(String[] args) {

        TestOnCompleteHandlers_UserServiceFactory.INSTANCE
                .get()
                .onSuccess(response -> {
                })
                .onFailed(failedResponse -> {
                })
                .onComplete(() -> {
                })
                .send();
    }

    @RequestFactory
    public interface UserService {

        @Path("/")
        @GET
        User get();
    }

    public static class User {
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}
