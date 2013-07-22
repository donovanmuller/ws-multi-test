package controllers;

import java.util.HashMap;
import java.util.Map;

import play.Logger;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.index;

public class Application extends Controller {

    private static Map<String, WebSocket.Out<String>> sockets = new HashMap<>();

    public static Result index() {
        return ok(index.render());
    }

    public static Result hello() {
        String message = request().getQueryString("message");
        for (Map.Entry<String, WebSocket.Out<String>> socket : sockets.entrySet()) {
            Logger.debug("Writing message to: " + socket.getKey());
            socket.getValue().write(message);
        }

        return ok(index.render());
    }

    public static Result reset() {
        for (Map.Entry<String, WebSocket.Out<String>> socket : sockets.entrySet()) {
            socket.getValue().close();
        }
        sockets.clear();

        return ok(index.render());
    }

    public static WebSocket<String> echo(final String uuid) {
        WebSocket<String> socket = new TestSocket(uuid);

        return socket;
    }

    static class TestSocket extends WebSocket<String> {

        private String uuid;

        TestSocket(String uuid) {
            this.uuid = uuid;
        }

        @Override
        public void onReady(final In<String> in, final Out<String> out) {

            in.onClose(new F.Callback0() {

                @Override
                public void invoke() throws Throwable {
                    Logger.debug("Closing: " + uuid);
                }
            });

            sockets.put(uuid, out);
        }
    }
}
