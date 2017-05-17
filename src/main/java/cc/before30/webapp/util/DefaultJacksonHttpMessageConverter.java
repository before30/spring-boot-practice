package cc.before30.webapp.util;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by before30 on 17/05/2017.
 */
public class DefaultJacksonHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private ObjectMapper objectMapper = new ObjectMapper();
    private boolean prefixJson = false;
    private boolean prettyPrint =false;

    public DefaultJacksonHttpMessageConverter() {
        super(new MediaType("application", "json", DEFAULT_CHARSET));

        objectMapper.registerModule(new JodaModule());
        objectMapper.registerModule(new GuavaModule());

    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        JavaType javaType = getJavaType(clazz);
        return objectMapper.canDeserialize(javaType) && canRead(mediaType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return objectMapper.canSerialize(clazz) && canWrite(mediaType);
    }

    protected JavaType getJavaType(Class<?> clazz) {
        return TypeFactory.defaultInstance().constructType(clazz);
    }



    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        JavaType javaType = getJavaType(clazz);

        try {
            return objectMapper.readValue(inputMessage.getBody(), javaType);
        } catch (JsonParseException ex) {
            throw new HttpMessageNotReadableException("Could not read JSON: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        JsonEncoding encoding = getEncoding(outputMessage.getHeaders().getContentType());
        JsonGenerator jsonGenerator = getObjectMapper().getJsonFactory().createJsonGenerator(outputMessage.getBody(),
                encoding);

        if (prefixJson) {
            jsonGenerator.writeRaw("{} && ");
        }
        if (isPrettyPrint()) {
            jsonGenerator.useDefaultPrettyPrinter();
        }
        getObjectMapper().writeValue(jsonGenerator, o);
    }

    private JsonEncoding getEncoding(MediaType contentType) {
        if (contentType != null && contentType.getCharSet() != null) {
            Charset charset = contentType.getCharSet();
            for (JsonEncoding encoding : JsonEncoding.values()) {
                if (charset.name().equals(encoding.getJavaName())) {
                    return encoding;
                }
            }
        }
        return JsonEncoding.UTF8;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        Assert.notNull(objectMapper, "'objectMapper' must not be null");
        this.objectMapper = objectMapper;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public void setPrefixJson(boolean prefixJson) {
        this.prefixJson = prefixJson;
    }
}
