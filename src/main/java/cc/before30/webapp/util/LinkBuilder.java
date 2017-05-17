package cc.before30.webapp.util;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Created by before30 on 17/05/2017.
 */
public class LinkBuilder {
    public static Link build(String requestMapping, String name, String rel) {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentServletMapping();
        String root = builder.build().toString();
        String href = root + requestMapping;
        return new Link(name, href, rel);
    }

    public static Link build(String requestMapping, String name) {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentServletMapping();
        String root = builder.build().toString();
        String href = root + requestMapping;
        return new Link(name, href, Link.REL_SELF);
    }
}
