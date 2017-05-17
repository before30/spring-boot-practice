package cc.before30.webapp.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created by before30 on 17/05/2017.
 */
@Embeddable
@Getter
@EqualsAndHashCode
@ToString
public class Link implements Serializable {

    private static final long serialVersionUID = 7758377561787616653L;

    public static final String REL_SELF = "self";
    public static final String REL_PARENT = "parent";
    public static final String REL_FIRST = "first";
    public static final String REL_PREVIOUS = "previous";
    public static final String REL_NEXT = "next";
    public static final String REL_LAST = "last";

    private String name;
    private String rel;
    private String href;

    public Link(String href) {
        this("", href, REL_SELF);
    }

    public Link(String name, String href, String rel) {
        this.name = name;
        this.href = href;
        this.rel = rel;
    }

    protected Link() {}
}
