package adapters.API.hateoas;

import jakarta.ws.rs.core.Link;
import java.util.ArrayList;
import java.util.List;

public class ResourceModel<T> {

    private T data;
    private List<Link> links = new ArrayList<>();

    public ResourceModel(T data) {
        this.data = data;
    }

    public T getData() { return data; }
    public List<Link> getLinks() { return links; }

    public void addLink(Link link) {
        links.add(link);
    }
}