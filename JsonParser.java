import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A small hand-written JSON reader (no external libraries needed).
 * Returns Map (object), List (array), String, Double, Boolean or null.
 */
public class JsonParser {
    private final String s;
    private int pos = 0;

    private JsonParser(String s) { this.s = s; }

    public static Object parse(String text) throws CorruptFileException {
        JsonParser p = new JsonParser(text);
        p.skipWhitespace();
        Object value = p.readValue();
        p.skipWhitespace();
        if (p.pos != p.s.length()) throw p.error("unexpected text after the JSON data");
        return value;
    }

    private CorruptFileException error(String message) {
        return new CorruptFileException("Invalid JSON: " + message + " (at character " + pos + ")");
    }

    private char peek() { return pos < s.length() ? s.charAt(pos) : '\0'; }

    private void skipWhitespace() {
        while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
    }

    private Object readValue() throws CorruptFileException {
        if (pos >= s.length()) throw error("unexpected end of file");
        char c = peek();
        if (c == '{') return readObject();
        if (c == '[') return readArray();
        if (c == '"') return readString();
        if (s.startsWith("true", pos))  { pos += 4; return Boolean.TRUE; }
        if (s.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
        if (s.startsWith("null", pos))  { pos += 4; return null; }
        return readNumber();
    }

    private Map<String, Object> readObject() throws CorruptFileException {
        Map<String, Object> map = new LinkedHashMap<>();
        pos++;                                  // skip {
        skipWhitespace();
        if (peek() == '}') { pos++; return map; }
        while (true) {
            skipWhitespace();
            if (peek() != '"') throw error("expected a quoted key");
            String key = readString();
            skipWhitespace();
            if (peek() != ':') throw error("expected ':' after key");
            pos++;
            skipWhitespace();
            map.put(key, readValue());
            skipWhitespace();
            char c = peek();
            if (c == ',') { pos++; continue; }
            if (c == '}') { pos++; return map; }
            throw error("expected ',' or '}'");
        }
    }

    private List<Object> readArray() throws CorruptFileException {
        List<Object> list = new ArrayList<>();
        pos++;                                  // skip [
        skipWhitespace();
        if (peek() == ']') { pos++; return list; }
        while (true) {
            skipWhitespace();
            list.add(readValue());
            skipWhitespace();
            char c = peek();
            if (c == ',') { pos++; continue; }
            if (c == ']') { pos++; return list; }
            throw error("expected ',' or ']'");
        }
    }

    private String readString() throws CorruptFileException {
        pos++;                                  // skip opening quote
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (pos >= s.length()) throw error("text is missing its closing quote");
            char c = s.charAt(pos++);
            if (c == '"') return sb.toString();
            if (c != '\\') { sb.append(c); continue; }
            if (pos >= s.length()) throw error("bad escape at end of file");
            char e = s.charAt(pos++);
            switch (e) {
                case 'n': sb.append('\n'); break;
                case 't': sb.append('\t'); break;
                case 'r': sb.append('\r'); break;
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case '"': sb.append('"'); break;
                case '\\': sb.append('\\'); break;
                case '/': sb.append('/'); break;
                case 'u':
                    if (pos + 4 > s.length()) throw error("bad unicode escape");
                    try {
                        sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
                    } catch (NumberFormatException ex) {
                        throw error("bad unicode escape");
                    }
                    pos += 4;
                    break;
                default: throw error("unknown escape \\" + e);
            }
        }
    }

    private Double readNumber() throws CorruptFileException {
        int start = pos;
        while (pos < s.length() && "+-0123456789.eE".indexOf(s.charAt(pos)) >= 0) pos++;
        if (start == pos) throw error("unexpected character '" + peek() + "'");
        try {
            return Double.valueOf(s.substring(start, pos));
        } catch (NumberFormatException ex) {
            throw error("bad number");
        }
    }
}
