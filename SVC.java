import java.io.*;
import java.net.*;
import java.util.*;
import com.sun.net.httpserver.*;
import javax.json.*;

public class SVC {
  public static void main(String[] args) throws Exception {
    System.out.println("Hi");
 
    HttpServer server = HttpServer.create(new InetSocketAddress(4008), 0);
    server.createContext("/form", new FormHandler());
    server.createContext("/json", new JsonHandler());
    server.setExecutor(null); // creates a default executor
    server.start();
  }

  static class FormHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
      String response =
        "<html>" +
        "<form action=\"/json\">" +
        "  <textarea rows=10 columns=120 name=\"text\">" +
        "  </textarea>" +
        "  <input type=\"submit\" name=\"btn\"/>" +
        "</form>" +
        "</html>";
      t.sendResponseHeaders(200, response.length());
      OutputStream os = t.getResponseBody();
      os.write(response.getBytes());
      os.close();
    }
  }

  static class JsonHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
    try {
      handle2(t);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

    public void handle2(HttpExchange t) throws IOException {
      System.err.println("handle");
      Map<String, List<String>> sp = splitQuery(t.getRequestURI().getQuery());
      String json_str = sp.get("text").get(0);
      System.err.println(json_str);

      JsonReader jsonReader = Json.createReader(new StringReader(json_str));
      JsonObject jobj = jsonReader.readObject();

      JsonArray ja = jobj.getJsonArray("a");
      System.err.println(ja);
      JsonArray jb = jobj.getJsonArray("b");
      System.err.println(jb);

      JsonBuilderFactory factory = Json.createBuilderFactory(null);
      JsonArrayBuilder jresult = factory.createArrayBuilder();

      for (int i=0; i<ja.size(); ++i) {
        long result = 0;
        JsonArray row = ja.getJsonArray(i);
        for (int j=0; j<jb.size(); ++j) {
	  result += row.getInt(j)*jb.getInt(j);
        }
	jresult.add(result);
      }

      JsonObject value = factory.createObjectBuilder().add("result", jresult.build()).build();
      String response = value.toString();
      System.err.println(response);
      t.sendResponseHeaders(200, response.length());
      OutputStream os = t.getResponseBody();
      os.write(response.getBytes());
      os.close();
    }
  }

  public static Map<String, List<String>> splitQuery(String qp) throws UnsupportedEncodingException {
    final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
    final String[] pairs = qp.split("&");
    for (String pair : pairs) {
      final int idx = pair.indexOf("=");
      final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
      if (!query_pairs.containsKey(key)) {
	query_pairs.put(key, new LinkedList<String>());
      }
      final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
      query_pairs.get(key).add(value);
    }
    return query_pairs;
  }

}
